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
 * Redis基础配置类
 * 提供RedisTemplate、序列化器、缓存管理器及RedisService的Bean定义
 */
public class BaseRedisConfig {

    /**
     * 配置RedisTemplate
     * 设置Key和HashKey使用String序列化，Value和HashValue使用JSON序列化
     *
     * @param redisConnectionFactory Redis连接工厂
     * @param redisSerializer        JSON序列化器
     * @return 配置好的RedisTemplate实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory, RedisSerializer<Object> redisSerializer) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // Key采用String序列化
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // Value采用JSON序列化
        redisTemplate.setValueSerializer(redisSerializer);
        // HashKey采用String序列化
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        // HashValue采用JSON序列化
        redisTemplate.setHashValueSerializer(redisSerializer);
        // 初始化属性
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 配置Redis序列化器
     * 使用Jackson2JsonRedisSerializer进行对象与JSON的互转
     *
     * @return JSON序列化器实例
     */
    @Bean
    public RedisSerializer<Object> redisSerializer() {
        // 创建JSON序列化器
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        // 设置所有字段的可见性
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 启用默认类型信息，确保反序列化为具体对象而非Map
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(objectMapper);
        return serializer;
    }

    /**
     * 配置Redis缓存管理器
     * 设置默认缓存过期时间为1天，并使用JSON序列化值
     *
     * @param redisConnectionFactory Redis连接工厂
     * @return RedisCacheManager实例
     */
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 创建非锁定缓存写入器
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory);
        // 配置缓存策略：值序列化方式及过期时间
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer()))
                .entryTtl(Duration.ofDays(1));
        return new RedisCacheManager(redisCacheWriter, redisCacheConfiguration);
    }

    /**
     * 注册RedisService服务实现
     *
     * @return RedisService实例
     */
    @Bean
    public RedisService redisService() {
        return new RedisServiceImpl();
    }

}
