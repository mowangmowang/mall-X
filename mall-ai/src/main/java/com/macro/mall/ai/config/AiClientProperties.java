package com.macro.mall.ai.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * AI 客户端配置 (AI Client Properties) - Stage 2
 *
 * <p>从 {@code application.yml} 的 {@code ai.client.*} 绑定。
 * 启动时自动校验，缺字段或越界即启动失败。</p>
 *
 * @param baseUrl    AI API 基础地址
 * @param apiKey     API Key
 * @param model      模型名称
 * @param temperature 温度参数 (0-2)
 * @param maxTokens  最大 token 数
 *
 * @author alan
 * @since 1.0
 */
@ConfigurationProperties(prefix = "ai.client")
@Validated
public record AiClientProperties(
    @NotBlank(message = "AI base URL 不能为空")
    String baseUrl,

    @NotBlank(message = "AI API Key 未配置，请设置环境变量 AI_API_KEY")
    String apiKey,

    @NotBlank
    String model,

    @DecimalMin("0.0") @DecimalMax("2.0")
    Double temperature,

    @Min(1) @Max(8192)
    Integer maxTokens
) {
}
