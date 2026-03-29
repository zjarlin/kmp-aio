# KCloud 插件开发指南

## 目标

`kcloud` 现在通过 Gradle 约定插件 `site.addzero.buildlogic.kmp.cmp-kcloud-aio` 自动聚合插件模块。

这会直接改变插件接入方式：

- 插件开发者只需要把模块放到 `apps/kcloud/plugins/**` 下
- 壳层不再手工维护插件依赖列表
- `composeApp`、`server`、`shared` 会在构建阶段自动发现插件入口

只要插件遵守下面的目录结构和命名约定，主壳层就会自动接入它。

## 必要目录结构

推荐结构：

```text
apps/kcloud/plugins/
└── <group>/<plugin-id>/
    ├── build.gradle.kts          # 可选，共享/common/api 根模块
    ├── src/commonMain/kotlin/...
    ├── ui/                       # 可选，Compose UI 分面
    │   ├── build.gradle.kts
    │   └── src/commonMain/kotlin/...
    └── server/                   # 可选，服务端分面
        ├── build.gradle.kts
        └── src/jvmMain/kotlin/...
```

规则：

- 所有社区插件都必须放在 `apps/kcloud/plugins/**` 下
- 根模块用于放共享模型、API 契约和公共逻辑
- 当插件的 UI 与共享契约能够明确分离时，使用 `ui/`
- 只有插件需要暴露后端服务或路由时才使用 `server/`
- 插件可以是纯 UI、纯 server 或全栈，但最常见的是纯 UI 插件

## 自动聚合规则

`site.addzero.buildlogic.kmp.cmp-kcloud-aio` 会按以下约定扫描插件模块：

- Compose 聚合目标：
  - 如果根模块本身包含 `@Route` 页面或 Compose Koin 模块，则使用 `<plugin>`
  - 否则使用 `<plugin>:ui`
- Server 聚合目标：
  - 当模块里存在 server Koin 模块或 `Route` 注册函数时，使用 `<plugin>:server`
- Shared 路由快照依赖：
  - 指向 Compose 聚合目标的 `:compileKotlinJvm`

这意味着壳层是按模块结构自动聚合的，不再需要额外的注册文件。

## Compose 插件契约

对于 Compose 插件分面，聚合器要求同时满足路由和 DI 约定。

### 1. 页面

遵循现有的 KCloud 路由规则：

- 页面入口必须是顶层 `@Composable` 函数
- 入口函数名必须以 `Screen` 结尾
- 页面入口必须放在插件自己的 `screen` 包下
- 每个页面入口都必须声明 `@Route(...)`

示例：

```kotlin
package site.addzero.kcloud.plugins.example.screen

import androidx.compose.runtime.Composable
import site.addzero.annotation.Route

@Route(
    value = "Examples",
    title = "Plugin Overview",
    routePath = "examples/plugin-overview",
    icon = "Extension",
    order = 10.0,
)
@Composable
fun PluginOverviewScreen() {
    // ...
}
```

### 2. Compose Koin 模块

聚合器会扫描顶层 `class` 或 `object`，名称必须以下列后缀之一结尾：

- `ComposeKoinModule`
- `KoinModule`

建议：新插件优先使用更明确的 `ComposeKoinModule` 后缀。

示例：

```kotlin
package site.addzero.kcloud.plugins.example

import org.koin.core.annotation.ComponentScan

@ComponentScan("site.addzero.kcloud.plugins.example")
class ExampleComposeKoinModule
```

### 3. Gradle 接线

对于 Compose 分面，必须保留现有的 route/KSP 链路，确保 `RouteKeys` 和 `RouteTable` 能正确生成。

典型的 `ui/build.gradle.kts` 或根模块 `build.gradle.kts`：

