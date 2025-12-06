# 校园帮系统 - 前端项目

## 项目简介

校园帮系统前端应用，基于 Vue 3 + Vite + Element Plus 构建，提供现代化的用户界面和良好的用户体验。

## 技术栈

- **框架**: Vue 3.4.21
- **构建工具**: Vite 5.2.0
- **UI组件库**: Element Plus 2.6.3
- **图标库**: @element-plus/icons-vue 2.3.1
- **状态管理**: Pinia 2.1.7
- **路由**: Vue Router 4.3.0
- **HTTP客户端**: Axios 1.6.7
- **图表库**: ECharts 5.5.0
- **实时通信**: SockJS + STOMP.js

## 项目结构

```
campus-help-web/
├── src/
│   ├── api/                    # API接口定义
│   │   ├── index.js           # API统一导出
│   │   ├── address.js         # 收货地址API
│   │   ├── admin.js           # 管理员API
│   │   ├── auth.js            # 认证相关API
│   │   ├── file.js            # 文件上传API
│   │   ├── goods.js           # 闲置交易API
│   │   ├── lostFound.js       # 失物招领API
│   │   ├── message.js         # 系统消息API（包含聊天API）
│   │   ├── order.js           # 订单相关API
│   │   ├── question.js        # 学习互助API
│   │   ├── searchHistory.js   # 搜索历史API
│   │   └── user.js            # 用户相关API
│   ├── assets/                 # 静态资源
│   │   └── styles/
│   │       └── global.css     # 全局样式
│   ├── components/             # 公共组件（空目录）
│   ├── config/                 # 配置文件
│   │   └── index.js           # 全局配置
│   ├── layouts/                # 布局组件
│   │   ├── AdminLayout.vue    # 管理后台布局
│   │   └── MainLayout.vue     # 主布局（带顶部导航栏）
│   ├── router/                 # 路由配置
│   │   ├── guard.js           # 路由守卫
│   │   └── index.js           # 路由定义
│   ├── stores/                 # Pinia状态管理
│   │   ├── api.js             # API状态
│   │   └── user.js            # 用户状态
│   ├── utils/                  # 工具函数
│   │   ├── auth.js            # 认证工具
│   │   ├── constants.js       # 常量定义
│   │   ├── directives.js      # Vue3自定义指令
│   │   ├── echarts.js         # ECharts图表工具
│   │   ├── image.js           # 图片处理
│   │   ├── index.js           # 工具函数统一导出
│   │   ├── request.js         # Axios封装
│   │   └── websocket.js       # WebSocket管理
│   ├── views/                  # 页面组件（按模块组织）
│   │   ├── address/           # 收货地址模块
│   │   │   └── List.vue       # 地址列表
│   │   ├── admin/             # 管理后台模块
│   │   │   ├── content/       # 内容管理
│   │   │   │   ├── GoodsManagement.vue     # 闲置交易管理
│   │   │   │   ├── LostFoundManagement.vue # 失物招领管理
│   │   │   │   └── StudyManagement.vue    # 学习互助管理
│   │   │   ├── Dashboard.vue  # 数据概览
│   │   │   ├── GoodsAudit.vue # 闲置交易审核
│   │   │   ├── LostFoundAudit.vue # 失物招领审核
│   │   │   ├── QuestionAudit.vue # 学习问题审核
│   │   │   ├── SensitiveWord.vue # 敏感词配置
│   │   │   ├── Users.vue      # 用户管理
│   │   │   └── Verification.vue # 认证审核
│   │   ├── chat/              # 聊天模块
│   │   │   ├── components/    # 聊天组件
│   │   │   │   ├── GoodsCardMessage.vue # 商品卡片消息
│   │   │   │   └── OrderCard.vue        # 订单卡片
│   │   │   └── Chat.vue       # 聊天页面
│   │   ├── goods/             # 闲置交易模块
│   │   │   ├── Detail.vue     # 商品详情
│   │   │   ├── Edit.vue       # 编辑商品
│   │   │   ├── List.vue       # 商品列表
│   │   │   └── Publish.vue    # 发布商品
│   │   ├── lost-found/        # 失物招领模块
│   │   │   ├── Detail.vue     # 失物详情
│   │   │   ├── Edit.vue       # 编辑失物
│   │   │   ├── List.vue       # 失物列表
│   │   │   └── Publish.vue    # 发布失物
│   │   ├── message/           # 消息模块
│   │   │   └── Messages.vue   # 消息通知页面
│   │   ├── order/             # 订单模块
│   │   │   ├── components/    # 订单组件
│   │   │   │   └── OrderListContent.vue # 订单列表内容
│   │   │   ├── Detail.vue     # 订单详情
│   │   │   ├── List.vue       # 订单列表
│   │   │   └── Pay.vue        # 订单支付
│   │   ├── post/              # 发布模块
│   │   │   └── Posts.vue      # 我的发布页面
│   │   ├── profile/           # 个人资料模块
│   │   │   └── Profile.vue    # 个人中心页面
│   │   ├── study/             # 学习互助模块
│   │   │   ├── Detail.vue     # 问题详情
│   │   │   ├── Edit.vue       # 编辑问题
│   │   │   ├── List.vue       # 问题列表
│   │   │   ├── MyAnswers.vue  # 我的回答
│   │   │   ├── MyQuestions.vue # 我的问题
│   │   │   └── Publish.vue     # 发布问题
│   │   ├── task/              # 跑腿服务模块（空目录）
│   │   ├── verification/      # 认证模块
│   │   │   ├── components/    # 认证组件
│   │   │   │   └── VerificationForm.vue # 认证表单
│   │   │   └── Verification.vue # 实名认证页面
│   │   ├── ForgotPassword.vue # 忘记密码页
│   │   ├── Home.vue           # 首页
│   │   ├── Login.vue           # 登录页
│   │   └── Register.vue       # 注册页
│   ├── App.vue                 # 根组件
│   └── main.js                 # 入口文件
├── index.html                   # HTML模板
├── package.json                 # 项目配置
├── package-lock.json            # 依赖锁定文件
├── README.md                    # 项目说明文档
└── vite.config.js              # Vite配置
```

