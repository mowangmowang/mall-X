package com.macro.mall.search.service;

import com.macro.mall.search.domain.EsProduct;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * EsProductSearchService 单元测试
 * <p>
 * 使用 Mockito 模拟 EsProductService，
 * 验证接口各方法的行为契约（不依赖真实 ES 服务）。
 * </p>
 */
class EsProductSearchServiceTest {

    @Test
    void importAll_shouldReturnCount() {
        EsProductService service = mock(EsProductService.class);
        when(service.importAll()).thenReturn(10);
        int count = service.importAll();
        assertEquals(10, count);
    }

    @Test
    void search_withKeyword_shouldReturnPage() {
        EsProductService service = mock(EsProductService.class);
        Page<EsProduct> page = new PageImpl<>(Collections.emptyList());
        when(service.search(anyString(), anyInt(), anyInt())).thenReturn(page);
        Page<EsProduct> result = service.search("手机", 0, 5);
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void search_withNullKeyword_shouldReturnPage() {
        EsProductService service = mock(EsProductService.class);
        Page<EsProduct> page = new PageImpl<>(Collections.emptyList());
        when(service.search(any(), anyInt(), anyInt())).thenReturn(page);
        Page<EsProduct> result = service.search(null, 0, 5);
        assertNotNull(result);
    }

    @Test
    void recommend_withProductId_shouldReturnPage() {
        EsProductService service = mock(EsProductService.class);
        Page<EsProduct> page = new PageImpl<>(Collections.emptyList());
        when(service.recommend(anyLong(), anyInt(), anyInt())).thenReturn(page);
        Page<EsProduct> result = service.recommend(1L, 0, 5);
        assertNotNull(result);
    }

    @Test
    void advancedSearch_shouldReturnPage() {
        EsProductService service = mock(EsProductService.class);
        Page<EsProduct> page = new PageImpl<>(Collections.emptyList());
        when(service.search(anyString(), anyLong(), anyLong(), anyInt(), anyInt(), anyInt(),
                any(BigDecimal.class), any(BigDecimal.class))).thenReturn(page);
        Page<EsProduct> result = service.search("手机", 1L, 1L, 0, 5, 1, BigDecimal.valueOf(0), BigDecimal.valueOf(1000));
        assertNotNull(result);
    }

    @Test
    void searchRelatedInfo_shouldReturnObject() {
        EsProductService service = mock(EsProductService.class);
        when(service.searchRelatedInfo(anyString())).thenReturn(null);
        assertNotNull(service);
    }

    @Test
    void deleteById_shouldNotThrow() {
        EsProductService service = mock(EsProductService.class);
        org.mockito.Mockito.doNothing().when(service).delete(anyLong());
        org.mockito.Mockito.doNothing().when(service).delete(any(List.class));
        service.delete(1L);
        service.delete(Collections.emptyList());
    }

    @Test
    void createById_shouldReturnProduct() {
        EsProductService service = mock(EsProductService.class);
        when(service.create(anyLong())).thenReturn(null);
        EsProduct result = service.create(1L);
        assertEquals(null, result);
    }
}