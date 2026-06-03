package com.macro.mall.search;

import com.macro.mall.search.dao.EsProductDao;
import com.macro.mall.search.domain.EsProduct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.test.context.junit4.SpringRunner;

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
 *
 * @author alan
 * @since 1.0
 */
@SpringBootTest
public class MallSearchApplicationTests {
    @Autowired
    private EsProductDao productDao;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;
    
    /**
     * 上下文加载测试 (Context Load Test)
     * 验证 Spring Boot 应用上下文是否正常加载
     */
    @Test
    public void contextLoads() {
    }
    
    /**
     * 测试商品数据查询 (Test Product Data Query)
     * 验证 MyBatis DAO 是否能正确从 MySQL 查询商品数据并映射为 EsProduct 对象
     */
    @Test
    public void testGetAllEsProductList(){
        List<EsProduct> esProductList = productDao.getAllEsProductList(null);
        System.out.print(esProductList);
    }
    
    /**
     * 测试 Elasticsearch 索引映射 (Test Elasticsearch Index Mapping)
     * 验证 EsProduct 实体是否能正确映射为 Elasticsearch 索引结构
     */
    @Test
    public void testEsProductMapping(){
        IndexOperations indexOperations = elasticsearchTemplate.indexOps(EsProduct.class);
        indexOperations.putMapping(indexOperations.createMapping(EsProduct.class));
        Map mapping = indexOperations.getMapping();
        System.out.println(mapping);
    }

}
