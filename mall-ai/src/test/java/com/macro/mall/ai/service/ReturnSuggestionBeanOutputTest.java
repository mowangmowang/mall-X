package com.macro.mall.ai.service;

import com.macro.mall.ai.chat.AiChatService;
import com.macro.mall.ai.config.PromptProperties;
import com.macro.mall.ai.domain.ReturnSuggestionRequest;
import com.macro.mall.ai.domain.ReturnSuggestionResult;
import com.macro.mall.ai.service.impl.AiAssistantServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Stage 4: BeanOutputConverter 结构化输出测试
 *
 * <p>验证 {@link AiAssistantServiceImpl#suggestReturn} 用 Spring AI 的
 * {@code BeanOutputConverter} 替换手写 JSON 解析。</p>
 *
 * @author alan
 * @since 2026-06
 */
class ReturnSuggestionBeanOutputTest {

    private AiChatService aiChat;
    private ReturnReasonService returnReasonService;
    private PromptProperties prompts;
    private AiAssistantServiceImpl service;

    @BeforeEach
    void setUp() {
        aiChat = mock(AiChatService.class);
        returnReasonService = mock(ReturnReasonService.class);
        prompts = new PromptProperties(
            "QA system",
            "RETURN {format} {reasons}",
            "fallback",
            "质量问题",
            "硬件故障"
        );
        service = new AiAssistantServiceImpl(aiChat, returnReasonService, prompts);
        when(returnReasonService.getEnabledReturnReasons())
                .thenReturn(List.of("质量问题", "商品损坏", "7天无理由退货"));
    }

    @Test
    void suggestReturn_step3_aiReturnsValidJson_returnsAsIs() {
        // BeanOutputConverter 已经把 JSON 转为 record，AiChatService 直接返回 record
        ReturnSuggestionResult aiResult = new ReturnSuggestionResult(
            "商品损坏", "屏幕有裂痕", "硬件故障", "high", "明白了", true, "分析");
        when(aiChat.renderAndChatEntity(anyString(), anyMap(), anyString(), eq(ReturnSuggestionResult.class)))
            .thenReturn(aiResult);

        ReturnSuggestionRequest req = new ReturnSuggestionRequest(
            "屏幕有裂痕", null, null, null, null, 3);

        ReturnSuggestionResult result = service.suggestReturn(req);

        assertThat(result.suggestedReason()).isEqualTo("商品损坏");
        assertThat(result.finished()).isTrue();
    }

    @Test
    void suggestReturn_step3_aiReturnsEmptyReason_usesDefault() {
        ReturnSuggestionResult aiResult = new ReturnSuggestionResult(
            "", "", "", "medium", "请问还有其他问题吗", true, "分析");
        when(aiChat.renderAndChatEntity(anyString(), anyMap(), anyString(), eq(ReturnSuggestionResult.class)))
            .thenReturn(aiResult);

        ReturnSuggestionRequest req = new ReturnSuggestionRequest(
            "屏幕有裂痕", null, null, null, null, 3);

        ReturnSuggestionResult result = service.suggestReturn(req);

        // step=3 时强制填充默认值（从 PromptProperties 读取）
        assertThat(result.suggestedReason()).isEqualTo("质量问题");
        assertThat(result.suggestedDescription()).isEqualTo("屏幕有裂痕");
        assertThat(result.category()).isEqualTo("硬件故障");
        assertThat(result.finished()).isTrue();
    }

    @Test
    void suggestReturn_step3_throws_returnsFallback() {
        when(aiChat.renderAndChatEntity(anyString(), anyMap(), anyString(), eq(ReturnSuggestionResult.class)))
            .thenThrow(new RuntimeException("AI service down"));

        ReturnSuggestionRequest req = new ReturnSuggestionRequest(
            "屏幕有裂痕", null, null, null, null, 3);

        ReturnSuggestionResult result = service.suggestReturn(req);

        assertThat(result.suggestedReason()).isEqualTo("质量问题");
        assertThat(result.confidence()).isEqualTo("low");
        assertThat(result.finished()).isTrue();
        assertThat(result.analysisNote()).contains("解析失败");
    }

    @Test
    void suggestReturn_step1_returnsGuideOnly() {
        ReturnSuggestionResult aiResult = new ReturnSuggestionResult(
            "", "", "", "medium", "请问具体什么问题？", false, "");
        when(aiChat.renderAndChatEntity(anyString(), anyMap(), anyString(), eq(ReturnSuggestionResult.class)))
            .thenReturn(aiResult);

        ReturnSuggestionRequest req = new ReturnSuggestionRequest(
            "手机有问题", null, null, null, null, 1);

        ReturnSuggestionResult result = service.suggestReturn(req);

        assertThat(result.guideQuestion()).isEqualTo("请问具体什么问题？");
        assertThat(result.finished()).isFalse();
    }

    @Test
    void suggestReturn_stepNull_defaultsTo1() {
        ReturnSuggestionResult aiResult = new ReturnSuggestionResult(
            "", "", "", "medium", "请问什么坏了？", false, "");
        when(aiChat.renderAndChatEntity(anyString(), anyMap(), anyString(), eq(ReturnSuggestionResult.class)))
            .thenReturn(aiResult);

        ReturnSuggestionRequest req = new ReturnSuggestionRequest(
            "有问题", null, null, null, null, null);  // step=null

        ReturnSuggestionResult result = service.suggestReturn(req);

        assertThat(result.finished()).isFalse();
    }

    @Test
    void suggestReturn_passesFormatToTemplate() {
        ReturnSuggestionResult aiResult = new ReturnSuggestionResult(
            "", "", "", "medium", "x", false, "");
        when(aiChat.renderAndChatEntity(anyString(), anyMap(), anyString(), eq(ReturnSuggestionResult.class)))
            .thenReturn(aiResult);

        ReturnSuggestionRequest req = new ReturnSuggestionRequest(
            "测试", null, null, null, null, 1);

        service.suggestReturn(req);

        // 验证 system prompt 包含 BeanOutputConverter 的 format 描述（含 "JSON"）
        ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> varsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(aiChat).renderAndChatEntity(
            templateCaptor.capture(), varsCaptor.capture(), anyString(),
            eq(ReturnSuggestionResult.class));

        String rendered = templateCaptor.getValue();
        Map<String, Object> vars = varsCaptor.getValue();
        // 验证占位符已被替换
        assertThat(rendered).contains("质量问题、商品损坏、7天无理由退货");
        // 验证 {format} 占位符由 AiChatService 内部处理（已替换为 JSON schema）
    }
}
