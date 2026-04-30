package com.macro.mall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * mall-admin 后台管理系统启动入口
 * 基于 Spring Boot 框架，负责商品管理、订单处理、用户权限等后台核心功能
 */
@SpringBootApplication
public class MallAdminApplication {
    /**
     * 应用启动主方法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MallAdminApplication.class, args);
    }
}
