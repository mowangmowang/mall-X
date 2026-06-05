# Stage 6: 移除 MyBatis 依赖，return reasons 走 HTTP 调 mall-portal

## 🎯 目标
- 让 mall-ai 真正"轻量"——不连 DB、不带 mybatis-spring、不带 druid 配置
- `ReturnReasonService` 改用 OpenFeign 调 `mall-portal` 的 `/returnReason/list`
- 从 `pom.xml` 删除：`mall-mbg` / `mybatis-spring-boot-starter` / `druid` / `mysql-connector`

## ⚠️ 前置条件
- Stage 3、4 完成
- `mall-portal` 已暴露 `/returnReason/list` REST 接口（mall-portal 的 `OmsOrderReturnReasonController` 默认存在）
- 确认 `mall-portal` 已加入 Spring Cloud / Nacos 注册中心，或使用 `URL` 直连

## 📂 涉及文件

### 新增
- `mall-ai/src/main/java/com/macro/mall/ai/feign/ReturnReasonClient.java`
- `mall-ai/src/main/java/com/macro/mall/ai/feign/ReturnReasonFeignConfig.java`
- `mall-ai/src/test/java/com/macro/mall/ai/feign/ReturnReasonClientIT.java`（WireMock 集成测试）
- `mall-ai/src/test/java/com/macro/mall/ai/service/ReturnReasonServiceHttpTest.java`

### 修改
- `mall-ai/pom.xml`（删 4 个依赖，加 OpenFeign）
- 根 `pom.xml`（如未引入 spring-cloud，加 BOM）
- `mall-ai/src/main/java/com/macro/mall/ai/MallAiApplication.java`（加 `@EnableFeignClients`）
- `mall-ai/src/main/java/com/macro/mall/ai/service/ReturnReasonService.java`（从 Feign Client 拿数据）
- `mall-ai/src/main/java/com/macro/mall/ai/config/MyBatisConfig.java`（**删除**）
- `mall-ai/src/main/resources/application-dev.yml`（删 `spring.datasource` / `mybatis.*`）

### 删除
- `mall-ai/src/main/java/com/macro/mall/ai/config/MyBatisConfig.java`
- `mall-ai/src/main/java/com/macro/mall/ai/service/ReturnReasonService.java`（**重写**，新版本只调 Feign）
- `mall-ai/src/main/java/com/macro/mall/ai/exception/...`（如 MyBatis 相关异常处理，可保留）

## 🔨 实施步骤

### Step 6.1: mall-ai/pom.xml 依赖调整
```xml
<!-- 删除 -->
<dependency>
    <groupId>com.macro.mall</groupId>
    <artifactId>mall-mbg</artifactId>
</dependency>

<!-- 新增：OpenFeign -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

> **注意**：mall-portal 是否也用 MyBatis 不受影响，本次只清理 mall-ai 的依赖。

### Step 6.2: 新建 Feign Client
```java
@FeignClient(name = "mall-portal", path = "/returnReason", 
             fallbackFactory = ReturnReasonFallbackFactory.class)
public interface ReturnReasonClient {
    @GetMapping("/list")
    CommonResult<List<OmsOrderReturnReason>> list();
}
```

**Fallback Factory**（服务降级，避免 mall-portal 宕机时 mall-ai 不可用）：
```java
@Component
public class ReturnReasonFallbackFactory implements FallbackFactory<ReturnReasonClient> {
    @Override
    public ReturnReasonClient create(Throwable cause) {
        return new ReturnReasonClient() {
            @Override
            public CommonResult<List<OmsOrderReturnReason>> list() {
                log.warn("Feign call to mall-portal failed, using empty list. cause={}", cause.getMessage());
                return CommonResult.success(List.of());
            }
        };
    }
}
```

### Step 6.3: 启动类加 `@EnableFeignClients`
```java
@SpringBootApplication(scanBasePackages = "com.macro.mall")
@EnableFeignClients(basePackages = "com.macro.mall.ai.feign")
public class MallAiApplication { ... }
```

### Step 6.4: 重写 `ReturnReasonService`
```java
@Service
@RequiredArgsConstructor
public class ReturnReasonService {

    private final ReturnReasonClient client;
    private final PromptProperties prompts;

