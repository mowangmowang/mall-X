package com.macro.mall.search;

import com.macro.mall.search.component.EsProductReceiver;
import com.macro.mall.search.service.EsProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.*;

/**
 * 商品同步与校对任务单元测试 (Product Synchronization Unit Tests)
 * <p>
 * 测试 EsProductReceiver 的商品同步功能，包括：
 * <ul>
 *   <li>定时全量校对任务 (Scheduled Full Sync Task)</li>
 *   <li>消息接收处理 (Message Receiving & Processing)</li>
 * </ul>
 * </p>
 * <p>
 * 使用 Mockito 模拟 EsProductService，验证方法调用是否正确。
 * </p>
 *
 * @author alan
 * @since 1.0
 */
@SpringBootTest(classes = com.macro.mall.search.MallSearchApplication.class, properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.swagger.SwaggerAutoConfiguration"
})
public class EsProductSyncTests {

    @Autowired
    private EsProductReceiver esProductReceiver;

    @MockBean
    private EsProductService esProductService;

    @Test
    public void testSyncAllProducts() {
        // 模拟 importAll 返回值为 10
        when(esProductService.importAll()).thenReturn(10);

        // 执行校对任务
        esProductReceiver.syncAllProducts();

        // 验证 importAll 是否被调用了一次
        verify(esProductService, times(1)).importAll();
    }
}