```kotlin
plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

val libs = versionCatalogs.named("libs")
val sharedSourceDir = project(":apps:kcloud:shared")
    .extensions
    .getByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()
    .sourceSets
    .getByName("commonMain")
    .kotlin
    .srcDirs
    .first()
    .absolutePath
val routeOwnerModuleDir = project(":apps:kcloud:composeApp")
    .extensions
    .getByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()
    .sourceSets
    .getByName("commonMain")
    .kotlin
    .srcDirs
    .first()
    .absolutePath

ksp {
    arg("sharedSourceDir", sharedSourceDir)
    arg("routeGenPkg", "site.addzero.generated")
    arg("routeOwnerModule", routeOwnerModuleDir)
    arg("routeModuleKey", project.path)
}

dependencies {
    add("kspCommonMainMetadata", libs.findLibrary("site-addzero-route-processor").get())
    add("kspJvm", libs.findLibrary("site-addzero-route-processor").get())
}
```

`routeModuleKey` 必须保持稳定。它是生成路由归属关系的一部分契约。

### 4. 可选：Ktorfit API 聚合

如果插件里定义了多组 Ktorfit service interface，并且你希望有一个统一生成的聚合入口，而不是手工一个个拼 factory，可以组合使用：

- `site.addzero.buildlogic.kmp.kmp-ktorfit`
- `site.addzero.ksp.apiprovider`

这个 Gradle 插件入口定义在：

- `/Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/ksp/metadata/apiprovider-gradle-plugin/build.gradle.kts`

它的职责很直接：

- 扫描 Ktorfit HTTP service interface
- 执行 `apiprovider-processor`
- 生成共享聚合类 `site.addzero.generated.api.ApiProvider`

当前生成器行为：

- 生成包名：`site.addzero.generated.api`
- 生成对象名：`ApiProvider`
- 每个识别到的 Ktorfit interface 会变成 `ApiProvider` 上的一个属性
- 该属性通过对应的生成入口 `ktorfit.createXxxApi()` 创建

典型插件接线：

```kotlin
plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.ksp.apiprovider")
}
```

只有当插件内确实存在一组相关联的 Ktorfit API，且值得共享一个聚合入口时，才使用这个方案。如果只有一两个 API，不要为了“统一”再多引入一层生成代码。

### 5. KCloud API Client 包装器模式

在 `kcloud` 内部，除了纯 `ApiProvider` 之外，还反复出现另一种模式：

- `object XxxApiClient`
- `configureBaseUrl(...)`
- 一组共享的 `HttpClientFactory` profile
- 一个或多个惰性暴露的 Ktorfit API，例如 `xxxApi`

仓库里已经存在的典型例子：

- `ServerApiClient`
- `RbacApiClient`
- `AiChatApiClient`
- `KnowledgeBaseApiClient`
- `McuConsoleApiClient`

这些包装器非常适合做成 KSP 生成目标，因为它们大部分都是机械性代码：

- 保存 `baseUrl`
- 选择一个 `HttpClientFactory` profile
- 调用 `Ktorfit.Builder().baseUrl(...).httpClient(...).build()`
- 以属性形式暴露 `createXxxApi()` / `create<SomeApi>()` 结果

后续如果继续做生成器，应把这种包装器本身当成真正的生成目标，而不是把注意力只放在 remote service 调用层。

适合生成的目标：

- 函数体基本就是 `baseUrl + profile + Ktorfit.createXxxApi()` 的包装器
- 只暴露 API 属性，不包含额外领域策略

不要强行生成这类同时承担真实业务行为的包装器，例如：

- fallback 策略
- 跨请求缓存
- 批处理
- 状态恢复
- 复杂 helper 方法

`site.addzero.ksp.apiprovider` 当前状态：

- 已经能生成普通的 `site.addzero.generated.api.ApiProvider`
- **还不能** 生成 `object XxxApiClient` 这类包装对象
- **还没有** 覆盖 `configureBaseUrl(...)`、`HttpClientFactory` profile 选择和分组缓存 API

所以，如果你想去掉 KCloud 里手写的 client wrapper，下一步处理器工作应该是：

