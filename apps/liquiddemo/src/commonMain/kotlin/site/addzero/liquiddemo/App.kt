package site.addzero.liquiddemo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import site.addzero.appsidebar.AdminWorkbenchScaffold
import site.addzero.appsidebar.AppSidebar
import site.addzero.appsidebar.WorkbenchScaffold
import site.addzero.appsidebar.findItemById
import site.addzero.appsidebar.rememberAppSidebarState
import site.addzero.liquiddemo.demos.DemoTokens
import site.addzero.liquiddemo.demos.adminBackendSidebarScene
import site.addzero.liquiddemo.demos.musicStudioSidebarScene
import site.addzero.liquiddemo.demos.projectWorkbenchSidebarScene
import site.addzero.liquiddemo.demos.settingsConsoleSidebarScene

@Composable
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF060B14),
        ) {
            Box(
                modifier = Modifier.fillMaxSize().demoBackdrop(),
            ) {
                SidebarShowcaseShell(
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun SidebarShowcaseShell(
    modifier: Modifier = Modifier,
) {
    val scenes = remember {
        listOf(
            adminBackendSidebarScene(),
            musicStudioSidebarScene(),
            projectWorkbenchSidebarScene(),
            settingsConsoleSidebarScene(),
        )
    }
    var selectedSceneId by rememberSaveable { mutableStateOf(scenes.first().id) }
    val selectedScene = scenes.first { scene -> scene.id == selectedSceneId }
    val sidebarState = rememberAppSidebarState()
    var utilityLanguageLabel by rememberSaveable { mutableStateOf("中文") }
    var utilityDarkTheme by rememberSaveable { mutableStateOf(true) }
    var utilityNotificationCount by rememberSaveable { mutableIntStateOf(3) }
    var utilityLastAction by rememberSaveable { mutableStateOf("还没有触发任何全局动作。") }

    LaunchedEffect(selectedScene.id) {
        sidebarState.clearSearch()
        sidebarState.resetExpandedState(selectedScene.items)
        sidebarState.updateSelectedId(selectedScene.initialSelectedId)
    }

    val selectedItem = remember(selectedScene.items, sidebarState.selectedId) {
        selectedScene.items.findItemById(sidebarState.selectedId)
            ?: selectedScene.items.findItemById(selectedScene.initialSelectedId)
    }

    if (selectedScene.shell == SidebarDemoShell.AdminWorkbench) {
        AdminWorkbenchShowcase(
            modifier = modifier,
            scenes = scenes,
            selectedSceneId = selectedSceneId,
            onSceneSelected = { selectedSceneId = it },
            selectedScene = selectedScene,
            selectedItem = selectedItem,
            sidebarState = sidebarState,
            utilityLanguageLabel = utilityLanguageLabel,
            utilityDarkTheme = utilityDarkTheme,
            utilityNotificationCount = utilityNotificationCount,
            utilityLastAction = utilityLastAction,
            onGlobalSearch = {
                utilityLastAction = "打开了全局搜索入口。"
            },
            onLanguageToggle = {
                utilityLanguageLabel = if (utilityLanguageLabel == "中文") "EN" else "中文"
                utilityLastAction = "切换语言为 $utilityLanguageLabel。"
            },
            onThemeToggle = {
                utilityDarkTheme = !utilityDarkTheme
                utilityLastAction = if (utilityDarkTheme) {
                    "切换到深色主题。"
                } else {
                    "切换到浅色主题。"
                }
            },
            onNotificationsClick = {
                utilityNotificationCount = if (utilityNotificationCount >= 9) 1 else utilityNotificationCount + 1
                utilityLastAction = "查看通知中心，当前角标 ${utilityNotificationCount}。"
            },
            onUserClick = {
                utilityLastAction = "打开了用户菜单。"
            },
        )
    } else {
        GenericWorkbenchShowcase(
            modifier = modifier,
            scenes = scenes,
            selectedSceneId = selectedSceneId,
            onSceneSelected = { selectedSceneId = it },
            selectedScene = selectedScene,
            selectedItem = selectedItem,
            sidebarState = sidebarState,
        )
    }
}

@Composable
private fun GenericWorkbenchShowcase(
    scenes: List<SidebarDemoScene>,
    selectedSceneId: String,
    onSceneSelected: (String) -> Unit,
    selectedScene: SidebarDemoScene,
    selectedItem: site.addzero.appsidebar.AppSidebarItem?,
    sidebarState: site.addzero.appsidebar.AppSidebarState,
    modifier: Modifier = Modifier,
) {
    WorkbenchScaffold(
        modifier = modifier.fillMaxSize(),
        defaultSidebarRatio = 0.24f,
        minSidebarWidth = 268.dp,
        maxSidebarWidth = 380.dp,
        outerPadding = PaddingValues(22.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
        detailPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        sidebar = {
            AppSidebar(
                title = selectedScene.title,
                supportText = selectedScene.subtitle,
                items = selectedScene.items,
                state = sidebarState,
                headerSlot = selectedScene.headerSlot,
                footerSlot = selectedScene.footerSlot,
            )
        },
        contentHeader = {
            DemoSceneSwitcher(
                scenes = scenes,
                selectedSceneId = selectedSceneId,
                onSceneSelected = onSceneSelected,
            )
            Spacer(modifier = Modifier.weight(1f))
            HeaderActionChip(
                title = "搜索",
                icon = Icons.Rounded.Search,
            )
            HeaderActionChip(
                title = "筛选",
                icon = Icons.Rounded.Tune,
            )
            HeaderActionChip(
                title = "更多",
                icon = Icons.Rounded.MoreHoriz,
            )
        },
        content = {
            DemoMainPanel(
                selectedScene = selectedScene,
                selectedItem = selectedItem,
            )
        },
        detail = {
            DemoInspectorPanel(
                selectedScene = selectedScene,
                selectedItem = selectedItem,
            )
        },
    )
}

@Composable
private fun AdminWorkbenchShowcase(
    scenes: List<SidebarDemoScene>,
    selectedSceneId: String,
    onSceneSelected: (String) -> Unit,
    selectedScene: SidebarDemoScene,
    selectedItem: site.addzero.appsidebar.AppSidebarItem?,
    sidebarState: site.addzero.appsidebar.AppSidebarState,
    utilityLanguageLabel: String,
    utilityDarkTheme: Boolean,
    utilityNotificationCount: Int,
    utilityLastAction: String,
    onGlobalSearch: () -> Unit,
    onLanguageToggle: () -> Unit,
    onThemeToggle: () -> Unit,
    onNotificationsClick: () -> Unit,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AdminWorkbenchScaffold(
        modifier = modifier.fillMaxSize(),
        breadcrumb = listOf("运营后台", selectedScene.title),
        pageTitle = selectedItem?.title ?: selectedScene.title,
        pageSubtitle = "后台管理版默认把页面级动作和全局壳层动作分开，避免每个页面都重新拼头部。",
        defaultSidebarRatio = 0.24f,
        minSidebarWidth = 268.dp,
        maxSidebarWidth = 380.dp,
        outerPadding = PaddingValues(22.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
        detailPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        sidebar = {
            AppSidebar(
                title = selectedScene.title,
                supportText = selectedScene.subtitle,
                items = selectedScene.items,
                state = sidebarState,
                headerSlot = selectedScene.headerSlot,
                footerSlot = selectedScene.footerSlot,
            )
        },
        pageActions = {
            DemoSceneSwitcher(
                scenes = scenes,
                selectedSceneId = selectedSceneId,
                onSceneSelected = onSceneSelected,
            )
        },
        content = {
            AdminDemoMainPanel(
                selectedScene = selectedScene,
                selectedItem = selectedItem,
            )
        },
        detail = {
            AdminDemoInspectorPanel(
                selectedScene = selectedScene,
                selectedItem = selectedItem,
                utilityLanguageLabel = utilityLanguageLabel,
                utilityDarkTheme = utilityDarkTheme,
                utilityNotificationCount = utilityNotificationCount,
                utilityLastAction = utilityLastAction,
            )
        },
        onGlobalSearchClick = onGlobalSearch,
        languageLabel = utilityLanguageLabel,
        onLanguageClick = onLanguageToggle,
        isDarkTheme = utilityDarkTheme,
        onThemeToggle = onThemeToggle,
        notificationCount = utilityNotificationCount,
        onNotificationsClick = onNotificationsClick,
        userLabel = "zjarlin",
        onUserClick = onUserClick,
    )
}

@Composable
private fun DemoMainPanel(
    selectedScene: SidebarDemoScene,
    selectedItem: site.addzero.appsidebar.AppSidebarItem?,
) {
    Column(
        modifier = Modifier.fillMaxSize().workbenchPanelFrame().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        DemoDetailPanel(
            selectedScene = selectedScene,
            selectedItem = selectedItem,
        )
    }
}

@Composable
private fun DemoDetailPanel(
    selectedScene: SidebarDemoScene,
    selectedItem: site.addzero.appsidebar.AppSidebarItem?,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = selectedItem?.title ?: selectedScene.title,
            color = DemoTokens.textPrimary,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = "这里展示的是“应用壳双栏布局 + 可拖拽侧栏 + 默认暗色工作台”的组合。调用方只要给数据和内容，不用重复发明骨架。",
            color = DemoTokens.textSecondary,
            style = MaterialTheme.typography.bodyLarge,
        )

        Column(
            modifier = Modifier.detailSectionFrame(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = {
                selectedScene.detail.invoke(this, selectedItem)
            },
        )
    }
}

@Composable
private fun AdminDemoMainPanel(
    selectedScene: SidebarDemoScene,
    selectedItem: site.addzero.appsidebar.AppSidebarItem?,
) {
    Column(
        modifier = Modifier.fillMaxSize().workbenchPanelFrame().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        DemoDetailPanel(
            selectedScene = selectedScene,
            selectedItem = selectedItem,
        )
        Column(
            modifier = Modifier.detailSectionFrame(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "后台页头默认元素",
                color = DemoTokens.textPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "左侧是 breadcrumb / 标题 / 副标题，中间是页面级动作，右侧是搜索、语言、主题、通知和用户入口。",
                color = DemoTokens.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "右侧 Inspector 在中等宽度下会自动隐藏，优先保证主工作区宽度。",
                color = DemoTokens.textMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun DemoInspectorPanel(
    selectedScene: SidebarDemoScene,
    selectedItem: site.addzero.appsidebar.AppSidebarItem?,
) {
    Column(
        modifier = Modifier.fillMaxSize().workbenchPanelFrame(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Inspector",
            color = DemoTokens.textPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = "当前场景",
            color = DemoTokens.textMuted,
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = selectedScene.title,
            color = DemoTokens.textPrimary,
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "当前节点",
            color = DemoTokens.textMuted,
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = selectedItem?.title ?: "未选择",
            color = DemoTokens.textPrimary,
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "这块对应常见三栏工作台里的右侧详情区，适合放属性、状态、快捷操作。",
            color = DemoTokens.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "窗口变窄时，这一栏会自动折叠，让主工作区优先保住可用宽度。",
            color = DemoTokens.textMuted,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun AdminDemoInspectorPanel(
    selectedScene: SidebarDemoScene,
    selectedItem: site.addzero.appsidebar.AppSidebarItem?,
    utilityLanguageLabel: String,
    utilityDarkTheme: Boolean,
    utilityNotificationCount: Int,
    utilityLastAction: String,
) {
    Column(
        modifier = Modifier.fillMaxSize().workbenchPanelFrame(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Inspector",
            color = DemoTokens.textPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
        )
        DemoInspectorField(
            label = "当前场景",
            value = selectedScene.title,
        )
        DemoInspectorField(
            label = "当前节点",
            value = selectedItem?.title ?: "未选择",
        )
        DemoInspectorField(
            label = "语言按钮",
            value = utilityLanguageLabel,
        )
        DemoInspectorField(
            label = "主题按钮",
            value = if (utilityDarkTheme) "深色" else "浅色",
        )
        DemoInspectorField(
            label = "通知角标",
            value = utilityNotificationCount.toString(),
        )
        DemoInspectorField(
            label = "最近动作",
            value = utilityLastAction,
        )
    }
}

@Composable
private fun DemoInspectorField(
    label: String,
    value: String,
) {
    Text(
        text = label,
        color = DemoTokens.textMuted,
        style = MaterialTheme.typography.labelLarge,
    )
    Text(
        text = value,
        color = DemoTokens.textPrimary,
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun RowScope.DemoSceneSwitcher(
    scenes: List<SidebarDemoScene>,
    selectedSceneId: String,
    onSceneSelected: (String) -> Unit,
) {
    scenes.forEach { scene ->
        SceneSwitchButton(
            title = scene.title,
            selected = scene.id == selectedSceneId,
            onClick = {
                onSceneSelected(scene.id)
            },
        )
    }
}

@Composable
private fun SceneSwitchButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                Color(0xFF2D64E3).copy(alpha = 0.88f)
            } else {
                Color.White.copy(alpha = 0.08f)
            },
            contentColor = Color.White.copy(alpha = 0.96f),
        ),
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(text = title)
    }
}

@Composable
private fun HeaderActionChip(
    title: String,
    icon: ImageVector,
) {
    Button(
        onClick = {},
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.06f),
            contentColor = Color.White.copy(alpha = 0.92f),
        ),
        shape = RoundedCornerShape(999.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
            )
            Text(text = title)
        }
    }
}

/** Demo 主背景：深蓝压底，再叠少量青紫光斑，保证侧栏和内容面板更稳定。 */
private fun Modifier.demoBackdrop(): Modifier {
    return background(
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF050A14),
                Color(0xFF0A1730),
                Color(0xFF11102B),
            ),
        ),
    )
}

/** 工作台内容底板：让主面板和右侧 inspector 共用同一套深色材质。 */
private fun Modifier.workbenchPanelFrame(): Modifier {
    return background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0C1525).copy(alpha = 0.94f),
                Color(0xFF0B1120).copy(alpha = 0.98f),
            ),
        ),
    ).padding(horizontal = 22.dp, vertical = 20.dp)
}

/** 详情区段底板：把说明和示例内容压进同一块高阶内容卡片。 */
private fun Modifier.detailSectionFrame(): Modifier {
    return background(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(24.dp),
    ).padding(horizontal = 18.dp, vertical = 16.dp)
}
