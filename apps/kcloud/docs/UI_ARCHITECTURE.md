# KCloud 系统托盘与Compose UI架构设计

> 复刻JetBrains Toolbox风格：常驻托盘、浮动面板、全局快捷键、状态实时同步

---

## 一、整体架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              应用架构 (Application)                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────┐     ┌─────────────────────┐     ┌───────────────┐ │
│  │   AppStateManager   │◄───►│   SyncEngine        │◄───►│   S3 Client   │ │
│  │   (单例状态管理)      │     │   (同步引擎)         │     │   (存储后端)   │ │
│  └──────────┬──────────┘     └─────────────────────┘     └───────────────┘ │
│             │                                                               │
│             ▼                                                               │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         UI Layer                                     │   │
│  │  ┌───────────────┐  ┌───────────────┐  ┌───────────────────────────┐ │   │
│  │  │ TrayManager   │  │ WindowManager │  │ FloatingUI                │ │   │
│  │  │ - 系统托盘     │  │ - 主窗口      │  │ - 迷你进度条(屏幕角落)      │ │   │
│  │  │ - 动态图标     │  │ - 设置窗口    │  │ - 冲突解决弹窗             │ │   │
│  │  │ - 右键菜单     │  │ - 历史记录窗口 │  │ - 通知Toast               │ │   │
│  │  └───────┬───────┘  └───────┬───────┘  └───────────┬───────────────┘ │   │
│  │          │                  │                      │                 │   │
│  │          └──────────────────┼──────────────────────┘                 │   │
│  │                             ▼                                        │   │
│  │  ┌─────────────────────────────────────────────────────────────┐    │   │
│  │  │                 Compose Runtime                              │    │   │
│  │  │  - StateFlow驱动UI                                           │    │   │
│  │  │  - 动画(进度条/状态切换)                                       │    │   │
│  │  │  - 主题(跟随系统/手动切换)                                     │    │   │
│  │  └─────────────────────────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                   Platform Integration                               │   │
│  │  macOS: NSStatusBar / Swift Bridge    Windows: JNA System Tray      │   │
│  │  Finder Sync Extension                Shell Icon Overlay Handler    │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 二、核心状态设计

### 2.1 应用级状态机 (AppState)

```kotlin
// 同步状态枚举
enum class SyncStatus {
    IDLE,           // 空闲 - 绿色勾
    SCANNING,       // 扫描中 - 蓝色旋转
    UPLOADING,      // 上传中 - 蓝色进度
    DOWNLOADING,    // 下载中 - 蓝色进度
    CONFLICT,       // 有冲突 - 红色感叹号
    ERROR,          // 错误 - 红色叉
    PAUSED,         // 暂停 - 黄色暂停
    OFFLINE         // 离线 - 灰色
}

// 应用全局状态
@Stable
data class AppState(
    // 同步状态
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val overallProgress: Float = 0f,        // 0.0 - 1.0
    val currentOperation: String? = null,    // "正在上传 document.pdf"

    // 统计
    val pendingUploads: Int = 0,
    val pendingDownloads: Int = 0,
    val conflictCount: Int = 0,

    // 连接状态
    val isOnline: Boolean = true,
    val lastSyncTime: Long? = null,

    // 窗口状态
    val isMainWindowVisible: Boolean = false,
    val activeWindow: ActiveWindow = ActiveWindow.MAIN
)

enum class ActiveWindow {
    NONE,       // 仅托盘
    MAIN,       // 主窗口
    SETTINGS,   // 设置窗口
    HISTORY,    // 历史记录窗口
    CONFLICT    // 冲突解决窗口
}

// 单例状态管理器
object AppStateManager {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    // 状态更新方法
    fun updateSyncStatus(status: SyncStatus, message: String? = null)
    fun updateProgress(progress: Float, operation: String? = null)
    fun incrementPendingUploads()
    fun decrementPendingDownloads()
    fun setOnline(online: Boolean)
    fun setMainWindowVisible(visible: Boolean)

    // 派生状态 - 用于UI判断
    val hasConflicts: Boolean get() = _state.value.conflictCount > 0
    val isBusy: Boolean get() = _state.value.syncStatus in setOf(
        SyncStatus.SCANNING, SyncStatus.UPLOADING, SyncStatus.DOWNLOADING
    )
}
```

