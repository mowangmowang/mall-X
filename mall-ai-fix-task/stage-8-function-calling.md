# Stage 8: 对话记忆 + Function Calling 升级退货建议 — 可选

## 🎯 目标
- 把"3 步硬编码引导"换成"AI 自己决定何时问何时答"
- 用 Spring AI 的 `@Tool` Function Calling 让 AI 自己调 `getEnabledReturnReasons()` 工具
- 用 `MessageWindowChatMemory` + Redis 存对话历史（mall-common 已有 `RedisService`）
- 去掉前端 `step` 字段（前后端协议简化）

## ⚠️ 前置条件
- Stage 3、4、6 完成
- 确认 mall-common 的 `RedisService` 可用（已有 Redis 连接配置）
- 前端团队同意协议变更（`step` 字段废弃）

## 📂 涉及文件

### 新增
- `mall-ai/src/main/java/com/macro/mall/ai/tools/ReturnReasonTools.java`（@Tool 函数）
- `mall-ai/src/main/java/com/macro/mall/ai/config/ChatMemoryConfig.java`
- `mall-ai/src/test/java/com/macro/mall/ai/tools/ReturnReasonToolsTest.java`
- `mall-ai/src/test/java/com/macro/mall/ai/config/ChatMemoryConfigIT.java`

### 修改
- `mall-ai/src/main/java/com/macro/mall/ai/service/impl/AiAssistantServiceImpl.java`（suggestReturn 重写）
- `mall-ai/src/main/java/com/macro/mall/ai/domain/ReturnSuggestionRequest.java`（`step` 字段标记 `@Deprecated`）
- `mall-ai/src/main/resources/application.yml`（prompt 改用新协议）

## 🔨 实施步骤

### Step 8.1: 新建 ReturnReasonTools（Function Calling）
```java
@Component
@RequiredArgsConstructor
public class ReturnReasonTools {

    private final ReturnReasonService returnReasonService;

    @Tool(description = "获取商城当前启用的所有退货原因列表。当你需要推荐退货原因时，先调用此工具获取准确选项。")
    public List<String> getEnabledReturnReasons() {
        return returnReasonService.getEnabledReturnReasons();
    }
}
```

### Step 8.2: 新建 ChatMemoryConfig
```java
@Configuration
@RequiredArgsConstructor
public class ChatMemoryConfig {

    private final RedisService redisService;  // mall-common 已有

    @Bean
    ChatMemory chatMemory() {
        // 用 Redis 存对话，max 20 条消息
        return MessageWindowChatMemory.builder()
            .maxMessages(20)
            .chatMemoryRepository(redisChatMemoryRepository())
            .build();
    }

    private ChatMemoryRepository redisChatMemoryRepository() {
        // 自定义 RedisChatMemoryRepository，委托给 mall-common RedisService
        return new ChatMemoryRepository() {
            @Override
            public List<Message> findByConversationId(String conversationId) {
                String json = redisService.get(redisKey(conversationId));
                if (json == null) return List.of();
                return JSON.parseArray(json, Message.class);
            }

            @Override
            public void saveAll(String conversationId, List<Message> messages) {
                redisService.set(redisKey(conversationId), 
                    JSON.toJSONString(messages), Duration.ofHours(2));
            }

            @Override
            public void deleteByConversationId(String conversationId) {
                redisService.del(redisKey(conversationId));
            }

            private String redisKey(String id) {
                return "mall:ai:chat:" + id;
            }
        };
    }
}
```

### Step 8.3: 重写 suggestReturn
```java
@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ReturnReasonTools returnReasonTools;
    private final BeanOutputConverter<ReturnSuggestionResult> converter = 
        new BeanOutputConverter<>(ReturnSuggestionResult.class);
    private final PromptProperties prompts;

    @PostConstruct
    void init() {
        // 配置 ChatClient：tools + memory advisor
        this.chatClient = ChatClient.builder(/* chatModel */)
            .defaultTools(returnReasonTools)
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .build();
    }

    @Override
    public ReturnSuggestionResult suggestReturn(ReturnSuggestionRequest req) {
        String sessionId = (req.sessionId() == null || req.sessionId().isEmpty())
            ? UUID.randomUUID().toString()
            : req.sessionId();
        String sanitized = InputSanitizer.sanitize(req.issue());

        Map<String, Object> vars = Map.of("format", converter.getFormat());
        String systemPrompt = new PromptTemplate(prompts.returnSuggestionSystemV2()).render(vars);

        try {
            ReturnSuggestionResult result = chatClient.prompt()
                .system(systemPrompt)
                .user(sanitized)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .entity(ReturnSuggestionResult.class);

            return result;
        } catch (Exception e) {
            log.warn("AI 退货建议 v2 失败，使用 fallback. err={}", e.getMessage());
            return fallbackResult(req);
        }
    }
}
```

### Step 8.4: 新增 prompt `return-suggestion-system-v2`
```yaml
ai:
  prompts:
    return-suggestion-system-v2: |
      你是专业电商售后客服助手。你可以通过调用 getEnabledReturnReasons 工具获取退货原因。
      
      任务：通过对话了解用户问题（1-3 轮即可，不必固定 3 轮），然后给出最终建议。
      
      关键原则：
      - 当用户描述问题后，主动调用工具获取最新退货原因列表
      - 调用工具后，从工具返回的列表中选择最匹配的退货原因
      - 当你认为已收集足够信息时，设置 finished=true 并返回 reason/description/category
      - 如果用户描述已很清晰，可以第 1 轮就给出建议
      
      {format}
```

