package com.moveoff.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moveoff.version.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 版本历史屏幕
 */
@Composable
fun VersionHistoryScreen(
    filePath: String,
    versionManager: VersionHistoryManager,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var versions by remember { mutableStateOf<List<FileVersion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedVersions by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showRestoreDialog by remember { mutableStateOf<FileVersion?>(null) }
    var showDeleteDialog by remember { mutableStateOf<FileVersion?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 加载版本列表
    LaunchedEffect(filePath) {
        versionManager.getVersions(filePath).fold(
            onSuccess = {
                versions = it
                isLoading = false
            },
            onFailure = {
                errorMessage = it.message
                isLoading = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题栏
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "版本历史",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = filePath,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            // 清理过期版本按钮
            TextButton(
                onClick = {
                    scope.launch {
                        versionManager.cleanupVersions(filePath).fold(
                            onSuccess = { result ->
                                // 刷新列表
                                versionManager.getVersions(filePath).onSuccess {
                                    versions = it
                                }
                            },
                            onFailure = {
                                errorMessage = it.message
                            }
                        )
                    }
                }
            ) {
                Icon(Icons.Default.CleaningServices, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("清理旧版本")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (versions.isEmpty()) {
            EmptyVersionHistory()
        } else {
            // 版本列表
            VersionList(
                versions = versions,
                selectedVersions = selectedVersions,
                onSelectVersion = { versionId ->
                    selectedVersions = if (versionId in selectedVersions) {
                        selectedVersions - versionId
                    } else {
                        selectedVersions + versionId
                    }
                },
                onRestore = { showRestoreDialog = it },
                onDelete = { showDeleteDialog = it },
                onCompare = { v1, v2 ->
                    // TODO: 实现版本对比
                }
            )
        }

        // 恢复确认对话框
        showRestoreDialog?.let { version ->
            RestoreConfirmDialog(
                version = version,
                onConfirm = {
                    scope.launch {
                        versionManager.restoreVersion(filePath, version.versionId).fold(
                            onSuccess = {
                                // 恢复成功
                                showRestoreDialog = null
                            },
                            onFailure = {
                                errorMessage = it.message
                                showRestoreDialog = null
                            }
                        )
                    }
                },
                onDismiss = { showRestoreDialog = null }
            )
        }

        // 删除确认对话框
        showDeleteDialog?.let { version ->
            DeleteConfirmDialog(
                version = version,
                onConfirm = {
                    scope.launch {
                        versionManager.deleteVersion(filePath, version.versionId).fold(
                            onSuccess = {
                                // 删除成功，刷新列表
                                versionManager.getVersions(filePath).onSuccess {
                                    versions = it
                                }
                                showDeleteDialog = null
                            },
                            onFailure = {
                                errorMessage = it.message
                                showDeleteDialog = null
                            }
                        )
                    }
                },
                onDismiss = { showDeleteDialog = null }
            )
        }

        // 错误提示
        errorMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { errorMessage = null },
                title = { Text("错误") },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = { errorMessage = null }) {
                        Text("确定")
                    }
                }
            )
        }
    }
}

/**
 * 空版本历史提示
 */
@Composable
private fun EmptyVersionHistory() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无版本历史",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "文件版本控制未启用或此文件尚无历史版本",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 版本列表
 */
@Composable
private fun VersionList(
    versions: List<FileVersion>,
    selectedVersions: Set<String>,
    onSelectVersion: (String) -> Unit,
    onRestore: (FileVersion) -> Unit,
    onDelete: (FileVersion) -> Unit,
    onCompare: (FileVersion, FileVersion) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(versions) { version ->
            VersionItem(
                version = version,
                isSelected = version.versionId in selectedVersions,
                isCompareSelected = selectedVersions.size == 2 && version.versionId in selectedVersions,
                dateFormat = dateFormat,
                onSelect = { onSelectVersion(version.versionId) },
                onRestore = { onRestore(version) },
                onDelete = { onDelete(version) }
            )
        }
    }
}

/**
 * 版本项
 */
@Composable
private fun VersionItem(
    version: FileVersion,
    isSelected: Boolean,
    isCompareSelected: Boolean,
    dateFormat: SimpleDateFormat,
    onSelect: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = when {
                version.isLatest -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                isCompareSelected -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                isSelected -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = if (version.isLatest) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        } else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 选择框
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelect() }
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 版本图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (version.isLatest) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (version.isLatest) {
                        Icons.Default.Star
                    } else {
                        Icons.Default.History
                    },
                    contentDescription = null,
                    tint = if (version.isLatest) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 版本信息
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateFormat.format(Date(version.lastModified)),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (version.isLatest) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "最新",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "大小: ${version.formatSize()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "版本ID: ${version.versionId.take(8)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // 操作按钮
            Row {
                IconButton(onClick = onRestore) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "恢复此版本"
                    )
                }
                if (!version.isLatest) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除此版本",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * 恢复确认对话框
 */
@Composable
private fun RestoreConfirmDialog(
    version: FileVersion,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Restore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("恢复版本") },
        text = {
            Column {
                Text("确定要恢复到此版本吗？")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "恢复时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(version.lastModified))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "文件大小: ${version.formatSize()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "注意：当前版本将被覆盖，但会保留为历史版本。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("恢复")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 删除确认对话框
 */
@Composable
private fun DeleteConfirmDialog(
    version: FileVersion,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("删除版本") },
        text = {
            Column {
                Text("确定要永久删除此版本吗？")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "此操作不可撤销，删除后无法恢复。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
