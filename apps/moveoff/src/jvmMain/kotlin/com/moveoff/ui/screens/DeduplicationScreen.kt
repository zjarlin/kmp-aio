package com.moveoff.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moveoff.dedup.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 文件去重屏幕
 */
@Composable
fun DeduplicationScreen(
    dedupManager: DeduplicationManager,
    syncRoot: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val progress by dedupManager.progress.collectAsState()

    var scanResult by remember { mutableStateOf<DedupScanResult?>(null) }
    var isScanning by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDryRunResult by remember { mutableStateOf<DedupActionResult?>(null) }
    var actionResult by remember { mutableStateOf<DedupActionResult?>(null) }
    var useHardLink by remember { mutableStateOf(false) }
    var expandedGroups by remember { mutableStateOf<Set<String>>(emptySet()) }

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
            Text(
                text = "文件去重",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 扫描控制区域
        if (!isScanning && scanResult == null) {
            ScanControlPanel(
                onStartScan = {
                    isScanning = true
                    scope.launch {
                        dedupManager.scanDuplicates(syncRoot).fold(
                            onSuccess = {
                                scanResult = it
                                isScanning = false
                            },
                            onFailure = {
                                isScanning = false
                                actionResult = DedupActionResult.Error(it.message ?: "扫描失败")
                            }
                        )
                    }
                }
            )
        }

        // 扫描进度
        if (isScanning && progress != null) {
            ScanProgressPanel(progress = progress!!)
        }

        // 扫描结果
        scanResult?.let { result ->
            if (result.duplicateGroups.isEmpty()) {
                EmptyResultPanel()
            } else {
                // 结果统计
                ResultSummaryPanel(
                    result = result,
                    useHardLink = useHardLink,
                    onUseHardLinkChanged = { useHardLink = it },
                    onPreview = {
                        scope.launch {
                            val dryRunResult = if (useHardLink) {
                                dedupManager.hardLinkDuplicates(result.duplicateGroups)
                            } else {
                                dedupManager.smartRemoveDuplicates(
                                    result.duplicateGroups,
                                    syncRoot,
                                    dryRun = true
                                )
                            }
                            showDryRunResult = dryRunResult
                        }
                    },
                    onExecute = {
                        showDeleteConfirm = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 重复组列表
                DuplicateGroupList(
                    groups = result.duplicateGroups,
                    expandedGroups = expandedGroups,
                    onToggleExpand = { hash ->
                        expandedGroups = if (hash in expandedGroups) {
                            expandedGroups - hash
                        } else {
                            expandedGroups + hash
                        }
                    }
                )
            }
        }

        // 删除确认对话框
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                icon = {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text("确认删除") },
                text = {
                    Column {
                        Text("确定要删除重复文件吗？")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (useHardLink) {
                                "将使用硬链接替换重复文件，节省空间但保留文件结构。"
                            } else {
                                "删除的重复文件将被移到回收站，保留原始文件。"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirm = false
                            scope.launch {
                                val result = if (useHardLink) {
                                    dedupManager.hardLinkDuplicates(scanResult!!.duplicateGroups)
                                } else {
                                    dedupManager.smartRemoveDuplicates(
                                        scanResult!!.duplicateGroups,
                                        syncRoot,
                                        dryRun = false
                                    )
                                }
                                actionResult = result
                            }
                        }
                    ) {
                        Text("确认")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("取消")
                    }
                }
            )
        }

        // 操作结果对话框
        actionResult?.let { result ->
            when (result) {
                is DedupActionResult.Success -> {
                    AlertDialog(
                        onDismissRequest = {
                            actionResult = null
                            scanResult = null // 清除结果，允许重新扫描
                        },
                        icon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        title = { Text("操作完成") },
                        text = {
                            Column {
                                Text("成功处理 ${result.deletedCount} 个重复文件")
                                Text(
                                    "节省空间: ${formatFileSize(result.savedSpace)}",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    actionResult = null
                                    scanResult = null
                                }
                            ) {
                                Text("确定")
                            }
                        }
                    )
                }
                is DedupActionResult.Error -> {
                    AlertDialog(
                        onDismissRequest = { actionResult = null },
                        icon = {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        title = { Text("操作失败") },
                        text = { Text(result.message) },
                        confirmButton = {
                            TextButton(onClick = { actionResult = null }) {
                                Text("确定")
                            }
                        }
                    )
                }
            }
        }

        // 预览结果对话框
        showDryRunResult?.let { result ->
            when (result) {
                is DedupActionResult.Success -> {
                    AlertDialog(
                        onDismissRequest = { showDryRunResult = null },
                        title = { Text("预览结果") },
                        text = {
                            Column {
                                Text("将处理 ${result.deletedCount} 个重复文件")
                                Text(
                                    "预计节省空间: ${formatFileSize(result.savedSpace)}",
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "这是模拟结果，实际执行前请确认。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showDryRunResult = null
                                    showDeleteConfirm = true
                                }
                            ) {
                                Text("执行")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDryRunResult = null }) {
                                Text("取消")
                            }
                        }
                    )
                }
                else -> { showDryRunResult = null }
            }
        }
    }
}

