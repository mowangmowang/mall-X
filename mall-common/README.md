# Mall Common 通用模块

## 📋 模块概述

`mall-common` 是 Mall 电商系统的**通用基础模块**，为整个项目提供共享的工具类、配置类、异常处理机制和统一响应封装。该模块被 `mall-admin`、`mall-portal`、`mall-search` 等其他模块依赖，是系统的核心基础设施。

### 核心职责

- **统一 API 响应格式**：标准化前后端数据交互协议，确保返回数据结构一致
- **全局异常处理**：集中管理系统各类异常，返回友好错误信息
- **Redis 操作封装**：提供简洁的 Redis 数据结构操作接口（String/Hash/Set/List）
- **日志记录切面**：自动记录 Controller 层请求详情，支持 LogStash 集成
- **Swagger 文档配置**：简化 API 文档集成流程，支持 Spring Boot 2.6+ 兼容
- **通用工具类**：提供 IP 获取、分页转换等常用功能

---

## 🏗️ 模块结构

```
mall-common/
├── src/main/java/com/macro/mall/common/
│   ├── api/                    # API 响应封装
│   │   ├── CommonPage.java     # 通用分页数据类
│   │   ├── CommonResult.java   # 通用返回结果类
│   │   ├── IErrorCode.java     # 错误码接口
│   │   └── ResultCode.java     # 标准错误码枚举
│   ├── config/                 # 基础配置类
│   │   ├── BaseRedisConfig.java    # Redis 基础配置
│   │   └── BaseSwaggerConfig.java  # Swagger 基础配置
│   ├── domain/                 # 领域模型
│   │   ├── EsProductMessage.java   # ES 商品同步消息
│   │   ├── SwaggerProperties.java  # Swagger 配置属性
│   │   └── WebLog.java             # Web 请求日志对象
│   ├── exception/              # 异常处理
│   │   ├── ApiException.java       # 自定义 API 异常
│   │   ├── Asserts.java            # 断言工具类
│   │   └── GlobalExceptionHandler.java # 全局异常处理器
│   ├── log/                    # 日志切面
│   │   └── WebLogAspect.java       # 统一日志处理切面
│   ├── service/                # 服务接口
│   │   ├── RedisService.java       # Redis 操作接口
│   │   └── impl/
│   │       └── RedisServiceImpl.java # Redis 操作实现
│   └── util/                   # 工具类
│       └── RequestUtil.java        # 请求工具类（IP 获取）
└── src/main/resources/
    └── logback-spring.xml      # Logback 日志配置
```

---

## 📦 核心功能详解

### 1. API 响应封装 (`api` 包)

#### 1.1 CommonResult - 统一返回结果

所有 Controller 接口的返回值均封装为 `CommonResult<T>` 对象，确保前端接收格式一致。

**标准响应格式：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

**常用静态方法：**

| 方法 | 说明 | 使用场景 |
|------|------|----------|
| `success(T data)` | 成功返回（默认消息） | 查询操作成功 |
| `success(T data, String message)` | 成功返回（自定义消息） | 需要提示具体信息 |
| `failed(IErrorCode errorCode)` | 失败返回（使用错误码） | 业务逻辑失败 |
| `failed(String message)` | 失败返回（自定义消息） | 简单错误提示 |
| `validateFailed(String message)` | 参数校验失败 | 表单验证不通过 |
| `unauthorized(T data)` | 未登录（401） | Token 过期或缺失 |
| `forbidden(T data)` | 无权限（403） | 访问受限资源 |

**使用示例：**
```java
@GetMapping("/product/{id}")
public CommonResult<PmsProduct> getProduct(@PathVariable Long id) {
    PmsProduct product = productService.getById(id);
    if (product != null) {
        return CommonResult.success(product);
    } else {
        return CommonResult.failed("商品不存在");
    }
}
```

#### 1.2 CommonPage - 通用分页数据

支持将 **PageHelper** 和 **Spring Data** 的分页结果转换为统一格式。

