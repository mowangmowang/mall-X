# MinIO 本地部署配置说明

## 📌 当前配置

### MinIO 部署方式
- **部署方式**：本地直接运行（非 Docker）
- **运行端口**：`9001`
- **访问地址**：`http://localhost:9001`

---

## ✅ 已完成的修复

### 1. 应用配置文件
**文件**：`mall-admin/src/main/resources/application-dev.yml`

```yaml
minio:
  endpoint: http://localhost:9001  # ✅ 本地 MinIO 端口
  bucketName: mall
  accessKey: minioadmin
  secretKey: minioadmin
```

### 2. 数据库修复脚本
**文件**：`document/sql/fix_minio_port.sql`

将所有错误端口（9000、9091）统一更新为 `9001`

---

## 🚀 执行步骤

### 步骤 1：确保 MinIO 运行在 9001 端口

```powershell
# 检查 MinIO 是否在 9001 端口运行
netstat -ano | findstr :9001

# 测试 MinIO 健康状态
curl http://localhost:9001/minio/health/live
# 应返回：OK
```

### 步骤 2：配置存储桶权限

访问：http://localhost:9001  
登录：minioadmin / minioadmin

1. 创建 `mall` 存储桶
2. 设置访问规则：
   - Prefix: `*`
   - Access: `Read`

### 步骤 3：更新数据库图片 URL

```bash
# 连接 MySQL
mysql -h localhost -u root -p

# 执行修复脚本
source D:/course/Java/graduateProject/finish/mall/document/sql/fix_minio_port.sql
```

### 步骤 4：重启后端服务

重启 `mall-admin` 和 `mall-portal` 服务

### 步骤 5：验证

```powershell
# 1. 检查配置
curl http://localhost:9001/minio/health/live

# 2. 检查数据库
mysql -h localhost -u root -p mall -e "SELECT pic FROM pms_product LIMIT 5;"

# 3. 访问前端商品详情页
# 浏览器打开商品详情页，确认图片正常加载
```

---

##  端口对照表

| 配置项 | 端口号 | 说明 |
|--------|--------|------|
| MinIO 本地运行 | 9001 | 本地 MinIO 服务端口 |
| Spring Boot 配置 | 9001 | endpoint 配置 |
| 数据库图片 URL | 9001 | pms_product 表 pic 字段 |
| MinIO Web Console | 9001 | 与 API 共用端口 |

---

## ️ 注意事项

1. **MinIO 新版（2021年后）**：API 和 Console 共用同一端口（9001）
2. **存储桶必须设置公开读取权限**，否则前端无法访问图片
3. **所有配置必须统一使用 9001 端口**

---

## 🔧 故障排查

### 问题 1：无法连接 MinIO

```powershell
# 检查端口占用
netstat -ano | findstr :9001

# 检查 MinIO 日志
# 查看启动 MinIO 的终端输出
```

### 问题 2：图片 403 Forbidden

- 检查存储桶 `mall` 的 Access Rules 是否设置为 `Read`
- 访问 http://localhost:9001 → Buckets → mall → Manage → Access Rules

### 问题 3：图片 404 Not Found

- 检查数据库中图片 URL 是否正确
- 确认 MinIO 中文件是否存在
- 验证 URL 格式：`http://localhost:9001/mall/20260425/xxx.png`

---

##  配置总结

```
MinIO 端口配置（本地部署）：
├── 运行端口：9001
├── 应用配置：http://localhost:9001
├── 数据库 URL：localhost:9001
└── 前端访问：localhost:9001

关键文件：
├── application-dev.yml（端口：9001）
├── fix_minio_port.sql（更新为 9001）
└── MinioController.java（动态读取配置）
```

---

**最后更新**：2026-05-03  
**MinIO 版本**：本地部署（非 Docker）  
**端口**：9001
