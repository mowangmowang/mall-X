package com.macro.mall.ai.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.macro.mall.ai.chat.AiChatService;
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
 * AI 助手服务实现类 (AI Assistant Service Implementation) - Stage 3
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
 * </ul>
 *
 * <p><b>Stage 3 改造：</b></p>
 * <ul>
 *   <li>删除 97 行手写 {@code OpenAiCompatibleClient}</li>
 *   <li>改用 Spring AI 的 {@code ChatClient}（通过 {@link AiChatService} 封装）</li>
 *   <li>删除对 {@code AiClient} 接口和 {@code ChatMessage} record 的依赖</li>
 *   <li>Stage 4 计划：用 {@code BeanOutputConverter} 替换 90 行手写 JSON 解析</li>
 * </ul>
 *
 * @author alan
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantServiceImpl.class);

    private final AiChatService aiChat;
    private final ReturnReasonService returnReasonService;
    private final PromptProperties prompts;

    @Override
    public AiResponse chatAboutProduct(ProductQaRequest request) {
        // 【步骤1】安全清理
        String sanitizedQuestion = InputSanitizer.sanitize(request.question());

        // 【步骤2】构建上下文
        String context = buildProductContext(request);

        // 【步骤3】构建完整内容
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(context);

        if (request.conversationHistory() != null && !request.conversationHistory().isEmpty()) {
            contentBuilder.append("\n\n【对话历史】\n").append(request.conversationHistory());
        }
        contentBuilder.append("\n\n【顾客问题】").append(sanitizedQuestion);

        String content = contentBuilder.toString();

        // 【步骤4】记录日志
        log.info("AI product Q&A - productId={}, question={}, hasHistory={}",
                request.productId(), sanitizedQuestion,
                request.conversationHistory() != null && !request.conversationHistory().isEmpty());

        // 【步骤5】调用 AI 客户端 (Stage 3: 通过 Spring AI ChatClient)
        String reply = aiChat.chat(prompts.productQaSystem(), content);

        // 【步骤6】封装响应
        return new AiResponse(reply);
    }

    @Override
    public ReturnSuggestionResult suggestReturn(ReturnSuggestionRequest request) {
        // 【步骤1】安全清理
        String sanitizedIssue = InputSanitizer.sanitize(request.issue());

        // 【步骤2】动态构建 Prompt
        int currentStep = request.step() == null ? 1 : request.step();
        String sessionId = request.sessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = java.util.UUID.randomUUID().toString();
        }

        // 【步骤3】构建用户内容
        String content = String.format(
                "当前引导步骤：%d/3\n用户描述的问题：%s\n商品名称：%s\n商品属性：%s\n订单编号：%s",
                currentStep,
                sanitizedIssue,
                InputSanitizer.sanitizeProductInfo(nullToEmpty(request.productName())),
                InputSanitizer.sanitizeProductInfo(nullToEmpty(request.productAttr())),
                nullToEmpty(request.orderSn())
        );

        // 【步骤4】记录日志
        log.info("AI return suggest - step={}, issue={}", currentStep, sanitizedIssue);

        // 【步骤5】调用 AI 客户端
        List<String> reasons = returnReasonService.getEnabledReturnReasons();
        String reasonsStr = String.join("、", reasons);
        String jsonResponse = aiChat.renderAndChat(
            prompts.returnSuggestionSystem(),
            Map.of("reasons", reasonsStr),
            content
        );

        // 【步骤6】解析响应
        return parseReturnSuggestion(jsonResponse, sanitizedIssue, currentStep, sessionId);
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
     * <p>Stage 3：保留手写 JSON 解析（Stage 4 用 {@code BeanOutputConverter} 替换）。</p>
     */
    private ReturnSuggestionResult parseReturnSuggestion(String json, String fallbackIssue, int currentStep, String sessionId) {
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

            String reason = obj.getStr("reason", "");
            String description = obj.getStr("description", "");
            String category = obj.getStr("category", "");
            String confidence = obj.getStr("confidence", "medium");
            String guideQuestion = obj.getStr("guideQuestion", "");
            Boolean finished = obj.getBool("finished", false);

            if (currentStep >= 3) {
                finished = true;
                if (reason == null || reason.isEmpty()) reason = prompts.returnReasonDefault();
                if (description == null || description.isEmpty()) description = fallbackIssue;
                if (category == null || category.isEmpty()) category = prompts.categoryDefault();
            }

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

            log.info("AI 退货建议解析成功 - step={}, finished={}, reason={}",
                    currentStep, finished, reason);

            return new ReturnSuggestionResult(
                reason, description, category, confidence, guideQuestion, finished, analysisNote);

        } catch (Exception e) {
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
