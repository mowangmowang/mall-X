# Mall 移动端商城 - 详细开发文档

## 📋 目录

- [项目概述](#项目概述)
- [技术架构](#技术架构)
- [项目结构](#项目结构)
- [核心功能模块](#核心功能模块)
- [开发环境搭建](#开发环境搭建)
- [API 接口规范](#api-接口规范)
- [状态管理](#状态管理)
- [网络请求封装](#网络请求封装)
- [页面路由配置](#页面路由配置)
- [组件库说明](#组件库说明)
- [部署指南](#部署指南)
- [常见问题](#常见问题)

---

## 项目概述

### 项目简介

**mall-app-web** 是基于 **uni-app** 框架开发的跨平台电商移动应用，采用前后端分离架构。一套代码可同时编译到 H5、微信小程序、Android App 和 iOS App 多个平台。

本项目是 mall 电商系统的移动端前端部分，为用户提供完整的购物体验，包括商品浏览、购物车管理、订单处理、支付等功能。

### 在线演示

- **H5 演示地址**: [https://www.macrozheng.com/app/](https://www.macrozheng.com/app/)
- **后端项目**: [https://github.com/macrozheng/mall](https://github.com/macrozheng/mall)
- **管理后台**: [https://github.com/macrozheng/mall-admin-web](https://github.com/macrozheng/mall-admin-web)

### 核心特性

- 🛍️ **完整电商流程**: 商品浏览 → 购物车 → 下单 → 支付 → 订单管理
- 📱 **跨平台支持**: H5、微信小程序、Android、iOS 四端统一
- 🔐 **JWT 认证**: 基于 Token 的安全身份验证机制
- 🛒 **智能购物车**: 实时价格计算、批量操作、库存校验
- 📦 **订单全流程**: 创建、支付、发货、收货、评价完整闭环
- 👤 **会员中心**: 个人信息、地址管理、优惠券、积分系统
- ❤️ **用户互动**: 商品收藏、浏览历史、品牌关注
- 🔍 **智能搜索**: 关键词搜索、分类筛选、多维度排序
- 💳 **多种支付**: 支付宝、微信支付集成
- 🎨 **响应式设计**: 适配不同屏幕尺寸

---

## 技术架构

### 前端技术栈

| 技术 | 版本 | 用途 | 说明 |
|------|------|------|------|
| Vue.js | 2.x | 核心框架 | 渐进式 JavaScript 框架 |
| uni-app | - | 跨平台框架 | 一套代码多端运行 |
| Vuex | 3.x | 状态管理 | 全局状态集中管理 |
| luch-request | - | HTTP 客户端 | 基于 Promise 的请求库 |
| SCSS | - | CSS 预处理器 | 增强样式编写体验 |

### 后端依赖

| 技术 | 版本 | 端口 | 用途 |
|------|------|------|------|
| Spring Boot | 2.7+ | 8085 | 后端服务框架 |
| MySQL | 5.7+ | 3306 | 关系型数据库 |
| Redis | 5.0+ | 6379 | 缓存数据库 |
| MongoDB | 4.0+ | 27017 | 文档数据库（浏览记录等） |
| RabbitMQ | 3.8+ | 5672 | 消息队列 |
| JWT | - | - | 身份认证 |

### 系统架构图

```
┌─────────────────────────────────────────────┐
│          mall-app-web (前端应用)              │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐    │
│  │  H5 端   │ │ 小程序端 │ │  App 端  │    │
│  └──────────┴─┴──────────┴─┴──────────┘    │
│           │          │          │            │
│         HTTP/HTTPS + JWT Token               │
└───────────────────┼─────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────┐
│         mall-portal (后端服务)                │
│  ┌──────────────────────────────────┐       │
│  │   Spring Boot (Port: 8085)       │       │
│  │  Controller → Service → DAO      │       │
│  └──────────────────────────────────┘       │
│     │            │            │              │
│     ▼            ▼            ▼              │
│  ┌──────┐  ┌──────┐  ┌──────────┐          │
│  │MySQL │  │Redis │  │ MongoDB  │          │
│  └──────┘  └──────┘  └──────────┘          │
│                    │                        │
│                    ▼                        │
│            ┌──────────────┐                │
│            │  RabbitMQ    │                │
│            └──────────────┘                │
└─────────────────────────────────────────────┘
```

---

## 项目结构

```
mall-app-web/
├── api/                          # API 接口定义
│   ├── address.js               # 收货地址接口
│   ├── brand.js                 # 品牌接口
│   ├── cart.js                  # 购物车接口
│   ├── coupon.js                # 优惠券接口
│   ├── home.js                  # 首页接口
│   ├── member.js                # 会员接口
│   ├── memberBrandAttention.js  # 品牌关注接口
│   ├── memberProductCollection.js # 商品收藏接口
│   ├── memberReadHistory.js     # 浏览历史接口
│   ├── order.js                 # 订单接口
│   └── product.js               # 商品接口
│
├── components/                   # 公共组件
│   ├── empty.vue                # 空状态组件
│   ├── mix-list-cell.vue        # 列表单元格组件
│   ├── mix-loading/             # 加载动画组件
│   ├── share.vue                # 分享组件
│   ├── uni-load-more/           # 加载更多组件
│   ├── uni-number-box.vue       # 数字输入框组件
│   └── upload-images.vue        # 图片上传组件
│
├── images/                       # 项目图片资源
│
├── js_sdk/                       # JS SDK
│   └── luch-request/            # HTTP 请求库
│       ├── request.js
│       └── utils.js
│
├── pages/                        # 页面文件
│   ├── index/                   # 首页模块
│   │   └── index.vue
│   ├── product/                 # 商品模块
│   │   ├── product.vue          # 商品详情
│   │   ├── list.vue             # 商品列表
│   │   ├── newProductList.vue   # 新品列表
│   │   └── hotProductList.vue   # 热销列表
│   ├── category/                # 分类模块
│   │   └── category.vue
│   ├── cart/                    # 购物车模块
│   │   └── cart.vue
│   ├── order/                   # 订单模块
│   │   ├── createOrder.vue      # 创建订单
│   │   ├── order.vue            # 订单列表
│   │   └── orderDetail.vue      # 订单详情
│   ├── money/                   # 支付模块
│   │   ├── money.vue            # 支付中心
│   │   ├── pay.vue              # 支付页面
│   │   └── paySuccess.vue       # 支付成功
│   ├── user/                    # 用户模块
│   │   ├── user.vue             # 个人中心
│   │   ├── userinfo.vue         # 个人信息
│   │   ├── readHistory.vue      # 浏览历史
│   │   ├── productCollection.vue # 商品收藏
│   │   └── brandAttention.vue   # 品牌关注
│   ├── public/                  # 公共页面
│   │   ├── login.vue            # 登录页
│   │   └── register.vue         # 注册页
│   ├── address/                 # 地址模块
│   │   ├── address.vue          # 地址列表
│   │   └── addressManage.vue    # 地址编辑
│   ├── coupon/                  # 优惠券模块
│   │   └── couponList.vue
│   ├── brand/                   # 品牌模块
│   │   ├── list.vue             # 品牌列表
│   │   └── brandDetail.vue      # 品牌详情
│   ├── notice/                  # 通知模块
│   │   └── notice.vue
│   └── set/                     # 设置模块
│       └── set.vue
│
├── static/                       # 静态资源
│   ├── tab-*.png                # TabBar 图标
│   ├── yticon.ttf               # 字体图标
│   └── ...                      # 其他图片资源
│
├── store/                        # Vuex 状态管理
│   └── index.js
│
├── utils/                        # 工具函数
│   ├── appConfig.js             # 应用配置
│   ├── date.js                  # 日期处理
│   └── requestUtil.js           # 请求封装
│
├── App.vue                       # 应用入口组件
├── main.js                       # 应用入口文件
├── manifest.json                 # 应用配置文件
├── pages.json                    # 页面路由配置
├── uni.scss                      # 全局样式变量
├── README.md                     # 项目说明文档
└── LICENSE                       # 开源协议
```

---

## 核心功能模块

### 1. 首页模块 (pages/index)

**页面路径**: `pages/index/index.vue`

**功能清单**:
- ✅ 轮播图展示（广告Banner）
- ✅ 商品分类快捷入口
- ✅ 推荐商品列表
- ✅ 新品推荐展示
- ✅ 人气热销商品
- ✅ 搜索入口跳转

**调用接口**:
```javascript
GET /home/content                    // 获取首页内容
GET /home/recommendProductList       // 获取推荐商品
GET /home/newProductList             // 获取新品列表
GET /home/hotProductList             // 获取热销商品
GET /home/productCateList/:parentId  // 获取商品分类
```

**实现要点**:
- 使用 `enablePullDownRefresh: true` 启用下拉刷新
- 透明导航栏设计 (`titleNView.type: "transparent"`)
- 搜索框和消息图标通过 `buttons` 配置

---

### 2. 商品模块 (pages/product)

**页面清单**:
- `product.vue` - 商品详情页
- `list.vue` - 商品列表页
- `newProductList.vue` - 新品列表
- `hotProductList.vue` - 热销列表

**功能清单**:
- ✅ 多图轮播展示
- ✅ 商品规格选择（SKU）
- ✅ 富文本详情展示
- ✅ 关键词搜索
- ✅ 分类/品牌筛选
- ✅ 多维度排序（价格、销量、新品）
- ✅ 添加购物车
- ✅ 商品收藏
- ✅ 浏览历史记录

**调用接口**:
```javascript
GET /product/search                  // 搜索商品
GET /product/detail/:id              // 商品详情
GET /product/categoryTreeList        // 分类树
POST /member/productCollection/add   // 添加收藏
POST /member/readHistory/create      // 创建浏览记录
```

**实现要点**:
- SKU 规格联动选择
- 库存实时校验
- 图片懒加载优化

---

### 3. 分类模块 (pages/category)

**页面路径**: `pages/category/category.vue`

**功能清单**:
- ✅ 左右分栏布局
- ✅ 一级/二级分类联动
- ✅ 分类商品实时加载

**调用接口**:
```javascript
GET /product/categoryTreeList        // 获取分类树
GET /home/productCateList/:parentId  // 获取子分类
```

**实现要点**:
- 左侧滚动导航，右侧商品网格
- 点击分类即时切换商品列表

---

### 4. 购物车模块 (pages/cart)

**页面路径**: `pages/cart/cart.vue`

**功能清单**:
- ✅ 添加商品到购物车
- ✅ 修改商品数量（步进器）
- ✅ 单选/全选商品
- ✅ 删除单个/批量删除
- ✅ 清空购物车
- ✅ 实时计算总价
- ✅ 库存校验提示

**调用接口**:
```javascript
POST /cart/add                       // 添加到购物车
GET /cart/list                       // 获取购物车列表
POST /cart/delete                    // 删除商品
GET /cart/update/quantity            // 更新数量
POST /cart/clear                     // 清空购物车
```

**实现要点**:
- 本地缓存与服务器同步
- 选中状态管理
- 价格实时计算

---

### 5. 订单模块 (pages/order)

**页面清单**:
- `createOrder.vue` - 创建订单
- `order.vue` - 订单列表
- `orderDetail.vue` - 订单详情

**功能清单**:
- ✅ 生成确认单（选择地址、优惠券）
- ✅ 创建订单
- ✅ 订单列表（按状态筛选）
- ✅ 订单详情查看
- ✅ 取消订单
- ✅ 确认收货
- ✅ 删除订单

**订单状态流转**:
```
待付款(0) → 待发货(1) → 已发货(2) → 已完成(3)
   ↓                                    ↓
已关闭(4) ←───────────────────────────┘
```

**调用接口**:
```javascript
POST /order/generateConfirmOrder     // 生成确认单
POST /order/generateOrder            // 生成订单
GET /order/list                      // 订单列表
GET /order/detail/:orderId           // 订单详情
POST /order/paySuccess               // 支付回调
POST /order/cancelUserOrder          // 取消订单
POST /order/confirmReceiveOrder      // 确认收货
POST /order/deleteOrder              // 删除订单
```

**实现要点**:
- 订单超时自动取消（RabbitMQ 延迟队列）
- 优惠券和积分抵扣计算
- 物流信息跟踪

---

### 6. 支付模块 (pages/money)

**页面清单**:
- `money.vue` - 支付中心
- `pay.vue` - 支付页面
- `paySuccess.vue` - 支付成功

**功能清单**:
- ✅ 支付方式选择（支付宝/微信）
- ✅ 第三方支付集成
- ✅ 支付结果查询
- ✅ 支付成功跳转

**调用接口**:
```javascript
POST /order/paySuccess               // 支付成功回调
GET /alipay/query                    // 查询支付状态
```

**实现要点**:
- 支付宝 H5 支付集成
- 微信支付小程序集成
- 支付状态轮询查询

---

### 7. 会员模块 (pages/user)

**页面清单**:
- `user.vue` - 个人中心
- `userinfo.vue` - 个人信息编辑
- `readHistory.vue` - 浏览历史
- `productCollection.vue` - 商品收藏
- `brandAttention.vue` - 品牌关注

**功能清单**:
- ✅ 个人信息展示与编辑
- ✅ 浏览历史记录（MongoDB存储）
- ✅ 商品收藏管理
- ✅ 品牌关注管理
- ✅ 优惠券查看
- ✅ 退出登录

**调用接口**:
```javascript
GET /sso/info                        // 获取会员信息
POST /member/readHistory/create      // 创建浏览记录
GET /member/readHistory/list         // 浏览历史列表
POST /member/readHistory/clear       // 清空浏览历史
POST /member/productCollection/add   // 添加收藏
GET /member/productCollection/list   // 收藏列表
POST /member/productCollection/delete // 取消收藏
POST /member/attention/add           // 关注品牌
GET /member/attention/list           // 关注列表
POST /member/attention/delete        // 取消关注
```

**实现要点**:
- 浏览历史自动记录（进入商品详情页时触发）
- MongoDB 存储非结构化数据
- 分页加载优化

---

### 8. 登录注册 (pages/public)

**页面清单**:
- `login.vue` - 登录页
- `register.vue` - 注册页

**功能清单**:
- ✅ 手机号+密码登录
- ✅ 手机号+验证码注册
- ✅ JWT Token 存储
- ✅ 自动登录（Token 有效期内）
- ✅ 退出登录清除

**调用接口**:
```javascript
POST /sso/login                      // 会员登录
GET /sso/info                        // 获取会员信息
```

**实现要点**:
- Token 存储在 `uni.storage`
- 登录状态通过 Vuex 管理
- 401 自动跳转登录页

---

### 9. 地址管理 (pages/address)

**页面清单**:
- `address.vue` - 地址列表
- `addressManage.vue` - 地址编辑

**功能清单**:
- ✅ 地址列表展示
- ✅ 新增收货地址
- ✅ 编辑现有地址
- ✅ 删除地址
- ✅ 设置默认地址

**调用接口**:
```javascript
GET /member/address/list             // 地址列表
GET /member/address/:id              // 地址详情
POST /member/address/add             // 添加地址
POST /member/address/update/:id      // 更新地址
POST /member/address/delete/:id      // 删除地址
```

**实现要点**:
- 表单验证（手机号、邮编等）
- 默认地址标识
- 地址选择联动（订单确认页）

---

### 10. 优惠券模块 (pages/coupon)

**页面路径**: `pages/coupon/couponList.vue`

**功能清单**:
- ✅ 可用优惠券列表
- ✅ 已使用优惠券
- ✅ 已过期优惠券
- ✅ 领取优惠券
- ✅ 下单自动抵扣

**调用接口**:
```javascript
GET /member/coupon/listByProduct/:productId  // 商品可用优惠券
POST /member/coupon/add/:couponId            // 领取优惠券
GET /member/coupon/list                      // 会员优惠券列表
```

**实现要点**:
- 优惠券有效期判断
- 满减规则计算
- 互斥券逻辑处理

---

### 11. 品牌模块 (pages/brand)

**页面清单**:
- `list.vue` - 推荐品牌列表
- `brandDetail.vue` - 品牌详情

**功能清单**:
- ✅ 推荐品牌展示
- ✅ 品牌详情介绍
- ✅ 品牌商品列表
- ✅ 关注/取消关注

**调用接口**:
```javascript
GET /brand/detail/:id                // 品牌详情
GET /brand/productList               // 品牌商品
GET /brand/recommendList             // 推荐品牌
```

---

### 12. 通知模块 (pages/notice)

**页面路径**: `pages/notice/notice.vue`

**功能清单**:
- ✅ 系统通知列表
- ✅ 订单状态通知
- ✅ 促销活动通知

---

### 13. 设置模块 (pages/set)

**页面路径**: `pages/set/set.vue`

**功能清单**:
- ✅ 退出登录
- ✅ 清除缓存
- ✅ 关于我们
- ✅ 联系客服

---

## 开发环境搭建

### 前置要求

1. **Node.js**: v14+ (推荐 v16 LTS)
2. **HBuilder X**: App开发版（下载地址：https://www.dcloud.io/hbuilderx.html）
3. **微信开发者工具**: 用于小程序调试
4. **后端服务**: mall-portal 已启动（端口 8085）

### 安装步骤

#### 1. 克隆项目

```bash
git clone https://github.com/macrozheng/mall-app-web.git
cd mall-app-web
```

#### 2. 使用 HBuilder X 打开项目

- 启动 HBuilder X
- 文件 → 打开目录 → 选择 `mall-app-web` 文件夹

#### 3. 配置后端 API 地址

编辑 `utils/appConfig.js`:

```javascript
// 开发环境
export const API_BASE_URL = 'http://localhost:8085';

// 生产环境（替换为实际域名）
// export const API_BASE_URL = 'https://api.macrozheng.com';
```

#### 4. 运行项目

**H5 端**:
- HBuilder X 中点击 "运行" → "运行到浏览器" → "Chrome"
- 访问 `http://localhost:8060`

**微信小程序**:
- HBuilder X 中点击 "运行" → "运行到小程序模拟器" → "微信开发者工具"
- 在微信开发者工具中预览

**App 端**:
- HBuilder X 中点击 "运行" → "运行到手机或模拟器"
- 连接真机或启动模拟器

---

## API 接口规范

### 基础配置

**API 基础路径**: `http://localhost:8085`

**请求头**:
```javascript
{
  "Content-Type": "application/json",
  "Authorization": "Bearer <jwt_token>"  // 需要认证的接口
}
```

### 响应格式

所有接口统一返回格式：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

**常见状态码**:
- `200`: 请求成功
- `401`: 未登录或 Token 失效
- `403`: 无权限
- `404`: 资源不存在
- `500`: 服务器错误

### 免认证接口白名单

以下接口无需 Token 即可访问：

```
/sso/**          # 登录注册
/home/**         # 首页内容
/product/**      # 商品相关
/brand/**        # 品牌相关
/alipay/**       # 支付宝回调
```

### 接口分类

详见 [API 接口文档](./README.md#-api-接口文档) 或参考 `api/` 目录下的接口定义文件。

---

## 状态管理

### Vuex Store 结构

**文件路径**: `store/index.js`

```javascript
{
  state: {
    hasLogin: false,    // 登录状态
    userInfo: {}        // 用户信息
  },
  mutations: {
    login(state, provider) { ... },   // 登录
    logout(state) { ... }             // 登出
  }
}
```

### 使用方法

**在组件中**:

```javascript
import { mapMutations } from 'vuex';

export default {
  methods: {
    ...mapMutations(['login', 'logout']),
    
    handleLogin() {
      // 登录成功后
      this.login(userInfo);
    },
    
    handleLogout() {
      this.logout();
      uni.redirectTo({ url: '/pages/public/login' });
    }
  }
}
```

**直接访问**:

```javascript
// 获取登录状态
const hasLogin = this.$store.state.hasLogin;

// 获取用户信息
const userInfo = this.$store.state.userInfo;
```

### 持久化策略

- `userInfo`: 存储在 `uni.storage`（本地缓存）
- `token`: 存储在 `uni.storage`
- 应用启动时从缓存恢复登录状态（见 `App.vue` 的 `onLaunch`）

---

## 网络请求封装

### 请求库: luch-request

**文件路径**: `utils/requestUtil.js`

### 核心功能

1. **全局配置**: 设置 baseURL、请求头等
2. **请求拦截器**: 自动添加 Token
3. **响应拦截器**: 统一错误处理、401 跳转登录
4. **Promise 封装**: 支持 async/await

### 使用示例

**基础用法**:

```javascript
import { request } from '@/utils/requestUtil.js';

// GET 请求
const res = await request({
  url: '/home/content',
  method: 'GET'
});

// POST 请求
const res = await request({
  url: '/cart/add',
  method: 'POST',
  data: {
    productId: 1,
    quantity: 2
  }
});
```

**API 模块化封装** (以购物车为例):

```javascript
// api/cart.js
import { request } from '@/utils/requestUtil.js';

export function addCart(data) {
  return request({
    url: '/cart/add',
    method: 'POST',
    data
  });
}

export function getCartList() {
  return request({
    url: '/cart/list',
    method: 'GET'
  });
}
```

**在页面中调用**:

```javascript
import { addCart, getCartList } from '@/api/cart.js';

export default {
  methods: {
    async addToCart() {
      try {
        const res = await addCart({
          productId: this.productId,
          quantity: 1
        });
        this.$api.msg('添加成功');
      } catch (err) {
        console.error(err);
      }
    }
  }
}
```

### 错误处理

**响应拦截器自动处理**:

```javascript
// 业务错误（code !== 200）
uni.showToast({ title: res.message });

// 401 未登录
uni.showModal({
  content: '你已被登出，可以取消继续留在该页面，或者重新登录',
  success: (res) => {
    if (res.confirm) {
      uni.navigateTo({ url: '/pages/public/login' });
    }
  }
});

// 网络错误
uni.showToast({ title: response.errMsg });
```

---

## 页面路由配置

### 配置文件: pages.json

**TabBar 页面** (底部导航):

```json
{
  "tabBar": {
    "list": [
      {
        "pagePath": "pages/index/index",
        "text": "首页",
        "iconPath": "static/tab-home.png",
        "selectedIconPath": "static/tab-home-current.png"
      },
      {
        "pagePath": "pages/category/category",
        "text": "分类"
      },
      {
        "pagePath": "pages/cart/cart",
        "text": "购物车"
      },
      {
        "pagePath": "pages/user/user",
        "text": "我的"
      }
    ]
  }
}
```

**普通页面路由**:

```json
{
  "pages": [
    {
      "path": "pages/product/product",
      "style": {
        "navigationBarTitleText": "商品详情",
        "app-plus": {
          "titleNView": {
            "type": "transparent"  // 透明导航栏
          }
        }
      }
    }
  ]
}
```

### 页面跳转方式

**1. 保留当前页，跳转到新页面**:
```javascript
uni.navigateTo({
  url: '/pages/product/product?id=' + productId
});
```

**2. 关闭当前页，跳转到新页面**:
```javascript
uni.redirectTo({
  url: '/pages/public/login'
});
```

**3. 关闭所有页面，跳转到 TabBar 页面**:
```javascript
uni.switchTab({
  url: '/pages/index/index'
});
```

**4. 返回上一页**:
```javascript
uni.navigateBack();
```

**5. 传递参数**:
```javascript
// 发送方
uni.navigateTo({
  url: `/pages/order/orderDetail?orderId=${orderId}`
});

// 接收方
export default {
  onLoad(options) {
    const orderId = options.orderId;
  }
}
```

---

## 组件库说明

### 公共组件位置: `components/`

### 1. empty.vue - 空状态组件

**用途**: 当列表为空时显示占位图

**使用示例**:
```vue
<empty v-if="list.length === 0" text="暂无数据"></empty>
```

### 2. uni-number-box.vue - 数字输入框

**用途**: 购物车数量修改

**Props**:
- `value`: 当前值
- `min`: 最小值
- `max`: 最大值

**Events**:
- `@change`: 值改变时触发

**使用示例**:
```vue
<uni-number-box 
  :value="item.quantity" 
  :min="1" 
  :max="100"
  @change="updateQuantity"
></uni-number-box>
```

### 3. uni-load-more - 加载更多组件

**用途**: 列表分页加载

**Props**:
- `status`: 加载状态（loading/noMore/error）
- `contentText`: 自定义文案

**使用示例**:
```vue
<uni-load-more :status="loadStatus"></uni-load-more>
```

### 4. upload-images.vue - 图片上传组件

**用途**: 头像、评价图片上传

**Props**:
- `value`: 图片URL数组
- `maxCount`: 最大上传数量

**Events**:
- `@input`: 图片变化时触发

### 5. share.vue - 分享组件

**用途**: 商品分享功能

**使用示例**:
```vue
<share 
  :contentText="product.name"
  :imageUrl="product.pic"
></share>
```

---

## 部署指南

### H5 部署

#### 1. 构建生产版本

在 HBuilder X 中:
- 发行 → 网站-H5手机版
- 或使用命令行: `npm run build:h5`

#### 2. 上传到服务器

将生成的 `unpackage/dist/build/h5` 目录上传到 Nginx 服务器。

#### 3. Nginx 配置

```nginx
server {
    listen 80;
    server_name app.macrozheng.com;
    root /var/www/mall-app-web;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # 代理后端 API
    location /api/ {
        proxy_pass http://localhost:8085/;
    }
}
```

### 微信小程序部署

#### 1. 构建小程序版本

在 HBuilder X 中:
- 发行 → 小程序-微信
- 填写 AppID

#### 2. 上传代码

- 使用微信开发者工具打开生成的项目
- 点击 "上传" 按钮
- 在微信公众平台提交审核

#### 3. 配置服务器域名

在微信公众平台配置合法域名:
- request 合法域名: `https://api.macrozheng.com`

### Android/iOS App 部署

#### 1. 云打包

在 HBuilder X 中:
- 发行 → 原生App-云打包
- 选择 Android 或 iOS
- 填写证书信息

#### 2. 下载安装包

打包完成后下载 `.apk` 或 `.ipa` 文件。

#### 3. 发布到应用市场

- Android: 华为、小米、OPPO、VIVO 等应用市场
- iOS: App Store Connect 提交审核

---

## 常见问题

### 1. 跨域问题

**现象**: H5 端请求后端接口报 CORS 错误

**解决方案**:

后端配置跨域（Spring Boot）:

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }
}
```

或使用 Nginx 反向代理:

```nginx
location /api/ {
    proxy_pass http://localhost:8085/;
    add_header Access-Control-Allow-Origin *;
}
```

### 2. Token 失效处理

**现象**: 登录后一段时间操作提示 "未登录"

**原因**: JWT Token 过期

**解决方案**:

前端已自动处理 401 状态，弹出登录框。如需延长 Token 有效期，修改后端配置:

```yaml
# application.yml
jwt:
  tokenHeader: Authorization
  secret: your-secret-key
  expiration: 604800  # 7天（单位：秒）
```

### 3. 图片加载慢

**优化方案**:

1. **使用 CDN**: 将图片上传到 OSS/CDN
2. **图片压缩**: 上传前压缩图片
3. **懒加载**: 使用 `lazy-load` 属性

```vue
<image :src="item.pic" lazy-load mode="aspectFill"></image>
```

### 4. 小程序真机调试空白

**原因**: 未配置合法域名

**解决方案**:

1. 登录微信公众平台
2. 开发 → 开发管理 → 开发设置
3. 配置 request 合法域名: `https://api.macrozheng.com`

或在开发阶段勾选 "不校验合法域名"

### 5. App 端样式错乱

**原因**: 不同平台 rpx 转换差异

**解决方案**:

使用条件编译:

```css
/* #ifdef APP-PLUS */
.container {
  padding: 20px;
}
/* #endif */

/* #ifdef H5 */
.container {
  padding: 15px;
}
/* #endif */
```

### 6. 购物车数据不同步

**现象**: 添加购物车后，切换到购物车页面看不到商品

**原因**: 未刷新数据

**解决方案**:

在购物车页面的 `onShow` 生命周期中重新加载数据:

```javascript
export default {
  onShow() {
    this.loadCartList();
  }
}
```

### 7. 支付回调不执行

**原因**: 支付宝/微信异步通知未正确配置

**解决方案**:

1. 确保后端支付回调接口在白名单中
2. 检查回调 URL 是否可外网访问
3. 查看后端日志确认是否收到回调

### 8. 浏览历史未记录

**原因**: MongoDB 未启动或连接失败

**解决方案**:

1. 检查 MongoDB 服务是否运行: `systemctl status mongod`
2. 查看后端日志中的 MongoDB 连接信息
3. 确认 `application.yml` 中 MongoDB 配置正确:

```yaml
spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: mall
```

---

## 开发规范

### 代码风格

1. **命名规范**:
   - 文件名: 小写 + 短横线（如 `product-list.vue`）
   - 变量名: 驼峰命名（如 `productName`）
   - 常量: 大写 + 下划线（如 `API_BASE_URL`）

2. **注释规范**:
   - 关键逻辑必须添加注释
   - 复杂算法需说明思路
   - 接口调用注明参数和返回值

3. **组件规范**:
   - 单一职责原则
   - Props 明确类型和默认值
   - 事件命名使用动词（如 `@click`, `@change`）

### Git 提交规范

```bash
# 新功能
git commit -m "feat: 添加商品收藏功能"

# 修复 Bug
git commit -m "fix: 修复购物车数量更新问题"

# 文档更新
git commit -m "docs: 更新 API 文档"

# 代码重构
git commit -m "refactor: 重构订单模块代码"
```

---

## 性能优化建议

### 1. 首屏加载优化

- 使用分包加载（小程序）
- 图片懒加载
- 骨架屏替代 loading

### 2. 列表性能优化

- 虚拟列表（长列表场景）
- 分页加载
- 防抖节流

```javascript
// 搜索防抖
searchKeyword: debounce(function(keyword) {
  this.searchProducts(keyword);
}, 500)
```

### 3. 缓存策略

- 首页数据缓存（5分钟）
- 用户信息本地缓存
- 图片缓存

```javascript
// 缓存示例
uni.setStorage({
  key: 'homeData',
  data: responseData,
  success: () => console.log('缓存成功')
});
```

### 4. 减少请求次数

- 合并接口请求
- 使用 WebSocket 推送（订单状态变更）
- 本地计算（购物车总价）

---

## 安全注意事项

### 1. Token 安全

- Token 存储在 `uni.storage`（加密存储更佳）
- HTTPS 传输
- 定期刷新 Token

### 2. 敏感信息保护

- 不要在代码中硬编码密钥
- 使用环境变量管理配置
- 生产环境关闭调试模式

### 3. 输入验证

- 前端表单验证
- 后端二次校验
- SQL 注入防护（MyBatis 参数化查询）

### 4. XSS 防护

- 富文本内容过滤
- 用户输入转义
- CSP 头配置

---

## 扩展开发

### 添加新页面

1. 在 `pages/` 目录下创建页面文件
2. 在 `pages.json` 中注册路由
3. 如需 TabBar，在 `tabBar.list` 中添加

### 添加新接口

1. 在 `api/` 目录下创建接口文件
2. 使用 `request` 封装 API 方法
3. 在页面中导入并调用

### 添加新组件

1. 在 `components/` 目录下创建组件
2. 遵循 Vue 单文件组件规范
3. 提供清晰的 Props 和 Events 文档

---

## 技术支持

### 官方文档

- uni-app 官方文档: https://uniapp.dcloud.io
- Vue.js 官方文档: https://vuejs.org
- luch-request 文档: https://github.com/lei-mu/luch-request

### 社区交流

- GitHub Issues: https://github.com/macrozheng/mall-app-web/issues
- 公众号: macrozheng
- 微信群: 关注公众号回复 "加群"

### 问题反馈

遇到问题请先:
1. 查阅本文档的 "常见问题" 章节
2. 搜索 GitHub Issues
3. 提交新 Issue（提供复现步骤、错误日志）

---

## 更新日志

### v1.0.0 (2024-01-01)

- ✅ 初始版本发布
- ✅ 完成核心电商功能
- ✅ 支持 H5、小程序、App 三端

---

## 开源协议

本项目基于 MIT 协议开源，详见 [LICENSE](./LICENSE) 文件。

---

## 致谢

感谢以下开源项目的支持:

- [uni-app](https://uniapp.dcloud.io) - 跨平台开发框架
- [Vue.js](https://vuejs.org) - 渐进式 JavaScript 框架
- [luch-request](https://github.com/lei-mu/luch-request) - HTTP 请求库
- [mall](https://github.com/macrozheng/mall) - 后端电商系统

---

**最后更新时间**: 2024-01-01

**文档维护者**: Mall 开发团队
