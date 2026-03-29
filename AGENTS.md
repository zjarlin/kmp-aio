# Project Rules

## Gradle Dependency Workflow

- 修改任何 `build.gradle.kts`、`settings.gradle.kts`、`libs.versions.toml` 之前，先检查 `checkouts/build-logic/src/main/kotlin` 里是否已经有对应的 convention plugin。
- 优先复用 `site.addzero.buildlogic.*` 约定插件和当前仓库里的复合构建模块，不要先写裸三方依赖。
- 新增或重构 Gradle convention plugin 时，默认使用 `checkouts/build-logic/src/main/kotlin/**/**.gradle.kts` 这种预编译脚本插件形式，不要再新增 `Plugin<Project>` 这类 class 形式的插件实现；只有脚本插件确实无法表达的场景才例外，并且需要先说明原因。
- 尤其是 `Ktor`、`Koin`、`serialization`、`KSP`、`Jimmer` 这一类依赖，默认先从 `build-logic` 查现成插件；只有确认缺失时，才允许在模块脚本里补原始坐标。
- `apps/coding-playground/build.gradle.kts` 后续新增依赖时，先对照 `checkouts/build-logic` 和 `apps/kcloud/**/build.gradle.kts` 的现成模式，不要重复声明已经由约定插件带入的依赖。
