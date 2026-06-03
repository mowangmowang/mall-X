package com.macro.mall.ai.domain;

import io.swagger.annotations.ApiModelProperty;

/**
 * 退货建议结果 (Return Suggestion Result)
 * 
 * <p>封装 AI 生成的退货建议，包括推荐的退货原因、标准化描述、问题分类等。</p>
 * 
 * <p><b>使用场景：</b></p>
 * <ul>
 *   <li>多轮对话的前两步（step=1,2）：仅返回 guideQuestion 引导问题，finished=false</li>
 *   <li>第3步（step=3）：返回完整的退货建议，finished=true，前端自动填写表单</li>
 * </ul>
 * 
 * <p><b>响应示例（第3步完成）：</b></p>
 * <pre>{@code
 * {
 *   "suggestedReason": "商品损坏",
 *   "suggestedDescription": "收到的手机屏幕有一条裂缝，属于物流中造成的损坏",
 *   "category": "硬件故障",
 *   "confidence": "high",
 *   "guideQuestion": "明白了，这确实影响了您的正常使用。我将为您推荐最合适的退货原因。",
 *   "finished": true,
 *   "analysisNote": "根据描述'屏幕有裂痕'，判断为硬件故障，匹配'商品损坏'原因"
 * }
 * }</pre>
 * 
 * @author alan
 * @since 1.0
 */
public class ReturnSuggestionResult {
    
    /**
     * 推荐的退货原因 (Suggested Return Reason)
     * <p>从数据库启用的退货原因列表中选择，如“质量问题”、“商品损坏”等</p>
     * <p>仅在 step=3 且 finished=true 时提供，step=1/2 时为空字符串</p>
     */
    @ApiModelProperty(value = "推荐的退货原因", example = "质量问题")
    private String suggestedReason;
    
    /**
     * 推荐的问题描述 (Suggested Description)
     * <p>标准化的问题描述，基于用户多轮对话内容生成</p>
     * <p>要求具体、清晰，包含故障现象和细节</p>
     * <p>正确示例：“商品镜头内部进灰，从购买时一直存在，影响拍照效果”</p>
     * <p>错误示例：“该商品一直存在故障”（太笼统）</p>
     */
    @ApiModelProperty(value = "推荐的问题描述", example = "商品收到后发现屏幕有明显裂痕，影响正常使用")
    private String suggestedDescription;
    
    /**
     * 问题分类 (Problem Category)
     * <p>对问题进行分类，便于后续统计分析</p>
     * <p>可选值：硬件故障、软件问题、商品不符、物流损坏、主观原因</p>
     */
    @ApiModelProperty(value = "问题分类", example = "硬件故障", 
                      allowableValues = "硬件故障,软件问题,商品不符,物流损坏,主观原因")
    private String category;
    
    /**
     * 置信度 (Confidence Level)
     * <p>AI 对推荐结果的信心程度</p>
     * <p>可选值：high（高）、medium（中）、low（低）</p>
     */
    @ApiModelProperty(value = "置信度", example = "high", allowableValues = "high,medium,low")
    private String confidence;
    
    /**
     * 分析说明 (Analysis Note)
     * <p>AI 对问题的分析过程说明，用于调试和理解 AI 的推理逻辑</p>
     * <p>示例：“根据描述'无法开机'，判断为硬件故障，匹配'质量问题'原因”</p>
     */
    @ApiModelProperty(value = "分析说明", example = "根据描述'无法开机'，判断为硬件故障")
    private String analysisNote;

    /**
     * 引导问题 (Guide Question)
     * <p>当前步骤需要问用户的问题，用于引导用户提供更多信息</p>
     * <p>step=1/2 时：询问故障现象或追问细节</p>
     * <p>step=3 时：确认性语句，如“明白了，这确实影响了您的正常使用...”</p>
     */
    @ApiModelProperty(value = "引导问题，用于下一步引导用户", example = "请问商品是否有物理损坏？")
    private String guideQuestion;

    /**
     * 是否完成引导 (Finished Flag)
     * <p>标记多轮引导对话是否完成</p>
     * <ul>
     *   <li>false - 引导未完成，前端应显示 guideQuestion 并等待用户回答</li>
     *   <li>true - 引导已完成，前端应自动填写表单并关闭 AI 弹窗</li>
     * </ul>
     */
    @ApiModelProperty(value = "是否完成引导", example = "false")
    private Boolean finished;

    // Getter and Setter methods with detailed comments
    
    /**
     * 获取推荐的退货原因 (Get Suggested Return Reason)
     * @return 退货原因字符串
     */
    public String getSuggestedReason() { return suggestedReason; }
    
    /**
     * 设置推荐的退货原因 (Set Suggested Return Reason)
     * @param suggestedReason 退货原因字符串
     */
    public void setSuggestedReason(String suggestedReason) { this.suggestedReason = suggestedReason; }
    
    /**
     * 获取推荐的问题描述 (Get Suggested Description)
     * @return 标准化问题描述
     */
    public String getSuggestedDescription() { return suggestedDescription; }
    
    /**
     * 设置推荐的问题描述 (Set Suggested Description)
     * @param suggestedDescription 标准化问题描述
     */
    public void setSuggestedDescription(String suggestedDescription) { this.suggestedDescription = suggestedDescription; }
    
    /**
     * 获取问题分类 (Get Problem Category)
     * @return 问题分类字符串
     */
    public String getCategory() { return category; }
    
    /**
     * 设置问题分类 (Set Problem Category)
     * @param category 问题分类字符串
     */
    public void setCategory(String category) { this.category = category; }
    
    /**
     * 获取置信度 (Get Confidence Level)
     * @return 置信度字符串（high/medium/low）
     */
    public String getConfidence() { return confidence; }
    
    /**
     * 设置置信度 (Set Confidence Level)
     * @param confidence 置信度字符串
     */
    public void setConfidence(String confidence) { this.confidence = confidence; }
    
    /**
     * 获取分析说明 (Get Analysis Note)
     * @return 分析说明文本
     */
    public String getAnalysisNote() { return analysisNote; }
    
    /**
     * 设置分析说明 (Set Analysis Note)
     * @param analysisNote 分析说明文本
     */
    public void setAnalysisNote(String analysisNote) { this.analysisNote = analysisNote; }
    
    /**
     * 获取引导问题 (Get Guide Question)
     * @return 引导问题文本
     */
    public String getGuideQuestion() { return guideQuestion; }
    
    /**
     * 设置引导问题 (Set Guide Question)
     * @param guideQuestion 引导问题文本
     */
    public void setGuideQuestion(String guideQuestion) { this.guideQuestion = guideQuestion; }
    
    /**
     * 获取完成标记 (Get Finished Flag)
     * @return 是否完成引导
     */
    public Boolean getFinished() { return finished; }
    
    /**
     * 设置完成标记 (Set Finished Flag)
     * @param finished 是否完成引导
     */
    public void setFinished(Boolean finished) { this.finished = finished; }
}