### 2.2 托盘图标状态映射

```kotlin
enum class TrayIconState(
    val iconResource: String,
    val tooltip: String
) {
    IDLE("tray/icon_idle.png", "KCloud - 已同步"),
    SYNCING("tray/icon_syncing.png", "KCloud - 同步中..."),
    WARNING("tray/icon_warning.png", "KCloud - 需要关注"),
    ERROR("tray/icon_error.png", "KCloud - 同步错误"),
    PAUSED("tray/icon_paused.png", "KCloud - 已暂停");

    companion object {
        fun fromSyncStatus(status: SyncStatus): TrayIconState = when (status) {
            SyncStatus.IDLE -> IDLE
            SyncStatus.SCANNING, SyncStatus.UPLOADING, SyncStatus.DOWNLOADING -> SYNCING
            SyncStatus.CONFLICT -> WARNING
            SyncStatus.ERROR -> ERROR
            SyncStatus.PAUSED -> PAUSED
            SyncStatus.OFFLINE -> PAUSED
        }
    }
}
```

---

## 三、系统托盘架构 (TrayManager)

### 3.1 跨平台托盘管理器

```kotlin
// 抽象托盘接口
interface TrayManager {
    val isSupported: Boolean

    fun install(
        onShowMainWindow: () -> Unit,
        onShowSettings: () -> Unit,
        onSyncNow: () -> Unit,
        onPauseResume: () -> Unit,
        onExit: () -> Unit
    ): Boolean

    fun uninstall()

    // 动态更新
    fun updateIcon(state: TrayIconState)
    fun updateTooltip(text: String)
    fun showNotification(title: String, message: String, type: NotificationType)

    // 菜单控制
    fun updateMenuItem(id: String, enabled: Boolean, label: String? = null)
    fun setBadge(count: Int)  // macOS: 菜单栏徽章, Windows: 托盘图标覆盖
}

// 实现: AWT通用版本 (跨平台基础)
class AwtTrayManager : TrayManager {
    private var trayIcon: TrayIcon? = null
    private val menuItems = mutableMapOf<String, MenuItem>()

    override fun install(...): Boolean {
        // 基础AWT实现
    }

    override fun updateIcon(state: TrayIconState) {
        val image = loadTrayImage(state.iconResource)
        trayIcon?.image = image
    }

    // Windows增强: 使用JNA调用Shell_NotifyIcon
    // macOS增强: 通过JNA调用NSStatusBar (或Swift Bridge)
}
```

### 3.2 托盘交互设计

```kotlin
// 托盘菜单结构
trayMenu {
    // 状态区 (不可点击)
    label("KCloud v1.0.0")
    label(if (isOnline) "已连接" else "离线")
    separator()

    // 操作区
    item("打开面板", onClick = onShowMainWindow, shortcut = "Ctrl+Shift+M")
    item("立即同步", onClick = onSyncNow)

    // 条件显示
    if (isPaused) {
        item("恢复同步", onClick = onPauseResume)
    } else {
        item("暂停同步", onClick = onPauseResume)
    }

    separator()

    // 最近活动区 (动态)
    if (recentActivities.isNotEmpty()) {
        label("最近活动")
        recentActivities.take(3).forEach { activity ->
            label("  ${activity.shortDescription}")
        }
        separator()
    }

    // 问题提示
    if (conflictCount > 0) {
        item("⚠️ $conflictCount 个冲突待解决", onClick = onShowMainWindow)
        separator()
    }

    item("设置...", onClick = onShowSettings)
    separator()
    item("退出", onClick = onExit)
}
```

---

## 四、窗口管理架构 (WindowManager)

### 4.1 多窗口生命周期管理

