package com.macro.mall.ai.config;

import com.macro.mall.ai.client.AiClient;
import com.macro.mall.ai.client.OpenAiCompatibleClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * AI 配置注册 (AI Configuration Registration) - Stage 2
 *
 * <p>本类作为 {@code @ConfigurationProperties} 记录的注册入口，
 * 同时保留 {@link AiClient} 的 Bean 定义（Stage 3 由 Spring AI ChatClient 替代）。</p>
 *
 * <p><b>Stage 2 改造：</b></p>
 * <ul>
 *   <li>手写 getter/setter 全部删除，{@code AiClientProperties} 用 record + @Validated</li>
 *   <li>{@code @PostConstruct} 手动校验删除，由 Bean Validation 自动接管</li>
 *   <li>Prompt 从 Java 硬编码迁出到 {@code application.yml} 的 {@code ai.prompts.*}，
 *       注入到 {@link PromptProperties}</li>
 * </ul>
 *
 * @author alan
 * @since 1.0
 */
@Configuration
@EnableConfigurationProperties({AiClientProperties.class, PromptProperties.class})
public class AiClientConfig {

    /**
     * AI 客户端 Bean（Stage 2 仍由 {@link OpenAiCompatibleClient} 提供）
     *
     * <p>Stage 3 将删除此 Bean，改用 Spring AI {@code ChatClient} 替代。</p>
     */
    @Bean
    public AiClient aiClient(AiClientProperties props) {
        return new OpenAiCompatibleClient(
            props.baseUrl(), props.apiKey(), props.model(),
            props.temperature(), props.maxTokens(),
            restTemplate());
    }

    /**
     * RestTemplate Bean（Stage 2 仍由 OpenAiCompatibleClient 使用）
     *
     * <p>Stage 3 将删除此 Bean，改用 Spring AI 内置 HTTP 客户端。</p>
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(60000);
        return new RestTemplate(factory);
    }
}
