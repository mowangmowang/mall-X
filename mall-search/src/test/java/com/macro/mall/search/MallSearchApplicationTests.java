package com.macro.mall.search;

import com.macro.mall.search.dao.EsProductDao;
import com.macro.mall.search.domain.EsProduct;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

import java.util.List;
import java.util.Map;

/**
 * 搜索服务应用测试类 (Search Application Tests)
 * <p>
 * 集成测试：需要 MySQL 和 Elasticsearch 等真实服务才能运行。
 * 默认禁用，使用 {@code -Dtest=MallSearchApplicationTests -DskipTests=false} 启用。
 * </p>
 *
 * @author alan
 * @since 1.0
 */
@Disabled("集成测试：需要 MySQL/Elasticsearch 等真实服务。")
@SpringBootTest
public class MallSearchApplicationTests {
    @Autowired
    private EsProductDao productDao;
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Test
    public void contextLoads() {
    }

    @Test
    public void testGetAllEsProductList(){
        List<EsProduct> esProductList = productDao.getAllEsProductList(null);
        System.out.print(esProductList);
    }

    @Test
    public void testEsProductMapping(){
        IndexOperations indexOperations = elasticsearchOperations.indexOps(EsProduct.class);
        indexOperations.putMapping(indexOperations.createMapping(EsProduct.class));
        Map mapping = indexOperations.getMapping();
        System.out.println(mapping);
    }

}