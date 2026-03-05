package com.moveoff.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moveoff.db.DatabaseManager
import com.moveoff.db.SyncOperation
import com.moveoff.db.SyncState
import com.moveoff.event.EventShortcuts
import kotlinx.coroutines.*
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.io.File

/**
 * 拖拽状态
 */
enum class DragState {
    NONE,       // 无拖拽
    DRAGGING,   // 拖拽中
    VALID,      // 有效的拖拽目标
    INVALID     // 无效的拖拽目标
}

/**
 * 拖拽上传区域
 */
@Composable
fun DragDropUploadArea(
    modifier: Modifier = Modifier,
    onFilesDropped: (List<File>) -> Unit = {}
) {
    var dragState by remember { mutableStateOf(DragState.NONE) }

    // 设置拖拽监听
    LaunchedEffect(Unit) {
        val window = androidx.compose.ui.window.LocalWindow.current
        window?.let { setupDropTarget(it, { state -> dragState = state }, onFilesDropped) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .border(
                width = when (dragState) {
                    DragState.VALID -> 3.dp
                    DragState.INVALID -> 3.dp
                    else -> 1.dp
                },
                color = when (dragState) {
                    DragState.VALID -> MaterialTheme.colorScheme.primary
                    DragState.INVALID -> MaterialTheme.colorScheme.error
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                when (dragState) {
                    DragState.VALID -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    DragState.INVALID -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    else -> Color.Transparent
                }
            )
    ) {
        // 拖拽覆盖层
        AnimatedVisibility(
            visible = dragState != DragState.NONE,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (dragState == DragState.VALID)
                            Icons.Default.CloudUpload
                        else
                            Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = if (dragState == DragState.VALID)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (dragState == DragState.VALID)
                            "释放以上传文件"
                        else
                            "不支持的文件类型",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 设置拖拽目标
 */
private fun setupDropTarget(
    window: java.awt.Window,
    onDragStateChange: (DragState) -> Unit,
    onFilesDropped: (List<File>) -> Unit
) {
    val dropTarget = DropTarget().apply {
        addDropTargetListener(object : DropTargetAdapter() {
            override fun dragEnter(dtde: DropTargetDragEvent?) {
                dtde ?: return
                if (isDragAcceptable(dtde)) {
                    onDragStateChange(DragState.VALID)
                    dtde.acceptDrag(java.awt.dnd.DnDConstants.ACTION_COPY)
                } else {
                    onDragStateChange(DragState.INVALID)
                    dtde.rejectDrag()
                }
            }

            override fun dragOver(dtde: DropTargetDragEvent?) {
                dtde ?: return
                if (isDragAcceptable(dtde)) {
                    onDragStateChange(DragState.VALID)
                    dtde.acceptDrag(java.awt.dnd.DnDConstants.ACTION_COPY)
                } else {
                    onDragStateChange(DragState.INVALID)
                    dtde.rejectDrag()
                }
            }

            override fun dragExit(dte: DropTargetEvent?) {
                onDragStateChange(DragState.NONE)
            }

            override fun drop(dtde: DropTargetDropEvent?) {
                dtde ?: return
                onDragStateChange(DragState.NONE)

                try {
                    dtde.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY)

                    val transferable = dtde.transferable
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        @Suppress("UNCHECKED_CAST")
                        val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>

                        // 处理文件
                        CoroutineScope(Dispatchers.IO).launch {
                            processDroppedFiles(files)
                            withContext(Dispatchers.Main) {
                                onFilesDropped(files)
                            }
                        }

                        dtde.dropComplete(true)
                    } else {
                        dtde.dropComplete(false)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    dtde.dropComplete(false)
                }
            }
        })
    }

    window.dropTarget = dropTarget
}

/**
 * 检查拖拽是否可接受
 */
private fun isDragAcceptable(dtde: DropTargetDragEvent): Boolean {
    return try {
        val transferable = dtde.transferable
        transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
    } catch (e: Exception) {
        false
    }
}

/**
 * 处理拖放的文件
 */
private suspend fun processDroppedFiles(files: List<File>) {
    val db = DatabaseManager.get()
    val syncRoot = getSyncRoot()

    files.forEach { file ->
        processFile(file, syncRoot, db, "")
    }

    EventShortcuts.notify("上传准备完成", "已添加 ${files.size} 个文件到同步队列")
}

/**
 * 递归处理文件
 */
private suspend fun processFile(
    file: File,
    syncRoot: String,
    db: com.moveoff.db.Database,
    relativePath: String
) {
    val targetPath = if (relativePath.isEmpty()) {
        file.name
    } else {
        "$relativePath/${file.name}"
    }

    if (file.isDirectory) {
        // 递归处理目录
        file.listFiles()?.forEach { child ->
            processFile(child, syncRoot, db, targetPath)
        }
    } else {
        // 复制文件到同步目录
        val targetFile = File(syncRoot, targetPath)
        targetFile.parentFile?.mkdirs()

        // 如果文件不在同步目录中，复制它
        if (file.absolutePath != targetFile.absolutePath) {
            withContext(Dispatchers.IO) {
                file.copyTo(targetFile, overwrite = true)
            }
        }

        // 更新数据库
        db.updateLocalInfo(
            path = targetPath,
            mtime = targetFile.lastModified(),
            size = targetFile.length(),
            hash = null
        )
        db.updateSyncState(targetPath, SyncState.PENDING_UPLOAD)

        // 添加到同步队列
        db.enqueueSync(targetPath, SyncOperation.UPLOAD, targetFile.length())
    }
}

/**
 * 获取同步根目录
 */
private fun getSyncRoot(): String {
    val userHome = System.getProperty("user.home")
    return File(userHome, "MoveOff").absolutePath
}

/**
 * 带拖拽上传功能的文件管理器容器
 */
@Composable
fun FileManagerWithDragDrop(
    content: @Composable () -> Unit
) {
    var showUploadOverlay by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0f) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 主内容
        content()

        // 拖拽上传层
        DragDropUploadArea(
            modifier = Modifier.fillMaxSize()
        )

        // 上传进度提示
        AnimatedVisibility(
            visible = showUploadOverlay,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "正在处理文件...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { uploadProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
