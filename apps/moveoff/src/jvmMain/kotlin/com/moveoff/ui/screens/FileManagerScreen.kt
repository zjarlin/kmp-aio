package com.moveoff.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moveoff.db.*
import com.moveoff.state.AppStateManager
import com.moveoff.sync.SyncEngineManager
import com.moveoff.ui.MainViewModel
import com.moveoff.ui.components.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

/**
 * 文件管理器屏幕 - 新版
 */
@Composable
fun FileManagerScreen(viewModel: MainViewModel = MainViewModel()) {
    val scope = rememberCoroutineScope()
    val db = remember { DatabaseManager.get() }

    // 状态
    var files by remember { mutableStateOf<List<FileItemViewModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    var sortBy by remember { mutableStateOf(SortBy.NAME) }
    var searchQuery by remember { mutableStateOf("") }
    var currentPath by remember { mutableStateOf("") }
    var selectedFiles by remember { mutableStateOf<Set<String>>(emptySet()) }

    // 右键菜单状态
    var contextMenuExpanded by remember { mutableStateOf(false) }
    var contextMenuFile by remember { mutableStateOf<FileItemViewModel?>(null) }
    var contextMenuOffset by remember { mutableStateOf(Offset.Zero) }

    // 监听数据库变化
    LaunchedEffect(Unit) {
        db.observeFileRecords().collectLatest { records ->
            val syncRoot = getSyncRoot()

            val mappedFiles = records.map { record ->
                val file = File(syncRoot, record.path)
                FileItemViewModel(
                    path = record.path,
                    name = file.name,
                    isDirectory = file.isDirectory,
                    localSize = record.localSize,
                    remoteSize = record.remoteSize,
                    syncState = record.syncState,
                    lastModified = record.localMtime?.let { Date(it) },
                    extension = file.extension
                )
            }

            // 应用搜索过滤
            files = if (searchQuery.isNotEmpty()) {
                mappedFiles.filter { it.name.contains(searchQuery, ignoreCase = true) }
            } else {
                mappedFiles
            }.sortedWith(getSortComparator(sortBy))
        }
    }

    // 使用带拖拽上传的容器
    FileManagerWithDragDrop {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 标题区域
            FileManagerHeader()

            // 工具栏
            FileManagerToolbar(
                viewMode = viewMode,
                sortBy = sortBy,
                searchQuery = searchQuery,
                onViewModeChange = { viewMode = it },
                onSortChange = { sortBy = it },
                onSearchChange = { searchQuery = it },
                onSyncAll = {
                    scope.launch {
                        try {
                            isLoading = true
                            val syncEngine = SyncEngineManager.get()
                            syncEngine.syncNow()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            isLoading = false
                        }
                    }
                }
            )

        // 路径面包屑
        if (currentPath.isNotEmpty()) {
            PathBreadcrumb(
                currentPath = currentPath,
                onPathClick = { path ->
                    currentPath = path
                }
            )
        }

        // 内容区域
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                files.isEmpty() -> {
                    EmptyFilesView(
                        onAddFiles = {
                            // TODO: 打开文件选择器
                        }
                    )
                }
                viewMode == ViewMode.LIST -> {
                    FileListView(
                        files = files,
                        selectedFiles = selectedFiles,
                        onFileClick = { file ->
                            selectedFiles = if (file.path in selectedFiles) {
                                selectedFiles - file.path
                            } else {
                                selectedFiles + file.path
                            }
                        },
                        onFileDoubleClick = { file ->
                            if (file.isDirectory) {
                                currentPath = file.path
                            }
                        },
                        onFileRightClick = { file, offset ->
                            contextMenuFile = file
                            contextMenuOffset = offset
                            contextMenuExpanded = true
                        }
                    )
                }
                else -> {
                    FileGridView(
                        files = files,
                        selectedFiles = selectedFiles,
                        onFileClick = { file ->
                            selectedFiles = if (file.path in selectedFiles) {
                                selectedFiles - file.path
                            } else {
                                selectedFiles + file.path
                            }
                        },
                        onFileDoubleClick = { file ->
                            if (file.isDirectory) {
                                currentPath = file.path
                            }
                        },
                        onFileRightClick = { file, offset ->
                            contextMenuFile = file
                            contextMenuOffset = offset
                            contextMenuExpanded = true
                        }
                    )
                }
            }
        }
    }
    } // FileManagerWithDragDrop 结束

    // 右键菜单
    FileContextMenu(
        file = contextMenuFile,
        expanded = contextMenuExpanded,
        onDismiss = { contextMenuExpanded = false },
        onSync = {
            contextMenuFile?.let { file ->
                scope.launch {
                    try {
                        val syncEngine = SyncEngineManager.get()
                        // TODO: 同步单个文件
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        },
        onDelete = {
            contextMenuFile?.let { file ->
                // TODO: 删除确认对话框
            }
        },
        onShowInFolder = {
            contextMenuFile?.let { file ->
                // TODO: 打开系统文件管理器
            }
        },
        onResolveConflict = {
            contextMenuFile?.let { file ->
                // TODO: 打开冲突解决对话框
            }
        }
    )
}

/**
 * 文件管理器头部
 */
@Composable
private fun FileManagerHeader() {
    Column(
        modifier = Modifier.padding(24.dp)
    ) {
        Text(
            text = "文件管理",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "管理本地和远程同步的文件",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * 统计信息卡片
 */
@Composable
private fun SyncStatsCard(modifier: Modifier = Modifier) {
    val db = remember { DatabaseManager.get() }
    var stats by remember { mutableStateOf<DatabaseStats?>(null) }

    LaunchedEffect(Unit) {
        stats = db.getStats()
    }

    Card(
        modifier = modifier.padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            stats?.let {
                StatItem("总文件", it.totalFiles.toString())
                StatItem("已同步", it.syncedFiles.toString(), Color(0xFF4CAF50))
                StatItem("待上传", it.pendingUploads.toString(), Color(0xFF2196F3))
                StatItem("待下载", it.pendingDownloads.toString(), Color(0xFF2196F3))
                StatItem("冲突", it.conflicts.toString(), Color(0xFFFF9800))
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 获取排序比较器
 */
private fun getSortComparator(sortBy: SortBy): Comparator<FileItemViewModel> = when (sortBy) {
    SortBy.NAME -> compareBy { it.name.lowercase() }
    SortBy.SIZE -> compareByDescending { it.localSize ?: it.remoteSize ?: 0 }
    SortBy.DATE -> compareByDescending { it.lastModified }
    SortBy.STATE -> compareBy { it.syncState.ordinal }
}

/**
 * 获取同步根目录
 */
private fun getSyncRoot(): String {
    val userHome = System.getProperty("user.home")
    return File(userHome, "MoveOff").absolutePath
}
