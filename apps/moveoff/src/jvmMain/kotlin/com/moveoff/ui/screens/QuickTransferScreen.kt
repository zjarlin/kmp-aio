package com.moveoff.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
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
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.datatransfer.Transferable
import com.moveoff.model.TransferRecord
import com.moveoff.model.TransferStatus
import com.moveoff.progress.*
import com.moveoff.ui.MainViewModel
import com.moveoff.ui.components.EmptyState
import com.moveoff.ui.components.FileItemCard
import kotlinx.coroutines.launch
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuickTransferScreen(viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var isDragging by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isTransferring by remember { mutableStateOf(false) }
    var transferComplete by remember { mutableStateOf(false) }

    val dragAndDropTarget = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                isDragging = true
            }

            override fun onEnded(event: DragAndDropEvent) {
                isDragging = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                isDragging = false
                return try {
                    // Use java.awt.dnd.DropTargetDragEvent to get transferable
                    val dtde = event.javaClass.getMethod("getNativeEvent").invoke(event)
                        as? java.awt.dnd.DropTargetDropEvent
                    val transferable = dtde?.transferable
                    if (transferable?.isDataFlavorSupported(DataFlavor.javaFileListFlavor) == true) {
                        val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
                        selectedFiles = files.filterIsInstance<File>()
                        true
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    println("Failed to handle drop: ${e.message}")
                    e.printStackTrace()
                    false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
        Text(
            text = "快速迁移",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "拖拽文件到下方区域，或点击选择文件",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Drop Zone
        if (selectedFiles.isEmpty()) {
            DropZone(
                isDragging = isDragging,
                dragAndDropTarget = dragAndDropTarget,
                onFilesSelected = { files ->
                    selectedFiles = files
                }
            )
        } else {
            SelectedFilesList(
                files = selectedFiles,
                onClear = { selectedFiles = emptyList() },
                onRemoveFile = { file ->
                    selectedFiles = selectedFiles - file
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Server Selection
        if (selectedFiles.isNotEmpty()) {
            ServerSelectionCard(
                servers = settings.servers,
                selectedServer = settings.servers.firstOrNull(),
                onServerSelected = { /* Handle server selection */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { selectedFiles = emptyList() },
                    enabled = !isTransferring
                ) {
                    Text("清空")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { showConfirmDialog = true },
                    enabled = settings.servers.isNotEmpty() && !isTransferring,
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开始迁移")
                }
            }
        }

        // Confirm Dialog
        if (showConfirmDialog) {
            TransferConfirmDialog(
                fileCount = selectedFiles.size,
                totalSize = selectedFiles.sumOf { it.length() },
                onConfirm = {
                    showConfirmDialog = false
                    isTransferring = true
                    scope.launch {
                        // Simulate transfer
                        simulateTransfer(selectedFiles, viewModel.progressTracker)
                        isTransferring = false
                        transferComplete = true
                    }
                },
                onDismiss = { showConfirmDialog = false }
            )
        }

        // Transfer Complete Dialog
        if (transferComplete) {
            AlertDialog(
                onDismissRequest = { transferComplete = false },
                icon = { Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) },
                title = { Text("迁移完成") },
                text = { Text("成功迁移 ${selectedFiles.size} 个文件") },
                confirmButton = {
                    TextButton(onClick = {
                        transferComplete = false
                        selectedFiles = emptyList()
                    }) {
                        Text("确定")
                    }
                }
            )
        }
    }
}

@Composable
fun DropZone(
    isDragging: Boolean,
    dragAndDropTarget: DragAndDropTarget,
    onFilesSelected: (List<File>) -> Unit
) {
    val backgroundColor = if (isDragging) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isDragging) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    @OptIn(ExperimentalFoundationApi::class)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .dragAndDropTarget(
                shouldStartDragAndDrop = { true },
                target = dragAndDropTarget
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isDragging) Icons.Default.KeyboardArrowDown else Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isDragging) "释放以上传文件" else "拖拽文件到此处",
                style = MaterialTheme.typography.titleMedium,
                color = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "或点击选择文件",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun SelectedFilesList(
    files: List<File>,
    onClear: () -> Unit,
    onRemoveFile: (File) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "已选择 ${files.size} 个文件",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                TextButton(onClick = onClear) {
                    Text("清空")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                items(files) { file ->
                    FileItemCard(
                        fileName = file.name,
                        fileSize = file.length(),
                        onRemove = { onRemoveFile(file) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ServerSelectionCard(
    servers: List<com.moveoff.model.ServerConfig>,
    selectedServer: com.moveoff.model.ServerConfig?,
    onServerSelected: (com.moveoff.model.ServerConfig) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "目标服务器",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (servers.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.AccountBox,
                    message = "暂无服务器配置",
                    actionText = "去添加服务器",
                    onAction = { /* Navigate to server management */ }
                )
            } else {
                servers.forEach { server ->
                    val isSelected = server.id == selectedServer?.id
                    ServerItem(
                        server = server,
                        isSelected = isSelected,
                        onClick = { onServerSelected(server) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ServerItem(
    server: com.moveoff.model.ServerConfig,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AccountBox,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = server.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${server.username}@${server.host}:${server.port}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TransferConfirmDialog(
    fileCount: Int,
    totalSize: Long,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Info, null) },
        title = { Text("确认迁移") },
        text = {
            Column {
                Text("即将迁移以下文件到远程服务器：")
                Spacer(modifier = Modifier.height(8.dp))
                Text("• 文件数量: $fileCount")
                Text("• 总大小: ${ProgressFormatter.formatBytes(totalSize)}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "迁移完成后，本地文件将根据设置进行处理。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("确认")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private suspend fun simulateTransfer(
    files: List<File>,
    progressTracker: ProgressTracker
) {
    files.forEach { file ->
        val taskId = "upload_${System.currentTimeMillis()}_${file.name}"
        val totalBytes = file.length()

        // Simulate each stage
        val stages = listOf(
            TransferStage.PRECHECK to 500L,
            TransferStage.SCAN_LOCAL to 300L,
            TransferStage.TRANSFER to 2000L,
            TransferStage.VERIFY to 500L,
            TransferStage.FINALIZE to 200L
        )

        stages.forEach { (stage, duration) ->
            val steps = 10
            val stepDuration = duration / steps

            repeat(steps) { step ->
                val progress = (step + 1).toDouble() / steps
                val transferred = if (stage == TransferStage.TRANSFER) {
                    (totalBytes * progress).toLong()
                } else totalBytes

                progressTracker.update(
                    StageUpdate(
                        taskId = taskId,
                        fileName = file.name,
                        stage = stage,
                        stageProgress = progress,
                        transferredBytes = transferred,
                        totalBytes = totalBytes,
                        speedBytesPerSec = if (stage == TransferStage.TRANSFER) 1024 * 1024L else 0L,
                        etaSeconds = if (stage == TransferStage.TRANSFER) 5L else 0L
                    )
                )
                kotlinx.coroutines.delay(stepDuration)
            }
        }
    }
}
