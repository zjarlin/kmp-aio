package site.addzero.kcloud.ui.tray

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import site.addzero.kcloud.app.WorkbenchRouteCatalog
import site.addzero.kcloud.app.WorkbenchShellState
import site.addzero.kcloud.feature.ShellSettingsService
import site.addzero.kcloud.feature.ShellThemeMode
import site.addzero.kcloud.ui.theme.KCloudTheme

@Composable
fun KCloudTrayPanelWindow(
    shellSettingsService: ShellSettingsService = koinInject(),
    shellState: WorkbenchShellState = koinInject(),
    routeCatalog: WorkbenchRouteCatalog = koinInject(),
) {
    val themeMode by shellSettingsService.themeMode.collectAsState()
    val primaryRoute = remember(routeCatalog) {
        routeCatalog.routeEntries.firstOrNull()
    }
    val trailingRoute = remember(routeCatalog) {
        routeCatalog.routeEntries.lastOrNull()
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
            color = Color.Transparent,
        ) {
            Box(
                modifier = Modifier.fillMaxSize().trayPanelBackdrop(),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = "KCloud 快捷面板",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
                        ),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "当前版本先保留一个轻量托盘入口。",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "后续可以继续把传输任务、物联网设备摘要或 Agent 状态收进这里。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                primaryRoute?.routePath?.let(shellState::selectRoute)
                                shellState.showWindow()
                                shellState.hideTrayPanel()
                            },
                        ) {
                            Text(primaryRoute?.title ?: "工作台")
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                trailingRoute?.routePath?.let(shellState::selectRoute)
                                shellState.showWindow()
                                shellState.hideTrayPanel()
                            },
                        ) {
                            Text(trailingRoute?.title ?: "最后页面")
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.trayPanelBackdrop(): Modifier = background(
    brush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF07111B),
            Color(0xFF0A1726),
            Color(0xFF08121D),
        ),
    ),
)
