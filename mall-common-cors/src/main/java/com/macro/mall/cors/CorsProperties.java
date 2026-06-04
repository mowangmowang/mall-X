package com.macro.mall.cors;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * CORS 跨域策略配置项
 * <p>
 * 绑定 {@code application.yml} 的 {@code mall.security.cors.*} 配置。
 * mall-common-cors 模块的"单一来源"，4 个服务（admin/portal/search/ai）共享。
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * mall:
 *   security:
 *     cors:
 *       allowed-origins: "*"          # dev 用通配符，prod 必须改为具体域名
 *       allowed-methods: "*"
 *       allowed-headers: "*"
 *       allow-credentials: true
 *       max-age: 3600
 * </pre>
 * </p>
 *
 * @author alan
 * @since 2026-06
 */
@Data
@ConfigurationProperties(prefix = "mall.security.cors")
public class CorsProperties {

    /** 允许的来源（Origin）。"*" 时需配 allowCredentials=false，否则浏览器拒绝。 */
    private List<String> allowedOrigins;

    /** 允许的 HTTP 方法，"*" 表示全部 */
    private List<String> allowedMethods;

    /** 允许的请求头，"*" 表示全部。Authorization 头必须包含 */
    private List<String> allowedHeaders;

    /** 是否允许携带凭证（Cookie / Authorization） */
    private Boolean allowCredentials = false;

    /** 预检结果缓存时间（秒） */
    private Long maxAge = 3600L;
}
