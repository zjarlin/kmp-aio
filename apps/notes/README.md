# VibeNotes

`VibeNotes` 是一个参考 macOS 备忘录体验的 Compose Multiplatform 笔记应用，重点围绕：

- 多端统一：Desktop / Android / iOS / Wasm
- Markdown 编写与预览
- SQLite + PostgreSQL 双数据源
- 真实 API（Ktor）+ 声明式客户端（Ktorfit）
- AI 整理、RAG 检索、知识图谱首页大屏

## 功能清单

### 1) macOS 备忘录风格三栏

- 左栏：工作区（首页图谱 / 笔记编辑）+ 分组（全部/置顶）
- 中栏：笔记列表 + 过滤检索
- 右栏：编辑器（编辑/预览/分栏）+ 同步/置顶/删除/一键整理

### 2) Markdown 支持

- 支持标题、列表、任务列表、引用、代码块等常见块结构
- 编辑与预览同屏可切换

### 3) 多数据源（PostgreSQL + SQLite）

- 服务端同时维护 SQLite 与 PostgreSQL 存储
- 客户端通过 Ktorfit 接口分别访问 `sqlite` 与 `postgres` 数据源
- Repository 会做双源合并，并支持手动 `sync`

### 4) 真实 API + Ktorfit

- 服务端模块：`apps/notes/server`
- 客户端声明：`NotesApi`（`@GET/@PUT/@DELETE`）
- 客户端实现：`NotesApiClient`（Ktor + ContentNegotiation + Json）
- API 数据源实现：`ApiNoteDataSource`

### 5) AI 一键整理（无外部依赖的本地版）

当前实现为可离线运行的本地规则引擎 `LocalNoteOrganizer`，输入当前笔记内容与引用上下文后，自动生成：

- AI整理摘要
- 待办拆解
- 整理后正文
- 上下文引用区块

### 6) `@thisFile` / `@file` 引用机制

一键整理时支持类似 Codex 上下文注入风格：

- `@thisFile`：显式引用当前笔记内容
- `@/notes/xxx.md`：按路径引用其他笔记
- `@project/plan.md`：也支持相对样式，内部会尝试路径归一化匹配

若引用未命中，会在整理结果中提示缺失引用数量。

### 7) 知识图谱首页（全局搜笔记入口）

- 首页展示全局知识图谱面板（节点 + 边）
- 支持 RAG 风格检索（标题/路径/正文 + 图谱关系度加权）
- 从图谱检索结果可直接打开目标笔记

> 日常检索建议从图谱首页进入，编辑工作流在笔记页进行。

## RAG + 图谱设计说明

当前为轻量实现，核心链路：

1. **Graph Build**
    - `KnowledgeGraphBuilder` 从笔记正文提取 `@引用` 关系，构建节点和边
2. **Retriever**
    - `RagSearchEngine` 对 query 做分词，按标题/路径/正文命中+图谱度数综合打分
3. **Organizer**
    - `LocalNoteOrganizer` 汇总当前笔记 + 引用笔记内容，输出结构化整理稿

后续可替换为真实向量检索架构（PGVector / Milvus / Elasticsearch + reranker），并接入外部 LLM。

## 运行（桌面端）

```bash
./gradlew :apps:notes:run
```

桌面端会自动内嵌启动 `apps/notes/server`（默认 `18080` 端口）。

## 独立启动 API 服务

```bash
./gradlew :apps:notes:server:run
```

## OpenAPI / Swagger

- OpenAPI 文件：`apps/notes/server/src/main/resources/openapi/documentation.yaml`
- 本地 Swagger UI：`http://127.0.0.1:18080/swagger`

## API / 数据源配置

### 客户端配置

`NOTES_API_BASE_URL`：客户端 API 地址（桌面端默认 `http://127.0.0.1:18080/`）

### 服务端配置

不配置环境变量时：

- SQLite 默认使用 `apps/notes/server/build/vibenotes-server.db`
- PostgreSQL 默认关闭（仅 SQLite 可用）

可选环境变量：

```bash
NOTES_API_BASE_URL=http://127.0.0.1:18080/
NOTES_SERVER_SQLITE_URL=jdbc:sqlite:/abs/path/vibenotes.db
NOTES_SERVER_POSTGRES_URL=jdbc:postgresql://127.0.0.1:5432/vibenotes
NOTES_SERVER_POSTGRES_USER=postgres
NOTES_SERVER_POSTGRES_PASSWORD=postgres
```

## Postman 联调

- Collection：`apps/notes/docs/postman/vibenotes-api.postman_collection.json`
- Environment：`apps/notes/docs/postman/local.postman_environment.json`

## 关键代码入口

- `apps/notes/src/commonMain/kotlin/site/addzero/notes/App.kt`
- `apps/notes/src/commonMain/kotlin/site/addzero/notes/api/NotesApi.kt`
- `apps/notes/src/commonMain/kotlin/site/addzero/notes/data/ApiNoteDataSource.kt`
- `apps/notes/src/commonMain/kotlin/site/addzero/notes/ai/LocalNoteOrganizer.kt`
- `apps/notes/src/commonMain/kotlin/site/addzero/notes/ai/RagSearchEngine.kt`
- `apps/notes/src/commonMain/kotlin/site/addzero/notes/graph/KnowledgeGraphBuilder.kt`
- `apps/notes/src/commonMain/kotlin/site/addzero/notes/data/MultiSourceNoteRepository.kt`
- `apps/notes/server/src/main/kotlin/site/addzero/notes/server/Application.kt`
