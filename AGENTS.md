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

## Addzero-lib Root Build Guard

- 严禁改动 `/Users/zjarlin/IdeaProjects/addzero-lib-jvm/settings.gradle.kts`。
- 严禁改动 `/Users/zjarlin/IdeaProjects/addzero-lib-jvm/build.gradle.kts`。
- 如果任务看起来需要改这两个根构建文件，必须先停下并说明原因，优先寻找模块级、build-logic、复合构建或其他替代方案；未经用户明确重新授权，不得自行修改。

## Naming And Navigation Rules

- 在 `apps/kcloud` 及后续可抽库代码中，命名默认技术优先、能力优先，不要再给可复用壳层、主题、脚手架、路由、设计组件起 `KCloud*` 这类业务前缀名。
- 如果一段代码预期未来进入组件库或共享模块，优先使用 `Workbench*`、`Shell*`、`Route*`、`Sidebar*`、`Theme*`、`Overlay*` 这类泛化命名。
- `nav3/navigation3` 写法默认采用 `NavDisplay(backStack) { key -> when (key) { ... NavEntry(key) { ... } } }` 这种声明式分发风格，不回退到旧的菜单树或额外壳层导航抽象。

## CMP Screen SPI Slot Rules

- 这条规则视为本仓库内的 `cmp skill` 补充约定：Compose 前端里凡是直接承接用户动作的人机交互点，默认都要抽成页面级 `SPI` 插槽接口，并提供 `Koin` 可替换默认实现。
- 适用范围包括按钮点击、图标按钮、工具栏动作、搜索入口、提交或重置、行内操作、下拉菜单、弹窗触发、确认或取消、空态 CTA、切换与选择回调等；纯展示文本、纯布局容器、静态装饰、单纯排版 helper 不在此列。
- 页面主布局、`Screen` 骨架、卡片或行列组合函数不要为了 SPI 机械抽离；布局继续留在 `Screen` 文件里，SPI 只负责可替换的人机交互块。
- 每个页面建立独立包，例如 `user_screen`、`dept_screen`、`adaptive_page`；该包下集中放本页交互 SPI，不要把多个页面的交互点混在同一个通用包里。
- 一个页面如果有多个交互点，按交互区域或动作语义拆成多个小 SPI，例如 `UserScreenSearchButtonSpi`、`UserScreenCreateActionSpi`、`DeptScreenRowActionsSpi`；不要把整页交互糊成一个巨型 `InteractionSpi`。
- SPI 接口默认写成 `interface XxxSpi { @Composable fun Render(state: XxxState, ...) }`；入参只保留渲染该交互块所需的最小上下文，优先传页面 state holder 或明确的 state 或 action 切片。
- 默认实现使用 `@org.koin.core.annotation.Single`，页面内通过 `koinInject<XxxSpi>().Render(...)` 挂载。
- 每个 SPI 接口和默认实现都必须写中文 KDoc，至少说明交互入口位于哪里、默认行为是什么、为什么要做成 slot、允许替换的边界是什么。
- 优先抽按钮区、行尾操作区、工具栏操作、弹窗触发器、筛选入口这类局部交互槽位；不要把稳定不变的布局容器、纯展示标题、静态说明文案一起 SPI 化。
- 当前仓库可参考 `apps/cupertino-demo/src/commonMain/kotlin/site/addzero/cupertinodemo/adaptive_page/AdaptivePageAdaptiveIconButtonSpi.kt` 作为页面级交互 SPI 模板。
