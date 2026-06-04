package com.macro.mall.pic;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 图片代理配置
 *
 * @author alan
 * @since 2026-06
 */
@Configuration
@EnableConfigurationProperties(PicProxyProperties.class)
public class PicProxyConfig {

    /**
     * 内部 RestTemplate，不传 Origin 头（绕开 OSS CORS 限制）
     */
    @Bean
    public RestTemplate picProxyRestTemplate(PicProxyProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.getConnectTimeout());
        factory.setReadTimeout(props.getReadTimeout());
        return new RestTemplate(factory);
    }
}
