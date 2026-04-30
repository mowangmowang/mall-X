package com.macro.mall.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis 持久层框架配置类
 * 启用事务管理并扫描 Mapper 接口和自定义 DAO
 */
@Configuration
@EnableTransactionManagement  // 启用 Spring 声明式事务管理
@MapperScan({"com.macro.mall.mapper", "com.macro.mall.dao"})  // 扫描 Mapper 接口包路径
public class MyBatisConfig {
    // 该配置类无需额外代码，通过注解完成所有配置
}
