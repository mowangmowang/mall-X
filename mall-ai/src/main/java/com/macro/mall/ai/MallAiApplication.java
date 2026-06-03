package com.macro.mall.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Mall-AI 微服务启动类 (Mall AI Microservice Application)
 * 
 * <p>这是 mall-ai 模块的入口类，负责启动 Spring Boot 应用。</p>
 * 
 * <p><b>功能说明：</b></p>
 * <ul>
 *   <li>扫描 com.macro.mall 包下的所有组件（包括 mall-common 中的通用组件）</li>
 *   <li>初始化 Spring 容器和 Bean 依赖</li>
 *   <li>启动内嵌的 Tomcat 服务器（默认端口 8086）</li>
 * </ul>
 * 
 * <p><b>使用示例：</b></p>
 * <pre>{@code
 * // 方式1：直接运行主类
 * java -jar mall-ai-1.0-SNAPSHOT.jar
 * 
 * // 方式2：使用 Maven 插件
 * mvn spring-boot:run -pl mall-ai -am
 * }</pre>
 * 
 * @author alan
 * @since 1.0
 */
@SpringBootApplication(scanBasePackages = "com.macro.mall")
public class MallAiApplication {

    /**
     * 程序入口方法 (Main Entry Point)
     * 
     * @param args 命令行参数数组 (Command Line Arguments)
     */
    public static void main(String[] args) {
        SpringApplication.run(MallAiApplication.class, args);
    }
}
