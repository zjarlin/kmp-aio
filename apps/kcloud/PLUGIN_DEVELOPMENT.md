# KCloud Plugin Development

This document is the repo-local source of truth for KCloud plugin structure, route aggregation, generated API flow, and frontend stack rules.

## Scope

- KCloud plugins live under `apps/kcloud/plugins/<plugin-key>/`.
- A real plugin is a triplet:
  - `shared`
  - `server`
  - `ui`
- Use neutral, capability-first package roots under `site.addzero.kcloud.plugins.<feature>`.
- Do not name reusable shells, routes, themes, or workbench helpers with business prefixes when a neutral name is available.

## Triplet Structure

```text
apps/kcloud/plugins/<plugin-key>/
├── shared/
│   └── src/commonMain/kotlin/site/addzero/kcloud/plugins/<feature>/
├── server/
│   ├── src/jvmMain/kotlin/site/addzero/kcloud/plugins/<feature>/
│   └── src/jvmMain/resources/
└── ui/
    └── src/commonMain/kotlin/site/addzero/kcloud/plugins/<feature>/
```

- `shared` owns DTOs, enums, and other frontend-backend contracts.
- `server` owns controllers, services, schema bootstrap, and plugin-local resources.
- `ui` owns routed screens, ViewModels, API wiring, and page-local UI helpers.

## Host Wiring

- `apps/kcloud/ui/build.gradle.kts` must directly depend on every plugin UI module that contributes routes.
- `apps/kcloud/server/build.gradle.kts` must directly depend on every plugin server module that contributes Ktor endpoints.
- Do not assume desktop host aggregation is automatic just because the plugin exists under `plugins/`.
- Koin discovery is root-scanned from `site.addzero`, but Gradle module reachability is still explicit.

Current working example:

- Desktop host depends on `project(":apps:kcloud:plugins:host-config:ui")`.
- Server host depends on `project(":apps:kcloud:plugins:host-config:server")`.

## UI Route Rules

- Plugin page entries must be top-level, no-arg `@Composable` functions.
- Page entry names must end with `Screen`.
- Page entries must live in the plugin `screen` package.
- Every page entry must declare `@Route(...)`.
- Use `order` for stable in-scene ordering.
- Group related routes by `RoutePlacement(scene = RouteScene(...))`.
- Do not add `List<Screen>`, `ScreenTree`, or `class XxxScreen : Screen` menu registries.

Example:

```kotlin
@Route(
    title = "工程配置",
    routePath = "host-config/projects",
    icon = "SettingsApplications",
    order = 0.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "宿主配置",
            icon = "SettingsApplications",
            order = 10,
        ),
        defaultInScene = true,
    ),
)
@Composable
fun ProjectsScreen() { ... }
```

## Route KSP Wiring

KCloud plugin UI modules that contribute routes must:

- apply `site.addzero.buildlogic.kmp.kmp-ksp-plugin`
- depend on `site-addzero-route-core`
- add `kspCommonMainMetadata(site-addzero-route-processor)`
- pass the same route KSP args used by the current host

Required route KSP args:

- `sharedSourceDir`
- `routeGenPkg`
- `routeOwnerModule`
- `routeModuleKey`

Repo-specific current convention:

- `routeGenPkg` is `site.addzero.generated`
- `routeOwnerModule` points to `apps:kcloud:ui` `commonMain` source dir, not `shared-compose` and not a legacy `composeApp`

Generated outputs:

- `apps/kcloud/shared/src/commonMain/kotlin/site/addzero/generated/RouteKeys.kt`
- `apps/kcloud/ui/src/commonMain/kotlin/site/addzero/generated/RouteTable.kt`

The desktop shell must read route metadata from `RouteKeys.allMeta` and page content from `RouteTable`, instead of maintaining a second hand-written navigation tree.

## Server Rules

