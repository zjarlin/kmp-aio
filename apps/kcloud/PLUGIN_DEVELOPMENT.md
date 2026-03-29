# KCloud Plugin Development Guide

## Goal

`kcloud` now aggregates plugin modules through the Gradle convention plugin `site.addzero.buildlogic.kmp.cmp-kcloud-aio`.

That changes the contribution model:

- Plugin contributors focus on adding modules under `apps/kcloud/plugins/**`
- The shell no longer keeps a hand-maintained plugin dependency list
- `composeApp`, `server`, and `shared` discover plugin entrypoints during the build

If a plugin follows the layout and naming rules below, the main shell will pick it up automatically.

## Required Layout

Recommended structure:

```text
apps/kcloud/plugins/
└── <group>/<plugin-id>/
    ├── build.gradle.kts          # optional shared/common/api module
    ├── src/commonMain/kotlin/...
    ├── ui/                       # optional Compose UI facet
    │   ├── build.gradle.kts
    │   └── src/commonMain/kotlin/...
    └── server/                   # optional server facet
        ├── build.gradle.kts
        └── src/jvmMain/kotlin/...
```

Rules:

- Put every community plugin under `apps/kcloud/plugins/**`
- Use the root module for shared models, API contracts, and common logic
- Use `ui/` when the plugin has a clear UI facet separated from shared contracts
- Use `server/` only when the plugin exposes backend services or routes
- A plugin can be UI-only, server-only, or full-stack, but UI-only is the most common case

## Automatic Aggregation Rules

`site.addzero.buildlogic.kmp.cmp-kcloud-aio` scans plugin modules using these conventions:

- Compose aggregation target:
  - `<plugin>` if the root module itself contains `@Route` screens or a Compose Koin module
  - otherwise `<plugin>:ui`
- Server aggregation target:
  - `<plugin>:server` when it contains a server Koin module or a `Route` registrar
- Shared route snapshot dependency:
  - the Compose aggregation target's `:compileKotlinJvm`

This means the shell aggregates by module layout. No extra registration file is needed.

## Compose Plugin Contract

For a Compose plugin facet, the aggregator expects both routing and DI conventions.

### 1. Screens

Follow the existing KCloud route rules:

- Screen entrypoints must be top-level `@Composable` functions
- Entry function names must end with `Screen`
- Put screen entrypoints in the plugin's `screen` package
- Every screen entrypoint must declare `@Route(...)`

Example:

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

### 2. Compose Koin Module

The aggregator scans for a top-level class or object whose name ends with:

- `ComposeKoinModule`
- or `KoinModule`

Recommendation: prefer the explicit `ComposeKoinModule` suffix for new community plugins.

Example:

```kotlin
package site.addzero.kcloud.plugins.example

import org.koin.core.annotation.ComponentScan

@ComponentScan("site.addzero.kcloud.plugins.example")
class ExampleComposeKoinModule
```

### 3. Gradle Wiring

For a Compose facet, keep the existing route/KSP chain so `RouteKeys` and `RouteTable` keep generating correctly.

Typical `ui/build.gradle.kts` or root `build.gradle.kts`:

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

Keep `routeModuleKey` stable. It is part of the generated route ownership contract.

### 4. Optional: Ktorfit API Aggregation

If the plugin defines multiple Ktorfit service interfaces and you want one generated aggregation entry instead of manually assembling factories one by one, you can combine:

- `site.addzero.buildlogic.kmp.kmp-ktorfit`
- `site.addzero.ksp.apiprovider`

The Gradle plugin entry is defined in:

- `/Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/ksp/metadata/apiprovider-gradle-plugin/build.gradle.kts`

Its purpose is straightforward:

- scan Ktorfit HTTP service interfaces
- run the `apiprovider-processor`
- generate a shared aggregation class `site.addzero.generated.api.ApiProvider`

Current generator behavior:

- generated package: `site.addzero.generated.api`
- generated object name: `ApiProvider`
- each detected Ktorfit interface becomes one property on `ApiProvider`
- the property is created through the corresponding generated `ktorfit.createXxxApi()` entry

Typical plugin wiring:

```kotlin
plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.ksp.apiprovider")
}
```

Use this only when the plugin truly has a cluster of related Ktorfit APIs that benefit from one aggregation surface. If there is only one or two APIs, do not introduce another generated layer without need.

### 5. KCloud API Client Wrapper Pattern

Inside `kcloud`, there is another repeated pattern beyond plain `ApiProvider`:

- `object XxxApiClient`
- `configureBaseUrl(...)`
- one shared `HttpClientFactory` profile
- one or more lazily exposed Ktorfit APIs such as `xxxApi`

