# Project Rules

## Gradle Dependency Workflow

- 修改任何 `build.gradle.kts`、`settings.gradle.kts`、`libs.versions.toml` 之前，先检查 `checkouts/build-logic/src/main/kotlin` 里是否已经有对应的 convention plugin。
- 优先复用 `site.addzero.buildlogic.*` 约定插件和当前仓库里的复合构建模块，不要先写裸三方依赖。
- 新增或重构 Gradle convention plugin 时，默认使用 `checkouts/build-logic/src/main/kotlin/**/**.gradle.kts` 这种预编译脚本插件形式，不要再新增 `Plugin<Project>` 这类 class 形式的插件实现；只有脚本插件确实无法表达的场景才例外，并且需要先说明原因。
- 尤其是 `Ktor`、`Koin`、`serialization`、`KSP`、`Jimmer` 这一类依赖，默认先从 `build-logic` 查现成插件；只有确认缺失时，才允许在模块脚本里补原始坐标。
- `apps/coding-playground/build.gradle.kts` 后续新增依赖时，先对照 `checkouts/build-logic` 和 `apps/kcloud/**/build.gradle.kts` 的现成模式，不要重复声明已经由约定插件带入的依赖。

## Library Migration Workflow

- `lib/` 目录里的可复用代码，除了 `coding-playground-demo-*` 这一类 demo 模块，后续默认优先迁移到 `/Users/zjarlin/IdeaProjects/addzero-lib-jvm`，不要长期留在 `kmp-aio` 仓库里继续演化。
- `kmp-aio` 应优先承担 app、集成、样例、验证和业务编排职责；通用组件、工具、client、starter、spi、runtime、processor 等应尽量收敛到 `addzero-lib-jvm`。
- 发版目标默认是中央仓库发布链路，优先复用 `addzero-lib-jvm` 里现有的 `publish-buddy`、`publishToMavenCentral`、README 和模块布局，不要在 `kmp-aio` 里单独再造一套发布体系。
- 在迁移任何 `lib/` 模块之前，先确认它已经去掉 app 专属依赖、仓库内部循环依赖和临时兼容层；不能发布的模块先解耦，再迁移，再发布。

## Naming And Navigation Rules

- 在 `apps/kcloud` 及后续可抽库代码中，命名默认技术优先、能力优先，不要再给可复用壳层、主题、脚手架、路由、设计组件起 `KCloud*` 这类业务前缀名。
- 如果一段代码预期未来进入组件库或共享模块，优先使用 `Workbench*`、`Shell*`、`Route*`、`Sidebar*`、`Theme*`、`Overlay*` 这类泛化命名。
- `nav3/navigation3` 写法默认采用 `NavDisplay(backStack) { key -> when (key) { ... NavEntry(key) { ... } } }` 这种声明式分发风格，不回退到旧的菜单树或额外壳层导航抽象。
