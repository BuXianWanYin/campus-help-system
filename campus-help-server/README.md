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
- **缓存**: Redis 
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
│   │   ├── JwtAuthenticationFilter.java   # JWT认证过滤器
│   │   ├── WebSocketConfig.java           # WebSocket配置
│   │   ├── WebSocketInterceptor.java      # WebSocket拦截器
│   │   ├── MybatisPlusConfig.java         # MyBatis Plus配置
│   │   ├── RedisConfig.java               # Redis配置
│   │   ├── FileConfig.java                # 文件上传配置
│   │   ├── OpenApiConfig.java             # SpringDoc OpenAPI配置
│   │   ├── JacksonConfig.java             # Jackson配置
│   │   └── AsyncConfig.java               # 异步任务配置
│   ├── controller/                         # 控制器层
│   │   ├── user/                           # 用户管理
│   │   │   └── UserController.java        # 用户控制器
│   │   ├── admin/                         # 管理员后台
│   │   │   ├── AdminController.java       # 管理员控制器
│   │   │   ├── AdminQuestionController.java # 问题审核控制器
│   │   │   └── SensitiveWordController.java # 敏感词管理控制器
│   │   ├── AuthController.java            # 认证控制器
│   │   ├── LostFoundController.java       # 失物招领控制器
│   │   ├── GoodsController.java           # 商品控制器
│   │   ├── OrderController.java           # 订单控制器
│   │   ├── QuestionController.java        # 问题控制器
│   │   ├── AddressController.java         # 地址控制器
│   │   ├── ChatController.java            # 聊天控制器
│   │   ├── SystemMessageController.java   # 系统消息控制器
│   │   ├── SearchHistoryController.java   # 搜索历史控制器
│   │   └── FileController.java            # 文件上传控制器
│   ├── service/                            # 服务层
│   │   ├── impl/                          # 服务实现
│   │   │   ├── user/                      # 用户服务实现
│   │   │   ├── admin/                     # 管理员服务实现（如有）
│   │   │   ├── AddressServiceImpl.java    # 地址服务实现
│   │   │   ├── LostFoundServiceImpl.java  # 失物招领服务实现
│   │   │   ├── GoodsServiceImpl.java      # 商品服务实现
│   │   │   ├── OrderServiceImpl.java      # 订单服务实现
│   │   │   ├── QuestionServiceImpl.java   # 学习互助服务实现
│   │   │   ├── ChatSessionServiceImpl.java # 聊天服务实现
│   │   │   ├── EmailServiceImpl.java      # 邮件服务实现
│   │   │   ├── SystemMessageServiceImpl.java # 系统消息服务实现
│   │   │   ├── SearchHistoryServiceImpl.java # 搜索服务实现
│   │   │   ├── SensitiveWordServiceImpl.java # 敏感词服务实现
│   │   │   └── PublishFrequencyServiceImpl.java # 发布频率检测服务实现
│   │   ├── user/                          # 用户服务接口
│   │   ├── AddressService.java            # 地址服务接口
│   │   ├── LostFoundService.java          # 失物招领服务接口
│   │   ├── GoodsService.java              # 商品服务接口
│   │   ├── OrderService.java              # 订单服务接口
│   │   ├── QuestionService.java           # 学习互助服务接口
│   │   ├── ChatSessionService.java        # 聊天服务接口
│   │   ├── EmailService.java              # 邮件服务接口
│   │   ├── SystemMessageService.java      # 系统消息服务接口
│   │   ├── SearchHistoryService.java      # 搜索服务接口
│   │   ├── SensitiveWordService.java      # 敏感词服务接口
│   │   └── PublishFrequencyService.java   # 发布频率检测服务接口
│   ├── mapper/                             # 数据访问层
│   │   ├── user/                          # 用户Mapper
│   │   │   └── UserMapper.java            # 用户Mapper接口
│   │   ├── AddressMapper.java            # 地址Mapper接口
│   │   ├── LostFoundMapper.java          # 失物招领Mapper接口
│   │   ├── ClaimRecordMapper.java        # 认领记录Mapper接口
│   │   ├── GoodsMapper.java              # 商品Mapper接口
│   │   ├── OrderMapper.java              # 订单Mapper接口
│   │   ├── StudyQuestionMapper.java      # 学习问题Mapper接口
│   │   ├── StudyAnswerMapper.java        # 学习回答Mapper接口
│   │   ├── ChatSessionMapper.java        # 聊天会话Mapper接口
│   │   ├── ChatMessageMapper.java        # 聊天消息Mapper接口
│   │   ├── SystemMessageMapper.java      # 系统消息Mapper接口
│   │   ├── SearchHistoryMapper.java       # 搜索历史Mapper接口
│   │   └── SensitiveWordMapper.java      # 敏感词Mapper接口
│   │   └── sensitive/                     # 敏感词Mapper
│   ├── entity/                             # 实体类
│   │   ├── user/                          # 用户实体
│   │   ├── lostfound/                     # 失物招领实体
│   │   ├── goods/                         # 商品实体
│   │   ├── order/                         # 订单实体
│   │   ├── study/                         # 学习互助实体
│   │   ├── address/                       # 地址实体
│   │   ├── chat/                          # 聊天实体
│   │   ├── message/                       # 消息实体
│   │   ├── search/                        # 搜索实体
│   │   ├── sensitive/                     # 敏感词实体
│   │   └── admin/                         # 管理员实体
│   ├── dto/                                # 数据传输对象（部分旧DTO）
│   │   └── user/                          # 用户DTO
│   ├── exception/                          # 异常处理
│   │   ├── BusinessException.java         # 业务异常
│   │   └── GlobalExceptionHandler.java    # 全局异常处理器
│   └── util/                               # 工具类
│       ├── SecurityUtils.java             # 安全工具类
│       ├── AhoCorasick.java               # AC自动机算法
│       ├── SensitiveWordCheckResult.java   # 敏感词检测结果
│       └── PasswordGenerator.java         # 密码生成工具
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
- ✅ 申请认领失物（支持多个用户同时申请）
- ✅ 确认认领申请（自动拒绝其他待处理申请）
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
- 内容审核机制（自动审核/人工审核）
- 认领流程管理（支持多认领申请）
- 确认认领时自动拒绝其他申请并发送通知
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
- 失物认领通知（认领申请、确认认领、拒绝认领）
- 失物审核通知（审核通过、审核拒绝）
- 管理员审核通知（待审核提醒）
- 系统公告
- 其他业务通知