```kotlin
// 窗口类型定义
sealed class AppWindow(
    open val id: String,
    open val title: String,
    open val width: Dp,
    open val height: Dp,
    open val resizable: Boolean = true
) {
    data class Main(
        override val width: Dp = 1200.dp,
        override val height: Dp = 800.dp
    ) : AppWindow("main", "KCloud", width, height)

    data class Settings(
        override val width: Dp = 600.dp,
        override val height: Dp = 500.dp
    ) : AppWindow("settings", "设置", width, height, resizable = false)

    data class ConflictResolver(
        val conflictId: String,
        override val width: Dp = 700.dp,
        override val height: Dp = 400.dp
    ) : AppWindow("conflict_$conflictId", "解决冲突", width, height, resizable = false)

    data class FloatingProgress(
        override val width: Dp = 300.dp,
        override val height: Dp = 80.dp
    ) : AppWindow("progress", "同步中...", width, height, resizable = false)
}

// 窗口管理器
class WindowManager(
    private val onExitApplication: () -> Unit
) {
    // 活跃的窗口集合
    private val _activeWindows = mutableStateMapOf<String, WindowState>()
    val activeWindows: Map<String, WindowState> = _activeWindows

    // 主窗口特殊处理
    private var _mainWindowVisible = mutableStateOf(false)
    val mainWindowVisible: Boolean by _mainWindowVisible

    // 悬浮进度条窗口
    private var _floatingProgressVisible = mutableStateOf(false)
    val floatingProgressVisible: Boolean by _floatingProgressVisible

    // 打开主窗口
    fun showMainWindow() {
        _mainWindowVisible.value = true
        AppStateManager.setMainWindowVisible(true)
    }

    // 隐藏主窗口到托盘 (不是退出)
    fun hideMainWindow() {
        _mainWindowVisible.value = false
        AppStateManager.setMainWindowVisible(false)
    }

    // 切换主窗口显示状态
    fun toggleMainWindow() {
        if (_mainWindowVisible.value) {
            hideMainWindow()
        } else {
            showMainWindow()
        }
    }

    // 打开设置窗口 (弹窗)
    fun showSettingsWindow() {
        openWindow(AppWindow.Settings())
    }

    // 打开冲突解决窗口
    fun showConflictResolver(conflictId: String) {
        openWindow(AppWindow.ConflictResolver(conflictId))
    }

    // 显示/隐藏悬浮进度条
    fun setFloatingProgressVisible(visible: Boolean) {
        _floatingProgressVisible.value = visible
    }

    // 窗口定位 (macOS: 从菜单栏下方展开, Windows: 从托盘位置展开)
    fun getWindowPosition(window: AppWindow): WindowPosition {
        return when (window) {
            is AppWindow.Main -> calculateMainWindowPosition()
            is AppWindow.FloatingProgress -> calculateFloatingPosition()
            else -> WindowPosition.center()
        }
    }

    // 全局快捷键
    fun registerGlobalShortcuts() {
        // Ctrl+Shift+M: 打开/隐藏主面板
        // Ctrl+Shift+S: 立即同步
        // Ctrl+Shift+P: 暂停/恢复
        GlobalShortcutManager.register()
    }
}
```

### 4.2 主窗口布局 (Toolbox风格)