## 已实现功能

### 1. 用户认证模块 ✅

**页面**: `Login.vue`, `Register.vue`, `ForgotPassword.vue`

- ✅ 用户登录（密码登录、验证码登录）
- ✅ 用户注册（邮箱注册，验证码验证）
- ✅ 忘记密码（通过验证码重置）
- ✅ 验证码发送（带防刷机制）
- ✅ 邮箱唯一性检查

### 2. 首页模块 ✅

**页面**: `Home.vue`

- ✅ 欢迎横幅
- ✅ 功能模块导航（失物招领、闲置交易、学习互助）
- ✅ 用户个性化欢迎信息

### 3. 失物招领模块 ✅

**页面**: `lost-found/List.vue`, `lost-found/Detail.vue`, `lost-found/Publish.vue`, `lost-found/Edit.vue`

- ✅ 失物列表（支持搜索、筛选、分页）
- ✅ 失物详情（查看详细信息、认领记录）
- ✅ 发布失物（支持多图片上传）
- ✅ 编辑失物（仅发布者可编辑）
- ✅ 申请认领（提交认领申请，支持多个用户同时申请）
- ✅ 确认/拒绝认领（发布者操作，确认时自动拒绝其他申请）
- ✅ 我的发布列表
- ✅ 删除失物（逻辑删除）
- ✅ 关闭失物（用户下架）
- ✅ 丢失物品提供线索功能
- ✅ 根据物品类型显示不同的操作按钮和状态

### 4. 聊天功能模块 ✅

**页面**: `chat/Chat.vue`

- ✅ 会话列表（显示所有聊天会话）
- ✅ 消息列表（显示会话中的所有消息）
- ✅ 发送消息（支持文字、图片）
- ✅ 实时消息推送（WebSocket）
- ✅ 已读状态显示（实时更新）
- ✅ 消息时间显示
- ✅ 图片预览功能
- ✅ 多行文本输入（支持换行）
- ✅ 消息气泡样式（发送/接收区分）

