# Mall Search - 基于 Elasticsearch 的商品搜索微服务

## 📋 项目概述

**Mall Search** 是 mall 电商系统的搜索微服务模块，基于 **Elasticsearch** 实现高性能的商品全文搜索、聚合分析和智能推荐功能。通过 **RabbitMQ** 消息队列实现与商品管理服务的异步数据同步，确保搜索索引的实时性。

### 核心特性

- 🔍 **全文搜索**：支持中文分词（IK Analyzer），提供商品名称、副标题、关键词的多字段加权搜索
- 🎯 **综合筛选**：支持品牌、分类、属性等多维度筛选条件
- 📊 **聚合分析**：动态获取搜索结果的品牌、分类、属性统计信息
- 💡 **智能推荐**：基于商品相似度算法，推荐相关商品
- ⚡ **异步同步**：通过 RabbitMQ 实现商品数据的实时索引更新
- 🔄 **批量导入**：支持从 MySQL 数据库批量导入商品到 Elasticsearch

---

## 🏗️ 系统架构

### 整体架构图

```mermaid
graph TB
    subgraph "前端应用"
        A[Vue.js 前端]
    end
    
    subgraph "mall-search 搜索服务"
        B[EsProductController<br/>REST API]
        C[EsProductService<br/>业务逻辑层]
        D[ElasticsearchRestTemplate<br/>ES 操作模板]
        E[EsProductRepository<br/>Spring Data Repository]
        F[EsProductReceiver<br/>消息接收器]
    end
    
    subgraph "数据存储"
        G[(MySQL<br/>pms_product)]
        H[(Elasticsearch<br/>pms 索引)]
    end
    
    subgraph "消息中间件"
        I[RabbitMQ<br/>mall.product.update]
    end
    
    subgraph "其他微服务"
        J[mall-admin<br/>商品管理服务]
    end
    
    A -->|HTTP 请求| B
    B --> C
    C --> D
    C --> E
    D --> H
    E --> H
    C -->|分页查询| G
    J -->|商品变更消息| I
    I -->|消费消息| F
    F --> C
    
    style H fill:#ff6b6b
    style I fill:#4ecdc4
    style G fill:#45b7d1
```

### 技术栈

| 技术 | 版本/说明 | 用途 |
|------|----------|------|
| **Spring Boot** | 2.x | 微服务框架 |
| **Elasticsearch** | 7.x | 搜索引擎，存储商品索引 |
| **Spring Data Elasticsearch** | - | ES 数据访问抽象层 |
| **RabbitMQ** | 3.x | 消息队列，异步同步商品数据 |
| **MyBatis** | - | 从 MySQL 查询商品数据 |
| **IK Analyzer** | - | 中文分词插件 |
| **Swagger** | 2.x | API 文档生成 |
| **Lombok** | - | 简化 Java 代码 |

---

## 📂 项目结构

```
mall-search/
├── src/main/java/com/macro/mall/search/
│   ├── component/                  # 消息组件
│   │   └── EsProductReceiver.java  # RabbitMQ 消息接收器
│   ├── config/                     # 配置类
│   │   ├── EsProductMqConfig.java  # RabbitMQ 配置
│   │   ├── MallCorsConfig.java     # 跨域配置
│   │   ├── MyBatisConfig.java      # MyBatis 配置
│   │   └── SwaggerConfig.java      # Swagger 文档配置
│   ├── controller/                 # 控制器层
│   │   └── EsProductController.java # REST API 接口
│   ├── dao/                        # 数据访问层
│   │   └── EsProductDao.java       # MyBatis DAO 接口
│   ├── domain/                     # 领域模型
│   │   ├── EsProduct.java          # ES 商品文档实体
│   │   ├── EsProductAttributeValue.java # 商品属性值（嵌套类型）
│   │   └── EsProductRelatedInfo.java    # 搜索关联信息（聚合结果）
│   ├── repository/                 # 仓储层
│   │   └── EsProductRepository.java # Spring Data Repository
│   ├── service/                    # 服务层
│   │   ├── EsProductService.java   # 服务接口
│   │   └── impl/
│   │       └── EsProductServiceImpl.java # 服务实现类
│   └── MallSearchApplication.java  # 启动类
├── src/main/resources/
│   ├── dao/
│   │   └── EsProductDao.xml        # MyBatis SQL 映射文件
│   ├── application.yml             # 主配置文件
│   ├── application-dev.yml         # 开发环境配置
│   └── application-prod.yml        # 生产环境配置
├── pom.xml                         # Maven 依赖配置
└── README.md                       # 项目文档
```

