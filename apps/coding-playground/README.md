# Coding Playground 生成工作台方案

## Summary

把这件事做成独立可启动的多模块 app：`apps/coding-playground`。它不依赖 `kcloud` 运行时，但内置 `kcloud` 风格脚手架模板族，负责维护低代码风格的模型元数据、DTO 元数据、代码模板元数据，并基于 SQLite + Jimmer 持久化这些定义，再全自动生成 Kotlin 源码模块、Gradle/composite build 接线和可选 `.kts` ETL 包裹层。

新增一条硬要求：**平台本身要支持完整增删改查**，并且第一期生成器也要能生成面向业务实体的 CRUD 全链路骨架。也就是说，`coding-playground` 既是元数据设计台，也是元数据管理后台。

## Implementation Changes

### 1. App 形态与模块布局

在 `apps/coding-playground` 下建立独立多模块结构：

- `apps/coding-playground`
  Compose Desktop 宿主，提供模型设计台、模板管理台、目标工程配置、生成预览与执行入口。
- `apps/coding-playground/shared`
  放元数据 DTO、CRUD 请求/响应 DTO、生成任务 DTO、路径变量与模板执行协议。
- `apps/coding-playground/server`
  放 Jimmer + SQLite 元数据存储、CRUD 服务、生成引擎、模板执行器、composite build 自动接入器、路径变量解析器。

默认沿用仓库现有“桌面 app + 内嵌本地 Ktor 服务 + Koin”模式，但不依赖 `kcloud` 业务模块。

### 2. 元数据域模型与平台 CRUD

元数据真源使用 SQLite，全部通过 Jimmer interface 实体维护。核心模型固定为：

- `ProjectMeta`
- `BoundedContextMeta`
- `EntityMeta`
- `FieldMeta`
- `RelationMeta`
- `DtoMeta`
- `DtoFieldMeta`
- `TemplateMeta`
- `GenerationTargetMeta`
- `EtlWrapperMeta`

每个元数据聚合都必须具备完整 CRUD 能力：

- Create：新建项目、上下文、实体、字段、关系、DTO、模板、目标、ETL 包裹器
- Read：列表、详情、树形展开、按上下文聚合读取
- Update：编辑名称、说明、字段定义、关系、模板参数、输出路径、变量配置
- Delete：删除单个节点，并做受控级联规则
- Search：按名称、类型、上下文、标签检索
- Reorder：字段、DTO 字段、模板输出顺序支持排序调整

级联规则需要固定下来，避免实现时再做判断：

- 删除 `ProjectMeta` 级联删除其下全部元数据
- 删除 `BoundedContextMeta` 级联删除其下实体/DTO/模板/目标
- 删除 `EntityMeta` 时必须先删除或解除关联 `RelationMeta` 与引用它的 `DtoFieldMeta`
- 删除 `TemplateMeta` 不删除业务元数据，但需要阻止仍被 `GenerationTargetMeta` 引用的模板被直接删掉
- 删除操作统一走软校验 + 明确错误提示，不做静默级联破坏

平台自身必须暴露本地 CRUD API，默认以 `spring2ktor` 路由提供，例如：

- `/api/playground/projects`
- `/api/playground/contexts`
- `/api/playground/entities`
- `/api/playground/dtos`
- `/api/playground/templates`
- `/api/playground/generation-targets`

### 3. 生成内核与模板体系

生成系统采用固定流水线：

`SQLite 元数据 -> 内部 IR -> 模板渲染 -> 可选 ETL -> 文件落盘 -> 目标工程自动接入`

硬约束如下：

- 内部只维护一套编译器无关 IR，不把 KSP / KotlinPoet / Jimmer KSP 细节泄漏到元数据存储层。
- 代码模板默认输出稳定源码，不强制脚本化；`.kts` ETL 只作为可选后处理层。
- 输出路径支持 `$HOME` 和自定义变量，解析顺序固定为：
  用户配置变量 > 环境变量 > 系统属性 > 内置变量。
- composite build 自动接入只修改生成器托管的 marker block，保证幂等。

第一期模板族直接内置以下产物生成：

- Jimmer entity interface
- DTO / Query / Request / Response 数据类
- Repository / query support skeleton
- Service / facade skeleton
- `spring2ktor` route skeleton
- Koin module skeleton
- 元数据单例对象
- Gradle 模块 `build.gradle.kts`
- 目标工程 `settings.gradle.kts` / `includeBuild` 接入片段

