package com.macro.mall.ai.controller;

import com.macro.mall.ai.domain.*;
import com.macro.mall.ai.service.AiAssistantService;
import com.macro.mall.common.api.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "AiAssistantController")
@Tag(name = "AiAssistantController", description = "AI购物助手")
@RequestMapping("/ai")
public class AiAssistantController {

    @Autowired
    private AiAssistantService aiAssistantService;

    @ApiOperation("AI商品问答")
    @RequestMapping(value = "/product/qa", method = RequestMethod.POST)
    public CommonResult<AiResponse> productQa(@RequestBody ProductQaRequest request) {
        AiResponse response = aiAssistantService.chatAboutProduct(request);
        return CommonResult.success(response);
    }

    @ApiOperation("AI退货建议")
    @RequestMapping(value = "/return/suggest", method = RequestMethod.POST)
    public CommonResult<ReturnSuggestionResult> returnSuggest(
            @RequestBody ReturnSuggestionRequest request) {
        ReturnSuggestionResult result = aiAssistantService.suggestReturn(request);
        return CommonResult.success(result);
    }
}
