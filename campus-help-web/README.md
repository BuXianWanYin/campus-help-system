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
│   │   ├── auth.js            # 认证相关API
│   │   ├── user.js            # 用户相关API
│   │   ├── lostFound.js       # 失物招领API
│   │   ├── chat.js            # 聊天相关API
│   │   ├── message.js         # 系统消息API
│   │   ├── file.js            # 文件上传API
│   │   └── admin.js           # 管理员API
│   ├── assets/                 # 静态资源
│   │   └── styles/
│   │       └── global.css     # 全局样式
│   ├── components/             # 公共组件
│   ├── config/                 # 配置文件
│   │   └── index.js           # 全局配置
│   ├── layouts/                # 布局组件
│   │   ├── MainLayout.vue     # 主布局（带顶部导航栏）
│   │   └── AdminLayout.vue    # 管理后台布局
│   ├── router/                 # 路由配置
│   │   ├── index.js           # 路由定义
│   │   └── guard.js           # 路由守卫
│   ├── stores/                 # Pinia状态管理
│   │   ├── user.js            # 用户状态
│   │   └── api.js             # API状态
│   ├── utils/                  # 工具函数
│   │   ├── request.js         # Axios封装
│   │   ├── auth.js            # 认证工具
│   │   ├── image.js           # 图片处理
│   │   ├── websocket.js       # WebSocket管理
│   │   └── ...
│   ├── views/                  # 页面组件（按模块组织）
│   │   ├── Home.vue           # 首页
│   │   ├── Login.vue          # 登录页
│   │   ├── Register.vue        # 注册页
│   │   ├── ForgotPassword.vue # 忘记密码页
│   │   ├── lost-found/        # 失物招领模块
│   │   │   ├── List.vue       # 失物列表
│   │   │   ├── Detail.vue     # 失物详情
│   │   │   ├── Publish.vue    # 发布失物
│   │   │   └── Edit.vue       # 编辑失物
│   │   ├── goods/              # 闲置交易模块
│   │   │   └── List.vue       # 商品列表（开发中）
│   │   ├── task/               # 跑腿服务模块
│   │   │   └── List.vue       # 任务列表（开发中）
│   │   ├── chat/               # 聊天模块
│   │   │   └── Chat.vue       # 聊天页面
│   │   ├── message/            # 消息模块
│   │   │   └── Messages.vue    # 消息通知页面
│   │   ├── post/               # 发布模块
│   │   │   └── Posts.vue      # 我的发布页面
│   │   ├── profile/            # 个人资料模块
│   │   │   └── Profile.vue    # 个人中心页面
│   │   ├── verification/      # 认证模块
│   │   │   ├── Verification.vue # 实名认证页面
│   │   │   └── components/
│   │   │       └── VerificationForm.vue
│   │   └── admin/              # 管理后台模块
│   │       ├── Dashboard.vue   # 数据概览
│   │       ├── Users.vue       # 用户管理
│   │       └── Verification.vue # 认证审核
│   ├── App.vue                 # 根组件
│   └── main.js                 # 入口文件
├── index.html                   # HTML模板
├── vite.config.js              # Vite配置
└── package.json                # 项目配置
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
- ✅ 功能模块导航（失物招领、闲置交易、跑腿服务）
- ✅ 用户个性化欢迎信息

### 3. 失物招领模块 ✅

**页面**: `lost-found/List.vue`, `lost-found/Detail.vue`, `lost-found/Publish.vue`, `lost-found/Edit.vue`

- ✅ 失物列表（支持搜索、筛选、分页）
- ✅ 失物详情（查看详细信息、认领记录）
- ✅ 发布失物（支持多图片上传）
- ✅ 编辑失物（仅发布者可编辑）
- ✅ 申请认领（提交认领申请）
- ✅ 确认/拒绝认领（发布者操作）
- ✅ 我的发布列表
- ✅ 删除失物（逻辑删除）
- ✅ 关闭失物（用户下架）

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
- 输入框支持多行和滚动
- 图片和发送按钮使用图标显示

### 5. 系统消息模块 ✅

**页面**: `message/Messages.vue`

- ✅ 消息列表（分页显示）
- ✅ 未读消息数量显示
- ✅ 标记消息为已读
- ✅ 全部标记为已读
- ✅ 删除消息（逻辑删除）
- ✅ 消息类型分类显示

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

- ✅ 查看我的发布列表（失物招领）
- ✅ 支持筛选和搜索
- ✅ 快速编辑和删除

### 9. 管理后台模块 ✅

**页面**: `admin/Dashboard.vue`, `admin/Users.vue`, `admin/Verification.vue`

- ✅ 数据概览（统计信息展示）
- ✅ 用户管理（用户列表、封禁/解封）
- ✅ 实名认证审核（待审核列表、审核操作）

### 10. 文件上传功能 ✅

- ✅ 图片上传（支持 jpg、jpeg、png、gif）
- ✅ 文件大小限制（5MB）
- ✅ 多图片上传（失物发布、聊天等）
- ✅ 图片预览功能

### 11. WebSocket 实时通信 ✅

- ✅ 自动连接/重连机制
- ✅ 聊天消息实时推送
- ✅ 系统消息实时推送
- ✅ 连接状态管理

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
VITE_SERVER_PROXY_TARGET=http://localhost:8080
```

## API 代理配置

项目已配置 API 代理，所有 `/api` 开头的请求会被代理到后端服务器。

**代理配置** (`vite.config.js`):
- 开发环境: `/api` → `http://localhost:8080`
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
- `/user/profile` - 个人中心
- `/user/verification` - 实名认证
- `/user/messages` - 消息通知
- `/user/posts` - 我的发布
- `/user/chat` - 聊天

### 管理员路由（需要管理员权限）
- `/admin/dashboard` - 数据概览
- `/admin/users` - 用户管理
- `/admin/verification` - 认证审核

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

## 开发规范

详细开发规范请参考: `doc/前端开发规范文档.md`

## 待实现功能

- [ ] 闲置交易模块（商品列表、发布、详情等）
- [ ] 跑腿服务模块（任务列表、发布、接单等）
- [ ] 评论功能
- [ ] 收藏功能
- [ ] 搜索功能优化
- [ ] 数据统计图表

## 注意事项

1. **API 地址**: 确保后端服务已启动（默认 `http://localhost:8080`）
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
