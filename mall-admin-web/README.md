# mall-admin-web

电商后台管理系统前端项目，基于 Vue3 + TypeScript + Element Plus 构建。

## 技术架构

### 技术栈

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 核心框架 | Vue 3 | ^3.5.25 | 组合式 API |
| 类型系统 | TypeScript | ~5.9.0 | 静态类型检查 |
| UI 框架 | Element Plus | ^2.12.0 | 组件库 |
| 构建工具 | Vite | ^7.2.4 | 快速开发服务器 |
| 状态管理 | Pinia | ^3.0.4 | 全局状态管理 |
| 路由管理 | Vue Router | ^4.6.3 | 客户端路由 |
| HTTP 客户端 | Axios | ^1.13.2 | API 请求封装 |
| 数据可视化 | ECharts | ^6.0.0 | 图表库 |
| 富文本编辑器 | TinyMCE | ^6.8.6 | 内容编辑 |
| CSS 预处理 | Sass | ^1.96.0 | 样式编译 |
| 代码规范 | ESLint + Prettier | - | 代码质量保障 |

### 架构设计

```mermaid
graph TB
    subgraph "表现层 (Presentation Layer)"
        Views[视图组件<br/>views/]
        Components[通用组件<br/>components/]
        Layout[布局组件<br/>layout/]
    end
    
    subgraph "业务逻辑层 (Business Logic Layer)"
        Stores[状态管理<br/>stores/]
        Router[路由管理<br/>router/]
        Composables[组合式函数<br/>composables/]
    end
    
    subgraph "数据访问层 (Data Access Layer)"
        APIs[API 接口封装<br/>apis/]
        Request[HTTP 请求拦截器<br/>utils/request.ts]
        Utils[工具函数<br/>utils/]
    end
    
    subgraph "基础设施层 (Infrastructure Layer)"
        ElementPlus[Element Plus 组件库]
        ECharts[ECharts 图表库]
        TinyMCE[TinyMCE 编辑器]
        Pinia[PersistedState 持久化]
    end
    
    Views --> Stores
    Components --> Stores
    Layout --> Router
    Stores --> APIs
    APIs --> Request
    Request --> ElementPlus
    Views --> ECharts
    Views --> TinyMCE
    Stores --> Pinia
```

### 目录结构

```
mall-admin-web/
├── src/
│   ├── apis/              # API 接口定义（按模块划分）
│   │   ├── product.ts     # 商品相关接口
│   │   ├── order.ts       # 订单相关接口
│   │   ├── member.ts      # 会员相关接口
│   │   └── ...
│   ├── assets/            # 静态资源
│   ├── components/        # 通用组件
│   │   ├── SvgIcon/       # SVG 图标组件
│   │   ├── Tinymce/       # 富文本编辑器封装
│   │   └── ...
│   ├── icons/             # SVG 图标文件
│   ├── router/            # 路由配置
│   │   ├── index.ts       # 路由主配置
│   │   └── modules/       # 路由模块
│   ├── stores/            # Pinia 状态管理
│   │   ├── user.ts        # 用户状态
│   │   ├── permission.ts  # 权限状态
│   │   └── app.ts         # 应用状态
│   ├── styles/            # 全局样式
│   │   ├── element/       # Element Plus 主题定制
│   │   └── var.scss       # SCSS 变量
│   ├── types/             # TypeScript 类型定义
│   ├── utils/             # 工具函数
│   │   ├── request.ts     # Axios 封装
│   │   ├── auth.ts        # 认证工具
│   │   └── validate.ts    # 表单验证
│   ├── views/             # 页面组件
│   │   ├── home/          # 首页
│   │   ├── pms/           # 商品管理 (Product)
│   │   ├── oms/           # 订单管理 (Order)
│   │   ├── sms/           # 营销管理 (Sale)
│   │   ├── ums/           # 用户管理 (User)
│   │   └── layout/        # 布局框架
│   ├── App.vue            # 根组件
│   └── main.ts            # 入口文件
├── .env.development       # 开发环境变量
├── .env.production        # 生产环境变量
├── vite.config.ts         # Vite 配置
├── tsconfig.json          # TypeScript 配置
└── package.json           # 项目依赖
```