**UI特性**:
- 发送人头像在消息气泡右侧
- 接收消息气泡有明显的边框和背景色
- 输入框支持多行和滚动（固定高度，无拖拽调整）
- 图片和发送按钮使用图标显示（右下角）
- Shift+Enter 换行，Enter 发送
- 输入框为空时显示提示信息
- 已读状态使用圆形打勾图标
- 实时更新已读状态（5秒轮询）

### 5. 系统消息模块 ✅

**页面**: `message/Messages.vue`

- ✅ 消息列表（分页显示）
- ✅ 未读消息数量显示
- ✅ 标记消息为已读
- ✅ 全部标记为已读
- ✅ 删除消息（逻辑删除）
- ✅ 批量删除消息（支持全选、单选）
- ✅ 消息类型分类显示（不同类型显示不同颜色和图标）
- ✅ 消息点击跳转（根据消息类型跳转到相关页面）

**消息通知面板**:
- ✅ 右下角消息通知按钮（带未读数量徽章）
- ✅ 消息通知面板（显示最近消息）
- ✅ 点击外部区域自动关闭
- ✅ 实时消息推送（WebSocket）
- ✅ 根据消息类型显示不同颜色和图标

### 6. 个人中心模块 ✅

**页面**: `profile/Profile.vue`

- ✅ 个人信息展示
- ✅ 更新个人信息
- ✅ 修改密码
- ✅ 头像上传

### 7. 实名认证模块 ✅

**页面**: `verification/Verification.vue`

- ✅ 提交实名认证（真实姓名、身份证、学号、证明文件）
- ✅ 查看认证状态（未认证、待审核、已认证、已拒绝）
- ✅ 认证审核结果展示

### 8. 我的发布模块 ✅

**页面**: `post/Posts.vue`

- ✅ 查看我的发布列表（失物招领、闲置交易、学习问题）
- ✅ 支持筛选和搜索
- ✅ 快速编辑和删除

### 9. 闲置交易模块 ✅

**页面**: `goods/List.vue`, `goods/Detail.vue`, `goods/Publish.vue`, `goods/Edit.vue`

- ✅ 商品列表（支持搜索、筛选、分页）
- ✅ 商品详情（查看详细信息、购买、聊天）
- ✅ 发布商品（支持多图片上传、价格设置、交易方式选择）
- ✅ 编辑商品（仅发布者可编辑）
- ✅ 购买商品（创建订单）
- ✅ 商品分类筛选（数码产品、图书教材、服装鞋包等）
- ✅ 商品成色筛选（全新、九成新、八成新等）
- ✅ 价格区间筛选
- ✅ 搜索历史记录
- ✅ 实名认证检查（发布和购买需要实名认证）

### 10. 订单模块 ✅

**页面**: `order/List.vue`, `order/Detail.vue`, `order/Pay.vue`

- ✅ 订单列表（买家/卖家视角，支持筛选）
- ✅ 订单详情（查看订单信息、物流信息、时间线）
- ✅ 订单支付
- ✅ 订单发货（填写物流信息）
- ✅ 确认收货
- ✅ 取消订单
- ✅ 订单改价（卖家操作）
- ✅ 订单状态跟踪（待付款、已付款、已发货、待自提、已完成、已取消）
- ✅ 交易方式支持（邮寄、自提）

### 11. 收货地址模块 ✅

**页面**: `address/List.vue`

- ✅ 地址列表（显示所有收货地址）
- ✅ 添加地址（收货人信息、地区、详细地址、邮编）
- ✅ 编辑地址
- ✅ 删除地址
- ✅ 设置默认地址
- ✅ 地址验证（手机号、邮编格式验证）

### 12. 学习互助模块 ✅

**页面**: `study/List.vue`, `study/Detail.vue`, `study/Publish.vue`, `study/Edit.vue`, `study/MyQuestions.vue`, `study/MyAnswers.vue`

