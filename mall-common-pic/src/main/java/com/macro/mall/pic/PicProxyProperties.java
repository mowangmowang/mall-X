package com.macro.mall.pic;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 图片代理配置项
 *
 * @author alan
 * @since 2026-06
 */
@Data
@ConfigurationProperties(prefix = "mall.pic")
public class PicProxyProperties {

    /**
     * 允许代理的上游 URL 前缀（白名单，防 SSRF）
     * <p>默认放行 macro-oss 与 localhost MinIO；生产环境请明确具体域名</p>
     */
    private List<String> allowedPrefixes = List.of(
            "http://macro-oss.oss-cn-shenzhen.aliyuncs.com/",
            "http://localhost:9000/"
    );

    /**
     * 上游请求超时（毫秒）
     */
    private Integer connectTimeout = 3000;

    /**
     * 上游读取超时（毫秒）
     */
    private Integer readTimeout = 10000;

    /**
     * 浏览器缓存时间（秒）
     */
    private Long cacheMaxAge = 86400L;
}
