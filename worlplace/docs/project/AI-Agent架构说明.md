# 智慧宠物管理系统 — AI Agent 智能客服架构说明

## 1. 概述

本系统采用 **基于 LLM 的多智能体协作架构**，实现了一个能理解自然语言、自动执行数据库操作的智能客服系统。用户通过自然语言对话即可完成宠物管理、健康记录、商城下单、服务预约等操作。

### 技术栈

| 组件 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7 + MyBatis-Plus 3.5 |
| LLM 调用 | OkHttp 3 原生 HTTP 调用 |
| 协议兼容 | 同时支持 Anthropic Messages API 和 OpenAI Chat Completions API |
| 模型 | DeepSeek / OpenAI / Anthropic Claude 可动态切换 |
| Python 补充模块 | LangChain + DeepSeek（独立商城下单 Agent） |

---

## 2. 架构全景图

```
                             ┌──────────────────────┐
                             │    前端 (Vue 3)        │
                             │   ChatWidget.vue      │
                             │   ChatBubble.vue      │
                             └──────┬───────────────┘
                                    │ POST /api/chat/send
                                    ▼
┌───────────────────────────────────────────────────────────────────┐
│                      ChatServiceImpl                               │
│                    (消息持久化 + 历史管理)                            │
└──────────────────────────────┬────────────────────────────────────┘
                               │ orchestrator.process()
                               ▼
┌───────────────────────────────────────────────────────────────────┐
│                    AgentOrchestrator (编排器)                       │
│                                                                   │
│   ┌──────────────┐     ┌──────────────┐     ┌─────────────────┐   │
│   │ IntentRouter │ ──→ │ Agent 选择器  │ ──→ │  ActionExecutor  │   │
│   │ (意图识别)    │     │ (领域路由)    │     │ (操作执行)       │   │
│   └──────────────┘     └──────────────┘     └─────────────────┘   │
│          │                     │                     │            │
│          ▼                     ▼                     ▼            │
│    LLM 语义分类           5个专业Agent          执行业务操作         │
│                                                                   │
│   ┌────────────────────────────────────────────────────┐         │
│   │                  LlmClient (LLM 客户端)              │         │
│   │         Anthropic Messages API / OpenAI API         │         │
│   └────────────────────────────────────────────────────┘         │
└───────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
                  MySQL           LLM API          Redis
               (对话记录/配置)    (大模型服务)     (缓存)
```

---

## 3. 核心组件详解

### 3.1 IntentRouter — 意图路由器

**文件**: `backend/src/main/java/com/pet/agent/core/IntentRouter.java`

将用户自然语言分类为 **5 种操作 × 5 个领域**：

| 维度 | 可选值 | 说明 |
|------|--------|------|
| `operation` | QUERY, ADD, MODIFY, DELETE, CHAT | 用户意图的操作类型 |
| `domain` | PET, HEALTH, MALL, GENERAL, CHAT | 操作涉及的领域 |

- **首选**: LLM 语义分类（高准确率）
- **兜底**: 关键词匹配 fallback（75+ 个中文关键词覆盖）

### 3.2 Agent 体系 — 策略模式

**Agent 接口** (`Agent.java`):
```java
public interface Agent {
    DomainType getDomain();
    String getName();
    String buildSystemPrompt(AgentContext ctx, OperationType operation);
    String buildDataContext(AgentContext ctx);
}
```

**五个专业 Agent**:

| Agent | Domain | 触发场景 |
|-------|--------|----------|
| `ArchiveAgent` | PET | 宠物档案查询/添加/修改 |
| `HealthAgent` | HEALTH | 疫苗/体检/驱虫/生病记录 |
| `MallAgent` | MALL | 商品搜索/推荐/下单/订单查询 |
| `AdvisorAgent` | GENERAL | 饲养/训练/护理/服务预约 |
| `GeneralAgent` | CHAT | 日常闲聊/非宠物话题 |

新增领域只需实现 `Agent` 接口 + 添加枚举值，无需修改编排器代码。

### 3.3 AgentContext — 上下文注入

在调用 LLM 之前，编排器将以下信息注入 Prompt：

