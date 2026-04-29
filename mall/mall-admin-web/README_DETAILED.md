# Mall Admin Web - 电商后台管理系统

<p>
  <a href="#在线演示"><img src="http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/badge/%E5%9C%A8%E7%BA%BF%E6%BC%94%E7%A4%BA-blue.svg" alt="在线演示"></a>
  <a href="#技术栈"><img src="http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/badge/Vue3-ElementPlus-brightgreen.svg" alt="技术栈"></a>
  <a href="#许可证"><img src="http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/badge/Apache2.0-orange.svg" alt="许可证"></a>
</p>

## 📖 项目简介

**mall-admin-web** 是一款基于 **Vue 3 + TypeScript + Element Plus** 开发的现代化电商后台管理系统前端项目。作为 `mall` 系列项目的前端部分，它提供了一套完整、高效、易用的电商管理解决方案。

### 🎯 核心特性

- ✨ **现代化技术栈**：采用 Vue 3.5 + TypeScript 5.9 + Vite 7.2，享受最新的开发体验
- 🎨 **精美 UI 设计**：基于 Element Plus 2.12，提供丰富的组件和优雅的交互
- 🔐 **完善的权限管理**：支持动态路由、角色权限控制、菜单资源管理
- 📦 **自动化工程化**：组件自动导入、SVG 图标系统、代码规范检查
- 🚀 **高性能构建**：Vite 极速开发服务器，优化的生产构建配置
- 📱 **响应式布局**：适配不同屏幕尺寸，提供良好的多端体验
- 🌐 **国际化支持**：内置中文语言包，易于扩展多语言
- 📊 **数据可视化**：集成 ECharts，提供直观的数据展示

### 🔗 相关项目

