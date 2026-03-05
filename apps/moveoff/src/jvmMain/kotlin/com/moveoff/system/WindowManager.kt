package com.moveoff.system

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.moveoff.event.EventBus
import com.moveoff.event.UIEvent
import com.moveoff.state.ActiveWindow
import com.moveoff.state.AppStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.awt.Toolkit
import androidx.compose.ui.window.WindowPosition

/**
 * 窗口管理器 - 管理所有应用窗口的生命周期
 */
class WindowManager(
    private val onExitApplication: () -> Unit,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    // 主窗口可见性
    private var _mainWindowVisible = mutableStateOf(false)
    val mainWindowVisible: Boolean by _mainWindowVisible

    // 设置窗口可见性
    private var _settingsWindowVisible = mutableStateOf(false)
    val settingsWindowVisible: Boolean by _settingsWindowVisible

    // 悬浮进度窗口
    private var _floatingProgressVisible = mutableStateOf(false)
    val floatingProgressVisible: Boolean by _floatingProgressVisible

    // 冲突解决窗口
    private var _conflictWindowVisible = mutableStateOf(false)
    private var _currentConflictId: String? = null
    val conflictWindowVisible: Boolean by _conflictWindowVisible
    val currentConflictId: String? get() = _currentConflictId

    init {
        // 订阅事件
        scope.launch {
            EventBus.events.collectLatest { event ->
                when (event) {
                    is UIEvent.WindowShouldShow -> showMainWindow()
                    is UIEvent.WindowShouldHide -> hideMainWindow()
                    is UIEvent.WindowShouldToggle -> toggleMainWindow()
                    is UIEvent.SettingsShouldOpen -> showSettingsWindow()
                    is UIEvent.ConflictDetected -> {
                        event.conflicts.firstOrNull()?.let { conflict ->
                            showConflictWindow(conflict.id)
                        }
                    }
                    is UIEvent.SyncStarted -> {
                        // 大文件同步时显示悬浮窗口
                        if (event.fileCount > 5) {
                            showFloatingProgress()
                        }
                    }
                    is UIEvent.SyncCompleted, is UIEvent.SyncError -> {
                        // 延迟隐藏悬浮窗口
                        kotlinx.coroutines.delay(2000)
                        hideFloatingProgress()
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * 显示主窗口
     */
    fun showMainWindow() {
        _mainWindowVisible.value = true
        AppStateManager.setMainWindowVisible(true)
        AppStateManager.setActiveWindow(ActiveWindow.MAIN)
    }

    /**
     * 隐藏主窗口（最小化到托盘）
     */
    fun hideMainWindow() {
        _mainWindowVisible.value = false
        AppStateManager.setMainWindowVisible(false)
        AppStateManager.setActiveWindow(ActiveWindow.NONE)
    }

    /**
     * 切换主窗口显示状态
     */
    fun toggleMainWindow() {
        if (_mainWindowVisible.value) {
            hideMainWindow()
        } else {
            showMainWindow()
        }
    }

    /**
     * 显示设置窗口
     */
    fun showSettingsWindow() {
        _settingsWindowVisible.value = true
        AppStateManager.setActiveWindow(ActiveWindow.SETTINGS)
    }

    /**
     * 关闭设置窗口
     */
    fun closeSettingsWindow() {
        _settingsWindowVisible.value = false
        if (!_mainWindowVisible.value) {
            AppStateManager.setActiveWindow(ActiveWindow.NONE)
        } else {
            AppStateManager.setActiveWindow(ActiveWindow.MAIN)
        }
    }

    /**
     * 显示冲突解决窗口
     */
    fun showConflictWindow(conflictId: String) {
        _currentConflictId = conflictId
        _conflictWindowVisible.value = true
        AppStateManager.setActiveWindow(ActiveWindow.CONFLICT)
    }

    /**
     * 关闭冲突窗口
     */
    fun closeConflictWindow() {
        _conflictWindowVisible.value = false
        _currentConflictId = null
        if (!_mainWindowVisible.value) {
            AppStateManager.setActiveWindow(ActiveWindow.NONE)
        } else {
            AppStateManager.setActiveWindow(ActiveWindow.MAIN)
        }
    }

    /**
     * 显示悬浮进度窗口
     */
    fun showFloatingProgress() {
        _floatingProgressVisible.value = true
    }

    /**
     * 隐藏悬浮进度窗口
     */
    fun hideFloatingProgress() {
        _floatingProgressVisible.value = false
    }

    /**
     * 退出应用
     */
    fun exitApplication() {
        onExitApplication()
    }

    /**
     * 获取主窗口位置（从托盘图标位置展开）
     */
    fun getMainWindowPosition(): WindowPosition {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val windowWidth = 1200
        val windowHeight = 800

        return WindowPosition.Aligned(Alignment.Center)
    }

    /**
     * 获取悬浮窗口位置（屏幕右下角）
     */
    fun getFloatingWindowPosition(): WindowPosition {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val margin = 20
        val windowWidth = 300
        val windowHeight = 80
        val dockHeight = 60 // 估算Dock/任务栏高度

        return WindowPosition.Absolute(
            x = (screenSize.width - windowWidth - margin).dp,
            y = (screenSize.height - windowHeight - margin - dockHeight).dp
        )
    }
}

/**
 * 设置窗口（弹窗风格）
 */
@Composable
fun SettingsWindow(
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Window(
        onCloseRequest = onCloseRequest,
        title = "设置",
        state = rememberWindowState(
            width = 600.dp,
            height = 500.dp,
            position = WindowPosition.Aligned(Alignment.Center)
        ),
        resizable = false,
        alwaysOnTop = false
    ) {
        MaterialTheme {
            Surface {
                content()
            }
        }
    }
}

/**
 * 悬浮进度窗口
 */
@Composable
fun FloatingProgressWindow(
    windowManager: WindowManager,
    onDismiss: () -> Unit
) {
    val appState by AppStateManager.state.collectAsState()

    Window(
        onCloseRequest = onDismiss,
        title = "同步中...",
        state = rememberWindowState(
            width = 300.dp,
            height = 80.dp,
            position = windowManager.getFloatingWindowPosition()
        ),
        undecorated = true,
        transparent = true,
        alwaysOnTop = true,
        focusable = false,
        resizable = false
    ) {
        FloatingProgressContent(
            progress = appState.overallProgress,
            operation = appState.currentOperation,
            onClick = {
                windowManager.showMainWindow()
                onDismiss()
            }
        )
    }
}

/**
 * 悬浮进度内容
 */
@Composable
private fun FloatingProgressContent(
    progress: Float,
    operation: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = operation ?: "同步中...",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

/**
 * 冲突解决窗口
 */
@Composable
fun ConflictResolutionWindow(
    conflictId: String,
    onCloseRequest: () -> Unit,
    onResolve: (resolution: String) -> Unit
) {
    Window(
        onCloseRequest = onCloseRequest,
        title = "解决冲突",
        state = rememberWindowState(
            width = 700.dp,
            height = 400.dp,
            position = WindowPosition.Aligned(Alignment.Center)
        ),
        resizable = false,
        alwaysOnTop = true
    ) {
        MaterialTheme {
            Surface {
                // 冲突解决UI
                ConflictResolutionContent(
                    conflictId = conflictId,
                    onResolve = onResolve,
                    onCancel = onCloseRequest
                )
            }
        }
    }
}

/**
 * 冲突解决内容（简化版）
 */
@Composable
private fun ConflictResolutionContent(
    conflictId: String,
    onResolve: (String) -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "文件冲突",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "文件在本地和远程都有修改，请选择保留哪个版本：",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        // TODO: 显示文件对比
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { onResolve("local") },
                modifier = Modifier.weight(1f)
            ) {
                Text("使用本地版本")
            }
            Button(
                onClick = { onResolve("remote") },
                modifier = Modifier.weight(1f)
            ) {
                Text("使用远程版本")
            }
            OutlinedButton(
                onClick = { onResolve("both") },
                modifier = Modifier.weight(1f)
            ) {
                Text("保留两者")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = onCancel,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("稍后处理")
        }
    }
}

/**
 * Toast通知数据类
 */
data class Toast(
    val id: String,
    val message: String,
    val type: ToastType = ToastType.INFO
)

enum class ToastType {
    INFO, SUCCESS, WARNING, ERROR
}

/**
 * Toast管理器
 */
class ToastManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _toasts = mutableStateListOf<Toast>()
    val toasts: List<Toast> = _toasts

    fun show(
        message: String,
        type: ToastType = ToastType.INFO,
        durationMillis: Long = 3000
    ) {
        val toast = Toast(
            id = System.currentTimeMillis().toString(),
            message = message,
            type = type
        )
        _toasts.add(toast)

        scope.launch {
            kotlinx.coroutines.delay(durationMillis)
            dismiss(toast.id)
        }
    }

    fun dismiss(id: String) {
        _toasts.removeAll { it.id == id }
    }
}

/**
 * Toast容器
 */
@Composable
fun ToastHost(
    toasts: List<Toast>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            toasts.forEach { toast ->
                ToastItem(toast = toast)
            }
        }
    }
}

/**
 * Toast项
 */
@Composable
private fun ToastItem(toast: Toast) {
    val (containerColor, contentColor) = when (toast.type) {
        ToastType.INFO -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        ToastType.SUCCESS -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        ToastType.WARNING -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        ToastType.ERROR -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = toast.message,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
