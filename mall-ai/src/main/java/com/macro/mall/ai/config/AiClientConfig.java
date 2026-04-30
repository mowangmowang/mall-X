package com.macro.mall.ai.config;

import com.macro.mall.ai.client.AiClient;
import com.macro.mall.ai.client.OpenAiCompatibleClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConfigurationProperties(prefix = "ai.client")
public class AiClientConfig {

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
}
