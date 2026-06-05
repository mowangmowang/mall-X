package com.macro.mall.ai.feign;

import com.macro.mall.ai.domain.ReturnReasonDto;
import com.macro.mall.common.api.CommonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;

/**
 * ReturnReasonClient Fallback 工厂 (Stage 6)
 *
 * <p>当 mall-portal 不可用时（如开发环境未启动、CI 无服务发现），
 * 返回空列表让 {@code ReturnReasonService} 走 yml 配置的默认降级列表。</p>
 *
 * @author alan
 * @since 2026-06
 */
public class ReturnReasonFallbackFactory implements FallbackFactory<ReturnReasonClient> {

    private static final Logger log = LoggerFactory.getLogger(ReturnReasonFallbackFactory.class);

    @Override
    public ReturnReasonClient create(Throwable cause) {
        return new ReturnReasonClient() {
            @Override
            public CommonResult<List<ReturnReasonDto>> list() {
                log.warn("Feign call to mall-portal /returnReason/list failed, returning empty list. cause={}",
                    cause.getMessage());
                return CommonResult.success(List.of());
            }
        };
    }
}
