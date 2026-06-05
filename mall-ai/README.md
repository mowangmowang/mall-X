# mall-ai — AI 购物助手微服务

Spring Boot 3.5 / Java 17 / Spring AI 1.0 实现的 AI 购物助手。提供商品问答与退货建议两个核心能力，部署在 `:8086`。

## 端点一览

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/ai/product/qa` | 商品问答（同步） |
| POST | `/ai/product/qa/stream` | 商品问答（**SSE 流式**，逐 token 推送） |
| POST | `/ai/return/suggest` | 退货建议（3 轮引导） |
| GET  | `/swagger-ui/index.html` | OpenAPI 3 文档 |

## 技术栈

- Spring Boot 3.5.14 + Java 17
- Spring AI 1.0（`spring-ai-starter-model-openai`，兼容 DeepSeek / OpenAI / SiliconFlow 等 OpenAI 协议 LLM）
- Spring Cloud OpenFeign（远程调 mall-portal 拿退货原因列表，替代 MyBatis）
- springdoc-openapi 2.6（Swagger 3）
- 不引 webflux（SSE 用 servlet 原生 `SseEmitter`）

## 模块依赖

```
mall-ai
├── mall-common          (CommonResult 统一响应)
├── mall-common-cors     (CORS 策略源)
├── spring-ai-starter-model-openai   (Spring AI ChatClient)
├── spring-cloud-starter-openfeign   (远程调 mall-portal)
└── springdoc-openapi-starter-webmvc-ui
```

**不依赖**：mall-mbg / mybatis / druid / mysql-connector（Stage 6 已彻底脱离 DB）。

## 本地启动

### 1. 配 API Key

编辑 `src/main/resources/application-local.yml`（**已被 .gitignore 排除**）：

```yaml
spring:
  ai:
    openai:
      api-key: sk-你的-deepseek-key
```

> **Spring AI 只认 `spring.ai.openai.api-key` 一个 key**。`ai.client.*` 等旧前缀在 Stage 3 后已废弃，写在 yml 也不会被读。

### 2. 启动

```bash
mvn spring-boot:run -pl mall-ai -am
```

服务跑在 `http://localhost:8086`。第一次启动会调 DeepSeek 验证 key 是否有效。

### 3. 验证

```bash
# 同步问答
curl -X POST http://localhost:8086/ai/product/qa \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"question":"适合送人吗？","productName":"Redmi Note 13","productBrand":"小米","productPrice":"1999"}'

# 流式问答（SSE）
curl -N -X POST http://localhost:8086/ai/product/qa/stream \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{"productId":1,"question":"适合送人吗？","productName":"Redmi Note 13"}'

# 退货建议
curl -X POST http://localhost:8086/ai/return/suggest \
  -H "Content-Type: application/json" \
  -d '{"issue":"屏幕有裂痕","step":1,"sessionId":"u-001"}'
```

## 切换 LLM 厂商

改 `application.yml` 即可，**无需改代码**：

```yaml
spring:
  ai:
    openai:
      api-key: ${DEEPSEEK_API_KEY:your-key}
      base-url: https://api.deepseek.com      # ← 改这里
      chat:
        options:
          model: deepseek-chat                # ← 改这里
          temperature: 0.7
          max-tokens: 1024
```

| 厂商 | base-url | model 示例 |
|---|---|---|
| DeepSeek | `https://api.deepseek.com` | `deepseek-chat` |
| SiliconFlow | `https://api.siliconflow.cn/v1` | `Qwen/Qwen2.5-7B-Instruct` |
| OpenAI | `https://api.openai.com/v1` | `gpt-4o-mini` |

所有 OpenAI Chat Completions 协议兼容的 LLM 都可直接接入。

## 架构

```
┌──────────────┐    HTTP    ┌─────────────────┐
│  前端 (uni-app) │ ────────→ │ AiAssistantController │
└──────────────┘            │  /ai/product/qa       │
                            └─────────┬───────────────┘
                                      │
                            ┌─────────▼───────────────┐
                            │ AiStreamController        │
                            │  /ai/product/qa/stream    │
                            │  (SseEmitter, servlet 栈) │
                            └─────────┬───────────────┘
                                      │
                            ┌─────────▼───────────────┐
                            │ AiAssistantServiceImpl    │
                            │  ├─ ChatClient.chat()     │
                            │  ├─ ChatClient.entity()   │ ← BeanOutputConverter
                            │  └─ ChatClient.stream()   │ → SseEmitter
                            └─────────┬───────────────┘
                                      │
                            ┌─────────▼───────────────┐
                            │ ChatClient (Spring AI)    │
                            │  + InputSanitizationAdvisor│ ← Stage 5
                            │  + OpenFeign → mall-portal │ ← Stage 6
                            └───────────────────────────┘
```

