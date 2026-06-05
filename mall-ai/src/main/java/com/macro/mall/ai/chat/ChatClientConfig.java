package com.macro.mall.ai.chat;

import com.macro.mall.ai.config.PromptProperties;
import com.macro.mall.ai.security.InputSanitizationAdvisor;
import com.macro.mall.ai.security.SanitizationProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring AI ChatClient Bean 注册 (Stage 3/5)
 *
 * <p>Spring Boot 3.5 + spring-ai-starter-model-openai 自动配置会生成
 * {@code ChatClient.Builder} Bean，但不会自动创建 {@code ChatClient} 单例。
 * 本配置显式 build()，供 {@link AiChatService} 注入。</p>
 *
 * <p><b>Stage 5 升级：</b>注册 {@link SanitizationProperties} + {@link InputSanitizationAdvisor}，
 * 并在 ChatClient 中通过 {@code defaultAdvisors} 注入，使所有 ChatClient 调用自动清洗 user input。</p>
 *
 * @author alan
 * @since 2026-06
 */
@Configuration
@EnableConfigurationProperties({PromptProperties.class, SanitizationProperties.class})
public class ChatClientConfig {

    @Bean
    @Primary
    public ChatClient chatClient(ChatClient.Builder builder, InputSanitizationAdvisor sanitizer) {
        return builder
            .defaultAdvisors(sanitizer)
            .build();
    }

    /**
     * Stage 5: 注册 {@link InputSanitizationAdvisor} Bean
     */
    @Bean
    public InputSanitizationAdvisor inputSanitizationAdvisor(SanitizationProperties props) {
        return new InputSanitizationAdvisor(props);
    }
}
