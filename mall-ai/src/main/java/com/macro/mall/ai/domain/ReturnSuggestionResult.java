package com.macro.mall.ai.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 退货建议结果 (Return Suggestion Result)
 *
 * <p>Stage 1 已迁移为 Java 17 record。</p>
 *
 * @author alan
 * @since 1.0
 */
@Schema(description = "退货建议结果")
public record ReturnSuggestionResult(

    @Schema(description = "推荐的退货原因", example = "质量问题")
    String suggestedReason,

    @Schema(description = "推荐的问题描述", example = "商品收到后发现屏幕有明显裂痕，影响正常使用")
    String suggestedDescription,

    @Schema(description = "问题分类", example = "硬件故障",
        allowableValues = {"硬件故障", "软件问题", "商品不符", "物流损坏", "主观原因"})
    String category,

    @Schema(description = "置信度", example = "high", allowableValues = {"high", "medium", "low"})
    String confidence,

    @Schema(description = "引导问题，用于下一步引导用户", example = "请问商品是否有物理损坏？")
    String guideQuestion,

    @Schema(description = "是否完成引导", example = "false")
    Boolean finished,

    @Schema(description = "分析说明", example = "根据描述'无法开机'，判断为硬件故障")
    String analysisNote
) {
}
