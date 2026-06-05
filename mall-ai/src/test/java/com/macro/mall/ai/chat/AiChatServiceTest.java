package com.macro.mall.ai.chat;

import com.macro.mall.ai.config.PromptProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AiChatService 单元测试 (Stage 3)
 *
 * <p>直接 mock {@link ChatClient}，绕过 {@link ChatClient.Builder} 的抽象复杂性。</p>
 *
 * @author alan
 * @since 2026-06
 */
class AiChatServiceTest {

    private ChatClient chatClient;
    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec callSpec;
    private PromptProperties prompts;
    private AiChatService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        chatClient = mock(ChatClient.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        callSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);

        prompts = new PromptProperties(
            "QA system",
            "RET system {reasons}",
            "fallback",
            "质量问题",
            "硬件故障"
        );

        service = new AiChatService(chatClient, prompts);
    }

    @Test
    void chat_withDirectPrompt_returnsContent() {
        when(callSpec.content()).thenReturn("AI 回复");

        String result = service.chat("You are helpful", "Hi");

        assertThat(result).isEqualTo("AI 回复");
        verify(requestSpec).system("You are helpful");
        verify(requestSpec).user("Hi");
    }

    @Test
    void chat_withTemplate_rendersAndCallsClient() {
        when(callSpec.content()).thenReturn("OK");

        String result = service.chat("Hello {name}", Map.of("name", "World"), "user msg");

        assertThat(result).isEqualTo("OK");
        verify(requestSpec).system("Hello World");
        verify(requestSpec).user("user msg");
    }

    @Test
    void renderAndChat_replacesReasonsPlaceholder() {
        when(callSpec.content()).thenReturn("result");

        String result = service.renderAndChat(
            prompts.returnSuggestionSystem(),
            Map.of("reasons", "质量问题、商品损坏"),
            "step 1 user input"
        );

        assertThat(result).isEqualTo("result");
        verify(requestSpec).system(contains("质量问题、商品损坏"));
    }
}
