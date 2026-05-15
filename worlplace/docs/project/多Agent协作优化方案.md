# 多 Agent 协作优化方案 — 方案抉择与实施记录

## 背景

智慧宠物管理系统当前使用 5 个专业 Agent（ArchiveAgent、HealthAgent、MallAgent、AdvisorAgent、GeneralAgent），通过 IntentRouter → AgentOrchestrator 进行单 Agent 串行调度。随着业务复杂度增长，暴露出 5 个核心问题。

---

## 当前架构问题诊断

| # | 问题 | 影响 |
|---|------|------|
| 1 | **单意图路由** — 每次只派发 1 个 Agent | 用户说"狗拉肚子，买药，预约医生"只处理第一个意图 |
| 2 | **Agent 零通信** — 各自孤岛运行 | HealthAgent 不知道 MallAgent 推荐了什么；MallAgent 不知道宠物过敏史 |
| 3 | **无冲突处理** — 矛盾建议直接展示 | HealthAgent 建议禁食 vs MallAgent 推荐零食，同时呈现给用户 |
| 4 | **无结果聚合** — 多个回复无法合并 | 即使支持多 Agent，回复也是割裂的多段文本 |
| 5 | **无依赖编排** — 无法控制执行顺序 | Agent A 的输出不能作为 Agent B 的输入 |

---

## 方案选型

### 候选方案

#### 方案 A：完整实现 A2A 协议

Google 于 2025 年 4 月发布的 Agent-to-Agent (A2A) 开放协议（2026 年 3 月 v1.0），定义 Agent 之间如何发现、委派任务、协调工作。

**技术特征**：

- HTTP + JSON-RPC 2.0 + SSE 流式传输
- Agent Card 自动发现（`/.well-known/agent-card.json`）
- Task 9 状态状态机（SUBMITTED → WORKING → COMPLETED/FAILED/CANCELED/INPUT_REQUIRED）
- Protocol Buffers (proto3) 数据模型
- OAuth 2.0 / OpenID Connect 认证
- Linux 基金会维护，Apache 2.0 开源，150+ 组织支持

**评估**：

| 维度 | 评分 | 说明 |
|------|------|------|
| 标准化程度 | ★★★★★ | Linux 基金会维护，行业标准 |
| 实现成本 | ★☆☆☆☆ | 13+ 新文件，需引入 A2A SDK + protobuf，前端需重写 SSE |
| 适合度 | ★★☆☆☆ | 解决"跨系统互操作"问题，本项目所有 Agent 在同一 JVM 进程 |
| 向后兼容 | ★★☆☆☆ | API 格式需要重新设计 |
| 开发周期 | ★☆☆☆☆ | 预估 2-3 周 |

**结论**：A2A 的核心价值在于跨组织、跨框架、跨语言的 Agent 互操作。本项目的 5 个 Agent 全部运行在同一个 Spring 容器中，用 HTTP+JSON-RPC 通信比直接方法调用慢 100 倍，属于过度设计。

#### 方案 B：轻量方案（借鉴 A2A 设计思想，去掉网络层）

保留 A2A 的四个核心设计模式，但在 JVM 进程内实现：

| A2A 概念 | 轻量实现 | 保留价值 |
|---------|---------|---------|
| Task 9 状态机 | TaskState 枚举 + 状态流转 | 可追踪、可中断、冲突可升级 |
| Agent Card | Agent.getCapabilities() 方法 | 自动发现 Agent 能力 |
| Messages + Parts | StructuredResponse(text + data) | 结构化输出，前端直接渲染 |
| Task 依赖编排 | CollaborationManager 拓扑排序 | 并行执行 + 依赖解析 |
| SSE 流式 | 暂保留同步，预留异步回调接口 | 未来按需添加 |
| JSON-RPC/gRPC | 直接方法调用 | 同进程不需要网络开销 |
| OAuth / JWS 签名 | Spring Security 已有 | 单系统不需要额外认证层 |

**评估**：

| 维度 | 评分 | 说明 |
|------|------|------|
| 实现成本 | ★★★★★ | 5 新文件 + 2 修改，零新依赖 |
| 适合度 | ★★★★★ | 专为同进程多 Agent 设计 |
| 向后兼容 | ★★★★★ | ChatController / ChatServiceImpl / 前端均不改 |
| 可扩展性 | ★★★★☆ | 未来如需接外部 Agent，加 A2A 协议层即可 |
| 开发周期 | ★★★★★ | 1-2 天 |

### 最终选择：方案 B — 轻量方案

**决策理由**：

1. **项目阶段不适合上重协议** — 项目完成度约 70%，商城模块和社区模块尚未完成，此时引入完整 A2A 会阻塞业务开发
2. **没有跨系统需求** — 5 个 Agent 在同一个 JVM、同一个 Spring 容器里，HTTP 通信无意义
3. **成本收益比最优** — 5 个新文件达到同样效果，未来可平滑升级到完整 A2A
4. **A2A 的设计思想才是核心资产** — 状态机、依赖编排、冲突升级，这些才是真正解决问题的东西

---

## 实施架构

### 新架构全景

