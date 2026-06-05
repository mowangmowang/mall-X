package com.macro.mall.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Mall-AI 微服务启动类 (Mall AI Microservice Application)
 *
 * <p>这是 mall-ai 模块的入口类，负责启动 Spring Boot 应用。</p>
 *
 * <p><b>Stage 6 升级：</b>{@code @EnableFeignClients} 启用 OpenFeign 客户端扫描，
 * 用于远程调 mall-portal / mall-admin 的 REST API（替代 MyBatis 直连 DB）。</p>
 *
 * @author alan
 * @since 1.0
 */
@SpringBootApplication(scanBasePackages = "com.macro.mall")
@EnableFeignClients(basePackages = "com.macro.mall.ai.feign")
public class MallAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallAiApplication.class, args);
    }
}

