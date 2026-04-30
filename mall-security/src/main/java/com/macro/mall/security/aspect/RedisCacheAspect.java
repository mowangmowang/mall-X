package com.macro.mall.security.aspect;

import com.macro.mall.security.annotation.CacheException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Redis 缓存切面 (Redis Cache Aspect)
 * <p>
 * 用于拦截缓存服务方法，防止因 Redis 宕机或异常导致正常业务逻辑中断。
 * 核心策略：
 * 1. 若方法标注了 {@link CacheException} 注解，则异常向上抛出（调用方需处理）
 * 2. 若方法未标注该注解，则记录错误日志并返回 null（降级处理，不影响主流程）
 * </p>
 * <p>
 * 适用场景：缓存作为辅助功能时，即使缓存失败也不应影响核心业务逻辑
 * </p>
 */
@Aspect
@Component
@Order(2) // 设置切面优先级，数值越小优先级越高
public class RedisCacheAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheAspect.class);

    /**
     * 定义切入点：拦截 portal 和 service 模块下所有以 CacheService 结尾的类中的公共方法
     * 示例：PmsProductCacheService、OmsOrderCacheService 等
     */
    @Pointcut("execution(public * com.macro.mall.portal.service.*CacheService.*(..)) || execution(public * com.macro.mall.service.*CacheService.*(..))")
    public void cacheAspect() {
    }

    /**
     * 环绕通知：执行目标方法并处理异常
     * <p>
     * 执行流程：
     * 1. 尝试执行目标缓存方法
     * 2. 若成功，直接返回结果
     * 3. 若失败，检查是否标注了 @CacheException 注解
     *    - 已标注：抛出异常，让调用方感知错误
     *    - 未标注：记录日志，返回 null（降级处理）
     * </p>
     *
     * @param joinPoint 连接点，包含方法信息和参数
     * @return 方法执行结果，若发生非预期异常且未标注 @CacheException 则返回 null
     * @throws Throwable 当方法标注了 {@link CacheException} 时抛出原始异常
     */
    @Around("cacheAspect()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        Object result = null;
        
        try {
            // 执行目标缓存方法（如：从 Redis 读取数据）
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            // 检查方法是否标注了 @CacheException 注解
            if (method.isAnnotationPresent(CacheException.class)) {
                // 场景1：标注了注解，抛出异常以确保调用方感知错误
                throw throwable;
            } else {
                // 场景2：未标注注解，记录错误日志，避免 Redis 异常影响主业务流程
                LOGGER.error("Redis 缓存操作失败: {}", throwable.getMessage());
            }
        }
        
        return result;
    }

}
