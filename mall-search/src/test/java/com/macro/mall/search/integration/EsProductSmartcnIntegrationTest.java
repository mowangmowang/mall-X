package com.macro.mall.search.integration;

import com.macro.mall.search.domain.EsProduct;
import com.macro.mall.search.repository.EsProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * smartcn 分词器端到端集成测试 (smartcn Analyzer End-to-End Integration Test)
 * <p>
 * 验证：中文商品文档写入 pms 索引后，方法名派生查询 findByNameOrSubTitleOrKeywords
 * 能通过 smartcn 分词器命中"华为"等中文关键词。
 * </p>
 * <p>
 * 前置条件：
 * <ul>
 *   <li>本机 Elasticsearch 8.x 启动并监听 9200（HTTP）</li>
 *   <li>xpack.security.enabled = false（见 AGENTS.md gotchas）</li>
 *   <li>analysis-smartcn 插件已安装</li>
 * </ul>
 * 测试会在 setup 时重建 pms 索引以保证清洁状态。
 * </p>
 *
 * @author alan
 * @since 1.0
 */
@SpringBootTest
@ActiveProfiles("dev")
class EsProductSmartcnIntegrationTest {

    @Autowired
    private ElasticsearchOperations esOps;

    @Autowired
    private EsProductRepository repo;

    @BeforeEach
    void cleanIndex() {
        IndexOperations indexOps = esOps.indexOps(EsProduct.class);
        if (indexOps.exists()) {
            indexOps.delete();
        }
        indexOps.create();
        indexOps.putMapping(indexOps.createMapping(EsProduct.class));
        indexOps.refresh();
    }

    @Test
    void smartcn_chineseKeyword_matches() {
        EsProduct p = new EsProduct();
        p.setId(1L);
        p.setName("华为 Mate 60 Pro 手机");
        p.setSubTitle("麒麟芯片旗舰");
        p.setKeywords("华为,手机,国产");
        p.setBrandId(1L);
        p.setBrandName("华为");
        p.setProductCategoryId(10L);
        p.setProductCategoryName("手机");
        p.setPrice(new BigDecimal("6999"));
        p.setPic("");
        p.setProductSn("SN-HW-1");
        p.setSale(0);
        p.setStock(100);
        p.setAttrValueList(Collections.emptyList());
        EsProduct saved = repo.save(p);
        assertNotNull(saved, "商品应成功写入 ES");
        esOps.indexOps(EsProduct.class).refresh();

        Page<EsProduct> page = repo.findByNameOrSubTitleOrKeywords(
                "华为", "华为", "华为", PageRequest.of(0, 5));
        assertTrue(page.getTotalElements() >= 1,
                "smartcn 应能通过 findByNameOrSubTitleOrKeywords 命中 '华为'，实际命中=" + page.getTotalElements());
    }

    @Test
    void smartcn_englishKeyword_doesNotInterfere() {
        EsProduct p = new EsProduct();
        p.setId(2L);
        p.setName("Apple iPhone 15 Pro Max");
        p.setSubTitle("Titanium Design");
        p.setKeywords("apple,iphone,smartphone");
        p.setBrandId(2L);
        p.setBrandName("Apple");
        p.setProductCategoryId(20L);
        p.setProductCategoryName("手机");
        p.setPrice(new BigDecimal("9999"));
        p.setPic("");
        p.setProductSn("SN-AP-2");
        p.setSale(0);
        p.setStock(50);
        p.setAttrValueList(Collections.emptyList());
        repo.save(p);
        esOps.indexOps(EsProduct.class).refresh();

        Page<EsProduct> page = repo.findByNameOrSubTitleOrKeywords(
                "Apple", "Apple", "Apple", PageRequest.of(0, 5));
        assertTrue(page.getTotalElements() >= 1,
                "smartcn 对英文 'Apple' 也应能命中，实际命中=" + page.getTotalElements());
    }
}
