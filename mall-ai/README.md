# mall-ai — AI 购物助手微服务

## 简介

mall-ai 是 mall 电商项目的 AI 微服务模块，提供基于大语言模型的智能购物助手功能。作为独立的微服务运行，可同时为多个前端（mall-app-web、mall-mobile-app 等）提供 AI 能力。

当前版本提供两个核心功能：

| 功能 | 说明 | 接口 |
|------|------|------|
| AI 商品导购 | 用户对商品提问，AI 根据商品信息回答 | `POST /ai/product/qa` |
| AI 售后建议 | 用户描述问题，AI 推荐退货原因并生成描述 | `POST /ai/return/suggest` |

## 架构

```
前端应用（uni-app）
     ↓  HTTP
mall-ai（Spring Boot, port 8086）
     ↓  AiClient（接口抽象层）
OpenAiCompatibleClient
     ↓  RestTemplate
DeepSeek / OpenAI / SiliconFlow / 任意 OpenAI 兼容 API
```

**设计特点：**
- **AI 客户端抽象**：通过 `AiClient` 接口 + `OpenAiCompatibleClient` 实现，切换大模型厂商只需改配置，无需改代码
- **无数据库依赖**：商品信息由前端传入，无需连接 MySQL/MyBatis，模块轻量
- **独立部署**：作为独立微服务运行，不影响现有 mall-portal 等模块

## 快速开始

### 1. 获取 API Key

选择任一平台注册获取 API Key：

| 平台 | 官网 | 备注 |
|------|------|------|
| DeepSeek | https://platform.deepseek.com | 注册送 500 万 token，性价比高 |
| SiliconFlow | https://cloud.siliconflow.cn | 注册送 2000 万 token，支持多种开源模型 |
| OpenAI | https://platform.openai.com | 需海外支付方式 |

### 2. 配置 API Key

编辑 `src/main/resources/application-dev.yml`：

```yaml
ai:
  client:
    api-key: sk-your-api-key-here
```

### 3. 启动服务

```bash
# 编译并启动
mvn spring-boot:run -pl mall-ai -am

# 或打包后启动
mvn package -pl mall-ai -am -DskipTests
java -jar mall-ai/target/mall-ai-1.0-SNAPSHOT.jar
```

启动后访问 http://localhost:8086/ai/product/qa 测试。

### 4. 验证

```bash
# 测试商品问答
curl -X POST http://localhost:8086/ai/product/qa \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "question": "这款适合送人吗？",
    "productName": "Redmi Note 13",
    "productPrice": "1999",
    "productBrand": "小米",
    "productSubTitle": "性能小钢炮 5G 手机"
  }'

# 测试售后建议
curl -X POST http://localhost:8086/ai/return/suggest \
  -H "Content-Type: application/json" \
  -d '{
    "issue": "收到手机屏幕就有一条裂缝",
    "productName": "Redmi Note 13",
    "productAttr": "颜色:黑色;版本:8+256G",
    "orderSn": "20240501123456"
  }'
```

## API 文档

### AI 商品问答

```
POST /ai/product/qa
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| productId | Long | 是 | 商品 ID |
| question | String | 是 | 用户问题 |
| productName | String | 是 | 商品名称 |
| productBrand | String | 否 | 品牌名 |
| productPrice | String | 否 | 价格 |
| productSubTitle | String | 否 | 副标题/描述 |

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "reply": "这款 Redmi Note 13 是小米推出的5G性能手机，价格1999元，配置均衡，作为礼物送人是不错的选择..."
  }
}
```

### AI 退货建议

```
POST /ai/return/suggest
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| issue | String | 是 | 用户描述的问题 |
| productName | String | 否 | 商品名称 |
| productAttr | String | 否 | 商品属性（如"颜色:黑色;版本:8+256G"）|
| orderSn | String | 否 | 订单编号 |

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "suggestedReason": "商品损坏",
    "suggestedDescription": "收到的手机屏幕有一条裂缝，属于物流中造成的损坏..."
  }
}
```

## 切换 AI 模型厂商

**无需改代码**，只需修改 `application.yml` 即可切换：

```yaml
ai:
  client:
    # DeepSeek
    base-url: https://api.deepseek.com/v1
    model: deepseek-chat

    # 或 OpenAI
    # base-url: https://api.openai.com/v1
    # model: gpt-4o-mini

    # 或 SiliconFlow
    # base-url: https://api.siliconflow.cn/v1
    # model: Qwen/Qwen2.5-7B-Instruct

    # 或任意 OpenAI 兼容 API
    # base-url: https://your-custom-endpoint/v1
    # model: your-model-name

    temperature: 0.7        # 生成随机性 (0-2)
    max-tokens: 1024        # 最大回复长度
```

所有兼容 OpenAI Chat Completions API 格式的服务均可接入。

## 模块结构

```
mall-ai/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/macro/mall/ai/
    │   ├── MallAiApplication.java         # 启动类
    │   ├── client/                        # AI 客户端抽象层
    │   │   ├── AiClient.java              #   接口
    │   │   ├── ChatMessage.java           #   消息 DTO
    │   │   └── OpenAiCompatibleClient.java #   OpenAI 兼容实现
    │   ├── config/
    │   │   ├── AiClientConfig.java        #   AI 客户端配置
    │   │   ├── RestTemplateConfig.java    #   HTTP 客户端配置
    │   │   └── MallCorsConfig.java        #   跨域配置
    │   ├── controller/
    │   │   └── AiAssistantController.java #   REST 控制器
    │   ├── domain/                        # DTO
    │   │   ├── AiResponse.java
    │   │   ├── ProductQaRequest.java
    │   │   ├── ReturnSuggestionRequest.java
    │   │   └── ReturnSuggestionResult.java
    │   └── service/
    │       ├── AiAssistantService.java    #   业务接口
    │       └── impl/
    │           └── AiAssistantServiceImpl.java  # 业务实现
    └── resources/
        ├── application.yml                # 公共配置
        └── application-dev.yml            # 开发环境配置（含 API Key）
```

## 技术栈

- **框架**: Spring Boot 2.7.5
- **语言**: Java 8
- **HTTP 客户端**: RestTemplate（spring-boot-starter-web）
- **AI 接口**: OpenAI Chat Completions API 格式（兼容 DeepSeek / OpenAI / SiliconFlow 等）
- **API 风格**: RESTful JSON，统一响应格式 `CommonResult<T>`

## 依赖关系

mall-ai 只依赖 mall-common 模块（使用 `CommonResult` 统一响应），不依赖 mall-mbg、mall-security 等模块，不连接数据库。

```
mall-ai → mall-common → Spring Boot
```
