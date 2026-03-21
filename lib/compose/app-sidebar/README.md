# app-sidebar

低心智负担的 Compose Multiplatform 应用侧边栏组件库，默认内建树结构、搜索、插槽和暗色专业风格。

- Maven coordinate: `site.addzero:app-sidebar`
- Local module path: `lib/compose/app-sidebar`

## Features

- 树形导航
- 内建搜索过滤
- 自动排序
- 头部 / 底部插槽
- 默认暗色专业风格
- `commonMain` 可复用

## Usage

```kotlin
val sidebarState = rememberAppSidebarState(initialSelectedId = "dashboard")

WorkbenchScaffold(
    defaultSidebarRatio = 0.22f,
    outerPadding = PaddingValues(16.dp),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    detailPadding = PaddingValues(16.dp),
    sidebar = {
        AppSidebar(
            title = "Workbench",
            supportText = "一个拿来就能用的通用应用侧栏。",
            items = listOf(
                AppSidebarItem(
                    id = "dashboard",
                    title = "仪表盘",
                    icon = Icons.Rounded.SpaceDashboard,
                    order = 0,
                ),
                AppSidebarItem(
                    id = "settings",
                    title = "设置",
                    icon = Icons.Rounded.Settings,
                    order = 100,
                ),
            ),
            state = sidebarState,
        )
    },
    contentHeader = {
        WorkbenchToolbar()
    },
    content = {
        WorkbenchContent()
    },
    detail = {
        InspectorPanel()
    },
)
```

## Admin Usage

```kotlin
AdminWorkbenchScaffold(
    breadcrumb = listOf("系统管理", "用户中心"),
    pageTitle = "成员管理",
    pageSubtitle = "把页面动作和全局工具动作收进统一后台骨架。",
    sidebar = {
        AppSidebar(
            title = "Admin",
            supportText = "后台工作台",
            items = adminItems,
            state = sidebarState,
        )
    },
    pageActions = {
        FilterChip()
        PrimaryActionButton()
    },
    content = {
        MemberTable()
    },
    detail = {
        MemberInspector()
    },
    onGlobalSearchClick = onGlobalSearchClick,
    languageLabel = currentLanguageLabel,
    onLanguageClick = onLanguageClick,
    isDarkTheme = isDarkTheme,
    onThemeToggle = onThemeToggle,
    notificationCount = notificationCount,
    onNotificationsClick = onNotificationsClick,
    userLabel = currentUserLabel,
    onUserClick = onUserClick,
)
```

## Preview

- 独立预览宿主：`/Users/zjarlin/IdeaProjects/vibepocket/apps/liquiddemo`
- 本地运行：`./gradlew :apps:liquiddemo:jvmRun`

## Notes

- 默认视觉参数已经写死成更适合商用的暗色桌面风格
- `AppSidebarScaffold` / `WorkbenchScaffold` 默认都是无缝布局
- `WorkbenchScaffold` 适合“侧栏 + 顶部工具栏 + 主内容 + 右侧详情栏”
- `AdminWorkbenchScaffold` 是后台管理版高阶封装，默认头部固定为“面包屑 / 标题 / 副标题 + 页面动作 + 全局工具动作”
- 后台工具动作默认顺序固定为“搜索 / 语言 / 主题 / 通知 / 用户”，未提供的参数不会渲染
- `WorkbenchScaffold` 会按宽度自适应：宽窗口三栏，窄一些自动折叠成双栏
- `rememberAppSidebarState` / `rememberWorkbenchScaffoldState` 都基于 Compose `rememberSaveable`
- 左侧栏默认支持拖拽调宽，同时保留默认比例
- 如果业务需要受控选中态，直接读写 `AppSidebarState.selectedId`