---

## 🚀 快速开始

### 前置要求

- JDK 1.8+
- Maven 3.6+
- Elasticsearch 7.x（需安装 IK 分词插件）
- RabbitMQ 3.x
- MySQL 5.7+

### 环境准备

#### 1. 安装 Elasticsearch 和 IK 分词插件

```bash
# 下载 Elasticsearch 7.x
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.17.0-linux-x86_64.tar.gz

# 安装 IK 分词插件
./bin/elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.17.0/elasticsearch-analysis-ik-7.17.0.zip

# 启动 Elasticsearch
./bin/elasticsearch
```

#### 2. 安装 RabbitMQ

```bash
# 使用 Docker 启动 RabbitMQ
docker run -d --name rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=mall \
  -e RABBITMQ_DEFAULT_PASS=mall \
  rabbitmq:3-management

# 创建虚拟主机和队列（可通过管理界面 http://localhost:15672 操作）
```

#### 3. 初始化数据库

执行 `document/sql/mall.sql` 脚本，确保 `pms_product`、`pms_product_attribute`、`pms_product_attribute_value` 表存在且有数据。

### 配置修改

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  rabbitmq:
    host: localhost      # RabbitMQ 地址
    port: 5672
    username: mall
    password: mall
    virtual-host: /mall

server:
  port: 8081            # 搜索服务端口
```

如需连接远程 Elasticsearch，在 `application-dev.yml` 中配置：

```yaml
spring:
  elasticsearch:
    rest:
      uris: http://localhost:9200
```

### 启动服务

```bash
# 编译项目
mvn clean package -DskipTests

# 启动服务
java -jar target/mall-search-1.0-SNAPSHOT.jar

# 或使用 Maven 插件启动
mvn spring-boot:run
```

启动成功后，访问 Swagger 文档：http://localhost:8081/swagger-ui.html

---

## 📖 API 接口说明

### 1. 商品索引管理

#### 1.1 批量导入商品到 ES

```http
POST /esProduct/importAll
```

**功能**：从 MySQL 数据库批量导入所有已上架商品到 Elasticsearch  
**返回**：成功导入的商品数量

**示例**：
```bash
curl -X POST http://localhost:8081/esProduct/importAll
```

**响应**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1250
}
```

#### 1.2 创建/更新单个商品索引

```http
POST /esProduct/create/{id}
```

**参数**：
- `id`: 商品 ID

**功能**：根据商品 ID 从 MySQL 查询并创建或更新 ES 索引

**示例**：
```bash
curl -X POST http://localhost:8081/esProduct/create/26
```

#### 1.3 删除商品索引

```http
GET /esProduct/delete/{id}
```

**参数**：
- `id`: 商品 ID

**示例**：
```bash
curl -X GET http://localhost:8081/esProduct/delete/26
```

#### 1.4 批量删除商品索引

```http
POST /esProduct/delete/batch?ids=26,27,28
```

**参数**：
- `ids`: 商品 ID 列表（逗号分隔）

---

### 2. 商品搜索

#### 2.1 简单搜索

```http
GET /esProduct/search/simple?keyword=手机&pageNum=0&pageSize=10
```

**参数**：
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| keyword | String | 否 | - | 搜索关键字 |
| pageNum | Integer | 否 | 0 | 页码（从 0 开始） |
| pageSize | Integer | 否 | 5 | 每页大小 |

