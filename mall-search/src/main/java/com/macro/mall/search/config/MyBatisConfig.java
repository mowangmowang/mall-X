package com.macro.mall.search.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置类 (MyBatis Configuration)
 * <p>
 * 配置 MyBatis Mapper 接口扫描路径，启用数据访问功能。
 * 扫描范围包括：
 * <ul>
 *   <li>com.macro.mall.mapper：通用 Mapper 接口（来自 mall-mbg 模块）</li>
 *   <li>com.macro.mall.search.dao：搜索模块自定义 DAO 接口</li>
 * </ul>
 * </p>
 *
 * @author macro
 * @since 1.0
 */
@Configuration
@MapperScan({"com.macro.mall.mapper","com.macro.mall.search.dao"})  // 扫描 Mapper 接口路径
public class MyBatisConfig {
}
