package com.macro.mall.search;

import com.macro.mall.search.dao.EsProductDao;
import com.macro.mall.search.domain.EsProduct;
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
 * 测试 Elasticsearch 相关功能，包括：
 * <ul>
 *   <li>商品数据查询测试 (Product Data Query Test)</li>
 *   <li>Elasticsearch 索引映射测试 (Index Mapping Test)</li>
 * </ul>
 * </p>
 * <p>
 * 需要 MySQL 和 Elasticsearch 真实服务。
 * </p>
 *
 * @author alan
 * @since 1.0
 */
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