**分页响应格式：**
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "totalPage": 5,
  "total": 50,
  "list": [ ... ]
}
```

**转换方法：**
- `CommonPage.restPage(List<T> list)` - 适用于 PageHelper 分页
- `CommonPage.restPage(Page<T> page)` - 适用于 Spring Data 分页

**使用示例：**
```java
@GetMapping("/product/list")
public CommonResult<CommonPage<PmsProduct>> list(
    @RequestParam(defaultValue = "1") Integer pageNum,
    @RequestParam(defaultValue = "10") Integer pageSize) {
    
    PageHelper.startPage(pageNum, pageSize);
    List<PmsProduct> productList = productService.list();
    return CommonResult.success(CommonPage.restPage(productList));
}
```

#### 1.3 ResultCode - 标准错误码

系统预定义的标准错误码枚举：

| 错误码 | 含义 | HTTP 状态码 | 说明 |
|--------|------|------------|------|
| 200 | SUCCESS | 200 | 操作成功 |
| 500 | FAILED | 500 | 操作失败（服务器内部错误） |
| 404 | VALIDATE_FAILED | 400 | 参数检验失败 |
| 401 | UNAUTHORIZED | 401 | 暂未登录或 Token 已过期 |
| 403 | FORBIDDEN | 403 | 没有相关权限 |

> ⚠️ **注意**：`VALIDATE_FAILED` 当前使用 404 作为业务错误码，但实际 HTTP 响应状态码为 200，建议在后续版本中根据 RESTful 规范调整为 400。

---

### 2. 异常处理机制 (`exception` 包)

#### 2.1 ApiException - 自定义业务异常

用于在 Service 层抛出业务逻辑错误，支持两种构造方式：

```java
// 方式一：使用错误码枚举
throw new ApiException(ResultCode.VALIDATE_FAILED);

// 方式二：使用自定义消息
throw new ApiException("库存不足，无法下单");
```

#### 2.2 Asserts - 断言工具类

提供便捷的参数校验方法，校验失败时自动抛出 `ApiException`。

**常用方法：**
```java
// 校验失败，抛出自定义消息
Asserts.fail("用户名不能为空");

// 校验失败，抛出错误码
Asserts.fail(ResultCode.VALIDATE_FAILED);
```

**使用示例：**
```java
public void createOrder(OrderParam param) {
    // 参数校验
    if (param.getProductId() == null) {
        Asserts.fail("商品ID不能为空");
    }
    if (param.getQuantity() <= 0) {
        Asserts.fail("购买数量必须大于0");
    }
    // ... 业务逻辑
}
```

#### 2.3 GlobalExceptionHandler - 全局异常处理器

使用 `@ControllerAdvice` 统一拦截并处理系统中抛出的各类异常，返回标准化的 `CommonResult`。

**处理的异常类型：**

| 异常类型 | 处理方法 | 返回内容 | HTTP 状态码 |
|---------|---------|---------|------------|
| `ApiException` | `handle()` | 错误码或自定义消息 | 200 |
| `MethodArgumentNotValidException` | `handleValidException()` | 字段校验错误（@RequestBody） | 200 |
| `BindException` | `handleValidException()` | 字段校验错误（@ModelAttribute） | 200 |
| `SQLSyntaxErrorException` | `handleSQLSyntaxErrorException()` | SQL 错误（演示环境特殊提示） | 200 |
| `Exception` | `handleException()` | 根因异常信息（兜底策略） | 200 |

> 💡 **说明**：所有异常均返回 HTTP 200 状态码，通过 `CommonResult.code` 区分业务成功或失败。

**参数校验错误示例：**
```json
{
  "code": 404,
  "message": "username不能为空",
  "data": null
}
```

---

### 3. Redis 操作封装 (`service` 包)

#### 3.1 RedisService 接口

提供常用的 Redis 数据结构操作方法，涵盖 **String**、**Hash**、**Set**、**List** 四种数据类型。

**核心功能分类：**

##### String 操作
- `set(key, value)` / `set(key, value, time)` - 保存属性（永久/带过期时间）
- `get(key)` - 获取属性
- `del(key)` / `del(keys)` - 删除单个/批量属性
- `expire(key, time)` - 设置过期时间
- `getExpire(key)` - 获取剩余过期时间
- `hasKey(key)` - 判断键是否存在
- `incr(key, delta)` / `decr(key, delta)` - 原子递增/递减

##### Hash 操作
- `hSet(key, hashKey, value)` / `hSet(key, hashKey, value, time)` - 设置哈希字段（永久/带过期时间）
- `hGet(key, hashKey)` - 获取哈希字段
- `hGetAll(key)` - 获取整个哈希表
- `hSetAll(key, map)` / `hSetAll(key, map, time)` - 批量设置哈希表
- `hDel(key, hashKeys)` - 删除哈希字段
- `hHasKey(key, hashKey)` - 判断哈希字段是否存在
- `hIncr(key, hashKey, delta)` / `hDecr(key, hashKey, delta)` - 哈希字段递增/递减

##### Set 操作
- `sAdd(key, values)` / `sAdd(key, time, values)` - 添加集合成员（永久/带过期时间）
- `sMembers(key)` - 获取所有成员
- `sIsMember(key, value)` - 判断成员是否存在
- `sSize(key)` - 获取集合大小
- `sRemove(key, values)` - 移除成员

##### List 操作
- `lPush(key, value)` / `lPush(key, value, time)` - 右侧入队（永久/带过期时间）
- `lPushAll(key, values)` / `lPushAll(key, time, values)` - 批量右侧入队
- `lRange(key, start, end)` - 范围查询
- `lSize(key)` - 获取列表长度
- `lIndex(key, index)` - 按索引获取
- `lRemove(key, count, value)` - 移除元素

**使用示例：**
```java
@Autowired
private RedisService redisService;

