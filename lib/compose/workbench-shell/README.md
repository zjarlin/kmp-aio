# workbench-shell

共享的 Compose Multiplatform 工作台导航契约，提供 `Screen` 树元数据、树校验、只读导航聚合和到 `AppSidebarItem` 的适配。

- Maven coordinate: `site.addzero:workbench-shell`
- Local module path: `lib/compose/workbench-shell`

## Usage

```kotlin
val catalog = ScreenCatalog(
    listOf(
        DemoRootScreen(),
        DemoLeafScreen(),
    ),
)

val sidebarItems = catalog.toAppSidebarItems()
val defaultLeafId = catalog.defaultLeafId
```

## Notes

- `id` / `pid` 当前使用显式 `String`
- `content == null` 表示父节点容器
- `content != null` 表示叶子页面
- 树节点按同父节点下的 `sort`，再按 `name` 兜底排序
- 这个模块会 `api` 导出 `app-sidebar`