```kotlin
@Composable
fun MainWindowContent(
    viewModel: MainViewModel = remember { MainViewModel() }
) {
    val appState by AppStateManager.state.collectAsState()
    val syncStatus = appState.syncStatus

    KCloudTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // 三栏布局
            Row(modifier = Modifier.fillMaxSize()) {
                // 左侧导航栏 (固定200dp)
                NavigationSidebar(
                    selectedItem = viewModel.selectedNavItem,
                    onItemSelected = { viewModel.selectNavItem(it) },
                    syncStatus = syncStatus,
                    modifier = Modifier.width(200.dp)
                )

                // 右侧内容区
                Column(modifier = Modifier.weight(1f)) {
                    // 顶部状态栏 (可选，显示警告/通知)
                    StatusBanner(appState)

                    // 主内容区域
                    Box(modifier = Modifier.weight(1f)) {
                        when (viewModel.selectedNavItem) {
                            NavigationItem.QUICK_TRANSFER -> QuickTransferScreen()
                            NavigationItem.SERVER_MANAGEMENT -> ServerManagementScreen()
                            NavigationItem.FILE_MANAGER -> FileManagerScreen()
                            NavigationItem.TRANSFER_HISTORY -> TransferHistoryScreen()
                            NavigationItem.SETTINGS -> SettingsScreen()
                        }
                    }

                    // 底部任务栏 (Toolbox风格)
                    TaskBar(
                        appState = appState,
                        onExpandTasks = { viewModel.showTaskDetailPanel() }
                    )
                }
            }
        }
    }
}

// 增强的导航侧边栏
@Composable
fun NavigationSidebar(
    selectedItem: NavigationItem,
    onItemSelected: (NavigationItem) -> Unit,
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    ) {
        // Logo区域
        BrandHeader()

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // 导航项
        NavigationItem.entries.forEach { item ->
            val badgeCount = when (item) {
                NavigationItem.FILE_MANAGER -> pendingConflicts
                NavigationItem.TRANSFER_HISTORY -> failedTasks
                else -> 0
            }

            NavigationItemRow(
                item = item,
                isSelected = item == selectedItem,
                badgeCount = badgeCount,
                onClick = { onItemSelected(item) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 底部同步状态卡片
        SyncStatusCard(syncStatus)
    }
}

// 同步状态卡片
@Composable
fun SyncStatusCard(status: SyncStatus) {
    val (icon, color, text) = when (status) {
        SyncStatus.IDLE -> Triple(Icons.Default.CheckCircle, Color.Green, "已同步")
        SyncStatus.SCANNING -> Triple(Icons.Default.Search, Color.Blue, "扫描中...")
        SyncStatus.UPLOADING, SyncStatus.DOWNLOADING ->
            Triple(Icons.Default.Sync, Color.Blue, "同步中...")
        SyncStatus.CONFLICT -> Triple(Icons.Default.Warning, Color.Yellow, "待解决")
        SyncStatus.ERROR -> Triple(Icons.Default.Error, Color.Red, "同步错误")
        SyncStatus.PAUSED -> Triple(Icons.Default.PauseCircle, Color.Gray, "已暂停")
        SyncStatus.OFFLINE -> Triple(Icons.Default.CloudOff, Color.Gray, "离线")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text, fontSize = 12.sp, color = color)
                Text("点击查看详情", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}
```

---

## 五、悬浮UI组件

### 5.1 迷你进度悬浮窗

```kotlin
// 屏幕角落的迷你进度条 (类似Mac的复制进度)
@Composable
fun FloatingProgressWindow(
    appState: AppState,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val windowState = rememberWindowState(
        width = 300.dp,
        height = 80.dp,
        position = calculateFloatingPosition()
    )

    Window(
        onCloseRequest = onDismiss,
        state = windowState,
        undecorated = true,  // 无边框
        transparent = true,   // 透明背景
        alwaysOnTop = true
    ) {
        FloatingProgressContent(
            progress = appState.overallProgress,
            operation = appState.currentOperation,
            onClick = onClick
        )
    }
}

@Composable
fun FloatingProgressContent(
    progress: Float,
    operation: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = operation ?: "同步中...",
                    fontSize = 13.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
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

// 计算悬浮位置 (右下角，避开Dock/任务栏)
fun calculateFloatingPosition(): WindowPosition {
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val margin = 20
    return WindowPosition(
        x = (screenSize.width - 300 - margin).dp,
        y = (screenSize.height - 80 - margin - getDockHeight()).dp
    )
}
```

### 5.2 Toast通知

```kotlin
// 轻量级Toast通知 (类似Android Toast)
class ToastManager {
    private val _toasts = MutableStateFlow<List<Toast>>(emptyList())
    val toasts: StateFlow<List<Toast>> = _toasts.asStateFlow()

    fun show(
        message: String,
        type: ToastType = ToastType.INFO,
        duration: Duration = 3.seconds
    ) {
        val toast = Toast(
            id = generateId(),
            message = message,
            type = type
        )
        _toasts.value += toast

        // 自动消失
        CoroutineScope(Dispatchers.Default).launch {
            delay(duration)
            dismiss(toast.id)
        }
    }

    fun dismiss(id: String) {
        _toasts.value = _toasts.value.filter { it.id != id }
    }
}

@Composable
fun ToastHost(
    toasts: List<Toast>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        toasts.forEach { toast ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                ToastItem(toast)
            }
        }
    }
}
```

---

## 六、状态流转与事件驱动

### 6.1 状态流转图

