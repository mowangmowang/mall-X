# Mall-AI 现代化重构变更日志

> 每个 Stage 完成后追加一条。格式：`<日期> - <类型> - <Stage 编号>: <一句话变更摘要>`

---

## 2026-06-05 - feat - Stage 7: 流式输出 (SSE)

**PR**: 待创建
**分支**: `feat/mall-ai-stage-7-streaming-sse`
**Commit 数**: 3 (test red / refactor green / docs)
**行数变化**: +124 / -96 (净增 28 行)
**验证**:
- ✅ `mvn test -pl mall-ai -DskipTests=false` 全绿 41/41 (新增 2 个 AiStreamControllerTest)
- ✅ curl SSE 缺 productId -> HTTP 400
- ✅ curl SSE 正常请求 -> Spring AI 调到 DeepSeek (401 with fake key, 证明链路通)

**变更摘要**:
- `AiChatService`: 新增 `streamChat()` 返回 `Flux<String>` (逐 token 流)
- `AiAssistantService`: 新增 `streamChatAboutProduct()` 接口方法
- `AiAssistantServiceImpl`: 实现流式方法 + 提取 `buildProductQaContent()` 共用工具
- 新建 `controller/AiStreamController`: SSE 端点 `POST /ai/product/qa/stream`
  - `produces = text/event-stream`
  - 旧同步端点 100% 向后兼容

**端到端实际行为**:
```
Test 1 (校验失败): POST /ai/product/qa/stream 缺 productId
-> HTTP 400 (Spring @Valid 触发)

Test 3 (正常请求): POST /ai/product/qa/stream
-> Spring AI 调 DeepSeek 流式 API
-> DeepSeek 返回 401 (fake key)
-> 响应 500 + WebClientResponseException
-> 证明 SSE 链路通 (需要真实 API Key 才能拿到 token 流)
```

**风险与回滚**:
- 风险：Spring AI 1.x `stream().content()` API 微调风险
- 风险：前端用 `EventSource` 不支持 POST body，需用 `fetch` + `ReadableStream`（已记入 Controller 注释）
- 风险：WebFlux 与 servlet 栈共存（SSE 端点会触发 WebFlux 自动配置）
- 回滚：`git revert <merge-commit-of-stage-7>`

---

## 2026-06-05 - refactor - Stage 6: 移除 MyBatis 依赖，ReturnReason 改用 OpenFeign

**PR**: 待创建
**分支**: `refactor/mall-ai-stage-6-remove-mybatis`
**Commit 数**: 3 (test red / refactor green / docs)
**行数变化**: +197 / -108 (净增 89 行 — 新增 Feign 配置 + Fallback)
**验证**:
- ✅ `mvn test -pl mall-ai -DskipTests=false` 全绿 39/39 (新增 5 个 ReturnReasonServiceHttpTest)
- ✅ 0 个 MyBatis 依赖 (mvn dependency:tree | grep mybatis -> 空)
- ✅ 0 个 Druid 依赖
- ✅ 0 个 mysql-connector-java 依赖

**变更摘要**:
- 根 pom.xml: `spring-cloud.version=2025.0.0` + `spring-cloud-dependencies` BOM
- mall-ai/pom.xml: 删 `mall-mbg`，加 `spring-cloud-starter-openfeign`
- 新增 `feign/ReturnReasonClient` (OpenFeign 接口)
- 新增 `feign/ReturnReasonFallbackFactory` (降级)
- 新增 `domain/ReturnReasonDto` (替代 mall-mbg 的 OmsOrderReturnReason)
- 重写 `service/ReturnReasonService` (改用 Feign)
- `MallAiApplication` 加 `@EnableFeignClients`
- 删除 `config/MyBatisConfig.java`
- `application-dev.yml`: 删 datasource/mybatis，加 feign 配置

**依赖清理**:
```
- mall-mbg (~50MB transitive: mybatis, pagehelper, druid, mysql-connector)
+ spring-cloud-starter-openfeign (~3MB)
```

**风险与回滚**:
- 风险：mall-portal 当前未暴露 `/returnReason/list` 端点 → Fallback 返回空 + 业务降级到 yml 默认列表
- 风险：服务发现 (Nacos) 暂未启用 → 当前用 URL 直连 localhost:8085
- 风险：Spring Cloud 2025.0.0 + Spring Boot 3.5.14 版本组合
- 回滚：`git revert <merge-commit-of-stage-6>`

---

## 2026-06-05 - refactor - Stage 5: InputSanitizer 收编为 Spring AI Advisor