- Build plugin HTTP features on the KCloud server stack, not by copying an old Spring Boot app shell.
- Controllers are Spring-style annotation sources for `spring2ktor`.
- Services should prefer Koin `@Single`.
- Persistence should prefer Jimmer `KSqlClient`.
- Local persistence defaults to SQLite-first wiring.
- Plugin tables and seeds must be owned by the plugin itself.
- If the plugin needs schema bootstrap, keep it plugin-local and scoped to its own resources.
- Namespace HTTP paths under `/api/<plugin-key>/v1/**` to avoid cross-plugin collisions.
- Split controller sources by feature package, not one flat catch-all routes file.

Current `host-config` example:

- `/api/host-config/v1/projects`
- `/api/host-config/v1/projects/{projectId}/mqtt-config`
- `/api/host-config/v1/projects/{projectId}/modbus-servers/{transportType}`
- `/api/host-config/v1/projects/{projectId}/upload-project`

## Optional: Ktorfit API Aggregation

Use this section when the plugin has a real HTTP API surface that the UI will call.

KCloud plugins should treat server controllers as the API source of truth.

Flow:

1. Define shared DTOs in the plugin `shared` module.
2. Define Spring-style controllers in the plugin `server` module.
3. Let `controller2api` emit Ktorfit interfaces into the plugin `ui` source tree.
4. Let the UI module run Ktorfit KSP against those emitted interfaces.
5. Consume generated APIs from page ViewModels through Koin wiring.

Important repo rules:

- Do not hand-write a parallel set of Ktorfit interfaces for plugin controllers.
- API file names matter because generated client names derive from controller source names.
- Emit generated API interfaces into a real UI source root when the UI module needs Ktorfit KSP to process them.
- `controller2api` 的 `koin` 聚合风格应直接生成稳定入口对象和对应的 Koin module，不要再手写一层逐个 `@Single` 透传绑定。

Current `host-config` example:

- Server writes generated interfaces and Koin aggregation artifacts into `apps/kcloud/plugins/host-config/ui/src/commonMain/kotlin/site/addzero/kcloud/plugins/hostconfig/api/external/`
- Generated outputs include `Apis.kt` and `ApisModule.kt`

## KCloud Frontend Stack

KCloud plugin/frontend Compose work defaults to Cupertino-first UI.

Use this stack first:

- `io.github.robinpcrd:cupertino`
- `io.github.robinpcrd:cupertino-icons-extended`
- `site.addzero:compose-cupertino-workbench`

Reference:

- Official upstream library: [compose-cupertino](https://github.com/RobinPcrd/compose-cupertino?tab=readme-ov-file)

Rules:

- Prefer existing `compose-cupertino-workbench` wrappers for shell actions, sidebars, and workbench layout.
- Prefer upstream Cupertino primitives for page-local content, dialogs, sheets, segmented controls, and text styling.
- Keep KCloud plugin pages visually Cupertino-first even when Material icons or small interop helpers are used under the hood.
- Do not default new KCloud plugin pages to Material-first scaffolds.
- Do not default new KCloud plugin pages to shadcn-style or addzero-native-only page kits.
- Do not recreate old Vue or Element Plus console structure as a Compose mental model.

State conventions:

- Page roots use `@Composable fun XxxScreen()`.
- Page-level state holders use `@KoinViewModel class XxxViewModel : ViewModel()`.
- Screen state models use `XxxScreenState`.

## Validation Checklist

- [ ] Plugin is a `shared` + `server` + `ui` triplet
- [ ] Host `ui` and `server` modules both depend on the new plugin modules explicitly
- [ ] UI page entries are top-level no-arg `*Screen` functions with `@Route`
- [ ] Route KSP args match current KCloud owner/shared settings
- [ ] `RouteKeys` and `RouteTable` regenerate successfully
- [ ] Controllers are the source for generated client APIs
- [ ] UI consumes generated APIs instead of hardcoded localhost or duplicate interfaces
- [ ] Plugin HTTP paths stay under `/api/<plugin-key>/v1/**`
- [ ] KCloud plugin pages use `compose-cupertino` and `compose-cupertino-workbench` as the primary component system
