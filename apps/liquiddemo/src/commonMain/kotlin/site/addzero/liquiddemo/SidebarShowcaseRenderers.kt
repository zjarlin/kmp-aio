package site.addzero.liquiddemo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.core.annotation.Single
import site.addzero.appsidebar.AdminWorkbenchScaffold
import site.addzero.appsidebar.AppSidebarScaffoldRenderer
import site.addzero.appsidebar.AppSidebarScaffoldShell
import site.addzero.appsidebar.WorkbenchLanguageButton
import site.addzero.appsidebar.WorkbenchScaffold
import site.addzero.appsidebar.WorkbenchThemeToggleButton
import site.addzero.workbenchshell.ScreenSidebar

@Single
class SidebarShowcaseWorkbenchRenderer(
    private val state: SidebarShowcaseState,
) : AppSidebarScaffoldRenderer {
    override val shell = AppSidebarScaffoldShell.Workbench

    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        val detailPanel: (@Composable BoxScope.() -> Unit)? = if (state.detailVisible) {
            { SidebarShowcaseDetail(state = state) }
        } else {
            null
        }
        WorkbenchScaffold(
            modifier = modifier.fillMaxSize(),
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
            detail = detailPanel,
        )
    }
}

@Single
class SidebarShowcaseAdminWorkbenchRenderer(
    private val state: SidebarShowcaseState,
) : AppSidebarScaffoldRenderer {
    override val shell = AppSidebarScaffoldShell.AdminWorkbench

    @Composable
    override fun Render(
        modifier: Modifier,
    ) {
        val activeSlot = state.activeSlot
        val detailPanel: (@Composable BoxScope.() -> Unit)? = if (state.detailVisible) {
            { SidebarShowcaseDetail(state = state) }
        } else {
            null
        }
        AdminWorkbenchScaffold(
            modifier = modifier.fillMaxSize(),
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
            detail = detailPanel,
            titleContent = {
                SidebarShowcaseHeaderTitle(state = state)
            },
            pageActions = {
                SidebarShowcaseHeaderActions(state = state)
            },
            onGlobalSearchClick = null,
            githubLabel = state.githubLabel,
            onGithubClick = state::openGithub,
            languageLabel = state.languageToggleLabel,
            onLanguageClick = state::toggleLanguage,
            isDarkTheme = state.isDarkTheme,
            onThemeToggle = state::toggleTheme,
            notificationCount = activeSlot.config.notificationCount,
            onNotificationsClick = state::openNotifications,
            userLabel = activeSlot.config.userLabel,
            onUserClick = state::openUserProfile,
        )
    }
}

@Composable
private fun SidebarShowcaseSidebar(
    state: SidebarShowcaseState,
) {
    ScreenSidebar(
        title = "",
        items = state.sceneChildren,
        selectedId = state.activeLeafNode.id,
        onLeafClick = { node -> state.selectScreen(node.id) },
        subtitle = null,
    )
}

@Composable
private fun RowScope.SidebarShowcaseWorkbenchHeader(
    state: SidebarShowcaseState,
) {
    SidebarShowcaseHeaderTitle(
        state = state,
        modifier = Modifier.weight(1f),
    )
    SidebarShowcaseHeaderActions(state = state)
}

@Composable
private fun SidebarShowcaseContent(
    state: SidebarShowcaseState,
) {
    val isAdminShell = state.currentShell == AppSidebarScaffoldShell.AdminWorkbench
    val panelShape = RoundedCornerShape(if (isAdminShell) 0.dp else 26.dp)
    Box(
        modifier = Modifier.fillMaxSize().background(
            brush = if (isAdminShell) {
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF2F5F9),
                        Color(0xFFF2F5F9),
                    ),
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xB3121A27),
                        Color(0xAA0B121F),
                    ),
                )
            },
            shape = panelShape,
        ).border(
            width = 1.dp,
            color = if (isAdminShell) {
                Color.Transparent
            } else {
                Color.White.copy(alpha = 0.07f)
            },
            shape = panelShape,
        ),
    ) {
        ProvideShowcasePageTone(
            tone = if (isAdminShell) ShowcasePageTone.Light else ShowcasePageTone.Dark,
        ) {
            state.activeLeafNode.content?.invoke()
        }
    }
}