- ✅ 问题列表（支持搜索、筛选、分页）
- ✅ 问题详情（查看问题、回答列表、提交回答）
- ✅ 发布问题（问题分类、标题、描述、悬赏金额、图片）
- ✅ 编辑问题（仅发布者可编辑）
- ✅ 回答问题（支持文字、图片）
- ✅ 采纳答案（发布者操作）
- ✅ 问题分类筛选（数学、物理、化学、计算机等）
- ✅ 问题状态筛选（待解答、已回答、已解决）
- ✅ 悬赏筛选
- ✅ 我的问题列表
- ✅ 我的回答列表
- ✅ 搜索历史记录

### 13. 管理后台模块 ✅

**页面**: `admin/Dashboard.vue`, `admin/Users.vue`, `admin/Verification.vue`, `admin/LostFoundAudit.vue`, `admin/GoodsAudit.vue`, `admin/QuestionAudit.vue`, `admin/SensitiveWord.vue`, `admin/content/*.vue`, `message/Messages.vue`

- ✅ 数据概览（统计信息展示、数据图表、趋势分析）
- ✅ 用户管理（用户列表、封禁/解封、新增/编辑/删除用户）
- ✅ 实名认证审核（待审核列表、审核操作）
- ✅ 失物招领审核（待审核列表、多条件筛选、审核操作）
- ✅ 闲置交易审核（待审核列表、多条件筛选、审核操作）
- ✅ 学习问题审核（待审核列表、多条件筛选、审核操作）
- ✅ 内容管理（失物招领管理、闲置交易管理、学习互助管理）
- ✅ 敏感词配置（敏感词列表、添加、删除、编辑）
- ✅ 管理员消息通知（查看所有管理员消息）
- ✅ 消息通知面板（管理员版，支持实时推送）

### 14. 文件上传功能 ✅

- ✅ 图片上传（支持 jpg、jpeg、png、gif）
- ✅ 文件大小限制（5MB）
- ✅ 多图片上传（失物发布、商品发布、聊天、学习问题等）
- ✅ 图片预览功能

### 15. WebSocket 实时通信 ✅

- ✅ 自动连接/重连机制
- ✅ 聊天消息实时推送
- ✅ 系统消息实时推送
- ✅ 连接状态管理
- ✅ 消息订阅/取消订阅管理

### 16. 搜索历史功能 ✅

- ✅ 搜索历史记录（失物招领、闲置交易、学习互助）
- ✅ 搜索历史展示（标签形式）
- ✅ 快速选择历史搜索
- ✅ 删除单个搜索历史
- ✅ 清空搜索历史

### 17. 聊天功能增强 ✅

- ✅ 商品卡片消息（在聊天中发送商品链接）
- ✅ 订单卡片消息（在聊天中发送订单信息）
- ✅ 消息类型扩展（文字、图片、商品卡片、订单卡片）

### 18. Vue3 自定义指令 ✅

**指令**: `v-click-outside` (`utils/directives.js`)

- ✅ 点击外部区域关闭面板
- ✅ 符合 Vue3 开发规范（不使用原生 DOM 操作）
- ✅ 全局注册，可在任何组件中使用

## 安装依赖

```bash
npm install
```

## 开发

```bash
npm run dev
```

项目将在 `http://localhost:3000` 启动

## 构建

```bash
npm run build
```

构建产物将输出到 `dist` 目录

## 环境变量配置

创建 `.env.development` 和 `.env.production` 文件：

```bash
# .env.development
VITE_APP_TITLE=校园帮系统
VITE_APP_BASE_API=/api
VITE_APP_TIMEOUT=30000
VITE_SERVER_PORT=3000
VITE_SERVER_PROXY_TARGET=http://localhost:8081
```

## API 代理配置

项目已配置 API 代理，所有 `/api` 开头的请求会被代理到后端服务器。

