package com.macro.mall.security.annotation;

import java.lang.annotation.*;

/**
 * 自定义缓存异常注解 (Cache Exception Annotation)
 * <p>
 * 用于标注那些在 Redis 缓存操作失败时需要抛出异常的方法。
 * </p>
 * <p>
 * 使用场景：
 * - 若方法标注了此注解，当 Redis 异常时，{@link com.macro.mall.security.aspect.RedisCacheAspect} 会将异常向上抛出
 * - 若方法未标注此注解，Redis 异常时仅记录日志并返回 null（降级处理）
 * </p>
 * <p>
 * 示例用法：
 * <pre>
 * {@code @CacheException}
 * public User getUserFromCache(Long userId) {
 *     // 从 Redis 获取用户信息
 *     // 若 Redis 宕机，将抛出异常
 * }
 * </pre>
 * </p>
 */
@Documented
@Target(ElementType.METHOD) // 仅可标注在方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时可见，可通过反射获取
public @interface CacheException {
}