**PR**: 待创建
**分支**: `refactor/mall-ai-stage-5-advisor-sanitizer`
**Commit 数**: 3 (test red / refactor green / docs)
**行数变化**: +286 / -221 (净增 65 行 — Advisor 模板代码 vs 删除 InputSanitizer)
**验证**:
- ✅ `mvn test -pl mall-ai -DskipTests=false` 全绿 34/34 (新增 8 个 InputSanitizationAdvisorTest)

**变更摘要**:
- 新增 `security/SanitizationProperties.java` (record + @ConfigurationProperties)
- 新增 `security/InputSanitizationAdvisor.java` (实现 Spring AI `CallAdvisor`)
- `ChatClientConfig`: 注册 Bean + `defaultAdvisors()` 注入
- `AiAssistantServiceImpl`: 删除显式 `InputSanitizer.sanitize()` 调用
- `application.yml`: 新增 `ai.security.sanitization.*`
- 删除 `util/InputSanitizer.java` (-146 行)

**安全收益**:
- 输入清理从业务层下移到 Spring AI Advisor 链，业务代码不再关心
- 所有 ChatClient 调用（`chat()` / `chatEntity()` / 未来 `stream()`）自动清洗
- Order = HIGHEST_PRECEDENCE 确保最先执行
- 危险模式检测 + 控制字符剥离 + 长度截断 一站式处理

**风险与回滚**:
- 风险：Spring AI 1.x 不同小版本 `CallAdvisor` API 微调
- 风险：自定义 `RequestAdvisor` 接口在不同 Spring AI 版本间命名不同
- 回滚：`git revert <merge-commit-of-stage-5>`

---

## 2026-06-05 - refactor - Stage 4: BeanOutputConverter 替换手写 JSON 解析

**PR**: 待创建
**分支**: `refactor/mall-ai-stage-4-bean-output-converter`
**Commit 数**: 3 (test red / refactor green / docs)
**行数变化**: +89 / -339 (净减 250 行)
**验证**:
- ✅ `mvn test -pl mall-ai -DskipTests=false` 全绿 26/26 (新增 6 个 ReturnSuggestionBeanOutputTest)

**变更摘要**:
- `AiChatService` 新增 `chatEntity()` 和 `renderAndChatEntity()` 重载，用 `BeanOutputConverter` 自动注入 JSON schema 并反序列化为 record
- `AiAssistantServiceImpl.suggestReturn` 重构：删除 90 行 `parseReturnSuggestion` 手写 JSON 解析
- 强制校验逻辑简化：`enforceStep3Defaults()` (15 行)
- Fallback 逻辑独立：`fallbackResult()` (15 行)
- 删除 hutool `JSONUtil` / `JSONObject` 依赖（业务侧）
- 删除 `AiAssistantServiceTest.java`（被 `ReturnSuggestionBeanOutputTest` 覆盖）

**风险与回滚**:
- 风险：BeanOutputConverter 对 record 的支持需要 Jackson 2.15+（Spring Boot 3.5 BOM 自带 2.18，已 OK）
- 风险：AI 返回的 JSON 字段名若与 record 字段名不一致 → 显式 `@JsonProperty` 注解（当前无此需求）
- 回滚：`git revert <merge-commit-of-stage-4>`

---

## 2026-06-05 - refactor - Stage 3: 引入 Spring AI 替代手写 OpenAI 客户端

**PR**: 待创建
**分支**: `feat/mall-ai-stage-3-spring-ai`
**Commit 数**: 3 (test red / refactor green / docs)
**行数变化**: +204 / -505 (净减 301 行, -22% vs master)
**验证**:
- ✅ `mvn test -pl mall-ai -DskipTests=false` 全绿 29/29 (新增 3 个 AiChatServiceTest)
- ✅ curl HTTP 400 校验 (Test 1, 3)
- ✅ curl HTTP 500 with deepseek 401 (Test 2 证明 Spring AI 真的打到 DeepSeek)
- ✅ App 启动 13.0s，Spring AI ChatClient Bean 注入成功

**变更摘要**:
- 根 pom.xml: 加 `spring-ai.version=1.0.0` + `spring-ai-bom` 依赖管理
- mall-ai/pom.xml: 加 `spring-ai-starter-model-openai`
- application.yml: `ai.client.*` → `spring.ai.openai.*`
- 新建 `AiChatService`：封装 `ChatClient`，提供 `chat()` / `renderAndChat()` / 模板渲染
- 新建 `ChatClientConfig`：显式 `builder.build()` 创建 `ChatClient` 单例 + 注册 `PromptProperties`
- `AiAssistantServiceImpl` 改用 `AiChatService`（97 行手写 OpenAICompatibleClient 消失）
- 删除 `client/{AiClient, ChatMessage, OpenAiCompatibleClient}.java` (3 个文件，~270 行)
- 删除 `config/AiClientConfig.java` + `config/AiClientProperties.java` (~90 行)
- `AiAssistantServiceTest` 适配新构造器（mock `AiChatService`）
- `PromptPropertiesTest` 去除 `AiClientProperties` 引用

