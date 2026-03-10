package com.moveoff.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moveoff.db.*
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 文件项视图模型
 */
data class FileItemViewModel(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val localSize: Long?,
    val remoteSize: Long?,
    val syncState: SyncState,
    val lastModified: Date?,
    val extension: String
)

/**
 * 文件管理器状态
 */
interface FileManagerState {
    val files: List<FileItemViewModel>
    val isLoading: Boolean
    val currentPath: String
    val selectedFiles: Set<String>
    val viewMode: ViewMode
    val sortBy: SortBy
    val searchQuery: String

    fun onFileClick(file: FileItemViewModel)
    fun onFileDoubleClick(file: FileItemViewModel)
    fun onFileRightClick(file: FileItemViewModel, offset: androidx.compose.ui.geometry.Offset)
    fun onPathChange(path: String)
    fun onToggleSelection(file: FileItemViewModel)
    fun onSelectAll()
    fun onClearSelection()
    fun onViewModeChange(mode: ViewMode)
    fun onSortChange(sort: SortBy)
    fun onSearchChange(query: String)
    fun onSyncFile(path: String)
    fun onDeleteFile(path: String)
    fun onShowInFolder(path: String)
}

enum class ViewMode { LIST, GRID }
enum class SortBy { NAME, SIZE, DATE, STATE }

/**
 * 同步状态图标
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncStatusIcon(
    state: SyncState,
    modifier: Modifier = Modifier
) {
    val (icon, color, tooltip) = when (state) {
        SyncState.SYNCED -> Triple(
            Icons.Default.CheckCircle,
            Color(0xFF4CAF50), // 绿色
            "已同步"
        )
        SyncState.PENDING_UPLOAD -> Triple(
            Icons.Default.Upload,
            Color(0xFF2196F3), // 蓝色
            "等待上传"
        )
        SyncState.PENDING_DOWNLOAD -> Triple(
            Icons.Default.Download,
            Color(0xFF2196F3), // 蓝色
            "等待下载"
        )
        SyncState.CONFLICT -> Triple(
            Icons.Default.Warning,
            Color(0xFFFF9800), // 橙色
            "冲突"
        )
        SyncState.ERROR -> Triple(
            Icons.Default.Error,
            Color(0xFFF44336), // 红色
            "错误"
        )
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(tooltip)
            }
        },
        state = rememberTooltipState()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltip,
            tint = color,
            modifier = modifier.size(20.dp)
        )
    }
}

/**
 * 文件列表项
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItem(
    file: FileItemViewModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onRightClick: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(backgroundColor)
            .combinedClickable(
                onClick = onClick,
                onDoubleClick = onDoubleClick,
                onLongClick = { onRightClick(Offset.Zero) }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 文件图标
        FileIcon(file, modifier = Modifier.size(40.dp))

        Spacer(modifier = Modifier.width(16.dp))

        // 文件名
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatFileSize(file.localSize ?: file.remoteSize ?: 0),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 修改时间
        file.lastModified?.let {
            Text(
                text = formatDate(it),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(120.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 同步状态图标
        SyncStatusIcon(state = file.syncState)
    }
}

/**
 * 文件图标
 */
