package com.macro.mall.ai.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 商品问答请求 (Product Q&A Request)
 *
 * <p>Stage 1 已迁移为 Java 17 record。JSR-303 注解放在 record 组件上，
 * springdoc-openapi 2.6 / Jackson 2.15+ 原生支持。</p>
 *
 * @author alan
 * @since 1.0
 */
@Schema(description = "商品问答请求参数")
public record ProductQaRequest(

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", example = "1")
    Long productId,

    @NotBlank(message = "问题不能为空")
    @Size(max = 500, message = "问题长度不能超过500字符")
    @Schema(description = "用户问题", example = "这个商品的材质是什么？")
    String question,

    @Schema(description = "商品名称", example = "iPhone 15 Pro")
    String productName,

    @Schema(description = "商品品牌", example = "Apple")
    String productBrand,

    @Schema(description = "商品价格", example = "7999")
    String productPrice,

    @Schema(description = "商品副标题/描述", example = "钛金属设计，A17 Pro芯片")
    String productSubTitle,

    @Size(max = 2000, message = "对话历史长度不能超过2000字符")
    @Schema(description = "对话历史上下文，用于多轮对话")
    String conversationHistory
) {
}
