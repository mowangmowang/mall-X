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
 * AI 助手控制器 MockMvc 测试
 * <p>
 * 注意: 全局异常处理 GlobalExceptionHandler (位于 mall-common) 不在 @WebMvcTest
 * 扫描范围, 因此 @Valid 校验失败测试需要 SpringBootTest 集成测试覆盖.
 * 本测试只覆盖 controller 的成功路径.
 * </p>
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
        ReturnSuggestionResult result = new ReturnSuggestionResult();
        result.setSuggestedReason("质量问题");
        result.setSuggestedDescription("屏幕有明显裂痕");
        result.setFinished(true);
        result.setConfidence("high");
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
        ReturnSuggestionResult result = new ReturnSuggestionResult();
        result.setGuideQuestion("请问具体什么故障？");
        result.setFinished(false);
        result.setConfidence("high");
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