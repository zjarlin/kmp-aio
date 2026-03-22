package site.addzero.kcloud.ui.tray

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import site.addzero.kcloud.app.KCloudShellState
import site.addzero.kcloud.feature.ShellSettingsService
import site.addzero.kcloud.feature.ShellThemeMode
import site.addzero.kcloud.plugins.settings.SettingsFeatureMenus
import site.addzero.kcloud.features.transferhistory.TransferHistoryFeatureMenus
import site.addzero.kcloud.features.transferhistory.TransferHistoryQueueItem
import site.addzero.kcloud.features.transferhistory.TransferHistoryQueueOperation
import site.addzero.kcloud.features.transferhistory.TransferHistoryQueueStatus
import site.addzero.kcloud.features.transferhistory.TransferHistoryService
import site.addzero.kcloud.ui.theme.KCloudTheme
import org.koin.compose.koinInject
import site.addzero.kcloud.ui.tray.displayName
import site.addzero.kcloud.ui.tray.progressRatio
import site.addzero.kcloud.ui.tray.queueProgressFill
import site.addzero.kcloud.ui.tray.queueProgressTrack
import site.addzero.kcloud.ui.tray.trayPanelBackdrop
import site.addzero.kcloud.ui.tray.trayPanelContainer

@Composable
fun KCloudTrayPanelWindow(
    shellSettingsService: ShellSettingsService = koinInject(),
    shellState: site.addzero.kcloud.app.KCloudShellState = koinInject(),
    transferHistoryService: TransferHistoryService = koinInject(),
) {
    val themeMode by shellSettingsService.themeMode.collectAsState()
    val snapshot by transferHistoryService.snapshot.collectAsState()

    _root_ide_package_.site.addzero.kcloud.ui.theme.KCloudTheme(
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
                    modifier = Modifier.fillMaxSize().trayPanelContainer(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    _root_ide_package_.site.addzero.kcloud.ui.tray.TrayPanelHeader(
                        onOpenMainWindow = {
                            shellState.showWindow()
                            shellState.hideTrayPanel()
                        },
                        onOpenTransferHistory = {
                            shellState.selectScreen(TransferHistoryFeatureMenus.TRANSFER_HISTORY)
                            shellState.showWindow()
                            shellState.hideTrayPanel()
                        },
                        onOpenSettings = {
                            shellState.selectScreen(site.addzero.kcloud.plugins.settings.SettingsFeatureMenus.SETTINGS)
                            shellState.showWindow()
                            shellState.hideTrayPanel()
                        },
                    )
                    _root_ide_package_.site.addzero.kcloud.ui.tray.TrayPanelStats(snapshot = snapshot)
                    _root_ide_package_.site.addzero.kcloud.ui.tray.TrayPanelQueueList(queueItems = snapshot.queueItems)
                }
            }
        }
    }
}

@Composable
private fun TrayPanelHeader(
    onOpenMainWindow: () -> Unit,
    onOpenTransferHistory: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "KCloud 传输面板",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "托盘点击直接看同步队列和迁移摘要，不用先展开主窗口。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onOpenMainWindow,
                modifier = Modifier.weight(1f),
            ) {
                Text("主窗口")
            }
            Button(
                onClick = onOpenTransferHistory,
                modifier = Modifier.weight(1f),
            ) {
                Text("迁移记录")
            }
            Button(
                onClick = onOpenSettings,
                modifier = Modifier.weight(1f),
            ) {
                Text("设置")
            }
        }
    }
}