// 缓存用户信息（有效期 1 小时）
redisService.set("user:" + userId, userVO, 3600);

// 获取缓存
UserVO cachedUser = (UserVO) redisService.get("user:" + userId);

// 购物车（Hash 结构）
redisService.hSet("cart:" + userId, productId.toString(), cartItem);
Map<Object, Object> cartItems = redisService.hGetAll("cart:" + userId);

// 商品浏览量计数（原子递增）
redisService.incr("product:views:" + productId, 1);
```

#### 3.2 BaseRedisConfig - Redis 配置类

提供 Redis 相关的 Bean 定义，各模块可通过继承快速集成。

**核心配置：**
- **RedisTemplate**：Key 使用 String 序列化，Value 使用 JSON 序列化（Jackson2Json）
- **RedisSerializer**：启用默认类型信息，确保反序列化为具体对象而非 Map
- **RedisCacheManager**：默认缓存过期时间 1 天，非锁定写入器（提高并发性能）
- **RedisService**：注册 RedisService 服务实现

**序列化策略说明：**
```
Key/HashKey: String 序列化（便于阅读和管理）
Value/HashValue: JSON 序列化（支持嵌套对象，保留类型信息）
```

**继承使用示例：**
```java
@Configuration
public class RedisConfig extends BaseRedisConfig {
    // 无需额外配置，直接注入 RedisService 即可使用
}
```

---

### 4. 日志记录切面 (`log` 包)

#### 4.1 WebLogAspect - 统一日志处理

使用 AOP 切面自动拦截 Controller 层的所有公共方法，记录请求详细信息。

**记录的信息包括：**
- 操作描述（从 `@ApiOperation` 注解提取）
- 请求 URI、URL、HTTP 方法
- 客户端 IP 地址
- 请求参数（@RequestBody 和 @RequestParam）
- 响应结果
- 执行耗时（毫秒）

**切点表达式：**
```java
@Pointcut("execution(public * com.macro.mall.controller.*.*(..)) || 
           execution(public * com.macro.mall.*.controller.*.*(..))")
```

**日志输出示例：**
```json
{
  "url": "http://localhost:8080/product/list",
  "method": "GET",
  "parameter": {"pageNum": 1, "pageSize": 10},
  "spendTime": 45,
  "description": "查询商品列表",
  "ip": "192.168.1.100",
  "startTime": 1714464000000,
  "result": {"code": 200, "message": "操作成功", ...}
}
```

**LogStash 集成：**
日志同时输出到 LogStash（端口 4560-4563），便于集中式日志分析和 Elasticsearch 检索。

**日志级别分类：**
- DEBUG (4560): 调试日志
- ERROR (4561): 错误日志
- BUSINESS (4562): 业务日志
- RECORD (4563): 接口访问记录

---

### 5. Swagger 文档配置 (`config` 包)

#### 5.1 BaseSwaggerConfig - Swagger 基础配置

提供 Swagger2 API 文档的标准化配置，支持安全认证和版本兼容。

**核心功能：**
- 自动生成 RESTful API 文档
- 支持 JWT Token 认证（可选，通过 `enableSecurity` 配置）
- 兼容 Spring Boot 2.6+ 路径匹配策略（通过 `BeanPostProcessor` 修复）
- 支持自定义文档标题、描述、版本、联系人等信息

**配置属性（SwaggerProperties）：**

| 属性 | 类型 | 说明 | 示例 |
|------|------|------|------|
| apiBasePackage | String | Controller 扫描包路径 | `com.macro.mall.admin.controller` |
| enableSecurity | boolean | 是否启用安全认证 | `true` |
| title | String | 文档标题 | `Mall Admin API` |
| description | String | 文档描述 | `后台管理系统接口文档` |
| version | String | 版本号 | `1.0.0` |
| contactName | String | 联系人姓名 | `macro` |
| contactUrl | String | 联系人网址 | `https://github.com/macrozheng` |
| contactEmail | String | 联系人邮箱 | `macro@macro.com` |

**继承使用示例：**
```java
@Configuration
@EnableSwagger2
public class SwaggerConfig extends BaseSwaggerConfig {

    @Override
    public SwaggerProperties swaggerProperties() {
        return SwaggerProperties.builder()
                .apiBasePackage("com.macro.mall.admin.controller")
                .title("Mall Admin API")
                .description("后台管理系统接口文档")
                .version("1.0.0")
                .enableSecurity(true)
                .contactName("macro")
                .contactEmail("macro@macro.com")
                .build();
    }

    @Bean
    public BeanPostProcessor springfoxBeanPostProcessor() {
        return generateBeanPostProcessor();
    }
}
```

**访问地址：**
```
http://localhost:8080/swagger-ui/
```

---

### 6. 工具类 (`util` 包)

#### 6.1 RequestUtil - 请求工具类

提供获取客户端真实 IP 地址的方法，支持多级反向代理场景。

**核心方法：**
```java
public static String getRequestIp(HttpServletRequest request)
```

**IP 获取策略（优先级从高到低）：**
1. `x-forwarded-for` - Nginx 反向代理
2. `Proxy-Client-IP` - Apache 服务器
3. `WL-Proxy-Client-IP` - WebLogic 服务器
4. `request.getRemoteAddr()` - 直连地址
5. 本地回环地址处理（127.0.0.1 → 本机网卡 IP）

**多级代理处理：**
当 `x-forwarded-for` 包含多个 IP 时（如 `"192.168.1.1, 10.0.0.1"`），取第一个作为真实客户端 IP。

**使用示例：**
```java
String clientIp = RequestUtil.getRequestIp(request);
webLog.setIp(clientIp);
```

---

### 7. 领域模型 (`domain` 包)

#### 7.1 WebLog - Web 请求日志对象

用于封装单次 HTTP 请求的完整信息，由 `WebLogAspect` 填充。

**字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| description | String | 操作描述（来自 @ApiOperation） |
| username | String | 操作用户 |
| startTime | Long | 请求开始时间戳 |
| spendTime | Integer | 消耗时间（毫秒） |
| basePath | String | 根路径 |
| uri | String | 请求 URI |
| url | String | 完整 URL |
| method | String | HTTP 方法（GET/POST 等） |
| ip | String | 客户端 IP |
| parameter | Object | 请求参数 |
| result | Object | 响应结果 |

#### 7.2 EsProductMessage - ES 商品同步消息

用于在 RabbitMQ 中传递商品索引的增删改操作指令。

**字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| productId | Long | 商品 ID |
| actionType | String | 操作类型：ADD/UPDATE/DELETE |
| timestamp | Long | 时间戳（用于消息顺序控制） |

**使用场景：**
```java
// 商品上架时发送消息
EsProductMessage message = new EsProductMessage();
message.setProductId(productId);
message.setActionType("ADD");
message.setTimestamp(System.currentTimeMillis());
rabbitTemplate.convertAndSend("mall-es-direct", "es.insertProduct", message);
```

#### 7.3 SwaggerProperties - Swagger 配置属性

使用 Lombok `@Builder` 模式构建 Swagger 文档元数据。

---

### 8. 日志配置 (`logback-spring.xml`)

#### 8.1 日志输出目标

| Appender | 类型 | 级别 | 说明 | 端口/路径 |
|----------|------|------|------|----------|
| CONSOLE | 控制台 | DEBUG+ | 开发调试使用 | - |
| FILE_DEBUG | 文件滚动 | DEBUG+ | 按天分割，单文件 10MB | `/logs/debug/` |
| FILE_ERROR | 文件滚动 | ERROR | 仅错误日志 | `/logs/error/` |
| LOG_STASH_DEBUG | LogStash | DEBUG | 调试日志集中收集 | 4560 |
| LOG_STASH_ERROR | LogStash | ERROR | 错误日志集中收集 | 4561 |
| LOG_STASH_BUSINESS | LogStash | DEBUG | 业务日志 | 4562 |
| LOG_STASH_RECORD | LogStash | DEBUG | 接口访问记录 | 4563 |

#### 8.2 日志滚动策略

- **单文件大小限制**：10 MB（可配置 `LOG_FILE_MAX_SIZE`）
- **保留天数**：30 天（可配置 `LOG_FILE_MAX_HISTORY`）
- **命名格式**：`{APP_NAME}-{yyyy-MM-dd}-{index}.log`

#### 8.3 Logger 配置

| Logger | 级别 | Appender | 说明 |
|--------|------|----------|------|
| root | DEBUG | CONSOLE, FILE_DEBUG, FILE_ERROR, LOG_STASH_DEBUG, LOG_STASH_ERROR | 全局日志 |
| com.macro.mall.common.log.WebLogAspect | DEBUG | LOG_STASH_RECORD | 接口访问日志 |
| com.macro.mall | DEBUG | LOG_STASH_BUSINESS | 业务日志 |
| org.springframework | INFO | - | 抑制框架调试信息 |

**JSON 日志格式（LogStash）：**
```json
{
  "project": "mall",
  "level": "INFO",
  "service": "mall-admin",
  "pid": "12345",
  "thread": "http-nio-8080-exec-1",
  "class": "com.macro.mall.controller.PmsProductController",
  "message": "查询商品列表成功",
  "stack_trace": ""
}
```

---

## 🔧 依赖说明

### Maven 依赖

```xml
<dependencies>
    <!-- 分页插件 -->
    <dependency>
        <groupId>com.github.pagehelper</groupId>
        <artifactId>pagehelper</artifactId>
    </dependency>
    
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Data Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    
    <!-- Spring Data Commons -->
    <dependency>
        <groupId>org.springframework.data</groupId>
        <artifactId>spring-data-commons</artifactId>
        <version>${spring-data-commons.version}</version>
    </dependency>
    
    <!-- LogStash 日志编码器 -->
    <dependency>
        <groupId>net.logstash.logback</groupId>
        <artifactId>logstash-logback-encoder</artifactId>
    </dependency>
    
    <!-- Swagger2 -->
    <dependency>
        <groupId>io.springfox</groupId>
        <artifactId>springfox-boot-starter</artifactId>
    </dependency>
    
    <!-- 参数校验 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```

### 传递依赖

其他模块只需依赖 `mall-common`，即可间接获得上述所有功能：

```xml
<dependency>
    <groupId>com.macro.mall</groupId>
    <artifactId>mall-common</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

---

## 🚀 快速开始

### 1. 在其他模块中引入 mall-common

```xml
<!-- mall-admin/pom.xml 或 mall-portal/pom.xml -->
<dependency>
    <groupId>com.macro.mall</groupId>
    <artifactId>mall-common</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置 Redis（application.yml）

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: your_password
    database: 0
```

### 3. 继承 Redis 配置类

```java
@Configuration
public class RedisConfig extends BaseRedisConfig {
    // 无需额外代码
}
```

### 4. 继承 Swagger 配置类

```java
@Configuration
@EnableSwagger2
public class SwaggerConfig extends BaseSwaggerConfig {

    @Override
    public SwaggerProperties swaggerProperties() {
        return SwaggerProperties.builder()
                .apiBasePackage("com.macro.mall.admin.controller")
                .title("Mall Admin API")
                .description("后台管理系统接口文档")
                .version("1.0.0")
                .enableSecurity(true)
                .build();
    }

    @Bean
    public BeanPostProcessor springfoxBeanPostProcessor() {
        return generateBeanPostProcessor();
    }
}
```

### 5. 使用 RedisService

```java
@Service
public class ProductService {
    
    @Autowired
    private RedisService redisService;
    
    public ProductVO getProduct(Long id) {
        // 尝试从缓存获取
        ProductVO cached = (ProductVO) redisService.get("product:" + id);
        if (cached != null) {
            return cached;
        }
        
        // 查询数据库
        ProductVO product = productMapper.selectById(id);
        
        // 写入缓存（有效期 1 小时）
        redisService.set("product:" + id, product, 3600);
        
        return product;
    }
}
```

### 6. 使用统一响应格式

```java
@RestController
@RequestMapping("/product")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping("/{id}")
    public CommonResult<ProductVO> getProduct(@PathVariable Long id) {
        ProductVO product = productService.getProduct(id);
        if (product != null) {
            return CommonResult.success(product);
        } else {
            return CommonResult.failed("商品不存在");
        }
    }
    
    @PostMapping("/create")
    public CommonResult create(@Valid @RequestBody ProductParam param) {
        productService.create(param);
        return CommonResult.success(null, "创建成功");
    }
}
```

### 7. 使用断言校验参数

```java
@Service
public class OrderService {
    
