package com.macro.mall.config;

import com.macro.mall.common.config.BaseSwaggerConfig;
import com.macro.mall.common.domain.SwaggerProperties;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger相关配置 (基于Swagger2框架)
 * 这是一份带有详细注释的示例，帮助初学者理解如何在具体模块中启用和配置Swagger。
 * Created by macro on 2018/4/26.
 */
@Configuration // @Configuration 注解表示这是一个配置类，Spring会在启动时扫描它并加载Bean
@EnableSwagger2 // @EnableSwagger2 注解表示开启Swagger2的API文档功能
public class SwaggerConfig extends BaseSwaggerConfig {

    /**
     * 重写父类定义的抽象方法，用来告诉父类如何构建当前模块专门的Swagger属性信息
     */
    @Override
    public SwaggerProperties swaggerProperties() {
        // 使用建造者模式(Builder)构造 SwaggerProperties 属性对象
        return SwaggerProperties.builder()
                // 指定要扫描的API接口所在的包路径（该包下的Controller类和方法将会暴漏在文档中）
                .apiBasePackage("com.macro.mall.controller")
                // 显示在Swagger UI顶部的页面标题
                .title("mall后台系统")
                // 文档的大致描述
                .description("mall后台相关接口文档")
                // 联系人名字
                .contactName("macro")
                // 该API文档的版本号
                .version("1.0")
                // 启用安全认证（比如在请求时要求带着Token认证头）
                .enableSecurity(true)
                .build();
    }

    /**
     * 注入BeanPostProcessor到Spring容器中
     * 作用：为了解决SpringBoot 2.6.x与Springfox不兼容的问题，调用父类的方法生成相应的后置处理器
     */
    @Bean
    public BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return generateBeanPostProcessor();
    }

}
