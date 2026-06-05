# Stage 3: 引入 Spring AI，删除 OpenAiCompatibleClient

## 🎯 目标
- 引入 `spring-ai-starter-model-openai`，完全替代 97 行手写的 `OpenAiCompatibleClient`
- 删除 `client/AiClient.java` / `ChatMessage.java` / `OpenAiCompatibleClient.java`
- 业务代码调用方式：`chatClient.prompt().system(...).user(...).call().content()`

## ⚠️ 前置条件（必须先验证）
1. **Spring AI 版本**：建议 `1.0.0-M6` 或更新 GA。需在根 `pom.xml` 加：
   ```xml
   <properties>
       <spring-ai.version>1.0.0-M6</spring-ai.version>
   </properties>
   <repositories>
       <repository>
           <id>spring-milestones</id>
           <name>Spring Milestones</name>
           <url>https://repo.spring.io/milestone</url>
       </repository>
   </repositories>
   ```
2. **DeepSeek 兼容**：Spring AI OpenAI client 与 DeepSeek 协议 99% 兼容；`max_tokens` 参数名确认（DeepSeek 接受 `max_tokens` 而非 `max_completion_tokens`）。

## 📂 涉及文件

### 新增
- `mall-ai/src/main/java/com/macro/mall/ai/chat/AiChatService.java`（替代 `AiClient` 接口）
- `mall-ai/src/test/java/com/macro/mall/ai/chat/AiChatServiceTest.java`
- `mall-ai/src/test/java/com/macro/mall/ai/wiremock/OpenAiWireMockIT.java`（集成测试）

### 修改
- `mall-ai/pom.xml`（加 `spring-ai-starter-model-openai`）
- 根 `pom.xml`（加 `spring-ai.version` + spring-milestones 仓库）
- `mall-ai/src/main/java/com/macro/mall/ai/service/impl/AiAssistantServiceImpl.java`
- `mall-ai/src/main/resources/application.yml`（`ai.client.*` → `spring.ai.openai.*`）
- `mall-ai/src/main/resources/application-dev.yml`

### 删除
- `mall-ai/src/main/java/com/macro/mall/ai/client/AiClient.java`
- `mall-ai/src/main/java/com/macro/mall/ai/client/ChatMessage.java`
- `mall-ai/src/main/java/com/macro/mall/ai/client/OpenAiCompatibleClient.java`
- `mall-ai/src/main/java/com/macro/mall/ai/config/AiClientConfig.java`
- `mall-ai/src/main/java/com/macro/mall/ai/config/RestTemplateConfig.java`

## 🔨 实施步骤

### Step 3.1: 根 pom.xml 加 Spring AI BOM
```xml
<properties>
    <spring-ai.version>1.0.0-M6</spring-ai.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>${spring-ai.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<repositories>
    <repository>
        <id>spring-milestones</id>
        <name>Spring Milestones</name>
        <url>https://repo.spring.io/milestone</url>
    </repository>
</repositories>
```

### Step 3.2: mall-ai/pom.xml 加依赖
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

### Step 3.3: application.yml 改造
```yaml
spring:
  ai:
    openai:
      api-key: ${AI_API_KEY:your-api-key-here}
      base-url: https://api.deepseek.com
      chat:
        options:
          model: deepseek-chat
          temperature: 0.7
          max-tokens: 1024
```

### Step 3.4: 新建 `AiChatService`
```java
@Service
public class AiChatService {

    private final ChatClient chatClient;

    public AiChatService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String chat(String systemPrompt, String userContent) {
        return chatClient.prompt()
            .system(systemPrompt)
            .user(userContent)
            .call()
            .content();
    }

    public String chat(String systemPromptTemplate, Map<String, Object> vars,
                       String userContent) {
        String rendered = new PromptTemplate(systemPromptTemplate).render(vars);
        return chat(rendered, userContent);
    }
}
```

### Step 3.5: `AiAssistantServiceImpl` 改造
```java
@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private final AiChatService aiChat;        // 替换 AiClient
    private final ReturnReasonService returnReasonService;
    private final PromptProperties prompts;

    @Override
    public AiResponse chatAboutProduct(ProductQaRequest request) {
        String content = buildContent(request);
        String reply = aiChat.chat(prompts.productQaSystem(), content);
        return new AiResponse(reply);
    }

    // suggestReturn 暂未变（Stage 4 再改 BeanOutputConverter）
}
```

## 🧪 测试细节

### 新增单元测试：`AiChatServiceTest.java`
```java
@ExtendWith(MockitoExtension.class)
class AiChatServiceTest {

    @Test
    void chat_rendersTemplateAndCallsClient() {
        // 用 mock ChatClient 验证 prompt template 渲染 + ChatClient 调用链
        ChatClient mockClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);

        when(mockClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("AI 回复");

        AiChatService service = new AiChatService(builder -> mockClient);
        String result = service.chat(
            "Hello {name}",
            Map.of("name", "World"),
            "user msg"
        );

        assertThat(result).isEqualTo("AI 回复");
        verify(requestSpec).system("Hello World");
        verify(requestSpec).user("user msg");
    }
}
```

