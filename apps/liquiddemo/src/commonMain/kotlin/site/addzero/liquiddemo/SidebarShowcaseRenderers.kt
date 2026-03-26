package site.addzero.liquiddemo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.core.annotation.Single
import site.addzero.appsidebar.AdminWorkbenchScaffold
import site.addzero.appsidebar.AppSidebarConfig
import site.addzero.appsidebar.AppSidebarScaffoldRenderer
import site.addzero.appsidebar.AppSidebarScaffoldShell
import site.addzero.appsidebar.AppSidebarSlots
import site.addzero.appsidebar.AppSidebarStyle
import site.addzero.appsidebar.WorkbenchScaffold
import site.addzero.workbenchshell.ScreenNode
import site.addzero.workbenchshell.ScreenSidebar

@Single(binds = [AppSidebarScaffoldRenderer::class])
class SidebarShowcaseWorkbenchRenderer(
    private val state: SidebarShowcaseState,
) : AppSidebarScaffoldRenderer {
    override val shell = AppSidebarScaffoldShell.Workbench

    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        WorkbenchScaffold(
            modifier = modifier.showcaseWorkbenchFrame(),
            contentHeaderScrollable = false,
            contentPadding = PaddingValues(18.dp),
            detailPadding = PaddingValues(18.dp),
            sidebar = {
                SidebarShowcaseSidebar(state = state)
            },
            contentHeader = {
                SidebarShowcaseWorkbenchHeader(state = state)
            },
            content = {
                SidebarShowcaseContent(state = state)
            },
            detail = {
                SidebarShowcaseDetail(state = state)
            },
        )
    }
}

@Single(binds = [AppSidebarScaffoldRenderer::class])
class SidebarShowcaseAdminWorkbenchRenderer(
    private val state: SidebarShowcaseState,
) : AppSidebarScaffoldRenderer {
    override val shell = AppSidebarScaffoldShell.AdminWorkbench

    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        val activeSlot = state.activeSlot
        AdminWorkbenchScaffold(
            modifier = modifier.showcaseWorkbenchFrame(),
            breadcrumb = state.breadcrumb,
            pageTitle = state.activeLeafNode.name,
            pageSubtitle = activeSlot.config.subtitle,
            contentPadding = PaddingValues(18.dp),
            detailPadding = PaddingValues(18.dp),
            sidebar = {
                SidebarShowcaseSidebar(state = state)
            },
            content = {
                SidebarShowcaseContent(state = state)
            },
            detail = {
                SidebarShowcaseDetail(state = state)
            },
            pageActions = {
                with(activeSlot) {
                    PageActions()
                }
            },
            onGlobalSearchClick = {},
            languageLabel = activeSlot.config.languageLabel,
            onLanguageClick = {},
            isDarkTheme = activeSlot.config.isDarkTheme,
            onThemeToggle = {},
            notificationCount = activeSlot.config.notificationCount,
            onNotificationsClick = {},
            userLabel = activeSlot.config.userLabel,
            onUserClick = {},
        )
    }
}

@Composable
private fun SidebarShowcaseSidebar(
    state: SidebarShowcaseState,
) {
    val activeSlot = state.activeSlot
    ScreenSidebar(
        title = state.activeSceneNode.name,
        items = state.sceneChildren,
        selectedId = state.activeLeafNode.id,
        onLeafClick = { node -> state.selectScreen(node.id) },
        config = AppSidebarConfig(
            style = AppSidebarStyle.FlushWorkbench,
            supportText = activeSlot.config.subtitle,
        ),
        slots = AppSidebarSlots(
            header = {
                with(activeSlot) {
                    SidebarHeader()
                }
            },
            footer = {
                with(activeSlot) {
                    SidebarFooter()
                }
            },
            trailing = { node, selected, descendantSelected ->
                with(activeSlot) {
                    SidebarTrailing(
                        node = node,
                        selected = selected,
                        descendantSelected = descendantSelected,
                    )
                }
            },
        ),
    )
}

@Composable
private fun RowScope.SidebarShowcaseWorkbenchHeader(
    state: SidebarShowcaseState,
) {
    val activeSlot = state.activeSlot
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = state.breadcrumb.joinToString(" / "),
            style = MaterialTheme.typography.labelLarge,
            color = ShowcaseRendererTokens.textMuted,
        )
        Text(
            text = state.activeLeafNode.name,
            style = MaterialTheme.typography.titleLarge,
            color = ShowcaseRendererTokens.textPrimary,
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        with(activeSlot) {
            PageActions()
        }
    }
}

@Composable
private fun SidebarShowcaseContent(
    state: SidebarShowcaseState,
) {
    Box(
        modifier = Modifier.fillMaxSize().showcasePanelFrame(),
    ) {
        state.activeLeafNode.content?.invoke()
    }
}

@Composable
private fun SidebarShowcaseDetail(
    state: SidebarShowcaseState,
) {
    val activeSlot = state.activeSlot
    Box(
        modifier = Modifier.fillMaxSize().showcasePanelFrame(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            with(activeSlot) {
                Detail(state.activeLeafNode)
            }
        }
    }
}

private fun Modifier.showcaseWorkbenchFrame(): Modifier {
    return fillMaxSize()
}

private fun Modifier.showcasePanelFrame(): Modifier {
    return background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xB3121A27),
                Color(0xAA0B121F),
            ),
        ),
        shape = RoundedCornerShape(26.dp),
    ).border(
        width = 1.dp,
        color = Color.White.copy(alpha = 0.07f),
        shape = RoundedCornerShape(26.dp),
    )
}

private object ShowcaseRendererTokens {
    val textPrimary = Color(0xFFE8EEF8)
    val textMuted = Color(0xFF9EB0C7)
}