**Stage 关键点：**

- **Stage 1**：5 个 DTO 全面 Java 17 record 化，构造器注入替 @Autowired
- **Stage 2**：100+ 行硬编码 Prompt 外置到 `application.yml` 的 `ai.prompts.*`
- **Stage 3**：删 97 行手写 `OpenAiCompatibleClient`，改用 Spring AI `ChatClient`
- **Stage 4**：删 90 行手写 JSON 解析，改用 `BeanOutputConverter<T>` 自动 schema 注入 + 反序列化 record
- **Stage 5**：删 146 行 `InputSanitizer` 工具类，改用 Spring AI `CallAdvisor` 拦截所有 ChatClient 调用
- **Stage 6**：删 mall-mbg / mybatis / druid / mysql-connector 4 个重依赖，`ReturnReasonService` 改用 OpenFeign 远程调 mall-portal
- **Stage 7**：新增 SSE 流式输出端点 `/ai/product/qa/stream`，用 servlet 原生 `SseEmitter` 避免 webflux 容器替换

## 业务流：3 轮引导退货建议

```
用户描述问题 ──→ step=1 ──→ AI: "请问具体什么故障？"     (finished=false)
用户回答     ──→ step=2 ──→ AI: "何时出现的？"           (finished=false)
用户回答     ──→ step=3 ──→ AI: 确认 + 给出退货原因/描述  (finished=true)
                                  ↓
                          后端 enforceStep3Defaults() 兜底
                          前端自动填表 + 关闭 AI 弹窗
```

详见 `application.yml` 的 `ai.prompts.return-suggestion-system` 提示词工程。

## 配置

`application.yml` 是基线，profile-specific 文件按以下顺序 merge（后者覆盖前者）：

```
application.yml
   ↓
application-dev.yml        (本地默认)
   ↓
application-local.yml      (个人 key，已 gitignore)
```

### 关键配置项

| 配置 | 含义 |
|---|---|
| `spring.ai.openai.api-key` | LLM API Key（仅此一个入口） |
| `spring.ai.openai.base-url` | LLM API base URL |
| `spring.ai.openai.chat.options.model` | 模型名 |
| `ai.prompts.*` | 业务 Prompt（运营可独立改） |
| `ai.security.sanitization.*` | 输入清理策略（max-length / strip-control-chars / detect-prompt-injection） |
| `mall.security.cors.*` | CORS 策略（由 mall-common-cors 读取） |
| `spring.cloud.openfeign.client.config.mall-portal.url` | mall-portal 直连地址 |

## 源码结构

```
src/main/java/com/macro/mall/ai/
├── MallAiApplication.java            @SpringBootApplication + @EnableFeignClients
├── chat/
│   ├── AiChatService.java            封装 ChatClient：chat / chatEntity / streamChat
│   └── ChatClientConfig.java         ChatClient @Bean（含 InputSanitizationAdvisor）
├── config/
│   ├── CorsFilterRegistration.java   CORS 过滤器（FilterRegistrationBean, HIGHEST_PRECEDENCE）
│   ├── PromptProperties.java         @ConfigurationProperties("ai.prompts") record
│   └── SwaggerConfig.java            OpenAPI Bean
├── controller/
│   ├── AiAssistantController.java    POST /ai/product/qa + /ai/return/suggest
│   └── AiStreamController.java       POST /ai/product/qa/stream (SseEmitter)
├── domain/                           5 个 record DTO
├── exception/                        AiApiException + AiServiceException
├── feign/
│   ├── ReturnReasonClient.java       @FeignClient(mall-portal, /returnReason)
│   └── ReturnReasonFallbackFactory.java
├── security/
│   ├── InputSanitizationAdvisor.java Spring AI CallAdvisor（Stage 5）
│   └── SanitizationProperties.java   @ConfigurationProperties("ai.security.sanitization")
└── service/
    ├── AiAssistantService.java       接口
    ├── ReturnReasonService.java      OpenFeign + yml 兜底
    └── impl/
        └── AiAssistantServiceImpl.java
```

## 测试

```bash
# 全量
mvn test -pl mall-ai -DskipTests=false

# 跑某个测试类
mvn test -pl mall-ai -DskipTests=false -Dtest=AiChatServiceTest
```

## 已知约束

1. `mall-portal` 当前未暴露 `/returnReason/list` 端点 → `ReturnReasonService` 自动 fallback 到 yml 默认列表
2. SSE 超时 60s（`AiChatService.SSE_TIMEOUT_MS`），长文输出可能触发超时
3. `application-local.yml` 在 git 仓库里**不会**出现，**但** commit 前请用 `git status` 确认 `.gitignore` 生效
