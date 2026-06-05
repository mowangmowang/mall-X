package com.macro.mall.ai.service.impl;

import com.macro.mall.ai.chat.AiChatService;
import com.macro.mall.ai.config.PromptProperties;
import com.macro.mall.ai.domain.*;
import com.macro.mall.ai.service.AiAssistantService;
import com.macro.mall.ai.service.ReturnReasonService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * AI 助手服务实现类 (AI Assistant Service Implementation) - Stage 5
 *
 * <p><b>Stage 1：</b>DTO Record 化 + 构造器注入</p>
 * <p><b>Stage 2：</b>Prompt 外置到 application.yml + PromptProperties 注入</p>
 * <p><b>Stage 3：</b>用 Spring AI ChatClient 替代手写 OpenAI 客户端</p>
 * <p><b>Stage 4：</b>用 {@code BeanOutputConverter} 替换 90 行手写 JSON 解析</p>
 * <p><b>Stage 5：</b>删除显式 {@code InputSanitizer.sanitize()} 调用 — 改由
 * Spring AI {@code InputSanitizationAdvisor} 自动拦截 ChatClient 调用</p>
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
        String content = buildProductQaContent(request);

        log.info("AI product Q&A - productId={}, question={}, hasHistory={}",
                request.productId(), request.question(),
                request.conversationHistory() != null && !request.conversationHistory().isEmpty());

        String reply = aiChat.chat(prompts.productQaSystem(), content);
        return new AiResponse(reply);
    }

    @Override
    public SseEmitter streamChatAboutProduct(ProductQaRequest request) {
        String content = buildProductQaContent(request);

        log.info("AI product Q&A (stream) - productId={}, question={}",
                request.productId(), request.question());

        return aiChat.streamChat(prompts.productQaSystem(), content);
    }

    /**
     * Stage 7: 共用上下文构建逻辑（同步 + 流式）
     */
    private String buildProductQaContent(ProductQaRequest request) {
        String context = buildProductContext(request);
        StringBuilder sb = new StringBuilder(context);
        if (request.conversationHistory() != null && !request.conversationHistory().isEmpty()) {
            sb.append("\n\n【对话历史】\n").append(request.conversationHistory());
        }
        sb.append("\n\n【顾客问题】").append(request.question());
        return sb.toString();
    }

    @Override
    public ReturnSuggestionResult suggestReturn(ReturnSuggestionRequest request) {
        int currentStep = request.step() == null ? 1 : request.step();
        String sessionId = request.sessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = java.util.UUID.randomUUID().toString();
        }

        String content = String.format(
                "当前引导步骤：%d/3\n用户描述的问题：%s\n商品名称：%s\n商品属性：%s\n订单编号：%s",
                currentStep,
                request.issue(),
                nullToEmpty(request.productName()),
                nullToEmpty(request.productAttr()),
                nullToEmpty(request.orderSn())
        );

        log.info("AI return suggest - step={}, issue={}", currentStep, request.issue());

        List<String> reasons = returnReasonService.getEnabledReturnReasons();
        String reasonsStr = String.join("、", reasons);

        try {
            // Stage 4: BeanOutputConverter 自动注入 JSON schema + 反序列化为 record
            // Stage 5: InputSanitizationAdvisor 自动清洗 user text
            ReturnSuggestionResult aiResult = aiChat.renderAndChatEntity(
                prompts.returnSuggestionSystem(),
                Map.of("reasons", reasonsStr),
                content,
                ReturnSuggestionResult.class
            );

            if (currentStep >= 3) {
                return enforceStep3Defaults(aiResult, request.issue());
            }
            return aiResult;
        } catch (Exception e) {
            log.warn("Failed to call AI for return suggestion, using fallback. err={}", e.getMessage());
            return fallbackResult(currentStep, request.issue());
        }
    }

    private ReturnSuggestionResult enforceStep3Defaults(ReturnSuggestionResult r, String issue) {
        return new ReturnSuggestionResult(
            r.suggestedReason() == null || r.suggestedReason().isEmpty()
                ? prompts.returnReasonDefault() : r.suggestedReason(),
            r.suggestedDescription() == null || r.suggestedDescription().isEmpty()
                ? issue : r.suggestedDescription(),
            r.category() == null || r.category().isEmpty()
                ? prompts.categoryDefault() : r.category(),
            r.confidence() == null ? "medium" : r.confidence(),
            r.guideQuestion() == null ? "" : r.guideQuestion(),
            true,
            String.format("根据描述'%s'判断并匹配", issue)
        );
    }

    private ReturnSuggestionResult fallbackResult(int currentStep, String issue) {
        if (currentStep >= 3) {
            return new ReturnSuggestionResult(
                prompts.returnReasonDefault(), issue, prompts.categoryDefault(), "low",
                "已为您生成建议，请确认。", true,
                "解析失败，但已为您生成默认建议");
        }
        return new ReturnSuggestionResult(
            "", "", "", "low",
            "抱歉，我没听清。请问具体是哪里出现了问题？", false,
            "解析失败，请重试");
    }

    private String buildProductContext(ProductQaRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("【商品信息】\n");
        sb.append("名称：").append(nullToEmpty(request.productName())).append("\n");
        sb.append("品牌：").append(nullToEmpty(request.productBrand())).append("\n");
        sb.append("价格：").append(nullToEmpty(request.productPrice())).append("元\n");
        sb.append("描述：").append(nullToEmpty(request.productSubTitle()));
        return sb.toString();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