### Step 8.5: 标记旧 `step` 字段
```java
public record ReturnSuggestionRequest(
    @NotBlank String issue,
    String productName,
    String productAttr,
    String orderSn,
    String sessionId,
    @Deprecated(since = "1.1", forRemoval = true)  // Stage 8 标记
    Integer step
) {}
```

## 🧪 测试细节

### 新增：`ReturnReasonToolsTest.java`
```java
@ExtendWith(MockitoExtension.class)
class ReturnReasonToolsTest {

    @Mock ReturnReasonService service;
    @InjectMocks ReturnReasonTools tools;

    @Test
    void getEnabledReturnReasons_delegatesToService() {
        when(service.getEnabledReturnReasons()).thenReturn(List.of("质量问题", "商品损坏"));

        List<String> result = tools.getEnabledReturnReasons();

        assertThat(result).containsExactly("质量问题", "商品损坏");
    }
}
```

### 新增：`ChatMemoryConfigIT.java`（真实 Redis 或 embedded redis）
```java
@SpringBootTest
@AutoConfigureMockBean
class ChatMemoryConfigIT {

    @Autowired ChatMemory memory;

    @Test
    void saveAndLoad_conversation() {
        String id = "test-session";
        UserMessage msg1 = new UserMessage("hi");
        AssistantMessage msg2 = new AssistantMessage("hello");

        memory.add(id, msg1);
        memory.add(id, msg2);

        List<Message> loaded = memory.get(id, 10);
        assertThat(loaded).hasSize(2);
    }
}
```

### 改写：`AiAssistantServiceTest.java`
- 旧 step-based 测试标记 `@Disabled` 或迁移到新协议
- 新增：`suggestReturn_v2_aiCallsTool_returnsResult`（用 mock `ChatClient` 验证 tool call 流程）

### 回归测试
- ✅ `mvn test -pl mall-ai -Dtest=ReturnReasonToolsTest`（新增）
- ✅ `mvn test -pl mall-ai -Dtest=ChatMemoryConfigIT`（新增）
- ✅ `mvn test -pl mall-ai -Dtest=AiAssistantServiceTest`（部分用例迁移）
- ✅ `mvn test -pl mall-ai -Dtest=CorsPreflightTest`

### 端到端
```bash
mvn spring-boot:run -pl mall-ai &

# 第 1 轮
curl -s -X POST http://localhost:8086/ai/return/suggest \
  -H "Content-Type: application/json" \
  -d '{"issue":"手机屏幕有裂痕","sessionId":"test-1"}' | jq .
# 预期: finished=false, guideQuestion="请问是开箱就有还是后来摔的？"

# 第 2 轮（用同一个 sessionId）
curl -s -X POST http://localhost:8086/ai/return/suggest \
  -H "Content-Type: application/json" \
  -d '{"issue":"开箱就有","sessionId":"test-1"}' | jq .
# 预期: finished=true, suggestedReason="商品损坏"
```

## 📊 验收标准
- [ ] AI 主动调 `getEnabledReturnReasons` tool（可通过 Spring AI debug log 验证）
- [ ] 同 sessionId 多次调用能维持上下文
- [ ] 旧 step=1/2/3 调用**仍能工作**（向后兼容）
- [ ] `mvn test -pl mall-ai` 全绿
- [ ] 端到端：1 轮 / 2 轮 / 3 轮 AI 都能完成引导

## 🌿 Git 操作
```bash
git checkout -b feat/mall-ai-stage-8-function-calling
git add mall-ai-fix-task/stage-8-function-calling.md
git commit -m "chore(mall-ai): Stage 8 任务文档"

git add mall-ai/src/test/java/com/macro/mall/ai/tools/ \
        mall-ai/src/test/java/com/macro/mall/ai/config/ChatMemoryConfigIT.java
git commit -m "test(mall-ai): Stage 8 Tools + ChatMemory 测试 (red)"

git add mall-ai/src/main/
git commit -m "feat(mall-ai): Stage 8 引入 Function Calling + 对话记忆

- 新建 ReturnReasonTools (@Tool 注解)
- 新建 ChatMemoryConfig (MessageWindowChatMemory + Redis)
- 重写 suggestReturn 用 ChatClient + Tools + Memory Advisor
- 新增 return-suggestion-system-v2 prompt (AI 自主决策)
- @Deprecated 标记 step 字段
- 行数：~80 → ~50

Refs: mall-ai-fix-task/stage-8-function-calling.md"

git add src/test/java/
git commit -m "test(mall-ai): Stage 8 全测试通过 (green)"

git add mall-ai/README.md mall-ai-fix-task/CHANGELOG.md
git commit -m "docs(mall-ai): Stage 8 README + CHANGELOG"

git push -u origin feat/mall-ai-stage-8-function-calling
gh pr create --title "[mall-ai] Stage 8: Function Calling + 对话记忆"
```

## ⚠️ 风险
- **风险 1**：Function Calling 在 DeepSeek 上的支持度（实测 DeepSeek 支持 tool_calls，行为与 OpenAI 接近）
- **风险 2**：`MessageChatMemoryAdvisor` 的 `CONVERSATION_ID` 传参方式因 Spring AI 版本而异
- **风险 3**：Redis ChatMemoryRepository 自定义实现需序列化 `Message` 对象，用 Fastjson（mall-common 已用 Hutool）或 Jackson
- **风险 4**：前端协议变更需同步修改 `mall-app-web` 的 `returnApply.vue`
- **风险 5**：Function Calling 调工具可能增加 1 次额外的 AI 调用 → 评估成本

## 🔙 回滚
```bash
git revert <merge-commit-of-stage-8>
# Tools 和 ChatMemory 会随之移除，旧 step-based 协议重新生效
```