@Composable
fun FileIcon(
    file: FileItemViewModel,
    modifier: Modifier = Modifier
) {
    val icon = when {
        file.isDirectory -> Icons.Default.Folder
        else -> getFileTypeIcon(file.extension)
    }

    val iconColor = when {
        file.isDirectory -> Color(0xFFFFB74D) // 文件夹黄色
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(iconColor.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 获取文件类型图标
 */
@Composable
fun getFileTypeIcon(extension: String) = when (extension.lowercase()) {
    "jpg", "jpeg", "png", "gif", "bmp", "webp" -> Icons.Default.Image
    "mp4", "avi", "mkv", "mov", "wmv" -> Icons.Default.PlayCircle
    "mp3", "wav", "flac", "aac", "ogg" -> Icons.Default.MusicNote
    "pdf" -> Icons.Default.PictureAsPdf
    "doc", "docx" -> Icons.Default.Description
    "xls", "xlsx" -> Icons.Default.TableChart
    "ppt", "pptx" -> Icons.Default.Slideshow
    "zip", "rar", "7z", "tar", "gz" -> Icons.Default.FolderZip
    "txt", "md", "log" -> Icons.Default.TextSnippet
    "json", "xml", "yaml", "yml" -> Icons.Default.Code
    else -> Icons.Default.InsertDriveFile
}

/**
 * 文件列表视图
 */
@Composable
fun FileListView(
    files: List<FileItemViewModel>,
    selectedFiles: Set<String>,
    onFileClick: (FileItemViewModel) -> Unit,
    onFileDoubleClick: (FileItemViewModel) -> Unit,
    onFileRightClick: (FileItemViewModel, androidx.compose.ui.geometry.Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        items(files, key = { it.path }) { file ->
            FileListItem(
                file = file,
                isSelected = file.path in selectedFiles,
                onClick = { onFileClick(file) },
                onDoubleClick = { onFileDoubleClick(file) },
                onRightClick = { offset -> onFileRightClick(file, offset) }
            )

            Divider(
                modifier = Modifier.padding(start = 72.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )
        }
    }
}

/**
 * 文件网格项
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileGridItem(
    file: FileItemViewModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onRightClick: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when (file.syncState) {
        SyncState.SYNCED -> Color(0xFF4CAF50)
        SyncState.PENDING_UPLOAD, SyncState.PENDING_DOWNLOAD -> Color(0xFF2196F3)
        SyncState.CONFLICT -> Color(0xFFFF9800)
        SyncState.ERROR -> Color(0xFFF44336)
    }

    Column(
        modifier = modifier
            .aspectRatio(0.8f)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else borderColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onDoubleClick = onDoubleClick,
                onLongClick = { onRightClick(Offset.Zero) }
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 文件图标
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            FileIcon(
                file = file,
                modifier = Modifier.size(64.dp)
            )

            // 状态徽章
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                SyncStatusIcon(
                    state = file.syncState,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 文件名
        Text(
            text = file.name,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )

        // 文件大小
        Text(
            text = formatFileSize(file.localSize ?: file.remoteSize ?: 0),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 文件网格视图
 */
@Composable
fun FileGridView(
    files: List<FileItemViewModel>,
    selectedFiles: Set<String>,
    onFileClick: (FileItemViewModel) -> Unit,
    onFileDoubleClick: (FileItemViewModel) -> Unit,
    onFileRightClick: (FileItemViewModel, androidx.compose.ui.geometry.Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val columns = 4

    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        items(files.chunked(columns)) { rowFiles ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowFiles.forEach { file ->
                    FileGridItem(
                        file = file,
                        isSelected = file.path in selectedFiles,
                        onClick = { onFileClick(file) },
                        onDoubleClick = { onFileDoubleClick(file) },
                        onRightClick = { offset -> onFileRightClick(file, offset) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 填充空白
                repeat(columns - rowFiles.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * 路径面包屑
 */
@Composable
fun PathBreadcrumb(
    currentPath: String,
    onPathClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val parts = currentPath.split("/").filter { it.isNotEmpty() }

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 根目录
        TextButton(
            onClick = { onPathClick("") }
        ) {
            Icon(Icons.Default.Home, contentDescription = "根目录")
        }

        var accumulatedPath = ""
        parts.forEachIndexed { index, part ->
            accumulatedPath += "/$part"

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextButton(
                onClick = { onPathClick(accumulatedPath) }
            ) {
                Text(part)
            }
        }
    }
}

/**
 * 文件管理器工具栏
 */
@Composable
fun FileManagerToolbar(
    viewMode: ViewMode,
    sortBy: SortBy,
    searchQuery: String,
    onViewModeChange: (ViewMode) -> Unit,
    onSortChange: (SortBy) -> Unit,
    onSearchChange: (String) -> Unit,
    onSyncAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 搜索框
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("搜索文件...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(16.dp))

        // 排序下拉
        var sortExpanded by remember { mutableStateOf(false) }
        Box {
            TextButton(
                onClick = { sortExpanded = true }
            ) {
                Icon(Icons.Default.Sort, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("排序")
            }

            DropdownMenu(
                expanded = sortExpanded,
                onDismissRequest = { sortExpanded = false }
            ) {
                SortBy.entries.forEach { sort ->
                    DropdownMenuItem(
                        text = { Text(sort.toDisplayName()) },
                        onClick = {
                            onSortChange(sort)
                            sortExpanded = false
                        },
                        leadingIcon = if (sort == sortBy) {
                            { Icon(Icons.Default.Check, null) }
                        } else null
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 视图切换
        IconButton(onClick = { onViewModeChange(ViewMode.LIST) }) {
            Icon(
                imageVector = Icons.Default.ViewList,
                contentDescription = "列表视图",
                tint = if (viewMode == ViewMode.LIST) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        IconButton(onClick = { onViewModeChange(ViewMode.GRID) }) {
            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = "网格视图",
                tint = if (viewMode == ViewMode.GRID) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 全部同步按钮
        Button(onClick = onSyncAll) {
            Icon(Icons.Default.Sync, null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("全部同步")
        }
    }
}

/**
 * 文件右键菜单
 */
@Composable
fun FileContextMenu(
    file: FileItemViewModel?,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onSync: () -> Unit,
    onDelete: () -> Unit,
    onShowInFolder: () -> Unit,
    onResolveConflict: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded && file != null,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        file?.let {
            // 同步操作
            if (it.syncState != SyncState.SYNCED) {
                DropdownMenuItem(
                    text = { Text("立即同步") },
                    onClick = {
                        onSync()
                        onDismiss()
                    },
                    leadingIcon = { Icon(Icons.Default.Sync, null) }
                )
            }

            // 解决冲突
            if (it.syncState == SyncState.CONFLICT) {
                DropdownMenuItem(
                    text = { Text("解决冲突") },
                    onClick = {
                        onResolveConflict()
                        onDismiss()
                    },
                    leadingIcon = { Icon(Icons.Default.MergeType, null) }
                )
            }

            Divider()

            // 在文件夹中显示
            DropdownMenuItem(
                text = { Text("在文件夹中显示") },
                onClick = {
                    onShowInFolder()
                    onDismiss()
                },
                leadingIcon = { Icon(Icons.Default.FolderOpen, null) }
            )

            Divider()

            // 删除
            DropdownMenuItem(
                text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    onDelete()
                    onDismiss()
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}

/**
 * 空状态视图
 */
@Composable
fun EmptyFilesView(
    onAddFiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "暂无文件",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "点击添加文件或拖拽文件到此处",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onAddFiles) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("添加文件")
        }
    }
}

// ========== 辅助函数 ==========

private fun formatFileSize(size: Long): String {
    return when {
        size >= 1024L * 1024 * 1024 -> "%.2f GB".format(size / (1024.0 * 1024 * 1024))
        size >= 1024L * 1024 -> "%.2f MB".format(size / (1024.0 * 1024))
        size >= 1024L -> "%.2f KB".format(size / 1024.0)
        else -> "$size B"
    }
}

private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(date)
}

private fun SortBy.toDisplayName(): String = when (this) {
    SortBy.NAME -> "名称"
    SortBy.SIZE -> "大小"
    SortBy.DATE -> "修改时间"
    SortBy.STATE -> "同步状态"
}