    public void createOrder(OrderParam param) {
        // 参数校验
        Asserts.fail(param.getProductId() == null, "商品ID不能为空");
        Asserts.fail(param.getQuantity() <= 0, "购买数量必须大于0");
        
        // 业务逻辑...
    }
}
```

---

## 📝 最佳实践

### 1. 统一响应规范

✅ **推荐做法：**
```java
return CommonResult.success(data);
return CommonResult.failed(ResultCode.VALIDATE_FAILED);
```

❌ **避免做法：**
```java
// 不要直接返回实体对象
return product;

// 不要手动构造 Map
Map<String, Object> result = new HashMap<>();
result.put("code", 200);
return result;
```

### 2. 异常处理规范

✅ **推荐做法：**
```java
// Service 层抛出业务异常
if (stock < quantity) {
    throw new ApiException("库存不足");
}

// 或使用断言
Asserts.fail(stock < quantity, "库存不足");
```

❌ **避免做法：**
```java
// 不要在 Controller 层捕获异常后返回 null
try {
    productService.create(param);
} catch (Exception e) {
    return null; // 错误！
}
```

### 3. Redis 键命名规范

建议使用冒号分隔的层级结构：

```
user:{userId}              # 用户信息
product:{productId}        # 商品信息
cart:{userId}:{productId}  # 购物车项
order:{orderId}            # 订单信息
product:views:{productId}  # 商品浏览量（计数器）
```

### 4. 缓存过期时间设置

根据业务场景合理设置过期时间：

| 数据类型 | 建议过期时间 | 说明 |
|---------|------------|------|
| 用户信息 | 1-2 小时 | 变更频率低 |
| 商品详情 | 30 分钟 | 可能修改价格/库存 |
| 验证码 | 5 分钟 | 安全性要求高 |
| 会话 Token | 2 小时 | 平衡安全与体验 |
| 热点数据 | 永久 | 配合主动更新策略 |

### 5. 日志记录规范

✅ **推荐做法：**
```java
// 使用 @ApiOperation 描述接口功能
@ApiOperation("查询商品列表")
@GetMapping("/list")
public CommonResult<CommonPage<ProductVO>> list(...) {
    // 自动记录日志
}

