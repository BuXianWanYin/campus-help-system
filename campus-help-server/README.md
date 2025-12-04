# 校园帮系统 - 后端项目

## 项目简介

校园帮系统后端服务，基于 Spring Boot 2.7.18 开发，提供 RESTful API 接口和 WebSocket 实时通信功能。

## 技术栈

- **框架**: Spring Boot 2.7.18
- **数据库**: MySQL 8.0+
- **ORM**: MyBatis Plus 3.5.3.1
- **安全**: Spring Security + JWT
- **实时通信**: WebSocket (STOMP)
- **API文档**: SpringDoc OpenAPI 3 (Swagger)
- **连接池**: Druid 1.2.18
- **缓存**: Redis (可选)
- **邮件服务**: JavaMail
- **构建工具**: Maven

## 项目结构

```
campus-help-server/
├── src/main/java/com/server/campushelpserver/
│   ├── CampusHelpServerApplication.java    # 启动类
│   ├── common/                             # 通用类
│   │   ├── Result.java                    # 统一响应结果
│   │   └── ResultCode.java                # 响应码枚举
│   ├── config/                             # 配置类
│   │   ├── SecurityConfig.java            # Spring Security配置
│   │   ├── JwtConfig.java                 # JWT配置
│   │   ├── WebSocketConfig.java           # WebSocket配置
│   │   ├── MybatisPlusConfig.java         # MyBatis Plus配置
│   │   ├── FileConfig.java                # 文件上传配置
│   │   └── ...
│   ├── controller/                         # 控制器层
│   │   ├── auth/                          # 认证相关
│   │   ├── user/                           # 用户管理
│   │   ├── lostfound/                     # 失物招领
│   │   ├── chat/                          # 聊天功能
│   │   ├── message/                       # 系统消息
│   │   ├── admin/                         # 管理员后台
│   │   └── common/                        # 通用接口（文件上传等）
│   ├── service/                            # 服务层
│   │   ├── impl/                          # 服务实现
│   │   └── ...                           # 服务接口
│   ├── mapper/                             # 数据访问层
│   ├── entity/                             # 实体类
│   ├── dto/                                # 数据传输对象
│   ├── exception/                          # 异常处理
│   └── util/                               # 工具类
├── src/main/resources/
│   ├── application.yml                     # 主配置文件
│   └── application-dev.yml                 # 开发环境配置
└── pom.xml                                 # Maven依赖配置
```

## 已实现功能

### 1. 用户认证模块 ✅

**Controller**: `AuthController`

- ✅ 用户注册（邮箱注册，验证码验证）
- ✅ 用户登录（支持密码登录和验证码登录）
- ✅ 发送验证码（注册、登录、重置密码）
- ✅ 重置密码（通过验证码）
- ✅ 检查邮箱是否已注册

**安全特性**:
- JWT Token 认证
- BCrypt 密码加密
- 验证码防刷机制（基于IP和邮箱）
- 验证码有效期管理

### 2. 用户管理模块 ✅

**Controller**: `UserController`

- ✅ 获取当前用户信息
- ✅ 更新用户信息
- ✅ 提交实名认证
- ✅ 审核实名认证（管理员）
- ✅ 修改密码
- ✅ 分页查询用户列表（管理员）
- ✅ 根据ID获取用户信息（管理员）

**用户功能**:
- 个人信息管理
- 实名认证（提交、审核、状态管理）
- 用户封禁/解封（管理员）
- 用户角色管理（USER/ADMIN）

### 3. 失物招领模块 ✅

**Controller**: `LostFoundController`

- ✅ 发布失物信息
- ✅ 搜索失物列表（支持多条件筛选）
- ✅ 获取失物详情
- ✅ 申请认领失物
- ✅ 确认认领申请
- ✅ 拒绝认领申请
- ✅ 获取我的发布列表
- ✅ 获取认领记录列表
- ✅ 编辑失物信息
- ✅ 删除失物（逻辑删除）
- ✅ 关闭失物（用户下架）
- ✅ 获取我的申请
- ✅ 更新认领申请
- ✅ 删除认领申请

**业务特性**:
- 敏感词检测
- 发布频率限制
- 内容审核机制
- 认领流程管理
- 统计字段（浏览次数、收藏次数、评论次数）

### 4. 聊天功能模块 ✅

**Controller**: `ChatController`

- ✅ 创建或获取会话
- ✅ 获取会话列表
- ✅ 获取会话详情
- ✅ 发送消息（文字、图片）
- ✅ 获取消息列表
- ✅ WebSocket 实时推送消息
- ✅ 消息已读状态管理

**实时通信**:
- WebSocket (STOMP) 实时消息推送
- 消息持久化存储
- 已读状态实时更新
- 会话管理

### 5. 系统消息模块 ✅

