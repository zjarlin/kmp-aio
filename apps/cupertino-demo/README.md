# Cupertino Starter

这个模块用于验证 [compose-cupertino](https://github.com/RobinPcrd/compose-cupertino) 的接入方式，并沉淀一套不依赖 Material 3 作为主界面的桌面脚手架。

## 当前结论

- Maven Central 当前可用版本：`3.3.1`
- 依赖坐标：
  - `io.github.robinpcrd:cupertino`
  - `io.github.robinpcrd:cupertino-adaptive`
  - `io.github.robinpcrd:cupertino-icons-extended`
- 包名前缀已经从旧 fork 迁移为 `io.github.robinpcrd.cupertino.*`
- README 主要给坐标，真实可参考的组件用法集中在上游 `example/composeApp`

## 当前脚手架包含什么

- `CupertinoTheme` / `CupertinoScaffold` / `CupertinoTopAppBar`
- 左侧导航栏、顶部动作区、内容工作区、底部状态区
- `CupertinoBorderedTextField` / `CupertinoSearchTextField`
- `CupertinoSwitch` / `CupertinoSegmentedControl` / `CupertinoSlider`
- `CupertinoButton` / `CupertinoIconButton` / `CupertinoIconToggleButton`
- `AdaptiveTheme(target = Theme.Cupertino)`
- `AdaptiveButton` / `AdaptiveTextButton` / `AdaptiveTonalButton`
- `AdaptiveSwitch` / `AdaptiveCheckbox` / `AdaptiveTriStateCheckbox`
- `AdaptiveSlider` / `AdaptiveIconButton` / `AdaptiveIconToggleButton`

## 运行

```bash
./gradlew :apps:cupertino-demo:run
```

## 关键源码

- `src/commonMain/kotlin/site/addzero/cupertinodemo/App.kt`
- `src/commonMain/kotlin/site/addzero/cupertinodemo/CupertinoWorkbenchScaffold.kt`
- `src/commonMain/kotlin/site/addzero/cupertinodemo/CupertinoDemoState.kt`
- `src/jvmMain/kotlin/site/addzero/cupertinodemo/main.kt`
