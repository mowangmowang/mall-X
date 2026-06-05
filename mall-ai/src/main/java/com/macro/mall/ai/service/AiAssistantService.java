package com.macro.mall.ai.service;

import com.macro.mall.ai.domain.AiResponse;
import com.macro.mall.ai.domain.ProductQaRequest;
import com.macro.mall.ai.domain.ReturnSuggestionRequest;
import com.macro.mall.ai.domain.ReturnSuggestionResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 助手服务接口 (AI Assistant Service Interface) - Stage 7
 *
 * <p>定义 AI 购物助手的核心业务方法。</p>
 *
 * <p><b>Stage 7 升级：</b>新增 {@code streamChatAboutProduct} 返回 servlet 原生
 * {@link SseEmitter}，业务层包装 ChatClient 流式输出为 SSE 端点。</p>
 *
 * @author alan
 * @since 1.0
 */
public interface AiAssistantService {

    /**
     * 商品问答 (同步)
     */
    AiResponse chatAboutProduct(ProductQaRequest request);

    /**
     * 商品问答 (Stage 7: 流式)
     *
     * <p>返回 servlet 原生 {@link SseEmitter}，前端用 SSE (text/event-stream) 消费。</p>
     */
    SseEmitter streamChatAboutProduct(ProductQaRequest request);

    /**
     * 退货建议 (3 轮引导)
     */
    ReturnSuggestionResult suggestReturn(ReturnSuggestionRequest request);
}

