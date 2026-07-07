# YuAiAgent — MoMoBallAgent

基于 **Spring AI + Vue 3** 的全栈 AI 智能体平台，接入阿里云 DashScope（通义千问）大模型，包含两个核心应用：

- **AI 恋爱大师** — 多轮记忆 + RAG 知识增强的情感咨询助手
- **AI 超级智能体 (MoMoBall)** — 支持工具调用的 ReAct 通用智能体

所有对话均通过 **SSE（Server-Sent Events）** 实时流式返回。

---

## 目录

- [项目架构](#项目架构)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [环境变量与配置](#环境变量与配置)
- [功能详解](#功能详解)
- [API 接口](#api-接口)
- [Docker 部署](#docker-部署)
- [MCP 扩展](#mcp-扩展)

---

## 项目架构

```
┌──────────────────────────────────────────────────────────┐
│             Vue 3 前端 (Vite + Axios + SSE)              │
│  App.vue  ──▶  ChatRoom.vue  ──▶  EventSource / Axios   │
└───────────────────────┬──────────────────────────────────┘
                        │ HTTP / SSE
                        ▼
┌──────────────────────────────────────────────────────────┐
│          Spring Boot 后端 (端口 8123, 路径 /api)          │
│                                                          │
│  AiController                                            │
│    ├── /ai/love_app/chat/sse   → LoveApp (流式对话)      │
│    ├── /ai/love_app/chat/sync  → LoveApp (同步对话)      │
│    └── /ai/momoball/chat       → MoMoBall (智能体)       │
│                                                          │
│  LoveApp ── ChatClient + Advisors + Memory + RAG         │
│  MoMoBall ── BaseAgent → ReActAgent → ToolCallAgent      │
│                                                          │
│  Tools: WebSearch / WebScraping / File / Terminal / PDF   │
│  MCP Client: amap-maps / yu-image-search                 │
└───────────────────────┬──────────────────────────────────┘
                        │
         ┌──────────────┼──────────────┐
         ▼              ▼              ▼
   DashScope API   SearchAPI.io    MCP Servers
   (Qwen 大模型)    (百度搜索)      (stdio 子进程)
```

---

## 技术栈

### 后端

| 分类 | 技术 |
|------|------|
| 语言 & 运行时 | Java 21, Spring Boot 3.4.5 |
| AI 框架 | Spring AI 1.1.2, Spring AI Alibaba 1.1.2.0 |
| 大模型 | 阿里云 DashScope（qwen-plus） |
| RAG | Spring AI RAG + DashScope 云知识库 + Markdown 文档 |
| 对话记忆 | 基于 Kryo 序列化的文件持久化 |
| MCP 协议 | Spring AI MCP Client（stdio 模式） |
| 工具调用 | Spring AI `@Tool` 注解 |
| API 文档 | Knife4j + SpringDoc OpenAPI |
| 工具库 | Lombok, Hutool, Jsoup, iText PDF |

### 前端

| 分类 | 技术 |
|------|------|
| 框架 | Vue 3 (Composition API) |
| 构建 | Vite |
| HTTP | Axios + 原生 EventSource (SSE) |
| 部署 | Nginx (Docker) |

### MCP 子项目

| 分类 | 技术 |
|------|------|
| 运行时 | Java 21, Spring Boot 4.1.0 |
| 协议 | Spring AI MCP Server (stdio) |
| 外部 API | Pexels 图片搜索 |

---

## 项目结构

```
yu-ai-agent/
├── pom.xml                                  # Maven 主构建文件
├── Dockerfile                               # 后端 Docker 镜像
├── src/main/java/com/itzhiqin/yuaiagent/
│   ├── YuAiAgentApplication.java            # 启动类
│   ├── controller/
│   │   └── AiController.java                # REST / SSE 接口
│   ├── app/
│   │   └── LoveApp.java                     # 恋爱大师应用
│   ├── agent/
│   │   ├── BaseAgent.java                   # Agent 基类（步骤循环 + SSE 流式）
│   │   ├── ReActAgent.java                  # Think → Act 模式
│   │   ├── ToolCallAgent.java               # 工具调用 ReAct 智能体
│   │   └── MoMoBall.java                    # 通用超级智能体
│   ├── tools/
│   │   ├── WebSearchTool.java               # 百度搜索（SearchAPI）
│   │   ├── WebScrapingTool.java             # 网页抓取（Jsoup）
│   │   ├── FileOperatorTool.java            # 文件读写
│   │   ├── TerminalOperationTool.java       # 终端命令执行
│   │   ├── PDFGenerationTool.java           # PDF 生成（iText）
│   │   ├── ResouceDownloadTool.java         # 资源下载
│   │   └── TerminateTool.java               # 终止 Agent 循环
│   ├── rag/                                 # RAG 管道组件
│   │   ├── LoveAppDocumentLoader.java       # Markdown 文档加载
│   │   ├── LoveAppRagCloundAdvisorConfig.java  # 云 RAG Advisor 配置
│   │   ├── QueryRewriter.java               # 查询重写
│   │   └── MyTokenTextSplitter.java         # 文本分割
│   ├── advisor/                             # 自定义 ChatClient Advisor
│   │   ├── MyLoggerAdvisor.java             # 日志记录
│   │   ├── MyReReadingAdvisor.java          # Re2 重读理解增强
│   │   └── MySafeGuardAdvisor.java          # 内容安全过滤
│   └── chatmemory/
│       └── FileBaseChatMemory.java          # Kryo 文件持久化记忆
├── src/main/resources/
│   ├── application.yml                      # 主配置
│   ├── application-local.yml                # 本地开发配置
│   ├── application-prod.yml                 # 生产环境配置
│   ├── mcp-server.json                      # MCP 服务端配置
│   └── document/                            # RAG 知识库文档
│       ├── 恋爱常见问题和回答 - 单身篇.md
│       ├── 恋爱常见问题和回答 - 恋爱篇.md
│       └── 恋爱常见问题和回答 - 已婚篇.md
│
├── yu-ai-agent-frontend/                    # Vue 3 前端
│   ├── package.json
│   ├── Dockerfile                           # 前端 Docker 镜像
│   ├── nginx.conf                           # Nginx 反向代理配置
│   └── src/
│       ├── App.vue                          # 应用外壳 + 导航
│       ├── components/ChatRoom.vue          # SSE 聊天组件
│       ├── api.js                           # API 地址配置
│       └── styles.css                       # 全局样式
│
└── yu-image-search-mcp-server/              # MCP 图片搜索子项目
    ├── pom.xml
    └── src/.../tools/ImageSearchTool.java   # Pexels 图片搜索工具
```

---

## 快速开始

### 环境要求

- **Java 21**
- **Maven 3.9+**（或使用项目自带的 `mvnw`）
- **Node.js 20+**（前端开发）
- **阿里云 DashScope API Key**

### 1. 启动后端

```bash
# 克隆项目
git clone <repo-url> && cd yu-ai-agent

# 设置 API Key（二选一）
# 方式一：环境变量
set DASHSCOPE_API_KEY=your_api_key_here

# 方式二：直接修改 application-local.yml

# 启动
.\mvnw.cmd spring-boot:run
```

后端启动后访问：
- API 地址：`http://localhost:8123/api`
- 接口文档：`http://localhost:8123/api/swagger-ui.html`

### 2. 启动前端

```bash
cd yu-ai-agent-frontend
npm install
npm run dev
```

前端启动后访问：`http://localhost:5173`

### 3. 构建 MCP 图片搜索服务（可选）

```bash
cd yu-image-search-mcp-server
.\mvnw.cmd clean package -DskipTests
```

构建产物会被主项目通过 `mcp-server.json` 中的 stdio 配置自动引用。

---

## 环境变量与配置

| 配置项 | 位置 | 说明 |
|--------|------|------|
| `DASHSCOPE_API_KEY` | `application-local.yml` | 阿里云 DashScope API 密钥 |
| `spring.profiles.active` | `application.yml` | 环境：`local`（默认）/ `prod` |
| `search-api.api-key` | `application.yml` | SearchAPI.io 搜索密钥 |
| `AMAP_MAPS_API_KEY` | `mcp-server.json` | 高德地图 MCP 服务密钥 |
| `server.port` | `application.yml` | 服务端口，默认 `8123` |
| `server.servlet.context-path` | `application.yml` | 上下文路径，默认 `/api` |

---

## 功能详解

### AI 恋爱大师

- **角色设定**：深耕恋爱心理领域的专家，围绕单身、恋爱、已婚三种状态提供咨询
- **多轮对话记忆**：基于 Kryo 序列化的文件持久化，支持会话恢复
- **RAG 知识增强**：接入 DashScope 云知识库，内置单身/恋爱/已婚三类 Markdown 知识文档
- **查询重写**：对话前自动重写用户查询以提升检索效果
- **SSE 流式输出**：实时逐字返回 AI 回复
- **自定义 Advisor 链**：
  - `MyLoggerAdvisor` — 请求/响应日志
  - `MyReReadingAdvisor` — Re2 重读理解增强
  - `MySafeGuardAdvisor` — 敏感词过滤（可选开启）
- **结构化输出**：支持生成 `LoveReport`（标题 + 建议列表）

### AI 超级智能体 (MoMoBall)

- **ReAct 推理模式**：Think → Act 循环，最多 5 步
- **内置工具集**：
  - `WebSearchTool` — 百度搜索（通过 SearchAPI.io）
  - `WebScrapingTool` — 网页内容抓取（Jsoup）
  - `FileOperatorTool` — 文件读写（`tmp/file/` 目录下）
  - `TerminalOperationTool` — Shell 命令执行
  - `ResouceDownloadTool` — 资源下载
  - `PDFGenerationTool` — PDF 文档生成（iText）
  - `TerminateTool` — 终止 Agent 执行循环
- **SSE 流式输出**：逐步返回思考和执行过程
- **MCP 工具扩展**：通过 MCP 协议接入外部工具服务

---

## API 接口

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | `/api/ai/love_app/chat/sync` | 恋爱大师同步对话 | `message`, `chatId` |
| GET | `/api/ai/love_app/chat/sse` | 恋爱大师 SSE 流式对话 | `message`, `chatId` |
| GET | `/api/ai/momoball/chat` | MoMoBall 智能体对话 | `message` |

完整接口文档启动后访问 Knife4j UI：`http://localhost:8123/api/swagger-ui.html`

---

## Docker 部署

### 后端

```bash
cd yu-ai-agent
docker build -t yu-ai-agent .
docker run -p 8123:8123 \
  -e DASHSCOPE_API_KEY=your_api_key \
  yu-ai-agent
```

### 前端

```bash
cd yu-ai-agent-frontend
docker build -t yu-ai-agent-frontend .
docker run -p 80:80 yu-ai-agent-frontend
```

> 生产环境部署时需修改 `nginx.conf` 中的后端代理地址。

---

## MCP 扩展

项目通过 Spring AI MCP Client 以 stdio 模式接入外部工具服务，配置文件为 `src/main/resources/mcp-server.json`：

- **@amap/amap-maps-mcp-server** — 高德地图服务（需配置 `AMAP_MAPS_API_KEY`）
- **yu-image-search-mcp-server** — Pexels 图片搜索（子项目，需先构建 JAR）

如需扩展新的 MCP 工具，在 `mcp-server.json` 中添加对应的 stdio 服务配置即可。
