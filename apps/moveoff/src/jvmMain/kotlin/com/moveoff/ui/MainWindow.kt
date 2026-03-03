package com.moveoff.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.moveoff.model.AppSettings
import com.moveoff.model.ServerConfig
import com.moveoff.model.Theme
import com.moveoff.progress.ProgressTracker
import com.moveoff.storage.SettingsStorage
import com.moveoff.ui.screens.*
import com.moveoff.ui.theme.MoveOffTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NavigationItem(val title: String, val icon: ImageVector) {
    QUICK_TRANSFER("快速迁移", Icons.Default.Send),
    SERVER_MANAGEMENT("服务器管理", Icons.Default.AccountBox),
    FILE_MANAGER("文件管理", Icons.Default.DateRange),
    TRANSFER_HISTORY("迁移记录", Icons.Default.Info),
    SETTINGS("设置", Icons.Default.Settings)
}

class MainViewModel {
    private val _settings = MutableStateFlow(SettingsStorage.loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _selectedNavItem = MutableStateFlow(NavigationItem.QUICK_TRANSFER)
    val selectedNavItem: StateFlow<NavigationItem> = _selectedNavItem.asStateFlow()

    private val _selectedServer = MutableStateFlow<ServerConfig?>(null)
    val selectedServer: StateFlow<ServerConfig?> = _selectedServer.asStateFlow()

    val progressTracker = ProgressTracker()

    fun selectNavItem(item: NavigationItem) {
        _selectedNavItem.value = item
    }

    fun selectServer(server: ServerConfig?) {
        _selectedServer.value = server
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        SettingsStorage.saveSettings(newSettings)
    }

    fun addServer(server: ServerConfig) {
        val current = _settings.value
        val updated = current.copy(servers = current.servers + server)
        updateSettings(updated)
    }

    fun removeServer(serverId: String) {
        val current = _settings.value
        val updated = current.copy(servers = current.servers.filter { it.id != serverId })
        updateSettings(updated)
    }
}

@Composable
fun MainWindow(viewModel: MainViewModel = remember { MainViewModel() }) {
    val selectedNavItem by viewModel.selectedNavItem.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val selectedServer by viewModel.selectedServer.collectAsState()
    val queueProgress by viewModel.progressTracker.queueProgress.collectAsState()

    MoveOffTheme(darkTheme = when (settings.theme) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        Theme.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Navigation Sidebar
                NavigationSidebar(
                    selectedItem = selectedNavItem,
                    onItemSelected = { viewModel.selectNavItem(it) },
                    modifier = Modifier.width(200.dp)
                )

                // Right Content Area
                Column(modifier = Modifier.weight(1f)) {
                    // Main Content
                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedNavItem) {
                            NavigationItem.QUICK_TRANSFER -> QuickTransferScreen(viewModel)
                            NavigationItem.SERVER_MANAGEMENT -> ServerManagementScreen(viewModel)
                            NavigationItem.FILE_MANAGER -> FileManagerScreen(viewModel)
                            NavigationItem.TRANSFER_HISTORY -> TransferHistoryScreen(viewModel)
                            NavigationItem.SETTINGS -> SettingsScreen(viewModel)
                        }
                    }

                    // Bottom Task Bar
                    TaskBar(
                        queueProgress = queueProgress,
                        modifier = Modifier.height(60.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationSidebar(
    selectedItem: NavigationItem,
    onItemSelected: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    ) {
        // App Logo/Title
        Text(
            text = "MoveOff",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Navigation Items
        NavigationItem.entries.forEach { item ->
            NavigationItemRow(
                item = item,
                isSelected = item == selectedItem,
                onClick = { onItemSelected(item) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Status indicator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "就绪",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "等待操作",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun NavigationItemRow(
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = item.title,
            fontSize = 14.sp,
            color = contentColor
        )
    }
}

@Composable
fun TaskBar(
    queueProgress: com.moveoff.progress.QueueProgress?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (queueProgress == null || queueProgress.activeTasks.isEmpty()) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "没有正在进行的任务",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                val activeTask = queueProgress.activeTasks.first()

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = activeTask.fileName,
                            fontSize = 12.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${activeTask.overallPercent}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { activeTask.overallPercent / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Task count badge
                if (queueProgress.activeTasks.size > 1 || queueProgress.pendingTasks.isNotEmpty()) {
                    Badge {
                        Text("${queueProgress.activeTasks.size + queueProgress.pendingTasks.size}")
                    }
                }
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "MoveOff - 文件迁移工具",
        state = WindowState(width = 1200.dp, height = 800.dp)
    ) {
        MainWindow()
    }
}
