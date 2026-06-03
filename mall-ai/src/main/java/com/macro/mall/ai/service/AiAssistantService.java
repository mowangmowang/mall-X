package com.macro.mall.ai.service;

import com.macro.mall.ai.domain.AiResponse;
import com.macro.mall.ai.domain.ProductQaRequest;
import com.macro.mall.ai.domain.ReturnSuggestionRequest;
import com.macro.mall.ai.domain.ReturnSuggestionResult;

/**
 * AI 助手服务接口 (AI Assistant Service Interface)
 * 
 * <p>定义 AI 购物助手的核心业务方法，包括商品问答和退货建议两大功能。</p>
 * 
 * <p><b>设计说明：</b></p>
 * <ul>
 *   <li>采用接口隔离原则，便于后续扩展其他 AI 功能</li>
 *   <li>实现类通过 @Service 注解标记为 Spring 服务组件</li>
 *   <li>所有方法均处理输入安全清理、Prompt 构建、AI 调用、响应解析等逻辑</li>
 * </ul>
 * 
 * @author alan
 * @since 1.0
 */
public interface AiAssistantService {

    /**
     * 商品问答 (Chat About Product)
     * 
     * <p>根据用户提问和商品信息，调用 AI 模型生成专业、准确的商品介绍回答。</p>
     * 
     * <p><b>处理流程：</b></p>
     * <ol>
     *   <li>对用户问题进行安全清理（防止 Prompt Injection 攻击）</li>
     *   <li>构建商品信息上下文（名称、品牌、价格、描述等）</li>
     *   <li>添加对话历史（如果存在多轮对话）</li>
     *   <li>调用 AI 客户端生成回答</li>
     *   <li>返回 AI 回复内容</li>
     * </ol>
     * 
     * <p><b>注意事项：</b></p>
     * <ul>
     *   <li>AI 回答限制在 100 字以内，控制 Token 消耗</li>
     *   <li>禁止 AI 编造未提供的商品信息</li>
     *   <li>禁止使用绝对化词语（如“绝对”、“保证”）</li>
     * </ul>
     * 
     * @param request 商品问答请求参数，包含商品ID、问题、商品信息等
     * @return AI 回复内容，封装在 AiResponse 对象中
     * @see ProductQaRequest 请求参数结构
     * @see AiResponse 响应数据结构
     */
    AiResponse chatAboutProduct(ProductQaRequest request);

    /**
     * 退货建议 (Suggest Return Reason)
     * 
     * <p>根据用户描述的售后问题，通过多轮引导对话，智能推荐最合适的退货原因和标准化描述。</p>
     * 
     * <p><b>3轮引导流程：</b></p>
     * <ol>
     *   <li><b>第1轮 (step=1)</b>：询问故障现象
     *     <ul><li>目标：了解商品出现了什么具体问题</li>
     *         <li>示例：“请问商品具体出现了什么问题？是无法开机、屏幕显示异常还是有其他表现？”</li></ul>
     *   </li>
     *   <li><b>第2轮 (step=2)</b>：追问细节
     *     <ul><li>目标：了解故障的细节或用户已尝试的解决方式</li>
     *         <li>示例：“请问这个问题是突然出现的还是一直存在？您是否尝试过重启或其他解决方式？”</li></ul>
     *   </li>
     *   <li><b>第3轮 (step=3)</b>：确认并给出建议
     *     <ul><li>目标：确认问题影响，给出最终退货建议</li>
     *         <li>输出：退货原因、标准化描述、问题分类、置信度</li></ul>
     *   </li>
     * </ol>
     * 
     * <p><b>状态管理：</b></p>
     * <ul>
     *   <li>前端维护 step（当前步骤）和 sessionId（会话ID）</li>
     *   <li>后端根据 step 值执行对应的引导逻辑</li>
     *   <li>第3步时强制设置 finished=true，并提供兜底默认值</li>
     * </ul>
     * 
     * <p><b>动态 Prompt：</b></p>
     * <ul>
     *   <li>从数据库动态获取启用的退货原因列表</li>
     *   <li>确保与后台配置的退货政策保持一致</li>
     *   <li>查询失败时使用默认列表降级</li>
     * </ul>
     * 
     * @param request 退货建议请求参数，包含问题描述、商品信息、当前步骤、会话ID等
     * @return 退货建议结果，包含推荐原因、描述、引导问题、完成标记等
     * @see ReturnSuggestionRequest 请求参数结构
     * @see ReturnSuggestionResult 响应数据结构
     */
    ReturnSuggestionResult suggestReturn(ReturnSuggestionRequest request);
}
