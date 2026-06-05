package com.macro.mall.ai.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.macro.mall.ai.client.AiClient;
import com.macro.mall.ai.config.PromptProperties;
import com.macro.mall.ai.domain.*;
import com.macro.mall.ai.service.AiAssistantService;
import com.macro.mall.ai.service.ReturnReasonService;
import com.macro.mall.ai.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
 * <p><b>Stage 2 改造：</b></p>
 * <ul>
 *   <li>100+ 行硬编码 Prompt 迁出到 {@code application.yml} 的 {@code ai.prompts.*}</li>
 *   <li>注入 {@link PromptProperties}，通过 {@code prompts.productQaSystem()} 等方法读取</li>
 *   <li>硬编码的 fallback 字符串（"质量问题" / "硬件故障"）改为配置化</li>
 *   <li>使用 {@code org.springframework.ai.chat.prompt.PromptTemplate} 占位符渲染（Stage 3 引入）</li>
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

    /**
     * Prompt 配置 (Stage 2)
     * <p>从 application.yml 注入，存储商品问答/退货建议的系统提示词和 fallback 默认值。</p>
     */
    private final PromptProperties prompts;

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
        String reply = aiClient.chat(prompts.productQaSystem(), content);

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
     *
     * <p>Stage 2：从 {@link PromptProperties#returnSuggestionSystem()} 读取模板，
     * 用 {@code {reasons}} 占位符动态填充从数据库加载的退货原因列表。</p>
     */
    private String buildReturnSystemPrompt() {
        List<String> reasons = returnReasonService.getEnabledReturnReasons();
        String reasonsStr = String.join("、", reasons);

        // Stage 2: 用 String.replace 占位符（Stage 3 替换为 PromptTemplate）
        return prompts.returnSuggestionSystem().replace("{reasons}", reasonsStr);
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
                if (reason == null || reason.isEmpty()) reason = prompts.returnReasonDefault();
                if (description == null || description.isEmpty()) description = fallbackIssue;
                if (category == null || category.isEmpty()) category = prompts.categoryDefault();
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
                    prompts.returnReasonDefault(), fallbackIssue, prompts.categoryDefault(), "low",
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
