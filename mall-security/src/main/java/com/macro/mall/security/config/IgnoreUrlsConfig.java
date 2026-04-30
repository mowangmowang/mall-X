package com.macro.mall.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Security 白名单资源路径配置 (Ignore URLs Configuration)
 * <p>
 * 通过 {@link ConfigurationProperties} 从配置文件读取不需要鉴权的 URL 路径列表。
 * 配置示例（application.yml）：
 * <pre>
 * secure:
 *   ignored:
 *     urls:
 *       - /api/auth/login
 *       - /api/auth/register
 *       - /swagger-ui/**
 * </pre>
 * </p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "secure.ignored")
public class IgnoreUrlsConfig {

    /**
     * 白名单 URL 路径列表，支持 Ant 风格通配符（如 **、*）
     */
    private List<String> urls = new ArrayList<>();

}
