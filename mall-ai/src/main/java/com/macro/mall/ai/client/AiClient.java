package com.macro.mall.ai.client;

import java.util.List;

/**
 * AI 客户端接口 (AI Client Interface)
 * 
 * <p>定义与 AI 模型交互的统一接口，实现模型无关的设计。</p>
 * 
 * <p><b>设计目的：</b></p>
 * <ul>
 *   <li>抽象 AI API 调用细节，业务层无需关心具体使用的 AI 服务商</li>
 *   <li>支持切换不同的 AI 模型（DeepSeek、OpenAI、SiliconFlow 等）</li>
 *   <li>只需修改配置文件，无需改动业务代码</li>
 * </ul>
 * 
 * <p><b>实现类：</b></p>
 * <ul>
 *   <li>{@link OpenAiCompatibleClient} - 兼容 OpenAI Chat Completions API 格式的实现</li>
 * </ul>
 * 
 * @author alan
 * @since 1.0
 */
public interface AiClient {

    /**
     * 简单对话 (Simple Chat)
     * 
     * <p>使用系统提示词和用户内容进行单次对话。</p>
     * 
     * <p><b>使用场景：</b></p>
     * <ul>
     *   <li>商品问答：系统提示词定义回答规范，用户内容为商品信息+问题</li>
     *   <li>退货建议：系统提示词定义引导流程，用户内容为问题描述+步骤信息</li>
     * </ul>
     * 
     * <p><b>示例：</b></p>
     * <pre>{@code
     * String systemPrompt = "你是专业电商购物助手...";
     * String userContent = "【商品信息】\n名称：Redmi Note 13\n...\n\n【顾客问题】\n这款手机拍照效果怎么样？";
     * String reply = aiClient.chat(systemPrompt, userContent);
     * }</pre>
     * 
     * @param systemPrompt 系统提示词 (System Prompt)，定义 AI 的角色、回答规范、禁止事项等
     * @param userContent 用户内容 (User Content)，包含具体的问题或任务描述
     * @return AI 生成的回复内容 (AI Generated Reply)
     */
    String chat(String systemPrompt, String userContent);

    /**
     * 多轮对话 (Multi-turn Chat)
     * 
     * <p>使用消息列表进行多轮对话，支持更复杂的对话场景。</p>
     * 
     * <p><b>消息角色：</b></p>
     * <ul>
     *   <li>system - 系统提示词，定义 AI 的行为规范</li>
     *   <li>user - 用户消息，包含用户的提问或指令</li>
     *   <li>assistant - AI 回复，记录之前的对话历史</li>
     * </ul>
     * 
     * <p><b>示例：</b></p>
     * <pre>{@code
     * List<ChatMessage> messages = new ArrayList<>();
     * messages.add(new ChatMessage("system", "你是专业客服助手..."));
     * messages.add(new ChatMessage("user", "我想退货"));
     * messages.add(new ChatMessage("assistant", "请问是什么原因需要退货？"));
     * messages.add(new ChatMessage("user", "商品有质量问题"));
     * String reply = aiClient.chat(messages);
     * }</pre>
     * 
     * @param messages 消息列表 (Message List)，按时间顺序排列的对话历史
     * @return AI 生成的最新回复内容 (AI Generated Reply)
     * @see ChatMessage 消息数据结构
     */
    String chat(List<ChatMessage> messages);
}
