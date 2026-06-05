package com.macro.mall.ai.feign;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.ai.domain.ReturnReasonDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 退货原因 OpenFeign 客户端 (Stage 6)
 *
 * <p>替代 Stage 5 之前的 MyBatis {@code OmsOrderReturnReasonMapper}，
 * 远程调 mall-portal / mall-admin 暴露的 {@code /returnReason/list} 接口。</p>
 *
 * <p><b>注意：</b>mall-portal 当前未暴露退货原因列表端点。Fallback 返回空列表，
 * {@code ReturnReasonService} 兜底使用 {@code application.yml} 中配置的默认列表。
 * 后续可扩展：在 mall-portal 添加 {@code OmsPortalReturnReasonController}，
 * 转发到 mall-admin 的 {@code /returnReason/list}。</p>
 *
 * @author alan
 * @since 2026-06
 */
@FeignClient(name = "mall-portal", path = "/returnReason",
             fallbackFactory = ReturnReasonFallbackFactory.class)
public interface ReturnReasonClient {

    /**
     * 获取所有启用的退货原因列表
     */
    @GetMapping("/list")
    CommonResult<List<ReturnReasonDto>> list();
}