@Composable
private fun SidebarShowcaseDetail(
    state: SidebarShowcaseState,
) {
    val activeSlot = state.activeSlot
    val isAdminShell = state.currentShell == AppSidebarScaffoldShell.AdminWorkbench
    val panelShape = RoundedCornerShape(if (isAdminShell) 0.dp else 26.dp)
    Box(
        modifier = Modifier.fillMaxSize().background(
            brush = if (isAdminShell) {
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFFFFFFF),
                    ),
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xB3121A27),
                        Color(0xAA0B121F),
                    ),
                )
            },
            shape = panelShape,
        ).border(
            width = 1.dp,
            color = if (isAdminShell) {
                Color.Transparent
            } else {
                Color.White.copy(alpha = 0.07f)
            },
            shape = panelShape,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SidebarShowcaseDetailToolbar(state = state)
            ProvideShowcasePageTone(
                tone = if (isAdminShell) ShowcasePageTone.Light else ShowcasePageTone.Dark,
            ) {
                with(activeSlot) {
                    Detail(state.activeLeafNode)
                }
            }
        }
    }
}

@Composable
private fun SidebarShowcaseHeaderTitle(
    state: SidebarShowcaseState,
    modifier: Modifier = Modifier,
) {
    val activeSlot = state.activeSlot
    val isAdminShell = state.currentShell == AppSidebarScaffoldShell.AdminWorkbench
    val mutedColor = if (isAdminShell) {
        ShowcaseRendererTokens.adminTextMuted
    } else {
        ShowcaseRendererTokens.textMuted
    }
    val primaryColor = if (isAdminShell) {
        ShowcaseRendererTokens.adminTextPrimary
    } else {
        ShowcaseRendererTokens.textPrimary
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "场景切换",
            style = MaterialTheme.typography.labelLarge,
            color = mutedColor,
        )
        SidebarShowcaseSceneTabs(state = state)
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = state.breadcrumb.joinToString(" / "),
                style = MaterialTheme.typography.labelLarge,
                color = mutedColor,
            )
            Text(
                text = state.activeLeafNode.name,
                style = MaterialTheme.typography.titleLarge,
                color = primaryColor,
            )
            activeSlot.config.subtitle.takeIf(String::isNotBlank)?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedColor,
                )
            }
        }
    }
}

@Composable
private fun SidebarShowcaseSceneTabs(
    state: SidebarShowcaseState,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        state.scenes.forEach { scene ->
            val selected = scene.id == state.selectedSceneId
            if (selected) {
                Button(
                    onClick = { state.selectScene(scene.id) },
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(scene.name)
                }
            } else {
                OutlinedButton(
                    onClick = { state.selectScene(scene.id) },
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(scene.name)
                }
            }
        }
    }
}

@Composable
private fun RowScope.SidebarShowcaseHeaderActions(
    state: SidebarShowcaseState,
) {
    val activeSlot = state.activeSlot
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        with(activeSlot) {
            PageActions()
        }
        if (!state.detailVisible) {
            SidebarShowcaseDetailToggleButton(state = state)
        }
    }
}

@Composable
private fun SidebarShowcaseDetailToolbar(
    state: SidebarShowcaseState,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.currentShell != AppSidebarScaffoldShell.AdminWorkbench) {
            WorkbenchThemeToggleButton(
                isDarkTheme = state.isDarkTheme,
                onClick = state::toggleTheme,
            )
            WorkbenchLanguageButton(
                label = state.languageToggleLabel,
                onClick = state::toggleLanguage,
            )
        }
        SidebarShowcaseDetailToggleButton(state = state)
    }
}

@Composable
private fun SidebarShowcaseDetailToggleButton(state: SidebarShowcaseState) {
    OutlinedButton(
        onClick = state::toggleDetailVisibility,
    ) {
        Text(
            text = if (state.detailVisible) "隐藏概览" else "显示概览",
        )
    }
}

private object ShowcaseRendererTokens {
    val textPrimary = Color(0xFFE8EEF8)
    val textMuted = Color(0xFF9EB0C7)
    val adminTextPrimary = Color(0xFF1F2937)
    val adminTextMuted = Color(0xFF6B7280)
}
