package com.macro.mall.ai.chat;

import com.macro.mall.ai.config.PromptProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring AI ChatClient Bean 注册 (Stage 3)
 *
 * <p>Spring Boot 3.5 + spring-ai-starter-model-openai 自动配置会生成
 * {@code ChatClient.Builder} Bean，但不会自动创建 {@code ChatClient} 单例。
 * 本配置显式 build()，供 {@link AiChatService} 注入。</p>
 *
 * <p>同时注册 {@link PromptProperties}（替代 Stage 2 的 AiClientConfig）。</p>
 *
 * @author alan
 * @since 2026-06
 */
@Configuration
@EnableConfigurationProperties(PromptProperties.class)
public class ChatClientConfig {

    @Bean
    @Primary
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
