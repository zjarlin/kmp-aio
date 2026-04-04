# Fluent Starter

这个模块用于验证 [compose-fluent-ui](https://github.com/compose-fluent/compose-fluent-ui) 的接入方式，并沉淀一套 Fluent Design 风格的桌面工作台脚手架。

## 当前结论

- GitHub tag 与 Maven Central 当前公开版本一致：`v0.1.0`
- 依赖坐标：
  - `io.github.compose-fluent:fluent:v0.1.0`
  - `io.github.compose-fluent:fluent-icons-extended:v0.1.0`
- 组件入口主要在 `io.github.composefluent.component.*`
- 主题入口是 `FluentTheme`，窗口背景层优先配合 `Mica` / `Layer`

## 当前脚手架包含什么

- `FluentTheme` + `Mica` 作为整体桌面背景
- `NavigationView` 左侧导航壳层
- `CommandBar` 顶部命令工具条
- `InfoBar` 状态提示
- `ContentDialog` 模态确认
- `TextField` / `Switcher` / `Button` / `AccentButton` / `SubtleButton`
- `Layer` / `Card` 作为内容工作区容器

## 运行

```bash
./gradlew :apps:fluent-demo:run
```

## 关键源码

- `src/commonMain/kotlin/site/addzero/fluentdemo/App.kt`
- `src/commonMain/kotlin/site/addzero/fluentdemo/FluentWorkbenchScaffold.kt`
- `src/commonMain/kotlin/site/addzero/fluentdemo/FluentDemoState.kt`
- `src/jvmMain/kotlin/site/addzero/fluentdemo/main.kt`
