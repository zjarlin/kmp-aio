package com.kcloud.plugins.file.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kcloud.plugins.file.FileSyncState
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class FileItemViewModel(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val localSize: Long?,
    val remoteSize: Long?,
    val syncState: FileSyncState,
    val lastModified: Long?,
    val extension: String
)

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
    fun onFileRightClick(file: FileItemViewModel, offset: Offset)
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

enum class ViewMode {
    LIST,
    GRID
}

enum class SortBy {
    NAME,
    SIZE,
    DATE,
    STATE
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SyncStatusIcon(
    state: FileSyncState,
    modifier: Modifier = Modifier
) {
    val (icon, color, tooltip) = when (state) {
        FileSyncState.SYNCED -> Triple(Icons.Default.CheckCircle, Color(0xFF4CAF50), "已同步")
        FileSyncState.PENDING_UPLOAD -> Triple(Icons.Default.Upload, Color(0xFF2196F3), "等待上传")
        FileSyncState.PENDING_DOWNLOAD -> Triple(Icons.Default.Download, Color(0xFF2196F3), "等待下载")
        FileSyncState.CONFLICT -> Triple(Icons.Default.Warning, Color(0xFFFF9800), "冲突")
        FileSyncState.ERROR -> Triple(Icons.Default.Error, Color(0xFFF44336), "错误")
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            TooltipAnchorPosition.Above
        ),
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
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
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
        FileIcon(file, modifier = Modifier.size(40.dp))
        Spacer(modifier = Modifier.width(16.dp))

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

        file.lastModified?.let {
            Text(
                text = formatTimestamp(it),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(120.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))
        SyncStatusIcon(state = file.syncState)
    }
}

@Composable
fun FileIcon(
    file: FileItemViewModel,
    modifier: Modifier = Modifier
) {
    val icon = when {
        file.isDirectory -> Icons.Default.Folder
        else -> file.extension.toFileTypeIcon()
    }

    val iconColor = if (file.isDirectory) {
        Color(0xFFFFB74D)
    } else {
        MaterialTheme.colorScheme.primary
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

private fun String.toFileTypeIcon(): ImageVector {
    return when (lowercase()) {
        "jpg", "jpeg", "png", "gif", "bmp", "webp" -> Icons.Default.Image
        "mp4", "avi", "mkv", "mov", "wmv" -> Icons.Default.PlayCircle
        "mp3", "wav", "flac", "aac", "ogg" -> Icons.Default.MusicNote
        "pdf" -> Icons.Default.PictureAsPdf
        "doc", "docx" -> Icons.Default.Description
        "xls", "xlsx" -> Icons.Default.TableChart
        "ppt", "pptx" -> Icons.Default.Slideshow
        "zip", "rar", "7z", "tar", "gz" -> Icons.Default.FolderZip
        "txt", "md", "log" -> Icons.AutoMirrored.Filled.TextSnippet
        "json", "xml", "yaml", "yml" -> Icons.Default.Code
        else -> Icons.AutoMirrored.Filled.InsertDriveFile
    }
}

@Composable
fun FileListView(
    files: List<FileItemViewModel>,
    selectedFiles: Set<String>,
    onFileClick: (FileItemViewModel) -> Unit,
    onFileDoubleClick: (FileItemViewModel) -> Unit,
    onFileRightClick: (FileItemViewModel, Offset) -> Unit,
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

            HorizontalDivider(
                modifier = Modifier.padding(start = 72.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )
        }
    }
}

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
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = when (file.syncState) {
        FileSyncState.SYNCED -> Color(0xFF4CAF50)
        FileSyncState.PENDING_UPLOAD,
        FileSyncState.PENDING_DOWNLOAD -> Color(0xFF2196F3)
        FileSyncState.CONFLICT -> Color(0xFFFF9800)
        FileSyncState.ERROR -> Color(0xFFF44336)
    }

    Column(
        modifier = modifier
            .aspectRatio(0.8f)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    borderColor.copy(alpha = 0.3f)
                },
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
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            FileIcon(
                file = file,
                modifier = Modifier.size(64.dp)
            )

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

        Text(
            text = file.name,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = formatFileSize(file.localSize ?: file.remoteSize ?: 0),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FileGridView(
    files: List<FileItemViewModel>,
    selectedFiles: Set<String>,
    onFileClick: (FileItemViewModel) -> Unit,
    onFileDoubleClick: (FileItemViewModel) -> Unit,
    onFileRightClick: (FileItemViewModel, Offset) -> Unit,
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

                repeat(columns - rowFiles.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

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
        TextButton(
            onClick = { onPathClick("") }
        ) {
            Icon(Icons.Default.Home, contentDescription = "根目录")
        }

        var accumulatedPath = ""
        parts.forEach { part ->
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
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("搜索文件...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(16.dp))

        var sortExpanded by remember { mutableStateOf(false) }
        Box {
            TextButton(
                onClick = { sortExpanded = true }
            ) {
                Icon(Icons.AutoMirrored.Filled.Sort, null)
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
                        } else {
                            null
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = { onViewModeChange(ViewMode.LIST) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ViewList,
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

        Button(onClick = onSyncAll) {
            Icon(Icons.Default.Sync, null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("全部同步")
        }
    }
}

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
            if (it.syncState != FileSyncState.SYNCED) {
                DropdownMenuItem(
                    text = { Text("立即同步") },
                    onClick = {
                        onSync()
                        onDismiss()
                    },
                    leadingIcon = { Icon(Icons.Default.Sync, null) }
                )
            }

            if (it.syncState == FileSyncState.CONFLICT) {
                DropdownMenuItem(
                    text = { Text("解决冲突") },
                    onClick = {
                        onResolveConflict()
                        onDismiss()
                    },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.MergeType, null) }
                )
            }

            HorizontalDivider()

            DropdownMenuItem(
                text = { Text("在文件夹中显示") },
                onClick = {
                    onShowInFolder()
                    onDismiss()
                },
                leadingIcon = { Icon(Icons.Default.FolderOpen, null) }
            )

            HorizontalDivider()

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

private fun formatFileSize(bytes: Long): String {
    fun formatUnit(value: Double, unit: String): String {
        val rounded = (value * 100).roundToInt() / 100.0
        return "${rounded.toString()} $unit"
    }

    return when {
        bytes >= 1024L * 1024 * 1024 -> formatUnit(bytes / (1024.0 * 1024 * 1024), "GB")
        bytes >= 1024L * 1024 -> formatUnit(bytes / (1024.0 * 1024), "MB")
        bytes >= 1024L -> formatUnit(bytes / 1024.0, "KB")
        else -> "$bytes B"
    }
}

@OptIn(ExperimentalTime::class)
private fun formatTimestamp(timestamp: Long): String {
    val dateTime = Instant
        .fromEpochMilliseconds(timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())

    return buildString {
        append(dateTime.year)
        append('-')
        append(dateTime.month.toString().padStart(2, '0'))
        append('-')
        append(dateTime.day.toString().padStart(2, '0'))
        append(' ')
        append(dateTime.hour.toString().padStart(2, '0'))
        append(':')
        append(dateTime.minute.toString().padStart(2, '0'))
    }
}

private fun SortBy.toDisplayName(): String {
    return when (this) {
        SortBy.NAME -> "名称"
        SortBy.SIZE -> "大小"
        SortBy.DATE -> "修改时间"
        SortBy.STATE -> "同步状态"
    }
}
