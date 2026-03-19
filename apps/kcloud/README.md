# KCloud - 模块化单体私有云工作台

> KCloud 现在不是“可运行时热插拔的插件平台”，而是一个基于 Kotlin Multiplatform / Compose Desktop 的 **编译期 feature 聚合桌面壳**。
>
> 它把笔记、安装包归档、快速迁移、Docker Compose 管理、SSH、WebDAV、Dotfiles、环境搭建、设置页 AI 等能力收进同一个桌面工作台，并通过本地 HTTP 聚合层把 UI feature 与 server feature 接起来。

---

## 目录

- [当前定位](#当前定位)
- [当前架构](#当前架构)
- [内置 Features](#内置-features)
- [运行方式](#运行方式)
- [开发约定](#开发约定)
- [路线图](#路线图)
- [已知边界](#已知边界)

---

## 当前定位

- 当前项目定位是 **模块化单体桌面壳**，不是运行时插件系统，也不是完整 Nextcloud 替代品。
- 主应用负责：
  - Compose Desktop 窗口与树状侧边栏
  - 系统托盘与全局快捷键
  - 本地 Ktor HTTP 聚合层
  - Koin 编译期聚合与 feature 生命周期
- 当前的模块拆分目标是：
  - 主应用只依赖 `:apps:kcloud:features:<feature>` 聚合模块
  - 具体业务内部再按 `client` / `server` / `spi` / provider 叶子模块拆开
  - 不做运行时加载 / 卸载，不保留旧 alias 菜单兼容层

---

## 当前架构

### 1. 编译期 feature 聚合

Koin 根入口在 [`src/jvmMain/kotlin/com/kcloud/KCloudKoinApplication.kt`](src/jvmMain/kotlin/com/kcloud/KCloudKoinApplication.kt)，通过 `@KoinApplication(configurations = ["kcloud"])` 聚合所有叶子模块的 `@Configuration("kcloud")` 定义。

当前是 **编译期聚合**：

- 没有运行时插件市场加载器
- 没有运行时 unload / reload
- 没有 `ServiceLoader` 动态发现链路

### 2. 壳层职责

[`src/jvmMain/kotlin/com/kcloud/KCloudRuntime.kt`](src/jvmMain/kotlin/com/kcloud/KCloudRuntime.kt) 负责：

1. 解析 `KCloudFeatureRegistry`
2. 聚合全部 `KCloudServerFeature`
3. 启动本地 HTTP 服务
4. 驱动桌面 feature 生命周期

[`src/commonMain/kotlin/com/kcloud/app/KCloudShellState.kt`](src/commonMain/kotlin/com/kcloud/app/KCloudShellState.kt) 只做壳层逻辑：

- 固定三组根菜单：`同步`、`管理`、`系统`
- 基于 feature 自己声明的菜单构建树
- 维护选中项和展开项
- 不再硬编码 `quick` / `docker-compose` / `server` / `history` 等旧 alias

### 3. feature 契约

共享契约在 [`features/feature-api`](features/feature-api)：

| Contract | 作用 |
| --- | --- |
| [`KCloudFeature`](features/feature-api/src/commonMain/kotlin/com/kcloud/feature/KCloudFeature.kt) | 客户端页面、生命周期、菜单入口 |
| [`KCloudServerFeature`](features/feature-api/src/commonMain/kotlin/com/kcloud/feature/KCloudFeature.kt) | 本地 HTTP 路由扩展入口 |
| [`KCloudMenuEntry`](features/feature-api/src/commonMain/kotlin/com/kcloud/feature/KCloudMenu.kt) | 菜单描述，支持 `parentId`、`visible`、`content` |
| [`KCloudMenuTreeBuilder`](features/feature-api/src/commonMain/kotlin/com/kcloud/feature/KCloudMenu.kt) | 树构建、循环校验、可见叶子展开 |

菜单规则：

- `parentId` 声明父子关系
- `visible` 控制是否显示
- `level` 是 `KCloudMenuNode` 的计算属性，不是配置字段
- 壳层默认根组固定为 `group.sync`、`group.management`、`group.system`

### 4. 目录形态

`apps/kcloud` 现在按“无源码聚合模块 + 叶子实现模块”组织：

```text
apps/kcloud/
├── src/                                # 桌面壳与 runtime
├── server/                             # 独立本地服务入口
└── features/
    ├── feature-api/                    # 壳层共享契约
    ├── quick-transfer/                 # 无源码聚合模块
    │   ├── client/
    │   └── server/
    ├── compose/
    │   ├── client/
    │   └── server/
    ├── ai/
    │   ├── client/
    │   ├── server/
    │   ├── spi/
    │   └── ollama-provider/
    └── ...
```

约束：

- `features/<feature>/` 聚合模块本身不放 `src/`
- 聚合模块只做依赖转发
- 真正的 UI / server / provider / SPI 都落在叶子模块

---

## 内置 Features

| Feature | 聚合模块 | 叶子模块 | 现状 |
| --- | --- | --- | --- |
| 桌面集成 | `features/desktop-integration` | `client` | 已提供系统托盘与全局快捷键 |
| 快速迁移 | `features/quick-transfer` | `client` + `server` | 已接入同步状态、立即同步、暂停 / 继续、拖拽接收 service |
| 迁移记录 | `features/transfer-history` | `client` + `server` | 已接入统计、队列读取、清理已完成队列 |
| 服务器管理 | `features/server-management` | `client` + `server` | 已有服务器 CRUD 与 `/api/servers` |
| Docker Compose 管理 | `features/compose` | `client` + `server` | 已有 Compose stack 管理界面和服务端执行能力 |
| 文件管理 | `features/file` | `client` + `server` | 已有目录浏览、搜索 / 排序、冲突处理与工作区 service |
| 笔记 | `features/notes` | `client` + `server` | 接入 `apps/notes` / `apps/notes/server`，当前不是 Nextcloud Notes 专项适配 |
| 安装包归档 | `features/package-organizer` | `client` + `server` | 支持扫描目录、分类展示、一键归档 |
| SSH 工作区 | `features/ssh` | `client` + `server` | 支持保存配置、测试连接、浏览目录、创建目录、删除路径 |
| WebDAV 工作区 | `features/webdav` | `client` + `server` | 支持保存配置、测试连接、浏览目录、创建目录、删除路径 |
| Dotfiles | `features/dotfiles` | `client` + `server` | 通过 chezmoi 初始化仓库、查看 diff、应用变更 |
| 环境搭建 | `features/environment` | `client` + `server` | Unix only；支持本机或 SSH 执行，内置 `JDK17` / `JDK21` / `Git` / `MySQL` / `PostgreSQL` / `Redis` / `Nginx` / `Docker` / `Node.js` / `pnpm` |
| 设置 | `features/settings` | `client` | 支持主题与通用设置持久化 |
| AI 设置接入 | `features/ai` | `client` + `server` + `spi` + `ollama-provider` | 作为设置页 section 注入；首个 provider 是 Ollama |

### 当前本地 HTTP 聚合层

桌面壳会先启动本地 Ktor 服务，再由各 `KCloudServerFeature` 把路由挂进来。当前可见前缀包括：

- `/api/notes`
- `/api/packages`
- `/api/ssh`
- `/api/webdav`
- `/api/dotfiles`
- `/api/environment`
- `/api/servers`
- `/api/sync`
- `/api/stats`
- `/api/queue`
- `/api/files`
- `/api/conflicts`

---

## 运行方式

### 桌面端

```bash
./gradlew :apps:kcloud:jvmRun
```

- 这是当前 **唯一** 的桌面应用入口
- 实际 `main` 在 [`src/jvmMain/kotlin/com/kcloud/Main.kt`](src/jvmMain/kotlin/com/kcloud/Main.kt)
- 会同时拉起 Compose Desktop 壳层和本地 HTTP 聚合服务
- 默认优先尝试 `127.0.0.1:18080`
- 端口可通过 `KCLOUD_LOCAL_SERVER_PORT` 或 `-Dkcloud.localServer.port=...` 覆盖

### 独立本地服务

```bash
./gradlew :apps:kcloud:server:run
```

- 只启动本地 HTTP 聚合层
- 适合单独调试 server feature 路由和 service

### IDE 指引

- Gradle Run Configuration 指向 `:apps:kcloud:jvmRun`
- 或直接以主类 `com.kcloud.MainKt` 启动

### 不要这样运行

- 不要直接运行 `features/*:jvmRun`
- `features/*` 下的模块是库模块，不是独立桌面应用
- 例如 [`features/desktop-integration/client/build.gradle.kts`](features/desktop-integration/client/build.gradle.kts) 只声明了库依赖，没有桌面入口

---

## 开发约定

新增 feature 时：

1. 在 `features/<feature>/` 建一个 **无源码聚合模块**
2. 真实实现放到 `client` / `server` / `spi` / provider 叶子模块
3. 让叶子模块通过 `@Configuration("kcloud")` 参与编译期聚合
4. 主应用只依赖 `:apps:kcloud:features:<feature>`，不直接依赖叶子模块

约束：

- 普通业务默认只拆 `client` + `server`
- 只有确实存在跨业务扩展点时，才增加 `spi`
- 不预留运行时热插拔结构

---

## 路线图

| 状态 | 内容 |
| --- | --- |
| 已落地 | 模块化单体桌面壳、树状侧边栏、编译期 feature 聚合、本地 HTTP 聚合层、桌面托盘与快捷键、笔记、快速迁移、迁移记录、服务器管理、Docker Compose 管理、文件管理、安装包归档、SSH、WebDAV、Dotfiles、环境搭建、AI 设置分区 |
| 已接线待补强 | 快速迁移 / 文件管理 / 迁移记录的 server 实现仍在收敛旧 `lib:kcloud-core` 同步 / 数据模型；部分工作区能力还没统一抽象 |
| 下一阶段 | 统一 SSH / WebDAV / File 的工作区抽象；给环境搭建补安装任务历史与实时日志；给笔记补 Nextcloud / WebDAV Notes 专项适配；继续缩减 legacy `kcloud-core` 依赖；继续把 `commonMain` / `jvmMain` 边界收紧到真正的平台差异处 |

---

## 已知边界

- 这不是运行时插件平台；当前只有编译期 feature 聚合
- 这不是完整 Nextcloud 替代品；当前更像“私有云工作台壳层”
- 笔记当前接的是 `apps/notes`，不是 Nextcloud Notes 专用实现
- AI 当前只做设置保存与连通性检测，不提供聊天面板或 agent 编排
- 环境搭建当前本质是“本机执行或 SSH 远程执行脚本”，不是远程 agent 部署

---

如果你接下来继续收敛架构，优先级最高的不是再发明“插件系统”，而是继续把 legacy `kcloud-core` 的同步 / 数据模型拆回各自 feature 边界里。
