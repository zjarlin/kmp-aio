# KCloud - 插件化私有云工作台

> KCloud 现在还不是“已完成的 Nextcloud 替代品”，而是一个基于 Kotlin Multiplatform / Compose Desktop 的插件化桌面壳。
>
> 它把笔记、安装包归纳、SSH、WebDAV、Dotfiles、环境搭建等能力聚合到一个本地工作台里，并通过本地 HTTP 聚合层把 UI 插件与 server 插件接起来。
>
> 愿景没有变：围绕个人私有云与个人基础设施，逐步收拢散落在各处的文件、配置与环境操作。

---

## 目录

- [当前定位](#当前定位)
- [当前架构](#当前架构)
- [内置插件](#内置插件)
- [运行方式](#运行方式)
- [路线图](#路线图)
- [已知边界](#已知边界)

---

## 当前定位

- 当前项目重点是 **插件化壳层**，不是完整同步引擎成品。
- 主应用负责：
  - Compose Desktop 窗口与侧边栏
  - 系统托盘与快捷键
  - 本地 Ktor HTTP 聚合层
  - Koin 注入与插件生命周期
- 现阶段已经适合承载多个“工作台型”功能插件：
  - 笔记
  - 安装包归纳
  - SSH 工作区
  - WebDAV 工作区
  - Chezmoi Dotfiles
  - Unix 环境搭建

---

## 当前架构

### 1. 插件聚合方式

当前不是运行时热插拔，而是 **编译时 SPI 聚合**。

主应用通过 [`src/jvmMain/kotlin/com/kcloud/KCloudKoinApplication.kt`](src/jvmMain/kotlin/com/kcloud/KCloudKoinApplication.kt) 的 `@KoinApplication(configurations = ["kcloud"])` 聚合所有插件模块，再由 runtime 一次性装配 Koin 模块。

### 2. 壳层职责

[`src/jvmMain/kotlin/com/kcloud/KCloudRuntime.kt`](src/jvmMain/kotlin/com/kcloud/KCloudRuntime.kt) 负责三件事：

1. 组装插件注册表与 server 插件注册表
2. 构建“同步 / 管理 / 系统”三层菜单树
3. 启动本地 HTTP 服务，并把各 `*-server-plugin` 的路由挂进来

桌面端启动时会自动启动本地聚合服务；独立 server 模式则只启动 HTTP 层，不启动桌面 UI。

### 3. 插件契约

当前扩展面来自 [`plugins/plugin-api`](plugins/plugin-api)：

| 接口 | 作用 |
| --- | --- |
| [`KCloudPlugin`](plugins/plugin-api/src/commonMain/kotlin/com/kcloud/plugin/KCloudPlugin.kt) | 客户端页面、生命周期、菜单入口 |
| [`KCloudServerPlugin`](plugins/plugin-api/src/commonMain/kotlin/com/kcloud/plugin/KCloudPlugin.kt) | 本地 HTTP 路由扩展入口 |
| [`KCloudMenuEntry`](plugins/plugin-api/src/commonMain/kotlin/com/kcloud/plugin/KCloudMenu.kt) | 壳层菜单描述，支持 `parentId`、`visible`、`content` |

菜单树能力：

- `parentId` 用于声明父子关系
- `visible` 用于控制是否显示
- `level` 不是配置字段，而是树节点根据祖先链计算出来的派生值
- 当前壳层固定根分组为：`同步`、`管理`、`系统`

### 4. 模块拆分约定

`apps/kcloud` 当前按“功能插件 + 独立 server 插件”组织：

```text
apps/kcloud/
├── src/                         # 桌面壳与 runtime
├── server/                      # 独立 server 入口
└── plugins/
    ├── plugin-api/
    ├── notes-plugin/
    ├── notes-server-plugin/
    ├── package-organizer-plugin/
    ├── package-organizer-server-plugin/
    ├── ssh-plugin/
    ├── ssh-server-plugin/
    ├── webdav-plugin/
    ├── webdav-server-plugin/
    ├── dotfiles-plugin/
    ├── dotfiles-server-plugin/
    ├── environment-plugin/
    ├── environment-server-plugin/
    └── ...
```

---

## 内置插件

### 已可用

| 功能 | UI 模块 | Server 模块 | 现状 |
| --- | --- | --- | --- |
| 桌面集成 | `desktop-integration-plugin` | 无 | 已提供系统托盘与全局快捷键 |
| 笔记 | `notes-plugin` | `notes-server-plugin` | 接入 `apps/notes` / `apps/notes/server` 的 VibeNotes；当前不是专门的 Nextcloud Notes 适配器 |
| 安装包归纳 | `package-organizer-plugin` | `package-organizer-server-plugin` | 支持扫描目录、分类展示、一键归档 |
| 快速迁移 | `quick-transfer-plugin` | `quick-transfer-server-plugin` | 已接入同步状态面板、阶段进度、立即同步 / 暂停 / 继续控制；页面与 HTTP 路由统一通过 `QuickTransferService` 访问状态 |
| 文件管理 | `file-plugin` | `file-server-plugin` | 支持读取记录、虚拟目录浏览、搜索 / 排序、冲突处理、在文件夹中显示；页面与 HTTP 路由统一通过 `FileWorkspaceService` 访问 |
| 迁移记录 | `transfer-history-plugin` | `transfer-history-server-plugin` | 支持读取统计、同步队列、清理已完成队列；页面与 HTTP 路由统一通过 `TransferHistoryService` 访问 |
| 服务器管理 | `server-management-plugin` | `server-management-server-plugin` | 支持维护统一服务器列表；桌面 UI 与独立 server 共用同一套 CRUD service，并暴露 `/api/servers` |
| SSH 工作区 | `ssh-plugin` | `ssh-server-plugin` | 支持保存配置、测试连接、浏览目录、创建目录、删除路径 |
| WebDAV 工作区 | `webdav-plugin` | `webdav-server-plugin` | 支持保存配置、测试连接、浏览目录、创建目录、删除路径 |
| Dotfiles | `dotfiles-plugin` | `dotfiles-server-plugin` | 通过 chezmoi 初始化仓库、查看 diff、应用变更 |
| 环境搭建 | `environment-plugin` | `environment-server-plugin` | 仅限 Unix；支持本机或 SSH 执行安装脚本，内置 `JDK17`、`JDK21`、`Git`、`MySQL`、`PostgreSQL`、`Redis`、`Nginx`、`Docker`、`Node.js`、`pnpm` |
| 设置 | `settings-plugin` | 无 | 支持主题、传输、更新等通用设置的本地持久化 |

### 仍待深化

| 功能 | 现状 |
| --- | --- |
| 快速迁移 / 文件管理 / 迁移记录 | UI 与路由已经切到插件自己的 service 契约，但当前 server 实现仍在适配 `lib:kcloud-core` 的旧同步 / 数据库模型，后续还要继续现代化 |

### 当前本地 HTTP 聚合层

桌面壳会先启动本地 Ktor 服务，再由各 `*-server-plugin` 把路由挂上来。当前可见的前缀包括：

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

- 请确保 Gradle / IDE 使用 JDK 11 或更高版本
- 会启动 Compose Desktop 壳层
- 会自动启动本地 HTTP 聚合服务
- 默认优先尝试 `127.0.0.1:18080`
- 如果端口被占用，会自动选择可用端口
- 端口也可以通过 `KCLOUD_LOCAL_SERVER_PORT` 或 `-Dkcloud.localServer.port=...` 覆盖
- 这是当前 **唯一** 的桌面应用入口；实际 `main` 在 [`src/jvmMain/kotlin/com/kcloud/Main.kt`](src/jvmMain/kotlin/com/kcloud/Main.kt)

### 独立服务

```bash
./gradlew :apps:kcloud:server:run
```

- 同样要求 Gradle / IDE 使用 JDK 11 或更高版本
- 只启动本地 HTTP 聚合层
- 适合单独调试 `*-server-plugin` 路由与服务逻辑

### 启动入口说明

- 不要直接运行 `plugins/*:jvmRun`
- `plugins/*` 下的模块当前定位是 **库模块 / 插件模块**，不是独立桌面应用
- 例如 [`plugins/desktop-integration/desktop-integration-plugin/build.gradle.kts`](plugins/desktop-integration/desktop-integration-plugin/build.gradle.kts) 只声明了库依赖，没有配置 `mainClass`
- 真正的桌面主入口定义在 [`build.gradle.kts`](build.gradle.kts) 的 `kotlin.jvm().mainRun` / `compose.desktop.application`
- 对应的实际启动类是 [`src/jvmMain/kotlin/com/kcloud/Main.kt`](src/jvmMain/kotlin/com/kcloud/Main.kt)
- 如果误跑了某个插件模块的 `jvmRun`，常见报错会是：`No main class specified and classpath is not an executable jar`

### IDE 启动指引

- Gradle Run Configuration 请指向 `:apps:kcloud:jvmRun`
- 如果按主类启动，请使用 `com.kcloud.MainKt`
- 需要只调本地 HTTP 聚合层时，再单独运行 `:apps:kcloud:server:run`

### 新增插件的当前接入方式

1. 在 `plugins/<feature>/` 下创建插件家族目录
2. 在家族目录中放置 `*-plugin`，需要本地 HTTP 能力时再增加 `*-server-plugin`
3. 让叶子模块通过 `@Configuration("kcloud")` 参与编译时 SPI 聚合
4. 让主应用只依赖家族聚合模块，例如 `:apps:kcloud:plugins:file`

---

## 路线图

| 状态 | 内容 |
| --- | --- |
| 已落地 | 插件化桌面壳、树状侧边栏、静态插件聚合、本地 HTTP 聚合层、桌面托盘与快捷键、笔记接入、快速迁移面板、文件管理页、迁移记录页、服务器管理页、设置页、安装包归档、SSH 工作区、WebDAV 工作区、Dotfiles、Unix 环境搭建 |
| 正在补强 | 快速迁移 / 文件管理 / 迁移记录的 server 实现继续脱离旧 `lib:kcloud-core` 模型；把未接入的拖拽上传链路也纳入新 service 边界 |
| 下一阶段 | 继续保持编译时 SPI 聚合，并补齐插件元数据与插件市场页面；统一 SSH / WebDAV / File 的工作区抽象；给环境搭建补安装任务历史与实时日志；给笔记插件补专门的 Nextcloud / WebDAV Notes 适配；继续收敛 legacy 同步 / 数据模型 |

---

## 已知边界

- 这还不是完整的 Nextcloud 替代品；当前更像“插件化私有云工作台”。
- 笔记插件当前接的是 `apps/notes`，不是 Nextcloud Notes 专用实现。
- 插件发现目前仍是静态聚合，不是动态 SPI 自动扫描。
- 快速迁移 / 文件管理 / 迁移记录的 UI 已经不再直接碰 legacy manager，但对应 server 实现仍在适配 `lib:kcloud-core`，还不是最终形态。
- `environment-plugin` 的“一键安装”本质是本机执行或 SSH 会话远程执行，不是 SSH 隧道或远程 agent 部署。
- `security-plugin` 目录当前没有接入主应用聚合，文档不把它算作已落地能力。

---

如果你接下来要继续做实现，优先级最高的不是再加新概念，而是把 legacy `kcloud-core` 的同步 / 数据库模型继续收敛到新的插件体系里。