| 上下文信息 | 来源 | 用途 |
|-----------|------|------|
| 用户宠物列表 | PetService 查询 | 让 LLM 知道用户有哪些宠物 |
| 对话历史（最近 20 条） | ChatMessage 表查询 | 多轮对话能力 |
| 当前所在页面 | 前端传入 | 理解用户操作上下文 |
| 健康记录（最近 5 条） | HealthAgent 注入 | 健康咨询场景 |
| 商城商品列表（50 件） | MallAgent 注入 | 下单时使用真实商品 ID |
| 用户地址列表 | MallAgent 注入 | 下单时填充收件信息 |
| 用户近期订单 | MallAgent 注入 | 订单查询和推荐 |
| 搜索缓存 | MallAgent 内存缓存 | 用户连续下单时保持上下文 |

### 3.4 LlmClient — LLM 调用客户端

**文件**: `backend/src/main/java/com/pet/agent/llm/LlmClient.java`

**核心特性**:

1. **双 API 格式兼容** — 根据 URL 自动判断 Anthropic Messages API 还是 OpenAI API
2. **动态配置** — API URL、Key、Model、Temperature、MaxTokens 从数据库读取，管理员可实时修改
3. **请求格式自适应**:
   - Anthropic: `{"model":"...", "system":"...", "messages":[...]}`
   - OpenAI: `{"model":"...", "messages":[{"role":"system","content":"..."}, ...]}`
4. **错误分级处理**: 401/403 → 认证失败提示 / 网络异常 → 连接失败提示

### 3.5 ActionExecutor — 动作执行器（核心创新）

**文件**: `backend/src/main/java/com/pet/agent/core/ActionExecutor.java`

从 LLM 回复文本**末尾提取结构化 JSON 指令**，自动执行真实业务操作。

**LLM 回复示例**:
```
已为您下单！

{"action":{"operation":"ADD","entity":"ORDER","fields":{"items":[{"productId":8,"quantity":1}],"shippingName":"张三","shippingPhone":"13800138000","shippingAddress":"北京市朝阳区xx小区"}}}
```

**支持的操作实体**:

| Entity | 操作 | 说明 |
|--------|------|------|
| `PRODUCT_SEARCH` | QUERY | 搜索商品，缓存结果到 MallAgent |
| `CART_ADD` | ADD | 添加商品到购物车 |
| `ORDER` | ADD | 创建订单（含商品 ID 验证） |
| `APPOINTMENT` | ADD | 创建服务预约 |
| `HEALTH_RECORD` | ADD | 添加健康记录 |
| `PET` | ADD/MODIFY | 新增或修改宠物信息 |
| `REMINDER` | ADD/DELETE | 创建/删除提醒 |
| `PAGE_NAVIGATE` | — | 前端页面跳转指令 |

---

## 4. 完整请求处理流程

```
用户消息 → ChatController → ChatServiceImpl.sendMessage()
                                    │
                         ┌──────────┴──────────┐
                         │  保存用户消息到数据库    │
                         │  加载最近 20 条历史     │
                         └──────────┬──────────┘
                                    ▼
                         AgentOrchestrator.process()
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
          IntentRouter.route()  buildContext()   Agent 选择
          LLM 语义分类           查询用户数据      路由到领域 Agent
          {operation, domain}   (宠物、历史等)
                    │               │               │
                    └───────────────┼───────────────┘
                                    ▼
                    ┌───────────────────────────┐
                    │ Agent.buildSystemPrompt() │
                    │ + buildDataContext()      │
                    │ → fullSystemPrompt        │
                    └───────────┬───────────────┘
                                ▼
                    ┌───────────────────────────┐
                    │ LlmClient.chat() /        │
                    │ chatWithHistory()         │
                    │ → LLM 生成回复文本          │
                    └───────────┬───────────────┘
                                ▼
                    ┌───────────────────────────┐
                    │ ActionExecutor.process()  │
                    │ 提取 action JSON          │
                    │ 执行数据库操作              │
                    │ → cleanReply + confirm    │
                    └───────────┬───────────────┘
                                ▼
                    ┌───────────────────────────┐
                    │ ChatResponse              │
                    │ {reply, operation, domain, │
                    │  agentName, navigate,      │
                    │  action, sessionId}        │
                    └───────────────────────────┘
```

---

## 5. Prompt 工程设计

### 5.1 输出规则（所有 Agent 共享）

- 简洁：每次回复 3-5 句话
- 语气温暖：称呼用户为「铲屎官」，称呼宠物为「毛孩子」
- 纯文本：禁止使用任何 Markdown 格式（`**`、`-`、`#` 等符号全部禁用）
- 一段到底：不要分点列举
- 紧跟上下文：基于对话历史回答
- 商品 ID 约束：只能使用上下文中以 `[ID:X]` 格式列出的商品 ID，禁止编造

