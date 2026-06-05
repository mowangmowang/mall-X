package com.macro.mall.ai.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 退货建议请求 (Return Suggestion Request)
 *
 * <p>Stage 1 已迁移为 Java 17 record。</p>
 *
 * @author alan
 * @since 1.0
 */
@Schema(description = "退货建议请求参数")
public record ReturnSuggestionRequest(

    @NotBlank(message = "问题描述不能为空")
    @Size(max = 1000, message = "问题描述长度不能超过1000字符")
    @Schema(description = "用户问题描述", example = "商品收到后发现屏幕有裂痕")
    String issue,

    @Schema(description = "商品名称", example = "iPhone 15 Pro")
    String productName,

    @Schema(description = "商品属性", example = "颜色:黑色,容量:256GB")
    String productAttr,

    @Schema(description = "订单编号", example = "202401010001")
    String orderSn,

    @Schema(description = "会话ID，用于多轮对话状态管理", example = "session_123456")
    String sessionId,

    @Schema(description = "当前引导步骤 (1-3)", example = "1")
    Integer step
) {
}
