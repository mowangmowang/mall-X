# Spring Boot 2.7.5 → 3.5.14 升级执行计划

## 一、目标与范围

**目标**：将 mall_X 多模块项目从 Spring Boot 2.7.5 + Java 8 升级到 Spring Boot 3.5.14 + Java 17

**决策**：

| 项 | 选择 |
|----|------|
| 目标版本 | Spring Boot 3.5.14（最新补丁） |
| Java 版本 | 17 |
| JJWT | 升级到 0.12.6 |
| 测试策略 | MockMvc 单元测试 |
| 分支策略 | 按阶段创建 feature 分支，PR 合并后进入下一阶段 |

---

## 二、模块依赖关系

```
mall-common (基座)
    ↓
mall-mbg (代码生成)  → mall-common
    ↓
mall-security (JWT/Security)  → mall-common
    ↓
┌───────────┬────────────┬──────────┬────────┐
mall-admin  mall-portal  mall-search mall-ai
```

**升级顺序**：mall-common → mall-mbg → mall-security → mall-admin / mall-portal / mall-search / mall-ai

---

## 三、阶段总览

| 阶段 | 分支名 | 内容 | 测试重点 |
|------|--------|------|----------|
| 0 | `feature/upgrade-stage0-root-pom` | 根 pom 升级（依赖、版本、Docker镜像） | 依赖解析验证 |
| 1 | `feature/upgrade-stage1-mall-common` | mall-common（web、redis、pagehelper、logback、springdoc） | CommonResult、RedisService、WebLogAspect、GlobalExceptionHandler 单元测试 |
| 2 | `feature/upgrade-stage2-mall-mbg` | mall-mbg（生成代码验证） | Generator 重新生成 + mvn compile |
| 3 | `feature/upgrade-stage3-mall-security` | mall-security（Spring Security 6、JJWT 0.12） | JwtTokenUtil 单测 + SecurityConfig 集成测试 + JWT 过滤器测试 |
| 4 | `feature/upgrade-stage4-mall-admin` | mall-admin（Swagger迁移、JWT使用改造） | LoginController、UmsAdminController MockMvc 测试 |
| 5 | `feature/upgrade-stage5-mall-portal` | mall-portal（Swagger迁移、MongoDB配置） | HomeController、MemberController MockMvc 测试 |
| 6 | `feature/upgrade-stage6-mall-search` | mall-search（ES客户端升级、Swagger迁移） | EsProductController MockMvc 测试 |
| 7 | `feature/upgrade-stage7-mall-ai` | mall-ai（Swagger迁移） | AiAssistantController MockMvc 测试 |

---

## 四、各阶段详细执行计划

### 阶段 0：根 pom.xml 升级

**分支**：`feature/upgrade-stage0-root-pom`

#### 1. 改动清单

**Parent 版本**：

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.14</version>  <!-- 2.7.5 → 3.5.14 -->
</parent>
```

**Properties 删除项**（由 Spring Boot BOM 管理或不再需要）：

- `springfox-swagger.version`
- `swagger-models.version`
- `swagger-annotations.version`
- `jaxb-api.version`
- `spring-data-commons.version`

**Properties 修改项**：

```properties
java.version=17                                    # 1.8 → 17

mybatis.version=3.5.19                             # 3.5.10 → 3.5.19
mybatis-generator.version=1.4.2                      # 1.4.1 → 1.4.2

pagehelper-starter.version=2.1.0                    # 1.4.5 → 2.1.0
pagehelper.version=6.1.1                            # 5.3.2 → 6.1.1

druid.version=1.2.28                                # 1.2.14 → 1.2.28
hutool.version=5.8.25                               # 5.8.9 → 5.8.25
mysql-connector.version=8.4.0                       # 8.0.29 → 8.4.0
jjwt.version=0.12.6                                 # 0.9.1 → 0.12.6 ⚠️ 高风险
minio.version=8.5.9                                 # 8.4.5 → 8.5.9
logstash-logback.version=7.4                        # 7.2 → 7.4
docker.maven.plugin.version=0.43.3                   # 0.40.2 → 0.43.3

# 新增
springdoc-openapi.version=2.6.0
```

**依赖管理删除项**：

```xml
<!-- 删除 springfox 依赖管理 -->
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-boot-starter</artifactId>
    <version>${springfox-swagger.version}</version>