1. 继续扫描 Ktorfit interface
2. 为包装器名称、基础 URL、HTTP client profile 增加类型化参数
3. 生成 `XxxApiClient` 包装对象，而不是只生成全局 `ApiProvider`
4. 在需要时支持多 API 分组包装器

评估一个类是否还要手写时，遵循这条规则：如果它只是一个薄薄的 Ktorfit wrapper，就应优先把它视为未来的 KSP 生成目标，而不是继续保留手工样板代码。

## Server 插件契约

Server 聚合是可选的。只有插件需要提供后端 API、存储或本地服务行为时才添加。

### 1. Server Koin 模块

显式的 server Koin 模块不是强制要求。

壳层已经内置了 `KCloudServerScanKoinModule`，其中包含 `@ComponentScan("site.addzero")`。这意味着，只要插件内部使用了普通的 `@Single`、`@Factory` 之类注解，通常就能被发现，而不必额外套一层包装模块。

只有当你确实需要显式模块组合时，才添加专门的 server Koin 模块。如果要加，聚合器会扫描 `server/src/jvmMain/kotlin` 下名称以 `ServerKoinModule` 结尾的类。

示例：

```kotlin
package site.addzero.kcloud.plugins.example

import org.koin.core.annotation.ComponentScan

@ComponentScan("site.addzero.kcloud.plugins.example")
class ExampleServerKoinModule
```

### 2. 路由注册函数

聚合器会扫描 `*Routes.kt` 文件里的公开顶层函数，函数形状必须是：

```kotlin
fun Route.exampleRoutes()
```

示例：

```kotlin
package site.addzero.kcloud.plugins.example

import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.exampleRoutes() {
    get("/api/example/health") {
        // ...
    }
}
```

### 3. Gradle 接线

典型的 `server/build.gradle.kts`：

```kotlin
plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}
```

按需添加 server 专属依赖即可，不要重复声明那些已经属于 `apps/kcloud/server` 壳层的 starter 模块。

## 壳层如何接入你的插件

当插件被放到 `apps/kcloud/plugins/**` 下，并被 `modules-buddy` 纳入后，壳层会在构建期间自动完成以下事情：

1. `apps/kcloud/shared` 先依赖插件 UI 的编译任务，确保路由元数据优先就绪。
2. `apps/kcloud/composeApp` 把识别到的 UI 模块加入依赖。
3. `apps/kcloud/composeApp` 根据识别到的 Compose Koin 模块生成 `KCloudComposeKoinApplication`。
4. `apps/kcloud/server` 把识别到的 `server` 分面加入依赖。
5. `apps/kcloud/server` 根据识别到的 server 入口生成 `KCloudServerStarterKoinApplication` 和 `registerKCloudPluginRoutes()`。

因此，现在已经不再需要手工修改壳层源码。

## 贡献者检查清单

- 插件位于 `apps/kcloud/plugins/**` 下
- UI 分面包含顶层 `@Route` 的 `...Screen()` 函数
- UI 分面对外暴露了 `*ComposeKoinModule` 或 `*KoinModule`
- 如果存在 server 分面，则在某个 `*Routes.kt` 文件中暴露公开的 `fun Route.xxxRoutes()`
- 只有在确实需要显式模块组合时，server 分面才添加 `*ServerKoinModule`
- Route KSP 参数正确指向 `:apps:kcloud:shared` 和 `:apps:kcloud:composeApp`
- 在让壳层自动聚合前，插件本身已经可以单独编译通过

## 验证命令

新增插件后，使用以下命令验证接入是否正确：

```bash
./gradlew :apps:kcloud:shared:compileCommonMainKotlinMetadata
./gradlew :apps:kcloud:composeApp:compileKotlinJvm
./gradlew :apps:kcloud:server:compileKotlinJvm
```

如果其中任何一个失败，就说明插件还没有正确遵守壳层契约。
