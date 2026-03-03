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
import com.moveoff.model.RemoteFile
import com.moveoff.model.ServerConfig
import com.moveoff.progress.ProgressFormatter
import com.moveoff.ui.MainViewModel
import com.moveoff.ui.components.EmptyState
import kotlinx.coroutines.launch

@Composable
fun FileManagerScreen(viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedServer by remember { mutableStateOf<ServerConfig?>(null) }
    var currentPath by remember { mutableStateOf("") }
    var files by remember { mutableStateOf<List<RemoteFile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFile by remember { mutableStateOf<RemoteFile?>(null) }
    var showFileActions by remember { mutableStateOf(false) }

    // Load files when server or path changes
    LaunchedEffect(selectedServer, currentPath) {
        selectedServer?.let { server ->
            isLoading = true
            // Simulate loading files
            kotlinx.coroutines.delay(500)
            files = generateSampleFiles(currentPath)
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
        Text(
            text = "文件管理",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "管理远程服务器上的文件",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (settings.servers.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Info,
                message = "没有可用的服务器",
                description = "请先添加服务器以管理远程文件",
                actionText = "去添加服务器",
                onAction = { /* Navigate to server management */ }
            )
        } else {
            // Server Selection
            ServerSelector(
                servers = settings.servers,
                selectedServer = selectedServer,
                onServerSelected = {
                    selectedServer = it
                    currentPath = it.remoteRootPath
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedServer != null) {
                // Toolbar
                FileManagerToolbar(
                    currentPath = currentPath,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onNavigateUp = {
                        if (currentPath != selectedServer?.remoteRootPath) {
                            currentPath = currentPath.substringBeforeLast("/", selectedServer?.remoteRootPath ?: "")
                        }
                    },
                    onRefresh = {
                        scope.launch {
                            isLoading = true
                            kotlinx.coroutines.delay(500)
                            files = generateSampleFiles(currentPath)
                            isLoading = false
                        }
                    },
                    onCreateFolder = { /* TODO */ },
                    onUpload = { /* TODO */ }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // File List
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    val filteredFiles = files.filter {
                        searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true)
                    }

                    if (filteredFiles.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.Info,
                            message = "文件夹为空",
                            description = if (searchQuery.isNotEmpty()) "没有找到匹配的文件" else "此文件夹中没有文件"
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredFiles) { file ->
                                FileListItem(
                                    file = file,
                                    isSelected = file == selectedFile,
                                    onClick = {
                                        selectedFile = file
                                        if (file.isDirectory) {
                                            currentPath = file.path
                                        } else {
                                            showFileActions = true
                                        }
                                    },
                                    onDoubleClick = {
                                        if (file.isDirectory) {
                                            currentPath = file.path
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // File Actions Dialog
    if (showFileActions && selectedFile != null) {
        FileActionsDialog(
            file = selectedFile!!,
            onDismiss = { showFileActions = false },
            onDownload = { /* TODO */ },
            onDelete = { /* TODO */ },
            onRename = { /* TODO */ },
            onPreview = { /* TODO */ }
        )
    }
}

@Composable
fun ServerSelector(
    servers: List<ServerConfig>,
    selectedServer: ServerConfig?,
    onServerSelected: (ServerConfig) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selectedServer?.name ?: "选择服务器")
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            servers.forEach { server ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(server.name, fontWeight = FontWeight.Medium)
                            Text(
                                "${server.host}:${server.port}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    onClick = {
                        onServerSelected(server)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.AccountBox, null)
                    }
                )
            }
        }
    }
}

@Composable
fun FileManagerToolbar(
    currentPath: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNavigateUp: () -> Unit,
    onRefresh: () -> Unit,
    onCreateFolder: () -> Unit,
    onUpload: () -> Unit
) {
    Column {
        // Path and Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Navigation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.Default.KeyboardArrowUp, "上级目录")
                }
                Text(
                    text = currentPath,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // Actions
            Row {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, "刷新")
                }
                IconButton(onClick = onCreateFolder) {
                    Icon(Icons.Default.Add, "新建文件夹")
                }
                IconButton(onClick = onUpload) {
                    Icon(Icons.Default.Add, "上传")
                }
            }
        }

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("搜索文件...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun FileListItem(
    file: RemoteFile,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (file.isDirectory) Icons.Default.DateRange else Icons.Default.Info,
            contentDescription = null,
            tint = if (file.isDirectory) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            },
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
            if (!file.isDirectory) {
                Text(
                    text = ProgressFormatter.formatBytes(file.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        Text(
            text = ProgressFormatter.formatDuration((System.currentTimeMillis() - file.modifiedTime) / 1000) + "前",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun FileActionsDialog(
    file: RemoteFile,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onPreview: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = if (file.isDirectory) Icons.Default.DateRange else Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        },
        title = { Text(file.name) },
        text = {
            Column {
                if (!file.isDirectory) {
                    DetailRow("大小", ProgressFormatter.formatBytes(file.size))
                }
                DetailRow("修改时间", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(file.modifiedTime)))
                DetailRow("路径", file.path)
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onDelete) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除")
                }
                Button(onClick = onDownload) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("下载")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// Sample data generator for demo
private fun generateSampleFiles(path: String): List<RemoteFile> {
    val extensions = listOf("txt", "pdf", "jpg", "png", "mp4", "zip")
    val names = listOf("文档", "图片", "视频", "备份", "项目", "资料")

    return (1..15).map { index ->
        val isDir = index % 4 == 0
        val name = if (isDir) {
            "${names.random()}_${index}"
        } else {
            "${names.random()}_${index}.${extensions.random()}"
        }

        RemoteFile(
            name = name,
            path = "$path/$name",
            size = if (isDir) 0L else (1024L..1024L * 1024 * 100).random(),
            isDirectory = isDir,
            modifiedTime = System.currentTimeMillis() - (0L..86400000L * 30).random(),
            extension = if (isDir) null else name.substringAfterLast(".", "")
        )
    }
}