</dependency>
<dependency>
    <groupId>io.swagger</groupId>
    <artifactId>swagger-models</artifactId>
    <version>${swagger-models.version}</version>
</dependency>
<dependency>
    <groupId>io.swagger</groupId>
    <artifactId>swagger-annotations</artifactId>
    <version>${swagger-annotations.version}</version>
</dependency>

<!-- 删除 javax.xml.bind -->
<dependency>
    <groupId>javax.xml.bind</groupId>
    <artifactId>jaxb-api</artifactId>
    <version>${jaxb-api.version}</version>
</dependency>
```

**依赖管理新增项**：

```xml
<!-- springdoc-openapi -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>${springdoc-openapi.version}</version>
</dependency>
```

**依赖管理修改项**：

```xml
<!-- druid-spring-boot-starter → druid-spring-boot-3-starter -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-3-starter</artifactId>  <!-- 改了 artifactId -->
    <version>${druid.version}</version>
</dependency>
```

**Docker 配置**：

```xml
<from>eclipse-temurin:17-jre-alpine</from>  <!-- openjdk:8 → eclipse-temurin:17-jre-alpine -->
```

#### 2. 阶段 0 测试

```bash
# 1. 依赖解析验证（不进入具体编译）
mvn dependency:tree

# 2. 完整编译验证（预期在未升级模块上部分失败，仅验证依赖下载正常）
mvn clean install -DskipTests

# 3. 验证 Docker 配置
grep -r "eclipse-temurin" pom.xml
```

#### 3. 阶段 0 完成标准

- [ ] `mvn dependency:tree` 无 SNAPSHOT/版本冲突
- [ ] 根 POM 中所有依赖可解析
- [ ] Docker 基础镜像已更新为 `eclipse-temurin:17-jre-alpine`
- [ ] Git commit：`feat: stage0 - upgrade root pom to Spring Boot 3.5.14`

---

### 阶段 1：mall-common 升级

**分支**：`feature/upgrade-stage1-mall-common`

**前置依赖**：阶段 0 已合并

#### 1. 改动清单

**mall-common/pom.xml**：

```xml
<!-- 删除 springfox-boot-starter -->
<!-- 添加 springdoc-openapi-starter-webmvc-ui -->
<!-- 删除 spring-data-commons 显式 version -->

<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
</dependency>

<!-- 删除 -->
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-boot-starter</artifactId>
</dependency>

<!-- spring-data-commons 移除 version -->
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-commons</artifactId>
    <!-- <version>${spring-data-commons.version}</version> -->  <!-- 删除 -->
</dependency>
```

**Java 代码 - 全局 javax → jakarta 替换**（3个文件）：

| 文件 | 替换 |
|------|------|
| `common/util/RequestUtil.java` | `javax.servlet.http.HttpServletRequest` → `jakarta.servlet.http.HttpServletRequest` |
| `common/log/WebLogAspect.java` | 同上 |
| `common/exception/GlobalExceptionHandler.java` | `javax.validation.ConstraintViolation*` → `jakarta.validation.*` |

**SwaggerConfig 改造**（路径：`config/SwaggerConfig.java`）：

```java
// 旧：springfox @EnableSwagger2 配置
// 新：springdoc OpenAPI Bean

package com.macro.mall.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("mall-common API")
                .version("1.0")
                .description("通用模块API文档"));
    }
}
```

**application.yml 调整**：

- 确认 `spring.redis.*` 配置路径（Spring Boot 3.x 基本兼容）
- 如有 `spring.http.encoding.*` 配置，可能需调整

#### 2. 阶段 1 测试

**新建测试文件**：`mall-common/src/test/java/com/macro/mall/common/`

```
├── api/CommonResultTest.java
├── exception/GlobalExceptionHandlerTest.java
├── util/RequestUtilTest.java
└── config/SwaggerConfigTest.java
```

**CommonResultTest.java**：

```java
package com.macro.mall.common.api;

