package com.macro.mall.ai.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PromptProperties Bean Validation 测试 (Stage 2/3)
 *
 * <p>Stage 3 后：{@code ai.client.*} 改用 Spring AI 的 {@code spring.ai.openai.*}，
 * 本测试只验证 {@link PromptProperties} 自身。</p>
 *
 * @author alan
 * @since 2026-06
 */
class PromptPropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
        .withUserConfiguration(EnablePropsConfig.class);

    @Configuration
    @EnableConfigurationProperties(PromptProperties.class)
    static class EnablePropsConfig {
    }

    @Test
    void missingPrompts_shouldFailToStart() {
        runner
            // 故意缺 ai.prompts.*
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasMessageContaining("ai.prompts");
            });
    }

    @Test
    void allPresent_bindsCorrectly() {
        runner.withPropertyValues(
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
            });
    }
}
