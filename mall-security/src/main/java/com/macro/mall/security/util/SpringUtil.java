package com.macro.mall.security.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring 上下文工具类 (Spring Context Utility)
 * <p>
 * 用于在非 Spring 管理的类中获取 Spring 容器中的 Bean 实例。
 * 通过实现 {@link ApplicationContextAware} 接口，在 Spring 容器启动时自动注入 {@link ApplicationContext}。
 * </p>
 *
 * @author alan
 */
@Component
public class SpringUtil implements ApplicationContextAware {

    /**
     * Spring 应用上下文 (Application Context)
     */
    private static ApplicationContext applicationContext;

    /**
     * 获取 Spring 应用上下文 (Application Context)
     *
     * @return 当前应用的 {@link ApplicationContext} 实例
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 设置 Spring 应用上下文 (Application Context)
     * <p>
     * 由 Spring 容器在初始化时自动调用，将当前的 {@link ApplicationContext} 注入到静态变量中。
     * 使用判空逻辑确保只赋值一次，避免重复覆盖。
     * </p>
     *
     * @param applicationContext Spring 应用上下文实例
     * @throws BeansException 如果设置上下文时发生错误
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringUtil.applicationContext == null) {
            SpringUtil.applicationContext = applicationContext;
        }
    }

    /**
     * 根据名称 (Name) 获取 Bean 实例
     *
     * @param name Bean 的名称 (Bean Name)
     * @return 对应的 Bean 实例，类型为 {@link Object}
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    /**
     * 根据类型 (Class) 获取 Bean 实例
     *
     * @param clazz Bean 的类类型 (Class Type)
     * @param <T>   泛型类型参数
     * @return 对应的 Bean 实例，类型为 {@code T}
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 根据名称 (Name) 和类型 (Class) 获取指定的 Bean 实例
     * <p>
     * 相比单独使用名称或类型，这种方式更加安全，可以避免类型转换异常。
     * </p>
     *
     * @param name  Bean 的名称 (Bean Name)
     * @param clazz Bean 的类类型 (Class Type)
     * @param <T>   泛型类型参数
     * @return 对应的 Bean 实例，类型为 {@code T}
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

}