import com.macro.mall.common.domain.CommonResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommonResultTest {

    @Test
    void successWithData() {
        CommonResult result = CommonResult.success("data");
        assertEquals(200, result.getCode());
        assertEquals("data", result.getData());
    }

    @Test
    void successWithMessage() {
        CommonResult result = CommonResult.success("操作成功");
        assertEquals(200, result.getCode());
    }

    @Test
    void failedWithCode() {
        CommonResult result = CommonResult.failed(500, "系统错误");
        assertEquals(500, result.getCode());
        assertEquals("系统错误", result.getMessage());
    }

    @Test
    void unauthorized() {
        CommonResult result = CommonResult.unauthorized("未登录");
        assertEquals(401, result.getCode());
    }

    @Test
    void forbidden() {
        CommonResult result = CommonResult.forbidden("无权限");
        assertEquals(403, result.getCode());
    }
}
```

**GlobalExceptionHandlerTest.java**：

```java
package com.macro.mall.common.exception;

import com.macro.mall.common.api.CommonResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GlobalExceptionHandlerTest {

    @Test
    void handleApiException() {
        // given: ApiException(404, "资源不存在")
        // when: handler.handleApiException(e)
        // then: CommonResult(404, "资源不存在")
    }

    @Test
    void handleMethodArgumentNotValid() {
        // given: MethodArgumentNotValidException
        // when: handler.handleMethodArgumentNotValid(e)
        // then: CommonResult(400, 验证失败信息)
    }

    @Test
    void handleBindException() {
        // given: BindException
        // when: handler.handleBindException(e)
        // then: CommonResult(400, 绑定异常信息)
    }

    @Test
    void handleConstraintViolation() {
        // given: ConstraintViolationException
        // when: handler.handleConstraintViolation(e)
        // then: CommonResult(400, constraint violation message)
        // ⚠️ 验证 jakarta.validation.ConstraintViolation 使用正确
    }
}
```

**SwaggerConfigTest.java**：

```java
package com.macro.mall.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SwaggerConfigTest {

    @Test
    void customOpenAPIIsNotNull() {
        SwaggerConfig config = new SwaggerConfig();
        OpenAPI openAPI = config.customOpenAPI();
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("mall-common API", openAPI.getInfo().getTitle());
    }
}
```

**执行测试**：

```bash
mvn -pl mall-common test
mvn -pl mall-common spring-boot:run &
# 等待启动后：
curl http://localhost:8080/swagger-ui/index.html
curl http://localhost:8080/v3/api-docs
```

#### 3. 阶段 1 完成标准

- [ ] `mvn -pl mall-common test` 全部通过
- [ ] `mvn -pl mall-common spring-boot:run` 启动成功（无端口冲突时可指定端口）
- [ ] 访问 `/swagger-ui/index.html` 显示 API 文档（非空白）
- [ ] 访问 `/v3/api-docs` 返回有效 JSON
- [ ] 无 `javax.servlet.*` 编译错误（已全部替换为 jakarta）
- [ ] Git commit：`test: stage1 - mall-common unit tests and swagger migration`

---

### 阶段 2：mall-mbg 升级

**分支**：`feature/upgrade-stage2-mall-mbg`

**前置依赖**：阶段 1 已合并

#### 1. 改动清单

**mall-mbg/pom.xml**：

```xml
<!-- pagehelper-spring-boot-starter 版本随 parent BOM 管理，但需确认版本 -->
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper-spring-boot-starter</artifactId>
    <!-- version 由 parent BOM 管理 -->
</dependency>

<!-- druid 依赖改为 druid-spring-boot-3-starter -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-3-starter</artifactId>
    <version>${druid.version}</version>
</dependency>

<!-- mysql-connector-java → mysql-connector-j -->
<dependency>
    <groupId>com.mysql.cj</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>${mysql-connector.version}</version>
</dependency>
```

**Maven 编译验证**：

```bash
mvn -pl mall-mbg -am clean compile
```

**重新运行 MyBatis Generator**：

```bash
mvn -pl mall-mbg mybatis-generator:generate
```

**验证生成代码**：

```bash
# 确保生成代码中无 javax 引用
grep -r "javax\." mall-mbg/src/main/java/ || echo "Clean: no javax imports"
```

#### 2. 阶段 2 测试

```bash
# 1. 编译验证
mvn -pl mall-mbg -am clean compile

# 2. 重新生成代码
mvn -pl mall-mbg mybatis-generator:generate

# 3. 验证生成代码无 javax 引用
grep -r "javax\." mall-mbg/src/main/java/ mall-mbg/src/main/resources/ || echo "Clean"

