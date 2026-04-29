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
 * 若方法标注了 {@link CacheException} 注解，则异常向上抛出；否则记录错误日志并返回 null。
 */
@Aspect
@Component
@Order(2)
public class RedisCacheAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheAspect.class);

    /**
     * 定义切入点：拦截 portal 和 service 模块下所有以 CacheService 结尾的类中的公共方法
     */
    @Pointcut("execution(public * com.macro.mall.portal.service.*CacheService.*(..)) || execution(public * com.macro.mall.service.*CacheService.*(..))")
    public void cacheAspect() {
    }

    /**
     * 环绕通知：执行目标方法并处理异常
     *
     * @param joinPoint 连接点 (Join Point)
     * @return 方法执行结果，若发生非预期异常且未标注 CacheException 则返回 null
     * @throws Throwable 当方法标注了 {@link CacheException} 时抛出原始异常
     */
    @Around("cacheAspect()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        Object result = null;
        try {
            // 执行目标方法
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            // 若方法标注了 CacheException 注解，则抛出异常以确保调用方感知错误
            if (method.isAnnotationPresent(CacheException.class)) {
                throw throwable;
            } else {
                // 否则记录错误日志，避免 Redis 异常影响主业务流程
                LOGGER.error("Redis 缓存操作失败: {}", throwable.getMessage());
            }
        }
        return result;
    }

}