**Controller**: `SystemMessageController`

- ✅ 分页查询系统消息
- ✅ 获取未读消息数量
- ✅ 标记消息为已读
- ✅ 全部标记为已读
- ✅ 标记聊天相关消息为已读
- ✅ 删除消息（逻辑删除）

**消息类型**:
- 实名认证审核结果
- 失物认领通知
- 系统公告
- 其他业务通知

### 6. 管理员后台模块 ✅

**Controller**: `AdminController`

- ✅ 获取待审核的实名认证列表
- ✅ 封禁用户（临时/永久）
- ✅ 解封用户

**管理功能**:
- 实名认证审核
- 用户封禁管理
- 用户列表查询

### 7. 文件上传模块 ✅

**Controller**: `FileController`

- ✅ 上传图片（支持 jpg、jpeg、png、gif）
- ✅ 文件大小限制（5MB）
- ✅ 按模块分类存储（avatar、lost-found、chat等）
- ✅ 按日期自动创建目录

## API 接口文档

启动项目后，访问 Swagger UI：
- 地址: `http://localhost:8080/swagger-ui.html`
- API文档: `http://localhost:8080/v3/api-docs`

## 数据库

- **数据库名称**: `campus_help`
- **字符集**: `utf8mb4`
- **存储引擎**: `InnoDB`

详细数据库说明请参考: `doc/数据库说明文档.md`

## 配置文件

### application.yml
主配置文件，包含数据库连接、JWT配置、文件上传路径等。

### application-dev.yml
开发环境配置，包含开发环境的特定配置。

**重要配置项**:
- 数据库连接信息
- JWT密钥和过期时间
- 文件上传路径
- 邮件服务配置（验证码发送）
- WebSocket配置

## 启动项目

### 前置条件
1. JDK 1.8+
2. Maven 3.6+
3. MySQL 8.0+
4. Redis (可选，如果使用缓存功能)

### 启动步骤

1. **配置数据库**
   - 创建数据库: `campus_help`
   - 修改 `application-dev.yml` 中的数据库连接信息

2. **安装依赖**
   ```bash
   mvn clean install
   ```

3. **启动项目**
   ```bash
   mvn spring-boot:run
   ```
   或使用IDE直接运行 `CampusHelpServerApplication.java`

4. **访问接口**
   - API地址: `http://localhost:8080/api`
   - Swagger文档: `http://localhost:8080/swagger-ui.html`

## 核心功能说明

### 1. JWT 认证机制

- 用户登录成功后生成 JWT Token
- Token 包含用户ID、邮箱、角色等信息
- 前端请求时在 Header 中携带: `Authorization: Bearer {token}`
- Token 过期时间可配置

### 2. 权限控制

- 使用 Spring Security 进行权限控制
- 基于角色的访问控制（ROLE_USER、ROLE_ADMIN）
- 使用 `@PreAuthorize` 注解进行方法级权限控制

### 3. 逻辑删除

- 所有表统一使用 `delete_flag` 字段进行逻辑删除
- 查询时自动过滤已删除数据
- 物理删除仅在特殊场景下使用

### 4. 敏感词检测

- 发布内容时自动检测敏感词
- 触发敏感词时自动进入人工审核
- 支持敏感词库管理

### 5. 发布频率限制

- 防止恶意刷屏
- 基于用户和时间的频率限制
- 超过限制时自动进入人工审核

### 6. WebSocket 实时通信

- 使用 STOMP 协议
- 支持点对点消息推送
- 消息持久化存储
- 已读状态实时更新

### 7. 文件上传

- 支持图片上传（jpg、jpeg、png、gif）
- 单张图片最大 5MB
- 按模块和日期自动分类存储
- 返回可访问的URL路径

## 开发规范

详细开发规范请参考: `doc/后端开发规范文档.md`

## 待实现功能

- [ ] 闲置交易模块（goods）
- [ ] 跑腿服务模块（errand_task）
- [ ] 订单管理模块（order）
- [ ] 评论功能模块（comment）
- [ ] 收藏功能模块（favorite）
- [ ] 数据统计模块
- [ ] 内容审核模块（管理员）

## 注意事项

1. **数据库连接**: 确保 MySQL 服务已启动，数据库已创建
2. **文件上传路径**: 确保配置的上传路径有写入权限
3. **邮件服务**: 如需使用验证码功能，需配置邮件服务
4. **JWT密钥**: 生产环境请修改 JWT 密钥
5. **跨域配置**: 已配置 CORS，支持前端跨域请求

## 版本信息

- **Spring Boot**: 2.7.18
- **Java**: 1.8+
- **MyBatis Plus**: 3.5.3.1
- **JWT**: 0.11.5

## 联系方式

如有问题，请查看项目文档或联系开发团队。

