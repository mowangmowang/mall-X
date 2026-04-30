package com.macro.mall.security.config;

import com.macro.mall.common.config.BaseRedisConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 缓存配置类 (Redis Configuration)
 * <p>
 * 继承自 {@link BaseRedisConfig}，启用 Spring Cache 注解支持（@Cacheable、@CacheEvict 等）。
 * 具体的 Redis 连接池、序列化器等配置在父类中定义。
 * </p>
 */
@EnableCaching
@Configuration
public class RedisConfig extends BaseRedisConfig {

}
