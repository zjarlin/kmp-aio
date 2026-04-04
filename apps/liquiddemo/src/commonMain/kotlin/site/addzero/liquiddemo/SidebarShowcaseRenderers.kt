package site.addzero.liquiddemo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import site.addzero.appsidebar.*
import site.addzero.appsidebar.spi.scaffoldConfig

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
            sidebar = {
                SidebarShowcaseSidebar(state = state)
            },
            content = {
                SidebarShowcaseContent(state = state)
            },
            config = scaffoldConfig(
                contentHeaderScrollable = false,
                contentPadding = PaddingValues(18.dp),
                detailPadding = PaddingValues(18.dp),
            ),
            slots = workbenchScaffoldSlots(
                contentHeader = {
                    SidebarShowcaseWorkbenchHeader(state = state)
                },
                detail = detailPanel,
            ),
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
        val activeScene = state.activeScene
        val detailPanel: (@Composable BoxScope.() -> Unit)? = if (state.detailVisible) {
            { SidebarShowcaseDetail(state = state) }
        } else {
            null
        }
        AdminWorkbenchScaffold(
            modifier = modifier.fillMaxSize(),
            sidebar = {
                SidebarShowcaseSidebar(state = state)
            },
            content = {
                SidebarShowcaseContent(state = state)
            },
            page = adminWorkbenchPageConfig(
                breadcrumb = state.breadcrumb,
                pageTitle = state.activeLeafNode.name,
                pageSubtitle = activeScene.config.subtitle,
            ),
            config = adminWorkbenchConfig(
                contentPadding = PaddingValues(18.dp),
                detailPadding = PaddingValues(18.dp),
                isDarkTheme = state.isDarkTheme,
            ),
            slots = adminWorkbenchSlots(
                detail = detailPanel,
                titleContent = {
                    SidebarShowcaseHeaderTitle(state = state)
                },
                pageActions = {
                    SidebarShowcaseHeaderActions(state = state)
                },
                topBarActions = {
                    WorkbenchGitHubButton(
                        label = state.githubLabel,
                        onClick = state::openGithub,
                    )
                    WorkbenchLanguageButton(
                        label = state.languageToggleLabel,
                        onClick = state::toggleLanguage,
                    )
                    WorkbenchThemeToggleButton(
                        isDarkTheme = state.isDarkTheme,
                        onClick = state::toggleTheme,
                    )
                    WorkbenchNotificationButton(
                        count = activeScene.config.notificationCount,
                        onClick = state::openNotifications,
                    )
                    WorkbenchUserButton(
                        label = activeScene.config.userLabel,
                        onClick = state::openUserProfile,
                    )
                },
            ),
        )
    }
}

@Composable
private fun SidebarShowcaseSidebar(
    state: SidebarShowcaseState,
) {
    val activeScene = state.activeScene
    val isAdminShell = state.currentShell == AppSidebarScaffoldShell.AdminWorkbench
    val mutedColor = if (isAdminShell) {
        ShowcaseRendererTokens.adminTextMuted
    } else {
        ShowcaseRendererTokens.textMuted
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SidebarShowcaseSidebarInfo(activeScene.config.headerInfo)
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "页面导航",
                style = MaterialTheme.typography.labelLarge,
                color = mutedColor,
            )
            activeScene.leaves.forEach { leaf ->
                val selected = leaf.id == state.activeLeafNode.id
                if (selected) {
                    Button(
                        onClick = { state.selectLeaf(leaf.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Text(leaf.name)
                    }
                } else {
                    OutlinedButton(
                        onClick = { state.selectLeaf(leaf.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Text(leaf.name)
                    }
                }
            }
        }
        SidebarShowcaseSidebarInfo(activeScene.config.footerInfo)
    }
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
            state.activeLeafNode.content()
        }
    }
}

@Composable
private fun SidebarShowcaseDetail(
    state: SidebarShowcaseState,
) {
    val activeScene = state.activeScene
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
                SidebarShowcaseDetailContent(
                    scene = activeScene,
                    leaf = state.activeLeafNode,
                )
            }
        }
    }
}

@Composable
private fun SidebarShowcaseHeaderTitle(
    state: SidebarShowcaseState,
    modifier: Modifier = Modifier,
) {
    val activeScene = state.activeScene
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
            activeScene.config.subtitle.takeIf(String::isNotBlank)?.let { subtitle ->
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
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SidebarShowcasePageActions(state.activeScene.config)
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

@Composable
private fun ColumnScope.SidebarShowcaseSidebarInfo(
    info: SidebarShowcaseInfoConfig,
) {
    if (info.title.isBlank() && info.value.isBlank()) {
        return
    }
    ShowcaseSidebarInfo(
        title = info.title,
        value = info.value,
    )
}

@Composable
private fun RowScope.SidebarShowcasePageActions(
    config: SidebarShowcaseSceneConfig,
) {
    if (config.pagePrimaryActionLabel.isNotBlank()) {
        Button(onClick = {}) {
            Text(config.pagePrimaryActionLabel)
        }
    }
    if (config.pageSecondaryActionLabel.isNotBlank()) {
        OutlinedButton(onClick = {}) {
            Text(config.pageSecondaryActionLabel)
        }
    }
}

@Composable
private fun ColumnScope.SidebarShowcaseDetailContent(
    scene: SidebarShowcaseSceneDefinition,
    leaf: SidebarShowcaseLeaf,
) {
    val detail = scene.details[leaf.id] ?: return
    ShowcaseInspector(
        title = detail.title,
        summary = detail.summary,
        facts = detail.facts.map { fact -> fact.label to fact.value },
        tasks = detail.tasks,
    )
}

private object ShowcaseRendererTokens {
    val textPrimary = Color(0xFFE8EEF8)
    val textMuted = Color(0xFF9EB0C7)
    val adminTextPrimary = Color(0xFF1F2937)
    val adminTextMuted = Color(0xFF6B7280)
}
