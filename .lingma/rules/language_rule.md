---
trigger: always_on
---
# 语言偏好与术语规范 (Language Preference & Terminology Standards)

## 1. 核心原则 (Core Principles)
在所有的代码生成、注释编写、文档撰写及日常交流中，请严格遵循**“中文为主，英文为辅”**的双语策略。旨在确保信息传达的准确性与专业性，同时保持阅读的自然流畅。

## 2. 术语处理规范 (Terminology Handling)

### 2.1 专业名词 (Technical Nouns)
对于计算机科学、软件工程及特定业务领域的**核心专业名词**，必须采用 **“中文名称 (English Term)”** 的格式首次出现，后续可酌情简化。
*   **例外情况**：对于行业内极度通用、无歧义的基础通识术语（如 Database, Server, API, HTTP 等），可直接使用中文或英文，无需强制标注英文原文，以保持文档简洁。
*   **示例**：
    *   ✅ 正确：使用 **依赖注入 (Dependency Injection)** 来解耦模块。
    *   ✅ 正确：配置 **反向代理 (Reverse Proxy)** 以优化负载。
    *   ✅ 正确：连接数据库 (Database) 并执行查询。（基础通识术语，无需强制标注）
    *   ❌ 错误：使用 DI 来解耦。（除非上下文已明确定义缩写）
    *   ❌ 错误：使用 Dependency Injection 来解耦。（缺乏中文主导）

### 2.2 通用动词与形容词 (General Verbs & Adjectives)
对于通用的操作、状态描述，优先使用自然流畅的**中文**。若英文术语在行业内具有极高的通用性且无歧义，可直接使用英文，但需保持语境一致。
*   **示例**：
    *   ✅ 正确：**初始化 (Initialize)** 数据库连接池。
    *   ✅ 正确：对列表进行 **排序 (Sort)**。
    *   ✅ 正确：执行 **CRUD** 操作。（行业通用缩写，可接受）

### 2.3 代码实体 (Code Entities)
*   **变量名/函数名/类名**：严格保持代码中的原始 **英文** 命名，严禁翻译代码标识符。
*   **注释中的引用**：当在注释中提及具体代码实体时，保留英文原名，并辅以中文解释。
    *   **示例**：`// 调用 UserService 的 getUserById 方法获取用户信息`

## 3. 代码注释规范 (Code Comment Standards)

### 3.1 类与方法文档 (Javadoc/Docstrings)
*   **摘要行**：使用简洁的中文概括功能。
*   **详细描述**：使用中文阐述逻辑、参数含义及返回值。
*   **标签内容**：`@param`, `@return`, `@throws` 后的描述使用中文。
    /**
     * 根据用户ID查询用户详细信息
     * 
     * @param userId 用户唯一标识符 (User ID)
     * @return 用户对象 (User Object)，若不存在则返回 null
     * @throws DataAccessException 当数据库访问失败时抛出
     */
    public User findUserById(Long userId) { ... }

### 3.2 行内注释 (Inline Comments)
*   用于解释复杂逻辑或“为什么这样做”（Why），而非“做了什么”（What）。
*   保持简短，使用中文。
    // 重试机制：防止网络波动导致的临时失败
    for (int i = 0; i < MAX_RETRIES; i++) { ... }

## 4. 异常与日志 (Exceptions & Logging)

*   **异常消息 (Exception Messages)**：建议保留 **英文**，以便于全球通用的日志分析工具处理和堆栈跟踪标准化。
*   **日志描述 (Log Descriptions)**：
    *   **INFO/WARN**：可使用中文，便于开发人员快速定位业务问题。
    *   **ERROR**：关键错误信息建议包含英文错误码或标准术语，辅以中文说明。
    *   **示例**：
        log.error("订单创建失败 (Order Creation Failed): orderId={}, reason={}", orderId, e.getMessage());

## 5. 沟通风格 (Communication Style)

*   在回答用户问题时，先给出中文结论或步骤，关键技术点附带英文原文。
*   避免中英混杂的句子结构（如：“请你 check 一下这个 bug”），应改为：“请 **检查 (Check)** 这个 **缺陷 (Bug)**”。

---

**总结示例 (Summary Example):**

> “我们需要优化 **缓存策略 (Caching Strategy)**。建议引入 **Redis** 作为 **分布式缓存 (Distributed Cache)**，以减少数据库的 **查询负载 (Query Load)**。请注意处理 **缓存穿透 (Cache Penetration)** 和 **缓存雪崩 (Cache Avalanche)** 问题。”

