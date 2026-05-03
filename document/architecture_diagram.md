# Mall 项目架构图

## 1. 系统总体架构 (System Architecture)

> **说明**：为避免连线交叉，本图采用**分层聚合**方式展示。后端服务统一访问底层基础设施，具体依赖关系见下方的《模块依赖图》。

```mermaid
graph TD
    %% 定义样式
    classDef client fill:#e1f5fe,stroke:#01579b,stroke-width:2px;
    classDef backend fill:#fff3e0,stroke:#e65100,stroke-width:2px;
    classDef infra fill:#e8f5e9,stroke:#1b5e20,stroke-width:2px,stroke-dasharray: 5 5;
    classDef core fill:#f3e5f5,stroke:#4a148c,stroke-width:2px;

    subgraph ClientLayer["客户端层 (Client Layer)"]
        direction LR
        Web["后台管理前端<br/>mall-admin-web<br/>(Vue.js + Element UI)"]
        App["移动端前台<br/>mall-app-web<br/>(UniApp)"]
    end

    subgraph BackendLayer["后端应用层 (Backend Application Layer)"]
        direction LR
        Admin["后台管理服务<br/>mall-admin<br/>(Port: 8080)"]
        Portal["前台商城服务<br/>mall-portal<br/>(Port: 8085)"]
        Search["商品搜索服务<br/>mall-search<br/>(Port: 8081)"]
        AI["AI 购物助手<br/>mall-ai<br/>(Port: 8086)"]
    end

    subgraph InfraLayer["基础设施与数据存储层 (Infrastructure)"]
        direction TB
        subgraph DB["关系型与文档存储"]
            MySQL[("MySQL<br/>核心业务数据")]
            MongoDB[("MongoDB<br/>用户行为记录")]
        end
        
        subgraph CacheMQ["缓存与消息队列"]
            Redis[("Redis<br/>缓存/Session/验证码")]
            RabbitMQ["RabbitMQ<br/>异步消息/订单超时"]
        end
        
        subgraph SearchFile["搜索与文件存储"]
            ES[("Elasticsearch<br/>商品搜索引擎")]
            OSS["对象存储<br/>MinIO/Aliyun OSS"]
        end
    end

    subgraph CoreLayer["核心支撑模块 (Core Modules)"]
        direction LR
        Security["安全模块<br/>mall-security<br/>(Spring Security + JWT)"]
        MBG["数据访问模块<br/>mall-mbg<br/>(MyBatis Generator)"]
        Common["通用工具模块<br/>mall-common"]
    end

    %% 客户端调用关系
    Web -->|HTTP API| Admin
    App -->|HTTP API| Portal
    App -->|搜索 API| Search
    App -->|AI 对话 API| AI

    %% 后端依赖核心模块 (聚合连接，减少线条)
    BackendLayer --> CoreLayer

    %% 后端访问基础设施 (聚合连接，减少线条)
    BackendLayer --> InfraLayer

    %% 核心模块内部依赖
    Security --> Common
    MBG --> Common

    %% 应用样式
    class Web,App client;
    class Admin,Portal,Search,AI backend;
    class DB,CacheMQ,SearchFile infra;
    class Security,MBG,Common core;
```

## 2. Maven 模块依赖关系 (Module Dependencies)

> **说明**：展示各 Java 模块之间的引用关系，`mall-common` 为最底层基础包。

```mermaid
graph TD
    Admin[mall-admin<br/>后台服务] --> Security[mall-security<br/>安全模块]
    Admin --> MBG[mall-mbg<br/>数据访问]
    
    Portal[mall-portal<br/>前台服务] --> Security
    Portal --> MBG
    
    Search[mall-search<br/>搜索服务] --> MBG
    Search --> Common[mall-common<br/>通用工具]
    
    AI[mall-ai<br/>AI 助手] --> MBG
    AI --> Common
    
    Security --> Common
    MBG --> Common

    classDef svc fill:#fff3e0,stroke:#e65100,stroke-width:2px;
    classDef mod fill:#f3e5f5,stroke:#4a148c,stroke-width:2px;
    class Admin,Portal,Search,AI svc;
    class Security,MBG,Common mod;
```

## 2. 后端服务详细架构 (Backend Service Architecture)

```mermaid
graph LR
    subgraph "Controller 层"
        C1["ProductController"]
        C2["OrderController"]
        C3["MemberController"]
    end

    subgraph "Service 层"
        S1["ProductService"]
        S2["OrderService"]
        S3["MemberService"]
    end

    subgraph "DAO/Mapper 层"
        D1["PmsProductMapper"]
        D2["OmsOrderMapper"]
        D3["UmsMemberMapper"]
    end

    subgraph "数据库"
        DB[("MySQL")]
    end

    C1 --> S1
    C2 --> S2
    C3 --> S3
    
    S1 --> D1
    S2 --> D2
    S3 --> D3
    
    D1 --> DB
    D2 --> DB
    D3 --> DB
    
    S1 -.->|Cache| Redis
    S2 -.->|Async Msg| RabbitMQ
```

## 3. 技术栈概览 (Technology Stack)

| 层级 | 技术选型 |
| :--- | :--- |
| **前端** | Vue.js, Element UI, UniApp, TypeScript |
| **后端框架** | Spring Boot 2.7.5, Spring Security, MyBatis |
| **数据库** | MySQL 8.0, Redis, MongoDB, Elasticsearch |
| **中间件** | RabbitMQ |
| **存储** | MinIO, Aliyun OSS |
| **运维** | Docker, Jenkins, Logstash |
| **开发工具** | Maven, Lombok, Hutool, Swagger |

## 4. 模块依赖关系 (Module Dependencies)

```mermaid
graph TD
    Admin[mall-admin] --> MBG[mall-mbg]
    Admin --> Security[mall-security]
    Portal[mall-portal] --> MBG
    Portal --> Security
    Search[mall-search] --> Common[mall-common]
    Search --> MBG
    AI[mall-ai] --> Common
    AI --> MBG
    
    MBG --> Common
    Security --> Common
```

## 5. 业务流程示例：订单创建 (Order Creation Flow)

```mermaid
sequenceDiagram
    participant User as 用户
    participant Portal as mall-portal
    participant Redis as Redis
    participant MQ as RabbitMQ
    participant DB as MySQL

    User->>Portal: 1. 提交订单
    Portal->>Redis: 2. 检查库存 (原子扣减)
    alt 库存充足
        Portal->>DB: 3. 创建订单记录
        Portal->>MQ: 4. 发送延迟消息 (超时取消)
        Portal-->>User: 5. 返回订单信息
    else 库存不足
        Portal-->>User: 6. 返回失败提示
    end
```