# 4. 检查 mapper.xml 中 SQL 语句是否正常
grep -c "<select" mall-mbg/src/main/resources/mapper/**/*.xml
```

#### 3. 阶段 2 完成标准

- [ ] `mvn -pl mall-mbg -am clean compile` 成功
- [ ] MyBatis Generator 重新生成代码成功
- [ ] 生成代码中无 `javax.*` 引用
- [ ] mapper.xml 文件完整
- [ ] Git commit：`test: stage2 - mall-mbg generator re-run and compile`

---

### 阶段 3：mall-security 升级 ⚠️ 高风险

**分支**：`feature/upgrade-stage3-mall-security`

**前置依赖**：阶段 1、2 已合并

#### 1. 改动清单

**mall-security/pom.xml**：

```xml
<!-- jjwt 0.9.1 → 0.12.6：需要三个独立依赖 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>${jjwt.version}</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>${jjwt.version}</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>${jjwt.version}</version>
    <scope>runtime</scope>
</dependency>
```

**JwtTokenUtil 重写**（如果当前文件存在，参考以下改造）：

```java
// 旧 (0.9.1)
public String generateToken(String username) {
    Date issuedAt = new Date();
    Date expiration = new Date(issuedAt.getTime() + expirationMs);
    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(issuedAt)
        .setExpiration(expiration)
        .signWith(SignatureAlgorithm.HS512, secretKey)
        .compact();
}

public String getUsernameFromToken(String token) {
    Claims claims = Jwts.parser()
        .setSigningKey(secretKey)
        .parseClaimsJws(token)
        .getBody();
    return claims.getSubject();
}

// 新 (0.12.6)
public String generateToken(String username) {
    Date issuedAt = new Date();
    Date expiration = new Date(issuedAt.getTime() + expirationMs);
    return Jwts.builder()
        .subject(username)
        .issuedAt(issuedAt)
        .expiration(expiration)
        .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS512)
        .compact();
}

public String getUsernameFromToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
        .build()
        .parseSignedClaims(token)
        .getPayload();
    return claims.getSubject();
}
```

**Spring Security 6 适配**（`SecurityConfig.java`）：

```java
// 旧
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            .antMatchers("/**").permitAll();
    }
}

// 新
@EnableWebSecurity
@EnableMethodSecurity  // 替换 @EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(AntPathRequestMatcher.antMatcher("/**")).permitAll()
                .anyRequest().authenticated());
        return http.build();
    }
}
```

**注意**：`requestMatchers(String)` 必须使用 `AntPathRequestMatcher.antMatcher(String)` 包装

**javax → jakarta 替换**（4个文件）：

| 文件 | 替换 |
|------|------|
| `JwtAuthenticationTokenFilter.java` | `javax.servlet.*` → `jakarta.servlet.*` |
| `RestfulAccessDeniedHandler.java` | `javax.servlet.*` → `jakarta.servlet.*` |
| `RestAuthenticationEntryPoint.java` | `javax.servlet.*` → `jakarta.servlet.*` |
| `DynamicSecurityFilter.java` | `javax.servlet.*` → `jakarta.servlet.*` |
| `DynamicSecurityMetadataSource.java` | `javax.annotation.PostConstruct` → `jakarta.annotation.PostConstruct` |

#### 2. 阶段 3 测试

**新建测试文件**：`mall-security/src/test/java/com/macro/mall/security/`

```
├── util/JwtTokenUtilTest.java
├── component/JwtAuthenticationTokenFilterTest.java
└── config/SecurityConfigTest.java
```

**JwtTokenUtilTest.java**：

```java
package com.macro.mall.security.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtTokenUtilTest {

    private static final String SECRET = "testSecretKeyAtLeast512BitsLongForHS512Algorithm!";
    private static final long EXPIRATION_MS = 3600000;

    @Test
    void generateToken_shouldCreateValidJwt() {
        String token = jwtTokenUtil.generateToken("admin");
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3); // JWT format: header.payload.signature
    }

    @Test
    void getUsernameFromToken_shouldExtractSubject() {
        String token = jwtTokenUtil.generateToken("testuser");
        String username = jwtTokenUtil.getUsernameFromToken(token);
        assertEquals("testuser", username);
    }

    @Test
    void validateToken_withValidToken_shouldReturnTrue() {
        String token = jwtTokenUtil.generateToken("admin");
        assertTrue(jwtTokenUtil.validateToken(token));
    }

    @Test
    void validateToken_withExpiredToken_shouldReturnFalse() {
        // given: token with expiration in the past
        String expiredToken = ...; // 需要修改 jwtTokenUtil 产生过期 token 的测试方法
        assertFalse(jwtTokenUtil.validateToken(expiredToken));
    }

    @Test
    void validateToken_withInvalidSignature_shouldReturnFalse() {
        // given: token signed with different key
        // when: jwtTokenUtil.validateToken(tamperedToken)
        // then: false
    }

    @Test
    void refreshToken_shouldExtendExpiration() {
        // given: existing token
        // when: jwtTokenUtil.refreshToken(token)
        // then: new token with later expiration
    }
}
```

**JwtAuthenticationTokenFilterTest.java**：

```java
package com.macro.mall.security.component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

