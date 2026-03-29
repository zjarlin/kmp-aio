# Kmp-Aio Library Migration Plan

This file tracks which `kmp-aio/lib` modules should move into `/Users/zjarlin/IdeaProjects/addzero-lib-jvm`.

Scope rule:

- Exclude demo modules such as `coding-playground-demo-*`
- Prefer `addzero-lib-jvm` as the final source of truth
- Prefer central publication from `addzero-lib-jvm`, not from `kmp-aio`

## Wave 1

These are the best first migration candidates because they are already library-shaped and have low app coupling.

### Config Center Suite

- `lib/config-center/spec`
- `lib/config-center/client`
- `lib/config-center/runtime-jvm`
- `lib/config-center/ktor`

Why:

- Package names are already generic: `site.addzero.configcenter.*`
- Internal dependency graph is clean and suite-like
- No direct dependency on `apps/*`
- Easy fit for a published library family in `addzero-lib-jvm`

Expected target shape:

- `addzero-lib-jvm/lib/config-center/spec`
- `addzero-lib-jvm/lib/config-center/client`
- `addzero-lib-jvm/lib/config-center/runtime-jvm`
- `addzero-lib-jvm/lib/config-center/ktor`

### Workbench UI Base

- `lib/compose/app-sidebar`
- `lib/compose/scaffold-spi`

Why:

- Reusable Compose UI and shell SPI
- No app-owned package imports
- `scaffold-spi` only depends on `app-sidebar`
- Good candidate for a reusable workbench shell layer

Expected target shape:

- either keep as a small `compose/workbench-*` family
- or merge into a clearer `addzero-lib-jvm` compose shell namespace

### System Spec

- `lib/spec/system-spec`

Why:

- No project dependencies
- Generic package namespace: `site.addzero.system.*`
- Pure contract/spec style module

Expected target shape:

- `addzero-lib-jvm/lib/biz/system-spec`
- or another spec-focused namespace in `addzero-lib-jvm`

### Media Playlist Player

- `lib/compose/media-playlist-player`

Why:

- Standalone KMP UI/runtime module
- No app-owned project dependency
- Public API surface is already component-like

Risk:

- Needs publication policy for platform-specific JVM media dependencies

## Wave 2

These should move next, but only after light cleanup or naming normalization.

### Music API Family

- `lib/api/api-music-spi`
- `lib/api/api-qqmusic`
- `lib/api/api-netease`
- `lib/api/api-suno`

Why not Wave 1:

- Package names are inconsistent
- Several packages still carry `site.addzero.kcloud.*`
- `api-suno` also contains `site.addzero.kcloud.music.suno.*`
- Better to normalize naming before publishing

Recommended cleanup:

- Remove `kcloud` branding from package names where the module is actually generic
- Decide whether these belong under `api/*`, `tool-jvm/network-call`, or another API family in `addzero-lib-jvm`
- Reconnect shared music abstractions to one stable SPI module

### Glass / Visual Component Family

- `lib/compose/glass-components`
- `lib/compose/liquid-glass`

Why not Wave 1:

- They are reusable, but `addzero-lib-jvm` already has a large Compose component tree
- Need to avoid duplicate product lines against:
  - `lib/compose/compose-native-component-glass`
  - other existing Compose component modules

Recommended cleanup:

- Decide whether to merge into an existing glass component family
- Avoid publishing parallel modules with overlapping responsibility

### Kbox Family

- `lib/kbox-core`
- `lib/kbox-plugin-api`
- `lib/kbox-plugin-runtime`
- `lib/kbox-ssh`

Why not Wave 1:

- Reusable enough, but still product-branded
- `kbox-plugin-runtime` test wiring depends on `apps/kbox/runtime-fixtures`
- Module boundary is publishable, but test and naming cleanup should happen first

Recommended cleanup:

- Remove app-fixture assumptions from published module tests
- Confirm whether `kbox` remains a product namespace or should be generalized before publication

## Wave 3

These are not good first migrations. They need stronger decoupling or a clearer ownership decision.

### Kcloud Core Family

- `lib/kcloud-core`
- `lib/kcloud-paths`

Why blocked:

- Strong `kcloud` product identity in package names and semantics
- `kcloud-core` currently hardcodes raw dependency versions in the module script
- More likely to be app-core or product-core than a broadly reusable public library

Required before migration:

- Remove hardcoded versions
- Decide whether this remains product-core in `kmp-aio` or becomes a real external library
- Split generic infra from `kcloud` business semantics if publication is still desired

### OpenAPI Codegen

- `lib/openapi-codegen`

Why blocked:

- Logic is reusable, but package namespace is still `site.addzero.kcloud.codegen`
- It is a processor/codegen module and should align with the existing `addzero-lib-jvm/lib/ksp/*` structure

Required before migration:

- Re-home under `addzero-lib-jvm/lib/ksp/...`
- Rename package and artifact around generic OpenAPI/KSP concerns

## Do Not Migrate

- `lib/coding-playground-demo-alpha`
- `lib/coding-playground-demo-beta`
- `lib/coding-playground-demo-gamma`

These remain demos unless explicitly reclassified.

## Next Execution Order

1. Migrate `config-center/spec`
2. Migrate `config-center/client`
3. Migrate `config-center/runtime-jvm`
4. Migrate `config-center/ktor`
5. Migrate `compose/app-sidebar`
6. Migrate `compose/scaffold-spi`
7. Migrate `spec/system-spec`

## Publish Gate

Before a module is considered ready for central publication from `addzero-lib-jvm`, verify:

- No `apps/*` dependency
- No repo-local test fixture dependency
- No compatibility shim kept only for old `kmp-aio` call sites
- README exists
- Artifact naming matches the long-term `addzero-lib-jvm` taxonomy
- `publishToMavenCentral` is available for the migrated module