**代理配置** (`vite.config.js`):
- 开发环境: `/api` → `http://localhost:8081`
- 生产环境: 需要配置 Nginx 反向代理

## 路由说明

### 公开路由（无需登录）
- `/login` - 登录页
- `/register` - 注册页
- `/forgot-password` - 忘记密码页

### 用户路由（需要登录）
- `/home` - 首页
- `/lost-found/list` - 失物列表
- `/lost-found/detail/:id` - 失物详情
- `/lost-found/publish` - 发布失物
- `/lost-found/edit/:id` - 编辑失物
- `/goods/list` - 商品列表
- `/goods/detail/:id` - 商品详情
- `/goods/publish` - 发布商品
- `/goods/edit/:id` - 编辑商品
- `/order/list` - 我的交易（订单列表）
- `/order/detail/:id` - 订单详情
- `/order/pay/:id` - 订单支付
- `/address/list` - 收货地址
- `/study/list` - 学习互助（问题列表）
- `/study/publish` - 发布问题
- `/study/detail/:id` - 问题详情
- `/study/edit/:id` - 编辑问题
- `/study/my-questions` - 我的问题
- `/study/my-answers` - 我的回答
- `/user/profile` - 个人中心
- `/user/verification` - 实名认证
- `/user/messages` - 消息通知
- `/user/posts` - 我的发布
- `/user/chat` - 聊天

### 管理员路由（需要管理员权限）
- `/admin/dashboard` - 数据概览
- `/admin/users` - 用户管理
- `/admin/verification` - 认证审核
- `/admin/lost-found-audit` - 失物招领审核
- `/admin/goods-audit` - 闲置交易审核
- `/admin/question-audit` - 学习问题审核
- `/admin/content/lost-found` - 失物招领管理
- `/admin/content/goods` - 闲置交易管理
- `/admin/content/study` - 学习互助管理
- `/admin/sensitive-word` - 敏感词配置
- `/admin/messages` - 消息通知
- `/admin/profile` - 个人中心

## 状态管理

### UserStore (`stores/user.js`)
- 用户信息
- 登录状态
- Token 管理
- 用户操作（登录、注册、退出等）

### ApiStore (`stores/api.js`)
- API 连接状态
- 测试连接功能

## 工具函数

### request.js
- Axios 封装
- 请求/响应拦截器
- Token 自动添加
- 错误统一处理

### auth.js
- Token 存储和获取
- 用户信息存储

### websocket.js
- WebSocket 连接管理
- 自动重连机制
- 消息订阅/取消订阅

### image.js
- 图片URL处理
- 头像URL生成

### constants.js
- 常量定义（API路径、Token键名、状态码等）
- 统一管理项目常量

### echarts.js
- ECharts图表初始化
- 图表响应式调整
- 图表工具函数

### directives.js
- Vue3 自定义指令
- `v-click-outside` 指令（点击外部关闭）

## 开发规范

详细开发规范请参考: `doc/前端开发规范文档.md`

## 待实现功能

- [ ] 搜索功能优化（全局搜索、高级搜索）
- [ ] 商品/问题排序优化
- [ ] 更多数据统计图表
- [ ] 消息推送通知（浏览器通知）
- [ ] 移动端适配优化

## 注意事项

1. **API 地址**: 确保后端服务已启动（默认 `http://localhost:8081`）
2. **跨域问题**: 开发环境已配置代理，生产环境需要配置 Nginx
3. **Token 管理**: Token 存储在 localStorage，注意安全性
4. **WebSocket**: 确保后端 WebSocket 服务正常运行
5. **文件上传**: 确保后端文件上传路径配置正确

## 浏览器支持

- Chrome (推荐)
- Firefox
- Safari
- Edge

## 版本信息

- **Vue**: 3.4.21
- **Vite**: 5.2.0
- **Element Plus**: 2.6.3
- **Pinia**: 2.1.7
- **Vue Router**: 4.3.0
- **Axios**: 1.6.7
- **ECharts**: 5.5.0

## 联系方式

如有问题，请查看项目文档或联系开发团队。
