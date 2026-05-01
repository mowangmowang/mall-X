package com.macro.mall.ai.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis 配置类 (MyBatis Configuration)
 * 扫描 Mapper 接口，启用 MyBatis 数据访问功能
 */
@Configuration
@EnableTransactionManagement  // 启用 Spring 声明式事务管理
@MapperScan({"com.macro.mall.mapper"})  // 扫描 mall-mbg 模块生成的 Mapper 接口
public class MyBatisConfig {
    // 该配置类无需额外代码，通过注解完成所有配置
}
