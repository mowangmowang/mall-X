package com.macro.mall.ai.domain;

import io.swagger.annotations.ApiModelProperty;

public class ReturnSuggestionResult {
    
    @ApiModelProperty(value = "推荐的退货原因", example = "质量问题")
    private String suggestedReason;
    
    @ApiModelProperty(value = "推荐的问题描述", example = "商品收到后发现屏幕有明显裂痕，影响正常使用")
    private String suggestedDescription;
    
    @ApiModelProperty(value = "问题分类", example = "硬件故障", 
                      allowableValues = "硬件故障,软件问题,商品不符,物流损坏,主观原因")
    private String category;
    
    @ApiModelProperty(value = "置信度", example = "high", allowableValues = "high,medium,low")
    private String confidence;
    
    @ApiModelProperty(value = "分析说明", example = "根据描述'无法开机'，判断为硬件故障")
    private String analysisNote;

    @ApiModelProperty(value = "引导问题，用于下一步引导用户", example = "请问商品是否有物理损坏？")
    private String guideQuestion;

    @ApiModelProperty(value = "是否完成引导", example = "false")
    private Boolean finished;

    public String getSuggestedReason() { return suggestedReason; }
    public void setSuggestedReason(String suggestedReason) { this.suggestedReason = suggestedReason; }
    public String getSuggestedDescription() { return suggestedDescription; }
    public void setSuggestedDescription(String suggestedDescription) { this.suggestedDescription = suggestedDescription; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }
    public String getAnalysisNote() { return analysisNote; }
    public void setAnalysisNote(String analysisNote) { this.analysisNote = analysisNote; }
    public String getGuideQuestion() { return guideQuestion; }
    public void setGuideQuestion(String guideQuestion) { this.guideQuestion = guideQuestion; }
    public Boolean getFinished() { return finished; }
    public void setFinished(Boolean finished) { this.finished = finished; }
}