```
                    ┌─────────────────────────────────────────────────────┐
                    │                                                     │
    ┌──────────►    ▼                                                     │
    │         ┌─────────┐    启动扫描    ┌──────────┐                     │
    │         │  IDLE   │ ─────────────►│ SCANNING │                     │
    │         │ (空闲)   │               │ (扫描中)  │                     │
    │         └────┬────┘               └────┬─────┘                     │
    │              │                          │                         │
    │         检测到   │扫描完成                  │ 发现文件                  │
    │         文件变化  │                         │                         │
    │              │                          ▼                         │
    │              │                    ┌──────────┐    有冲突            │
    │              │                    │ UPLOADING│ ◄────────────┐      │
    │              │                    │DOWNLOADING│              │      │
    │              │                    │ (传输中)  │─────────────►│      │
    │              │                    └────┬─────┘   完成        │      │
    │              │                         │                    │      │
    │              │                    完成/失败                   │      │
    │              │                         │                    │      │
    │              │                         ▼                    │      │
    │              │                    ┌──────────┐               │      │
    │              └────────────────────┤  IDLE    │◄──────────────┘      │
    │                                   │ (回到空闲)│                      │
    │                                   └────┬─────┘                      │
    │                                        │                           │
    │         ┌──────────────────────────────┘                           │
    │         │                                                          │
    │         ▼                                                          │
    │    ┌─────────┐    用户暂停    ┌──────────┐    用户恢复    ┌────────┐│
    └────┤  ERROR  │◄──────────────┤  PAUSED  │◄───────────────┤CONFLICT│┘
         │ (错误)   │               │ (已暂停)  │                │ (冲突) │
         └────┬────┘               └──────────┘                └────────┘
              │
              │ 错误恢复
              ▼
         ┌─────────┐
         │  IDLE   │
         └─────────┘
```

### 6.2 事件总线设计

```kotlin
// UI事件定义
sealed class UIEvent {
    // 同步事件
    data class SyncStarted(val fileCount: Int) : UIEvent()
    data class SyncProgress(val current: Int, val total: Int, val fileName: String) : UIEvent()
    data class SyncCompleted(val uploaded: Int, val downloaded: Int) : UIEvent()
    data class SyncError(val message: String) : UIEvent()
    data class ConflictDetected(val conflicts: List<Conflict>) : UIEvent()

    // 窗口事件
    object WindowShouldShow : UIEvent()
    object WindowShouldHide : UIEvent()
    data class NavigateTo(val screen: NavigationItem) : UIEvent()

    // 托盘事件
    data class TrayNotification(val title: String, val message: String) : UIEvent()
    data class TrayBadgeUpdate(val count: Int) : UIEvent()
}

// 事件总线
object EventBus {
    private val _events = MutableSharedFlow<UIEvent>()
    val events: SharedFlow<UIEvent> = _events.asSharedFlow()

    suspend fun emit(event: UIEvent) = _events.emit(event)

    fun subscribe(scope: CoroutineScope, onEvent: (UIEvent) -> Unit) {
        events.onEach(onEvent).launchIn(scope)
    }
}

// 在UI层订阅事件
@Composable
fun EventHandler() {
    val scope = rememberCoroutineScope()
    val windowManager = remember { WindowManager }
    val trayManager = remember { TrayManager }

    LaunchedEffect(Unit) {
        EventBus.events.collect { event ->
            when (event) {
                is UIEvent.SyncStarted -> {
                    windowManager.setFloatingProgressVisible(true)
                    trayManager.updateIcon(TrayIconState.SYNCING)
                }
                is UIEvent.SyncCompleted -> {
                    windowManager.setFloatingProgressVisible(false)
                    trayManager.updateIcon(TrayIconState.IDLE)
                    trayManager.showNotification("同步完成", "${event.uploaded}个文件已同步")
                }
                is UIEvent.ConflictDetected -> {
                    trayManager.updateIcon(TrayIconState.WARNING)
                    trayManager.setBadge(event.conflicts.size)
                    windowManager.showConflictResolver(event.conflicts.first().id)
                }
                is UIEvent.WindowShouldShow -> windowManager.showMainWindow()
                is UIEvent.TrayNotification -> {
                    trayManager.showNotification(event.title, event.message)
                }
                // ...
            }
        }
    }
}
```

