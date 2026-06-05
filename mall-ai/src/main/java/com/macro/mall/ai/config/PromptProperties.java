package com.macro.mall.ai.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Prompt 配置 (Prompt Properties) - Stage 2
 *
 * <p>从 {@code application.yml} 的 {@code ai.prompts.*} 绑定。
 * Stage 3 会引入 {@code spring.ai.openai.*} 后部分字段废弃。</p>
 *
 * @param productQaSystem          商品问答系统提示词（必填）
 * @param returnSuggestionSystem  退货建议系统提示词，支持 {@code {reasons}} 占位符
 * @param productQaFallback       商品问答兜底话术
 * @param returnReasonDefault     退货原因默认值
 * @param categoryDefault         问题分类默认值
 *
 * @author alan
 * @since 2026-06
 */
@ConfigurationProperties(prefix = "ai.prompts")
@Validated
public record PromptProperties(
    @NotBlank(message = "ai.prompts.product-qa-system 不能为空")
    String productQaSystem,

    @NotBlank(message = "ai.prompts.return-suggestion-system 不能为空")
    String returnSuggestionSystem,

    @NotBlank
    String productQaFallback,

    @NotBlank
    String returnReasonDefault,

    @NotBlank
    String categoryDefault
) {
}
