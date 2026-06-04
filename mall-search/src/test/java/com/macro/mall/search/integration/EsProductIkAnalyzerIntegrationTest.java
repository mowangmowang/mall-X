package com.macro.mall.search.integration;

import com.macro.mall.search.domain.EsProduct;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * IK 8.19.16 分词器端到端集成测试 (IK 8.19.16 Analyzer End-to-End Integration Test)
 * <p>
 * 验证：中文商品文档写入后，{@code match} 查询能通过 IK 8.19.16 分词器
 * 命中"小米"、"华为"等中文品牌关键词。
 * </p>
 * <p>
 * 前置条件：
 * <ul>
 *   <li>本机 Elasticsearch 8.x 启动并监听 9200（HTTP）</li>
 *   <li>xpack.security.enabled = false（见 AGENTS.md gotchas）</li>
 *   <li>analysis-ik 8.19.16 插件已安装</li>
 * </ul>
 * 测试在独立索引 {@value #TEST_INDEX} 上运行，<b>不污染生产 pms 索引</b>。
 * </p>
 *
 * @author alan
 * @since 1.0
 */
@SpringBootTest
@ActiveProfiles("dev")
class EsProductIkAnalyzerIntegrationTest {

    /**
     * 测试专用索引名，与生产 pms 完全隔离
     */
    private static final String TEST_INDEX = "pms_test";

    @Autowired
    private ElasticsearchOperations esOps;

    private IndexCoordinates testIndexCoords() {
        return IndexCoordinates.of(TEST_INDEX);
    }

    @BeforeEach
    void cleanTestIndex() {
        IndexOperations indexOps = esOps.indexOps(testIndexCoords());
        if (indexOps.exists()) {
            indexOps.delete();
        }
        indexOps.create();
        indexOps.putMapping(indexOps.createMapping(EsProduct.class));
        indexOps.refresh();
    }

    @AfterAll
    static void cleanupTestIndex() {
        // 清理工作在 @BeforeEach 中已经按测试粒度进行；这里保留钩子以备未来需要
    }

    /**
     * 通过 IndexQuery 显式写入测试索引，绕过 @Document 注解的硬编码索引名
     */
    private void indexProduct(String id, EsProduct p) {
        IndexQuery indexQuery = new IndexQueryBuilder()
                .withId(id)
                .withObject(p)
                .build();
        esOps.index(indexQuery, testIndexCoords());
        esOps.indexOps(testIndexCoords()).refresh();
    }

    private EsProduct newXiaomiProduct() {
        EsProduct p = new EsProduct();
        p.setId(1L);
        p.setName("小米 8 全面屏手机");
        p.setSubTitle("骁龙845 处理器");
        p.setKeywords("小米,手机,国产");
        p.setBrandId(6L);
        p.setBrandName("小米");
        p.setProductCategoryId(19L);
        p.setProductCategoryName("手机");
        p.setPrice(new BigDecimal("2699"));
        p.setPic("https://via.placeholder.com/300x300.png?text=Xiaomi-8");
        p.setProductSn("SN-XM-1");
        p.setSale(99);
        p.setStock(100);
        p.setAttrValueList(Collections.emptyList());
        return p;
    }

    private EsProduct newHuaweiProduct() {
        EsProduct p = new EsProduct();
        p.setId(2L);
        p.setName("华为 Mate 60 Pro 手机");
        p.setSubTitle("麒麟芯片旗舰");
        p.setKeywords("华为,手机,国产");
        p.setBrandId(1L);
        p.setBrandName("华为");
        p.setProductCategoryId(10L);
        p.setProductCategoryName("手机");
        p.setPrice(new BigDecimal("6999"));
        p.setPic("https://via.placeholder.com/300x300.png?text=Huawei-Mate60");
        p.setProductSn("SN-HW-2");
        p.setSale(0);
        p.setStock(100);
        p.setAttrValueList(Collections.emptyList());
        return p;
    }

    @Test
    void ik_chineseKeyword_matchesXiaomi() {
        indexProduct("1", newXiaomiProduct());

        SearchHits<EsProduct> hits = esOps.search(
                NativeQuery.builder()
                        .withQuery(q -> q.match(m -> m.field("name").query("小米")))
                        .build(),
                EsProduct.class,
                testIndexCoords());

        assertTrue(hits.getTotalHits() >= 1,
                "IK 应能命中'小米'，实际命中=" + hits.getTotalHits()
                        + "，前 1 条：" + (hits.getSearchHits().isEmpty() ? "(无)" : hits.getSearchHits().get(0).getContent().getName()));
    }

    @Test
    void ik_chineseKeyword_matchesHuawei() {
        indexProduct("2", newHuaweiProduct());

        SearchHits<EsProduct> hits = esOps.search(
                NativeQuery.builder()
                        .withQuery(q -> q.match(m -> m.field("name").query("华为")))
                        .build(),
                EsProduct.class,
                testIndexCoords());

        assertTrue(hits.getTotalHits() >= 1,
                "IK 应能命中'华为'，实际命中=" + hits.getTotalHits());
    }

    @Test
    void ik_rawMappingUsesIkMaxWord() {
        IndexOperations indexOps = esOps.indexOps(testIndexCoords());
        Map<String, Object> mapping = indexOps.getMapping();
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) mapping.get("properties");
        assertNotNull(properties, "pms_test properties 应存在");
        for (String field : new String[]{"name", "subTitle", "keywords"}) {
            @SuppressWarnings("unchecked")
            Map<String, Object> fieldMapping = (Map<String, Object>) properties.get(field);
            assertNotNull(fieldMapping, field + " mapping 应存在");
            assertTrue("ik_max_word".equals(fieldMapping.get("analyzer")),
                    field + " 字段的索引分词器应为 ik_max_word，实际=" + fieldMapping.get("analyzer"));
        }
    }
}