// 关键业务操作手动记录日志
log.info("订单创建成功，orderId: {}", orderId);
log.error("支付失败，orderId: {}, reason: {}", orderId, e.getMessage());
```

---

## ⚠️ 注意事项

### 1. 序列化问题

**问题**：Redis 存储的对象必须实现 `Serializable` 接口。

**解决方案**：
```java
@Data
public class ProductVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    // ...
}
```

### 2. 循环依赖

**问题**：`BaseRedisConfig` 中定义的 Bean 可能被其他配置类引用，注意避免循环依赖。

**解决方案**：使用 `@Lazy` 注解延迟加载。

### 3. Swagger 兼容性

**问题**：Spring Boot 2.6+ 改变了路径匹配策略，可能导致 Swagger 启动失败。

**解决方案**：已在 `BaseSwaggerConfig` 中通过 `BeanPostProcessor` 修复，确保继承该类。

### 4. LogStash 连接

**问题**：如果未部署 LogStash，日志输出可能会阻塞。

**解决方案**：
- 开发环境可注释掉 LogStash Appender
- 或在 `application.yml` 中配置正确的 LogStash 地址

```yaml
logstash:
  host: localhost  # 修改为实际地址
  enableInnerLog: false
```

### 5. 分页页码差异

**问题**：PageHelper 页码从 1 开始，Spring Data 页码从 0 开始。

**解决方案**：`CommonPage.restPage()` 已处理此差异，直接使用即可。

---

## 🔄 扩展指南

### 1. 添加自定义错误码

```java
public enum BusinessResultCode implements IErrorCode {
    ORDER_NOT_FOUND(50001, "订单不存在"),
    STOCK_INSUFFICIENT(50002, "库存不足"),
    COUPON_EXPIRED(50003, "优惠券已过期");
    