```
                          ┌─────────────────────┐
                          │   用户消息           │
                          └─────────┬───────────┘
                                    │
                          ┌─────────▼───────────┐
                          │ ① IntentRouter(增强) │  多意图识别 + 依赖排序
                          │ 输出: List<Intent>   │
                          └─────────┬───────────┘
                                    │
                          ┌─────────▼───────────┐
                          │ ② CollaborationMgr  │  并行调度 + 依赖编排
                          │ 线程池并发执行Agent  │
                          └────┬─────┬─────┬────┘
                               │     │     │
                    ┌──────────┘  ┌──┘  └──┐  └──────────┐
                    ▼             ▼        ▼              ▼
               ArchiveAgent  HealthAgent MallAgent  AdvisorAgent
                    │             │        │              │
                    └──────────┬──┘────────┘──────────────┘
                               │
                    ┌──────────▼──────────┐
                    │ ③ AgentBus(黑板模式) │  跨Agent共享上下文
                    │ publish / subscribe │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │ ④ ConflictResolver  │  冲突检测 + 3层裁决引擎
                    │ + ResultAggregator  │  结果合并去重排序
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │     最终回复         │
                    └─────────────────────┘
```

### 新增组件

| 文件 | 行数(估) | 职责 |
|------|---------|------|
| `core/TaskState.java` | ~30 | 任务状态枚举：SUBMITTED → WORKING → COMPLETED/FAILED/INPUT_REQUIRED |
| `core/AgentBus.java` | ~80 | 黑板模式：共享数据发布/订阅，事件日志可审计 |
| `core/CollaborationManager.java` | ~150 | 并行调度：拓扑排序、线程池、依赖等待 |
| `core/ConflictResolver.java` | ~130 | 3层裁决：领域优先级 → 置信度投票 → 升级用户 |
| `core/ResultAggregator.java` | ~120 | 结果合并：去重、排序、结构化输出 |

### 修改组件

| 文件 | 改动量 | 改动内容 |
|------|--------|---------|
| `core/IntentRouter.java` | +40行 | 单意图 → 多意图，每个 Intent 带 `dependsOn` |
| `core/AgentOrchestrator.java` | +60行 | 接入全部新组件，支持多 Agent 编排 |

### 不改动的组件

- `ChatServiceImpl.java` — 接口不变
- `ChatController.java` — 接口不变
- 5 个 Agent 实现类 — 不变
- `ActionExecutor.java` — 不变
- `LlmClient.java` — 不变
- 前端所有文件 — 不变

---

## 冲突解决引擎

### 三层裁决机制

```
Layer 1 — 领域优先级规则（自动）
  HEALTH(100) > GENERAL(80) > MALL(60) > PET(50) > CHAT(30)
  医疗建议永远覆盖商城推荐

Layer 2 — 置信度投票（自动）
  同优先级时，比较每个 Agent 的 confidence 分数，取高的

Layer 3 — 升级给用户（人工）
  前两层无法裁决时，生成结构化选项让用户选择
  例: {"choices": [{"id":"A","label":"益生菌调理"},{"id":"B","label":"直接就医"}]}
```

### 冲突类型与策略

| 冲突类型 | 示例 | 裁决 |
|---------|------|------|
| 建议矛盾 | HealthAgent 说禁食 vs MallAgent 推荐零食 | 优先级：HEALTH > MALL，剔除零食 |
| 信息不一致 | ArchiveAgent 体重 5kg vs HealthAgent 记录 8kg | 以最近更新时间为准 |
| 操作冲突 | Agent A 创建订单 vs Agent B 取消同一订单 | 互斥检测，阻止并询问 |
| 重复推荐 | 两个 Agent 推荐了同一商品 | 去重合并，只展示一次 |

### 依赖冲突处理

```
静态依赖声明：
  MALL    → depends on HEALTH（商城推荐依赖健康分析结果）
  GENERAL → depends on PET（预约依赖宠物信息）
  HEALTH  → depends on PET（健康记录依赖宠物信息）

执行逻辑：
  1. HealthAgent 先跑 → 结果写入 AgentBus
  2. MallAgent 检查 AgentBus → 有健康结论 → 基于结论搜索
  3. HealthAgent 超时/失败 → MallAgent 降级为通用推荐 + 标注风险
```

---

## 向后兼容设计

### 单意图降级

当 IntentRouter 只识别出 1 个意图时，自动退化为原有单 Agent 模式，行为完全不变。

### 配置开关

在 `chat_config` 表中增加开关：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `agent.collaboration.enabled` | true | 多 Agent 协作总开关 |
| `agent.collaboration.max_concurrent` | 3 | 最大并行 Agent 数 |
| `agent.collaboration.timeout_seconds` | 30 | 单个 Agent 超时时间 |

---

## 版本历史

| 日期 | 版本 | 变更 |
|------|------|------|
| 2026-05-15 | v0.1 | 初始方案文档，完成方案抉择分析 |

---

## 参考资料

- [Google A2A Protocol Specification](https://a2aproject.github.io/A2A/)
- [A2A GitHub Repository](https://github.com/a2aproject/A2A)
- [项目 AI Agent 架构说明](./AI-Agent架构说明.md)