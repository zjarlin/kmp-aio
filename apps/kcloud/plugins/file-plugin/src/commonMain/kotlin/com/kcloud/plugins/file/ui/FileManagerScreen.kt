package com.kcloud.plugins.file.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kcloud.plugins.file.FileConflictResolution
import com.kcloud.plugins.file.FileSyncState
import com.kcloud.plugins.file.FileWorkspaceRecord
import com.kcloud.plugins.file.FileWorkspaceRevealService
import com.kcloud.plugins.file.FileWorkspaceService
import com.kcloud.plugins.file.ui.components.EmptyFilesView
import com.kcloud.plugins.file.ui.components.FileGridView
import com.kcloud.plugins.file.ui.components.FileItemViewModel
import com.kcloud.plugins.file.ui.components.FileListView
import com.kcloud.plugins.file.ui.components.FileManagerToolbar
import com.kcloud.plugins.file.ui.components.PathBreadcrumb
import com.kcloud.plugins.file.ui.components.SortBy
import com.kcloud.plugins.file.ui.components.ViewMode
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun FileManagerScreen(
    service: FileWorkspaceService = koinInject(),
    revealService: FileWorkspaceRevealService = koinInject()
) {
    val scope = rememberCoroutineScope()
    val fileRecords by service.records.collectAsState()

    var currentPath by remember { mutableStateOf("") }
    var selectedPath by remember { mutableStateOf<String?>(null) }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    var sortBy by remember { mutableStateOf(SortBy.NAME) }
    var searchQuery by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("当前页面直接读取本地数据库记录；目录由路径前缀虚拟聚合。") }

    val visibleEntries = remember(fileRecords, currentPath, searchQuery, sortBy) {
        buildVisibleEntries(
            records = fileRecords,
            currentPath = currentPath,
            searchQuery = searchQuery,
            sortBy = sortBy
        )
    }
    val selectedEntry = visibleEntries.firstOrNull { it.path == selectedPath }
    val selectedRecord = fileRecords.firstOrNull { it.path == selectedPath }

    LaunchedEffect(visibleEntries) {
        if (selectedPath != null && visibleEntries.none { it.path == selectedPath }) {
            selectedPath = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("文件管理", style = MaterialTheme.typography.headlineSmall)
        Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant)

        FileManagerToolbar(
            viewMode = viewMode,
            sortBy = sortBy,
            searchQuery = searchQuery,
            onViewModeChange = { viewMode = it },
            onSortChange = { sortBy = it },
            onSearchChange = { searchQuery = it },
            onSyncAll = {
                scope.launch {
                    status = service.triggerSync().message
                }
            }
        )

        PathBreadcrumb(
            currentPath = currentPath,
            onPathClick = {
                currentPath = it.trim('/')
                selectedPath = null
            },
            modifier = Modifier.fillMaxWidth()
        )

        selectedEntry?.let { entry ->
            SelectedEntryCard(
                entry = entry,
                selectedRecord = selectedRecord,
                onShowInFolder = {
                    status = revealService.revealPath(entry.path).message
                },
                onResolveConflict = { resolution ->
                    scope.launch {
                        status = service.resolveConflict(entry.path, resolution).message
                    }
                },
                onRemoveRecord = {
                    val result = service.removeRecord(entry.path)
                    status = result.message
                    if (result.success) {
                        selectedPath = null
                    }
                }
            )
        }

        if (visibleEntries.isEmpty()) {
            EmptyFilesView(
                onAddFiles = {
                    status = "当前页面还没接文件选择器；可以先通过拖拽上传或让同步目录生成记录。"
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            when (viewMode) {
                ViewMode.LIST -> FileListView(
                    files = visibleEntries,
                    selectedFiles = selectedPath?.let(::setOf).orEmpty(),
                    onFileClick = { file ->
                        selectedPath = file.path
                    },
                    onFileDoubleClick = { file ->
                        if (file.isDirectory) {
                            currentPath = file.path
                            selectedPath = null
                            status = "已进入目录：${file.path}"
                        } else {
                            status = revealService.revealPath(file.path).message
                        }
                    },
                    onFileRightClick = { file, _ ->
                        selectedPath = file.path
                        status = "右键菜单暂未恢复，先用上方操作卡片处理 ${file.name}"
                    },
                    modifier = Modifier.fillMaxSize()
                )

                ViewMode.GRID -> FileGridView(
                    files = visibleEntries,
                    selectedFiles = selectedPath?.let(::setOf).orEmpty(),
                    onFileClick = { file ->
                        selectedPath = file.path
                    },
                    onFileDoubleClick = { file ->
                        if (file.isDirectory) {
                            currentPath = file.path
                            selectedPath = null
                            status = "已进入目录：${file.path}"
                        } else {
                            status = revealService.revealPath(file.path).message
                        }
                    },
                    onFileRightClick = { file, _ ->
                        selectedPath = file.path
                        status = "右键菜单暂未恢复，先用上方操作卡片处理 ${file.name}"
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun SelectedEntryCard(
    entry: FileItemViewModel,
    selectedRecord: FileWorkspaceRecord?,
    onShowInFolder: () -> Unit,
    onResolveConflict: (FileConflictResolution) -> Unit,
    onRemoveRecord: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(entry.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("路径：${entry.path}", style = MaterialTheme.typography.bodySmall)
            Text(
                text = if (entry.isDirectory) "目录（虚拟聚合节点）" else "文件 · 状态 ${entry.syncState.name}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onShowInFolder, enabled = !entry.isDirectory) {
                    Text("在文件夹中显示")
                }
                Button(onClick = onRemoveRecord, enabled = !entry.isDirectory) {
                    Text("移除记录")
                }
            }

            if (selectedRecord?.syncState == FileSyncState.CONFLICT) {
                Text("检测到冲突，可直接选择处理策略：")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { onResolveConflict(FileConflictResolution.USE_LOCAL) }) {
                        Text("保留本地")
                    }
                    Button(onClick = { onResolveConflict(FileConflictResolution.USE_REMOTE) }) {
                        Text("使用远程")
                    }
                    Button(onClick = { onResolveConflict(FileConflictResolution.KEEP_BOTH) }) {
                        Text("两边都保留")
                    }
                }
            }
        }
    }
}

private fun buildVisibleEntries(
    records: List<FileWorkspaceRecord>,
    currentPath: String,
    searchQuery: String,
    sortBy: SortBy
): List<FileItemViewModel> {
    val normalizedPath = currentPath.trim('/')
    val prefix = normalizedPath.takeIf { it.isNotBlank() }?.let { "$it/" }.orEmpty()
    val directoryChildren = linkedMapOf<String, MutableList<FileWorkspaceRecord>>()
    val fileChildren = mutableListOf<FileItemViewModel>()

    records.forEach { record ->
        if (prefix.isNotEmpty() && !record.path.startsWith(prefix)) {
            return@forEach
        }

        val relative = if (prefix.isEmpty()) record.path else record.path.removePrefix(prefix)
        if (relative.isBlank()) {
            return@forEach
        }

        val slashIndex = relative.indexOf('/')
        if (slashIndex >= 0) {
            val directoryName = relative.substring(0, slashIndex)
            val directoryPath = listOf(normalizedPath, directoryName)
                .filter { it.isNotBlank() }
                .joinToString("/")
            directoryChildren.getOrPut(directoryPath) { mutableListOf() }.add(record)
        } else {
            fileChildren += record.toViewModel()
        }
    }

    val directoryEntries = directoryChildren.map { (path, childRecords) ->
        FileItemViewModel(
            path = path,
            name = path.substringAfterLast('/'),
            isDirectory = true,
            localSize = childRecords.mapNotNull { it.localSize }.sum().takeIf { it > 0 },
            remoteSize = childRecords.mapNotNull { it.remoteSize }.sum().takeIf { it > 0 },
            syncState = aggregateSyncState(childRecords.map { it.syncState }),
            lastModified = childRecords.mapNotNull { it.lastSyncTime }.maxOrNull(),
            extension = ""
        )
    }

    val filtered = (directoryEntries + fileChildren).filter { item ->
        if (searchQuery.isBlank()) {
            true
        } else {
            item.name.contains(searchQuery, ignoreCase = true) ||
                item.path.contains(searchQuery, ignoreCase = true)
        }
    }

    return filtered.sortedWith(compareBy<FileItemViewModel> { !it.isDirectory }.then(compareValuesBy(sortBy)))
}

private fun compareValuesBy(sortBy: SortBy): Comparator<FileItemViewModel> {
    return Comparator { left, right ->
        when (sortBy) {
            SortBy.NAME -> left.name.compareTo(right.name, ignoreCase = true)
            SortBy.SIZE -> (right.localSize ?: right.remoteSize ?: 0L)
                .compareTo(left.localSize ?: left.remoteSize ?: 0L)
            SortBy.DATE -> (right.lastModified ?: 0L).compareTo(left.lastModified ?: 0L)
            SortBy.STATE -> left.syncState.name.compareTo(right.syncState.name)
        }
    }
}

private fun FileWorkspaceRecord.toViewModel(): FileItemViewModel {
    return FileItemViewModel(
        path = path,
        name = path.substringAfterLast('/'),
        isDirectory = false,
        localSize = localSize,
        remoteSize = remoteSize,
        syncState = syncState,
        lastModified = lastSyncTime,
        extension = path.substringAfterLast('.', "")
    )
}

private fun aggregateSyncState(states: List<FileSyncState>): FileSyncState {
    return when {
        FileSyncState.CONFLICT in states -> FileSyncState.CONFLICT
        FileSyncState.ERROR in states -> FileSyncState.ERROR
        FileSyncState.PENDING_UPLOAD in states -> FileSyncState.PENDING_UPLOAD
        FileSyncState.PENDING_DOWNLOAD in states -> FileSyncState.PENDING_DOWNLOAD
        else -> FileSyncState.SYNCED
    }
}
