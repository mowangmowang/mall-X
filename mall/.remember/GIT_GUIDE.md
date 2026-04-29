# Mall 项目 Git 管理规范

## 📋 当前 Git 状态

- **主分支**: `master` (稳定版本)
- **开发分支**: `learnning` (学习开发分支，建议改名为 `develop` 或 `learning`)
- **工作目录**: 干净 ✅

## ✅ 已完成的优化

1. **更新了 `.gitignore`**
   - 忽略 IDE 缓存文件 (`.idea/.cache/`)
   - 忽略 Node.js 依赖 (`mall-web/node_modules/`)
   - 忽略 Python 缓存文件
   - 忽略文档文件 (`.docx`, `.pdf`)，但保留 `guidence/` 中的 Markdown 文档

2. **清理了不必要的文件**
   - 移除了 `.idea/.cache/.Apifox_Helper/.toolWindow.db` (IDE 临时数据库)
   - 添加了 `mall-web` 前端项目到版本控制

3. **规范了提交信息**
   - 使用英文提交信息（避免编码问题）
   - 遵循约定式提交规范

## 🎯 Git 最佳实践

### 1. 分支管理策略

```bash
# 查看当前分支
git branch

# 创建新功能分支
git checkout -b feature/user-authentication

# 切换到主分支
git checkout master

# 合并功能分支
git merge feature/user-authentication
```

**推荐分支命名：**
- `master` / `main` - 生产环境稳定版本
- `develop` - 开发主分支
- `feature/xxx` - 功能开发分支
- `bugfix/xxx` - Bug 修复分支
- `hotfix/xxx` - 紧急修复分支

### 2. 提交规范

使用**约定式提交 (Conventional Commits)**：

```bash
# 新功能
git commit -m "feat: add user login functionality"

# Bug 修复
git commit -m "fix: resolve product list pagination issue"

# 文档更新
git commit -m "docs: update API documentation"

# 代码重构
git commit -m "refactor: optimize database query performance"

# 样式调整
git commit -m "style: format code with prettier"

# 测试相关
git commit -m "test: add unit tests for user service"

# 构建/工具链
git commit -m "build: update dependencies"
```

**常用类型前缀：**
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档变更
- `style`: 代码格式（不影响功能）
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具变动
- `perf`: 性能优化

### 3. 日常操作流程

```bash
# 1. 开始工作前，拉取最新代码
git checkout develop
git pull origin develop

# 2. 创建功能分支
git checkout -b feature/your-feature-name

# 3. 开发过程中频繁提交
git add .
git commit -m "feat: implement xxx feature"

# 4. 完成开发后，合并到 develop
git checkout develop
git merge feature/your-feature-name

# 5. 推送到远程仓库
git push origin develop
```

### 4. 查看 Git 状态

```bash
# 查看工作区状态
git status

# 查看提交历史
git log --oneline --graph -10

# 查看具体改动
git diff

# 查看某个文件的修改历史
git log --follow -- filename.java
```

### 5. 撤销操作

```bash
# 撤销工作区的修改（未 add）
git restore <file>

# 取消暂存（已 add，未 commit）
git restore --staged <file>

# 撤销最后一次提交（保留修改）
git reset --soft HEAD~1

# 完全撤销最后一次提交（丢弃修改）⚠️ 谨慎使用
git reset --hard HEAD~1
```

## ⚠️ 注意事项

### 不要提交的文件

以下文件已在 `.gitignore` 中配置，不应被追踪：

- ✅ IDE 配置和缓存：`.idea/`, `*.iml`, `.vscode/`
- ✅ 编译输出：`target/`, `*.class`
- ✅ 依赖包：`node_modules/`, `venv/`
- ✅ 日志文件：`*.log`, `logs/`
- ✅ 系统文件：`.DS_Store`, `Thumbs.db`
- ✅ 文档二进制文件：`*.docx`, `*.pdf`（Markdown 文档除外）

### 应该提交的文件

- ✅ 源代码：`.java`, `.vue`, `.js`, `.ts`
- ✅ 配置文件：`pom.xml`, `package.json`, `.gitignore`
- ✅ 文档：`README.md`, `guidence/*.md`
- ✅ 脚本：`.sh`, `.py`（如果有用途）

## 🔧 常见问题解决

### 1. 误提交了不应该的文件

```bash
# 从 Git 追踪中移除，但保留本地文件
git rm --cached <file>

# 添加到 .gitignore
echo "<file>" >> .gitignore

# 提交更改
git commit -m "chore: remove unwanted files from tracking"
```

### 2. 想重命名分支

```bash
# 重命名当前分支
git branch -m learnning learning

# 或者改为更规范的名称
git branch -m learnning develop
```

### 3. 合并冲突解决

```bash
# 1. 查看冲突文件
git status

# 2. 手动编辑冲突文件，解决冲突标记（<<<<<<<, =======, >>>>>>>）

# 3. 标记冲突已解决
git add <resolved-file>

# 4. 完成合并
git commit -m "merge: resolve conflicts in xxx"
```

### 4. 查看大文件

```bash
# 查看仓库中最大的文件
git rev-list --objects --all | git cat-file --batch-check='%(objecttype) %(objectname) %(objectsize) %(rest)' | sed -n 's/^blob //p' | sort -rnk2 | head -20
```

## 📊 推荐的 Git 工作流

对于毕业设计项目，推荐使用简化的 Git Flow：

```
master (稳定版本，定期合并)
  ↑
develop (主要开发分支)
  ↑
feature/xxx (短期功能分支，完成后删除)
```

**工作流程：**
1. 从 `develop` 创建功能分支
2. 在功能分支上开发和提交
3. 完成后合并回 `develop`
4. 定期将 `develop` 合并到 `master`（里程碑时）

## 🚀 下一步建议

1. **修正分支名称**（可选）
   ```bash
   git branch -m learnning develop
   ```

2. **关联远程仓库**（如果需要备份）
   ```bash
   git remote add origin https://github.com/yourusername/mall.git
   git push -u origin develop
   ```

3. **设置 Git 用户信息**
   ```bash
   git config user.name "Your Name"
   git config user.email "your.email@example.com"
   ```

4. **定期备份**
   - 推送到 GitHub / Gitee
   - 或使用云盘同步整个项目文件夹

---

**最后更新**: 2026-04-24
**当前分支**: learnning
**最新提交**: 0fc2df3 - refactor: optimize gitignore and add mall-web