    public List<String> getEnabledReturnReasons() {
        try {
            CommonResult<List<OmsOrderReturnReason>> result = client.list();
            if (result == null || result.getData() == null) {
                return getDefaultReasons();
            }
            return result.getData().stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 1)
                .sorted(Comparator.comparingInt(
                    OmsOrderReturnReason::getSort == null ? 0 : OmsOrderReturnReason::getSort).reversed())
                .map(OmsOrderReturnReason::getName)
                .filter(Objects::nonNull)
                .toList();
        } catch (Exception e) {
            log.warn("远程获取退货原因失败，使用默认列表. err={}", e.getMessage());
            return getDefaultReasons();
        }
    }

    private List<String> getDefaultReasons() {
        return List.of(prompts.returnReasonDefault(), "7天无理由退货", "其他");
    }
}
```

### Step 6.5: 删除 `MyBatisConfig.java`
```bash
git rm mall-ai/src/main/java/com/macro/mall/ai/config/MyBatisConfig.java
```

### Step 6.6: application-dev.yml 清理
```yaml
# 删除
spring:
  datasource:
    url: ...
    username: ...
    password: ...
    druid: ...
mybatis:
  mapper-locations: ...

# 新增（如使用 Feign + 服务发现）
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
# 或（直连模式）
feign:
  client:
    config:
      mall-portal:
        url: http://localhost:8085
```

## 🧪 测试细节

### 新增集成测试：`ReturnReasonClientIT.java`（WireMock 模拟 mall-portal）
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class ReturnReasonClientIT {

    @Autowired ReturnReasonClient client;

    @BeforeEach
    void setup() {
        stubFor(get(urlEqualTo("/returnReason/list"))
            .willReturn(okJson("""
                {
                  "code": 200,
                  "message": "操作成功",
                  "data": [
                    {"id":1,"name":"质量问题","status":1,"sort":100},
                    {"id":2,"name":"商品损坏","status":1,"sort":90},
                    {"id":3,"name":"已禁用","status":0,"sort":80}
                  ]
                }
                """)));
    }

    @Test
    void list_returnsEnabledReasonsOnly() {
        CommonResult<List<OmsOrderReturnReason>> result = client.list();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(3);
        // 业务层在 ReturnReasonService 里过滤 status=1
    }
}
```

### 新增单元测试：`ReturnReasonServiceHttpTest.java`
```java
@ExtendWith(MockitoExtension.class)
class ReturnReasonServiceHttpTest {

    @Mock ReturnReasonClient client;
    @Mock PromptProperties prompts;
    @InjectMocks ReturnReasonService service;

    @Test
    void getEnabledReturnReasons_filtersAndSorts() {
        when(client.list()).thenReturn(CommonResult.success(List.of(
            new OmsOrderReturnReason() {{ setName("商品损坏"); setStatus(1); setSort(90); }},
            new OmsOrderReturnReason() {{ setName("质量问题"); setStatus(1); setSort(100); }},
            new OmsOrderReturnReason() {{ setName("已禁用"); setStatus(0); setSort(80); }}
        )));
        when(prompts.returnReasonDefault()).thenReturn("质量问题");

        List<String> result = service.getEnabledReturnReasons();

        assertThat(result).containsExactly("质量问题", "商品损坏");  // 已过滤 + 已排序
    }

    @Test
    void getEnabledReturnReasons_remoteFails_usesDefaults() {
        when(client.list()).thenThrow(new RuntimeException("Connection refused"));
        when(prompts.returnReasonDefault()).thenReturn("质量问题");

        List<String> result = service.getEnabledReturnReasons();

        assertThat(result).contains("质量问题", "7天无理由退货", "其他");
    }

    @Test
    void getEnabledReturnReasons_nullData_usesDefaults() {
        when(client.list()).thenReturn(CommonResult.success(null));
        when(prompts.returnReasonDefault()).thenReturn("质量问题");

        List<String> result = service.getEnabledReturnReasons();

        assertThat(result).contains("质量问题");
    }
}
```

### 改写：`AiAssistantServiceTest.java`（用 mock `ReturnReasonService` 替代）
- 已用 mock，不需大改

### 回归测试
- ✅ `mvn test -pl mall-ai -Dtest=ReturnReasonServiceHttpTest`（新增）
- ✅ `mvn test -pl mall-ai -Dtest=ReturnReasonClientIT`（新增）
- ✅ `mvn test -pl mall-ai -Dtest=AiAssistantServiceTest`
- ✅ `mvn test -pl mall-ai -Dtest=AiAssistantControllerTest`
- ✅ `mvn test -pl mall-ai -Dtest=CorsPreflightTest`

