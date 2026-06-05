package com.macro.mall.ai.service;

import com.macro.mall.ai.config.PromptProperties;
import com.macro.mall.ai.domain.ReturnReasonDto;
import com.macro.mall.ai.feign.ReturnReasonClient;
import com.macro.mall.common.api.CommonResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 退货原因服务 (Stage 6)
 *
 * <p>改用 OpenFeign 远程调 mall-portal / mall-admin 暴露的 {@code /returnReason/list}，
 * 替代 Stage 1-5 期间的 MyBatis 直连 DB。</p>
 *
 * <p><b>降级策略：</b>远程调用失败 / 返回 null / 返回空列表 → 使用 yml 配置的默认列表
 * （{@code prompts.returnReasonDefault} + 固定常见原因）。</p>
 *
 * @author alan
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class ReturnReasonService {

    private static final Logger log = LoggerFactory.getLogger(ReturnReasonService.class);

    private final ReturnReasonClient client;
    private final PromptProperties prompts;

    /**
     * 获取所有启用的退货原因名称列表（远程 + 降级）
     */
    public List<String> getEnabledReturnReasons() {
        try {
            CommonResult<List<ReturnReasonDto>> result = client.list();
            if (result == null || result.getData() == null || result.getData().isEmpty()) {
                log.info("远程退货原因列表为空，使用 yml 默认降级列表");
                return getDefaultReasons();
            }
            return result.getData().stream()
                .filter(Objects::nonNull)
                .filter(r -> r.status() != null && r.status() == 1)
                .filter(r -> r.name() != null && !r.name().isEmpty())
                .sorted(Comparator.comparingInt(
                    r -> r.sort() == null ? 0 : -r.sort()))  // sort 降序
                .map(ReturnReasonDto::name)
                .toList();
        } catch (Exception e) {
            log.warn("获取退货原因列表失败，使用 yml 默认降级列表. err={}", e.getMessage());
            return getDefaultReasons();
        }
    }

    /**
     * yml 配置的默认退货原因（远程调用失败时使用）
     */
    private List<String> getDefaultReasons() {
        return List.of(
            prompts.returnReasonDefault(),  // "质量问题"
            "商品损坏",
            "尺码不符",
            "7天无理由退货",
            "其他"
        );
    }
}