class JwtAuthenticationTokenFilterTest {

    @Test
    void doFilter_withValidAuthorizationHeader_shouldContinueChain() throws Exception {
        // given: request with valid Bearer token
        // when: filter.doFilter(request, response, chain)
        // then: chain.doFilter called
    }

    @Test
    void doFilter_withNoAuthorizationHeader_shouldContinueChain() throws Exception {
        // given: request without Authorization header
        // when: filter.doFilter(request, response, chain)
        // then: chain.doFilter called (no auth)
    }

    @Test
    void doFilter_withInvalidToken_shouldReturn401() throws Exception {
        // given: request with invalid JWT
        // when: filter.doFilter(request, response, chain)
        // then: 401 response
    }
}
```

**SecurityConfigTest.java**：

```java
package com.macro.mall.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SecurityConfigTest {

    @Autowired
    SecurityFilterChain filterChain;

    @Test
    void securityFilterChainBeanExists() {
        assertNotNull(filterChain);
    }

    @Test
    void permitSwaggerUIPath() throws Exception {
        // 验证 /swagger-ui/** 等公开路径可访问
    }
}
```

**执行测试**：

```bash
mvn -pl mall-security test
```

#### 3. 阶段 3 完成标准

- [ ] `mvn -pl mall-security test` 全部通过
- [ ] JJWT 0.12 `generateToken` → `getUsernameFromToken` 流程单测通过
- [ ] JWT 过滤器模拟测试通过（带有效Token进、失效Token 401）
- [ ] SecurityConfig 集成测试通过（公开路径可访问、受保护路径需认证）
- [ ] `mvn -pl mall-security spring-boot:run` 启动成功
- [ ] Git commit：`feat: stage3 - mall-security Spring Security 6 and JJWT 0.12 upgrade`

---

### 阶段 4：mall-admin 升级

**分支**：`feature/upgrade-stage4-mall-admin`

**前置依赖**：阶段 1、2、3 已合并

#### 1. 改动清单

**mall-admin/pom.xml**：

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
</dependency>
```

**Swagger 注解全局替换**（批量处理，12+ 文件）：

| 旧注解 | 新注解 | 示例 |
|--------|--------|------|
| `io.swagger.annotations.Api` | `io.swagger.v3.oas.annotations.tags.Tag` | `@Tag(name="商品管理")` |
| `io.swagger.annotations.ApiOperation` | `io.swagger.v3.oas.annotations.Operation` | `@Operation(summary="查询商品")` |
| `io.swagger.annotations.ApiParam` | `io.swagger.v3.oas.annotations.Parameter` | `@Parameter(name="id")` |
| `io.swagger.annotations.ApiModelProperty` | `io.swagger.v3.oas.annotations.media.Schema` | `@Schema(description="商品名称")` |

**javax → jakarta 替换**（6个文件）：

- `UmsAdminController.java`
- `OssController.java`
- `OssService.java` + `OssServiceImpl.java`
- `UmsAdminServiceImpl.java`
- `FlagValidator.java` + `FlagValidatorClass.java`
- `dto/UmsAdminLoginParam.java`、`dto/UpdateAdminPasswordParam.java`、`dto/UmsAdminParam.java`、`dto/PmsProductCategoryParam.java`、`dto/PmsBrandParam.java`

**SwaggerConfig 重写**：

