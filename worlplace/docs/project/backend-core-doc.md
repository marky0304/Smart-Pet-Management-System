# 智慧宠物管理系统 - 后端核心代码说明

## 一、项目概览

| 属性 | 值 |
|------|-----|
| 项目名称 | smart-pet-system（智慧宠物管理系统） |
| 技术框架 | Spring Boot 2.7.14 |
| ORM | MyBatis Plus 3.5.3.1 |
| 数据库 | MySQL 8.x |
| 缓存 | Redis（Lettuce 连接池） |
| 认证 | JWT（jjwt 0.11.5） |
| 加密 | BCrypt（Spring Security Crypto） |
| JSON | FastJSON2 + Jackson |
| HTTP 客户端 | OkHttp 4.9.3 |
| 工具库 | Hutool 5.8.20 |
| 构建工具 | Maven |
| Java 版本 | 1.8 |
| 服务端口 | 8080，上下文路径 `/api` |

---

## 二、项目包结构

```
com.pet
├── SmartPetSystemApplication.java     # 启动类
├── common/
│   ├── result/Result.java             # 统一响应封装
│   └── exception/
│       ├── BusinessException.java     # 业务异常
│       └── GlobalExceptionHandler.java # 全局异常处理
├── config/
│   ├── MybatisPlusConfig.java         # MyBatis Plus 分页插件
│   └── PasswordConfig.java            # BCrypt 加密 Bean
├── interceptor/
│   └── AuthInterceptor.java           # JWT 认证拦截器
├── utils/
│   └── JwtUtil.java                   # JWT 工具类
├── entity/                            # 数据库实体
│   ├── User.java                      # 用户
│   ├── Pet.java                       # 宠物
│   ├── HealthRecord.java              # 健康记录
│   ├── Appointment.java               # 预约
│   ├── PetService.java                # 服务项目
│   ├── Product.java                   # 商品
│   ├── OrderItem.java                 # 订单项
│   ├── CommunityPost.java             # 社区动态
│   ├── CommunityComment.java          # 社区评论
│   └── CommunityLike.java             # 社区点赞
├── dto/                               # 请求 DTO
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── UpdatePasswordRequest.java
│   ├── PetRequest.java
│   ├── AppointmentRequest.java
│   └── ...
├── vo/                                # 响应 VO
│   ├── LoginResponse.java
│   ├── UserVO.java
│   ├── PetVO.java
│   └── ...
├── mapper/                            # MyBatis Mapper 接口
├── service/                           # 业务接口 + 实现
│   └── impl/
├── controller/                        # REST 控制器
│   ├── AuthController.java            # 认证（注册/登录）
│   ├── UserController.java            # 用户管理
│   ├── PetController.java             # 宠物管理
│   ├── HealthController.java          # 健康记录
│   ├── ServiceController.java         # 服务项目
│   ├── AppointmentController.java     # 预约管理
│   ├── CommunityController.java       # 社区
│   ├── FileUploadController.java      # 文件上传
│   └── LoginBackgroundController.java # 登录背景
└── agent/                             # AI 智能客服子系统
    ├── config/LlmConfig.java          # LLM 配置
    ├── core/
    │   ├── Agent.java                 # Agent 接口
    │   ├── AgentContext.java          # Agent 上下文
    │   ├── AgentOrchestrator.java     # Agent 编排器
    │   ├── IntentRouter.java          # 意图路由
    │   ├── ActionExecutor.java        # 动作执行器
    │   ├── DomainType.java            # 领域枚举
    │   └── OperationType.java         # 操作类型枚举
    ├── agents/                        # 各领域 Agent
    │   ├── ArchiveAgent.java          # 宠物档案
    │   ├── HealthAgent.java           # 健康管理
    │   ├── MallAgent.java             # 商城
    │   ├── AdvisorAgent.java          # 饲养顾问
    │   └── GeneralAgent.java          # 通用/闲聊
    ├── llm/LlmClient.java             # LLM API 客户端
    ├── service/impl/ChatServiceImpl.java # 对话服务
    ├── controller/ChatController.java # 对话接口
    └── scheduler/ReminderScheduler.java # 提醒定时任务
```

