# Stage 7: 流式输出 (SSE) — 可选

## 🎯 目标
- 让前端体验从"等 3 秒弹一坨"变成"逐字打字机效果"
- 新增 `Flux<String>` 类型的 SSE 端点 `/ai/product/qa/stream` 和 `/ai/return/suggest/stream`
- 旧端点保留，行为不变（向后兼容）

## ⚠️ 前置条件
- Stage 3、4 完成
- 确认前端（`mall-app-web`）能消费 SSE（`EventSource` 或 `fetch` + reader）

## 📂 涉及文件

### 新增
- `mall-ai/src/main/java/com/macro/mall/ai/controller/AiStreamController.java`
- `mall-ai/src/test/java/com/macro/mall/ai/controller/AiStreamControllerTest.java`
- `mall-ai/src/main/java/com/macro/mall/ai/chat/AiChatService.java`（**改**：加 `stream()` 方法）

### 修改
- `mall-ai/src/main/java/com/macro/mall/ai/chat/AiChatService.java`

## 🔨 实施步骤

### Step 7.1: AiChatService 加 stream 方法
```java
public Flux<String> streamChat(String systemPrompt, String userContent) {
    return chatClient.prompt()
        .system(systemPrompt)
        .user(userContent)
        .stream()
        .content();
}

public Flux<String> streamChat(String systemPromptTemplate, 
                               Map<String, Object> vars,
                               String userContent) {
    String rendered = new PromptTemplate(systemPromptTemplate).render(vars);
    return streamChat(rendered, userContent);
}
```

### Step 7.2: 新建 `AiStreamController`
```java
@RestController
@RequestMapping("/ai")
@Tag(name = "AiStreamController", description = "AI 流式输出 (SSE)")
@RequiredArgsConstructor
public class AiStreamController {

    private final AiAssistantService service;
    private final PromptProperties prompts;

    @PostMapping(value = "/product/qa/stream", 
                 produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> productQaStream(@Valid @RequestBody ProductQaRequest request) {
        // 复用 chatAboutProduct 业务逻辑，但用 stream
        // ...（需要重构 service 以分离同步/流式路径）
        return aiChatService.streamChat(
            prompts.productQaSystem(), 
            buildContent(request));
    }

    // 退货建议流式版较复杂（因为有 JSON 解析 + 强制校验）
    // Stage 7 仅做商品问答流式；退货建议在 Stage 8 改造时再做
}
```

### Step 7.3: 配置 Jackson 支持 SSE（默认已支持，无需配置）

## 🧪 测试细节

### 新增：`AiStreamControllerTest.java`
```java
@WebMvcTest(AiStreamController.class)
class AiStreamControllerTest {

    @Autowired MockMvc mvc;
    @MockBean AiChatService chatService;

    @Test
    void productQaStream_returnsFluxOfChunks() throws Exception {
        when(chatService.streamChat(anyString(), anyString()))
            .thenReturn(Flux.just("你好", "，", "这是", "测试"));

        MvcResult result = mvc.perform(post("/ai/product/qa/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":1,\"question\":\"hi\"}"))
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();

        // 验证 content type
        assertThat(result.getResponse().getContentType())
            .isEqualTo(MediaType.TEXT_EVENT_STREAM_VALUE);
    }

    @Test
    void productQaStream_validationError_returns400() throws Exception {
        mvc.perform(post("/ai/product/qa/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\":\"\"}"))  // 缺 productId
           .andExpect(status().isBadRequest());
    }
}
```

