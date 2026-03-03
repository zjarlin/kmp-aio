package site.addzero.vibepocket

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.shadcn.ui.components.sidebar.LocalSidebarState
import com.shadcn.ui.components.sidebar.SidebarLayout
import com.shadcn.ui.components.sidebar.SidebarProvider
import com.shadcn.ui.components.sidebar.SidebarTrigger
import com.shadcn.ui.themes.styles
import site.addzero.component.glass.GlassSidebar
import site.addzero.component.glass.LiquidGlassCard
import site.addzero.vibepocket.music.ioc.generated.iocComposablesByTag
import site.addzero.vibepocket.navigation.MenuNode
import site.addzero.vibepocket.navigation.MenuTreeBuilder
import site.addzero.vibepocket.navigation.RouteKey
import site.addzero.vibepocket.navigation.defaultMenuItems
import site.addzero.vibepocket.screens.PlaceholderScreen
import site.addzero.vibepocket.screens.WelcomeScreenWrapper

private val WELCOME_ROUTE = RouteKey("site.addzero.vibepocket.auth.WelcomePage")

@Composable
@Preview
fun App() {
    compoaseApp()

}

@Composable
private fun compoaseApp() {
    val menuTree = remember { MenuTreeBuilder.buildTree(defaultMenuItems) }
    val visibleLeaves = remember { MenuTreeBuilder.flattenVisibleLeaves(menuTree) }
    val homeRoute = RouteKey(visibleLeaves.firstOrNull()?.routeKey ?: "")

    var isSetupDone by remember { mutableStateOf(false) }
    val backStack = remember { mutableStateListOf(WELCOME_ROUTE) }

    // API 配置（欢迎页填写后传入，后续会改成从 DB 读取）

    MaterialTheme {
        // A surface container using the 'background' color from the theme
        SidebarProvider {
            if (!isSetupDone) {
                WelcomeScreenWrapper(
                    backStack = backStack,
                    homeRoute = homeRoute,
                    onSetupComplete = { token, url ->
                        isSetupDone = true
                    }
                )
            } else {
                // 主界面：侧边栏 + NavDisplay
                MainScreen(menuTree, backStack)
            }
        }

    }
}

@Composable
private fun MainScreen(
    menuTree: List<MenuNode>,
    backStack: SnapshotStateList<RouteKey>
) {
    val sidebarState = LocalSidebarState.current
    SidebarLayout(
        modifier = Modifier.fillMaxSize(),
        sidebarContent = {
            GlassSidebar(
                roots = menuTree,
                nodeKey = { it.metadata.routeKey },
                nodeTitle = { it.metadata.menuNameAlias },
                nodeIcon = { it.metadata.icon },
                nodeChildren = { it.children },
                nodeVisible = { it.metadata.visible },
                selectedKey = backStack.lastOrNull()?.key ?: "",
                onLeafClick = { leaf ->
                    backStack.clear()
                    backStack.add(RouteKey(leaf.metadata.routeKey))
                    if (sidebarState.isMobile) {
                        sidebarState.closeSidebar()
                    }
                },
                modifier = Modifier.fillMaxSize(),
                title = "Vibepocket",
                width = 256.dp
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.styles.background)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SidebarTrigger()
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Vibepocket",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.styles.foreground
                )
            }
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = { routeKey ->
                    NavEntry(routeKey) {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            LiquidGlassCard(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                val function = iocComposablesByTag["screen"]?.get(routeKey.key)
                                if (function == null) {
                                    PlaceholderScreen("❓", "未知页面")
                                } else {
                                    when (routeKey.key) {
                                        "site.addzero.vibepocket.screens.ImageScreen" -> PlaceholderScreen(
                                            "🖼️ 图片",
                                            "即将开放"
                                        )

                                        "site.addzero.vibepocket.screens.VideoScreen" -> PlaceholderScreen(
                                            "🎬 视频",
                                            "即将开放"
                                        )

                                        else -> function()
                                    }
                                }
                            }
                        }
                    }
                },
            )
        }
    }
}