---

## 三、核心架构设计

### 3.1 分层架构

```
┌─────────────────────────────────────────────┐
│              Controller 层                    │
│  REST API 接口，参数校验，权限检查              │
├─────────────────────────────────────────────┤
│              Service 层                       │
│  业务逻辑，事务管理，DTO/VO 转换               │
├─────────────────────────────────────────────┤
│              Mapper 层                        │
│  MyBatis Plus BaseMapper，数据访问            │
├─────────────────────────────────────────────┤
│              Entity 层                        │
│  数据库表映射实体，使用 Lombok @Data           │
└─────────────────────────────────────────────┘
```

- **Controller** 负责接收 HTTP 请求、参数校验、调用 Service、返回 `Result<T>` 统一响应
- **Service** 继承 MyBatis Plus 的 `IService<T>` / `ServiceImpl<M, T>`，自动获得 CRUD 能力
- **Mapper** 继承 `BaseMapper<T>`，自动获得单表 CRUD
- **Entity** 使用 `@TableName`、`@TableId`、`@TableLogic` 等注解映射

### 3.2 统一响应格式

所有 API 返回 `Result<T>` 结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

- `Result.success(data)` → code=200
- `Result.error(message)` → code=500
- `Result.error(code, message)` → 自定义错误码

### 3.3 异常处理机制

- **BusinessException**：业务异常，携带 code 和 message，由 `GlobalExceptionHandler` 统一捕获
- **@RestControllerAdvice**：全局异常拦截，分类处理：
  - `BusinessException` → 返回对应 code/message
  - `MethodArgumentNotValidException` / `BindException` → 参数校验失败，返回 400
  - `Exception` → 兜底，返回通用 "系统异常" 消息（不暴露内部细节）

---

## 四、认证与授权

### 4.1 JWT 认证流程

```
用户登录 → 验证账号密码 → 生成 JWT Token → 返回 Token
                          ↓
            后续请求携带 Authorization: Bearer <token>
                          ↓
            AuthInterceptor 拦截验证 → 解析 userId/username/role → 存入 request 属性
```

### 4.2 JwtUtil 核心方法

| 方法 | 说明 |
|------|------|
| `generateToken(userId, username, role)` | 生成 7 天有效期的 JWT |
| `parseToken(token)` | 解析 Token，返回 Claims |
| `getUserIdFromToken(token)` | 提取用户 ID |
| `getUsernameFromToken(token)` | 提取用户名 |
| `getRoleFromToken(token)` | 提取角色 |
| `validateToken(token)` | 验证 Token 是否有效 |

### 4.3 配置项

```yaml
jwt:
  secret: smart-pet-system-secret-key-2024
  expiration: 604800000   # 7天（毫秒）
  header: Authorization
  prefix: "Bearer "
```

### 4.4 密码安全

- 使用 **BCryptPasswordEncoder** 加密存储
- 登录验证：`passwordEncoder.matches(rawPassword, encodedPassword)`
- 重置密码验证码通过 Redis 存储，5 分钟有效期
- Redis 不可用时降级为固定测试验证码 `123456`

---

## 五、API 路由总览

