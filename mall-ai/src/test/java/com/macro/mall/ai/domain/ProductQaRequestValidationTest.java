package com.macro.mall.ai.domain;

import com.macro.mall.ai.controller.AiAssistantController;
import com.macro.mall.ai.service.AiAssistantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ProductQaRequest Bean Validation 测试 (Stage 1 - Record 化)
 *
 * <p>验证 DTO 迁移到 Java 17 record 后 JSR-303 校验仍然生效。</p>
 *
 * @author alan
 * @since 2026-06
 */
@WebMvcTest(AiAssistantController.class)
class ProductQaRequestValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiAssistantService aiAssistantService;

    @Test
    void blankQuestion_returns400() throws Exception {
        mockMvc.perform(post("/ai/product/qa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":1,\"question\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nullProductId_returns400() throws Exception {
        mockMvc.perform(post("/ai/product/qa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":null,\"question\":\"hi\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void oversizedQuestion_501_returns400() throws Exception {
        String big = "a".repeat(501);
        mockMvc.perform(post("/ai/product/qa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":1,\"question\":\"" + big + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void boundaryQuestion_500_returns200() throws Exception {
        String boundary = "a".repeat(500);
        mockMvc.perform(post("/ai/product/qa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":1,\"question\":\"" + boundary + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void oversizedConversationHistory_2001_returns400() throws Exception {
        String big = "x".repeat(2001);
        mockMvc.perform(post("/ai/product/qa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":1,\"question\":\"hi\",\"conversationHistory\":\"" + big + "\"}"))
                .andExpect(status().isBadRequest());
    }
}