@Composable
private fun TrayPanelStats(
    snapshot: site.addzero.kcloud.features.transferhistory.TransferHistorySnapshot,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        _root_ide_package_.site.addzero.kcloud.ui.tray.TrayStatCard(
            title = "运行中",
            value = snapshot.stats.queueRunning.toString(),
            modifier = Modifier.weight(1f),
        )
        _root_ide_package_.site.addzero.kcloud.ui.tray.TrayStatCard(
            title = "等待中",
            value = snapshot.stats.queuePending.toString(),
            modifier = Modifier.weight(1f),
        )
        _root_ide_package_.site.addzero.kcloud.ui.tray.TrayStatCard(
            title = "失败",
            value = snapshot.stats.queueFailed.toString(),
            modifier = Modifier.weight(1f),
        )
        _root_ide_package_.site.addzero.kcloud.ui.tray.TrayStatCard(
            title = "冲突",
            value = snapshot.stats.conflicts.toString(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TrayStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun TrayPanelQueueList(
    queueItems: List<TransferHistoryQueueItem>,
) {
    if (queueItems.isEmpty()) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
            ),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "当前没有传输任务",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "后续这里会实时显示上传、下载、失败和重试摘要。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "最近传输",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(queueItems.take(8), key = { item -> item.id }) { item ->
                _root_ide_package_.site.addzero.kcloud.ui.tray.TrayQueueItemCard(item = item)
            }
        }
    }
}

@Composable
private fun TrayQueueItemCard(
    item: TransferHistoryQueueItem,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "#${item.id} · ${item.operation.displayName()}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = item.status.displayName(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.weight(1f).height(6.dp).queueProgressTrack(),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(item.progressRatio()).height(6.dp).queueProgressFill(),
                    )
                }
                Text(
                    text = "${(item.progressRatio() * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = buildString {
                    append("文件 ${item.fileId}")
                    append(" · ${item.progressBytes} / ${item.totalBytes} B")
                    if (item.retryCount > 0) {
                        append(" · 重试 ${item.retryCount}")
                    }
                    if (!item.errorMessage.isNullOrBlank()) {
                        append(" · ${item.errorMessage}")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** 托盘面板底色：和主工作台同源，但更聚焦、更像悬浮工具面板。 */
private fun Modifier.trayPanelBackdrop(): Modifier {
    return background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A1522),
                Color(0xFF0B1A29),
                Color(0xFF08111C),
            ),
        ),
    ).background(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF2F64A0).copy(alpha = 0.26f),
                Color.Transparent,
            ),
            radius = 360f,
        ),
    )
}

/** 托盘面板容器：用一层半透明卡片收住内容，避免像原生灰盒。 */
private fun Modifier.trayPanelContainer(): Modifier {
    return padding(12.dp)
        .background(
            color = Color(0xCC0A1522),
            shape = RoundedCornerShape(18.dp),
        )
        .padding(14.dp)
}

/** 传输进度轨道：保持细而稳，不让托盘面板显得笨重。 */
private fun Modifier.queueProgressTrack(): Modifier {
    return background(
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(999.dp),
    )
}

/** 传输进度填充：沿用 KCloud 主色，让托盘面板和主窗口有同一语言。 */
private fun Modifier.queueProgressFill(): Modifier {
    return background(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF4D8EFF),
                Color(0xFF6AB0FF),
            ),
        ),
        shape = RoundedCornerShape(999.dp),
    )
}

private fun TransferHistoryQueueItem.progressRatio(): Float {
    if (totalBytes <= 0) {
        return 0f
    }
    return (progressBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
}

private fun TransferHistoryQueueStatus.displayName(): String {
    return when (this) {
        TransferHistoryQueueStatus.PENDING -> "等待中"
        TransferHistoryQueueStatus.RUNNING -> "运行中"
        TransferHistoryQueueStatus.PAUSED -> "已暂停"
        TransferHistoryQueueStatus.COMPLETED -> "已完成"
        TransferHistoryQueueStatus.FAILED -> "失败"
        TransferHistoryQueueStatus.CANCELLED -> "已取消"
    }
}

private fun TransferHistoryQueueOperation.displayName(): String {
    return when (this) {
        TransferHistoryQueueOperation.UPLOAD -> "上传"
        TransferHistoryQueueOperation.DOWNLOAD -> "下载"
        TransferHistoryQueueOperation.DELETE_LOCAL -> "删本地"
        TransferHistoryQueueOperation.DELETE_REMOTE -> "删远端"
    }
}