/**
 * 扫描控制面板
 */
@Composable
private fun ScanControlPanel(
    onStartScan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "扫描重复文件",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "基于文件内容哈希检测完全相同的文件",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onStartScan,
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("开始扫描")
            }
        }
    }
}

/**
 * 扫描进度面板
 */
@Composable
private fun ScanProgressPanel(
    progress: DedupProgress
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = when (progress.stage) {
                    DedupStage.SCANNING -> "正在扫描文件..."
                    DedupStage.HASHING -> "正在计算文件哈希..."
                    DedupStage.ANALYZING -> "正在分析重复..."
                    DedupStage.COMPLETED -> "扫描完成"
                },
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = progress.currentFile,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress.progress },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${progress.processedCount} / ${progress.totalCount} 文件",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 空结果面板
 */
@Composable
private fun EmptyResultPanel() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "未发现重复文件",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "您的同步目录很干净！",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 结果汇总面板
 */
@Composable
private fun ResultSummaryPanel(
    result: DedupScanResult,
    useHardLink: Boolean,
    onUseHardLinkChanged: (Boolean) -> Unit,
    onPreview: () -> Unit,
    onExecute: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = "${result.totalDuplicates}",
                    label = "重复文件"
                )
                StatItem(
                    value = "${result.duplicateGroups.size}",
                    label = "重复组"
                )
                StatItem(
                    value = formatFileSize(result.potentialSavings),
                    label = "可节省空间"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 硬链接选项
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = useHardLink,
                    onCheckedChange = onUseHardLinkChanged
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("使用硬链接")
                    Text(
                        "保留文件结构，通过硬链接节省空间",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPreview,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("预览")
                }
                Button(
                    onClick = onExecute,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("清理重复")
                }
            }
        }
    }
}

/**
 * 统计项
 */
@Composable
private fun StatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 重复组列表
 */
@Composable
private fun DuplicateGroupList(
    groups: List<DuplicateGroup>,
    expandedGroups: Set<String>,
    onToggleExpand: (String) -> Unit
) {
    Text(
        text = "重复文件组",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(8.dp))

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(groups) { group ->
            DuplicateGroupItem(
                group = group,
                isExpanded = group.hash in expandedGroups,
                onToggle = { onToggleExpand(group.hash) }
            )
        }
    }
}

/**
 * 重复组项
 */
@Composable
private fun DuplicateGroupItem(
    group: DuplicateGroup,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${group.files.size} 个相同文件",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "大小: ${formatFileSize(group.size)} | 可节省: ${formatFileSize(group.potentialSavings())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "-${formatFileSize(group.potentialSavings())}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                group.files.forEach { file ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (file.isOriginal) {
                                Icons.Default.Star
                            } else {
                                Icons.Default.InsertDriveFile
                            },
                            contentDescription = null,
                            tint = if (file.isOriginal) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = file.path,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                            if (file.isOriginal) {
                                Text(
                                    text = "原始文件",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
