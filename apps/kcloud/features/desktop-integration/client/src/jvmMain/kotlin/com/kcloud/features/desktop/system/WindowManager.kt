package com.kcloud.features.desktop.system

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.kcloud.db.ConflictResolution
import com.kcloud.event.EventBus
import com.kcloud.event.UIEvent
import com.kcloud.model.Conflict
import com.kcloud.model.ConflictStrategy
import com.kcloud.features.desktop.ui.components.ConflictResolutionDialog
import com.kcloud.features.desktop.ui.components.FloatingProgressContent
import com.kcloud.features.desktop.ui.components.Toast
import com.kcloud.features.desktop.ui.components.ToastHost
import com.kcloud.features.desktop.ui.components.ToastManager
import com.kcloud.features.desktop.ui.components.ToastType
import com.kcloud.state.ActiveWindow
import com.kcloud.state.AppStateManager
import com.kcloud.sync.SyncEngineManager
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
 * 冲突解决内容 - 使用 ConflictResolutionDialog
 */
@Composable
private fun ConflictResolutionContent(
    conflictId: String,
    onResolve: (String) -> Unit,
    onCancel: () -> Unit
) {
    // 获取冲突详情
    val scope = rememberCoroutineScope()
    var conflict by remember { mutableStateOf<Conflict?>(null) }

    LaunchedEffect(conflictId) {
        // 从数据库或状态管理器获取冲突信息
        // 这里简化处理，实际应该从数据库查询
        conflict = loadConflictById(conflictId)
    }

    conflict?.let { c ->
        ConflictResolutionDialog(
            conflict = c,
            onResolve = { strategy, rememberChoice ->
                // 转换策略
                val resolution = when (strategy) {
                    ConflictStrategy.OVERWRITE -> ConflictResolution.USE_LOCAL
                    ConflictStrategy.SKIP -> ConflictResolution.USE_REMOTE
                    ConflictStrategy.RENAME -> ConflictResolution.KEEP_BOTH
                }

                // 应用解决策略
                scope.launch {
                    try {
                        SyncEngineManager.get().resolveConflict(c.path, resolution)
                        onResolve(strategy.name)
                    } catch (e: Exception) {
                        // 处理错误
                    }
                }

                // 如果选择了记住，保存默认策略
                if (rememberChoice) {
                    // TODO: 保存默认冲突解决策略到设置
                }
            },
            onCancel = onCancel,
            onSkip = onCancel
        )
    } ?: run {
        // 加载中或冲突不存在
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

/**
 * 根据 ID 加载冲突信息
 * 实际应该从数据库查询
 */
private fun loadConflictById(conflictId: String): Conflict? {
    // TODO: 从数据库查询冲突信息
    // 临时返回示例数据
    return Conflict(
        id = conflictId,
        path = "/Users/example/KCloud/document.txt",
        localVersion = "v1.2",
        remoteVersion = "v1.3",
        localSize = 1024,
        remoteSize = 2048,
        localMtime = System.currentTimeMillis() - 3600000,
        remoteMtime = System.currentTimeMillis() - 1800000
    )
}