Typical examples already present in the repo:

- `ServerApiClient`
- `RbacApiClient`
- `AiChatApiClient`
- `KnowledgeBaseApiClient`
- `McuConsoleApiClient`

These wrappers are strong KSP generation candidates because most of the code is mechanical:

- hold `baseUrl`
- pick one `HttpClientFactory` profile
- call `Ktorfit.Builder().baseUrl(...).httpClient(...).build()`
- expose `createXxxApi()` / `create<SomeApi>()` results as properties

For future generator work, treat the wrapper as the real generation target, not the remote service call site.

Good generation targets:

- wrappers whose body is mostly `baseUrl + profile + Ktorfit.createXxxApi()`
- wrappers that only expose API properties and no extra domain policy

Do not force generation when the wrapper also owns real behavior, for example:

- fallback policy
- cross-request caching
- batching
- state recovery
- non-trivial helper methods

Current status of `site.addzero.ksp.apiprovider`:

- it can already generate plain `site.addzero.generated.api.ApiProvider`
- it does **not** yet generate `object XxxApiClient` wrappers
- it does **not** yet cover `configureBaseUrl(...)`, `HttpClientFactory` profile selection, or cached grouped APIs

So if you want to remove hand-written client wrappers in KCloud, the next processor step is:

1. keep scanning Ktorfit interfaces
2. add typed options for wrapper name, base URL, and HTTP client profile
3. generate `XxxApiClient` wrapper objects instead of only a global `ApiProvider`
4. support grouped multi-API wrappers where needed

Use this rule when evaluating whether a class should still be hand-written: if it is only a thin Ktorfit wrapper, prefer making it a future KSP target instead of keeping manual boilerplate.

## Server Plugin Contract

Server aggregation is optional. Only add it when the plugin has backend APIs, storage, or local service behavior.

### 1. Server Koin Module

An explicit server Koin module is optional.

The shell already includes `KCloudServerScanKoinModule`, which does `@ComponentScan("site.addzero")`. That means plain `@Single`, `@Factory`, and similar annotated services inside your plugin can be discovered without adding another wrapper module.

Only add a dedicated server Koin module when you need explicit module composition. If you do, the aggregator scans `server/src/jvmMain/kotlin` for classes whose names end with `ServerKoinModule`.

Example:

```kotlin
package site.addzero.kcloud.plugins.example

import org.koin.core.annotation.ComponentScan

@ComponentScan("site.addzero.kcloud.plugins.example")
class ExampleServerKoinModule
```

### 2. Route Registrar

The aggregator scans `*Routes.kt` files for public top-level functions with the shape:

```kotlin
fun Route.exampleRoutes()
```

Example:

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

### 3. Gradle Wiring

Typical `server/build.gradle.kts`:

```kotlin
plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}
```

Add server-specific dependencies as needed, but do not repeat shell starter modules that already belong to `apps/kcloud/server`.

## How The Shell Picks Up Your Plugin

When a plugin is added under `apps/kcloud/plugins/**` and included by `modules-buddy`, the shell will do the following at build time:

1. `apps/kcloud/shared` depends on the plugin UI compile task so route metadata is ready first.
2. `apps/kcloud/composeApp` adds the discovered UI module as a dependency.
3. `apps/kcloud/composeApp` generates `KCloudComposeKoinApplication` from discovered Compose Koin modules.
4. `apps/kcloud/server` adds discovered `server` facets as dependencies.
5. `apps/kcloud/server` generates `KCloudServerStarterKoinApplication` and `registerKCloudPluginRoutes()` from discovered server entrypoints.

No shell source patching is required anymore.

## Contributor Checklist

- The plugin lives under `apps/kcloud/plugins/**`
- The UI facet has top-level `@Route` `...Screen()` functions
- The UI facet exposes a `*ComposeKoinModule` or `*KoinModule`
- The server facet, if present, exposes a public `fun Route.xxxRoutes()` in a `*Routes.kt` file
- The server facet adds `*ServerKoinModule` only when explicit module composition is needed
- Route KSP arguments point to `:apps:kcloud:shared` and `:apps:kcloud:composeApp`
- The plugin compiles in isolation before asking the shell to aggregate it

## Validation Commands

After adding a plugin, verify the integration with:

```bash
./gradlew :apps:kcloud:shared:compileCommonMainKotlinMetadata
./gradlew :apps:kcloud:composeApp:compileKotlinJvm
./gradlew :apps:kcloud:server:compileKotlinJvm
```

If one of these fails, the plugin is not yet following the shell contract correctly.
