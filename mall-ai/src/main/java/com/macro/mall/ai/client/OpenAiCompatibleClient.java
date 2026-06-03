package com.macro.mall.ai.client;

import com.macro.mall.ai.exception.AiApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

public class OpenAiCompatibleClient implements AiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleClient.class);

    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final Double temperature;
    private final Integer maxTokens;
    private final RestTemplate restTemplate;

    public OpenAiCompatibleClient(String baseUrl, String apiKey, String model,
                                  Double temperature, Integer maxTokens,
                                  RestTemplate restTemplate) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.restTemplate = restTemplate;
    }

    @Override
    public String chat(String systemPrompt, String userContent) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", systemPrompt));
        messages.add(new ChatMessage("user", userContent));
        return chat(messages);
    }

    @Override
    public String chat(List<ChatMessage> messages) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", maxTokens);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String url = baseUrl + "/chat/completions";
        log.debug("Calling AI API: {} model={}", url, model);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map body = response.getBody();
            if (body == null || !body.containsKey("choices")) {
                throw new AiApiException("AI API 返回数据格式错误：缺少 choices 字段");
            }
            List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
            if (choices.isEmpty()) {
                throw new AiApiException("AI API 返回空响应：choices 为空");
            }
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");
            
            log.debug("AI API call succeeded, response length: {}", content != null ? content.length() : 0);
            return content;
        } catch (HttpClientErrorException e) {
            // 客户端错误（4xx）：认证失败、参数错误等
            log.error("AI API client error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiApiException(e.getStatusCode().value(), "AI API 调用失败: " + e.getStatusCode(), e);
        } catch (HttpServerErrorException e) {
            // 服务端错误（5xx）：AI 服务内部错误
            log.error("AI API server error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiApiException(e.getStatusCode().value(), "AI 服务暂时不可用，请稍后重试", e);
        } catch (ResourceAccessException e) {
            // 网络异常：超时、连接拒绝等
            log.error("AI API network error: {}", e.getMessage());
            throw new AiApiException("AI API 网络请求失败: " + e.getMessage(), e);
        } catch (AiApiException e) {
            // 已经包装过的 AI 异常，直接抛出
            throw e;
        } catch (Exception e) {
            // 其他未知异常
            log.error("AI API unexpected error: {}", e.getMessage(), e);
            throw new AiApiException("AI 服务调用异常: " + e.getMessage(), e);
        }
    }
}
