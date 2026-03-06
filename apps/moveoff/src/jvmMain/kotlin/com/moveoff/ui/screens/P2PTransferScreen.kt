package com.moveoff.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.moveoff.p2p.*
import kotlinx.coroutines.launch
import java.io.File

/**
 * P2P传输屏幕
 */
@Composable
fun P2PTransferScreen(
    p2pManager: P2PManager,
    syncRoot: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val discoveredNodes by p2pManager.discoveredNodes.collectAsState()
    var transferProgress by remember { mutableStateOf<P2PTransferProgress?>(null) }
    var selectedNode by remember { mutableStateOf<P2PNode?>(null) }
    var showSendDialog by remember { mutableStateOf(false) }
    var transferResult by remember { mutableStateOf<P2PTransferResult?>(null) }

    // 监听传输进度
    LaunchedEffect(Unit) {
        p2pManager.transferProgress.collect { progress ->
            transferProgress = progress
        }
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
            Text(
                text = "局域网传输",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            // P2P服务开关
            var isRunning by remember { mutableStateOf(false) }
            Switch(
                checked = isRunning,
                onCheckedChange = {
                    isRunning = it
                    if (it) p2pManager.start() else p2pManager.stop()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 本机信息
        LocalNodeInfo()

        Spacer(modifier = Modifier.height(16.dp))

        // 传输进度
        transferProgress?.let { progress ->
            TransferProgressCard(progress = progress)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 发现节点列表
        Text(
            text = "附近的设备 (${discoveredNodes.count { it.isOnline() }})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (discoveredNodes.isEmpty()) {
            EmptyNodeList()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(discoveredNodes.toList()) { node ->
                    NodeItem(
                        node = node,
                        onClick = {
                            selectedNode = node
                            showSendDialog = true
                        }
                    )
                }
            }
        }

        // 发送文件对话框
        if (showSendDialog && selectedNode != null) {
            SendFileDialog(
                node = selectedNode!!,
                syncRoot = syncRoot,
                onSend = { file ->
                    scope.launch {
                        val result = p2pManager.sendFile(selectedNode!!, file, file.name)
                        transferResult = result
                        showSendDialog = false
                    }
                },
                onDismiss = { showSendDialog = false }
            )
        }

        // 传输结果
        transferResult?.let { result ->
            TransferResultDialog(
                result = result,
                onDismiss = { transferResult = null }
            )
        }
    }
}

/**
 * 本机信息卡片
 */
@Composable
private fun LocalNodeInfo() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Computer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "本机",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "等待发现其他设备...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // 状态指示器
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

/**
 * 传输进度卡片
 */
@Composable
private fun TransferProgressCard(progress: P2PTransferProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = progress.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = "${(progress.percentage * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.percentage },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatFileSize(progress.transferred) + " / " + formatFileSize(progress.fileSize),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${formatFileSize(progress.speed.toLong())}/s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 空节点列表提示
 */
@Composable
private fun EmptyNodeList() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.WifiTethering,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "未发现其他设备",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "确保其他设备在同一局域网内并开启了P2P传输",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 节点项
 */
@Composable
private fun NodeItem(
    node: P2PNode,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (node.isOnline()) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 设备图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (node.deviceType) {
                        DeviceType.MOBILE -> Icons.Default.Smartphone
                        DeviceType.LAPTOP -> Icons.Default.Laptop
                        DeviceType.SERVER -> Icons.Default.Dns
                        else -> Icons.Default.Computer
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 节点信息
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = node.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (node.isOnline()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "在线",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "${node.host}:${node.port}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 发送按钮
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "发送文件"
                )
            }
        }
    }
}

/**
 * 发送文件对话框
 */
@Composable
private fun SendFileDialog(
    node: P2PNode,
    syncRoot: String,
    onSend: (File) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFile by remember { mutableStateOf<File?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("发送文件到 ${node.name}") },
        text = {
            Column {
                Text("选择要发送的文件:")
                Spacer(modifier = Modifier.height(8.dp))
                // 这里简化处理，实际应该有文件选择器
                OutlinedTextField(
                    value = selectedFile?.path ?: "",
                    onValueChange = {},
                    label = { Text("文件路径") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            // TODO: 打开文件选择器
                            selectedFile = File(syncRoot).listFiles()?.firstOrNull()
                        }) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "选择文件")
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedFile?.let { onSend(it) } },
                enabled = selectedFile != null
            ) {
                Text("发送")
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
 * 传输结果对话框
 */
@Composable
private fun TransferResultDialog(
    result: P2PTransferResult,
    onDismiss: () -> Unit
) {
    when (result) {
        is P2PTransferResult.Success -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                icon = {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = { Text("传输成功") },
                text = {
                    Column {
                        Text("文件传输完成")
                        Text("耗时: ${result.duration}ms")
                        Text("大小: ${formatFileSize(result.bytesTransferred)}")
                    }
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("确定")
                    }
                }
            )
        }
        is P2PTransferResult.Error -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                icon = {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text("传输失败") },
                text = { Text(result.message) },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("确定")
                    }
                }
            )
        }
        is P2PTransferResult.Rejected -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                icon = {
                    Icon(
                        Icons.Default.Block,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text("传输被拒绝") },
                text = { Text("原因: ${result.reason}") },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("确定")
                    }
                }
            )
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
    }
}