# AI / RAG / Knowledge Graph 设计

## 目标

围绕“从知识图谱大屏检索笔记 + 打开后一键整理”的主工作流，形成闭环：

1. 图谱首页检索定位笔记
2. 进入编辑器阅读/编辑
3. 使用 `@thisFile` + `@路径` 聚合上下文
4. 一键整理输出结构化内容

## 当前实现（V1）

## 0. 数据面（真实 API）

- 客户端通过 `NotesApi`（Ktorfit）访问后端
- 后端 Ktor 提供 `sqlite` / `postgres` 双源路由
- 客户端 Repository 仍保留双源合并与同步语义

核心路径：

- `apps/notes/src/commonMain/kotlin/site/addzero/notes/api/NotesApi.kt`
- `apps/notes/src/commonMain/kotlin/site/addzero/notes/data/ApiNoteDataSource.kt`
- `apps/notes/server/src/main/kotlin/site/addzero/notes/server/routes/NoteRoutes.kt`

## 1. 引用协议

- `@thisFile`：当前笔记全文上下文
- `@/notes/a.md`：绝对路径引用
- `@project/a.md`：相对路径引用（内部归一化后尝试匹配）

引用解析入口：`ReferenceTokenParser`

## 2. 本地整理引擎

`LocalNoteOrganizer` 在无外部 LLM 的情况下提供可落地能力：

- 摘要提取（正文有效句）
- 待办提取（任务/列表行）
- 正文净化（去除引用 token）
- 上下文摘录（每个引用截取若干行）

输出结构：

- `## AI整理摘要`
- `## 待办拆解`
- `## 整理后正文`
- `## 上下文引用`

## 3. 图谱构建

`KnowledgeGraphBuilder` 基于引用关系构图：

- 节点：笔记（id/title/path）
- 边：引用（from -> to，携带 token）

图谱在首页绘制为可视化关系板。

## 4. RAG 检索

`RagSearchEngine` 采用轻量加权：

- 标题命中高权重
- 路径命中中权重
- 正文命中词频加权
- 图谱度数（节点连接数）额外加分

最终用于首页检索结果排序。

## 后续升级建议（V2）

1. Chunk + Embedding
    - 按段落/标题切片
    - 写入 PGVector / Milvus
2. Hybrid Retrieval
    - 向量召回 + BM25 召回
    - 交叉重排（reranker）
3. LLM 整理器
    - 用 Prompt 模板替换本地规则
    - 输出固定 JSON Schema，保证可解析
4. 图谱增强
    - 引入实体抽取（人名/项目/时间）
    - 支持关系类型（依赖/引用/冲突/计划）

## 与数据源关系

- SQLite：服务端本地存储
- PostgreSQL：服务端远程存储
- 客户端 `sync`：通过 API 在两个数据源间双向合并（按 version）
