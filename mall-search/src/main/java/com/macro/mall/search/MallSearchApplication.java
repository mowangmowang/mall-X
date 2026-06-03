package com.macro.mall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 搜索服务应用启动类 (Search Application Starter)
 * <p>
 * 负责启动基于 Elasticsearch 的商品搜索微服务，提供以下核心功能：
 * <ul>
 *   <li>商品全文搜索 (Full-text Search)</li>
 *   <li>多维度筛选与排序 (Multi-dimensional Filtering & Sorting)</li>
 *   <li>商品推荐 (Product Recommendation)</li>
 *   <li>搜索聚合分析 (Search Aggregation Analysis)</li>
 * </ul>
 * </p>
 *
 * @author alan
 * @since 1.0
 */
@SpringBootApplication(scanBasePackages = "com.macro.mall")
@EnableScheduling
public class MallSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallSearchApplication.class, args);
    }
}
