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

/**
 * AI 助手服务实现类 (AI Assistant Service Implementation)
 * 
 * <p>实现 AI 购物助手的核心业务逻辑，包括商品问答和退货建议两大功能。</p>
 * 
 * <p><b>主要职责：</b></p>
 * <ul>
 *   <li>构建系统提示词（System Prompt），定义 AI 的回答规范和约束</li>
 *   <li>对用户输入进行安全清理，防止 Prompt Injection 攻击</li>
 *   <li>调用 AI 客户端生成回答</li>
 *   <li>解析 AI 响应，处理异常情况并提供兜底逻辑</li>
 * </ul>
 * 
 * <p><b>设计特点：</b></p>
 * <ul>
 *   <li>使用静态常量定义 System Prompt，避免重复构建</li>
 *   <li>动态从数据库获取退货原因列表，确保与后台配置一致</li>
 *   <li>实现强制逻辑校验，防止 AI 不按步骤执行</li>
 *   <li>完善的异常处理和 fallback 机制</li>
 * </ul>
 * 
 * @author alan
 * @since 1.0
 */
@Service
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
     * <p>通过 Spring 依赖注入自动装配</p>
     */
    @Autowired
    private AiClient aiClient;

    /**
     * 退货原因服务实例 (Return Reason Service Instance)
     * <p>用于从数据库动态获取启用的退货原因列表</p>
     * <p>确保 AI 推荐的退货原因与后台配置保持一致</p>
     */
    @Autowired
    private ReturnReasonService returnReasonService;

    @Override
    public AiResponse chatAboutProduct(ProductQaRequest request) {
        // 【步骤1】安全清理：对用户问题进行过滤，防止 Prompt Injection 攻击
        String sanitizedQuestion = InputSanitizer.sanitize(request.getQuestion());
        
        // 【步骤2】构建上下文：组装商品信息（名称、品牌、价格、描述）
        String context = buildProductContext(request);
        
        // 【步骤3】构建完整内容：拼接商品信息 + 对话历史（可选） + 用户问题
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(context);  // 添加商品信息
        
        // 添加对话历史（如果存在多轮对话）
        if (request.getConversationHistory() != null && !request.getConversationHistory().isEmpty()) {
            contentBuilder.append("\n\n【对话历史】\n").append(request.getConversationHistory());
        }
        
        // 添加当前用户问题
        contentBuilder.append("\n\n【顾客问题】").append(sanitizedQuestion);
        
        String content = contentBuilder.toString();

        // 【步骤4】记录日志：便于追踪和调试
        log.info("AI product Q&A - productId={}, question={}, hasHistory={}", 
                request.getProductId(), sanitizedQuestion, 
                request.getConversationHistory() != null && !request.getConversationHistory().isEmpty());
        
        // 【步骤5】调用 AI 客户端：传入系统提示词和用户内容，获取 AI 回复
        String reply = aiClient.chat(QA_SYSTEM_PROMPT, content);

        // 【步骤6】封装响应：将 AI 回复包装成 AiResponse 对象返回
        return new AiResponse(reply);
    }

    @Override
    public ReturnSuggestionResult suggestReturn(ReturnSuggestionRequest request) {
        // 【步骤1】安全清理：对用户问题描述进行过滤，防止恶意输入
        String sanitizedIssue = InputSanitizer.sanitize(request.getIssue());
        
        // 【步骤2】动态构建 Prompt：从数据库获取启用的退货原因列表，生成系统提示词
        String systemPrompt = buildReturnSystemPrompt();
        
        // 【步骤3】获取当前步骤：默认为第1步（询问故障现象）
        int currentStep = request.getStep() == null ? 1 : request.getStep();
        
        // 【步骤4】会话管理：如果没有 sessionId，生成新的 UUID
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = java.util.UUID.randomUUID().toString();  // 生成唯一会话ID
        }

        // 【步骤5】构建用户内容：包含当前步骤、问题描述、商品信息等
        String content = String.format(
                "当前引导步骤：%d/3\n用户描述的问题：%s\n商品名称：%s\n商品属性：%s\n订单编号：%s",
                currentStep,
                sanitizedIssue,
                InputSanitizer.sanitizeProductInfo(nullToEmpty(request.getProductName())),
                InputSanitizer.sanitizeProductInfo(nullToEmpty(request.getProductAttr())),
                nullToEmpty(request.getOrderSn())
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
     * 
     * <p>从数据库获取启用的退货原因列表，动态构建系统提示词。</p>
     * 
     * <p><b>设计目的：</b></p>
     * <ul>
     *   <li>确保 AI 推荐的退货原因与后台配置保持一致</li>
     *   <li>支持不同商城自定义退货政策</li>
     *   <li>查询失败时使用默认列表降级</li>
     * </ul>
     * 
     * @return 完整的系统提示词字符串，包含退货原因选项、3轮引导流程、输出格式要求
     */
    private String buildReturnSystemPrompt() {
        // 从数据库动态获取启用的退货原因列表（如：质量问题、商品损坏、7天无理由退货等）
        List<String> reasons = returnReasonService.getEnabledReturnReasons();
        // 将列表转换为字符串，用顿号分隔（如：“质量问题、商品损坏、7天无理由退货”）
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
     * 
     * <p>将商品的各项信息格式化为结构化文本，供 AI 参考。</p>
     * 
     * <p><b>输出格式示例：</b></p>
     * <pre>{@code
     * 【商品信息】
     * 名称：Redmi Note 13
     * 品牌：小米
     * 价格：1999元
     * 描述：性能小钢炮 5G 手机
     * }</pre>
     * 
     * @param request 商品问答请求参数，包含商品名称、品牌、价格、描述等信息
     * @return 格式化后的商品信息字符串
     */
    // 商品信息
    private String buildProductContext(ProductQaRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("【商品信息】\n");  // 标题
        // 商品名称（经过安全清理，限制长度1000字符）
        sb.append("名称：").append(InputSanitizer.sanitizeProductInfo(nullToEmpty(request.getProductName()))).append("\n");
        // 商品品牌
        sb.append("品牌：").append(InputSanitizer.sanitizeProductInfo(nullToEmpty(request.getProductBrand()))).append("\n");
        // 商品价格（单位：元）
        sb.append("价格：").append(InputSanitizer.sanitizeProductInfo(nullToEmpty(request.getProductPrice()))).append("元\n");
        // 商品副标题/描述
        sb.append("描述：").append(InputSanitizer.sanitizeProductInfo(nullToEmpty(request.getProductSubTitle())));
        return sb.toString();
    }
    /**
     * 解析退货建议响应 (Parse Return Suggestion Response)
     * 
     * <p>解析 AI 返回的 JSON 格式响应，应用强制校验逻辑，提供兜底方案。</p>
     * 
     * <p><b>处理流程：</b></p>
     * <ol>
     *   <li>清理 JSON 字符串（去除 Markdown 代码块标记）</li>
     *   <li>解析 JSON 字段（reason、description、category、confidence、guideQuestion、finished）</li>
     *   <li>强制校验：如果是第3步，确保 finished=true，并提供默认值兜底</li>
     *   <li>生成分析说明：记录 AI 的推理过程</li>
     *   <li>异常处理：解析失败时使用 fallback 默认值</li>
     * </ol>
     * 
     * @param json AI 返回的原始 JSON 字符串
     * @param fallbackIssue 原始问题描述（用于兜底）
     * @param currentStep 当前引导步骤（1-3）
     * @param sessionId 会话ID（预留，未来可用于会话跟踪）
     * @return 解析后的退货建议结果对象
     */
    //
    private ReturnSuggestionResult parseReturnSuggestion(String json, String fallbackIssue, int currentStep, String sessionId) {
        ReturnSuggestionResult result = new ReturnSuggestionResult();
        try {
            // 【步骤1】清理 JSON：去除首尾空白
            String cleaned = json.trim();
            
            // 【步骤2】处理 Markdown 代码块：如果 AI 返回 ```json ... ```，提取中间的 JSON 内容
            if (cleaned.startsWith("```")) {
                int start = cleaned.indexOf('\n');  // 找到第一个换行符
                if (start > 0) cleaned = cleaned.substring(start + 1);  // 去除 ```json
                int end = cleaned.lastIndexOf("```");  // 找到最后一个 ```
                if (end > 0) cleaned = cleaned.substring(0, end);  // 去除末尾的 ```
                cleaned = cleaned.trim();
            }
            
            // 【步骤3】解析 JSON：使用 Hutool 工具类解析
            JSONObject obj = JSONUtil.parseObj(cleaned);
            
            // 【步骤4】提取字段：从 JSON 中获取各个字段的值
            result.setSuggestedReason(obj.getStr("reason", ""));  // 退货原因
            result.setSuggestedDescription(obj.getStr("description", ""));  // 问题描述
            result.setCategory(obj.getStr("category", ""));  // 问题分类
            result.setConfidence(obj.getStr("confidence", "medium"));  // 置信度（默认 medium）
            result.setGuideQuestion(obj.getStr("guideQuestion", ""));  // 引导问题
            result.setFinished(obj.getBool("finished", false));  // 是否完成（默认 false）
            
            // 【步骤5】强制校验：如果是第3步，必须结束对话并给出建议
            if (currentStep >= 3) {
                result.setFinished(true);  // 强制设置为完成状态
                
                // 如果 AI 没有提供退货原因，使用默认值“质量问题”
                if (result.getSuggestedReason() == null || result.getSuggestedReason().isEmpty()) {
                    result.setSuggestedReason("质量问题");
                }
                
                // 如果 AI 没有提供问题描述，使用用户原始问题作为兜底
                if (result.getSuggestedDescription() == null || result.getSuggestedDescription().isEmpty()) {
                    result.setSuggestedDescription(fallbackIssue);
                }
                
                // 如果 AI 没有提供问题分类，使用默认值“硬件故障”
                if (result.getCategory() == null || result.getCategory().isEmpty()) {
                    result.setCategory("硬件故障");
                }
            }
            
            // 【步骤6】生成分析说明：记录 AI 的推理过程，便于调试和理解
            if (result.getFinished()) {
                // 截取问题描述前20个字符，避免过长
                String truncatedIssue = fallbackIssue.length() > 20 ? fallbackIssue.substring(0, 20) + "..." : fallbackIssue;
                String analysisNote = String.format("根据描述'%s'，判断为%s，匹配'%s'原因",
                        truncatedIssue,
                        result.getCategory(),
                        result.getSuggestedReason());
                result.setAnalysisNote(analysisNote);
            } else {
                // 未完成时，显示引导进度提示
                result.setAnalysisNote("正在引导您完善问题描述...");
            }
            
            // 【步骤7】记录成功日志
            log.info("AI 退货建议解析成功 - step={}, finished={}, reason={}", 
                    currentStep, result.getFinished(), result.getSuggestedReason());
                    
        } catch (Exception e) {
            // 【异常处理】JSON 解析失败时的兜底逻辑
            log.warn("Failed to parse AI return suggestion JSON, using fallback. Raw: {}", json, e);
            
            // 根据当前步骤设置不同的默认值
            if (currentStep >= 3) {
                // 第3步：提供完整的默认建议
                result.setSuggestedReason("质量问题");
                result.setSuggestedDescription(fallbackIssue);
                result.setCategory("硬件故障");
                result.setConfidence("low");  // 置信度设为 low，表示是兜底结果
                result.setFinished(true);
                result.setGuideQuestion("已为您生成建议，请确认。");
                result.setAnalysisNote("解析失败，但已为您生成默认建议");
            } else {
                // 第1/2步：仅提供引导问题
                result.setSuggestedReason("");
                result.setSuggestedDescription("");
                result.setCategory("");
                result.setConfidence("low");
                result.setFinished(false);
                result.setGuideQuestion("抱歉，我没听清。请问具体是哪里出现了问题？");
                result.setAnalysisNote("解析失败，请重试");
            }
        }
        return result;
    }

    /**
     * 空值转空字符串工具方法 (Null to Empty String Utility)
     * 
     * <p>将 null 值转换为空字符串，避免 NullPointerException。</p>
     * 
     * <p><b>使用场景：</b></p>
     * <ul>
     *   <li>商品信息字段可能为 null，需要安全地拼接字符串</li>
     *   <li>简化代码，避免频繁的 null 检查</li>
     * </ul>
     * 
     * @param s 原始字符串（可能为 null）
     * @return 如果输入为 null 则返回空字符串 ""，否则返回原字符串
     */
    private static String nullToEmpty(String s) {
        return s == null ? "" : s;  // 三元运算符：null 转 ""
    }
}