### 5.2 操作指引

```
QUERY: 用户在做【查询】，只展示已有数据不编造
ADD:   用户在做【新增】，信息不全就主动问
MODIFY:用户在做【修改】，先确认改哪条和新值是什么
DELETE:用户在做【删除】，先确认删哪条，提醒不可恢复
CHAT:  用户在做【闲聊咨询】，耐心回答不操作数据库
```

---

## 6. Python 独立 Agent 模块

**文件**: `agent/agent.py`

基于 **LangChain + DeepSeek** 的独立商城下单 Agent:

| 对比维度 | Java Agent 系统 | Python Agent 模块 |
|---------|----------------|-------------------|
| 框架 | 手工实现编排 | LangChain AgentExecutor |
| Agent 模式 | 意图路由 + 策略模式 | Tool Calling Agent |
| 工具调用 | LLM 输出 JSON → ActionExecutor | LangChain native function calling |
| 运行方式 | 集成在 Spring Boot Web 服务中 | 独立终端程序 |
| 触发方式 | HTTP POST /chat/send | 命令行交互 |

两个工具函数:
- `search_goods(category, sub_category)` → 调用 GET /product/searchByCategory
- `create_order(goods_id, goods_name, price, quantity)` → 调用 POST /order/createByAgent

---

## 7. 数据流完整示例

以**用户通过 AI 客服购买狗粮**为例:

```
1. 用户发送: "给我家柯基叮当买一包狗粮"
2. IntentRouter → {operation:ADD, domain:MALL}
3. 选择 MallAgent，注入商品/地址/订单上下文
4. LLM 回复: "铲屎官，为叮当找到了鸡肉干狗粮 ¥59.00，要帮您下单吗？"
5. 用户确认: "好的，帮我下单"
6. LLM 回复含 action JSON:
   {"action":{"operation":"ADD","entity":"ORDER","fields":{...}}}
7. ActionExecutor 验证商品ID → 创建订单 → 返回订单号
8. 前端收到: reply + navigate + action
```

---

## 8. 架构设计亮点

1. **LLM 驱动意图识别** — 语义理解 + 关键词 fallback，准确且可靠
2. **策略模式解耦** — 5 个 Agent 各管一摊，新增领域无需改动编排器
3. **结构化动作提取** — LLM 回复中嵌入 JSON 指令，自动执行数据库操作
4. **上下文注入** — 动态查询用户数据注入 Prompt，LLM 回复个性化
5. **双 API 兼容** — 一套代码支持 Anthropic / OpenAI / DeepSeek
6. **动态配置** — API Key、Model 等参数数据库管理，运行时实时切换

---

## 9. 关键文件索引

### Java Agent 核心

| 文件 | 行数 | 说明 |
|------|------|------|
| `agent/core/Agent.java` | 13 | Agent 接口定义 |
| `agent/core/AgentOrchestrator.java` | 173 | 编排器，核心调度 |
| `agent/core/IntentRouter.java` | 99 | 意图路由器 |
| `agent/core/ActionExecutor.java` | 560 | 动作执行器 |
| `agent/core/DomainType.java` | 22 | 领域枚举 |
| `agent/core/OperationType.java` | 17 | 操作枚举 |
| `agent/core/AgentContext.java` | 34 | 上下文数据对象 |
| `agent/llm/LlmClient.java` | 263 | LLM HTTP 客户端 |
| `agent/prompt/PromptTemplates.java` | 70 | 提示词模板工厂 |
| `agent/agents/MallAgent.java` | 192 | 商城 Agent |
| `agent/agents/HealthAgent.java` | 78 | 健康 Agent |
| `agent/agents/GeneralAgent.java` | 49 | 闲聊 Agent |
| `agent/service/impl/ChatServiceImpl.java` | 151 | 聊天服务实现 |
| `agent/controller/ChatController.java` | 45 | 聊天 API 控制器 |

### Python Agent

| 文件 | 说明 |
|------|------|
| `agent/agent.py` | LangChain Agent 主程序 |
| `agent/requirements.txt` | Python 依赖 |

### 前端 Agent 相关

| 文件 | 说明 |
|------|------|
| `frontend/src/components/ChatBubble.vue` | 客服浮动按钮 |
| `frontend/src/components/ChatWidget.vue` | 客服聊天窗口 |
| `frontend/src/api/chat.js` | 聊天 API 封装 |
| `frontend/src/views/ChatConfig.vue` | 客服设置页面（管理员） |