# mall-ai 模块配置说明

## 环境变量配置

### 必需的环境变量

mall-ai 模块需要从环境变量中读取 AI API Key，请在使用前设置以下环境变量：

#### 方式一：在操作系统中设置

**Linux/Mac:**
```bash
export AI_API_KEY=your_api_key_here
```

**Windows:**
```cmd
set AI_API_KEY=your_api_key_here
```

#### 方式二：在 IDE 中设置（开发环境）

**IntelliJ IDEA:**
1. 打开 Run → Edit Configurations
2. 找到 MallAiApplication 配置
3. 在 Environment variables 中添加：`AI_API_KEY=your_api_key_here`
4. 点击 Apply 保存

**Eclipse:**
1. 右键项目 → Run As → Run Configurations
2. 选择 Environment 标签
3. 点击 New 添加变量：名称 `AI_API_KEY`，值 `your_api_key_here`

#### 方式三：使用 .env 文件（需要 dotenv 支持）

创建 `.env.local` 文件（已被 .gitignore 排除）：
```env
AI_API_KEY=your_api_key_here
```

### 其他可选配置

可以在 `application.yml` 或 `application-dev.yml` 中配置以下参数：

```yaml
ai:
  client:
    base-url: https://api.deepseek.com/v1  # AI API 基础地址
    model: deepseek-chat                     # 使用的模型
    temperature: 0.7                         # 温度参数 (0-1)
    max-tokens: 1024                         # 最大 token 数
```

## 安全注意事项

1. **永远不要将 API Key 提交到版本控制系统**
2. 生产环境建议使用密钥管理服务（如 AWS Secrets Manager、HashiCorp Vault）
3. 定期轮换 API Key
4. 限制 API Key 的使用范围和配额

## 验证配置是否成功

启动应用后，如果看到以下日志说明配置成功：
```
AI Client configuration validated successfully. Model: deepseek-chat, BaseURL: https://api.deepseek.com/v1
```

如果配置失败，会看到错误信息：
```
AI API Key 未配置，请设置环境变量 AI_API_KEY 或在配置文件中设置 ai.client.api-key
```

## P0 优化完成清单

✅ 已完成的优化：
- [x] API Key 从环境变量读取，不再硬编码
- [x] .gitignore 已更新，排除敏感配置文件
- [x] 添加了配置项非空校验
- [x] 请求参数验证（@Valid + JSR-303 注解）
- [x] 全局异常处理器
- [x] Prompt Injection 攻击防护
- [x] 输入清理和长度限制

## 下一步计划

后续将进行 P1-P3 级别的优化：
- P1: 改进错误处理、配置连接池、添加重试机制
- P2: 使用 Lombok、完善文档、添加缓存、限流保护
- P3: 编写单元测试、优化日志记录