- **后端项目 (Spring Boot)**: [mall](https://github.com/macrozheng/mall)
- **微服务版本 (Spring Cloud)**: [mall-swarm](https://github.com/macrozheng/mall-swarm)
- **移动端 H5**: [mall-app-web](https://github.com/macrozheng/mall-app-web)
- **完整学习教程**: [mall 学习教程](https://www.macrozheng.com)

---

## 🌐 在线演示

- **演示地址**: [https://www.macrozheng.com/admin/](https://www.macrozheng.com/admin/)
- **测试账号**: 
  - 用户名: `admin`
  - 密码: `123456`

![系统演示](http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/project/mall_admin_show.png)

---

## 🛠️ 技术栈

### 核心框架

| 技术 | 版本 | 说明 | 官方文档 |
|------|------|------|----------|
| Vue | 3.5.25 | 渐进式 JavaScript 框架 | [Vue.js](https://cn.vuejs.org/) |
| TypeScript | 5.9.0 | JavaScript 的超集，提供类型支持 | [TypeScript](https://www.typescriptlang.org/) |
| Vite | 7.2.4 | 下一代前端构建工具 | [Vite](https://vite.dev/) |
| Element Plus | 2.12.0 | 基于 Vue 3 的组件库 | [Element Plus](https://element-plus.org/) |

### 路由与状态管理

| 技术 | 版本 | 说明 | 官方文档 |
|------|------|------|----------|
| Vue Router | 4.6.3 | Vue.js 官方路由管理器 | [Vue Router](https://router.vuejs.org/) |
| Pinia | 3.0.4 | Vue 官方推荐的状态管理库 | [Pinia](https://pinia.vuejs.org/) |
| Pinia Plugin Persistedstate | 4.7.1 | Pinia 持久化插件 | [文档](https://prazdevs.github.io/pinia-plugin-persistedstate/) |

### HTTP 与工具库

| 技术 | 版本 | 说明 | 官方文档 |
|------|------|------|----------|
| Axios | 1.13.2 | 基于 Promise 的 HTTP 客户端 | [Axios](https://axios-http.com/) |
| JS Cookie | 3.0.5 | 轻量级 Cookie 操作库 | [JS Cookie](https://github.com/js-cookie/js-cookie) |
| Normalize.css | 8.0.1 | CSS 重置库，统一浏览器默认样式 | [Normalize.css](https://necolas.github.io/normalize.css/) |
| NProgress | 0.2.0 | 页面加载进度条 | [NProgress](https://ricostacruz.com/nprogress/) |

### 富文本与图表

| 技术 | 版本 | 说明 | 官方文档 |
|------|------|------|----------|
| TinyMCE | 6.8.6 | 强大的富文本编辑器 | [TinyMCE](https://www.tiny.cloud/) |
| TinyMCE Vue | 5.1.1 | TinyMCE 的 Vue 组件封装 | [文档](https://github.com/tinymce/tinymce-vue) |
| ECharts | 6.0.0 | 强大的数据可视化图表库 | [ECharts](https://echarts.apache.org/) |
| Vue ECharts | 8.0.1 | ECharts 的 Vue 组件封装 | [Vue ECharts](https://github.com/ecomfe/vue-echarts) |

### 开发工具与插件

| 技术 | 版本 | 说明 |
|------|------|------|
| unplugin-auto-import | 20.3.0 | API 自动导入（无需手动 import） |
| unplugin-vue-components | 30.0.0 | 组件自动导入 |
| unplugin-element-plus | 0.11.1 | Element Plus 按需引入 |
| vite-plugin-svg-icons | 2.0.1 | SVG 图标管理插件 |
| vite-plugin-vue-devtools | 8.0.5 | Vue DevTools 支持 |
| ESLint | 9.39.1 | 代码质量检查 |
| Prettier | 3.7.4 | 代码格式化工具 |
| Sass | 1.96.0 | CSS 预处理器 |

---

## 📂 项目结构

```
mall-admin-web/
├── public/                          # 静态资源目录
│   ├── tinymce6.8.6/               # TinyMCE 富文本编辑器资源
│   │   ├── icons/                  # 图标资源
│   │   ├── langs/                  # 语言包（含中文）
│   │   ├── plugins/                # 插件
│   │   ├── skins/                  # 主题皮肤
│   │   └── tinymce.min.js          # 核心文件
│   └── favicon.ico                 # 网站图标
│
├── src/                            # 源代码目录
│   ├── apis/                       # API 接口定义（29个模块）
│   │   ├── admin.ts               # 管理员接口
│   │   ├── product.ts             # 商品接口
│   │   ├── order.ts               # 订单接口
│   │   ├── brand.ts               # 品牌接口
│   │   ├── coupon.ts              # 优惠券接口
│   │   ├── flash.ts               # 秒杀接口
│   │   ├── menu.ts                # 菜单接口
│   │   ├── role.ts                # 角色接口
│   │   └── ...                    # 其他业务接口
│   │
│   ├── assets/images/             # 静态图片资源
│   │   ├── 404.png               # 404页面图片
│   │   ├── login_center_bg.png   # 登录页背景
│   │   ├── home_order.png        # 首页订单图标
│   │   └── ...                   # 其他图片
│   │
│   ├── components/                # 通用组件
│   │   ├── Breadcrumb/           # 面包屑导航组件
│   │   ├── Hamburger/            # 汉堡菜单按钮
│   │   ├── ScrollBar/            # 自定义滚动条
│   │   ├── SvgIcon/              # SVG 图标组件
│   │   ├── Tinymce/              # 富文本编辑器组件
│   │   └── Upload/               # 上传组件
│   │       ├── singleUpload.vue  # 单图上传
│   │       └── multiUpload.vue   # 多图上传
│   │
│   ├── icons/                     # SVG 图标库
│   │   ├── svg/                  # SVG 图标文件（40+图标）
│   │   │   ├── dashboard.svg     # 仪表盘图标
│   │   │   ├── product.svg       # 商品图标
│   │   │   ├── order.svg         # 订单图标
│   │   │   ├── ums.svg           # 用户管理图标
│   │   │   └── ...              # 其他图标
│   │   └── index.ts              # 图标注册入口
│   │
│   ├── router/                    # 路由配置
│   │   ├── index.ts              # 路由定义（常量路由+异步路由）
│   │   └── guard.ts              # 路由守卫（权限控制）
│   │
│   ├── stores/                    # Pinia 状态管理
│   │   ├── app.ts                # 应用全局状态（侧边栏等）
│   │   ├── user.ts               # 用户信息状态
│   │   ├── permission.ts         # 权限状态（动态路由）
│   │   ├── order.ts              # 订单状态
│   │   └── counter.ts            # 示例状态
│   │
│   ├── styles/                    # 全局样式
│   │   ├── element/              # Element Plus 主题定制
│   │   │   └── index.scss        # 主题色覆盖配置
│   │   ├── index.scss            # 全局样式入口
│   │   ├── variables.scss        # SCSS 变量定义
│   │   ├── var.scss              # CSS 变量定义
│   │   ├── sidebar.scss          # 侧边栏样式
│   │   ├── transition.scss       # 过渡动画样式
│   │   └── mixin.scss            # SCSS 混合宏
│   │
│   ├── types/                     # TypeScript 类型定义（27个类型文件）
│   │   ├── common.d.ts           # 通用类型（API响应等）
│   │   ├── product.d.ts          # 商品类型
│   │   ├── order.d.ts            # 订单类型
│   │   ├── admin.d.ts            # 管理员类型
│   │   ├── menu.d.ts             # 菜单类型
│   │   ├── role.d.ts             # 角色类型
│   │   └── ...                   # 其他业务类型
│   │
│   ├── utils/                     # 工具函数
│   │   ├── http.ts               # Axios 封装（请求/响应拦截器）
│   │   ├── cookie.ts             # Cookie 操作工具
│   │   ├── validate.ts           # 表单验证工具
│   │   ├── datetime.ts           # 日期时间处理
│   │   └── constant.ts           # 常量定义
│   │
│   ├── views/                     # 页面视图
│   │   ├── home/                 # 首页（仪表盘）
│   │   │   └── index.vue         # 数据统计展示
│   │   │
│   │   ├── layout/               # 布局组件
│   │   │   ├── Layout.vue        # 主布局框架
│   │   │   ├── components/       # 布局子组件
│   │   │   │   ├── AppMain.vue   # 主内容区
│   │   │   │   ├── Navbar.vue    # 顶部导航栏
│   │   │   │   └── Sidebar/      # 侧边栏
│   │   │   └── composables/      # 组合式函数
│   │   │
│   │   ├── normal/               # 通用页面
│   │   │   ├── login/            # 登录页
│   │   │   ├── 404/              # 404错误页
│   │   │   └── link/             # 外链页面
│   │   │
│   │   ├── pms/                  # 商品管理模块 (Product Management)
│   │   │   ├── product/          # 商品列表
│   │   │   │   ├── index.vue     # 商品列表页
│   │   │   │   ├── add.vue       # 添加商品
│   │   │   │   └── update.vue    # 编辑商品
│   │   │   ├── productCate/      # 商品分类
│   │   │   ├── productAttr/      # 商品属性
│   │   │   └── brand/            # 品牌管理
│   │   │
│   │   ├── oms/                  # 订单管理模块 (Order Management)
│   │   │   ├── order/            # 订单管理
│   │   │   │   ├── index.vue     # 订单列表
│   │   │   │   ├── orderDetail.vue  # 订单详情
│   │   │   │   ├── deliverOrderList.vue  # 发货列表
│   │   │   │   └── setting.vue   # 订单设置
│   │   │   └── apply/            # 退货申请
│   │   │       ├── index.vue     # 退货列表
│   │   │       ├── reason.vue    # 退货原因
│   │   │       └── applyDetail.vue  # 退货详情
│   │   │
│   │   ├── sms/                  # 营销管理模块 (Sales Management)
│   │   │   ├── flash/            # 秒杀活动
│   │   │   ├── coupon/           # 优惠券管理
│   │   │   ├── brand/            # 品牌推荐
│   │   │   ├── new/              # 新品推荐
│   │   │   ├── hot/              # 人气推荐
│   │   │   ├── subject/          # 专题推荐
│   │   │   └── advertise/        # 广告管理
│   │   │
│   │   ├── ums/                  # 权限管理模块 (User Management)
│   │   │   ├── admin/            # 管理员管理
│   │   │   ├── role/             # 角色管理
│   │   │   ├── menu/             # 菜单管理
│   │   │   └── resource/         # 资源管理
│   │   │
│   │   └── test/                 # 组件测试页（开发用）
│   │
│   ├── App.vue                    # 根组件
│   └── main.ts                    # 应用入口文件
│
├── .env                           # 环境变量（通用）
├── .env.development               # 开发环境变量
├── .env.production                # 生产环境变量
├── vite.config.ts                 # Vite 配置文件
├── tsconfig.json                  # TypeScript 配置
├── package.json                   # 项目依赖配置
├── eslint.config.ts               # ESLint 配置
├── .prettierrc                    # Prettier 配置
└── README.md                      # 项目说明文档
```

---

## 🚀 快速开始

### 环境要求

在开始之前，请确保您的开发环境满足以下要求：

- **Node.js**: `^20.19.0` 或 `>=22.12.0`（推荐使用 v20 LTS 版本）
- **npm**: `>= 10.0.0`（随 Node.js 一起安装）
- **Git**: 用于克隆代码仓库
- **IDE**: 推荐使用 [VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) 插件

> ⚠️ **注意**: 本项目不支持 Node.js 18 及以下版本，请使用 v20 或更高版本。

### 安装步骤

#### 1. 克隆项目

```bash
git clone https://github.com/macrozheng/mall-admin-web.git
cd mall-admin-web
```

#### 2. 安装依赖

```bash
npm install
```

> 💡 **提示**: 如果下载速度较慢，可以使用国内镜像：
> ```bash
> npm config set registry https://registry.npmmirror.com
> npm install
> ```

#### 3. 配置后端接口

编辑 `.env.development` 文件，配置后端 API 地址：

```bash
# 本地开发环境（需要启动后端服务）
VITE_BASE_SERVER_URL = http://localhost:8080

# 或使用在线测试接口（无需搭建后端）
# VITE_BASE_SERVER_URL = https://admin-api.macrozheng.com
```

**不同场景的配置：**

| 场景 | 配置值 | 说明 |
|------|--------|------|
| 本地单体架构后端 | `http://localhost:8080` | 运行 [mall](https://github.com/macrozheng/mall) 项目 |
| 本地微服务后端 | `http://localhost:8201/mall-admin` | 运行 [mall-swarm](https://github.com/macrozheng/mall-swarm) 项目 |
| 在线测试接口 | `https://admin-api.macrozheng.com` | 无需搭建后端，直接调试前端 |

#### 4. 启动开发服务器

```bash
npm run dev
```

启动成功后，浏览器会自动打开 [http://localhost:5173](http://localhost:5173)

#### 5. 登录系统

使用默认账号登录：
- **用户名**: `admin`
- **密码**: `123456`

---

## ⚙️ 环境配置

### 环境变量说明

项目使用 Vite 的环境变量系统，所有以 `VITE_` 开头的变量会在客户端代码中可用。

#### 开发环境 (.env.development)

```bash
# 后端 API 基础路径
VITE_BASE_SERVER_URL = http://localhost:8080
```

#### 生产环境 (.env.production)

```bash
# 后端 API 基础路径（部署时修改为实际地址）
VITE_BASE_SERVER_URL = https://your-api-domain.com
```

### 跨域配置

开发环境下，如果后端接口不在同一域名，需要在后端配置 CORS 跨域支持。

**Spring Boot 后端配置示例：**

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

---

## 📦 构建与部署

### 生产环境构建

#### 1. 类型检查并构建

```bash
npm run build
```

该命令会执行：
- TypeScript 类型检查
- 代码编译和优化
- 生成生产环境的静态文件到 `dist/` 目录

#### 2. 仅构建（跳过类型检查）

```bash
npm run build-only
```

#### 3. 预览生产构建

```bash
npm run preview
```

在本地预览生产构建效果，访问 [http://localhost:4173](http://localhost:4173)

### 部署到服务器

#### 方式一：Nginx 部署（推荐）

1. **上传构建文件**

将 `dist/` 目录下的所有文件上传到服务器的 `/usr/share/nginx/html/mall-admin` 目录

2. **配置 Nginx**

```nginx
server {
    listen       80;
    server_name  your-domain.com;
    
    root /usr/share/nginx/html/mall-admin;
    index index.html;
    
    # 启用 Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;
    
    # 处理 Vue Router History 模式
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # 代理后端 API 请求
    location /api/ {
        proxy_pass http://backend-server:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
    
    # 缓存静态资源
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
```

3. **重启 Nginx**

```bash
sudo nginx -t          # 检查配置
sudo systemctl restart nginx
```

#### 方式二：Docker 部署

创建 `Dockerfile`：

```dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

构建并运行：

```bash
docker build -t mall-admin-web .
docker run -d -p 80:80 mall-admin-web
```

### CI/CD 自动化部署

项目支持使用 Jenkins 进行自动化部署，详细配置请参考：[使用 Jenkins 一键打包部署前端应用](https://www.macrozheng.com/mall/reference/jenkins_vue.html)

---

## 🎯 核心功能模块

### 1. 首页仪表盘 (Home)

![首页](http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/badge/home.png)

- 📊 今日/昨日订单统计
- 💰 今日/昨日销售金额
- 📈 近7天订单趋势图
- 🔢 关键业务指标展示

**技术实现**：
- 使用 Vue ECharts 渲染图表
- Pinia 管理统计数据状态
- 响应式布局适配不同屏幕

### 2. 商品管理 (PMS - Product Management System)

#### 商品列表
- ✅ 商品搜索与筛选（名称、货号、品牌、分类）
- ✅ 批量上架/下架/删除
- ✅ 商品排序
- ✅ 分页展示

#### 添加/编辑商品
- 📝 基本信息（名称、副标题、品牌、分类）
- 🏷️ 商品规格（SKU）管理
- 📸 商品相册上传（支持多图）
- 📄 商品详情（富文本编辑器）
- ⚙️ 高级设置（运费模板、积分赠送等）

#### 商品分类
- 🌳 树形结构展示
- ➕ 添加/编辑/删除分类
- 🔄 拖拽排序
- 🖼️ 分类图标上传

#### 商品属性
- 📋 属性分类管理
- ✏️ 属性参数定义
- 🔗 关联商品类型

#### 品牌管理
- 🏢 品牌列表维护
- 🖼️ 品牌 Logo 上传
- 🌐 品牌故事编辑

### 3. 订单管理 (OMS - Order Management System)

#### 订单列表
- 🔍 多维度搜索（订单号、收货人、订单状态）
- 📅 按时间范围筛选
- 📊 订单状态统计
- 📄 订单详情查看

#### 订单处理流程
1. **待付款** → 取消订单
2. **待发货** → 确认订单 → 发货
3. **已发货** → 确认收货
4. **已完成** → 评价
5. **已关闭** → 退款/售后

#### 发货管理
- 📦 批量发货
- 🚚 物流公司选择
- 🔢 物流单号录入

#### 退货申请处理
- 📋 退货申请列表
- ✅ 审核退货申请
- 💰 退款处理
- 📝 退货原因管理

#### 订单设置
- ⏰ 超时自动取消时间
- ⏱️ 自动确认收货时间
- 💳 退款策略配置

### 4. 营销管理 (SMS - Sales Management System)

#### 秒杀活动
- ⚡ 秒杀场次管理
- 🕐 时间段配置
- 🛒 秒杀商品关联
- 💲 秒杀价格设置
- 📊 库存管理

#### 优惠券管理
- 🎫 优惠券创建（满减券、折扣券）
- 📅 有效期设置
- 👥 发放范围（全场/指定商品/指定分类）
- 📈 领取情况统计
- 🔍 领取详情查询

#### 推荐管理
- 🌟 品牌推荐
- 🆕 新品推荐
- 🔥 人气推荐
- 📰 专题推荐

#### 广告管理
- 🖼️ 轮播图管理
- 📍 广告位置配置
- 🔗 跳转链接设置
- 📅 上下架时间

### 5. 权限管理 (UMS - User Management System)

#### 管理员管理
- 👤 管理员账号 CRUD
- 🔑 密码重置
- 🚫 账号启用/禁用
- 👥 角色分配

#### 角色管理
- 🎭 角色定义
- 🔐 菜单权限分配
- 🔧 资源权限分配
- 👥 角色成员管理

#### 菜单管理
- 📋 菜单树形结构
- ➕ 添加/编辑/删除菜单
- 🎨 菜单图标配置
- 🔗 路由路径设置
- 👁️ 显示/隐藏控制

#### 资源管理
- 🔌 API 资源定义
- 📁 资源分类
- 🔒 权限标识配置
- 🔗 关联角色

---

## 🔧 开发指南

### 路由配置

项目采用**常量路由 + 异步路由**的方式实现权限控制。

#### 常量路由 (constantRouterMap)

无需权限即可访问的路由，如登录页、404页、首页等。

```typescript
// src/router/index.ts
export const constantRouterMap: RouteRecordExt[] = [
  { path: '/login', component: () => import('@/views/normal/login/index.vue'), hidden: true },
  { path: '/404', component: () => import('@/views/normal/404/index.vue'), hidden: true },
  {
    path: '',
    component: Layout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页', icon: 'dashboard' },
      },
    ],
  },
]
```

#### 异步路由 (asyncRouterMap)

需要根据用户权限动态加载的路由，如商品管理、订单管理等模块。

```typescript
export const asyncRouterMap: RouteRecordExt[] = [
  {
    path: '/pms',
    component: Layout,
    redirect: '/pms/product',
    name: 'pms',
    meta: { title: '商品', icon: 'product' },
    children: [
      {
        path: 'product',
        name: 'product',
        component: () => import('@/views/pms/product/index.vue'),
        meta: { title: '商品列表', icon: 'product-list' },
      },
      // ... 更多子路由
    ],
  },
  // ... 更多模块
]
```

#### 路由元信息 (meta)

```typescript
meta: {
  title: '页面标题',      // 侧边栏和标签页显示的标题
  icon: 'dashboard',      // 侧边栏图标（对应 src/icons/svg 中的文件名）
  hidden: true,           // 是否在侧边栏隐藏（默认 false）
  roles: ['admin'],       // 可访问的角色（可选）
}
```

### API 调用规范

#### 1. 定义 API 接口

在 `src/apis/` 目录下创建对应的 API 文件：

```typescript
// src/apis/product.ts
import http from '@/utils/http'
import type { Product, ProductQueryParams, CommonResult } from '@/types/product'

// 获取商品列表
export function getProductList(params: ProductQueryParams) {
  return http.get<CommonResult<Product[]>>('/product/list', { params })
}

// 创建商品
export function createProduct(data: Product) {
  return http.post<CommonResult<number>>('/product/create', data)
}

// 更新商品
export function updateProduct(id: number, data: Product) {
  return http.put<CommonResult<number>>(`/product/update/${id}`, data)
}

// 删除商品
export function deleteProduct(id: number) {
  return http.delete<CommonResult<number>>(`/product/delete/${id}`)
}
```

#### 2. 在组件中调用

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getProductList } from '@/apis/product'
import type { Product } from '@/types/product'

const productList = ref<Product[]>([])
const loading = ref(false)

// 加载商品列表
async function loadProducts() {
  loading.value = true
  try {
    const res = await getProductList({ pageNum: 1, pageSize: 10 })
    if (res.code === 200) {
      productList.value = res.data
    }
  } catch (error) {
    console.error('加载商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProducts()
})
</script>
```

### 状态管理 (Pinia)

#### 定义 Store

```typescript
// src/stores/user.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserInfo } from '@/types/admin'

export const useUserStore = defineStore('user', () => {
  // 状态
  const userInfo = ref<UserInfo>({
    id: 0,
    username: '',
    token: '',
    roles: [],
  })

  // Getter
  const isLoggedIn = computed(() => !!userInfo.value.token)

  // Actions
  function setUserInfo(info: UserInfo) {
    userInfo.value = info
  }

  function clearUserInfo() {
    userInfo.value = {
      id: 0,
      username: '',
      token: '',
      roles: [],
    }
  }

  async function login(username: string, password: string) {
    // 调用登录 API
    const res = await loginApi({ username, password })
    setUserInfo(res.data)
  }

  async function logout() {
    await logoutApi()
    clearUserInfo()
  }

  return {
    userInfo,
    isLoggedIn,
    setUserInfo,
    clearUserInfo,
    login,
    logout,
  }
}, {
  persist: true, // 启用持久化
})
```

#### 在组件中使用

```vue
<script setup lang="ts">
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 访问状态
console.log(userStore.userInfo.username)
console.log(userStore.isLoggedIn)

// 调用方法
await userStore.login('admin', '123456')
await userStore.logout()
</script>
```

### 组件自动导入

项目配置了 `unplugin-auto-import` 和 `unplugin-vue-components`，无需手动导入以下内容：

#### 1. Vue API 自动导入

```vue
<script setup lang="ts">
// ❌ 不需要手动导入
// import { ref, computed, watch, onMounted } from 'vue'

// ✅ 直接使用
const count = ref(0)
const doubleCount = computed(() => count.value * 2)
watch(count, (newVal) => console.log(newVal))
onMounted(() => console.log('mounted'))
</script>
```

#### 2. Element Plus 组件自动导入

```vue
<template>
  <!-- ❌ 不需要手动导入 -->
  <!-- <el-button type="primary">按钮</el-button> -->
  
  <!-- ✅ 直接使用 -->
  <el-button type="primary">按钮</el-button>
  <el-table :data="tableData">
    <el-table-column prop="name" label="姓名" />
  </el-table>
  <el-dialog v-model="dialogVisible" title="对话框">
    内容
  </el-dialog>
</template>
```

#### 3. 自定义组件自动导入

放置在 `src/components/` 目录下的组件会自动注册：

```vue
<template>
  <!-- ✅ 直接使用，无需 import -->
  <SvgIcon icon-class="dashboard" />
  <Breadcrumb />
  <SingleUpload v-model="imageUrl" />
</template>
```

### SVG 图标系统

#### 1. 添加新图标

将 SVG 文件放入 `src/icons/svg/` 目录：

```bash
src/icons/svg/
└── my-custom-icon.svg
```

#### 2. 使用图标

```vue
<template>
  <!-- 使用 SvgIcon 组件 -->
  <SvgIcon icon-class="my-custom-icon" />
  
  <!-- 在路由 meta 中使用 -->
  <!-- meta: { icon: 'my-custom-icon' } -->
</template>
```

#### 3. 图标命名规则

- 文件名即为图标类名（不含 `.svg` 后缀）
- 建议使用小写字母和连字符，如：`user-management.svg`
- 避免使用特殊字符和空格

### 富文本编辑器 (TinyMCE)

#### 基本使用

```vue
<template>
  <Tinymce v-model="content" :height="400" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Tinymce from '@/components/Tinymce/index.vue'

const content = ref('<p>初始内容</p>')
</script>
```

#### 配置选项

```vue
<Tinymce
  v-model="content"
  :height="500"
  :plugins="['link', 'image', 'table', 'lists']"
  :toolbar="['undo redo', 'bold italic', 'alignleft aligncenter alignright']"
/>
```

### 文件上传

#### 单图上传

```vue
<template>
  <SingleUpload v-model="imageUrl" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import SingleUpload from '@/components/Upload/singleUpload.vue'

const imageUrl = ref('')
</script>
```

#### 多图上传

```vue
<template>
  <MultiUpload v-model="imageUrls" :limit="5" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import MultiUpload from '@/components/Upload/multiUpload.vue'

const imageUrls = ref<string[]>([])
</script>
```

### 路由守卫

项目在 `src/router/guard.ts` 中配置了全局路由守卫：

```typescript
// 前置守卫
router.beforeEach(async (to, from, next) => {
  // 1. 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - Mall Admin` : 'Mall Admin'
  
  // 2. 检查登录状态
  const userStore = useUserStore()
  if (!userStore.isLoggedIn && to.path !== '/login') {
    next('/login')
    return
  }
  
  // 3. 加载动态路由
  if (userStore.isLoggedIn && to.path === '/login') {
    next('/')
    return
  }
  
  next()
})

// 后置守卫
router.afterEach(() => {
  // 关闭进度条
  NProgress.done()
})
```

---

## 🐛 常见问题

### 1. Node.js 版本不兼容

**问题**: 运行时提示 Node.js 版本过低

**解决**: 
```bash
# 检查当前版本
node -v

# 升级到 v20 LTS
# 使用 nvm (Node Version Manager)
nvm install 20
nvm use 20
```

### 2. 依赖安装失败

**问题**: `npm install` 时报错

**解决**:
```bash
# 清除缓存
npm cache clean --force

# 删除 node_modules 和 package-lock.json
rm -rf node_modules package-lock.json

# 重新安装
npm install
```

### 3. 端口被占用

**问题**: `npm run dev` 提示端口 5173 已被占用

**解决**:
```bash
# 方式一：终止占用端口的进程
# Windows
netstat -ano | findstr :5173
taskkill /PID <进程ID> /F

# Mac/Linux
lsof -ti:5173 | xargs kill -9

# 方式二：修改 Vite 端口
# vite.config.ts
export default defineConfig({
  server: {
    port: 5174, // 改为其他端口
  },
})
```

### 4. 跨域问题

**问题**: 浏览器控制台提示 CORS 错误

**解决**:
- 确保后端已配置 CORS
- 检查 `.env.development` 中的 `VITE_BASE_SERVER_URL` 是否正确
- 开发环境可使用 Vite 代理（见下方配置）

**Vite 代理配置示例**:

```typescript
// vite.config.ts
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
```

### 5. TypeScript 类型错误

**问题**: IDE 提示类型错误

**解决**:
```bash
# 重新生成类型声明
npm run type-check

# 如果仍有问题，重启 TypeScript 服务器
# VSCode: Ctrl+Shift+P -> "TypeScript: Restart TS Server"
```

### 6. 构建失败

**问题**: `npm run build` 报错

**解决**:
```bash
# 查看详细错误信息
npm run build -- --debug

# 常见原因：
# 1. TypeScript 类型错误 -> 修复类型问题
# 2. 内存不足 -> 增加 Node.js 内存限制
#    Windows: set NODE_OPTIONS=--max-old-space-size=4096 && npm run build
#    Mac/Linux: NODE_OPTIONS=--max-old-space-size=4096 npm run build
```

### 7. 白屏问题

**问题**: 部署后访问页面白屏

**解决**:
- 检查浏览器控制台是否有报错
- 确认 `vite.config.ts` 中 `base` 配置正确（相对路径用 `'./'`）
- 检查 Nginx 配置是否正确处理 History 模式
- 清除浏览器缓存

### 8. SVG 图标不显示

**问题**: `<SvgIcon>` 组件图标不显示

**解决**:
```typescript
// 确保 main.ts 中已正确引入
import { setupSvgIcon } from './icons'
import 'virtual:svg-icons-register'

setupSvgIcon(app)
```

---

## 📚 学习资源

### 官方文档

- [Vue 3 官方文档](https://cn.vuejs.org/)
- [TypeScript 官方文档](https://www.typescriptlang.org/zh/)
- [Element Plus 官方文档](https://element-plus.org/zh-CN/)
- [Vite 官方文档](https://cn.vite.dev/)
- [Pinia 官方文档](https://pinia.vuejs.org/zh/)
- [Vue Router 官方文档](https://router.vuejs.org/zh/)

### Mall 系列教程

- [完整学习教程](https://www.macrozheng.com/)
- [mall 后端项目](https://github.com/macrozheng/mall)
- [mall-swarm 微服务项目](https://github.com/macrozheng/mall-swarm)
- [mall-app-web 移动端](https://github.com/macrozheng/mall-app-web)
- [mall-admin-web 前端项目](https://github.com/macrozheng/mall-admin-web)

### 视频教程

- [B站视频教程](https://www.bilibili.com/video/BV1qR4y1T7wX)
- [mall 项目实战教程](https://www.macrozheng.com/mall/foreword/mall_video.html)

### 相关文章

- [mall 前端项目的安装与部署](https://www.macrozheng.com/mall/start/mall_deploy_web.html)
- [使用 Jenkins 一键打包部署前端应用](https://www.macrozheng.com/mall/reference/jenkins_vue.html)
- [Vue3 + TypeScript 最佳实践](https://cn.vuejs.org/guide/typescript/overview.html)
- [Element Plus 主题定制](https://element-plus.org/zh-CN/guide/theming.html)

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

### 提交 Issue

- 🐛 **Bug 报告**: 详细描述问题、复现步骤、期望行为
- 💡 **功能建议**: 说明需求背景和预期效果
- ❓ **使用疑问**: 先查阅文档和已有 Issue

### 提交 PR

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范

- 遵循 ESLint 和 Prettier 规则
- 使用 TypeScript 类型注解
- 组件使用 Composition API (`<script setup>`)
- 提交前运行 `npm run lint` 检查代码

---

## 📄 许可证

本项目采用 [Apache License 2.0](LICENSE) 开源协议。

Copyright (c) 2018-2026 macrozheng

---

## 📮 联系方式

### 公众号

关注公众号「macrozheng」，获取最新技术文章和项目动态。

![公众号](http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/banner/qrcode_for_macrozheng_258.jpg)

### 交流群

公众号后台回复「**加群**」加入技术交流群，与开发者一起讨论。

### GitHub

- ⭐ 如果这个项目对你有帮助，欢迎 Star 支持！
- 🔔 Watch 关注项目更新
- 🍴 Fork 参与项目开发

---

## 🙏 致谢

感谢以下开源项目的支持：

- [Vue.js](https://github.com/vuejs/core)
- [Element Plus](https://github.com/element-plus/element-plus)
- [Vite](https://github.com/vitejs/vite)
- [TypeScript](https://github.com/microsoft/TypeScript)
- [Axios](https://github.com/axios/axios)
- [Pinia](https://github.com/vuejs/pinia)
- [TinyMCE](https://github.com/tinymce/tinymce)
- [ECharts](https://github.com/apache/echarts)

---

**Made with ❤️ by macrozheng**
