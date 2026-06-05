package com.macro.mall.ai.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI 响应 (AI Response)
 *
 * <p>封装 AI 模型生成的回复内容。Stage 1 已迁移为 Java 17 record。</p>
 *
 * @param reply AI 回复内容
 * @author alan
 * @since 1.0
 */
@Schema(description = "AI 响应")
public record AiResponse(
    @Schema(description = "AI 回复内容", example = "根据商品信息，这款手机采用钛金属设计...")
    String reply
) {
    public static AiResponse of(String reply) {
        return new AiResponse(reply);
    }
}