**功能**：根据关键字匹配商品名称、副标题或关键词，按相关度排序

**示例**：
```bash
curl -X GET "http://localhost:8081/esProduct/search/simple?keyword=华为&pageNum=0&pageSize=5"
```

**响应**：
```json
{
  "code": 200,
  "data": {
    "pageNum": 1,
    "pageSize": 5,
    "totalPage": 10,
    "total": 48,
    "list": [
      {
        "id": 26,
        "name": "华为 HUAWEI P40 Pro",
        "subTitle": "超感知徕卡四摄",
        "price": 5988.00,
        "pic": "http://example.com/p40.jpg",
        "brandName": "华为",
        "productCategoryName": "手机"
      }
    ]
  }
}
```

#### 2.2 综合搜索（支持筛选和排序）

```http
GET /esProduct/search?keyword=手机&brandId=6&productCategoryId=18&pageNum=0&pageSize=10&sort=0
```

**参数**：
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| keyword | String | 否 | - | 搜索关键字 |
| brandId | Long | 否 | - | 品牌 ID |
| productCategoryId | Long | 否 | - | 分类 ID |
| pageNum | Integer | 否 | 0 | 页码 |
| pageSize | Integer | 否 | 5 | 每页大小 |
| sort | Integer | 否 | 0 | 排序方式：<br/>0->相关度<br/>1->新品<br/>2->销量<br/>3->价格升序<br/>4->价格降序 |

**功能**：支持多维度筛选和多种排序策略的综合搜索

**示例**：
```bash
# 搜索华为品牌手机，按价格从低到高排序
curl -X GET "http://localhost:8081/esProduct/search?keyword=手机&brandId=6&sort=3"
```

---

### 3. 商品推荐

#### 3.1 基于商品 ID 推荐相似商品

```http
GET /esProduct/recommend/{id}?pageNum=0&pageSize=5
```

**参数**：
- `id`: 参考商品 ID
- `pageNum`: 页码（可选）
- `pageSize`: 每页大小（可选）

**功能**：根据参考商品的名称、品牌、分类进行加权匹配，推荐相似商品（排除自身）

**示例**：
```bash
curl -X GET "http://localhost:8081/esProduct/recommend/26?pageNum=0&pageSize=5"
```

**推荐算法权重**：
- 名称匹配：权重 8
- 同品牌：权重 5
- 关键词匹配：权重 5
- 同分类：权重 3
- 副标题匹配：权重 3

---

### 4. 聚合分析

#### 4.1 获取搜索相关的聚合信息

```http
GET /esProduct/search/relate?keyword=手机
```

**参数**：
- `keyword`: 搜索关键字（可选）

**功能**：返回搜索结果中涉及的品牌列表、分类列表、属性筛选条件

**示例**：
```bash
curl -X GET "http://localhost:8081/esProduct/search/relate?keyword=手机"
```

**响应**：
```json
{
  "code": 200,
  "data": {
    "brandNames": ["华为", "小米", "苹果", "OPPO"],
    "productCategoryNames": ["手机", "手机配件"],
    "productAttrs": [
      {
        "attrId": 51,
        "attrName": "颜色",
        "attrValues": ["红色", "蓝色", "黑色", "白色"]
      },
      {
        "attrId": 52,
        "attrName": "容量",
        "attrValues": ["64GB", "128GB", "256GB"]
      }
    ]
  }
}
```

---

## 🔧 核心功能详解

### 1. Function Score Query 加权搜索

搜索时使用 **Function Score Query** 对不同字段设置不同权重，提升搜索相关性：

```mermaid
graph LR
    A[用户输入关键字] --> B[Function Score Query]
    B --> C[名称匹配<br/>权重 10]
    B --> D[关键词匹配<br/>权重 5]
    B --> E[副标题匹配<br/>权重 3]
    C --> F[分数累加]
    D --> F
    E --> F
    F --> G[按总分排序]
    G --> H[返回结果]
    
    style C fill:#ff6b6b
    style D fill:#ffd93d
    style E fill:#6bcf7f
```