**e2e 实际行为**:
```
Test 2 用真实 sk-test-key 调 DeepSeek -> 401 invalid_request_error
-> Spring AI 自动重试 1 次 -> 抛 NonTransientAiException
-> 返 HTTP 500 (含 deepseek error 信息)
```
证明 Spring AI ChatClient 已正确替换手写 HTTP 客户端，仅需真实 API Key 即可全链路打通。

**风险与回滚**:
- 风险：Spring AI 1.0.0 API 与未来 1.x 兼容性（小版本可能微调）
- 风险：`max_tokens` vs `max_completion_tokens`（DeepSeek 用前者，已验证兼容）
- 风险：aliyun maven 镜像可能缓存滞后，已 fallback 到 Maven Central
- 回滚：`git revert <merge-commit-of-stage-3>`

---

## 2026-06-05 - refactor - Stage 2: 配置 Record 化 + Prompt 外置

**PR**: 待创建
**分支**: `refactor/mall-ai-stage-2-config-record-prompts`
**Commit 数**: 3 (test red / refactor green / docs)
**行数变化**: +228 / -129 (净增 99 行 — yml 配置占大头)
**验证**:
- ✅ `mvn test -pl mall-ai -DskipTests=false` 全绿 28/28 (新增 4 个 PromptPropertiesTest)
- ✅ curl 端到端 2 个接口 HTTP 400 校验 + HTTP 200 真实 DeepSeek 调用
- ✅ App 启动 11.9s，无配置加载错误

**变更摘要**:
- 新增 `AiClientProperties` record（@ConfigurationProperties + @Validated）
- 新增 `PromptProperties` record
- `AiClientConfig`: 61 → 47 行，删除 12 个手写 getter/setter
- 删除 `@PostConstruct` 手动校验
- application.yml: 100+ 行硬编码 Prompt 迁到 `ai.prompts.*`
- `AiAssistantServiceImpl` 注入 `PromptProperties`
- 硬编码 fallback（"质量问题" / "硬件故障"）改读配置
- 删除 `RestTemplateConfig.java`（合并到 `AiClientConfig`）
- `buildReturnSystemPrompt` 用 `String.replace("{reasons}", ...)` 占位符渲染

**风险与回滚**:
- 风险：yml 多行字符串（`|`）缩进敏感，IDE 格式化可能破坏 Prompt
- 风险：`{reasons}` 占位符在 yml 模板中若与其他字段冲突需 escape
- 回滚：`git revert <merge-commit-of-stage-2>`

---

## 2026-06-05 - refactor - Stage 1: DTO 全面 Record 化 + 构造器注入

**PR**: 待创建
**分支**: `refactor/mall-ai-stage-1-dto-record`
**Commit 数**: 4 (chore / test red / refactor green / docs)
**行数变化**: +430 / -869 (净减 439 行, -57%)
**验证**:
- ✅ `mvn test -pl mall-ai -DskipTests=false` 全绿 24/24
- ✅ curl 端到端 2 个接口 400 校验生效
- ✅ 端到端 app 启动 10.5s，DB + AI 配置均正确加载

**变更摘要**:
- 5 个 DTO/POJO 改为 Java 17 record（AiResponse / ProductQaRequest / ReturnSuggestionRequest / ReturnSuggestionResult / ChatMessage）
- 3 个业务类改 `@RequiredArgsConstructor` 构造器注入（Controller / ServiceImpl / ReturnReasonService）
- JSON 解析改 record 构造（替代 setter 原地修改）
- 新增 2 个测试类（ProductQaRequestValidationTest + RecordImmutabilityTest）
- 改写 2 个测试类（AiAssistantServiceTest + AiAssistantControllerTest）

**风险与回滚**:
- 风险：项目 GlobalExceptionHandler 把校验异常映射为 `code=404`（HTTP 200），与 Spring 标准 400 不一致。Stage 1 不修改。
- 回滚：`git revert <merge-commit-of-stage-1>`

---

```markdown
## YYYY-MM-DD - <refactor|feat|fix> - Stage X: <阶段名>

**PR**: #<PR号>
**分支**: `<分支名>`
**Commit 数**: N
**行数变化**: +A / -B
**验证**:
- ✅ `mvn test -pl mall-ai` 全绿
- ✅ curl 端到端通过
- ✅ 覆盖率 X%

**变更摘要**:
- 改动了什么
- 删除了什么
- 新增了什么

**风险与回滚**:
- 风险点
- 回滚命令
```