### 端到端
```bash
# 启动 mall-portal + mall-ai
mvn spring-boot:run -pl mall-portal &
mvn spring-boot:run -pl mall-ai &

# 验证 mall-ai 能调通 mall-portal
curl -s -X POST http://localhost:8086/ai/return/suggest \
  -H "Content-Type: application/json" \
  -d '{"issue":"屏幕有裂痕","productName":"iPhone","step":3}' | jq .data.suggestedReason
# 预期: 返回 mall-portal 启用的退货原因之一
```

## 📊 验收标准
- [ ] `mall-ai/pom.xml` 净减 4 个依赖（mall-mbg / mybatis-spring-boot-starter / druid / mysql-connector）
- [ ] `mvn dependency:tree -pl mall-ai | grep -E "mybatis|druid|mysql"` 输出为空
- [ ] `MyBatisConfig.java` 已删除
- [ ] `application-dev.yml` 0 个 `spring.datasource` / `mybatis.*` 配置
- [ ] 端到端：mall-ai 能从 mall-portal 拉到退货原因
- [ ] Feign fallback 触发时降级到默认列表
- [ ] `mvn test -pl mall-ai` 全绿

## 🌿 Git 操作
```bash
git checkout -b refactor/mall-ai-stage-6-remove-mybatis
git add mall-ai-fix-task/stage-6-remove-mybatis.md
git commit -m "chore(mall-ai): Stage 6 任务文档"

git add mall-ai/src/test/java/com/macro/mall/ai/feign/ \
        mall-ai/src/test/java/com/macro/mall/ai/service/ReturnReasonServiceHttpTest.java
git commit -m "test(mall-ai): Stage 6 ReturnReasonService + Feign Client 测试 (red)"

git add mall-ai/pom.xml \
        mall-ai/src/main/java/com/macro/mall/ai/feign/ \
        mall-ai/src/main/java/com/macro/mall/ai/service/ReturnReasonService.java \
        mall-ai/src/main/java/com/macro/mall/ai/MallAiApplication.java
git rm mall-ai/src/main/java/com/macro/mall/ai/config/MyBatisConfig.java
git add mall-ai/src/main/resources/application-dev.yml
git commit -m "refactor(mall-ai): Stage 6 移除 MyBatis 依赖，ReturnReason 改用 OpenFeign

- pom.xml: 删 mall-mbg/mybatis/druid/mysql，加 spring-cloud-starter-openfeign
- 新建 ReturnReasonClient Feign 接口 + FallbackFactory
- 重写 ReturnReasonService 走 HTTP 调用
- 删除 MyBatisConfig
- 清理 application-dev.yml 的 datasource/mybatis 配置
- 净减 4 个依赖

Refs: mall-ai-fix-task/stage-6-remove-mybatis.md"

git add src/test/java/
git commit -m "test(mall-ai): Stage 6 全测试通过 (green)"

git add mall-ai/README.md mall-ai-fix-task/CHANGELOG.md
git commit -m "docs(mall-ai): Stage 6 README + CHANGELOG"

git push -u origin refactor/mall-ai-stage-6-remove-mybatis
gh pr create --title "[mall-ai] Stage 6: 移除 MyBatis 依赖"
```

## ⚠️ 风险
- **风险 1**：mall-portal `/returnReason/list` 需要鉴权放行（JWT）→ 确认 mall-portal 的 SecurityConfig 对该路径放行，或 mall-ai 走 OpenFeign 内部调用（不带 JWT）
- **风险 2**：服务发现（Nacos）vs 直连 URL → 评估生产环境用哪种
- **风险 3**：mall-portal 宕机时 mall-ai 启动可能因为 `@FeignClient` 注册失败 → 加 `spring.cloud.discovery.enabled=false` 或配 fallback
- **风险 4**：`OmsOrderReturnReason` 仍来自 `mall-mbg`（已删除）→ 需要把这个 model 类**复制到 mall-ai** 或定义一个简化版 DTO

## 🔙 回滚
```bash
git revert <merge-commit-of-stage-6>
# pom.xml 依赖会恢复，MyBatisConfig 会回来
```
