package com.macro.mall.ai.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 退货原因 DTO (Stage 6)
 *
 * <p>替代 mall-mbg 的 {@code OmsOrderReturnReason} 实体类（Stage 6 移除了 mall-mbg 依赖）。
 * 只保留 AI 业务需要的字段，避免依赖整个 mapper 体系。</p>
 *
 * <p>字段语义与 mall-admin / mall-portal 的同名数据库表保持一致。</p>
 *
 * @param id     主键
 * @param name   退货原因名称
 * @param status 状态：0->不启用；1->启用
 * @param sort   排序（降序）
 *
 * @author alan
 * @since 2026-06
 */
@Schema(description = "退货原因")
public record ReturnReasonDto(
    @Schema(description = "主键") Long id,
    @Schema(description = "退货原因名称") String name,
    @Schema(description = "状态 0=禁用 1=启用") Integer status,
    @Schema(description = "排序") Integer sort
) {
}