### 新增集成测试：`OpenAiWireMockIT.java`（用 WireMock 拦截真实 HTTP）
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class OpenAiWireMockIT {

    @Autowired TestRestTemplate rest;
    @MockBean AiAssistantService service;  // ★ 关键：Mock service 避免真调 LLM

    @BeforeEach
    void setup() {
        stubFor(post(urlEqualTo("/v1/chat/completions"))
            .willReturn(okJson("""
                {
                  "id": "chat-1",
                  "choices": [{
                    "message": {"role": "assistant", "content": "测试回复"},
                    "index": 0
                  }],
                  "usage": {"total_tokens": 10}
                }
                """)));
    }

    @Test
    void productQa_callAiAndReturn() {
        var response = rest.postForEntity("/ai/product/qa",
            new ProductQaRequest(1L, "测试", "iPhone", null, null, null, null),
            CommonResult.class);

        assertThat(response.getBody().getCode()).isEqualTo(200);
        // 不应打到 WireMock（service 被 mock）
        verify(0, postRequestedFor(urlEqualTo("/v1/chat/completions")));
    }

    @Test
    void chatClient_actuallyCallsOpenAi_whenServiceNotMocked() {
        // 这是更深入的集成测试：直接测 AiChatService 真实打 WireMock
        ChatClient client = ChatClient.builder(/* wiremock base url */).build();
        AiChatService svc = new AiChatService(b -> client);

        String result = svc.chat("You are helpful", "Hi");

        assertThat(result).isEqualTo("测试回复");
        verify(postRequestedFor(urlEqualTo("/v1/chat/completions"))
            .withHeader("Authorization", matching("Bearer .+")));
    }
}
```

### 回归测试
- ✅ `AiAssistantServiceTest`（用 mock `AiChatService` 替换原 `AiClient` mock）
- ✅ `AiAssistantControllerTest`
- ✅ `CorsPreflightTest`

### 端到端（仅本地手动，CI 跳过）
```bash
# 真实 DeepSeek 调用
export AI_API_KEY=sk-real-key
mvn spring-boot:run -pl mall-ai

curl -s -X POST http://localhost:8086/ai/product/qa \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"question":"这款手机拍照怎么样？","productName":"iPhone 15 Pro","productPrice":"7999","productBrand":"Apple"}' \
  | jq .data.reply
# 预期: 100 字以内专业导购回答
```

## 📊 验收标准
- [ ] `mvn install -pl mall-ai -am -DskipTests` 编译通过
- [ ] 0 个 `RestTemplate` / `OpenAiCompatibleClient` 引用
- [ ] `ChatClient` 注入正常
- [ ] `mvn test -pl mall-ai` 全绿（含 WireMock 集成测试）
- [ ] 真实 API 端到端测试通过（人工）
- [ ] `git diff --stat` 净减少 ≥ 80 行（client 目录整体删除）

## 🌿 Git 操作
```bash
git checkout -b feat/mall-ai-stage-3-spring-ai
git add mall-ai-fix-task/stage-3-spring-ai.md pom.xml
git commit -m "chore(mall-ai): Stage 3 任务文档 + 根 POM 加 spring-ai BOM"

git add mall-ai/pom.xml mall-ai/src/test/java/com/macro/mall/ai/chat/
git commit -m "test(mall-ai): Stage 3 AiChatService + WireMock 集成测试 (red)"

git add mall-ai/src/main/
git commit -m "feat(mall-ai): Stage 3 引入 Spring AI 替代手写 OpenAI 客户端

- 加 spring-ai-starter-model-openai 依赖
- 新建 AiChatService 封装 ChatClient
- application.yml 用 spring.ai.openai.* 配置
- 删除 client/{AiClient,ChatMessage,OpenAiCompatibleClient}.java
- 删除 AiClientConfig / RestTemplateConfig
- 行数：~270 → ~30 (client 模块 -89%)

Refs: mall-ai-fix-task/stage-3-spring-ai.md"

git add src/test/java/
git commit -m "test(mall-ai): Stage 3 全测试通过 (green)"

git add mall-ai/README.md mall-ai-fix-task/CHANGELOG.md
git commit -m "docs(mall-ai): Stage 3 README + CHANGELOG"

git push -u origin feat/mall-ai-stage-3-spring-ai
gh pr create --title "[mall-ai] Stage 3: 引入 Spring AI"
```

## ⚠️ 风险
- **风险 1**：Spring AI 1.0.0-M 阶段 API 可能在 GA 时微调 → 锁定 GA 版本后做最后对齐
- **风险 2**：DeepSeek `max_tokens` vs `max_completion_tokens` 差异 → 抓包验证
- **风险 3**：Spring AI 仓库（spring-milestones）CI 网络可能不稳 → 备份 Nexus/阿里云镜像
- **风险 4**：WireMock 与 Spring Boot 3.5 / Spring Cloud 兼容性需用 `wiremock-standalone` 3.x

## 🔙 回滚
```bash
git revert <merge-commit-of-stage-3>
# Spring AI 依赖会随之移除
```
