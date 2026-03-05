package com.moveoff.system

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.moveoff.event.EventBus
import com.moveoff.event.NotificationType
import com.moveoff.event.UIEvent
import com.moveoff.state.AppStateManager
import com.moveoff.state.TrayIconState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.imageio.ImageIO
import javax.swing.SwingUtilities

/**
 * 托盘菜单项数据类
 */
data class TrayMenuItem(
    val id: String,
    val label: String,
    val enabled: Boolean = true,
    val isSeparator: Boolean = false,
    val shortcut: String? = null,
    val badge: Int? = null,
    val onClick: (() -> Unit)? = null
)

/**
 * 增强的系统托盘管理器
 */
class EnhancedTrayManager(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private var trayIcon: TrayIcon? = null
    private var popupMenu: PopupMenu? = null
    private val menuItems = mutableMapOf<String, MenuItem>()

    // 当前图标状态
    private var currentIconState: TrayIconState = TrayIconState.IDLE


    // 当前徽章数字
    private var currentBadge: Int = 0

    // 菜单项列表（响应式）
    val menuItemsList: SnapshotStateList<TrayMenuItem> = mutableStateListOf()

    val isSupported: Boolean get() = SystemTray.isSupported()

    /**
     * 安装托盘图标
     */
    fun install(
        onShowMainWindow: () -> Unit,
        onShowSettings: () -> Unit,
        onSyncNow: () -> Unit,
        onPauseResume: () -> Unit,
        onExit: () -> Unit
    ): Boolean {
        if (!isSupported) {
            println("System tray is not supported on this platform")
            return false
        }

        return try {
            val tray = SystemTray.getSystemTray()

            // 创建弹出菜单
            popupMenu = createPopupMenu(
                onShowMainWindow,
                onShowSettings,
                onSyncNow,
                onPauseResume,
                onExit
            )

            // 加载图标
            val icon = loadTrayIcon(TrayIconState.IDLE)

            // 创建托盘图标
            trayIcon = TrayIcon(icon, "MoveOff", popupMenu).apply {
                isImageAutoSize = true

                // 单击打开窗口
                addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        if (e.button == MouseEvent.BUTTON1) {
                            onShowMainWindow()
                        }
                    }
                })

                // 添加ActionListener（双击）
                addActionListener { onShowMainWindow() }
            }

            tray.add(trayIcon)

            // 启动事件监听
            startEventListening()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 卸载托盘
     */
    fun uninstall() {
        scope.cancel()
        trayIcon?.let {
            try {
                SystemTray.getSystemTray().remove(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        trayIcon = null
        popupMenu = null
        menuItems.clear()
    }

    /**
     * 更新托盘图标
     */
    fun updateIcon(state: TrayIconState) {
        if (currentIconState == state) return
        currentIconState = state

        SwingUtilities.invokeLater {
            try {
                val image = loadTrayIcon(state)
                trayIcon?.image = image
                updateTooltip()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 更新工具提示
     */
    fun updateTooltip(text: String? = null) {
        val tooltip = text ?: buildTooltip()
        SwingUtilities.invokeLater {
            trayIcon?.toolTip = tooltip
        }
    }

    /**
     * 显示通知气泡
     */
    fun showNotification(
        title: String,
        message: String,
        type: NotificationType = NotificationType.INFO
    ) {
        SwingUtilities.invokeLater {
            val awtType = when (type) {
                NotificationType.INFO -> TrayIcon.MessageType.INFO
                NotificationType.SUCCESS -> TrayIcon.MessageType.INFO
                NotificationType.WARNING -> TrayIcon.MessageType.WARNING
                NotificationType.ERROR -> TrayIcon.MessageType.ERROR
            }
            trayIcon?.displayMessage(title, message, awtType)
        }
    }

    /**
     * 设置徽章数字（在图标上显示数字）
     * 注意：AWT原生不支持，需要自定义绘制或平台特定实现
     */
    fun setBadge(count: Int) {
        currentBadge = count
        // TODO: Windows使用图标覆盖，macOS使用NSStatusBar的badge
        // 目前仅更新工具提示
        if (count > 0) {
            updateTooltip("MoveOff - $count 个冲突待解决")
        }
    }

    /**
     * 更新菜单项
     */
    fun updateMenuItem(id: String, enabled: Boolean, label: String? = null) {
        SwingUtilities.invokeLater {
            menuItems[id]?.let { item ->
                item.isEnabled = enabled
                label?.let { item.label = it }
            }
        }
    }

    /**
     * 根据应用状态刷新菜单
     */
    fun refreshMenu() {
        val state = AppStateManager.currentState

        // 更新暂停/恢复菜单项
        updateMenuItem(
            "pause_resume",
            enabled = true,
            label = if (state.syncStatus == com.moveoff.state.SyncStatus.PAUSED) "恢复同步" else "暂停同步"
        )

        // 更新同步菜单项
        updateMenuItem("sync_now", enabled = state.isOnline && !state.isBusy)

        // 更新冲突提示
        updateMenuItem(
            "conflicts",
            enabled = state.conflictCount > 0,
            label = if (state.conflictCount > 0) "⚠️ ${state.conflictCount} 个冲突待解决" else ""
        )
    }

    /**
     * 创建弹出菜单
     */
    private fun createPopupMenu(
        onShowMainWindow: () -> Unit,
        onShowSettings: () -> Unit,
        onSyncNow: () -> Unit,
        onPauseResume: () -> Unit,
        onExit: () -> Unit
    ): PopupMenu {
        val popup = PopupMenu()

        // 应用名称（禁用）
        popup.add(MenuItem("MoveOff").apply { isEnabled = false })

        // 状态显示
        val statusItem = MenuItem("就绪").apply { isEnabled = false }
        menuItems["status"] = statusItem
        popup.add(statusItem)
        popup.addSeparator()

        // 打开面板
        popup.add(MenuItem("打开面板").apply {
            addActionListener { onShowMainWindow() }
        })

        // 立即同步
        popup.add(MenuItem("立即同步").apply {
            menuItems["sync_now"] = this
            addActionListener { onSyncNow() }
        })

        // 暂停/恢复
        popup.add(MenuItem("暂停同步").apply {
            menuItems["pause_resume"] = this
            addActionListener { onPauseResume() }
        })

        popup.addSeparator()

        // 冲突提示（动态显示）
        popup.add(MenuItem("").apply {
            menuItems["conflicts"] = this
            isVisible = false
            addActionListener { onShowMainWindow() }
        })

        popup.addSeparator()

        // 设置
        popup.add(MenuItem("设置...").apply {
            addActionListener { onShowSettings() }
        })

        popup.addSeparator()

        // 退出
        popup.add(MenuItem("退出").apply {
            addActionListener { onExit() }
        })

        return popup
    }

    /**
     * 加载托盘图标
     */
    private fun loadTrayIcon(state: TrayIconState): Image {
        return try {
            // 尝试加载资源
            val resource = Thread.currentThread().contextClassLoader.getResource(state.iconResource)
            if (resource != null) {
                ImageIO.read(resource)
            } else {
                createFallbackIcon(state)
            }
        } catch (e: Exception) {
            createFallbackIcon(state)
        }
    }

    /**
     * 创建备用图标（纯色块）
     */
    private fun createFallbackIcon(state: TrayIconState): Image {
        val size = 22
        val image = java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()

        val color = when (state) {
            TrayIconState.IDLE -> Color(0x4CAF50)      // 绿色
            TrayIconState.SYNCING -> Color(0x2196F3)   // 蓝色
            TrayIconState.WARNING -> Color(0xFF9800)   // 橙色
            TrayIconState.ERROR -> Color(0xF44336)     // 红色
            TrayIconState.PAUSED -> Color(0x9E9E9E)    // 灰色
        }

        // 绘制圆形背景
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.color = color
        g2d.fillOval(1, 1, size - 2, size - 2)

        // 绘制首字母
        g2d.color = Color.WHITE
        g2d.font = Font("Arial", Font.BOLD, 12)
        val letter = "M"
        val metrics = g2d.fontMetrics
        val x = (size - metrics.stringWidth(letter)) / 2
        val y = ((size - metrics.height) / 2) + metrics.ascent - 1
        g2d.drawString(letter, x, y)

        g2d.dispose()
        return image
    }

    /**
     * 构建工具提示文本
     */
    private fun buildTooltip(): String {
        val state = AppStateManager.currentState
        val base = "MoveOff"

        return when {
            state.conflictCount > 0 -> "$base - ${state.conflictCount} 个冲突待解决"
            state.isBusy -> "$base - ${state.currentOperation ?: "同步中..."}"
            state.syncStatus == com.moveoff.state.SyncStatus.OFFLINE -> "$base - 离线"
            state.lastSyncTime != null -> {
                val timeStr = formatLastSyncTime(state.lastSyncTime)
                "$base - 上次同步: $timeStr"
            }
            else -> base
        }
    }

    /**
     * 格式化上次同步时间
     */
    private fun formatLastSyncTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < 60000 -> "刚刚"
            diff < 3600000 -> "${diff / 60000}分钟前"
            diff < 86400000 -> "${diff / 3600000}小时前"
            else -> "${diff / 86400000}天前"
        }
    }

    /**
     * 启动事件监听
     */
    private fun startEventListening() {
        scope.launch {
            // 监听事件总线
            EventBus.events.collectLatest { event ->
                when (event) {
                    is UIEvent.SyncStarted -> {
                        updateIcon(TrayIconState.SYNCING)
                        updateTooltip("正在${event.operation}...")
                    }
                    is UIEvent.SyncCompleted -> {
                        updateIcon(TrayIconState.IDLE)
                        showNotification(
                            "同步完成",
                            "上传: ${event.uploaded}, 下载: ${event.downloaded}",
                            NotificationType.SUCCESS
                        )
                    }
                    is UIEvent.SyncError -> {
                        updateIcon(TrayIconState.ERROR)
                        showNotification("同步错误", event.message, NotificationType.ERROR)
                    }
                    is UIEvent.ConflictDetected -> {
                        setBadge(event.conflicts.size)
                        updateIcon(TrayIconState.WARNING)
                    }
                    is UIEvent.TrayNotification -> {
                        showNotification(event.title, event.message, event.type)
                    }
                    else -> {}
                }
                refreshMenu()
            }
        }

        // 监听状态变化
        scope.launch {
            AppStateManager.state.collectLatest { state ->
                updateIcon(TrayIconState.fromSyncStatus(state.syncStatus))
                refreshMenu()
            }
        }
    }
}

/**
 * 创建平台特定的托盘管理器
 */
fun createTrayManager(scope: CoroutineScope = CoroutineScope(SupervisorJob())): EnhancedTrayManager {
    val osName = System.getProperty("os.name").lowercase()
    return when {
        osName.contains("mac") -> MacOSTrayManager(scope)
        osName.contains("win") -> WindowsTrayManager(scope)
        else -> EnhancedTrayManager(scope)
    }
}

/**
 * macOS专用托盘管理器（未来扩展）
 */
class MacOSTrayManager(scope: CoroutineScope) : EnhancedTrayManager(scope) {
    // TODO: 通过JNA调用NSStatusBar实现原生macOS体验
    // - 支持暗/亮模式切换
    // - 支持菜单栏徽章
    // - 更好的图标模板支持
}

/**
 * Windows专用托盘管理器（未来扩展）
 */
class WindowsTrayManager(scope: CoroutineScope) : EnhancedTrayManager(scope) {
    // TODO: 使用JNA实现
    // - 托盘图标上的数字覆盖
    // - 进度条图标（Windows 7+）
    // - 跳转列表（Jump List）
}
