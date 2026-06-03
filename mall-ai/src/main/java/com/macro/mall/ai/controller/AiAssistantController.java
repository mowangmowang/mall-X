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
/**
 * AI 购物助手控制器 (AI Shopping Assistant Controller)
 * 
 * <p>提供 RESTful API 接口，处理前端发起的 AI 相关请求。</p>
 * 
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>AI 商品问答：根据商品信息回答用户问题</li>
 *   <li>AI 退货建议：智能推荐退货原因和问题描述</li>
 * </ul>
 * 
 * <p><b>接口路径前缀：</b> /ai</p>
 * 
 *
 * @since 1.0
 */
@RestController
@Api(tags = "AiAssistantController")
@Tag(name = "AiAssistantController", description = "AI购物助手")
@RequestMapping("/ai")
public class AiAssistantController {

    /**
     * AI 助手服务层实例 (AI Assistant Service Instance)
     * 通过 Spring 依赖注入自动装配
     */
    @Autowired
    private AiAssistantService aiAssistantService;

    /**
     * AI 商品问答接口 (AI Product Q&A API)
     * 
     * <p>接收用户对商品的提问，结合商品信息调用 AI 模型生成专业回答。</p>
     * 
     * <p><b>业务场景：</b></p>
     * <ul>
     *   <li>用户询问商品材质、尺寸、功能等详细信息</li>
     *   <li>用户咨询商品是否适合特定用途（如送礼、运动等）</li>
     *   <li>多轮对话中追问商品细节</li>
     * </ul>
     * 
     * <p><b>请求示例：</b></p>
     * <pre>{@code
     * POST /ai/product/qa
     * Content-Type: application/json
     * 
     * {
     *   "productId": 1,
     *   "question": "这款手机拍照效果怎么样？",
     *   "productName": "Redmi Note 13",
     *   "productBrand": "小米",
     *   "productPrice": "1999",
     *   "productSubTitle": "性能小钢炮 5G 手机"
     * }
     * }</pre>
     * 
     * <p><b>响应示例：</b></p>
     * <pre>{@code
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": {
     *     "reply": "该手机配备XX万像素摄像头，支持光学防抖和夜景模式..."
     *   }
     * }
     * }</pre>
     * 
     * @param request 商品问答请求参数，包含商品ID、用户问题、商品信息等
     * @return 统一响应格式，data 字段包含 AI 回复内容
     * @see ProductQaRequest 请求参数定义
     * @see AiResponse 响应数据结构
     */
    @ApiOperation(value = "AI商品问答", notes = "根据商品信息回答用户的问题，提供准确的商品信息和建议")
    @RequestMapping(value = "/product/qa", method = RequestMethod.POST)
    public CommonResult<AiResponse> productQa(
            @Valid @RequestBody @ApiParam(value = "商品问答请求参数", required = true) ProductQaRequest request) {
        // 调用业务层处理商品问答逻辑
        AiResponse response = aiAssistantService.chatAboutProduct(request);
        // 返回统一成功响应
        return CommonResult.success(response);
    }

    /**
     * AI 退货建议接口 (AI Return Suggestion API)
     * 
     * <p>接收用户描述的售后问题，通过多轮引导对话，智能推荐最合适的退货原因和标准化描述。</p>
     * 
     * <p><b>业务流程：</b></p>
     * <ol>
     *   <li><b>第1轮 (step=1)</b>：询问故障现象 - "请问商品具体出现了什么问题？"</li>
     *   <li><b>第2轮 (step=2)</b>：追问细节 - "这个问题是突然出现的还是一直存在？"</li>
     *   <li><b>第3轮 (step=3)</b>：确认并给出建议 - 返回推荐的退货原因和描述</li>
     * </ol>
     * 
     * <p><b>请求示例（第1轮）：</b></p>
     * <pre>{@code
     * POST /ai/return/suggest
     * Content-Type: application/json
     * 
     * {
     *   "issue": "手机有问题",
     *   "step": 1,
     *   "sessionId": "uuid-xxx"
     * }
     * }</pre>
     * 
     * <p><b>响应示例（第3轮完成）：</b></p>
     * <pre>{@code
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": {
     *     "suggestedReason": "商品损坏",
     *     "suggestedDescription": "收到的手机屏幕有一条裂缝，属于物流中造成的损坏",
     *     "category": "硬件故障",
     *     "confidence": "high",
     *     "finished": true
     *   }
     * }
     * }</pre>
     * 
     * @param request 退货建议请求参数，包含问题描述、商品信息、当前步骤等
     * @return 统一响应格式，data 字段包含推荐的退货原因、描述、引导问题等
     * @see ReturnSuggestionRequest 请求参数定义
     * @see ReturnSuggestionResult 响应数据结构
     */
    @ApiOperation(value = "AI退货建议", notes = "根据用户描述的问题，智能推荐最合适的退货原因和问题描述")
    @RequestMapping(value = "/return/suggest", method = RequestMethod.POST)
    public CommonResult<ReturnSuggestionResult> returnSuggest(
            @Valid @RequestBody @ApiParam(value = "退货建议请求参数", required = true) ReturnSuggestionRequest request) {
        // 调用业务层处理退货建议逻辑
        ReturnSuggestionResult result = aiAssistantService.suggestReturn(request);
        // 返回统一成功响应
        return CommonResult.success(result);
    }
}
