# Git 版本控制重构完整教程

## 📋 目录

- [方案选择指南](#方案选择指南)
- [方案一：完全重置 Git 历史（推荐）](#方案一完全重置-git-历史推荐)
- [方案二：保留历史并更改远程仓库](#方案二保留历史并更改远程仓库)
- [方案三：创建新分支断开联系](#方案三创建新分支断开联系)
- [常见问题与解决方案](#常见问题与解决方案)
- [最佳实践建议](#最佳实践建议)

---

## 方案选择指南

### 对比表格

| 方案 | 保留原历史 | 操作复杂度 | 适用场景 | 风险等级 |
|------|-----------|-----------|---------|---------|
| 方案一 | ❌ 不保留 | ⭐ 简单 | 毕业设计、个人项目 | 🟢 低 |
| 方案二 | ✅ 保留 | ⭐⭐ 中等 | 团队协作、需要追溯历史 | 🟡 中 |
| 方案三 | ⚠️ 部分保留 | ⭐⭐ 中等 | 基于开源项目开发新功能 | 🟡 中 |

### 推荐选择

**对于毕业设计或个人项目，强烈推荐方案一**，原因：
1. 干净的提交历史，便于展示自己的工作
2. 避免版权和归属问题
3. 操作简单，不易出错

---

## 方案一：完全重置 Git 历史（推荐）

### 适用场景
- 毕业设计项目
- 个人学习项目
- 想要从零开始记录自己的开发过程

### 操作步骤

#### 步骤 1：备份当前代码（重要！）

```powershell
# 确保所有更改已保存
git status

# 如果有未提交的更改，先提交或暂存
git add .
git commit -m "备份：重置前的最后状态"
```

#### 步骤 2：删除 Git 历史记录

```powershell
# 进入项目根目录
cd D:\course\Java\graduateProject\finish\mall

# 删除 .git 文件夹（这会移除所有 Git 历史）
Remove-Item -Recurse -Force .git

# 验证删除成功
Test-Path .git  # 应返回 False
```

#### 步骤 3：初始化新的 Git 仓库

```powershell
# 初始化新的 Git 仓库
git init

# 查看初始化状态
git status
```

#### 步骤 4：配置 Git 用户信息

```powershell
# 设置用户名（使用您的真实姓名）
git config user.name "您的姓名"

# 设置邮箱（使用您的邮箱）
git config user.email "your-email@example.com"

# 验证配置
git config user.name
git config user.email
```

#### 步骤 5：检查 .gitignore 文件

```powershell
# 查看是否已有 .gitignore 文件
Get-Content .gitignore

# 如果没有，创建标准的 Java/Maven 项目 .gitignore
```

推荐的 `.gitignore` 内容：

```gitignore
# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/timing.properties

# IDE
../.idea/
*.iml
*.iws
*.ipr
.vscode/
.settings/
.classpath
.project
.factorypath

# Eclipse
bin/
tmp/
*.tmp
*.bak
*.swp
*~.nib

# Log files
*.log
logs/

# OS
.DS_Store
Thumbs.db

# Compiled class files
*.class

# Package Files
*.jar
*.war
*.nar
*.ear
*.zip
*.tar.gz
*.rar

# Virtual machine crash logs
hs_err_pid*
replay_pid*
```

#### 步骤 6：添加所有文件并提交

```powershell
# 添加所有文件到暂存区
git add .

# 查看将要提交的文件
git status

# 首次提交
git commit -m "初始提交：Mall 电商系统项目初始化"

# 查看提交历史
git log --oneline
```

#### 步骤 7：创建并推送到新的远程仓库

**在 GitHub/Gitee/GitLab 上创建新仓库：**

1. 访问代码托管平台（如 GitHub、Gitee）
2. 点击 "New Repository" / "新建仓库"
3. 填写仓库名称（如 `mall-graduate-project`）
4. **不要** 初始化 README、.gitignore 或 license
5. 点击 "Create" / "创建"

**将本地仓库关联到远程仓库：**

```powershell
# 添加远程仓库（替换为您的仓库地址）
# GitHub 示例：
git remote add origin https://github.com/your-username/mall-graduate-project.git

# Gitee 示例：
git remote add origin https://gitee.com/your-username/mall-graduate-project.git

# 验证远程仓库
git remote -v

# 推送到远程仓库
git branch -M main
git push -u origin main
```

#### 步骤 8：验证推送结果

```powershell
# 查看远程分支
git branch -r

# 拉取最新代码验证连接
git pull origin main

# 查看完整提交历史
git log --graph --oneline --all
```

---

## 方案二：保留历史并更改远程仓库

### 适用场景
- 需要保留完整的开发历史
- 团队协作者需要查看历史提交
- 基于原有项目进行持续开发

### 操作步骤

#### 步骤 1：查看当前远程仓库

```powershell
# 查看所有远程仓库
git remote -v

# 通常会看到类似：
# origin  https://github.com/original-owner/mall.git (fetch)
# origin  https://github.com/original-owner/mall.git (push)
```

#### 步骤 2：移除原有远程仓库

```powershell
# 移除名为 origin 的远程仓库
git remote remove origin

# 验证移除成功
git remote -v  # 应该没有输出
```

#### 步骤 3：添加新的远程仓库

```powershell
# 添加您的新仓库地址
git remote add origin https://github.com/your-username/mall-graduate-project.git

# 验证添加成功
git remote -v
```

#### 步骤 4：重命名分支（可选）

```powershell
# 如果原仓库使用 master 分支，可以重命名为 main
git branch -m master main

# 或者保持当前分支名不变
git branch  # 查看当前分支
```

#### 步骤 5：推送到新仓库

```powershell
# 推送所有分支到新仓库
git push -u origin --all

# 推送所有标签（如果有）
git push -u origin --tags
```

#### 步骤 6：处理推送冲突（如果出现）

如果远程仓库已有内容：

```powershell
# 方法一：强制推送（谨慎使用，会覆盖远程内容）
git push -u origin main --force

# 方法二：先拉取再合并（推荐）
git pull origin main --allow-unrelated-histories
git push -u origin main
```

---

## 方案三：创建新分支断开联系

### 适用场景
- 想保留原有历史作为参考
- 在新分支上开发自己的功能
- 可能需要与原项目进行对比

### 操作步骤

#### 步骤 1：创建新分支

```powershell
# 从当前分支创建新分支
git checkout -b my-graduate-project

# 或者从特定提交创建
git checkout -b my-graduate-project <commit-hash>
```

#### 步骤 2：重置分支历史（可选）

如果想让新分支从某个点重新开始：

```powershell
# 软重置：保留更改在暂存区
git reset --soft <commit-hash>

# 混合重置：保留更改在工作区（默认）
git reset --mixed <commit-hash>

# 硬重置：丢弃所有更改（危险！）
git reset --hard <commit-hash>
```

#### 步骤 3：添加新的远程仓库

```powershell
# 添加新远程仓库
git remote add my-origin https://github.com/your-username/mall-graduate-project.git

# 或者替换原有远程
git remote set-url origin https://github.com/your-username/mall-graduate-project.git
```

#### 步骤 4：推送到新仓库

```powershell
# 推送新分支
git push -u my-origin my-graduate-project
```

---

## 常见问题与解决方案

### 问题 1：推送时出现 "rejected" 错误

**原因**：远程仓库已有内容，与本地历史冲突

**解决方案**：

```powershell
# 方法一：先拉取合并
git pull origin main --allow-unrelated-histories
git push -u origin main

# 方法二：强制推送（仅在确认安全时使用）
git push -u origin main --force
```

### 问题 2：大文件导致推送失败

**原因**：项目中包含过大的文件（如数据库文件、视频等）

**解决方案**：

```powershell
# 1. 找出大文件
git rev-list --objects --all | grep "$(git verify-pack -v .git/objects/pack/*.idx | sort -k 3 -n | tail -5 | awk '{print $1}')"

# 2. 从 Git 历史中移除大文件
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch path/to/large-file' \
  --prune-empty HEAD

# 3. 清理并重新推送
git for-each-ref --format='delete %(refname)' refs/original | git update-ref --stdin
git reflog expire --expire=now --all
git gc --prune=now
git push -u origin main --force
```

**更好的方案**：使用 `.gitignore` 排除大文件类型

```gitignore
# 数据库文件
*.sql
*.db

# 压缩文件
*.zip
*.rar
*.7z

# 视频文件
*.mp4
*.avi
```

### 问题 3：敏感信息泄露（密码、密钥等）

**紧急处理**：

```powershell
# 1. 立即更改泄露的密码/密钥

# 2. 从 Git 历史中移除敏感文件
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch config/application.yml' \
  --prune-empty HEAD

# 3. 添加到 .gitignore
echo "config/application.yml" >> .gitignore

# 4. 强制推送到远程
git push -u origin main --force
```

**预防措施**：

```gitignore
# 配置文件
application*.yml
application*.properties
*.env

# 密钥文件
*.pem
*.key
*.p12
```

### 问题 4：权限被拒绝（Permission denied）

**原因**：SSH 密钥未配置或访问令牌无效

**解决方案**：

```powershell
# 方法一：使用 HTTPS + Personal Access Token
# 在 GitHub/Gitee 生成 Token，然后：
git remote set-url origin https://<token>@github.com/your-username/repo.git

# 方法二：配置 SSH 密钥
ssh-keygen -t ed25519 -C "your-email@example.com"
# 将公钥添加到 GitHub/Gitee
cat ~/.ssh/id_ed25519.pub

# 测试 SSH 连接
ssh -T git@github.com
```

### 问题 5：中文文件名乱码

**解决方案**：

```powershell
# 配置 Git 正确处理中文
git config core.quotepath false
git config i18n.commitencoding utf-8
git config i18n.logoutputencoding utf-8
```

---

## 最佳实践建议

### 1. 提交规范（Commit Convention）

采用语义化提交信息格式：

```
<type>(<scope>): <subject>

<body>

<footer>
```

**常用类型（Type）**：
- `feat`: 新功能
- `fix`: 修复 Bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具变动

**示例**：

```powershell
git commit -m "feat(user): 添加用户注册功能

- 实现邮箱验证
- 添加密码强度检查
- 完成用户信息持久化

Closes #123"
```

### 2. 分支管理策略

```
main/master          # 主分支，保持稳定
├── develop          # 开发分支
├── feature/login    # 功能分支
├── feature/order    # 功能分支
└── hotfix/bug-123   # 紧急修复分支
```

**操作流程**：

```powershell
# 创建功能分支
git checkout -b feature/user-authentication

# 开发完成后合并到 develop
git checkout develop
git merge feature/user-authentication

# 发布时合并到 main
git checkout main
git merge develop
```

### 3. 定期备份

```powershell
# 添加多个远程仓库作为备份
git remote add backup https://gitee.com/your-username/mall-backup.git

# 推送到所有远程
git push origin main
git push backup main
```

### 4. 标签管理（版本发布）

```powershell
# 创建标签
git tag -a v1.0.0 -m "第一个正式版本"

# 推送标签
git push origin v1.0.0

# 查看所有标签
git tag -l
```

### 5. 有用的 Git 别名配置

```powershell
# 配置常用别名
git config alias.st status
git config alias.co checkout
git config alias.br branch
git config alias.ci commit
git config alias.lg "log --graph --oneline --all"

# 使用别名
git st      # 等同于 git status
git lg      # 查看图形化日志
```

### 6. 毕业项目特别建议

#### 6.1 阶段性提交

按照开发阶段组织提交：

```powershell
# 第一阶段：环境搭建
git commit -m "chore: 完成项目环境搭建和依赖配置"

# 第二阶段：用户模块
git commit -m "feat: 完成用户注册登录功能"

# 第三阶段：商品模块
git commit -m "feat: 实现商品浏览和搜索功能"

# 第四阶段：订单模块
git commit -m "feat: 完成订单创建和管理功能"
```

#### 6.2 文档同步

每次重要更新时同步文档：

```powershell
git add README.md docs/
git commit -m "docs: 更新项目文档和用户手册"
```

#### 6.3 演示版本标记

```powershell
# 为答辩演示创建专门标签
git tag -a demo-v1.0 -m "毕业答辩演示版本"
git push origin demo-v1.0
```

---

## 快速参考命令清单

### 初始化新仓库

```powershell
Remove-Item -Recurse -Force .git
git init
git config user.name "Your Name"
git config user.email "your-email@example.com"
git add .
git commit -m "初始提交"
git remote add origin <your-repo-url>
git branch -M main
git push -u origin main
```

### 更改远程仓库

```powershell
git remote -v
git remote remove origin
git remote add origin <new-repo-url>
git push -u origin main
```

### 日常操作

```powershell
# 查看状态
git status

# 添加文件
git add .

# 提交
git commit -m "描述信息"

# 推送
git push origin main

# 拉取
git pull origin main

# 查看历史
git log --oneline
```

---

## 总结

### 方案选择决策树

```
是否需要保留原提交历史？
├─ 否 → 选择方案一（完全重置）✅ 推荐
│        └─ 适合毕业设计、个人项目
│
└─ 是 → 是否需要与原项目保持联系？
         ├─ 否 → 选择方案二（更换远程）
         │        └─ 适合独立继续开发
         │
         └─ 是 → 选择方案三（新分支）
                  └─ 适合同步上游更新
```

### 关键注意事项

1. ⚠️ **操作前务必备份代码**
2. ⚠️ **检查是否有敏感信息（密码、密钥）**
3. ⚠️ **确认 .gitignore 配置正确**
4. ⚠️ **推送前先在本地验证提交历史**
5. ✅ **遵循规范的提交信息格式**
6. ✅ **定期推送到远程仓库备份**
7. ✅ **为重要节点创建标签**

### 下一步行动

1. 根据您的需求选择合适的方案
2. 按照对应方案的步骤操作
3. 遇到问题参考"常见问题"章节
4. 遵循"最佳实践"进行后续开发

---

## 附录：Git 学习资源

- [Git 官方文档](https://git-scm.com/doc)
- [Pro Git 中文版](https://git-scm.com/book/zh/v2)
- [GitHub 入门指南](https://docs.github.com/cn/get-started)
- [Gitee 帮助文档](https://help.gitee.com/)

---

**祝您重构顺利！如有问题，欢迎随时咨询。** 🎓
