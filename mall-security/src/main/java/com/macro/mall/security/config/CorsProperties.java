package com.macro.mall.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * CORS 跨域策略配置项
 * <p>
 * 将跨域规则从硬编码迁移到 {@code application.yml}，通过 {@code mall.security.cors.*} 绑定。
 * 这样不同环境（dev / prod）可以差异化配置，代码中不再出现魔法字符串。
 * </p>
 * <p>
 * 配置示例（{@code application.yml}）：
 * <pre>
 * mall:
 *   security:
 *     cors:
 *       allowed-origins: "*"          # 生产环境必须改为具体域名
 *       allowed-methods: "*"
 *       allowed-headers: "*"
 *       allow-credentials: true
 *       max-age: 3600
 * </pre>
 * </p>
 * <p>
 * 设计要点：
 * <ul>
 *   <li>列表类型属性（allowed-origins / allowed-methods / allowed-headers）支持 YAML 数组写法，
 *       也支持逗号分隔字符串，符合 Spring Boot 标准配置习惯。</li>
 *   <li>{@code allowCredentials} 默认 false，避免与 {@code allowedOrigins=["*"]} 组合时触发
 *       浏览器拒绝（W3C CORS 规范明确禁止 credentials + 通配符 origin）。</li>
 *   <li>{@code maxAge} 默认 3600 秒，浏览器会在该时间窗内缓存预检结果，减少 OPTIONS 请求频次。</li>
 * </ul>
 * </p>
 *
 * @author alan
 * @since 2026-06
 */
@Data
@ConfigurationProperties(prefix = "mall.security.cors")
public class CorsProperties {

    /**
     * 允许的来源（Origin）
     * <p>
     * 使用 {@code "*"} 时需配 {@code allowCredentials=false}，否则浏览器拒绝。生产环境必须指定具体域名。
     * </p>
     */
    private List<String> allowedOrigins;

    /**
     * 允许的 HTTP 方法（GET / POST / PUT / DELETE 等），{@code "*"} 表示全部
     */
    private List<String> allowedMethods;

    /**
     * 允许的请求头，{@code "*"} 表示全部
     * <p>
     * Authorization 头必须在此列中或使用通配符，否则浏览器预检失败（"Request header field
     * authorization is not allowed..."）。
     * </p>
     */
    private List<String> allowedHeaders;

    /**
     * 是否允许携带凭证（Cookie / Authorization）
     * <p>
     * 生产环境强烈建议仅对受信域名开启。
     * </p>
     */
    private Boolean allowCredentials = false;

    /**
     * 预检结果缓存时间（秒），减少重复 OPTIONS 请求。默认 3600 秒（1 小时）
     */
    private Long maxAge = 3600L;
}