**消息发送机制**:
- 支持发送消息给单个用户
- 支持发送消息给所有管理员（用于审核提醒）
- 自动发送系统消息（认领申请、审核结果等）

### 6. 闲置交易模块 ✅

**Controller**: `GoodsController`

- ✅ 发布商品信息
- ✅ 搜索商品列表（支持多条件筛选）
- ✅ 获取商品详情
- ✅ 编辑商品信息
- ✅ 删除商品（逻辑删除）
- ✅ 下架/上架商品
- ✅ 获取我的发布列表

**业务特性**:
- 敏感词检测
- 发布频率限制
- 内容审核机制（自动审核/人工审核）
- 库存管理
- 商品状态管理（在售、已下架、已售完等）
- 统计字段（浏览次数、收藏次数等）

### 7. 订单管理模块 ✅

**Controller**: `OrderController`

- ✅ 创建订单
- ✅ 获取订单列表（我买到的/我卖出的）
- ✅ 获取订单详情
- ✅ 修改订单价格（卖家）
- ✅ 支付订单（买家）
- ✅ 发货订单（卖家，邮寄方式）
- ✅ 确认收货（买家）
- ✅ 取消订单（待付款状态）

**业务特性**:
- 订单状态流转（待付款→已付款→已发货/待自提→已完成）
- 库存检查和乐观锁机制
- 交易方式支持（自提/邮寄）
- 收货地址管理
- 订单价格修改记录