### 核心模块说明

#### 1. 请求封装层 (Request Layer)

```mermaid
sequenceDiagram
    participant Component as 组件
    participant API as API 模块
    participant Interceptor as 请求拦截器
    participant Server as 后端服务
    
    Component->>API: 调用接口方法
    API->>Interceptor: 发起 HTTP 请求
    Interceptor->>Interceptor: 添加 Token
    Interceptor->>Server: 发送请求
    Server-->>Interceptor: 返回响应
    Interceptor->>Interceptor: 统一错误处理
    Interceptor-->>API: 返回数据
    API-->>Component: 返回结果
```

**特性：**
- 统一的请求/响应拦截器
- 自动携带 JWT Token
- 统一的错误处理和提示
- 请求超时控制
- 响应数据格式化

#### 2. 状态管理 (State Management)

```mermaid
graph LR
    subgraph "Pinia Stores"
        UserStore[用户状态<br/>- token<br/>- userInfo<br/>- permissions]
        PermissionStore[权限状态<br/>- routes<br/>- menus<br/>- buttons]
        AppStore[应用状态<br/>- sidebar<br/>- theme<br/>- tagsView]
    end
    
    subgraph "持久化"
        LocalStorage[(LocalStorage)]
    end
    
    UserStore --> LocalStorage
    PermissionStore --> LocalStorage
    AppStore --> LocalStorage
```

**使用示例：**
```typescript
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    userInfo: null as UserInfo | null,
    permissions: [] as string[]
  }),
  
  actions: {
    async login(username: string, password: string) {
      const res = await loginApi({ username, password })
      this.token = res.data.token
      await this.getUserInfo()
    },
    
    async getUserInfo() {
      const res = await getInfoApi()
      this.userInfo = res.data
      this.permissions = res.data.permissions
    }
  },
  
  persist: true // 启用持久化
})
```

#### 3. 路由与权限控制

```mermaid
flowchart TD
    Start[访问页面] --> CheckToken{是否有 Token?}
    CheckToken -->|否| RedirectLogin[重定向到登录页]
    CheckToken -->|是| GetUserInfo[获取用户信息]
    GetUserInfo --> GetPermissions[获取权限列表]
    GetPermissions --> FilterRoutes[过滤动态路由]
    FilterRoutes --> AddRoutes[动态添加路由]
    AddRoutes --> RenderPage[渲染页面]
    
    style CheckToken fill:#f9f,stroke:#333
    style FilterRoutes fill:#ff9,stroke:#333
```

**权限控制策略：**
- **路由级权限**：根据用户角色动态生成可访问路由
- **按钮级权限**：通过自定义指令 `v-permission` 控制按钮显示
- **菜单级权限**：后端返回菜单树，前端动态渲染

#### 4. 组件自动导入机制

```mermaid
graph TB
    subgraph "自动导入流程"
        Source[源代码] --> AutoImport[unplugin-auto-import]
        Source --> AutoComponents[unplugin-vue-components]
        
        AutoImport --> VueAPI[Vue API<br/>ref, reactive, computed...]
        AutoImport --> PiniaAPI[Pinia API<br/>defineStore...]
        
        AutoComponents --> ElementPlus[Element Plus 组件<br/>el-button, el-table...]
        AutoComponents --> CustomComponents[自定义组件<br/>SvgIcon, Tinymce...]
    end
    
    VueAPI --> Runtime[运行时]
    PiniaAPI --> Runtime
    ElementPlus --> Runtime
    CustomComponents --> Runtime
```

**优势：**
- 无需手动导入 Vue API 和常用工具函数
- Element Plus 组件按需自动导入
- 减少样板代码，提升开发效率

### 功能模块架构

