package com.macro.mall.ai.security;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 输入清理配置 (Input Sanitization Properties) - Stage 5
 *
 * <p>从 {@code application.yml} 的 {@code ai.security.sanitization.*} 绑定。</p>
 *
 * @param maxLength             输入最大长度（超长截断）
 * @param stripControlChars     是否剥离控制字符（除换行/制表）
 * @param detectPromptInjection 是否记录 Prompt Injection 警告日志
 *
 * @author alan
 * @since 2026-06
 */
@ConfigurationProperties(prefix = "ai.security.sanitization")
@Validated
public record SanitizationProperties(
    @NotNull @Min(1) @Max(100_000) Integer maxLength,
    @NotNull Boolean stripControlChars,
    @NotNull Boolean detectPromptInjection
) {
    public SanitizationProperties {
        // 默认值（避免 yml 漏配时 NPE）
        if (maxLength == null) maxLength = 5000;
        if (stripControlChars == null) stripControlChars = true;
        if (detectPromptInjection == null) detectPromptInjection = true;
    }
}
