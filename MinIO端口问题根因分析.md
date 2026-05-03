# MinIO 端口不一致问题根因分析

## 🔍 问题现象

数据库中商品图片 URL 出现 `localhost:9000` 端口，而 MinIO 实际运行在 `9001` 端口。

---

## 📊 根因分析

### 原因 1：初始化 SQL 脚本中的硬编码端口 ⭐⭐⭐

**文件**：`document/sql/mall.sql`

```sql
-- 第 920 行（已修复）
INSERT INTO `pms_brand` VALUES (59, '测试品牌', 'C', 0, 0, 0, NULL, NULL, 
'http://localhost:9000/mall/20220609/Snipaste_2022-06-08_14-35-53.png', 
'http://localhost:9000/mall/20220609/biji_05.jpg', '12345');
```

**分析**：
- 这是 2022 年 6 月创建的数据
- 当时可能使用 Docker 部署 MinIO，默认端口是 9000
- SQL 脚本中直接硬编码了完整的 URL

---

### 原因 2：MinIO 上传时的配置读取 ⭐⭐

**文件**：`mall-admin/src/main/java/com/macro/mall/controller/MinioController.java`

```java
@Value("${minio.endpoint}")
private String ENDPOINT;

// 第 77 行：上传成功后返回完整 URL
minioUploadDto.setUrl(ENDPOINT + "/" + BUCKET_NAME + "/" + objectName);
```

**分析**：
- 商品 ID 46 的图片 URL 时间戳：`20260425`（2026年4月25日）
- 如果当时 `application-dev.yml` 配置的是 9000 端口
- 上传的图片 URL 就会是 `http://localhost:9000/mall/...`

---

### 原因 3：配置文件的历史变更

**Git 历史记录**：

```
b723849 清理项目根目录迁移后的旧路径引用
8252720 初始化mall项目完整代码

原始配置：endpoint: http://localhost:9000
第一次修改：改为 9091（我误以为是 Docker 部署）
第二次修改：改为 9001（你的本地部署端口）
```

---

## ✅ 已完成的修复

### 1. 数据库数据修复

**执行脚本**：`document/sql/fix_minio_port.sql`

```sql
-- 将所有 9000 端口更新为 9001
UPDATE pms_product 
SET pic = REPLACE(pic, 'localhost:9000', 'localhost:9001')
WHERE pic LIKE '%localhost:9000%';

UPDATE pms_product 
SET pic = REPLACE(pic, 'localhost:9091', 'localhost:9001')
WHERE pic LIKE '%localhost:9091%';
```

**更新结果**：
- ✅ 商品主图：1 条记录（9001端口）
- ✅ 商品相册图：1 条记录（9001端口）
- ✅ 错误端口记录：0 条

---

### 2. 初始化 SQL 脚本修复

**文件**：`document/sql/mall.sql` 第 920 行

```sql
-- 修复前
'http://localhost:9000/mall/20220609/...'

-- 修复后
'http://localhost:9001/mall/20220609/...'
```

---

### 3. 应用配置文件修正

**文件**：`mall-admin/src/main/resources/application-dev.yml`

```yaml
minio:
  endpoint: http://localhost:9001  # ✅ 统一使用 9001
  bucketName: mall
  accessKey: minioadmin
  secretKey: minioadmin
```

---

## 🎯 为什么会出现这个问题？

### 时间线分析

1. **2022年6月**：创建初始化 SQL 脚本，MinIO 运行在 9000 端口
2. **2026年4月25日之前**：某个时间点配置可能还是 9000
3. **2026年4月25日**：上传商品 ID 46，使用了当时的配置（9000端口）
4. **2026年5月3日**：发现问题，修正为 9001 端口

---

## 🔧 预防措施

### 1. 避免硬编码完整 URL

**不推荐**（当前做法）：
```java
minioUploadDto.setUrl(ENDPOINT + "/" + BUCKET_NAME + "/" + objectName);
```

**推荐做法**：
```java
// 只存储相对路径
minioUploadDto.setUrl(BUCKET_NAME + "/" + objectName);

// 前端动态拼接完整 URL
const imageUrl = `${minioEndpoint}/${objectUrl}`;
```

### 2. 使用环境变量

```yaml
minio:
  endpoint: ${MINIO_ENDPOINT:http://localhost:9001}
```

### 3. 数据库迁移脚本

每次修改端口时，必须执行数据迁移脚本：
```sql
-- 示例：9000 → 9001
UPDATE pms_product SET pic = REPLACE(pic, ':9000', ':9001') WHERE pic LIKE '%:9000%';
```

---

## 📝 总结

### 问题根源

| 原因 | 影响 | 修复状态 |
|------|------|----------|
| 初始化 SQL 硬编码 9000 | 测试品牌数据 | ✅ 已修复 |
| 上传时配置为 9000 | 商品 ID 46 | ✅ 已修复 |
| 配置文件历史变更 | 所有新上传文件 | ✅ 已修复 |

### 关键教训

1. **不要硬编码完整 URL**：应该存储相对路径，动态拼接
2. **配置文件变更需同步更新数据**：修改端口必须执行数据迁移
3. **初始化脚本需保持一致**：SQL 脚本中的 URL 应使用变量或最新端口

---

## 🚀 后续建议

1. **重构 MinioController**：只返回相对路径，前端动态拼接
2. **添加配置校验**：启动时检查 MinIO 端口是否可访问
3. **编写迁移脚本**：提供端口变更的自动化迁移工具

---

**分析完成时间**：2026-05-03  
**问题类型**：配置不一致 + 历史数据未同步  
**修复状态**：✅ 已全部修复