| 模块 | 基础路径 | 主要接口 |
|------|---------|---------|
| 认证 | `/api/auth` | 注册、登录、重置密码、修改密码 |
| 用户 | `/api/user` | 用户信息、列表（管理员）、更新、删除、统计 |
| 宠物 | `/api/pet` | 宠物 CRUD、品种列表、管理端统计 |
| 健康 | `/api/health` | 健康记录 CRUD、趋势分析 |
| 服务 | `/api/service` | 服务项目管理 |
| 预约 | `/api/appointment` | 预约 CRUD、状态管理 |
| 社区 | `/api/community` | 动态/评论/点赞、话题、热门内容 |
| 商城 | `/api/shop` | 商品、购物车、订单、评价 |
| 上传 | `/api/upload` | 图片/视频/头像上传 |
| 智能客服 | `/api/chat` | AI 对话、历史记录 |
| 提醒 | `/api/reminder` | 提醒 CRUD |
| 登录背景 | `/api/loginBackground` | 登录页背景图管理 |

---

## 六、智能客服 AI Agent 系统

### 6.1 整体架构

```
用户消息
  ↓
ChatController (/api/chat/send)
  ↓
ChatServiceImpl
  ↓
AgentOrchestrator.process()
  ├── IntentRouter.route()          # LLM 意图识别 → Domain + Operation
  ├── Agent.buildSystemPrompt()     # 构建领域提示词
  ├── Agent.buildDataContext()      # 注入用户数据上下文
  ├── LlmClient.chat()             # 调用 LLM 获取回复
  └── ActionExecutor.process()     # 解析并执行动作 JSON
  ↓
ChatResponse (reply + operation + domain + navigate + action)
```

### 6.2 意图路由

**IntentRouter** 是双层路由机制：

1. **LLM 路由（主路径）**：向 LLM 发送分类 Prompt，要求返回 `{"operation":"QUERY","domain":"HEALTH"}` 格式的 JSON
2. **关键词兜底（降级路径）**：LLM 调用失败时，基于中文关键词规则匹配

分类维度：
- **Operation**：QUERY / ADD / MODIFY / DELETE / CHAT
- **Domain**：PET / HEALTH / MALL / GENERAL / CHAT

### 6.3 多 Agent 架构

每个 Agent 实现 `Agent` 接口：

```java
public interface Agent {
    DomainType getDomain();
    String getName();
    String buildSystemPrompt(AgentContext ctx, OperationType operation);
    String buildDataContext(AgentContext ctx);
}
```

| Agent | 领域 | 职责 |
|-------|------|------|
| ArchiveAgent | PET | 宠物档案查询、添加、修改 |
| HealthAgent | HEALTH | 健康记录、疫苗提醒、疾病咨询 |
| MallAgent | MALL | 商品搜索、推荐、下单 |
| AdvisorAgent | GENERAL | 饲养建议、训练指导、护理知识 |
| GeneralAgent | CHAT | 闲聊、问候、兜底处理 |

### 6.4 ActionExecutor 动作执行

LLM 回复中嵌入 `{"action":{"operation":"ADD","entity":"ORDER","fields":{...}}}` JSON，由 ActionExecutor 解析并执行：

| entity | 支持操作 | 说明 |
|--------|---------|------|
| APPOINTMENT | ADD | 创建预约 |
| HEALTH_RECORD | ADD | 添加健康记录 |
| PET | ADD / MODIFY | 添加/修改宠物信息 |
| REMINDER | ADD | 创建提醒 |
| ORDER | ADD | 创建订单 |
| PRODUCT_SEARCH | QUERY | 商品搜索 |
| CART_ADD | ADD | 加入购物车 |
| PAGE_NAVIGATE | — | 页面导航 |

执行成功后返回确认消息和前端导航指令（navigate + navigateParams）。

### 6.5 LlmClient LLM 客户端

- 支持 **OpenAI 兼容格式**（DeepSeek 标准 API）和 **Anthropic Messages API** 两种格式
- 自动根据 API URL 后缀判断协议类型
- 配置优先级：**数据库动态配置 > application.yml 静态配置**
- 通过 `ChatConfigService` 从数据库 `chat_config` 表读取运行时可调参数
- 支持开关控制：`chat.enabled` 为 `false` 时关闭智能客服

### 6.6 对话管理

