package com.macro.mall.ai.service;

import com.macro.mall.ai.client.AiClient;
import com.macro.mall.ai.domain.AiResponse;
import com.macro.mall.ai.domain.ProductQaRequest;
import com.macro.mall.ai.domain.ReturnSuggestionRequest;
import com.macro.mall.ai.domain.ReturnSuggestionResult;
import com.macro.mall.ai.service.impl.AiAssistantServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AiAssistantService 单元测试
 * <p>
 * 使用 Mockito 模拟 AiClient 和 ReturnReasonService，
 * 验证 3 轮引导业务逻辑、强制校验、JSON 解析 fallback 等场景。
 * </p>
 */
class AiAssistantServiceTest {

    private AiClient aiClient;
    private ReturnReasonService returnReasonService;
    private AiAssistantServiceImpl service;

    @BeforeEach
    void setUp() {
        aiClient = mock(AiClient.class);
        returnReasonService = mock(ReturnReasonService.class);
        service = new AiAssistantServiceImpl();
        // 注入 private 字段
        ReflectionTestUtils.setField(service, "aiClient", aiClient);
        ReflectionTestUtils.setField(service, "returnReasonService", returnReasonService);
        when(returnReasonService.getEnabledReturnReasons())
                .thenReturn(List.of("质量问题", "商品损坏", "7天无理由退货"));
    }

    // ============ 商品问答测试 ============

    @Test
    void chatAboutProduct_normalFlow() {
        ProductQaRequest request = new ProductQaRequest();
        request.setProductId(1L);
        request.setQuestion("材质是什么？");
        request.setProductName("iPhone 15");

        when(aiClient.chat(anyString(), anyString())).thenReturn("该手机采用钛金属设计。");

        AiResponse response = service.chatAboutProduct(request);

        assertNotNull(response);
        assertEquals("该手机采用钛金属设计。", response.getReply());
        verify(aiClient, times(1)).chat(anyString(), anyString());
    }

    @Test
    void chatAboutProduct_withConversationHistory() {
        ProductQaRequest request = new ProductQaRequest();
        request.setProductId(1L);
        request.setQuestion("还有吗？");
        request.setConversationHistory("用户: 拍照怎么样？\nAI: 4800万像素");

        when(aiClient.chat(anyString(), anyString())).thenReturn("有夜景模式。");

        service.chatAboutProduct(request);

        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(aiClient).chat(anyString(), contentCaptor.capture());
        assertTrue(contentCaptor.getValue().contains("【对话历史】"));
        assertTrue(contentCaptor.getValue().contains("【顾客问题】"));
    }

    // ============ 退货建议 3 轮引导测试 ============

    @Test
    void suggestReturn_step1_returnsGuideQuestion() {
        ReturnSuggestionRequest request = new ReturnSuggestionRequest();
        request.setIssue("手机有问题");
        request.setStep(1);

        when(aiClient.chat(anyString(), anyString())).thenReturn(
            "{\"reason\":\"\",\"description\":\"\",\"category\":\"\",\"confidence\":\"high\",\"guideQuestion\":\"请问具体什么故障？\",\"finished\":false}"
        );

        ReturnSuggestionResult result = service.suggestReturn(request);

        assertNotNull(result);
        assertFalse(result.getFinished());
        assertEquals("请问具体什么故障？", result.getGuideQuestion());
    }

    @Test
    void suggestReturn_step2_returnsGuideQuestion() {
        ReturnSuggestionRequest request = new ReturnSuggestionRequest();
        request.setIssue("屏幕不亮");
        request.setStep(2);

        when(aiClient.chat(anyString(), anyString())).thenReturn(
            "{\"reason\":\"\",\"description\":\"\",\"category\":\"\",\"confidence\":\"high\",\"guideQuestion\":\"突然还是一直？\",\"finished\":false}"
        );

        ReturnSuggestionResult result = service.suggestReturn(request);

        assertFalse(result.getFinished());
        assertEquals("突然还是一直？", result.getGuideQuestion());
    }