新增 CRUD 要求后，生成器默认还要生成业务实体的 CRUD 骨架：

- 列表查询接口
- 详情查询接口
- 新增接口
- 更新接口
- 删除接口
- 基础查询条件对象与分页对象
- 对应 service / route / DTO 映射占位实现

第一期只要求生成“可编译的 CRUD 骨架”，不要求自动生成复杂业务规则。

### 4. 上层应用消费方式

所有生成结果必须暴露单例 object，方便上层应用直接获取元数据，不需要反射扫描。固定 API 形状为：

- 每个上下文生成一个聚合对象，例如 `object XxxMetadata`
- 每个聚合对象至少暴露：
  `models()`
  `dtos()`
  `templates()`
  `findModel(name)`
  `findDto(name)`
- 每个目标工程生成一个顶层索引对象，例如 `object GeneratedMetadataIndex`

这些 descriptor 必须是稳定、可序列化、无运行时数据库依赖的纯 Kotlin 数据结构。上层 app 获取的是导出后的静态元数据对象，不回连 `coding-playground` 数据库。

### 5. 与现有仓库的关系

- `coding-playground` 是独立 app，不直接依赖现有 `kcloud` 插件。
- 第一阶段不迁移现有 `lib/system-spec` 和 `apps/kcloud/plugins/system/*`。
- 但内置一套 `kcloud-style scaffold preset`，能生成 `composeApp + plugins/<context> + client/server/spi` 风格布局。
- 后续若要把 `system-spec`、RBAC、配置、通知等域迁进来，只走“先在 playground 建模，再导出源码模块”的路径。

## Public APIs / Interfaces

第一期需要明确这些公共接口：

- `PathVariableResolver`
- `MetadataSnapshotService`
- `GenerationPlanner`
- `TemplateRenderer`
- `EtlWrapperExecutor`
- `CompositeBuildIntegrator`
- `ProjectMetaService` / `ContextMetaService` / `EntityMetaService` / `DtoMetaService` / `TemplateMetaService`
  这些服务统一提供 CRUD + query + validate 能力
- `GeneratedMetadataIndex`

Jimmer 约束固定为：

- 所有持久化实体、生成实体统一为 `interface` + Jimmer 注解
- 不允许 class-based entity fallback
- 关系建模统一走 Jimmer 关联注解，不允许手写字符串 foreign key 模式代替

## Test Plan

必须覆盖下面这些场景：

- 平台元数据 CRUD：
  项目、上下文、实体、字段、关系、DTO、模板、目标的增删改查全部可用
- 删除级联与引用校验：
  删除被引用实体、模板、DTO 时能正确拦截或级联
- SQLite + Jimmer：
  元数据表能完成建模、关联、增删改查，关系字段和业务键规则正确
- 元数据快照：
  导出后再次导入，结构与内容稳定
- 路径变量：
  `$HOME`、自定义变量、相对路径都能正确解析
- 代码生成：
  给定最小上下文模型，能生成完整源码模块集合并通过 Gradle 编译
- CRUD 骨架生成：
  生成后的业务模块至少包含 list/get/create/update/delete 入口和对应 DTO
- ETL 包裹：
  启用 ETL 时能正确转换文本；未启用时直接落原文
- composite build 自动接入：
  重复执行保持幂等，只修改受管标记块
- 元数据单例对象：
  `GeneratedMetadataIndex` 和 `XxxMetadata` 可直接返回正确 descriptor
- 脚手架预设：
  `kcloud-style scaffold preset` 至少有一个 smoke test
- 失败场景：
  非法字段类型、关系断裂、模板变量缺失、目录不可写、ETL 失败、自动接入冲突，都要给出可读错误

## Assumptions

- 独立 app 名称采用 `coding-playground`
- 第一阶段只做底座与内置示例模板，不直接迁移现有 `kcloud` 或 `system-spec` 业务域
- 默认交互形态是 Compose Desktop 工作台
- 元数据真源为 SQLite，支持导出稳定快照到代码仓库
- 默认输出是源码模块；ETL 只作为可选包裹层
- composite build 第一阶段按你的要求做全自动接入，但实现上只允许改写生成器托管标记区块
- “要求能增删改查” 同时适用于：
  `coding-playground` 自身的元数据管理
  以及第一期生成出来的业务模块 CRUD 骨架