```mermaid
graph TB
    subgraph "PMS 商品管理"
        Product[商品管理]
        Brand[品牌管理]
        Category[分类管理]
        Attr[属性管理]
    end
    
    subgraph "OMS 订单管理"
        Order[订单列表]
        OrderDetail[订单详情]
        ReturnApply[退货申请]
    end
    
    subgraph "SMS 营销管理"
        Coupon[优惠券]
        Advertise[广告管理]
        HotProduct[热门商品]
        NewProduct[新品推荐]
        Subject[专题管理]
    end
    
    subgraph "UMS 用户管理"
        Admin[管理员]
        Role[角色管理]
        Menu[菜单管理]
        Resource[资源管理]
    end
    
    Product --> Brand
    Product --> Category
    Product --> Attr
    
    Order --> OrderDetail
    Order --> ReturnApply
```

### 构建与部署

```mermaid
flowchart LR
    Dev[开发环境<br/>npm run dev] --> ViteDev[Vite Dev Server<br/>HMR 热更新]
    
    Build[生产构建<br/>npm run build] --> TypeCheck[TypeScript 检查]
    TypeCheck --> ViteBuild[Vite Build]
    ViteBuild --> Minify[代码压缩]
    Minify --> Optimize[资源优化]
    Optimize --> Dist[dist 目录]
    
    Dist --> Deploy[部署到 Nginx/CDN]
```

**构建命令：**
```bash
# 开发环境
npm run dev              # 启动开发服务器（端口 5173）

# 生产构建
npm run build            # 类型检查 + 构建
npm run build-only       # 仅构建（无类型检查）
npm run type-check       # 仅类型检查

# 其他
npm run preview          # 预览生产构建
npm run lint             # ESLint 代码检查并修复
```

**环境变量：**
```bash
# .env.development
VITE_BASE_SERVER_URL=http://localhost:8080

# .env.production
VITE_BASE_SERVER_URL=https://your-api-domain.com
```

### 性能优化策略

```mermaid
graph TB
    subgraph "加载优化"
        CodeSplitting[代码分割<br/>路由懒加载]
        TreeShaking[Tree Shaking<br/>移除未使用代码]
        Minification[代码压缩<br/>Terser]
    end
    
    subgraph "运行时优化"
        KeepAlive[Keep-Alive 缓存]
        VirtualScroll[虚拟滚动<br/>大数据列表]
        Debounce[防抖节流<br/>频繁操作]
    end
    
    subgraph "资源优化"
        ImageOptimize[图片优化<br/>WebP 格式]
        CDN[CDN 加速<br/>静态资源]
        Gzip[Gzip 压缩<br/>Nginx 配置]
    end
    
    CodeSplitting --> Performance[性能提升]
    TreeShaking --> Performance
    Minification --> Performance
    KeepAlive --> Performance
    VirtualScroll --> Performance
    Debounce --> Performance
    ImageOptimize --> Performance
    CDN --> Performance
    Gzip --> Performance
```

### 开发规范

#### TypeScript 规范
```typescript
// ✅ 推荐：明确定义类型
interface Product {
  id: number
  name: string
  price: number
}

// ❌ 避免：使用 any
const data: any = {}
```

#### 组件命名规范
```
✅ PascalCase: ProductList.vue, OrderDetail.vue
✅ 多单词组成，避免单个单词
✅ 基础组件以 Base 前缀：BaseButton.vue
```

#### API 组织规范
```typescript
// apis/product.ts
import request from '@/utils/request'

export interface ProductQuery {
  pageNum: number
  pageSize: number
  productName?: string
}

export function getProductList(params: ProductQuery) {
  return request<ProductListResponse>({
    url: '/product/list',
    method: 'get',
    params
  })
}
```

## 快速开始

### 环境要求
- Node.js >= 20.19.0
- npm >= 10.0.0

### 安装与启动
```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 访问 http://localhost:5173
```

### 后端对接
- 本地开发：修改 `.env.development` 中的 `VITE_BASE_SERVER_URL` 为后端服务地址
- 生产环境：修改 `.env.production` 中的 `VITE_BASE_SERVER_URL` 为生产 API 地址

## 许可证

Apache License 2.0
