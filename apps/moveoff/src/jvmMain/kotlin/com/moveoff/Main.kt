package com.moveoff

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import androidx.compose.material3.Text
import com.moveoff.db.DatabaseImpl
import com.moveoff.db.DatabaseManager
import com.moveoff.event.EventBus
import com.moveoff.event.UIEvent
import com.moveoff.server.LocalServerManager
import com.moveoff.storage.S3StorageClient
import com.moveoff.sync.SyncEngineManager
import com.moveoff.sync.FailoverStorageManager
import com.moveoff.sync.StorageBackendType
import com.moveoff.system.*
import com.moveoff.ui.MainWindow
import com.moveoff.storage.SSHStorageClient
import com.moveoff.storage.SSHConfig
import com.moveoff.storage.SSHAuthType
import com.moveoff.storage.SettingsStorage
import com.moveoff.update.UpdateCheckerManager
import kotlinx.coroutines.*
import java.io.File

/**
 * MoveOff 应用入口
 *
 * 初始化顺序：
 * 1. 数据库 (SQLite)
 * 2. 存储客户端 (S3)
 * 3. 同步引擎
 * 4. 本地服务器 (HTTP API)
 * 5. 原生系统集成 (Finder/Explorer)
 * 6. 系统托盘
 */
fun main() = application {
    val scope = rememberCoroutineScope()

    // ========== 1. 初始化数据库 ==========
    LaunchedEffect(Unit) {
        try {
            DatabaseManager.initialize()
            println("数据库初始化成功: ${DatabaseImpl.getDefaultDbPath()}")
        } catch (e: Exception) {
            println("数据库初始化失败: ${e.message}")
            e.printStackTrace()
        }
    }

    // ========== 2. 初始化存储客户端和同步引擎 ==========
    LaunchedEffect(Unit) {
        try {
            // 从环境变量或配置读取 S3 配置
            val s3Client = S3StorageClient(
                com.moveoff.storage.S3Config(
                    endpoint = System.getenv("S3_ENDPOINT") ?: "http://localhost:9000",
                    region = "us-east-1",
                    bucket = System.getenv("S3_BUCKET") ?: "moveoff",
                    accessKey = System.getenv("S3_ACCESS_KEY") ?: "minioadmin",
                    secretKey = System.getenv("S3_SECRET_KEY") ?: "minioadmin",
                    prefix = "sync/",
                    forcePathStyle = true  // MinIO需要
                )
            )

            // 从环境变量读取 SSH 配置（如果存在）
            val sshHost = System.getenv("SSH_HOST")
            val sshClient = if (!sshHost.isNullOrBlank()) {
                SSHStorageClient(
                    SSHConfig(
                        host = sshHost,
                        port = System.getenv("SSH_PORT")?.toIntOrNull() ?: 22,
                        username = System.getenv("SSH_USERNAME") ?: "",
                        authType = if (System.getenv("SSH_PRIVATE_KEY") != null)
                            SSHAuthType.PRIVATE_KEY else SSHAuthType.PASSWORD,
                        password = System.getenv("SSH_PASSWORD"),
                        privateKeyPath = System.getenv("SSH_PRIVATE_KEY"),
                        passphrase = System.getenv("SSH_PASSPHRASE"),
                        remoteRootPath = System.getenv("SSH_REMOTE_PATH") ?: "/home/${System.getenv("SSH_USERNAME")}/moveoff"
                    )
                )
            } else null

            // 获取同步根目录
            val syncRoot = getSyncRoot()

            // 初始化故障转移存储客户端（S3 优先，SSH 备用）
            val failoverClient = if (sshClient != null) {
                FailoverStorageManager.initialize(
                    primaryClient = s3Client,
                    primaryType = StorageBackendType.S3,
                    fallbackClient = sshClient,
                    fallbackType = StorageBackendType.SSH
                )
                println("故障转移存储客户端已初始化（S3 主，SSH 备）")
                FailoverStorageManager.get()
            } else {
                // 没有配置 SSH，直接使用 S3
                println("仅使用 S3 存储（未配置 SSH 备用）")
                s3Client
            }

            // 初始化同步引擎
            SyncEngineManager.initialize(
                database = DatabaseManager.get(),
                storageClient = failoverClient,
                syncRoot = syncRoot
            )
            println("同步引擎初始化成功，同步目录: $syncRoot")

        } catch (e: Exception) {
            println("同步引擎初始化失败: ${e.message}")
            e.printStackTrace()
        }
    }

    // ========== 3. 启动本地服务器 ==========
    LaunchedEffect(Unit) {
        try {
            LocalServerManager.start(port = 18475)
            println("本地服务器已启动: http://127.0.0.1:18475")
        } catch (e: Exception) {
            println("本地服务器启动失败: ${e.message}")
        }
    }

    // ========== 4. 初始化自动更新检查 ==========
    LaunchedEffect(Unit) {
        try {
            val settings = SettingsStorage.loadSettings()
            UpdateCheckerManager.initialize(
                currentVersion = "1.0.0", // 应该从构建配置读取
                updateSettings = settings.updateSettings
            )
            println("自动更新检查器已初始化")
        } catch (e: Exception) {
            println("自动更新检查器初始化失败: ${e.message}")
        }
    }

    // ========== 4. 初始化原生系统集成 ==========
    LaunchedEffect(Unit) {
        try {
            NativeIntegration.initialize()
            println("原生系统集成已初始化")
        } catch (e: Exception) {
            println("原生系统集成初始化失败: ${e.message}")
        }
    }

    // ========== 5. 注册全局快捷键 ==========
    LaunchedEffect(Unit) {
        val registered = GlobalShortcutManagerInstance.register()
        if (registered) {
            println("全局快捷键已注册")
        } else {
            println("警告: 全局快捷键注册失败")
        }
    }

    // ========== 5. 初始化核心UI组件 ==========
    val trayManager = remember { createTrayManager(scope) }
    val windowManager = remember { WindowManager(::exitApplication, scope) }
    val toastManager = remember { ToastManager(scope) }

    // ========== 5. 订阅全局事件 ==========
    LaunchedEffect(Unit) {
        EventBus.events.collect { event ->
            when (event) {
                is UIEvent.TrayNotification -> {
                    toastManager.show(
                        message = "${event.title}: ${event.message}",
                        type = when (event.type) {
                            com.moveoff.event.NotificationType.SUCCESS -> ToastType.SUCCESS
                            com.moveoff.event.NotificationType.ERROR -> ToastType.ERROR
                            com.moveoff.event.NotificationType.WARNING -> ToastType.WARNING
                            else -> ToastType.INFO
                        }
                    )
                }
                else -> {}
            }
        }
    }

    // ========== 6. 安装系统托盘 ==========
    LaunchedEffect(Unit) {
        val installed = trayManager.install(
            onShowMainWindow = { windowManager.toggleMainWindow() },
            onShowSettings = { windowManager.showSettingsWindow() },
            onSyncNow = {
                scope.launch {
                    try {
                        val syncEngine = SyncEngineManager.get()
                        syncEngine.syncNow()
                    } catch (e: Exception) {
                        com.moveoff.event.EventShortcuts.notifyError("同步失败", e.message ?: "未知错误")
                    }
                }
            },
            onPauseResume = {
                try {
                    SyncEngineManager.get()
                    // TODO: 检查当前状态决定暂停还是恢复
                } catch (_: Exception) {
                    // 忽略
                }
            },
            onExit = { exitApplication() }
        )

        if (!installed) {
            println("警告: 系统托盘安装失败，应用将以普通窗口模式运行")
        }
    }

    // ========== 7. 窗口管理 ==========
    if (windowManager.mainWindowVisible) {
        MainWindow(
            windowManager = windowManager,
            toastManager = toastManager
        )
    }

    if (windowManager.settingsWindowVisible) {
        SettingsWindow(
            onCloseRequest = { windowManager.closeSettingsWindow() }
        ) {
            // TODO: 设置内容
            Text("设置面板 - 待实现")
        }
    }

    if (windowManager.conflictWindowVisible) {
        ConflictResolutionWindow(
            conflictId = windowManager.currentConflictId ?: "",
            onCloseRequest = { windowManager.closeConflictWindow() },
            onResolve = { _ ->
                // TODO: 处理冲突解决
                windowManager.closeConflictWindow()
            }
        )
    }

    if (windowManager.floatingProgressVisible) {
        FloatingProgressWindow(
            windowManager = windowManager,
            onDismiss = { windowManager.hideFloatingProgress() }
        )
    }

    // ========== 9. 资源清理 ==========
    DisposableEffect(Unit) {
        onDispose {
            GlobalShortcutManagerInstance.unregister()
            trayManager.uninstall()
            LocalServerManager.stop()
            SyncEngineManager.stop()
            FailoverStorageManager.stop()
            UpdateCheckerManager.stop()
            DatabaseManager.close()
        }
    }
}

/**
 * 获取同步根目录
 */
private fun getSyncRoot(): String {
    val userHome = System.getProperty("user.home")
    val syncDir = File(userHome, "MoveOff")
    if (!syncDir.exists()) {
        syncDir.mkdirs()
    }
    return syncDir.absolutePath
}

/**
 * 主窗口
 */
@Composable
private fun MainWindow(
    windowManager: WindowManager,
    toastManager: ToastManager
) {
    val windowState = rememberWindowState(
        width = 1200.dp,
        height = 800.dp,
        position = windowManager.getMainWindowPosition()
    )

    Window(
        onCloseRequest = { windowManager.hideMainWindow() },
        title = "MoveOff - 文件同步",
        state = windowState,
        visible = true
    ) {
        window.minimumSize = java.awt.Dimension(900, 600)

        MainWindow()

        // Toast容器
        ToastHost(
            toasts = toastManager.toasts,
            modifier = Modifier.fillMaxSize()
        )
    }
}