---

## 七、平台特定实现

### 7.1 macOS增强

```kotlin
// macOS: 使用NSStatusBar实现更原生的托盘
class MacOSTrayManager : TrayManager {
    // 通过JNA或JNI调用AppKit
    external fun createStatusBarItem(): Long
    external fun setStatusBarImage(nativeHandle: Long, imagePath: String)
    external fun setStatusBarMenu(nativeHandle: Long, menuItems: Array<String>)

    // Finder Sync Extension集成
    fun updateFinderBadge(path: String, status: FileSyncStatus) {
        // 通过XPC与Finder扩展通信
    }
}

// macOS全局快捷键 (通过JNA调用Carbon或CGEventTap)
class MacOSGlobalShortcuts {
    fun register() {
        // 注册Cmd+Shift+M打开面板
        // 需要在Info.plist中声明权限
    }
}
```

### 7.2 Windows增强

```kotlin
// Windows: Shell Icon Overlay Handler需要COM组件
class WindowsTrayManager : TrayManager {
    // 使用JNA调用Windows API
    fun setOverlayIcon(badge: Int) {
        // 在托盘图标上叠加数字徽章
    }
}

// Windows资源管理器图标覆盖
// 需要实现IShellIconOverlayIdentifier接口
// 注册为COM组件
```

---

## 八、初始化流程

```kotlin
fun main() = application {
    // 1. 初始化单例
    val appStateManager = remember { AppStateManager }
    val windowManager = remember { WindowManager(::exitApplication) }
    val trayManager = remember { createPlatformTrayManager() }
    val toastManager = remember { ToastManager }

    // 2. 启动后台服务
    LaunchedEffect(Unit) {
        SyncEngine.initialize()
        GlobalShortcutManager.register()
    }

    // 3. 安装系统托盘
    LaunchedEffect(Unit) {
        trayManager.install(
            onShowMainWindow = { windowManager.toggleMainWindow() },
            onShowSettings = { windowManager.showSettingsWindow() },
            onSyncNow = { SyncEngine.syncNow() },
            onPauseResume = { SyncEngine.togglePause() },
            onExit = { exitApplication() }
        )
    }

    // 4. 订阅状态变化更新托盘
    LaunchedEffect(Unit) {
        AppStateManager.state.collect { state ->
            trayManager.updateIcon(TrayIconState.fromSyncStatus(state.syncStatus))
            trayManager.updateTooltip("KCloud - ${state.currentOperation ?: "就绪"}")
        }
    }

    // 5. 主窗口 (可隐藏)
    if (windowManager.mainWindowVisible) {
        MainWindow(
            windowManager = windowManager,
            onCloseRequest = { windowManager.hideMainWindow() }
        )
    }

    // 6. 悬浮进度窗口
    if (windowManager.floatingProgressVisible) {
        FloatingProgressWindow(
            appState = appStateManager.state.collectAsState().value,
            onClick = { windowManager.showMainWindow() },
            onDismiss = { windowManager.setFloatingProgressVisible(false) }
        )
    }

    // 7. Toast容器
    ToastHost(toasts = toastManager.toasts.collectAsState().value)

    // 8. 事件处理器
    EventHandler()
}
```

---

## 九、关键实现要点

1. **状态单一数据源**: 所有UI组件从`AppStateManager`订阅状态，避免状态不一致

2. **托盘与窗口联动**: 点击托盘图标切换窗口显示，窗口关闭时最小化到托盘而非退出

3. **动态图标**: 托盘图标需要根据同步状态实时变化，准备多组图标资源

4. **全局快捷键**: 即使窗口隐藏，快捷键也能触发操作

5. **悬浮窗智能显示**: 只在长时间操作时显示，完成后自动消失

6. **通知去重**: 避免短时间内重复通知打扰用户

7. **平台适配**: macOS的菜单栏体验与Windows的托盘体验有所不同，需要分别优化

---

**下一步**: 我可以实现具体的代码文件，比如：
- `AppStateManager.kt` - 状态管理
- `EnhancedTrayManager.kt` - 增强托盘
- `FloatingProgressWindow.kt` - 悬浮进度窗
- `EventBus.kt` - 事件总线

你希望先从哪个部分开始？
