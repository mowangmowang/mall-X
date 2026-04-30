package com.macro.mall.ai.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.macro.mall.ai.client.AiClient;
import com.macro.mall.ai.domain.*;
import com.macro.mall.ai.service.AiAssistantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantServiceImpl.class);

    private static final String QA_SYSTEM_PROMPT =
            "你是一个电商购物助手，帮助顾客了解商品信息。请根据提供的商品信息回答顾客的问题。" +
            "回答要简洁、准确、有帮助，使用中文。如果问题超出商品信息范围，诚实说明你不知道。";

    private static final String RETURN_SYSTEM_PROMPT =
            "你是一个电商售后客服助手。根据用户描述的问题，推荐最合适的退货原因和问题描述。" +
            "退货原因必须是以下之一：['质量问题', '商品与描述不符', '不想要了', '商品损坏', '其他']。" +
            "请以JSON格式返回，格式为：{\"reason\": \"退货原因\", \"description\": \"详细的问题描述（50-200字）\"}。" +
            "只返回JSON，不要包含其他文字。";

    @Autowired
    private AiClient aiClient;

    @Override
    public AiResponse chatAboutProduct(ProductQaRequest request) {
        String context = buildProductContext(request);
        String content = context + "\n\n【顾客问题】" + request.getQuestion();

        log.info("AI product Q&A - productId={}, question={}", request.getProductId(), request.getQuestion());
        String reply = aiClient.chat(QA_SYSTEM_PROMPT, content);

        return new AiResponse(reply);
    }

    @Override
    public ReturnSuggestionResult suggestReturn(ReturnSuggestionRequest request) {
        String content = String.format(
                "用户描述的问题：%s\n商品名称：%s\n商品属性：%s\n订单编号：%s",
                request.getIssue(),
                nullToEmpty(request.getProductName()),
                nullToEmpty(request.getProductAttr()),
                nullToEmpty(request.getOrderSn())
        );

        log.info("AI return suggest - issue={}", request.getIssue());
        String jsonResponse = aiClient.chat(RETURN_SYSTEM_PROMPT, content);

        return parseReturnSuggestion(jsonResponse, request.getIssue());
    }

    private String buildProductContext(ProductQaRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("【商品信息】\n");
        sb.append("名称：").append(nullToEmpty(request.getProductName())).append("\n");
        sb.append("品牌：").append(nullToEmpty(request.getProductBrand())).append("\n");
        sb.append("价格：").append(nullToEmpty(request.getProductPrice())).append("元\n");
        sb.append("描述：").append(nullToEmpty(request.getProductSubTitle()));
        return sb.toString();
    }

    private ReturnSuggestionResult parseReturnSuggestion(String json, String fallbackIssue) {
        ReturnSuggestionResult result = new ReturnSuggestionResult();
        try {
            String cleaned = json.trim();
            if (cleaned.startsWith("```")) {
                int start = cleaned.indexOf('\n');
                if (start > 0) cleaned = cleaned.substring(start + 1);
                int end = cleaned.lastIndexOf("```");
                if (end > 0) cleaned = cleaned.substring(0, end);
                cleaned = cleaned.trim();
            }
            JSONObject obj = JSONUtil.parseObj(cleaned);
            result.setSuggestedReason(obj.getStr("reason", "其他"));
            result.setSuggestedDescription(obj.getStr("description", fallbackIssue));
        } catch (Exception e) {
            log.warn("Failed to parse AI return suggestion JSON, using fallback. Raw: {}", json, e);
            result.setSuggestedReason("其他");
            result.setSuggestedDescription(fallbackIssue);
        }
        return result;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
