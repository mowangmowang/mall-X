package com.macro.mall.ai.chat;

import com.macro.mall.ai.config.PromptProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AiChatService 单元测试 (Stage 3)
 *
 * <p>验证 Spring AI ChatClient 封装：模板渲染 + ChatClient 调用链。</p>
 *
 * @author alan
 * @since 2026-06
 */
class AiChatServiceTest {

    private ChatClient chatClient;
    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec callSpec;
    private ChatClient.Builder builder;
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

        builder = new ChatClient.Builder() {
            @Override
            public ChatClient build() {
                return chatClient;
            }
        };

        prompts = new PromptProperties(
            "QA system",
            "RET system {reasons}",
            "fallback",
            "质量问题",
            "硬件故障"
        );

        service = new AiChatService(builder, prompts);
    }

    @Test
    void chat_withDirectPrompt_returnsContent() {
        when(callSpec.content()).thenReturn("AI 回复");

        String result = service.chat("You are helpful", "Hi");

        assertThat(result).isEqualTo("AI 回复");
    }

    @Test
    void chat_withTemplate_rendersAndCallsClient() {
        when(callSpec.content()).thenReturn("OK");

        String result = service.chat("Hello {name}", Map.of("name", "World"), "user msg");

        assertThat(result).isEqualTo("OK");
        // 验证 system prompt 被渲染后传入
        org.mockito.Mockito.verify(requestSpec).system("Hello World");
        org.mockito.Mockito.verify(requestSpec).user("user msg");
    }

    @Test
    void chat_rendersPromptPropertyWithReasons() {
        when(callSpec.content()).thenReturn("result");

        String result = service.renderAndChat(
            prompts.returnSuggestionSystem(),
            Map.of("reasons", "质量问题、商品损坏"),
            "step 1 user input"
        );

        assertThat(result).isEqualTo("result");
        // 验证 yml 模板的 {reasons} 占位符被替换
        org.mockito.Mockito.verify(requestSpec).system(org.mockito.ArgumentMatchers.contains("质量问题、商品损坏"));
    }
}