### 8. 地址管理模块 ✅

**Controller**: `AddressController`

- ✅ 添加收货地址
- ✅ 获取地址列表
- ✅ 更新地址信息
- ✅ 删除地址（逻辑删除）
- ✅ 设置默认地址

**业务特性**:
- 地址信息验证
- 默认地址管理
- 地址数量限制

### 9. 学习互助模块 ✅

**Controller**: `QuestionController`

- ✅ 发布问题
- ✅ 搜索问题列表（支持多条件筛选）
- ✅ 获取问题详情
- ✅ 回答问题
- ✅ 采纳答案
- ✅ 编辑问题
- ✅ 删除问题（逻辑删除）
- ✅ 获取我的问题列表
- ✅ 获取我的回答列表

**业务特性**:
- 敏感词检测
- 发布频率限制
- 内容审核机制（自动审核/人工审核）
- 悬赏机制（可选）
- 问题状态管理（待解答、已回答、已解决）
- 答案采纳机制

### 10. 搜索历史模块 ✅

**Controller**: `SearchHistoryController`

- ✅ 保存搜索历史
- ✅ 获取搜索历史列表
- ✅ 删除搜索历史
- ✅ 清空搜索历史

**业务特性**:
- 搜索关键词记录
- 搜索历史去重
- 搜索历史数量限制

### 11. 管理员后台模块 ✅

**Controller**: `AdminController`, `AdminQuestionController`, `SensitiveWordController`

**用户管理**:
- ✅ 获取待审核的实名认证列表
- ✅ 审核实名认证（通过/拒绝）
- ✅ 封禁用户（临时/永久）
- ✅ 解封用户
- ✅ 分页查询用户列表
- ✅ 获取用户详情

**内容审核**:
- ✅ 获取待审核的失物招领列表（支持多条件筛选）
- ✅ 审核失物招领（通过/拒绝）
- ✅ 获取待审核的商品列表（支持多条件筛选）
- ✅ 审核商品（通过/拒绝）
- ✅ 获取待审核的问题列表
- ✅ 审核问题（通过/拒绝）
- ✅ 下架违规内容

**数据统计**:
- ✅ 获取数据概览统计
- ✅ 获取每日趋势数据
- ✅ 获取用户统计数据
- ✅ 获取互助类型分布

**敏感词管理**:
- ✅ 获取敏感词列表
- ✅ 添加敏感词
- ✅ 更新敏感词
- ✅ 删除敏感词
- ✅ 批量删除敏感词
- ✅ 重新加载敏感词库

**管理功能**:
- 实名认证审核
- 内容审核（失物招领、闲置交易、学习互助）
- 用户封禁管理
- 用户列表查询
- 审核记录管理（记录审核原因、审核时间、审核管理员）
- 敏感词库管理（支持普通/严重敏感词分级）

### 12. 文件上传模块 ✅

**Controller**: `FileController`

- ✅ 上传图片（支持 jpg、jpeg、png、gif）
- ✅ 文件大小限制（5MB）
- ✅ 按模块分类存储（avatar、lost-found、goods、chat、study、verification等）
- ✅ 按日期自动创建目录
- ✅ 返回可访问的URL路径

### 13. 认领流程优化 ✅

**业务逻辑**:
- ✅ 支持多个用户同时申请认领同一件"招领物品"
- ✅ 发布者可以查看所有认领申请
- ✅ 申请人只能看到自己的申请
- ✅ 确认认领时自动拒绝其他待处理申请
- ✅ 被拒绝的申请人会收到系统消息通知

## API 接口文档

启动项目后，访问 Swagger UI：
- 地址: `http://localhost:8081/swagger-ui/index.html`

## 数据库

- **数据库名称**: `campus_help`
- **字符集**: `utf8mb4`
- **存储引擎**: `InnoDB`

