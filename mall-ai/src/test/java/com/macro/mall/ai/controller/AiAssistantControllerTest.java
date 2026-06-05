package com.macro.mall.ai.controller;

import com.macro.mall.ai.domain.AiResponse;
import com.macro.mall.ai.domain.ReturnSuggestionResult;
import com.macro.mall.ai.service.AiAssistantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AI 助手控制器 MockMvc 测试 (Stage 1 - Record 化)
 *
 * <p>DTO 已迁移为 Java 17 record，测试改用 record 构造器。</p>
 */
@WebMvcTest(AiAssistantController.class)
class AiAssistantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiAssistantService aiAssistantService;

    @Test
    void productQa_shouldReturn200() throws Exception {
        AiResponse response = new AiResponse("该手机配备XX万像素摄像头，支持夜景模式。");
        when(aiAssistantService.chatAboutProduct(any())).thenReturn(response);

        String requestBody = "{\"productId\":1,\"question\":\"这款手机拍照效果如何？\",\"productName\":\"iPhone 15\"}";

        mockMvc.perform(post("/ai/product/qa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.reply").value("该手机配备XX万像素摄像头，支持夜景模式。"));
    }

    @Test
    void returnSuggest_shouldReturn200() throws Exception {
        ReturnSuggestionResult result = new ReturnSuggestionResult(
            "质量问题", "屏幕有明显裂痕", null, "high", null, true, null);
        when(aiAssistantService.suggestReturn(any())).thenReturn(result);

        String requestBody = "{\"issue\":\"屏幕有裂痕\",\"step\":3}";

        mockMvc.perform(post("/ai/return/suggest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.suggestedReason").value("质量问题"))
                .andExpect(jsonPath("$.data.finished").value(true));
    }

    @Test
    void returnSuggest_step1_returnsGuideQuestion() throws Exception {
        ReturnSuggestionResult result = new ReturnSuggestionResult(
            null, null, null, "high", "请问具体什么故障？", false, null);
        when(aiAssistantService.suggestReturn(any())).thenReturn(result);

        String requestBody = "{\"issue\":\"手机有问题\",\"step\":1}";

        mockMvc.perform(post("/ai/return/suggest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.finished").value(false))
                .andExpect(jsonPath("$.data.guideQuestion").value("请问具体什么故障？"));
    }
}