    @Test
    void suggestReturn_step3_setsFinishedTrue() {
        ReturnSuggestionRequest request = new ReturnSuggestionRequest();
        request.setIssue("屏幕不亮");
        request.setStep(3);

        when(aiClient.chat(anyString(), anyString())).thenReturn(
            "{\"reason\":\"质量问题\",\"description\":\"屏幕无法显示\",\"category\":\"硬件故障\",\"confidence\":\"high\",\"guideQuestion\":\"已确认\",\"finished\":true}"
        );

        ReturnSuggestionResult result = service.suggestReturn(request);

        assertTrue(result.getFinished());
        assertEquals("质量问题", result.getSuggestedReason());
        assertEquals("屏幕无法显示", result.getSuggestedDescription());
        assertEquals("硬件故障", result.getCategory());
    }

    // ============ 强制校验与 fallback 测试 ============

    @Test
    void suggestReturn_step3_aiReturnsEmpty_fillsDefaults() {
        ReturnSuggestionRequest request = new ReturnSuggestionRequest();
        request.setIssue("手机有问题");
        request.setStep(3);

        when(aiClient.chat(anyString(), anyString())).thenReturn(
            "{\"reason\":\"\",\"description\":\"\",\"category\":\"\",\"confidence\":\"high\",\"guideQuestion\":\"完成\",\"finished\":true}"
        );

        ReturnSuggestionResult result = service.suggestReturn(request);

        assertTrue(result.getFinished());
        // step=3 时强制填充默认值
        assertEquals("质量问题", result.getSuggestedReason());
        assertEquals("手机有问题", result.getSuggestedDescription());
        assertEquals("硬件故障", result.getCategory());
    }

    @Test
    void suggestReturn_invalidJson_fallbackToDefault() {
        ReturnSuggestionRequest request = new ReturnSuggestionRequest();
        request.setIssue("屏幕坏");
        request.setStep(3);

        when(aiClient.chat(anyString(), anyString())).thenReturn("not a json");

        ReturnSuggestionResult result = service.suggestReturn(request);

        assertTrue(result.getFinished());
        // JSON 解析失败, step=3 走兜底
        assertEquals("质量问题", result.getSuggestedReason());
        assertEquals("屏幕坏", result.getSuggestedDescription());
        assertEquals("硬件故障", result.getCategory());
        assertEquals("low", result.getConfidence());
    }

    @Test
    void suggestReturn_nullSessionId_generatesUuid() {
        ReturnSuggestionRequest request = new ReturnSuggestionRequest();
        request.setIssue("测试");
        request.setStep(1);
        request.setSessionId(null);

        when(aiClient.chat(anyString(), anyString())).thenReturn(
            "{\"reason\":\"\",\"description\":\"\",\"category\":\"\",\"confidence\":\"high\",\"guideQuestion\":\"x\",\"finished\":false}"
        );

        ReturnSuggestionResult result = service.suggestReturn(request);

        // 会话ID在结果中保留(AI 实际未返回 sessionId, 此处主要验证不抛异常)
        assertNotNull(result);
    }

    @Test
    void suggestReturn_markdownJsonCodeBlock_parsesCorrectly() {
        ReturnSuggestionRequest request = new ReturnSuggestionRequest();
        request.setIssue("测试");
        request.setStep(3);

        // 模拟 AI 返回 Markdown 代码块格式
        when(aiClient.chat(anyString(), anyString())).thenReturn(
            "```json\n{\"reason\":\"商品损坏\",\"description\":\"屏幕破裂\",\"category\":\"硬件故障\",\"confidence\":\"high\",\"guideQuestion\":\"ok\",\"finished\":true}\n```"
        );

        ReturnSuggestionResult result = service.suggestReturn(request);

        assertTrue(result.getFinished());
        assertEquals("商品损坏", result.getSuggestedReason());
    }
}