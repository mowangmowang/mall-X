package com.macro.mall.ai.config;

import com.macro.mall.ai.client.AiClient;
import com.macro.mall.ai.client.OpenAiCompatibleClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

@Configuration
@ConfigurationProperties(prefix = "ai.client")
public class AiClientConfig {

    private static final Logger log = LoggerFactory.getLogger(AiClientConfig.class);

    private String baseUrl = "https://api.deepseek.com/v1";
    private String apiKey;
    private String model = "deepseek-chat";
    private Double temperature = 0.7;
    private Integer maxTokens = 1024;

    @Bean
    public AiClient aiClient(RestTemplate restTemplate) {
        return new OpenAiCompatibleClient(baseUrl, apiKey, model,
                temperature, maxTokens, restTemplate);
    }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }

    /**
     * 在 Bean 初始化后校验必填配置项
     */
    @PostConstruct
    public void validate() {
        if (!StringUtils.hasText(apiKey)) {
            log.error("AI API Key is not configured! Please set environment variable AI_API_KEY or configure ai.client.api-key in application.yml");
            throw new IllegalStateException("AI API Key 未配置，请设置环境变量 AI_API_KEY 或在配置文件中设置 ai.client.api-key");
        }
        
        if (!StringUtils.hasText(baseUrl)) {
            log.error("AI Base URL is not configured!");
            throw new IllegalStateException("AI Base URL 未配置");
        }
        
        log.info("AI Client configuration validated successfully. Model: {}, BaseURL: {}", model, baseUrl);
    }
}
