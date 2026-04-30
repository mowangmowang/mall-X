package com.macro.mall.ai.domain;

import io.swagger.annotations.ApiModelProperty;

public class ReturnSuggestionResult {
    
    @ApiModelProperty(value = "推荐的退货原因", example = "质量问题", allowableValues = "质量问题,商品与描述不符,不想要了,商品损坏,其他")
    private String suggestedReason;
    
    @ApiModelProperty(value = "推荐的问题描述", example = "商品收到后发现屏幕有明显裂痕，影响正常使用")
    private String suggestedDescription;

    public String getSuggestedReason() { return suggestedReason; }
    public void setSuggestedReason(String suggestedReason) { this.suggestedReason = suggestedReason; }
    public String getSuggestedDescription() { return suggestedDescription; }
    public void setSuggestedDescription(String suggestedDescription) { this.suggestedDescription = suggestedDescription; }
}
