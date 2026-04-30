package com.macro.mall.ai.service;

import com.macro.mall.ai.domain.AiResponse;
import com.macro.mall.ai.domain.ProductQaRequest;
import com.macro.mall.ai.domain.ReturnSuggestionRequest;
import com.macro.mall.ai.domain.ReturnSuggestionResult;

public interface AiAssistantService {

    AiResponse chatAboutProduct(ProductQaRequest request);

    ReturnSuggestionResult suggestReturn(ReturnSuggestionRequest request);
}
