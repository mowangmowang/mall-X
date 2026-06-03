package com.macro.mall.ai.client;

/**
 * 聊天消息数据结构 (Chat Message Data Structure)
 * 
 * <p>封装单条聊天消息，包含角色和内容两个字段。</p>
 * 
 * <p><b>对应 OpenAI API 格式：</b></p>
 * <pre>{@code
 * {
 *   "role": "user",
 *   "content": "你好，请问这款手机怎么样？"
 * }
 * }</pre>
 * 
 * <p><b>角色类型 (Role Types)：</b></p>
 * <ul>
 *   <li><b>system</b> - 系统提示词：定义 AI 的行为规范、回答风格等</li>
 *   <li><b>user</b> - 用户消息：用户的提问、指令或输入内容</li>
 *   <li><b>assistant</b> - AI 回复：AI 生成的回答，用于构建对话历史</li>
 * </ul>
 * 
 * @author alan
 * @since 1.0
 */
public class ChatMessage {
    
    /**
     * 消息角色 (Message Role)
     * <p>可选值："system"、"user"、"assistant"</p>
     */
    private String role;
    
    /**
     * 消息内容 (Message Content)
     * <p>实际的文本内容，可以是问题、回答或指令</p>
     */
    private String content;

    /**
     * 默认构造函数 (Default Constructor)
     * <p>用于 JSON 反序列化</p>
     */
    public ChatMessage() {}

    /**
     * 带参构造函数 (Parameterized Constructor)
     * 
     * @param role 消息角色 (Message Role)，如 "user"、"system"、"assistant"
     * @param content 消息内容 (Message Content)，实际的文本内容
     */
    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    /**
     * 获取消息角色 (Get Message Role)
     * 
     * @return 角色字符串，如 "user"、"system"、"assistant"
     */
    public String getRole() { 
        return role; 
    }
    
    /**
     * 设置消息角色 (Set Message Role)
     * 
     * @param role 角色字符串，如 "user"、"system"、"assistant"
     */
    public void setRole(String role) { 
        this.role = role; 
    }
    
    /**
     * 获取消息内容 (Get Message Content)
     * 
     * @return 消息的文本内容
     */
    public String getContent() { 
        return content; 
    }
    
    /**
     * 设置消息内容 (Set Message Content)
     * 
     * @param content 消息的文本内容
     */
    public void setContent(String content) { 
        this.content = content; 
    }
}
