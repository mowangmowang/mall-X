package com.macro.mall.ai.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
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
                throw new RuntimeException("AI API returned no choices");
            }
            List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
            if (choices.isEmpty()) {
                throw new RuntimeException("AI API returned empty choices");
            }
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            log.error("AI API call failed", e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage(), e);
        }
    }
}