    private long code;
    private String message;
    
    BusinessResultCode(long code, String message) {
        this.code = code;
        this.message = message;
    }
    
    @Override
    public long getCode() {
        return code;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
```

**使用：**
```java
return CommonResult.failed(BusinessResultCode.ORDER_NOT_FOUND);
```

### 2. 扩展 RedisService

如需添加新的 Redis 操作方法：

```java
// 1. 在 RedisService 接口中添加方法
void setEx(String key, Object value, long time, TimeUnit unit);

// 2. 在 RedisServiceImpl 中实现
@Override
public void setEx(String key, Object value, long time, TimeUnit unit) {
    redisTemplate.opsForValue().set(key, value, time, unit);
}
```

### 3. 自定义日志切点

如需记录 Service 层日志：

```java
@Pointcut("execution(public * com.macro.mall.service.*.*(..))")
public void serviceLog() {}

@Around("serviceLog()")
public Object doServiceAround(ProceedingJoinPoint joinPoint) throws Throwable {
    // 类似 WebLogAspect 的实现
}
```

---

## 📊 性能优化建议

### 1. Redis 连接池配置

```yaml
spring:
  redis:
    lettuce:
      pool:
        max-active: 8      # 最大连接数
        max-idle: 8        # 最大空闲连接
        min-idle: 0        # 最小空闲连接
        max-wait: -1ms     # 连接超时时间
```

### 2. 缓存穿透防护

对于可能查询不存在数据的场景，使用空值缓存：

```java
ProductVO product = (ProductVO) redisService.get("product:" + id);
if (product == null) {
    product = productMapper.selectById(id);
    if (product == null) {
        // 缓存空值，防止穿透（有效期 5 分钟）
        redisService.set("product:" + id, new EmptyObject(), 300);
        return null;
    }
    redisService.set("product:" + id, product, 3600);
}
return product;
```

### 3. 批量操作优化

```java
// ❌ 避免循环单个操作
for (Long id : ids) {
    redisService.del("product:" + id);
}

// ✅ 使用批量删除
List<String> keys = ids.stream()
    .map(id -> "product:" + id)
    .collect(Collectors.toList());
redisService.del(keys);
```

---

## 🐛 常见问题排查

### 1. Redis 连接失败

**现象**：`Cannot get Jedis connection`

**排查步骤**：
1. 检查 Redis 服务是否启动：`redis-cli ping`
2. 检查 `application.yml` 中的 host/port/password 配置
3. 检查防火墙是否开放 6379 端口

### 2. Swagger 页面空白

**现象**：访问 `/swagger-ui/` 显示空白

**排查步骤**：
1. 检查是否正确继承 `BaseSwaggerConfig`
2. 检查 `apiBasePackage` 路径是否正确
3. 查看控制台是否有异常日志

### 3. 日志未输出到文件

**现象**：只有控制台日志，无文件日志

**排查步骤**：
1. 检查 `logback-spring.xml` 中的 `LOG_FILE_PATH` 路径
2. 确认应用有写入权限
3. 检查日志级别配置（root level）

### 4. 反序列化类型丢失

**现象**：从 Redis 获取的对象变成 `LinkedHashMap`

**原因**：未启用默认类型信息

**解决方案**：已在 `BaseRedisConfig.redisSerializer()` 中配置：
```java
objectMapper.activateDefaultTyping(
    LaissezFaireSubTypeValidator.instance, 
    ObjectMapper.DefaultTyping.NON_FINAL
);
```