```java
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("mall-admin API")
                .description("后台管理系统API文档")
                .version("1.0"))
            .components(new Components()
                .addSecuritySchemes("bearer-jwt",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

#### 2. 阶段 4 测试

**新建测试文件**：`mall-admin/src/test/java/com/macro/mall/`

```
├── controller/UmsAdminControllerTest.java
├── controller/PmsProductControllerTest.java
├── controller/OssControllerTest.java
└── service/UmsAdminServiceImplTest.java
```

**UmsAdminControllerTest.java**：

```java
package com.macro.mall.controller;

import com.macro.mall.service.UmsAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import static org.mockito.Mockito.*;

@WebMvcTest(UmsAdminController.class)
class UmsAdminControllerTest {

    @MockBean
    private UmsAdminService adminService;

    @Test
    void loginSuccess() throws Exception {
        // given: valid login param
        // when: POST /admin/login
        // then: 200 with jwt token
    }

    @Test
    void loginWithInvalidParams_shouldReturn400() throws Exception {
        // given: empty username
        // when: POST /admin/login
        // then: 400
    }

    @Test
    void getAdminInfoWithoutAuth_shouldReturn401() throws Exception {
        // when: GET /admin/info (no token)
        // then: 401
    }

    @Test
    @WithMockUser(username = "admin", roles = {"admin"})
    void getAdminInfoWithAuth_shouldReturn200() throws Exception {
        // when: GET /admin/info (with token)
        // then: 200
    }

    @Test
    void refreshToken() throws Exception {
        // given: valid refresh token
        // when: GET /admin/refresh
        // then: 200 with new token
    }
}
```

**PmsProductControllerTest.java**：

```java
package com.macro.mall.controller;

import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PmsProductController.class)
class PmsProductControllerTest {

    @Test
    void listProducts() throws Exception {
        // given: page params
        // when: GET /product/list?pageNum=1&pageSize=10
        // then: 200 with paginated result
    }

    @Test
    void createProduct() throws Exception {
        // given: product param
        // when: POST /product/create
        // then: 200
    }

    @Test
    @WithMockUser(roles = {"admin"})
    void deleteProduct_requiresAdmin() throws Exception {
        // when: DELETE /product/delete/1
        // then: 200
    }
}
```

**执行测试**：

```bash
mvn -pl mall-admin test
mvn -pl mall-admin spring-boot:run &
curl http://localhost:8080/swagger-ui/index.html
```

#### 3. 阶段 4 完成标准

- [ ] `mvn -pl mall-admin test` 全部通过
- [ ] `mvn -pl mall-admin spring-boot:run` 启动成功
- [ ] `/swagger-ui/index.html` 显示 API 文档
- [ ] 登录接口正常（POST /admin/login 返回 Token）
- [ ] 所有 Swagger 注解已替换为 io.swagger.v3.oas 标准
- [ ] Git commit：`test: stage4 - mall-admin swagger migration and MockMvc tests`

---

### 阶段 5：mall-portal 升级

**分支**：`feature/upgrade-stage5-mall-portal`

**前置依赖**：阶段 1、2、3 已合并

#### 1. 改动清单

**mall-portal/pom.xml**：

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
</dependency>
```

**javax → jakarta 替换**（2个文件）：

- `AlipayController.java`
- `UmsMemberController.java`

**Swagger 注解替换**（8+ 文件）：

- `HomeController.java`、`UmsMemberController.java`、`MemberReadHistoryController.java`
- `MemberProductCollectionController.java`、`MemberAttentionController.java`
- `OmsCartItemController.java`、`OmsPortalOrderController.java`、`PmsPortalProductController.java`
- `PmsPortalBrandController.java`、`OmsPortalOrderReturnApplyController.java`

**SwaggerConfig 重写**（路径：`portal/config/SwaggerConfig.java`）：

```java
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("mall-portal API")
                .description("用户端API文档")
                .version("1.0"));
    }
}
```

**注意**：`AlipayController` 引用了 alipay-sdk，SDK 版本 4.38.61 未发布 Java 17 兼容性说明，如启动报错需临时 mock

#### 2. 阶段 5 测试

**新建测试文件**：`mall-portal/src/test/java/com/macro/mall/portal/`

```
├── controller/UmsMemberControllerTest.java
├── controller/HomeControllerTest.java
└── controller/AlipayControllerTest.java
```

