package com.macro.mall.ai.service;

import com.macro.mall.ai.config.PromptProperties;
import com.macro.mall.ai.feign.ReturnReasonClient;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.OmsOrderReturnReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * ReturnReasonService + Feign Client 单元测试 (Stage 6)
 *
 * <p>验证移除 MyBatis 后，ReturnReasonService 通过 OpenFeign 调 mall-portal。</p>
 *
 * @author alan
 * @since 2026-06
 */
class ReturnReasonServiceHttpTest {

    private ReturnReasonClient client;
    private PromptProperties prompts;
    private ReturnReasonService service;

    @BeforeEach
    void setUp() {
        client = mock(ReturnReasonClient.class);
        prompts = new PromptProperties(
            "QA", "RET", "fb", "质量问题", "硬件故障");
        service = new ReturnReasonService(client, prompts);
    }

    private OmsOrderReturnReason reason(Long id, String name, Integer status, Integer sort) {
        OmsOrderReturnReason r = new OmsOrderReturnReason();
        r.setId(id);
        r.setName(name);
        r.setStatus(status);
        r.setSort(sort);
        return r;
    }

    @Test
    void getEnabledReturnReasons_filtersAndSorts() {
        when(client.list()).thenReturn(CommonResult.success(List.of(
            reason(1L, "商品损坏", 1, 90),
            reason(2L, "质量问题", 1, 100),
            reason(3L, "已禁用", 0, 80)
        )));

        List<String> result = service.getEnabledReturnReasons();

        // 过滤 status=1 + 按 sort 降序
        assertThat(result).containsExactly("质量问题", "商品损坏");
    }

    @Test
    void getEnabledReturnReasons_remoteFails_usesDefaults() {
        when(client.list()).thenThrow(new RuntimeException("Connection refused"));

        List<String> result = service.getEnabledReturnReasons();

        // 降级到 yml 配置的默认列表
        assertThat(result).contains("质量问题", "7天无理由退货", "其他");
    }

    @Test
    void getEnabledReturnReasons_nullData_usesDefaults() {
        when(client.list()).thenReturn(CommonResult.success(null));

        List<String> result = service.getEnabledReturnReasons();

        assertThat(result).contains("质量问题", "7天无理由退货");
    }

    @Test
    void getEnabledReturnReasons_emptyList_usesDefaults() {
        when(client.list()).thenReturn(CommonResult.success(List.of()));

        List<String> result = service.getEnabledReturnReasons();

        assertThat(result).contains("质量问题");
    }

    @Test
    void getEnabledReturnReasons_nullName_skipped() {
        when(client.list()).thenReturn(CommonResult.success(List.of(
            reason(1L, "有效原因", 1, 100),
            reason(2L, null, 1, 90)
        )));

        List<String> result = service.getEnabledReturnReasons();

        assertThat(result).containsExactly("有效原因");
    }
}
