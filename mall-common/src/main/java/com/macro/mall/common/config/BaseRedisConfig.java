package com.macro.mall.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.macro.mall.common.service.RedisService;
import com.macro.mall.common.service.impl.RedisServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 基础配置类 (Redis Base Configuration)
 * 提供 RedisTemplate、序列化器、缓存管理器及 RedisService 的 Bean 定义
 * 各模块可通过继承此配置类快速集成 Redis 功能
 */
public class BaseRedisConfig {

    /**
     * 配置 RedisTemplate
     * 设置 Key 和 HashKey 使用 String 序列化，Value 和 HashValue 使用 JSON 序列化
     *
     * @param redisConnectionFactory Redis 连接工厂
     * @param redisSerializer        JSON 序列化器
     * @return 配置好的 RedisTemplate 实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory, RedisSerializer<Object> redisSerializer) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // Key 采用 String 序列化（便于阅读和管理）
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // Value 采用 JSON 序列化（支持复杂对象存储）
        redisTemplate.setValueSerializer(redisSerializer);
        // HashKey 采用 String 序列化
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        // HashValue 采用 JSON 序列化
        redisTemplate.setHashValueSerializer(redisSerializer);
        // 初始化属性
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 配置 Redis 序列化器
     * 使用 Jackson2JsonRedisSerializer 进行对象与 JSON 的互转
     *
     * @return JSON 序列化器实例
     */
    @Bean
    public RedisSerializer<Object> redisSerializer() {
        // 创建 JSON 序列化器
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        // 设置所有字段的可见性（包括 private 字段）
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 启用默认类型信息，确保反序列化为具体对象而非 Map
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(objectMapper);
        return serializer;
    }

    /**
     * 配置 Redis 缓存管理器
     * 设置默认缓存过期时间为 1 天，并使用 JSON 序列化值
     *
     * @param redisConnectionFactory Redis 连接工厂
     * @return RedisCacheManager 实例
     */
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 创建非锁定缓存写入器（提高并发性能）
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory);
        // 配置缓存策略：值序列化方式及过期时间
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer()))
                .entryTtl(Duration.ofDays(1));
        return new RedisCacheManager(redisCacheWriter, redisCacheConfiguration);
    }

    /**
     * 注册 RedisService 服务实现
     *
     * @return RedisService 实例
     */
    @Bean
    public RedisService redisService() {
        return new RedisServiceImpl();
    }

}
