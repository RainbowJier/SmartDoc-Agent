# SmartDoc-Agent

智能文档问答助手 — 基于 LLM + RAG 技术，让用户通过上传文档（PDF/DOCX/TXT）并以自然语言对话的方式查询文档内容。

后端提供 SSE 流式对话、文档管理、会话管理等 REST API，附带 Vue 3 前端界面。

---

## 功能特性

- **文档上传与解析** — 支持 PDF、DOCX、TXT（≤ 50MB），自动解析、分块、向量化
- **RAG 智能问答** — 基于上传文档的语义检索 + LLM 生成，答案可追溯到原文片段
- **SSE 流式对话** — AI 回复实时逐 token 推送，前端逐字展示
- **多轮会话管理** — 20 条消息滑动窗口记忆，30 分钟未活跃自动清理
- **多 LLM 支持** — 内置 DeepSeek 和智谱 GLM 配置，可扩展任意 OpenAI 兼容 API
- **Embedding 可选** — 本地 ONNX 模型（AllMiniLmL6V2）或远程 Zhipu API
- **Agent 工具机制** — 演示 AI 主动调用外部工具的 `@Tool` 框架
- **Apple 风格前端** — Vue 3 + TypeScript，支持 Markdown 渲染、Mermaid 图表、代码高亮

---

## 技术栈

| 层级     | 技术                                       |
|--------|------------------------------------------|
| 后端语言   | Java 17+                                 |
| 框架     | Spring Boot 3.5.0, LangChain4j 1.13.0    |
| 构建工具   | Maven 3.8+                               |
| 文档解析   | Apache PDFBox, Apache POI                |
| 向量存储   | InMemoryEmbeddingStore                   |
| LLM 集成 | OpenAI-compatible HTTP API               |
| 前端框架   | Vue 3.5 + TypeScript 6.0                 |
| 前端构建   | Vite 8.0                                 |
| 前端渲染   | markdown-it, Mermaid 11.15, highlight.js |

---

## 项目结构

```
smartdoc-backend/                # 后端 (Spring Boot 多模块)
├── smartdoc-common/             # 公共 DTO / 枚举
├── smartdoc-chatModel/          # LLM & Embedding Bean 配置
├── smartdoc-rag/                # 文档解析、分块、向量化、检索
├── smartdoc-tools/              # Agent 工具 (stub)
├── smartdoc-chat/               # AiServices 装配 + 会话管理
└── smartdoc-api/                # REST 控制器 + 启动入口

smartdoc-frontend/               # 前端 (Vue 3 + Vite)
└── src/
    ├── api/                     # SSE 流式客户端
    ├── stores/                  # Pinia 状态管理
    ├── components/              # 聊天 UI 组件
    └── views/                   # 页面视图

doc/                             # 详细文档
├── PRD.md                       # 产品需求文档
├── Architecture.md              # 架构说明
├── API-Reference.md             # API 接口文档
├── SSE-Protocol.md              # SSE 协议说明
├── Configuration.md             # 配置参考
└── Development-Setup.md         # 开发环境搭建
```

---

## 快速开始

### 环境要求

- Java 17+
- Maven 3.8+
- Node.js 20+ (前端)

### 后端

```bash
# 1. 配置 API Key（任选一个 provider）
set DEEPSEEK_API_KEY=sk-your-key-here
set LLM_PROVIDER=deepseek

# 或
set ZHIPU_API_KEY=your-key-here
set LLM_PROVIDER=zhipu

# 2. 构建所有模块
cd smartdoc-backend
mvn clean install -DskipTests

# 3. 启动服务
cd smartdoc-api
mvn spring-boot:run
```

服务启动在 `http://localhost:8080`。

### 前端

```bash
cd smartdoc-frontend
npm install
npm run dev
```

开发服务器启动在 `http://localhost:5173`（API 代理到 `localhost:8080`）。

---

## API 概览

| 方法     | 路径                              | 说明                      |
|--------|---------------------------------|-------------------------|
| POST   | `/api/chat`                     | SSE 流式对话                |
| GET    | `/api/chat/history/{sessionId}` | 会话历史                    |
| DELETE | `/api/chat/session/{sessionId}` | 清除会话                    |
| POST   | `/api/documents/upload`         | 上传文档 (multipart, ≤50MB) |
| GET    | `/api/documents`                | 文档列表                    |

---

## 配置

关键环境变量：

| 变量                   | 默认值     | 说明                      |
|----------------------|---------|-------------------------|
| `LLM_API_KEY`        | —       | 统一 API Key（优先级最高）       |
| `LLM_PROVIDER`       | `zhipu` | 可选 `zhipu` / `deepseek` |
| `DEEPSEEK_API_KEY`   | —       | DeepSeek 专用 Key         |
| `ZHIPU_API_KEY`      | —       | 智谱 GLM 专用 Key           |
| `RAG_EMBEDDING_TYPE` | `local` | `local`(ONNX) / `zhipu` |

详细配置参见 [Configuration.md](doc/Configuration.md)。

---

## 文档

- [产品需求文档](doc/PRD.md)
- [架构说明](doc/Architecture.md)
- [API 参考](doc/API-Reference.md)
- [SSE 协议](doc/SSE-Protocol.md)
- [配置参考](doc/Configuration.md)
- [开发环境搭建](doc/Development-Setup.md)

---

## License

[Apache License 2.0](LICENSE)
