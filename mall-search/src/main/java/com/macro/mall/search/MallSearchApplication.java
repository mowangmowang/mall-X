package com.macro.mall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 搜索服务启动类
 * 负责启动基于 Elasticsearch 的商品搜索微服务
 */
@SpringBootApplication(scanBasePackages = "com.macro.mall")
public class MallSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallSearchApplication.class, args);
    }
}
