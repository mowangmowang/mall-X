package com.macro.mall.search;

import com.macro.mall.search.component.EsProductReceiver;
import com.macro.mall.search.service.EsProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.*;

/**
 * 商品同步与校对任务单元测试
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
