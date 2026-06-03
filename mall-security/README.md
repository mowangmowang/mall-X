# Mall Security 模块 - 安全认证与授权

## 📋 目录

- [模块概述](#模块概述)
- [核心功能](#核心功能)
- [技术栈](#技术栈)
- [架构设计](#架构设计)
- [核心组件](#核心组件)
- [工作流程](#工作流程)
- [配置说明](#配置说明)
- [使用指南](#使用指南)
- [扩展开发](#扩展开发)
- [常见问题](#常见问题)

---

## 模块概述

**Mall Security** 是电商系统 (Mall) 的安全认证与授权模块，基于 **Spring Security** 和 **JWT (JSON Web Token)** 实现无状态的身份验证和基于角色的访问控制 (RBAC)。

### 主要职责

1. **身份认证 (Authentication)**：验证用户身份，生成和验证 JWT 令牌
2. **权限授权 (Authorization)**：基于动态权限规则控制资源访问
3. **缓存保护 (Cache Protection)**：防止 Redis 异常影响核心业务逻辑
4. **异常处理 (Exception Handling)**：统一处理认证和授权失败场景

### 模块定位

```
mall-common (通用工具)
    ↑
mall-security (安全认证) ← 当前模块
    ↑
mall-admin / mall-portal / mall-search (业务模块)
```

本模块作为独立的安全基础设施，被其他业务模块依赖使用，提供统一的安全认证能力。

---

## 核心功能

### ✅ 已实现功能

| 功能 | 说明 | 状态 |
|------|------|------|
| JWT 令牌管理 | 生成、验证、刷新 JWT 令牌 | ✅ |
| 动态权限控制 | 基于数据库的 URL-权限映射 | ✅ |
| 白名单机制 | 配置无需认证的路径 | ✅ |
| CORS 支持 | 跨域请求预检放行 | ✅ |
| 密码加密 | BCrypt 单向加密 | ✅ |
| Redis 缓存降级 | 缓存异常不影响主流程 | ✅ |
| 统一异常处理 | 401/403 标准化响应 | ✅ |
| 无状态会话 | STATELESS 会话策略 | ✅ |

---

## 技术栈

### 核心框架

- **Spring Security 5.x** - 安全框架
- **JWT (io.jsonwebtoken)** - 令牌标准
- **Spring AOP** - 面向切面编程
- **Redis** - 分布式缓存

### 工具库

- **Hutool** - Java 工具类库
- **Lombok** - 简化代码
- **SLF4J + Logback** - 日志框架

### 依赖关系

```xml
<!-- 核心依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

---

## 架构设计

### 整体架构图

```mermaid
graph TB
    Client[客户端 Client]
    
    subgraph "Mall Security 模块"
        JWTFilter[JWT 认证过滤器<br/>JwtAuthenticationTokenFilter]
        DynFilter[动态权限过滤器<br/>DynamicSecurityFilter]
        
        subgraph "决策组件"
            DecisionMgr[访问决策管理器<br/>DynamicAccessDecisionManager]
            MetaSource[安全元数据源<br/>DynamicSecurityMetadataSource]
        end
        
        subgraph "异常处理"
            AuthEntryPoint[认证入口点<br/>RestAuthenticationEntryPoint]
            AccessDeniedHandler[拒绝处理器<br/>RestfulAccessDeniedHandler]
        end
        
        subgraph "工具类"
            JwtUtil[JWT 工具<br/>JwtTokenUtil]
            RedisAspect[Redis 缓存切面<br/>RedisCacheAspect]
        end
    end
    
    subgraph "Spring Security 核心"
        FilterChain[安全过滤链<br/>SecurityFilterChain]
        SecurityContext[安全上下文<br/>SecurityContextHolder]
    end
    
    subgraph "外部服务"
        Redis[(Redis 缓存)]
        DB[(MySQL 数据库)]
    end
    
    Client -->|HTTP Request| JWTFilter
    JWTFilter -->|提取 Token| JwtUtil
    JwtUtil -->|验证令牌| Redis
    JwtUtil -->|解析用户| DB
    
    JWTFilter -->|认证成功| SecurityContext
    SecurityContext --> DynFilter
    DynFilter -->|获取权限规则| MetaSource
    MetaSource -->|查询 URL-权限映射| DB
    
    DynFilter -->|投票决策| DecisionMgr
    DecisionMgr -->|允许访问| FilterChain
    DecisionMgr -->|拒绝访问| AccessDeniedHandler
    
    JWTFilter -->|令牌无效| AuthEntryPoint
    DynFilter -->|权限不足| AccessDeniedHandler
    
    RedisAspect -.->|拦截缓存方法| Redis
```

### 请求处理流程

```mermaid
sequenceDiagram
    participant C as 客户端
    participant JF as JWT 过滤器
    participant JU as JWT 工具
    participant SC as 安全上下文
    participant DF as 动态权限过滤器
    participant MS as 元数据源
    participant DM as 决策管理器
    participant API as 业务接口
    
    C->>JF: HTTP Request + Authorization Header
    JF->>JF: 检查是否为白名单路径
    
    alt 白名单路径
        JF->>API: 直接放行
    else 需要认证
        JF->>JU: 提取并验证 JWT Token
        JU->>JU: 解析用户名
        JU->>JU: 验证令牌有效性
        
        alt 令牌无效
            JU-->>JF: 验证失败
            JF->>C: 返回 401 Unauthorized
        else 令牌有效
            JU->>SC: 设置认证信息
            JF->>DF: 继续过滤链
            
            DF->>MS: 获取当前 URL 所需权限
            MS->>MS: 匹配 URL 模式
            MS-->>DF: 返回权限配置
            
            DF->>DM: 执行权限决策
            DM->>DM: 比对用户权限
            
            alt 权限充足
                DM-->>DF: 允许访问
                DF->>API: 执行业务逻辑
                API-->>C: 返回响应
            else 权限不足
                DM-->>DF: 拒绝访问
                DF->>C: 返回 403 Forbidden
            end
        end
    end
```

---

## 核心组件

### 1. JWT 认证过滤器 (JwtAuthenticationTokenFilter)

**职责**：拦截每个 HTTP 请求，验证 JWT 令牌并设置用户认证信息。

**关键逻辑**：
```java
// 1. 从请求头提取 Token
String authHeader = request.getHeader("Authorization");
String authToken = authHeader.substring("Bearer ".length());

// 2. 解析用户名
String username = jwtTokenUtil.getUserNameFromToken(authToken);

// 3. 加载用户信息（含 Redis 缓存）
UserDetails userDetails = userDetailsService.loadUserByUsername(username);

// 4. 验证令牌有效性
if (jwtTokenUtil.validateToken(authToken, userDetails)) {
    // 5. 设置安全上下文
    SecurityContextHolder.getContext().setAuthentication(authentication);
}
```

**调用链路**：
```
JwtAuthenticationTokenFilter.doFilterInternal()
    ↓
UserDetailsService.loadUserByUsername()
    ↓
AdminServiceImpl.loadUserByUsername()
    ├─ getAdminByUsername()    // 查询用户基本信息（缓存+DB）
    └─ getResourceList()       // 查询用户权限列表（缓存+DB）
    ↓
返回 AdminUserDetails（包含完整用户信息和权限）
```

---

### 2. 动态权限过滤器 (DynamicSecurityFilter)

**职责**：根据数据库配置的 URL-权限映射规则，动态判断用户是否有访问权限。

**核心流程**：
```mermaid
flowchart TD
    Start[请求到达] --> CheckOptions{是否 OPTIONS 请求?}
    CheckOptions -->|是| Allow1[直接放行]
    CheckOptions -->|否| CheckWhite{是否白名单路径?}
    CheckWhite -->|是| Allow2[直接放行]
    CheckWhite -->|否| GetMeta[获取 URL 所需权限]
    
    GetMeta --> MatchURL[匹配 URL 模式]
    MatchURL --> HasRule{是否有权限规则?}
    
    HasRule -->|否| Allow3[默认允许访问]
    HasRule -->|是| Decision[执行权限决策]
    
    Decision --> Compare{用户权限是否匹配?}
    Compare -->|是| Allow4[允许访问]
    Compare -->|否| Deny[拒绝访问 403]
    
    Allow1 --> End[继续过滤链]
    Allow2 --> End
    Allow3 --> End
    Allow4 --> End
    Deny --> End
```

**关键代码**：
```java
// 1. 获取当前 URL 所需的权限配置
Collection<ConfigAttribute> attributes = metadataSource.getAttributes(request);

// 2. 执行访问决策
InterceptorStatusToken token = super.beforeInvocation(fi);

try {
    // 3. 继续执行后续过滤器
    fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
} finally {
    super.afterInvocation(token, null);
}
```

---

### 3. 动态权限决策管理器 (DynamicAccessDecisionManager)

**职责**：判断用户拥有的权限是否满足访问资源所需的权限。

**决策逻辑**：
```java
public void decide(Authentication authentication, Object object,
                   Collection<ConfigAttribute> configAttributes) {
    // 场景1：未配置权限规则，直接放行
    if (CollUtil.isEmpty(configAttributes)) {
        return;
    }
    
    // 场景2：遍历所需权限，逐一比对
    for (ConfigAttribute configAttribute : configAttributes) {
        String needAuthority = configAttribute.getAttribute();
        
        for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            if (needAuthority.equals(grantedAuthority.getAuthority())) {
                return; // 找到匹配权限，允许访问
            }
        }
    }
    
    // 场景3：所有权限都不匹配，拒绝访问
    throw new AccessDeniedException("抱歉，您没有访问权限");
}
```

---

### 4. JWT 工具类 (JwtTokenUtil)

**职责**：提供 JWT 令牌的生成、验证、刷新功能。

**JWT 结构**：
```
Header.Payload.Signature

Header: {"alg": "HS512", "typ": "JWT"}
Payload: {"sub": "admin", "created": 1489079981393, "exp": 1489684781}
Signature: HMACSHA512(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

**核心方法**：

| 方法 | 功能 | 返回值 |
|------|------|--------|
| `generateToken(UserDetails)` | 根据用户信息生成令牌 | JWT 字符串 |
| `validateToken(String, UserDetails)` | 验证令牌有效性 | boolean |
| `getUserNameFromToken(String)` | 从令牌提取用户名 | String |
| `refreshHeadToken(String)` | 刷新未过期的令牌 | 新令牌或 null |

**令牌刷新策略**：
```mermaid
flowchart LR
    A[收到旧令牌] --> B{令牌为空?}
    B -->|是| E[返回 null]
    B -->|否| C{令牌已过期?}
    C -->|是| E
    C -->|否| D{30分钟内刚刷新?}
    D -->|是| F[返回原令牌]
    D -->|否| G[更新创建时间<br/>生成新令牌]
```

---

### 5. Redis 缓存切面 (RedisCacheAspect)

**职责**：拦截缓存服务方法，防止 Redis 异常导致业务中断。

**降级策略**：
```mermaid
flowchart TD
    A[调用缓存方法] --> B{执行成功?}
    B -->|是| C[返回结果]
    B -->|否| D{标注 @CacheException?}
    D -->|是| E[抛出异常]
    D -->|否| F[记录日志<br/>返回 null]
    
    E --> G[调用方处理异常]
    F --> H[业务降级处理]
```

**使用示例**：
```java
// 场景1：缓存失败不影响主流程（默认行为）
public Product getProductFromCache(Long id) {
    // 若 Redis 宕机，返回 null，从数据库查询
    return redisTemplate.opsForValue().get("product:" + id);
}

// 场景2：缓存失败必须感知（标注注解）
@CacheException
public CriticalData getCriticalDataFromCache(String key) {
    // 若 Redis 宕机，抛出异常，调用方需处理
    return redisTemplate.opsForValue().get(key);
}
```

---

### 6. 异常处理器

#### 6.1 认证入口点 (RestAuthenticationEntryPoint)

**触发场景**：
- 请求头缺少 `Authorization` 字段
- JWT 令牌已过期
- JWT 令牌签名无效

**响应示例**：
```json
{
  "code": 401,
  "message": "Full authentication is required to access this resource",
  "data": null
}
```

#### 6.2 拒绝处理器 (RestfulAccessDeniedHandler)

**触发场景**：
- 普通用户尝试访问管理员接口
- 用户角色权限不足
- 访问未分配给当前角色的资源

**响应示例**：
```json
{
  "code": 403,
  "message": "抱歉，您没有访问权限",
  "data": null
}
```

---

## 工作流程

### 完整认证授权流程

```mermaid
stateDiagram-v2
    [*] --> 接收请求
    
    接收请求 --> 检查白名单: JWT 过滤器
    检查白名单 --> 直接放行: 是白名单路径
    检查白名单 --> 提取 Token: 非白名单路径
    
    提取 Token --> 验证 Token: JWT 工具
    验证 Token --> 返回 401: 令牌无效
    验证 Token --> 加载用户信息: 令牌有效
    
    加载用户信息 --> 设置安全上下文
    设置安全上下文 --> 动态权限校验: 动态权限过滤器
    
    动态权限校验 --> 获取权限规则: 元数据源
    获取权限规则 --> 执行决策: 决策管理器
    
    执行决策 --> 返回 403: 权限不足
    执行决策 --> 执行业务逻辑: 权限充足
    
    执行业务逻辑 --> 返回响应
    直接放行 --> 返回响应
    返回 401 --> [*]
    返回 403 --> [*]
    返回响应 --> [*]
```

### 用户登录流程

```mermaid
sequenceDiagram
    participant U as 用户
    participant C as 前端
    participant A as 认证接口
    participant S as 用户服务
    participant J as JWT 工具
    participant R as Redis
    participant D as 数据库
    
    U->>C: 输入用户名密码
    C->>A: POST /api/auth/login
    A->>S: authenticate(username, password)
    
    S->>D: 查询用户信息
    D-->>S: 返回用户数据
    
    S->>S: 验证密码 (BCrypt)
    
    alt 密码正确
        S->>J: generateToken(userDetails)
        J->>J: 生成 JWT 令牌
        J-->>S: 返回 Token
        
        S->>R: 缓存用户信息
        S-->>A: 返回 Token + 用户信息
        A-->>C: 响应成功
        C->>C: 存储 Token (localStorage)
        C-->>U: 登录成功
    else 密码错误
        S-->>A: 抛出异常
        A-->>C: 返回错误信息
        C-->>U: 提示密码错误
    end
```

### 令牌刷新流程

```mermaid
flowchart TD
    A[前端检测到 Token 即将过期] --> B[发送刷新请求<br/>/api/auth/refreshToken]
    B --> C{后端验证旧 Token}
    
    C -->|Token 为空| H[返回 null]
    C -->|Token 格式错误| H
    C -->|Token 已过期| I[返回错误<br/>需重新登录]
    
    C -->|Token 有效| D{30分钟内刚刷新?}
    
    D -->|是| E[返回原 Token<br/>防抖机制]
    D -->|否| F[更新创建时间<br/>生成新 Token]
    
    E --> G[前端替换 Token]
    F --> G
    H --> J[前端清除 Token<br/>跳转登录页]
    I --> J
    
    G --> K[继续使用新 Token]
```

---

## 配置说明

### application.yml 配置项

```yaml
# JWT 配置
jwt:
  tokenHeader: Authorization          # Token 在请求头中的键名
  tokenHead: "Bearer "                # Token 前缀
  secret: mall-admin-secret           # 签名密钥（生产环境应使用环境变量）
  expiration: 604800                  # 令牌有效期（秒），默认 7 天

# 安全白名单配置
secure:
  ignored:
    urls:
      - /api/auth/login               # 登录接口
      - /api/auth/register            # 注册接口
      - /api/auth/refreshToken        # 刷新令牌接口
      - /swagger-ui/**                # Swagger 文档
      - /v3/api-docs/**               # OpenAPI 文档
      - /actuator/**                  # 健康检查端点
      - /static/**                    # 静态资源

# Redis 配置
spring:
  redis:
    host: localhost
    port: 6379
    password:                         # Redis 密码（若有）
    database: 0
    timeout: 3000                     # 连接超时时间（毫秒）
    lettuce:
      pool:
        max-active: 8                 # 最大活跃连接数
        max-idle: 8                   # 最大空闲连接数
        min-idle: 0                   # 最小空闲连接数
```

### 安全配置类说明

#### CommonSecurityConfig

注册所有 Security 相关的 Bean：

```java
@Configuration
public class CommonSecurityConfig {
    @Bean public PasswordEncoder passwordEncoder()           // 密码加密器
    @Bean public IgnoreUrlsConfig ignoreUrlsConfig()         // 白名单配置
    @Bean public JwtTokenUtil jwtTokenUtil()                 // JWT 工具
    @Bean public RestfulAccessDeniedHandler accessDeniedHandler()  // 403 处理器
    @Bean public RestAuthenticationEntryPoint authEntryPoint()     // 401 处理器
    @Bean public JwtAuthenticationTokenFilter jwtFilter()    // JWT 过滤器
    
    // 以下 Bean 仅在业务模块提供 DynamicSecurityService 时加载
    @ConditionalOnBean(name = "dynamicSecurityService")
    @Bean public DynamicAccessDecisionManager decisionManager()
    
    @ConditionalOnBean(name = "dynamicSecurityService")
    @Bean public DynamicSecurityMetadataSource metadataSource()
    
    @ConditionalOnBean(name = "dynamicSecurityService")
    @Bean public DynamicSecurityFilter dynamicFilter()
}
```

#### SecurityConfig

配置 Spring Security 过滤链：

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) {
        // 1. 配置白名单
        // 2. 放行 OPTIONS 请求
        // 3. 其他请求需要认证
        // 4. 关闭 CSRF
        // 5. 无状态会话
        // 6. 配置异常处理器
        // 7. 添加 JWT 过滤器
        // 8. 条件添加动态权限过滤器
    }
}
```

---

## 使用指南

### 1. 引入依赖

在业务模块的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.macro.mall</groupId>
    <artifactId>mall-security</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 实现动态权限服务（可选）

若需启用动态权限控制，需实现 `DynamicSecurityService` 接口：

```java
@Service("dynamicSecurityService")
public class MyDynamicSecurityService implements DynamicSecurityService {
    
    @Autowired
    private UmsResourceService resourceService;
    
    @Override
    public Map<String, ConfigAttribute> loadDataSource() {
        Map<String, ConfigAttribute> map = new HashMap<>();
        
        // 从数据库加载所有资源
        List<UmsResource> resources = resourceService.list();
        
        for (UmsResource resource : resources) {
            // Key: URL 模式（支持 Ant 通配符）
            // Value: 所需权限标识
            map.put(resource.getUrl(), 
                new org.springframework.security.access.SecurityConfig(
                    resource.getName()
                )
            );
        }
        
        return map;
    }
}
```

### 3. 配置白名单

在 `application.yml` 中配置无需认证的路径：

```yaml
secure:
  ignored:
    urls:
      - /api/public/**      # 公开接口
      - /api/webhook/**     # Webhook 回调
```

### 4. 使用 JWT 工具

#### 生成令牌

```java
@Autowired
private JwtTokenUtil jwtTokenUtil;

public String login(String username, String password) {
    // 1. 验证用户名密码
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    
    if (!passwordEncoder.matches(password, userDetails.getPassword())) {
        throw new BadCredentialsException("密码错误");
    }
    
    // 2. 生成 JWT 令牌
    String token = jwtTokenUtil.generateToken(userDetails);
    
    return token;
}
```

#### 刷新令牌

```java
public String refreshToken(String oldToken) {
    // 自动处理过期、防抖等逻辑
    String newToken = jwtTokenUtil.refreshHeadToken(oldToken);
    
    if (newToken == null) {
        throw new IllegalStateException("令牌刷新失败，请重新登录");
    }
    
    return newToken;
}
```

### 5. 使用缓存降级

#### 默认降级（推荐）

```java
@Service
public class ProductCacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public Product getProduct(Long id) {
        // 若 Redis 宕机，返回 null，调用方可从数据库查询
        String key = "product:" + id;
        return (Product) redisTemplate.opsForValue().get(key);
    }
}
```

#### 强制异常

```java
@Service
public class CriticalCacheService {
    
    @CacheException  // Redis 异常时抛出异常
    public CriticalData getData(String key) {
        return (CriticalData) redisTemplate.opsForValue().get(key);
    }
}
```

---

## 扩展开发

### 自定义权限决策逻辑

继承 `DynamicAccessDecisionManager` 并重写 `decide` 方法：

```java
@Component
public class CustomAccessDecisionManager extends DynamicAccessDecisionManager {
    
    @Override
    public void decide(Authentication authentication, Object object,
                       Collection<ConfigAttribute> configAttributes) {
        // 自定义决策逻辑
        // 例如：支持 OR/AND 逻辑组合、时间限制、IP 白名单等
        
        super.decide(authentication, object, configAttributes);
    }
}
```

### 添加自定义过滤器

在 `SecurityConfig` 中注册：

```java
@Bean
SecurityFilterChain filterChain(HttpSecurity http) {
    // ... 其他配置
    
    // 在 JWT 过滤器之前添加自定义过滤器
    http.addFilterBefore(new CustomFilter(), JwtAuthenticationTokenFilter.class);
    
    return http.build();
}
```

### 扩展 JWT Payload

在 `JwtTokenUtil` 中添加自定义字段：

```java
public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
    claims.put(CLAIM_KEY_CREATED, new Date());
    
    // 添加自定义字段
    if (userDetails instanceof AdminUserDetails) {
        AdminUserDetails admin = (AdminUserDetails) userDetails;
        claims.put("userId", admin.getId());
        claims.put("nickname", admin.getNickname());
    }
    
    return generateToken(claims);
}
```

---

## 常见问题

### Q1: 如何处理 Token 过期？

**方案1：前端主动刷新**
```javascript
// 前端拦截器
axios.interceptors.response.use(response => {
    return response;
}, error => {
    if (error.response.status === 401) {
        // Token 过期，尝试刷新
        return refreshToken().then(newToken => {
            // 使用新 Token 重试请求
            return retryRequest(error.config, newToken);
        }).catch(() => {
            // 刷新失败，跳转登录页
            window.location.href = '/login';
        });
    }
});
```

**方案2：后端自动刷新**
- 在响应头中返回 Token 剩余有效期
- 前端检测到即将过期时主动调用 `/api/auth/refreshToken`

---

### Q2: 如何实现权限实时更新？

**步骤1：清空缓存**
```java
@Autowired
private DynamicSecurityMetadataSource metadataSource;

@PostMapping("/api/admin/resource/update")
public CommonResult updateResource() {
    // 1. 更新数据库中的权限规则
    resourceService.updateById(resource);
    
    // 2. 清空内存中的权限缓存
    metadataSource.clearDataSource();
    
    // 3. 下次请求时会自动重新加载
    return CommonResult.success("权限更新成功");
}
```

**步骤2：通知其他节点（集群环境）**
```java
// 使用 Redis Pub/Sub 或消息队列通知其他服务实例
redisTemplate.convertAndSend("permission:update", resourceId);
```

---

### Q3: 如何调试权限问题？

**启用调试日志**：
```yaml
logging:
  level:
    com.macro.mall.security: DEBUG
    org.springframework.security: DEBUG
```

**关键日志点**：
```java
// JwtAuthenticationTokenFilter
LOGGER.info("checking username:{}", username);
LOGGER.info("authenticated user:{}", username);

// DynamicAccessDecisionManager
// 在 decide 方法中添加日志
LOGGER.debug("用户权限: {}", authentication.getAuthorities());
LOGGER.debug("所需权限: {}", configAttributes);
```

**Postman 测试**：
```http
GET /api/admin/product/list
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

---

### Q4: Redis 宕机如何应对？

**当前策略**：
- 未标注 `@CacheException` 的方法：返回 null，业务层从数据库查询
- 标注 `@CacheException` 的方法：抛出异常，调用方处理

**优化建议**：
```java
@Component
public class CacheHealthChecker {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Scheduled(fixedRate = 30000) // 每 30 秒检查一次
    public void checkRedisHealth() {
        try {
            redisTemplate.opsForValue().set("health_check", "ok", 1, TimeUnit.SECONDS);
            LOGGER.info("Redis 连接正常");
        } catch (Exception e) {
            LOGGER.error("Redis 连接异常，切换至数据库直连模式", e);
            // 触发告警、切换数据源等
        }
    }
}
```

---

### Q5: 如何支持多租户？

**方案1：在 JWT 中包含租户 ID**
```java
// 生成 Token 时
claims.put("tenantId", user.getTenantId());

// 过滤器中提取
String tenantId = claims.get("tenantId", String.class);
TenantContext.setCurrentTenant(tenantId);
```

**方案2：基于域名区分租户**
```java
String host = request.getServerName(); // tenant1.example.com
String tenantId = extractTenantFromHost(host);
```

---

## 最佳实践

### ✅ 推荐做法

1. **密钥管理**：生产环境使用环境变量或密钥管理服务存储 JWT 密钥
   ```yaml
   jwt:
     secret: ${JWT_SECRET:default-secret}
   ```

2. **令牌有效期**：根据业务场景设置合理的有效期
   - 管理后台：7 天
   - 移动端 App：30 天
   - 敏感操作：1 小时

3. **HTTPS 强制**：生产环境必须使用 HTTPS 传输 Token

4. **权限粒度**：遵循最小权限原则，避免过度授权
   ```
   pms:product:create    # 商品创建权限
   pms:product:update    # 商品更新权限
   pms:product:delete    # 商品删除权限
   ```

5. **日志审计**：记录关键安全事件
   ```java
   LOGGER.warn("非法访问尝试: userId={}, url={}, method={}", 
       userId, requestUri, requestMethod);
   ```

### ❌ 避免做法

1. **不要在客户端存储敏感信息**：JWT Payload 仅 Base64 编码，可被解码
2. **不要硬编码密钥**：避免在代码中明文存储 `secret`
3. **不要忽略 Token 验证**：所有受保护接口都必须经过 JWT 过滤器
4. **不要滥用缓存降级**：核心业务数据不应依赖缓存

---

## 性能优化

### 1. Redis 连接池调优

```yaml
spring:
  redis:
    lettuce:
      pool:
        max-active: 20      # 根据并发量调整
        max-idle: 10
        min-idle: 5
        max-wait: 2000      # 最大等待时间（毫秒）
```

### 2. JWT 验证优化

- 使用本地缓存存储已验证的 Token（短期，如 5 分钟）
- 避免每次请求都查询数据库

```java
@Component
public class TokenValidationCache {
    
    private final Cache<String, Boolean> cache = 
        Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();
    
    public boolean isValid(String token) {
        return cache.get(token, k -> jwtTokenUtil.validateToken(k, userDetails));
    }
}
```

### 3. 权限规则缓存

- 将 URL-权限映射缓存在本地内存
- 使用版本号机制实现增量更新

---

## 安全建议

### 🔒 加固措施

1. **速率限制**：防止暴力破解
   ```java
   @RateLimiter(maxAttempts = 5, timeWindow = 60) // 60 秒内最多 5 次
   public LoginResponse login(LoginRequest request) {
       // ...
   }
   ```

2. **IP 白名单**：限制管理后台访问来源
   ```java
   if (!ipWhitelist.contains(request.getRemoteAddr())) {
       throw new AccessDeniedException("IP 不在白名单中");
   }
   ```

3. **双因素认证 (2FA)**：敏感操作要求二次验证
   ```java
   if (requires2FA(user)) {
       sendVerificationCode(user.getPhone());
       return CommonResult.pending("请输入验证码");
   }
   ```

4. **会话固定攻击防护**：登录后更换 Session ID（虽为无状态，但需注意）

5. **CORS 严格配置**：生产环境限制允许的域名
   ```java
   response.setHeader("Access-Control-Allow-Origin", "https://yourdomain.com");
   ```

