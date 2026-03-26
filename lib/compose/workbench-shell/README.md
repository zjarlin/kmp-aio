# workbench-shell

共享的 Compose Multiplatform 工作台导航契约，提供 `Screen` 树元数据、树校验、只读导航聚合，以及把 `ScreenNode` 直接渲染进共享侧边栏的能力。

- Maven coordinate: `site.addzero:workbench-shell`
- Local module path: `lib/compose/workbench-shell`

## Usage

```kotlin
val tree = ScreenTree(
    listOf(
        DemoRootScreen(),
        DemoLeafScreen(),
    ),
)

val defaultLeafId = tree.defaultLeafId
val selectedId = defaultLeafId

ScreenSidebar(
    title = "Workbench",
    items = tree.roots,
    selectedId = selectedId,
    onLeafClick = { node ->
        navigateTo(node.id)
    },
)
```

## Notes

- `id` / `pid` 当前使用显式 `String`
- `content == null` 表示父节点容器
- `content != null` 表示叶子页面
- 树节点按同父节点下的 `sort`，再按 `name` 兜底排序
- 默认直接传 `ScreenNode` 给 `ScreenSidebar` / `AppSidebar`，不要再做额外 `SidebarItem` 包装层
- 这个模块会 `api` 导出 `app-sidebar`