- 每次对话生成 8 位 sessionId
- 用户消息和 AI 回复均持久化到 `chat_message` 表
- 每次请求加载最近 20 条历史消息作为上下文
- Markdown 格式回复自动剥离格式化标记后展示

---

## 七、数据访问层

### 7.1 MyBatis Plus 配置

- **分页插件**：`PaginationInnerInterceptor`，数据库类型 MySQL
- **主键策略**：`IdType.AUTO` 数据库自增
- **逻辑删除**：`@TableLogic(value = "1", delval = "0")`，status 字段 1=正常 0=删除
- **驼峰转换**：`map-underscore-to-camel-case: true`
- **SQL 日志**：`StdOutImpl` 控制台输出

### 7.2 核心实体

| 实体 | 表名 | 核心字段 |
|------|------|---------|
| User | user | username, password, nickname, phone, email, role, status |
| Pet | pet | user_id, name, type, breed, gender, age, weight |
| HealthRecord | health_records | pet_id, record_type, record_date, diagnosis, medicine |
| Appointment | appointment | user_id, pet_id, service_id, appointment_datetime, status |
| Product | products | name, category, price, stock, status |
| CommunityPost | community_post | user_id, title, content, topic_id |
| ChatMessage | chat_message | user_id, session_id, role, content, operation, domain |

---

## 八、关键设计决策

### 8.1 软删除策略

所有核心实体使用 MyBatis Plus `@TableLogic` 注解实现逻辑删除，status=0 表示已删除，数据仍保留在数据库中。

### 8.2 Redis 降级

Redis 不可用时自动降级：
- 验证码降级：使用固定测试码 `123456`
- `@Autowired(required = false)` 允许 Redis Bean 不存在时正常启动

### 8.3 文件上传安全

- 双重校验（Content-Type + 文件扩展名白名单）
- UUID 重命名防止文件名注入
- 目录穿越防护（白名单扩展名：jpg/jpeg/png/gif/webp/mp4/mov/avi）
- 文件大小限制：图片 5MB、头像 2MB、视频 50MB

### 8.4 权限控制

- AuthInterceptor 拦截所有请求进行 Token 校验
- Controller 层检查 `"ADMIN".equals(userRole)` 实现管理权限
- 用户只能操作自己的数据（宠物、预约等），管理员可操作全部

### 8.5 LLM 配置灵活性

LLM 配置支持两级覆盖：

```
application.yml 默认值  →  数据库 chat_config 表动态覆盖
```

可在管理后台实时调整 API URL、Key、Model、Temperature 等参数，无需重启服务。

---

## 九、技术栈依赖

| 依赖 | 版本 | 用途 |
|------|------|------|
| spring-boot-starter-web | 2.7.14 | Web MVC 框架 |
| spring-boot-starter-validation | 2.7.14 | Bean Validation |
| spring-boot-starter-data-redis | 2.7.14 | Redis 集成 |
| spring-boot-starter-aop | 2.7.14 | AOP 支持 |
| spring-boot-starter-websocket | 2.7.14 | WebSocket 支持 |
| spring-security-crypto | — | BCrypt 加密 |
| mybatis-plus-boot-starter | 3.5.3.1 | ORM 增强 |
| mysql-connector-java | 8.0.33 | MySQL 驱动 |
| jjwt (api/impl/jackson) | 0.11.5 | JWT 认证 |
| fastjson2 | 2.0.32 | JSON 序列化 |
| hutool-all | 5.8.20 | 通用工具 |
| okhttp | 4.9.3 | HTTP 客户端（LLM 调用） |
| lombok | — | 减少模板代码 |

---

## 十、配置文件

- `application.yml` — 主配置（数据源、Redis、JWT、LLM、文件上传）
- `application-prod.yml` — 生产环境配置覆盖
- LLM 配置支持环境变量注入：`ANTHROPIC_BASE_URL`、`ANTHROPIC_AUTH_TOKEN`、`LLM_API_KEY`