**代码实现**（[EsProductServiceImpl.java](file:///D:/course/Java/graduateProject/finish/mall/mall-search/src/main/java/com/macro/mall/search/service/impl/EsProductServiceImpl.java#L320-L345)）：

```java
private FunctionScoreQueryBuilder buildFunctionScoreQuery(String keyword) {
    List<FunctionScoreQueryBuilder.FilterFunctionBuilder> filterFunctionBuilders = new ArrayList<>();
    // 商品名称匹配，权重最高（10）
    filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
            QueryBuilders.matchQuery(FIELD_NAME, keyword),
            ScoreFunctionBuilders.weightFactorFunction(10)));
    // 关键词匹配，权重中等（5）
    filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
            QueryBuilders.matchQuery(FIELD_KEYWORDS, keyword),
            ScoreFunctionBuilders.weightFactorFunction(5)));
    // 副标题匹配，权重较低（3）
    filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
            QueryBuilders.matchQuery(FIELD_SUB_TITLE, keyword),
            ScoreFunctionBuilders.weightFactorFunction(3)));
    
    return QueryBuilders.functionScoreQuery(builders)
            .scoreMode(FunctionScoreQuery.ScoreMode.SUM)  // 分数累加模式
            .setMinScore(0.5f);  // 最低分数阈值
}
```

---

### 2. 商品数据同步流程

```mermaid
sequenceDiagram
    participant Admin as mall-admin<br/>商品管理服务
    participant MQ as RabbitMQ
    participant Receiver as EsProductReceiver
    participant Service as EsProductService
    participant MySQL as MySQL 数据库
    participant ES as Elasticsearch

    Admin->>MQ: 发送商品变更消息<br/>(ADD/UPDATE/DELETE)
    Note over MQ: Queue: mall.product.update
    
    MQ->>Receiver: 消费消息
    Receiver->>Service: 调用 create/delete 方法
    
    alt ADD 或 UPDATE
        Service->>MySQL: 查询商品数据
        MySQL-->>Service: 返回 EsProduct 对象
        Service->>ES: 保存/更新索引
        ES-->>Service: 确认写入
    else DELETE
        Service->>ES: 删除索引文档
        ES-->>Service: 确认删除
    end
    
    Service-->>Receiver: 返回处理结果
    Receiver->>MQ: 发送 ACK
```

**消息格式**：
```json
{
  "productId": 26,
  "actionType": "UPDATE"  // ADD / UPDATE / DELETE
}
```

**配置位置**：[EsProductMqConfig.java](file:///D:/course/Java/graduateProject/finish/mall/mall-search/src/main/java/com/macro/mall/search/config/EsProductMqConfig.java)

---

### 3. 批量导入策略

为避免内存溢出，采用**分页批量导入**策略：

```mermaid
flowchart TD
    A[开始导入] --> B[pageNum = 1<br/>pageSize = 500]
    B --> C[PageHelper 分页查询 MySQL]
    C --> D{是否有数据?}
    D -->|是| E[批量保存到 ES<br/>saveAll]
    E --> F[totalImported += size]
    F --> G[pageNum++]
    G --> C
    D -->|否| H[返回 totalImported]
    H --> I[结束]
    
    style E fill:#4ecdc4
    style H fill:#ff6b6b
```

**关键代码**（[EsProductServiceImpl.java](file:///D:/course/Java/graduateProject/finish/mall/mall-search/src/main/java/com/macro/mall/search/service/impl/EsProductServiceImpl.java#L80-L97)）：

```java
@Override
public int importAll() {
    int pageNum = 1;
    int pageSize = 500;  // 每批处理 500 条
    int totalImported = 0;
    while (true) {
        PageHelper.startPage(pageNum, pageSize);
        List<EsProduct> esProductList = productDao.getAllEsProductList(null);
        if (CollectionUtils.isEmpty(esProductList)) {
            break;  // 无更多数据，退出循环
        }
        productRepository.saveAll(esProductList);  // 批量保存
        totalImported += esProductList.size();
        pageNum++;
        LOGGER.info("已导入 {} 条商品数据", totalImported);
    }
    return totalImported;
}
```

---

### 4. Elasticsearch 索引结构

**索引名称**：`pms`  
**分片设置**：1 个主分片，0 个副本（开发环境）

#### 字段映射

| 字段 | 类型 | 说明 | 分词器 |
|------|------|------|--------|
| id | Long | 商品 ID | - |
| productSn | Keyword | 商品编码 | 不分词 |
| name | Text | 商品名称 | ik_max_word |
| subTitle | Text | 副标题 | ik_max_word |
| keywords | Text | 关键词 | ik_max_word |
| brandId | Long | 品牌 ID | - |
| brandName | Keyword | 品牌名称 | 不分词 |
| productCategoryId | Long | 分类 ID | - |
| productCategoryName | Keyword | 分类名称 | 不分词 |
| price | BigDecimal | 价格 | - |
| sale | Integer | 销量 | - |
| attrValueList | Nested | 属性值列表 | - |
| └─ id | Long | 属性值 ID | - |
| └─ productAttributeId | Long | 属性 ID | - |
| └─ name | Keyword | 属性名称 | 不分词 |
| └─ value | Keyword | 属性值 | 不分词 |
| └─ type | Integer | 属性类型<br/>0->规格 1->参数 | - |

**Nested 类型说明**：`attrValueList` 使用 Nested 类型而非 Object 类型，确保属性值的独立查询和聚合，避免交叉匹配问题。

---

### 5. 聚合分析实现

获取搜索相关的品牌、分类、属性统计信息：

```mermaid
graph TB
    A[搜索请求] --> B[构建 NativeSearchQuery]
    B --> C[添加 Terms Aggregation<br/>brandNames]
    B --> D[添加 Terms Aggregation<br/>productCategoryNames]
    B --> E[添加 Nested Aggregation<br/>allAttrValues]
    E --> F[Filter Aggregation<br/>type=1 参数类型]
    F --> G[Terms Aggregation<br/>attrIds]
    G --> H[Terms Aggregation<br/>attrValues]
    G --> I[Terms Aggregation<br/>attrNames]
    C --> J[执行查询]
    D --> J
    I --> J
    J --> K[解析聚合结果]
    K --> L[返回 EsProductRelatedInfo]
    
    style C fill:#ff6b6b
    style D fill:#4ecdc4
    style E fill:#ffd93d
```

**代码位置**：[EsProductServiceImpl.java#searchRelatedInfo](file:///D:/course/Java/graduateProject/finish/mall/mall-search/src/main/java/com/macro/mall/search/service/impl/EsProductServiceImpl.java#L287-L318)

---

## 🧪 测试

### 单元测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=MallSearchApplicationTests
```

### 集成测试流程

1. **启动依赖服务**：
   ```bash
   # 确保 Elasticsearch 和 RabbitMQ 正在运行
   curl http://localhost:9200  # 检查 ES
   curl http://localhost:15672 # 检查 RabbitMQ 管理界面
   ```

2. **导入测试数据**：
   ```bash
   curl -X POST http://localhost:8081/esProduct/importAll
   ```

3. **执行搜索测试**：
   ```bash
   # 简单搜索
   curl "http://localhost:8081/esProduct/search/simple?keyword=手机"
   
   # 综合搜索
   curl "http://localhost:8081/esProduct/search?keyword=手机&brandId=6&sort=3"
   
   # 获取聚合信息
   curl "http://localhost:8081/esProduct/search/relate?keyword=手机"
   ```

---

## 📊 性能优化建议

### 1. Elasticsearch 调优

- **索引刷新间隔**：批量导入时临时增大 `refresh_interval`
- **批量大小**：当前设置为 500 条/批，可根据服务器内存调整
- **分片策略**：生产环境建议设置 replicas > 0

### 2. 查询优化

- **使用 Filter 上下文**：品牌和分类筛选使用 `filter` 而非 `query`，利用缓存提升性能
- **限制返回字段**：前端只需展示字段时，使用 `_source` 过滤
- **分页深度限制**：避免深度分页（pageNum * pageSize > 10000），建议使用 `search_after`

### 3. 消息队列优化

- **消费者并发**：增加 `concurrency` 提升消息处理速度
- **重试机制**：当前配置最大重试 3 次，指数退避（1s → 2s → 4s）
- **死信队列**：重试失败的消息进入死信队列，便于后续人工处理

---

## 🔍 常见问题

### Q1: 搜索结果为空？

**排查步骤**：
1. 检查 Elasticsearch 是否正常运行：`curl http://localhost:9200`
2. 确认索引是否存在：`curl http://localhost:9200/_cat/indices?v`
3. 验证是否有数据：`curl http://localhost:9200/pms/_count`
4. 如无数据，执行批量导入：`POST /esProduct/importAll`

### Q2: 中文分词不生效？

**解决方案**：
1. 确认已安装 IK 分词插件：`./bin/elasticsearch-plugin list`
2. 检查字段映射是否指定了 `analyzer: ik_max_word`
3. 重启 Elasticsearch 使插件生效

### Q3: 消息队列消费失败？

**排查步骤**：
1. 查看 RabbitMQ 管理界面，确认队列是否有积压消息
2. 检查应用日志，定位具体错误信息
3. 验证 MySQL 和 Elasticsearch 连接是否正常
4. 确认消息格式是否正确（JSON 序列化）

### Q4: 聚合结果不准确？

**可能原因**：
1. Nested 类型字段未正确使用 `nested` 路径
2. 聚合查询未添加正确的过滤条件
3. 数据同步延迟，ES 索引未及时更新

---

## 📝 开发指南

### 添加新的搜索字段

1. **修改 EsProduct.java**：
   ```java
   @Field(analyzer = "ik_max_word", type = FieldType.Text)
   private String newField;  // 新字段
   ```

2. **修改 EsProductDao.xml**：在 SQL 查询中添加新字段映射

3. **重新导入数据**：调用 `/esProduct/importAll` 重建索引

### 调整搜索权重

修改 [EsProductServiceImpl.java](file:///D:/course/Java/graduateProject/finish/mall/mall-search/src/main/java/com/macro/mall/search/service/impl/EsProductServiceImpl.java#L320-L345) 中的 `buildFunctionScoreQuery` 方法：

```java
// 调整权重值
ScoreFunctionBuilders.weightFactorFunction(10)  // 修改此数值
```

### 自定义排序规则

在 [EsProductServiceImpl.java#search](file:///D:/course/Java/graduateProject/finish/mall/mall-search/src/main/java/com/macro/mall/search/service/impl/EsProductServiceImpl.java#L150-L188) 方法中添加新的 `sort` 分支：

```java
else if (sort == 5) {
    nativeSearchQueryBuilder.withSorts(
        SortBuilders.fieldSort("newField").order(SortOrder.DESC)
    );
}
```

---

## 🌐 部署

### Docker 部署

```bash
# 构建镜像
mvn clean package docker:build

# 启动容器
docker run -d --name mall-search \
  -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=prod \
  mall/mall-search:1.0-SNAPSHOT
```

### Kubernetes 部署

参考 `document/docker/docker-compose-app.yml` 中的配置，编写 K8s Deployment 和 Service YAML 文件。

---

## 📄 许可证

本项目遵循 MIT 许可证。详见 [LICENSE](../../LICENSE) 文件。

---

## 👥 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/AmazingFeature`
3. 提交更改：`git commit -m 'Add some AmazingFeature'`
4. 推送到分支：`git push origin feature/AmazingFeature`
5. 开启 Pull Request

---

## 📞 联系方式

- **项目主页**：[https://github.com/macrozheng/mall](https://github.com/macrozheng/mall)
- **作者**：macro
- **邮箱**：macrozheng@163.com

---

**最后更新时间**：2026-04-30