**UmsMemberControllerTest.java**：

```java
package com.macro.mall.portal.controller;

import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UmsMemberController.class)
class UmsMemberControllerTest {

    @Test
    void registerSuccess() throws Exception {
        // given: valid member register param
        // when: POST /member/register
        // then: 200
    }

    @Test
    void loginSuccess() throws Exception {
        // when: POST /member/login
        // then: 200 with token
    }

    @Test
    void getCurrentMemberWithoutAuth_shouldReturn401() throws Exception {
        // when: GET /member/current
        // then: 401
    }

    @Test
    @WithMockUser(username = "user")
    void getCurrentMemberWithAuth_shouldReturn200() throws Exception {
        // when: GET /member/current
        // then: 200
    }
}
```

**AlipayControllerTest.java**：

```java
package com.macro.mall.portal.controller;

import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlipayController.class)
class AlipayControllerTest {

    @Test
    void payOrder_shouldReturnQrCode() throws Exception {
        // given: order param
        // when: POST /order/pay
        // then: 200 with qr code (mock AlipayClient)
    }

    @Test
    void asyncNotify() throws Exception {
        // given: alipay notify params
        // when: POST /order/notify
        // then: "success"
    }
}
```

**执行测试**：

```bash
mvn -pl mall-portal test
mvn -pl mall-portal spring-boot:run &
curl http://localhost:8085/swagger-ui/index.html
```

#### 3. 阶段 5 完成标准

- [ ] `mvn -pl mall-portal test` 全部通过
- [ ] `mvn -pl mall-portal spring-boot:run` 启动成功
- [ ] `/swagger-ui/index.html` 显示 API 文档
- [ ] 会员注册/登录接口正常
- [ ] 支付接口至少调通（mock 也可）
- [ ] Git commit：`test: stage5 - mall-portal swagger migration and MockMvc tests`

---

### 阶段 6：mall-search 升级

**分支**：`feature/upgrade-stage6-mall-search`

**前置依赖**：阶段 1、2、3 已合并

#### 1. 改动清单

**mall-search/pom.xml**：

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
</dependency>

<!-- spring-boot-starter-data-elasticsearch 由 Spring Boot BOM 管理版本 -->
<!-- Spring Data Elasticsearch 2025.0.0 (对应 Spring Boot 3.5.14) -->
```

**javax → jakarta 替换**（搜索确认，可能没有）

**Swagger 注解替换**：

- `EsProductController.java`

**SwaggerConfig 重写**（`search/config/SwaggerConfig.java`）：

```java
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("mall-search API")
                .description("商品搜索API文档")
                .version("1.0"));
    }
}
```

**Elasticsearch 配置确认**：

- Spring Data Elasticsearch 5.2.x 默认使用 ES 8.x 客户端
- 确认 `application-dev.yml` 中 ES 配置地址
- 确认 ES 服务端版本与客户端兼容

#### 2. 阶段 6 测试

**新建测试文件**：`mall-search/src/test/java/com/macro/mall/search/`

```
├── controller/EsProductControllerTest.java
└── service/EsProductSearchServiceTest.java
```

**EsProductControllerTest.java**：

```java
package com.macro.mall.search.controller;

import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EsProductController.class)
class EsProductControllerTest {

    @Test
    void searchProducts() throws Exception {
        // given: keyword param
        // when: GET /search?keyword=手机
        // then: 200 with search results (mock ES service)
    }

    @Test
    void recommendProducts() throws Exception {
        // when: GET /search/recommend
        // then: 200
    }
}
```

**执行测试**：

```bash
mvn -pl mall-search test
mvn -pl mall-search spring-boot:run &
curl http://localhost:8081/swagger-ui/index.html
```

#### 3. 阶段 6 完成标准

- [ ] `mvn -pl mall-search test` 全部通过
- [ ] `mvn -pl mall-search spring-boot:run` 启动成功
- [ ] `/swagger-ui/index.html` 显示 API 文档
- [ ] 搜索接口可调通（mock ES 响应也通过）
- [ ] Git commit：`test: stage6 - mall-search swagger migration and MockMvc tests`

---

### 阶段 7：mall-ai 升级

**分支**：`feature/upgrade-stage7-mall-ai`

**前置依赖**：阶段 1、2、3 已合并

#### 1. 改动清单

**mall-ai/pom.xml**：

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
</dependency>
```

