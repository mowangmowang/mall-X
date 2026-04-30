package com.macro.mall.search.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置类 (MyBatis Configuration)
 * 扫描 Mapper 接口，启用 MyBatis 数据访问功能
 */
@Configuration
@MapperScan({"com.macro.mall.mapper","com.macro.mall.search.dao"})  // 扫描 Mapper 接口路径
public class MyBatisConfig {
}
