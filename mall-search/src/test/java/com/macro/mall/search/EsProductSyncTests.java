package com.macro.mall.search;

import com.macro.mall.search.component.EsProductReceiver;
import com.macro.mall.search.service.EsProductService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.*;

/**
 * 商品同步与校对任务单元测试 (Product Synchronization Unit Tests)
 * <p>
 * 测试 EsProductReceiver 的商品同步功能。
 * </p>
 * <p>
 * 集成测试：需要 MySQL、Redis、RabbitMQ、Elasticsearch 等真实服务才能运行。
 * 默认禁用，使用 {@code -Dtest=EsProductSyncTests -DskipTests=false -DenableITs=true} 启用。
 * </p>
 */
@Disabled("集成测试：需要 MySQL/Redis/RabbitMQ/Elasticsearch 等真实服务。在 CI 环境或本地全栈环境运行。")
@SpringBootTest(classes = com.macro.mall.search.MallSearchApplication.class)
public class EsProductSyncTests {

    @Autowired
    private EsProductReceiver esProductReceiver;

    @MockBean
    private EsProductService esProductService;

    @Test
    public void testSyncAllProducts() {
        when(esProductService.importAll()).thenReturn(10);
        esProductReceiver.syncAllProducts();
        verify(esProductService, times(1)).importAll();
    }
}