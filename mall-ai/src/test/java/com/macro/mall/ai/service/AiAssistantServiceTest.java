package com.macro.mall.ai.service;

import com.macro.mall.ai.client.AiClient;
import com.macro.mall.ai.config.PromptProperties;
import com.macro.mall.ai.domain.AiResponse;
import com.macro.mall.ai.domain.ProductQaRequest;
import com.macro.mall.ai.domain.ReturnSuggestionRequest;
import com.macro.mall.ai.domain.ReturnSuggestionResult;
import com.macro.mall.ai.service.impl.AiAssistantServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AiAssistantService 单元测试 (Stage 1 - Record 化)
 *
 * <p>使用 Mockito 模拟 AiClient 和 ReturnReasonService，
 * 验证 3 轮引导业务逻辑、强制校验、JSON 解析 fallback 等场景。
 * DTO 已迁移为 record，测试改用 record 构造器和 accessor。</p>
 */
class AiAssistantServiceTest {

    private AiClient aiClient;
    private ReturnReasonService returnReasonService;
    private PromptProperties prompts;
    private AiAssistantServiceImpl service;

    @BeforeEach
    void setUp() {
        aiClient = mock(AiClient.class);
        returnReasonService = mock(ReturnReasonService.class);
        // Stage 2: PromptProperties 通过 mock 提供测试用 prompt 与默认值
        prompts = new PromptProperties(
            "QA system prompt",
            "RETURN system prompt {reasons}",
            "fallback",
            "质量问题",
            "硬件故障"
        );
        // Stage 1: 构造器注入
        service = new AiAssistantServiceImpl(aiClient, returnReasonService, prompts);
        when(returnReasonService.getEnabledReturnReasons())
                .thenReturn(List.of("质量问题", "商品损坏", "7天无理由退货"));
    }

    // ============ 商品问答测试 ============

    @Test
    void chatAboutProduct_normalFlow() {
        ProductQaRequest request = new ProductQaRequest(1L, "材质是什么？", "iPhone 15", null, null, null, null);

        when(aiClient.chat(anyString(), anyString())).thenReturn("该手机采用钛金属设计。");

        AiResponse response = service.chatAboutProduct(request);

        assertNotNull(response);
        assertEquals("该手机采用钛金属设计。", response.reply());
        verify(aiClient, times(1)).chat(anyString(), anyString());
    }

    @Test
    void chatAboutProduct_withConversationHistory() {
        ProductQaRequest request = new ProductQaRequest(
            1L, "还有吗？", null, null, null, null,
            "用户: 拍照怎么样？\nAI: 4800万像素");

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
        ReturnSuggestionRequest request = new ReturnSuggestionRequest(
            "手机有问题", null, null, null, null, 1);

        when(aiClient.chat(anyString(), anyString())).thenReturn(
            "{\"reason\":\"\",\"description\":\"\",\"category\":\"\",\"confidence\":\"high\",\"guideQuestion\":\"请问具体什么故障？\",\"finished\":false}"
        );

        ReturnSuggestionResult result = service.suggestReturn(request);

        assertNotNull(result);
        assertFalse(result.finished());
        assertEquals("请问具体什么故障？", result.guideQuestion());
    }

    @Test
    void suggestReturn_step2_returnsGuideQuestion() {
        ReturnSuggestionRequest request = new ReturnSuggestionRequest(
            "屏幕不亮", null, null, null, null, 2);

        when(aiClient.chat(anyString(), anyString())).thenReturn(
            "{\"reason\":\"\",\"description\":\"\",\"category\":\"\",\"confidence\":\"high\",\"guideQuestion\":\"突然还是一直？\",\"finished\":false}"
        );

        ReturnSuggestionResult result = service.suggestReturn(request);

        assertFalse(result.finished());
        assertEquals("突然还是一直？", result.guideQuestion());
    }

    @Test
    void suggestReturn_step3_setsFinishedTrue() {
        ReturnSuggestionRequest request = new ReturnSuggestionRequest(
            "屏幕不亮", null, null, null, null, 3);

        when(aiClient.chat(anyString(), anyString())).thenReturn(
            "{\"reason\":\"质量问题\",\"description\":\"屏幕无法显示\",\"category\":\"硬件故障\",\"confidence\":\"high\",\"guideQuestion\":\"已确认\",\"finished\":true}"
        );

        ReturnSuggestionResult result = service.suggestReturn(request);

        assertTrue(result.finished());
        assertEquals("质量问题", result.suggestedReason());
        assertEquals("屏幕无法显示", result.suggestedDescription());
        assertEquals("硬件故障", result.category());
    }

    // ============ 强制校验与 fallback 测试 ============

    @Test
    void suggestReturn_step3_aiReturnsEmpty_fillsDefaults() {
        ReturnSuggestionRequest request = new ReturnSuggestionRequest(
            "手机有问题", null, null, null, null, 3);

        when(aiClient.chat(anyString(), anyString())).thenReturn(
            "{\"reason\":\"\",\"description\":\"\",\"category\":\"\",\"confidence\":\"high\",\"guideQuestion\":\"完成\",\"finished\":true}"
        );

        ReturnSuggestionResult result = service.suggestReturn(request);

        assertTrue(result.finished());
        // step=3 时强制填充默认值
        assertEquals("质量问题", result.suggestedReason());
        assertEquals("手机有问题", result.suggestedDescription());
        assertEquals("硬件故障", result.category());
    }

    @Test
    void suggestReturn_invalidJson_fallbackToDefault() {
        ReturnSuggestionRequest request = new ReturnSuggestionRequest(
            "屏幕坏", null, null, null, null, 3);

        when(aiClient.chat(anyString(), anyString())).thenReturn("not a json");

        ReturnSuggestionResult result = service.suggestReturn(request);

        assertTrue(result.finished());
        // JSON 解析失败, step=3 走兜底
        assertEquals("质量问题", result.suggestedReason());
        assertEquals("屏幕坏", result.suggestedDescription());
        assertEquals("硬件故障", result.category());
        assertEquals("low", result.confidence());
    }

    @Test
    void suggestReturn_nullSessionId_generatesUuid() {
        ReturnSuggestionRequest request = new ReturnSuggestionRequest(
            "测试", null, null, null, null, 1);

        when(aiClient.chat(anyString(), anyString())).thenReturn(
            "{\"reason\":\"\",\"description\":\"\",\"category\":\"\",\"confidence\":\"high\",\"guideQuestion\":\"x\",\"finished\":false}"
        );

        ReturnSuggestionResult result = service.suggestReturn(request);

        // 会话ID在结果中保留(AI 实际未返回 sessionId, 此处主要验证不抛异常)
        assertNotNull(result);
    }

    @Test
    void suggestReturn_markdownJsonCodeBlock_parsesCorrectly() {
        ReturnSuggestionRequest request = new ReturnSuggestionRequest(
            "测试", null, null, null, null, 3);

        // 模拟 AI 返回 Markdown 代码块格式
        when(aiClient.chat(anyString(), anyString())).thenReturn(
            "```json\n{\"reason\":\"商品损坏\",\"description\":\"屏幕破裂\",\"category\":\"硬件故障\",\"confidence\":\"high\",\"guideQuestion\":\"ok\",\"finished\":true}\n```"
        );

        ReturnSuggestionResult result = service.suggestReturn(request);

        assertTrue(result.finished());
        assertEquals("商品损坏", result.suggestedReason());
    }
}
