package com.macro.mall.ai.controller;

import com.macro.mall.ai.domain.*;
import com.macro.mall.ai.service.AiAssistantService;
import com.macro.mall.common.api.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@Api(tags = "AiAssistantController")
@Tag(name = "AiAssistantController", description = "AI购物助手")
@RequestMapping("/ai")
public class AiAssistantController {

    @Autowired
    private AiAssistantService aiAssistantService;

    @ApiOperation(value = "AI商品问答", notes = "根据商品信息回答用户的问题，提供准确的商品信息和建议")
    @RequestMapping(value = "/product/qa", method = RequestMethod.POST)
    public CommonResult<AiResponse> productQa(
            @Valid @RequestBody @ApiParam(value = "商品问答请求参数", required = true) ProductQaRequest request) {
        AiResponse response = aiAssistantService.chatAboutProduct(request);
        return CommonResult.success(response);
    }

    @ApiOperation(value = "AI退货建议", notes = "根据用户描述的问题，智能推荐最合适的退货原因和问题描述")
    @RequestMapping(value = "/return/suggest", method = RequestMethod.POST)
    public CommonResult<ReturnSuggestionResult> returnSuggest(
            @Valid @RequestBody @ApiParam(value = "退货建议请求参数", required = true) ReturnSuggestionRequest request) {
        ReturnSuggestionResult result = aiAssistantService.suggestReturn(request);
        return CommonResult.success(result);
    }
}