**javax → jakarta 替换**（3个文件）：

- `AiAssistantController.java`
- `AiClientConfig.java`

**Swagger 注解替换**（`AiAssistantController.java`）：

- 已有 `io.swagger.v3.oas.annotations.tags.Tag`，需检查 `@ApiOperation` → `@Operation`
- `@ApiModelProperty` → `@Schema`

**SwaggerConfig 重写**（`ai/config/SwaggerConfig.java`）：

```java
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("mall-ai API")
                .description("AI购物助手API文档")
                .version("1.0"));
    }
}
```

#### 2. 阶段 7 测试

**新建测试文件**：`mall-ai/src/test/java/com/macro/mall/ai/`

```
├── controller/AiAssistantControllerTest.java
└── service/AiServiceTest.java
```

**AiAssistantControllerTest.java**：

```java
package com.macro.mall.ai.controller;

import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AiAssistantController.class)
class AiAssistantControllerTest {

    @Test
    void productQA() throws Exception {
        // given: product qa request
        // when: POST /ai/product-qa
        // then: 200 with answer (mock AiClient)
    }

    @Test
    void returnSuggestion() throws Exception {
        // given: return suggestion request
        // when: POST /ai/return-suggestion
        // then: 200
    }
}
```

**执行测试**：

```bash
mvn -pl mall-ai test
mvn -pl mall-ai spring-boot:run &
curl http://localhost:8086/swagger-ui/index.html
```

#### 3. 阶段 7 完成标准

- [ ] `mvn -pl mall-ai test` 全部通过
- [ ] `mvn -pl mall-ai spring-boot:run` 启动成功
- [ ] `/swagger-ui/index.html` 显示 API 文档
- [ ] AI 接口可调通（mock 响应即可）
- [ ] Git commit：`test: stage7 - mall-ai swagger migration and MockMvc tests`

---

## 五、高风险项汇总

| 风险等级 | 项 | 说明 | 处理方式 |
|----------|-----|------|----------|
| 🔴 高 | JJWT 0.9.1 → 0.12.6 | API 大改，Token 生成/验证需重写 | 阶段 3 集中处理，写单测验证 |
| 🔴 高 | Spring Security 6 配置 | `WebSecurityConfigurerAdapter` 废弃，配置方式完全改变 | 阶段 3 集中处理，SecurityConfig 单测 |
| 🟡 中 | springfox → springdoc | 注解替换，SwaggerConfig 重写 | 阶段 1、4、5、6、7 分模块处理 |
| 🟡 中 | PageHelper 6.1.1 | `PageInfo` API 有变化，分页行为需测试 | 阶段 1 验证，阶段 4、5 回归测试 |
| 🟢 低 | javax → jakarta | 46处替换，IDE 批量处理 | 各阶段开始时处理，编译检查 |
| 🟢 低 | Druid artifactId 改名 | `druid-spring-boot-starter` → `druid-spring-boot-3-starter` | 阶段 0 处理 |

---

## 六、测试执行总命令

```bash
# 阶段 0
mvn clean install -DskipTests

# 阶段 1
mvn -pl mall-common test
mvn -pl mall-common spring-boot:run

# 阶段 2
mvn -pl mall-mbg -am clean compile
mvn -pl mall-mbg mybatis-generator:generate

# 阶段 3
mvn -pl mall-security test

# 阶段 4
mvn -pl mall-admin test
mvn -pl mall-admin spring-boot:run

# 阶段 5
mvn -pl mall-portal test
mvn -pl mall-portal spring-boot:run

# 阶段 6
mvn -pl mall-search test
mvn -pl mall-search spring-boot:run

# 阶段 7
mvn -pl mall-ai test
mvn -pl mall-ai spring-boot:run
```

---

## 七、提交规范

每阶段完成后提交一个 commit，message 格式：

```
<type>: stage <N> - <简短描述>

<type>: feat (新功能/代码改动) | test (测试用例) | fix (修复) | refactor (重构)
```

示例：

```
feat: stage0 - upgrade root pom to Spring Boot 3.5.14 and Java 17
test: stage1 - mall-common unit tests and swagger migration
feat: stage3 - mall-security JJWT 0.12 and Spring Security 6 upgrade
test: stage4 - mall-admin MockMvc tests
```