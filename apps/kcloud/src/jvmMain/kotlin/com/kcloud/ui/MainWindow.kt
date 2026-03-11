package com.kcloud.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.kcloud.model.AppSettings
import com.kcloud.model.ServerConfig
import com.kcloud.model.Theme
import com.kcloud.plugin.ui.SidebarContributor
import com.kcloud.progress.ProgressTracker
import com.kcloud.storage.SettingsStorage
import com.kcloud.ui.theme.MoveOffTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.compose.koinInject
import androidx.compose.runtime.LaunchedEffect
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import org.koin.core.context.startKoin
import com.kcloud.ui.contributors.QuickTransferContributor
import com.kcloud.ui.contributors.ServerManagementContributor
import com.kcloud.ui.contributors.FileManagerContributor
import com.kcloud.ui.contributors.TransferHistoryContributor
import com.kcloud.ui.contributors.SettingsContributor

// 注入所有 SidebarContributor
val appModule = module {
    // ViewModel
    single { MainViewModel() }

    // Contributors
    singleOf(::QuickTransferContributor)
    singleOf(::ServerManagementContributor)
    singleOf(::FileManagerContributor)
    singleOf(::TransferHistoryContributor)
    singleOf(::SettingsContributor)
}

class MainViewModel {
    private val _settings = MutableStateFlow(SettingsStorage.loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _selectedContributorId = MutableStateFlow<String>("quick-transfer")
    val selectedContributorId: StateFlow<String> = _selectedContributorId.asStateFlow()

    private val _selectedServer = MutableStateFlow<ServerConfig?>(null)
    val selectedServer: StateFlow<ServerConfig?> = _selectedServer.asStateFlow()

    val progressTracker = ProgressTracker()

    fun selectContributor(id: String) {
        _selectedContributorId.value = id
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
fun MainWindow(viewModel: MainViewModel = koinInject()) {
    val selectedId by viewModel.selectedContributorId.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val queueProgress by viewModel.progressTracker.queueProgress.collectAsState()

    // 从 Koin 获取所有 SidebarContributor
    val contributors = remember {
        KoinPlatform.getKoin().getAll<SidebarContributor>()
            .sortedBy { it.order }
    }

    // 获取当前选中的 contributor
    val selectedContributor = contributors.find { it.id == selectedId }

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
                // 插件化侧边栏
                PluginSidebar(
                    contributors = contributors,
                    selectedId = selectedId,
                    onSelect = { viewModel.selectContributor(it) },
                    modifier = Modifier.width(200.dp)
                )

                // 内容区域
                Column(modifier = Modifier.weight(1f)) {
                    // 主内容 - 由选中的 contributor 渲染
                    Box(modifier = Modifier.weight(1f)) {
                        selectedContributor?.Content()
                            ?: Text("未找到页面: $selectedId")
                    }

                    // 底部任务栏
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
fun PluginSidebar(
    contributors: List<SidebarContributor>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    ) {
        // 应用标题
        Text(
            text = "KCloud",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // 插件化导航项
        contributors.forEach { contributor ->
            ContributorRow(
                contributor = contributor,
                isSelected = contributor.id == selectedId,
                onClick = { onSelect(contributor.id) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 状态指示器
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
fun ContributorRow(
    contributor: SidebarContributor,
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
        contributor.icon?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = contributor.title,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(
            text = contributor.title,
            fontSize = 14.sp,
            color = contentColor
        )
    }
}

@Composable
fun TaskBar(
    queueProgress: com.kcloud.progress.QueueProgress?,
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

                if (queueProgress.activeTasks.size > 1 || queueProgress.pendingTasks.isNotEmpty()) {
                    Badge {
                        Text("${queueProgress.activeTasks.size + queueProgress.pendingTasks.size}")
                    }
                }
            }
        }
    }
}

fun main() {
    // 启动 Koin
    startKoin {
        modules(appModule)
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KCloud - 文件同步",
            state = WindowState(width = 1200.dp, height = 800.dp)
        ) {
            MainWindow()
        }
    }
}
