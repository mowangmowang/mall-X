package com.macro.mall.ai.client;

import java.util.List;

public interface AiClient {

    String chat(String systemPrompt, String userContent);

    String chat(List<ChatMessage> messages);
}
