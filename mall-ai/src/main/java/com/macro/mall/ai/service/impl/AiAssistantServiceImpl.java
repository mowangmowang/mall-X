package com.macro.mall.ai.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.macro.mall.ai.client.AiClient;
import com.macro.mall.ai.domain.*;
import com.macro.mall.ai.service.AiAssistantService;
import com.macro.mall.ai.service.ReturnReasonService;
import com.macro.mall.ai.util.InputSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantServiceImpl.class);

    /**
     * 商品问答系统 Prompt
     * 优化版：明确回答边界、防止过度承诺、结构化回答
     */
    private static final String QA_SYSTEM_PROMPT =
            "你是专业电商购物助手，帮助顾客了解商品信息。请严格遵循以下规范：\n\n" +
            "【回答原则】\n" +
            "1. 仅基于提供的商品信息回答，不要编造或推测未提供的信息\n" +
            "2. 回答简洁明了，分点说明，便于顾客理解\n" +
            "3. 使用专业但易懂的语言，避免过度技术化\n" +
            "4. 对于不确定的信息，诚实说明'该信息暂未提供，建议咨询客服'\n\n" +
            "【禁止承诺】\n" +
            "- 不要承诺价格优惠、赠品、售后服务等未明确说明的内容\n" +
            "- 不要使用'绝对'、'保证'、'100%'等绝对化词语\n" +
            "- 不要提供竞品对比或贬低其他品牌\n" +
            "- 不要提供购买建议（如'强烈推荐'），仅客观介绍商品\n\n" +
            "【回答结构】\n" +
            "- 先直接回答顾客问题的核心\n" +
            "- 然后补充相关的商品特点（如有）\n" +
            "- 最后询问是否还有其他问题\n\n" +
            "【示例】\n" +
            "顾客问：'这款手机拍照效果怎么样？'\n" +
            "回答：'该手机配备XX万像素摄像头，支持光学防抖和夜景模式。根据商品描述，拍照效果满足日常使用需求。如果您需要了解更详细的拍照参数，建议咨询客服。还有其他问题吗？'\n\n" +
            "【重要】只使用中文回答，回答控制在100字以内。";

    @Autowired
    private AiClient aiClient;

    @Autowired
    private ReturnReasonService returnReasonService;

    @Override
    public AiResponse chatAboutProduct(ProductQaRequest request) {
        // 对用户问题进行安全清理，防止 Prompt Injection 攻击
        String sanitizedQuestion = InputSanitizer.sanitize(request.getQuestion());
        
        // 对商品信息进行基本清理
        String context = buildProductContext(request);
        String content = context + "\n\n【顾客问题】" + sanitizedQuestion;

        log.info("AI product Q&A - productId={}, question={}", request.getProductId(), sanitizedQuestion);
        String reply = aiClient.chat(QA_SYSTEM_PROMPT, content);

        return new AiResponse(reply);
    }

    @Override
    public ReturnSuggestionResult suggestReturn(ReturnSuggestionRequest request) {
        // 对用户问题描述进行安全清理
        String sanitizedIssue = InputSanitizer.sanitize(request.getIssue());
        
        // 动态生成退货建议系统 Prompt
        String systemPrompt = buildReturnSystemPrompt();
        
        int currentStep = request.getStep() == null ? 1 : request.getStep();
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = java.util.UUID.randomUUID().toString();
        }

        String content = String.format(
                "当前引导步骤：%d/3\n用户描述的问题：%s\n商品名称：%s\n商品属性：%s\n订单编号：%s",
                currentStep,
                sanitizedIssue,
                InputSanitizer.sanitizeProductInfo(nullToEmpty(request.getProductName())),
                InputSanitizer.sanitizeProductInfo(nullToEmpty(request.getProductAttr())),
                nullToEmpty(request.getOrderSn())
        );

        log.info("AI return suggest - step={}, issue={}", currentStep, sanitizedIssue);
        String jsonResponse = aiClient.chat(systemPrompt, content);

        return parseReturnSuggestion(jsonResponse, sanitizedIssue, currentStep, sessionId);
    }

    /**
     * 动态生成退货建议系统 Prompt
     * 从数据库获取启用的退货原因，确保通用性
     */
    private String buildReturnSystemPrompt() {
        // 从数据库动态获取启用的退货原因
        List<String> reasons = returnReasonService.getEnabledReturnReasons();
        String reasonsStr = String.join("、", reasons);
        
        return "你是专业电商售后客服助手，严格遵循商城售后政策。请根据用户描述的问题进行专业分析。\n\n" +
               "【退货原因选项】（必须从以下选项中选择）：\n" +
               reasonsStr + "\n\n" +
               "【3轮引导流程 - 严格按步骤执行】\n" +
               "你的任务是通过3轮对话引导用户清晰描述问题，最后给出建议。\n" +
               "⚠️ 重要：你会收到'当前引导步骤：X/3'的信息，你必须严格按照这个数字执行对应步骤！\n\n" +
               "📌 第1轮 (step=1) - 询问故障现象：\n" +
               "  - 目标：了解商品出现了什么具体问题\n" +
               "  - 示例问题：'请问商品具体出现了什么问题？是无法开机、屏幕显示异常还是有其他表现？'\n" +
               "  - 此轮不输出 reason 和 description\n\n" +
               "📌 第2轮 (step=2) - 追问细节：\n" +
               "  - 目标：了解故障的细节或用户已尝试的解决方式\n" +
               "  - 示例问题：'请问这个问题是突然出现的还是一直存在？您是否尝试过重启或其他解决方式？'\n" +
               "  - 此轮不输出 reason 和 description\n\n" +
               "📌 第3轮 (step=3) - 确认并给出建议：\n" +
               "  - 目标：确认问题影响，给出最终退货建议\n" +
               "  - 此时必须设置 finished=true，并输出 reason、description、category\n" +
               "  - 示例：'明白了，这确实影响了您的正常使用。我将为您推荐最合适的退货原因。'\n\n" +
               "【输出格式】\n" +
               "返回 JSON 格式，必须包含以下字段：\n" +
               "{\n" +
               "  \"reason\": \"退货原因（仅在 step=3 且 finished=true 时提供，否则必须为空字符串\"\n" +
               "  \"description\": \"标准化的问题描述（仅在 step=3 且 finished=true 时提供，否则必须为空字符串\"\n" +
               "  \"category\": \"问题分类（仅在 step=3 且 finished=true 时提供，否则必须为空字符串\"\n" +
               "  \"confidence\": \"置信度（high/medium/low）\"\n" +
               "  \"guideQuestion\": \"当前步骤需要问用户的问题（step=3 时为确认性语句）\"\n" +
               "  \"finished\": false // 仅在 step=3 时为 true，step=1或2时必须为 false\n" +
               "}\n\n" +
               "【重要原则】\n" +
               "- 必须严格按照收到的 step 数字执行对应步骤，不能跳步或重复\n" +
               "- step=1 和 step=2 时，finished 必须为 false，reason/description/category 必须为空字符串\n" +
               "- step=3 时，finished 必须为 true，必须提供 reason/description/category\n" +
               "- guideQuestion 必须简洁、有针对性，一次只问一个核心问题\n" +
               "- 只返回 JSON，不要包含其他文字";
    }

    private String buildProductContext(ProductQaRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("【商品信息】\n");
        sb.append("名称：").append(InputSanitizer.sanitizeProductInfo(nullToEmpty(request.getProductName()))).append("\n");
        sb.append("品牌：").append(InputSanitizer.sanitizeProductInfo(nullToEmpty(request.getProductBrand()))).append("\n");
        sb.append("价格：").append(InputSanitizer.sanitizeProductInfo(nullToEmpty(request.getProductPrice()))).append("元\n");
        sb.append("描述：").append(InputSanitizer.sanitizeProductInfo(nullToEmpty(request.getProductSubTitle())));
        return sb.toString();
    }

    private ReturnSuggestionResult parseReturnSuggestion(String json, String fallbackIssue, int currentStep, String sessionId) {
        ReturnSuggestionResult result = new ReturnSuggestionResult();
        try {
            String cleaned = json.trim();
            if (cleaned.startsWith("```")) {
                int start = cleaned.indexOf('\n');
                if (start > 0) cleaned = cleaned.substring(start + 1);
                int end = cleaned.lastIndexOf("```");
                if (end > 0) cleaned = cleaned.substring(0, end);
                cleaned = cleaned.trim();
            }
            JSONObject obj = JSONUtil.parseObj(cleaned);
            result.setSuggestedReason(obj.getStr("reason", ""));
            result.setSuggestedDescription(obj.getStr("description", ""));
            result.setCategory(obj.getStr("category", ""));
            result.setConfidence(obj.getStr("confidence", "medium"));
            result.setGuideQuestion(obj.getStr("guideQuestion", ""));
            result.setFinished(obj.getBool("finished", false));
            
            // 强制逻辑：如果是第3步，必须结束对话并给出建议
            if (currentStep >= 3) {
                result.setFinished(true);
                // 如果AI没有提供原因和描述，使用默认值
                if (result.getSuggestedReason() == null || result.getSuggestedReason().isEmpty()) {
                    result.setSuggestedReason("质量问题");
                }
                if (result.getSuggestedDescription() == null || result.getSuggestedDescription().isEmpty()) {
                    result.setSuggestedDescription(fallbackIssue);
                }
                if (result.getCategory() == null || result.getCategory().isEmpty()) {
                    result.setCategory("硬件故障");
                }
            }
            
            // 生成分析说明
            if (result.getFinished()) {
                String analysisNote = String.format("根据描述'%s'，判断为%s，匹配'%s'原因",
                        fallbackIssue.length() > 20 ? fallbackIssue.substring(0, 20) + "..." : fallbackIssue,
                        result.getCategory(),
                        result.getSuggestedReason());
                result.setAnalysisNote(analysisNote);
            } else {
                result.setAnalysisNote("正在引导您完善问题描述...");
            }
            
            log.info("AI 退货建议解析成功 - step={}, finished={}, reason={}", 
                    currentStep, result.getFinished(), result.getSuggestedReason());
        } catch (Exception e) {
            log.warn("Failed to parse AI return suggestion JSON, using fallback. Raw: {}", json, e);
            result.setSuggestedReason(currentStep >= 3 ? "质量问题" : "");
            result.setSuggestedDescription(currentStep >= 3 ? fallbackIssue : "");
            result.setCategory(currentStep >= 3 ? "硬件故障" : "");
            result.setConfidence("low");
            result.setFinished(currentStep >= 3);
            result.setGuideQuestion(currentStep >= 3 ? "已为您生成建议，请确认。" : "抱歉，我没听清。请问具体是哪里出现了问题？");
            result.setAnalysisNote(currentStep >= 3 ? "解析失败，但已为您生成默认建议" : "解析失败，请重试");
        }
        return result;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
