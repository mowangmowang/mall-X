package com.macro.mall.common.config;

import com.macro.mall.common.domain.SwaggerProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.WebFluxRequestHandlerProvider;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Swagger基础配置类 (Swagger2的核心配置)
 * 这是一份提供给初学者的带有详细注释的Swagger2配置类，主要用于自动生成API文档。 */
public abstract class BaseSwaggerConfig {

    /**
     * @Bean 注解表示将其返回值作为Spring容器中的一个Bean进行管理。
     * Docket 是 Swagger 的主要配置对象，用来定义了Swagger的行为和提供哪些接口。
     */
    @Bean
    public Docket createRestApi() {
        // 调用我们自定义的抽象方法获取有关Swagger的自定义属性(如标题、版本等)
        SwaggerProperties swaggerProperties = swaggerProperties();
        
        // 实例化Docket对象，指定使用SWAGGER_2版本
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo(swaggerProperties)) // 配置文档的基本信息，如标题、描述、作者等
                .select() // 返回一个ApiSelectorBuilder实例，用来控制哪些接口暴露给Swagger来展现
                // 指明只暴露指定包(basePackage)下的接口
                .apis(RequestHandlerSelectors.basePackage(swaggerProperties.getApiBasePackage()))
                // 所有的请求(paths)都可以被纳入文档管理
                .paths(PathSelectors.any())
                .build();
                
        // 判断是否开启了安全认证(例如需要携带Token)
        if (swaggerProperties.isEnableSecurity()) {
            // 如果开启了，则配置如何传递和验证安全认证信息
            docket.securitySchemes(securitySchemes()).securityContexts(securityContexts());
        }
        return docket;
    }

    /**
     * 配置API文档的基本信息(展示在Swagger页面的顶部)
     * 包括：标题、描述、联系人信息、版本等
     */
    private ApiInfo apiInfo(SwaggerProperties swaggerProperties) {
        return new ApiInfoBuilder()
                .title(swaggerProperties.getTitle())               // 文档标题
                .description(swaggerProperties.getDescription())   // 文档描述
                // 联系人信息：姓名，网址，邮箱
                .contact(new Contact(swaggerProperties.getContactName(), swaggerProperties.getContactUrl(), swaggerProperties.getContactEmail()))
                .version(swaggerProperties.getVersion())           // 版本号
                .build();
    }

    /**
     * 配置安全机制 (SecurityScheme)
     * 主要定义了认证请求头信息的名称和传递方式，比如通过请求头(Header)传递 Authorization 字段
     */
    private List<SecurityScheme> securitySchemes() {
        // 设置请求头信息
        List<SecurityScheme> result = new ArrayList<>();
        // 第一个参数是Swagger页面的参数名，第二个是HTTP请求的Header名，第三个是参数所在位置(header)
        ApiKey apiKey = new ApiKey("Authorization", "Authorization", "header");
        result.add(apiKey);
        return result;
    }

    /**
     * 配置安全上下文 (SecurityContext)
     * 主要定义了哪些请求路径需要进行登录认证
     */
    private List<SecurityContext> securityContexts() {
        // 设置需要登录认证的路径
        List<SecurityContext> result = new ArrayList<>();
        // /*/.* 表示拦截所有路径的请求进行认证
        result.add(getContextByPath("/*/.*"));
        return result;
    }

    /**
     * 根据正则表达式匹配出需要进行认证的上下文对象
     */
    private SecurityContext getContextByPath(String pathRegex) {
        return SecurityContext.builder()
                // 为该上下文提供认证引用配置(即告诉Swagger这一系列路径应该用什么方式认证)
                .securityReferences(defaultAuth())
                // 定义操作过滤器，符合给定正则表达式的路径才纳入认证范围
                .operationSelector(oc -> oc.requestMappingPattern().matches(pathRegex))
                .build();
    }

    /**
     * 定义默认的安全引用 (SecurityReference)
     * 将前面定义的认证机制和作用域绑定在一起
     */
    private List<SecurityReference> defaultAuth() {
        List<SecurityReference> result = new ArrayList<>();
        // AuthorizationScope 用来定义作用域，"global" 表示全局，"accessEverything" 意味着能访问一切接口
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        // 创建引用：名称为"Authorization"(与securitySchemes方法中保持一致)，作用域使用了全局作用域
        result.add(new SecurityReference("Authorization", authorizationScopes));
        return result;
    }

    /**
     * 解决 SpringBoot 2.6.x 以后由于 MVC 路径匹配策略改变导致 Swagger 报 NullPointerException 的兼容性问题
     * 这个 Bean 后置处理器会修改 Springfox 的映射处理逻辑，剔除不能解析的映射模式
     */
    public BeanPostProcessor generateBeanPostProcessor(){
        return new BeanPostProcessor() {

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                // 如果是Springfox中用来提供映射请求类的实例
                if (bean instanceof WebMvcRequestHandlerProvider || bean instanceof WebFluxRequestHandlerProvider) {
                    customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
                }
                return bean;
            }

            private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
                // 过滤由于 SpringBoot 2.6.x 的新路由匹配逻辑带来的空指针映射
                List<T> copy = mappings.stream()
                        .filter(mapping -> mapping.getPatternParser() == null)
                        .collect(Collectors.toList());
                mappings.clear();
                mappings.addAll(copy);
            }

            // 使用反射机制强制获取处理器映射列表 (忽略了私有属性的访问限制)
            @SuppressWarnings("unchecked")
            private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
                try {
                    Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                    field.setAccessible(true);
                    return (List<RequestMappingInfoHandlerMapping>) field.get(bean);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }

    /**
     * 自定义Swagger配置的抽象方法
     * 具体的模块（如admin、portal）在继承本类时，需提供自己模块独有的接口扫描包名、标题等参数
     */
    public abstract SwaggerProperties swaggerProperties();
}
