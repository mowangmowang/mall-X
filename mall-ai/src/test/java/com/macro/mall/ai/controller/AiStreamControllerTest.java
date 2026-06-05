package com.macro.mall.ai.controller;

import com.macro.mall.ai.config.PromptProperties;
import com.macro.mall.ai.service.AiAssistantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AiStreamController 单元测试 (Stage 7 - SSE)
 *
 * <p>验证流式输出端点的 SSE 协议行为。Stage 7 后期重构：返回 servlet 原生
 * {@link SseEmitter} 替代 {@code Flux}，避免 webflux 容器替换 servlet 栈。</p>
 *
 * @author alan
 * @since 2026-06
 */
@WebMvcTest(AiStreamController.class)
class AiStreamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiAssistantService aiAssistantService;

    @MockBean
    private PromptProperties prompts;

    @Test
    void productQaStream_returnsOk() throws Exception {
        // MockMvc 不会真正订阅 emitter，只需返回非空 SseEmitter 即可
        when(aiAssistantService.streamChatAboutProduct(any()))
            .thenReturn(new SseEmitter(60_000L));

        String requestBody = "{\"productId\":1,\"question\":\"hi\",\"productName\":\"iPhone\"}";

        mockMvc.perform(post("/ai/product/qa/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void productQaStream_validationError_returns400() throws Exception {
        // 缺 productId 触发 @Valid
        String requestBody = "{\"question\":\"\"}";

        mockMvc.perform(post("/ai/product/qa/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
