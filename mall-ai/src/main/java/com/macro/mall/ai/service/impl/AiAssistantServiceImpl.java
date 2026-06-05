package com.macro.mall.ai.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.macro.mall.ai.client.AiClient;
import com.macro.mall.ai.domain.*;
import com.macro.mall.ai.service.AiAssistantService;
import com.macro.mall.ai.service.ReturnReasonService;
import com.macro.mall.ai.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 助手服务实现类 (AI Assistant Service Implementation)
 *
 * <p>实现 AI 购物助手的核心业务逻辑，包括商品问答和退货建议两大功能。</p>
 *
 * <p><b>Stage 1 改造：</b></p>
 * <ul>
 *   <li>DTO 全部改为 Java 17 record，业务代码用 record accessor 替代 getter</li>
 *   <li>{@code @Autowired} 字段注入改为 Lombok {@code @RequiredArgsConstructor} 构造器注入</li>
 *   <li>JSON 解析时构造新 record 替代 setter 原地修改</li>
 * </ul>
 *
 * @author alan
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    /**
     * 日志记录器 (Logger)
     * <p>用于记录 AI 调用过程、错误信息等</p>
     */
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

    /**
     * AI 客户端实例 (AI Client Instance)
     * <p>用于调用外部 AI API（DeepSeek/OpenAI/SiliconFlow 等）</p>
     * <p>通过 Lombok {@code @RequiredArgsConstructor} 生成构造器注入</p>
     */
    private final AiClient aiClient;

    /**
     * 退货原因服务实例 (Return Reason Service Instance)
     * <p>用于从数据库动态获取启用的退货原因列表</p>
     * <p>确保 AI 推荐的退货原因与后台配置保持一致</p>
     */
    private final ReturnReasonService returnReasonService;

    @Override
    public AiResponse chatAboutProduct(ProductQaRequest request) {
        // 【步骤1】安全清理：对用户问题进行过滤，防止 Prompt Injection 攻击
        String sanitizedQuestion = InputSanitizer.sanitize(request.question());

        // 【步骤2】构建上下文：组装商品信息（名称、品牌、价格、描述）
        String context = buildProductContext(request);

        // 【步骤3】构建完整内容：拼接商品信息 + 对话历史（可选） + 用户问题
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(context);  // 添加商品信息

        // 添加对话历史（如果存在多轮对话）
        if (request.conversationHistory() != null && !request.conversationHistory().isEmpty()) {
            contentBuilder.append("\n\n【对话历史】\n").append(request.conversationHistory());
        }

        // 添加当前用户问题
        contentBuilder.append("\n\n【顾客问题】").append(sanitizedQuestion);

        String content = contentBuilder.toString();

        // 【步骤4】记录日志：便于追踪和调试
        log.info("AI product Q&A - productId={}, question={}, hasHistory={}",
                request.productId(), sanitizedQuestion,
                request.conversationHistory() != null && !request.conversationHistory().isEmpty());

        // 【步骤5】调用 AI 客户端：传入系统提示词和用户内容，获取 AI 回复
        String reply = aiClient.chat(QA_SYSTEM_PROMPT, content);

        // 【步骤6】封装响应：将 AI 回复包装成 AiResponse 对象返回
        return new AiResponse(reply);
    }

    @Override
    public ReturnSuggestionResult suggestReturn(ReturnSuggestionRequest request) {
        // 【步骤1】安全清理：对用户问题描述进行过滤，防止恶意输入
        String sanitizedIssue = InputSanitizer.sanitize(request.issue());

        // 【步骤2】动态构建 Prompt：从数据库获取启用的退货原因列表，生成系统提示词
        String systemPrompt = buildReturnSystemPrompt();

        // 【步骤3】获取当前步骤：默认为第1步（询问故障现象）
        int currentStep = request.step() == null ? 1 : request.step();

        // 【步骤4】会话管理：如果没有 sessionId，生成新的 UUID
        String sessionId = request.sessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = java.util.UUID.randomUUID().toString();  // 生成唯一会话ID
        }

        // 【步骤5】构建用户内容：包含当前步骤、问题描述、商品信息等
        String content = String.format(
                "当前引导步骤：%d/3\n用户描述的问题：%s\n商品名称：%s\n商品属性：%s\n订单编号：%s",
                currentStep,
                sanitizedIssue,
                InputSanitizer.sanitizeProductInfo(nullToEmpty(request.productName())),
                InputSanitizer.sanitizeProductInfo(nullToEmpty(request.productAttr())),
                nullToEmpty(request.orderSn())
        );

        // 【步骤6】记录日志：追踪引导进度
        log.info("AI return suggest - step={}, issue={}", currentStep, sanitizedIssue);

        // 【步骤7】调用 AI 客户端：传入系统提示词和用户内容，获取 JSON 格式响应
        String jsonResponse = aiClient.chat(systemPrompt, content);

        // 【步骤8】解析响应：解析 JSON，应用强制校验逻辑，返回结果
        return parseReturnSuggestion(jsonResponse, sanitizedIssue, currentStep, sessionId);
    }

    /**
     * 动态生成退货建议系统 Prompt (Build Return System Prompt Dynamically)
     */
    private String buildReturnSystemPrompt() {
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
               "  - 你会收到'用户描述的问题'中包含完整的对话历史（用；分隔）\n" +
               "  - ️ description 必须基于所有对话内容生成，包含具体问题和细节！\n" +
               "  - 正确示例：'商品镜头内部进灰，从购买时一直存在，影响拍照效果'\n" +
               "  - 错误示例：'该商品一直存在故障'（太笼统，缺少具体问题）\n" +
               "  - 确认语句示例：'明白了，这确实影响了您的正常使用。我将为您推荐最合适的退货原因。'\n\n" +
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

    /**
     * 构建商品信息上下文 (Build Product Context)
     */
    private String buildProductContext(ProductQaRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("【商品信息】\n");
        sb.append("名称：").append(InputSanitizer.sanitizeProductInfo(nullToEmpty(request.productName()))).append("\n");
        sb.append("品牌：").append(InputSanitizer.sanitizeProductInfo(nullToEmpty(request.productBrand()))).append("\n");
        sb.append("价格：").append(InputSanitizer.sanitizeProductInfo(nullToEmpty(request.productPrice()))).append("元\n");
        sb.append("描述：").append(InputSanitizer.sanitizeProductInfo(nullToEmpty(request.productSubTitle())));
        return sb.toString();
    }

    /**
     * 解析退货建议响应 (Parse Return Suggestion Response)
     *
     * <p>Stage 1 改造：因为 record 不可变，每次修改都生成新实例。</p>
     */
    private ReturnSuggestionResult parseReturnSuggestion(String json, String fallbackIssue, int currentStep, String sessionId) {
        try {
            // 【步骤1】清理 JSON：去除首尾空白
            String cleaned = json.trim();

            // 【步骤2】处理 Markdown 代码块
            if (cleaned.startsWith("```")) {
                int start = cleaned.indexOf('\n');
                if (start > 0) cleaned = cleaned.substring(start + 1);
                int end = cleaned.lastIndexOf("```");
                if (end > 0) cleaned = cleaned.substring(0, end);
                cleaned = cleaned.trim();
            }

            // 【步骤3】解析 JSON
            JSONObject obj = JSONUtil.parseObj(cleaned);

            // 【步骤4】提取字段
            String reason = obj.getStr("reason", "");
            String description = obj.getStr("description", "");
            String category = obj.getStr("category", "");
            String confidence = obj.getStr("confidence", "medium");
            String guideQuestion = obj.getStr("guideQuestion", "");
            Boolean finished = obj.getBool("finished", false);

            // 【步骤5】强制校验：如果是第3步，必须结束对话并给出建议
            if (currentStep >= 3) {
                finished = true;
                if (reason == null || reason.isEmpty()) reason = "质量问题";
                if (description == null || description.isEmpty()) description = fallbackIssue;
                if (category == null || category.isEmpty()) category = "硬件故障";
            }

            // 【步骤6】生成分析说明
            String analysisNote;
            if (Boolean.TRUE.equals(finished)) {
                String truncatedIssue = fallbackIssue.length() > 20
                    ? fallbackIssue.substring(0, 20) + "..."
                    : fallbackIssue;
                analysisNote = String.format("根据描述'%s'，判断为%s，匹配'%s'原因",
                        truncatedIssue, category, reason);
            } else {
                analysisNote = "正在引导您完善问题描述...";
            }

            // 【步骤7】记录成功日志
            log.info("AI 退货建议解析成功 - step={}, finished={}, reason={}",
                    currentStep, finished, reason);

            return new ReturnSuggestionResult(
                reason, description, category, confidence, guideQuestion, finished, analysisNote);

        } catch (Exception e) {
            // 【异常处理】JSON 解析失败时的兜底逻辑
            log.warn("Failed to parse AI return suggestion JSON, using fallback. Raw: {}", json, e);

            if (currentStep >= 3) {
                return new ReturnSuggestionResult(
                    "质量问题", fallbackIssue, "硬件故障", "low",
                    "已为您生成建议，请确认。", true,
                    "解析失败，但已为您生成默认建议");
            }
            return new ReturnSuggestionResult(
                "", "", "", "low",
                "抱歉，我没听清。请问具体是哪里出现了问题？", false,
                "解析失败，请重试");
        }
    }

    /**
     * 空值转空字符串工具方法 (Null to Empty String Utility)
     */
    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
