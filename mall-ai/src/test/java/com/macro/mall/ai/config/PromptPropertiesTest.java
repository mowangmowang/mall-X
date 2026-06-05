package com.macro.mall.ai.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PromptProperties Bean Validation 测试 (Stage 2)
 *
 * <p>验证 @ConfigurationProperties record + @Validated 启动时校验。</p>
 *
 * @author alan
 * @since 2026-06
 */
class PromptPropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
        .withUserConfiguration(EnablePropsConfig.class);

    @Configuration
    @EnableConfigurationProperties({AiClientProperties.class, PromptProperties.class})
    static class EnablePropsConfig {
    }

    @Test
    void missingPrompts_shouldFailToStart() {
        runner.withPropertyValues(
                "ai.client.base-url=https://api.test.com",
                "ai.client.api-key=sk-test",
                "ai.client.model=test-model",
                "ai.client.temperature=0.7",
                "ai.client.max-tokens=1024"
                // 故意缺 ai.prompts.*
            )
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasMessageContaining("ai.prompts");
            });
    }

    @Test
    void missingApiKey_shouldFailToStart() {
        runner.withPropertyValues(
                "ai.client.base-url=https://api.test.com",
                "ai.client.model=test-model",
                "ai.prompts.product-qa-system=QA",
                "ai.prompts.return-suggestion-system=RET {reasons}",
                "ai.prompts.product-qa-fallback=fallback",
                "ai.prompts.return-reason-default=质量问题",
                "ai.prompts.category-default=硬件故障"
                // 故意缺 api-key
            )
            .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void allPresent_bindsCorrectly() {
        runner.withPropertyValues(
                "ai.client.base-url=https://api.test.com",
                "ai.client.api-key=sk-test",
                "ai.client.model=test-model",
                "ai.client.temperature=0.7",
                "ai.client.max-tokens=1024",
                "ai.prompts.product-qa-system=QA prompt",
                "ai.prompts.return-suggestion-system=RETURN prompt {reasons}",
                "ai.prompts.product-qa-fallback=fallback",
                "ai.prompts.return-reason-default=质量问题",
                "ai.prompts.category-default=硬件故障"
            )
            .run(context -> {
                assertThat(context).hasNotFailed();
                PromptProperties p = context.getBean(PromptProperties.class);
                assertThat(p.productQaSystem()).isEqualTo("QA prompt");
                assertThat(p.returnSuggestionSystem()).contains("{reasons}");
                assertThat(p.returnReasonDefault()).isEqualTo("质量问题");
                assertThat(p.categoryDefault()).isEqualTo("硬件故障");

                AiClientProperties c = context.getBean(AiClientProperties.class);
                assertThat(c.apiKey()).isEqualTo("sk-test");
                assertThat(c.model()).isEqualTo("test-model");
                assertThat(c.temperature()).isEqualTo(0.7);
                assertThat(c.maxTokens()).isEqualTo(1024);
            });
    }

    @Test
    void temperatureOutOfRange_shouldFail() {
        runner.withPropertyValues(
                "ai.client.base-url=https://api.test.com",
                "ai.client.api-key=sk-test",
                "ai.client.model=m",
                "ai.client.temperature=3.0",
                "ai.client.max-tokens=1024",
                "ai.prompts.product-qa-system=QA",
                "ai.prompts.return-suggestion-system=RET",
                "ai.prompts.product-qa-fallback=fb",
                "ai.prompts.return-reason-default=质量问题",
                "ai.prompts.category-default=硬件故障"
            )
            .run(context -> assertThat(context).hasFailed());
    }
}