### 集成测试：`AiStreamWireMockIT.java`（用 WireMock 模拟 SSE 风格的流式响应）
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class AiStreamWireMockIT {

    @Autowired TestRestTemplate rest;
    @Autowired AiChatService chatService;  // 真实 bean

    @BeforeEach
    void setup() {
        // WireMock 返回 SSE 风格的 chunked response
        stubFor(post(urlEqualTo("/v1/chat/completions"))
            .willReturn(okJson("""
                data: {"choices":[{"delta":{"content":"你"}}]}
                
                data: {"choices":[{"delta":{"content":"好"}}]}
                
                data: [DONE]
                """)));
    }

    @Test
    void streamChat_emitsChunks() {
        Flux<String> flux = chatService.streamChat("system", "hi");
        List<String> chunks = flux.collectList().block();

        assertThat(chunks).containsExactly("你", "好");
    }
}
```

### 回归测试
- ✅ `mvn test -pl mall-ai -Dtest=AiStreamControllerTest`（新增）
- ✅ `mvn test -pl mall-ai -Dtest=AiStreamWireMockIT`（新增，可选）
- ✅ `mvn test -pl mall-ai -Dtest=AiAssistantServiceTest`
- ✅ `mvn test -pl mall-ai -Dtest=CorsPreflightTest`

### 端到端（curl SSE）
```bash
mvn spring-boot:run -pl mall-ai &

# 流式调用
curl -N -X POST http://localhost:8086/ai/product/qa/stream \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"question":"介绍下","productName":"iPhone"}'

# 预期输出（SSE 格式，每行 data: ...）
# data:这
# data:款
# data:手
# data:机
# ...
```

## 📊 验收标准
- [ ] 2 个新 SSE 端点可访问
- [ ] 旧端点（`/ai/product/qa`）行为不变
- [ ] SSE 响应 `Content-Type: text/event-stream`
- [ ] 流式 chunks 按 AI 输出的 token 顺序到达
- [ ] `mvn test -pl mall-ai` 全绿
- [ ] 前端 demo 能消费（SSE/EventSource）

## 🌿 Git 操作
```bash
git checkout -b feat/mall-ai-stage-7-streaming-sse
git add mall-ai-fix-task/stage-7-streaming-sse.md
git commit -m "chore(mall-ai): Stage 7 任务文档"

git add mall-ai/src/test/java/com/macro/mall/ai/controller/AiStreamControllerTest.java
git commit -m "test(mall-ai): Stage 7 SSE Controller 测试 (red)"

git add mall-ai/src/main/java/com/macro/mall/ai/controller/AiStreamController.java \
        mall-ai/src/main/java/com/macro/mall/ai/chat/AiChatService.java
git commit -m "feat(mall-ai): Stage 7 新增 SSE 流式输出端点

- AiChatService.streamChat() 返回 Flux<String>
- 新增 /ai/product/qa/stream 端点 (text/event-stream)
- 旧同步端点保持兼容
- 端到端体验：逐字打字机效果

Refs: mall-ai-fix-task/stage-7-streaming-sse.md"

git add src/test/java/
git commit -m "test(mall-ai): Stage 7 全测试通过 (green)"

git add mall-ai/README.md mall-ai-fix-task/CHANGELOG.md
git commit -m "docs(mall-ai): Stage 7 README + CHANGELOG"

git push -u origin feat/mall-ai-stage-7-streaming-sse
gh pr create --title "[mall-ai] Stage 7: 流式输出 (SSE)"
```

## ⚠️ 风险
- **风险 1**：Spring AI `ChatClient.stream().content()` 在不同小版本 API 微调
- **风险 2**：WebFlux 与 Servlet 栈混用（mall-ai 是 servlet 栈，引入 `spring-boot-starter-webflux` 会有冲突）→ 用 `SseEmitter` 而非 `Flux` 可能更安全（评估）
- **风险 3**：前端消费 SSE 需要 `EventSource` 或 `fetch` + reader；uni-app 兼容性需测试
- **风险 4**：退货运维建议的流式解析更复杂（边解析 JSON 边输出 guideQuestion）→ **建议 Stage 7 只做商品问答流式**，退货建议留到 Stage 8

## 🔙 回滚
```bash
git revert <merge-commit-of-stage-7>
# 新增的 AiStreamController 和 streamChat() 方法会随之移除
```