详细数据库说明请参考: `doc/数据库操作文档.md`

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
   - API地址: `http://localhost:8081/api`
   - Swagger文档: `http://localhost:8081/swagger-ui.html`

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

- 发布内容时自动检测敏感词（使用AC自动机算法）
- 支持敏感词分级（普通/严重）
- 严重敏感词：自动拒绝并保存记录
- 普通敏感词：转人工审核
- 支持敏感词库动态管理（添加、编辑、删除、重新加载）

### 5. 发布频率限制

- 防止恶意刷屏
- 基于用户和时间的频率限制
- 超过限制时自动进入人工审核

### 6. WebSocket 实时通信

- 使用 STOMP 协议
- 支持点对点消息推送
- 消息持久化存储
- 已读状态实时更新
- JWT认证拦截器（WebSocket连接时进行身份验证）
- 支持发送商品卡片、订单卡片等特殊消息类型

### 7. 文件上传

- 支持图片上传（jpg、jpeg、png、gif）
- 单张图片最大 5MB
- 按模块和日期自动分类存储
- 返回可访问的URL路径

## 开发规范

详细开发规范请参考: `doc/后端开发规范文档.md`

## 待实现功能

### 核心功能
- [ ] 跑腿服务模块（errand_task）
  - 跑腿任务发布
  - 跑腿任务接单
  - 跑腿任务管理
  - 跑腿任务评价

### 可选功能
- [ ] 评论功能模块（comment）
  - 商品评论
  - 失物评论
  - 评论回复

- [ ] 收藏功能模块（favorite）
  - 商品收藏
  - 失物收藏
  - 收藏列表管理

- [ ] 订单评价功能
  - 买卖双方评价
  - 评价展示

### 功能优化
- [ ] 学习互助模块前端页面完善
  - 我的问题页面（后端接口已实现）
  - 我的回答页面（后端接口已实现）

- [ ] 数据统计功能增强
  - 更详细的统计报表
  - 数据导出功能

## 注意事项

1. **数据库连接**: 确保 MySQL 服务已启动，数据库已创建
2. **文件上传路径**: 确保配置的上传路径有写入权限
3. **邮件服务**: 如需使用验证码功能，需配置邮件服务（SMTP配置）
4. **JWT密钥**: 生产环境请修改 JWT 密钥（application-dev.yml中的security.jwt.secret）
5. **跨域配置**: 已配置 CORS，支持前端跨域请求
6. **Redis配置**: 如需使用缓存功能，需配置Redis连接（可选）
7. **逻辑删除**: 所有删除操作使用逻辑删除，通过delete_flag字段控制
8. **敏感词库**: 首次启动时会自动加载敏感词库，可通过管理员后台管理
9. **文件存储**: 上传的文件存储在项目根目录的uploads文件夹下，按模块和日期分类
10. **WebSocket**: WebSocket连接需要JWT认证，前端需在连接时携带Token

## 版本信息

- **Spring Boot**: 2.7.18
- **Java**: 1.8+
- **MyBatis Plus**: 3.5.3.1
- **JWT**: 0.11.5
- **SpringDoc OpenAPI**: 1.7.0
- **Druid**: 1.2.18
- **Redis**: Spring Boot Starter Data Redis
- **Lombok**: 1.18.28
- **MapStruct**: 1.5.5.Final

## 功能完成度

根据功能完成度检查报告（`doc/功能完成度检查报告.md`），当前项目核心功能完成度：

- ✅ **用户管理模块**: 100%
- ✅ **失物招领模块**: 100%
- ✅ **闲置交易模块**: 100%
- ✅ **学习互助模块**: 100%
- ✅ **消息通知模块**: 100%
- ✅ **管理员功能**: 100%
- ✅ **订单管理模块**: 100%
- ✅ **地址管理模块**: 100%
- ✅ **搜索历史模块**: 100%
- ✅ **敏感词管理**: 100%

**总体完成度**: 核心功能已全部实现 ✅

## 联系方式

如有问题，请查看项目文档或联系开发团队。

