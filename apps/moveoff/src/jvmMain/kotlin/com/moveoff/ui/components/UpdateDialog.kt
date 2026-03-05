package com.moveoff.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.moveoff.model.VersionInfo
import com.moveoff.update.DownloadProgress
import com.moveoff.update.UpdateState

/**
 * 更新可用对话框
 */
@Composable
fun UpdateAvailableDialog(
    versionInfo: VersionInfo,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onSkip: () -> Unit,
    onRemindLater: () -> Unit
) {
    Dialog(
        onDismissRequest = { if (!versionInfo.mandatory) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !versionInfo.mandatory,
            dismissOnClickOutside = !versionInfo.mandatory
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "发现新版本",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "v${versionInfo.version}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (versionInfo.mandatory) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "此更新为强制更新，需要安装后才能继续使用",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Release notes
                Text(
                    text = "更新内容",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = versionInfo.releaseNotes,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (!versionInfo.mandatory) {
                        TextButton(onClick = onRemindLater) {
                            Text("稍后提醒")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = onSkip) {
                            Text("跳过此版本")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Button(onClick = onDownload) {
                        Icon(Icons.Default.Download, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("立即下载")
                    }
                }
            }
        }
    }
}

/**
 * 下载进度对话框
 */
@Composable
fun DownloadProgressDialog(
    version: String,
    progress: DownloadProgress,
    onCancel: () -> Unit
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "正在下载更新",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "MoveOff v$version",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { progress.percentage / 100f },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${formatBytes(progress.downloadedBytes)} / ${formatBytes(progress.totalBytes)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${progress.percentage}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("取消")
                }
            }
        }
    }
}

/**
 * 下载完成对话框
 */
@Composable
fun DownloadCompleteDialog(
    version: String,
    onInstall: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "下载完成",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "MoveOff v$version 已准备就绪",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("稍后安装")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onInstall) {
                        Icon(Icons.Default.InstallDesktop, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("立即安装")
                    }
                }
            }
        }
    }
}

/**
 * 格式化字节数
 */
private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 * 1024 -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        bytes >= 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> String.format("%.2f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}

/**
 * 更新管理器 Composable
 * 监听更新状态并显示相应对话框
 */
@Composable
fun UpdateManager(
    updateChecker: com.moveoff.update.UpdateChecker
) {
    val updateState by updateChecker.updateState.collectAsState()
    val availableUpdate by updateChecker.availableUpdate.collectAsState()
    val downloadProgress by updateChecker.downloadProgress.collectAsState()

    var showDownloadComplete by remember { mutableStateOf(false) }

    // 显示更新可用对话框
    availableUpdate?.let { versionInfo ->
        if (updateState != UpdateState.DOWNLOADING &&
            updateState != UpdateState.DOWNLOADED &&
            !showDownloadComplete
        ) {
            UpdateAvailableDialog(
                versionInfo = versionInfo,
                onDismiss = { updateChecker.remindLater() },
                onDownload = {
                    // 开始下载
                    // 需要在协程中调用
                },
                onSkip = { updateChecker.skipVersion(versionInfo.version) },
                onRemindLater = { updateChecker.remindLater() }
            )
        }
    }

    // 显示下载进度
    if (updateState == UpdateState.DOWNLOADING) {
        availableUpdate?.let { versionInfo ->
            DownloadProgressDialog(
                version = versionInfo.version,
                progress = downloadProgress,
                onCancel = {
                    // 取消下载
                }
            )
        }
    }

    // 显示下载完成
    if (updateState == UpdateState.DOWNLOADED || showDownloadComplete) {
        availableUpdate?.let { versionInfo ->
            DownloadCompleteDialog(
                version = versionInfo.version,
                onInstall = {
                    // 触发安装
                },
                onDismiss = { showDownloadComplete = false }
            )
        }
    }
}
