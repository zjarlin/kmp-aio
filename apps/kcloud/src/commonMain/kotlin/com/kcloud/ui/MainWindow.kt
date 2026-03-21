package com.kcloud.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kcloud.app.KCloudShellState
import com.kcloud.feature.ShellThemeMode
import com.kcloud.feature.ShellSettingsService
import com.kcloud.ui.theme.KCloudTheme
import org.koin.compose.koinInject
import site.addzero.appsidebar.AppSidebar
import site.addzero.appsidebar.rememberAppSidebarState
import site.addzero.appsidebar.WorkbenchScaffold
import site.addzero.workbenchshell.ScreenCatalog
import site.addzero.workbenchshell.toAppSidebarItems

@Composable
fun MainWindow(
    screenCatalog: ScreenCatalog = koinInject(),
    shellState: KCloudShellState = koinInject(),
    shellSettingsService: ShellSettingsService = koinInject(),
) {
    val selectedScreenId by shellState.selectedScreenId.collectAsState()
    val themeMode by shellSettingsService.themeMode.collectAsState()
    val selectedNode = remember(screenCatalog, selectedScreenId) {
        screenCatalog.findLeaf(selectedScreenId)
    }
    val sidebarItems = remember(screenCatalog) {
        screenCatalog.toAppSidebarItems()
    }
    val sidebarState = rememberAppSidebarState(
        initialSelectedId = selectedNode?.id,
    )

    LaunchedEffect(selectedNode?.id, sidebarItems) {
        sidebarState.updateSelectedId(selectedNode?.id)
        sidebarState.revealSelection(
            items = sidebarItems,
            selectedId = selectedNode?.id,
        )
    }

    KCloudTheme(
        darkTheme = when (themeMode) {
            ShellThemeMode.LIGHT -> false
            ShellThemeMode.DARK -> true
            ShellThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        },
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            WorkbenchScaffold(
                modifier = Modifier.fillMaxSize(),
                minSidebarWidth = 248.dp,
                maxSidebarWidth = 340.dp,
                sidebar = {
                    AppSidebar(
                        title = "KCloud",
                        supportText = "模块化单体工作台",
                        items = sidebarItems,
                        state = sidebarState,
                        modifier = Modifier.fillMaxSize().sidebarFrame(),
                        onItemClick = { item ->
                            shellState.selectScreen(item.id)
                        },
                        footerSlot = {
                            SidebarSummaryCard(
                                pageCount = screenCatalog.visibleLeafNodes.size,
                            )
                        },
                    )
                },
                contentHeaderScrollable = false,
                contentHeader = {
                    ScreenHeader(
                        breadcrumb = screenCatalog.breadcrumbNamesFor(selectedNode?.id.orEmpty()),
                        title = selectedNode?.name ?: "未选择页面",
                    )
                },
                content = {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth().contentFrame(),
                        ) {
                            val content = selectedNode?.content
                            if (content == null) {
                                EmptyShellContent()
                            } else {
                                content()
                            }
                        }
                        ShellStatusBar(
                            currentTitle = selectedNode?.name ?: "未选择页面",
                            pageCount = screenCatalog.visibleLeafNodes.size,
                            modifier = Modifier.height(60.dp),
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun SidebarSummaryCard(
    pageCount: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "模块化单体",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "当前聚合了 $pageCount 个页面",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
            )
        }
    }
}

@Composable
private fun ScreenHeader(
    breadcrumb: List<String>,
    title: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (breadcrumb.isNotEmpty()) {
            Text(
                text = breadcrumb.joinToString(" / "),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun ShellStatusBar(
    currentTitle: String,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = currentTitle,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "已接入 $pageCount 个 Screen 节点",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            )
        }
    }
}

@Composable
private fun EmptyShellContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "暂无可用页面",
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

/** 侧栏外壳：让导航区和业务区保持稳定间距。 */
private fun Modifier.sidebarFrame(): Modifier {
    return fillMaxSize().padding(12.dp)
}

/** 内容安全区：避免业务页面直接贴到工作台边界。 */
private fun Modifier.contentFrame(): Modifier {
    return fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp)
}
