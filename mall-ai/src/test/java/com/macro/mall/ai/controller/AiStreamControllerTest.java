package com.macro.mall.ai.controller;

import com.macro.mall.ai.config.PromptProperties;
import com.macro.mall.ai.domain.AiResponse;
import com.macro.mall.ai.service.AiAssistantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AiStreamController 单元测试 (Stage 7 - SSE)
 *
 * <p>验证流式输出端点的 SSE 协议行为。</p>
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
        // Stage 7 简化：流式端点直接复用 chatAboutProduct 业务逻辑
        // 业务层负责把流式响应包装成 Flux
        when(aiAssistantService.streamChatAboutProduct(any()))
            .thenReturn(Flux.just("你", "好", "，", "AI"));

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
