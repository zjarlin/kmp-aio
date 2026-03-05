package com.moveoff.system

import com.moveoff.event.EventBus
import com.moveoff.event.UIEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * 原生系统集成管理器
 *
 * 管理 Finder/Explorer 右键菜单和状态图标集成
 */
object NativeIntegration {

    private var isInitialized = false
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * 初始化原生集成
     */
    fun initialize() {
        if (isInitialized) return

        val osName = System.getProperty("os.name").lowercase()
        when {
            osName.contains("mac") -> initializeMacOS()
            osName.contains("win") -> initializeWindows()
            osName.contains("linux") -> initializeLinux()
        }

        isInitialized = true
    }

    /**
     * 发送文件状态更新到原生扩展
     */
    fun updateFileStatus(path: String, status: FileSyncStatus) {
        val osName = System.getProperty("os.name").lowercase()
        when {
            osName.contains("mac") -> updateMacOSFileStatus(path, status)
            osName.contains("win") -> updateWindowsFileStatus(path, status)
        }
    }

    /**
     * 处理来自原生扩展的消息
     */
    fun handleNativeMessage(action: String, path: String) {
        scope.launch {
            when (action) {
                "SYNC_NOW" -> {
                    EventBus.emit(UIEvent.SyncStarted(1, "右键菜单触发同步"))
                }
                "SHOW_IN_APP" -> {
                    EventBus.emit(UIEvent.WindowShouldShow)
                    // TODO: 选中该文件
                }
                "RESOLVE_CONFLICT" -> {
                    EventBus.emit(UIEvent.WindowShouldShow)
                    // TODO: 打开冲突解决
                }
            }
        }
    }

    // ========== macOS 实现 ==========

    private fun initializeMacOS() {
        try {
            // 检查 Finder 扩展是否已启用
            val enabled = checkFinderExtensionEnabled()
            if (!enabled) {
                println("警告: Finder 扩展未启用，请手动在系统设置中启用 MoveOffFinderExtension")
            }

            // 启动与扩展的通信
            startMacOSCommunication()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkFinderExtensionEnabled(): Boolean {
        return try {
            val process = ProcessBuilder(
                "pluginkit", "-m", "-p", "com.apple.FinderSync"
            ).start()
            val output = process.inputStream.bufferedReader().readText()
            output.contains("moveoff")
        } catch (e: Exception) {
            false
        }
    }

    private fun startMacOSCommunication() {
        // 通过 XPC 或 Socket 与 Finder 扩展通信
        // TODO: 实现与 Swift 扩展的通信机制
    }

    private fun updateMacOSFileStatus(path: String, status: FileSyncStatus) {
        // 通过 XPC 发送状态更新
        // TODO: 实现状态更新
    }

    // ========== Windows 实现 ==========

    private fun initializeWindows() {
        try {
            // 注册 Shell 扩展
            registerShellExtension()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun registerShellExtension() {
        // 检查注册表项是否存在
        val regPath = "HKCR\\CLSID\\{Your-GUID-Here}"
        // TODO: 实现注册表检查和注册
    }

    private fun updateWindowsFileStatus(path: String, status: FileSyncStatus) {
        // 更新文件属性或图标覆盖
        // TODO: 实现 Windows 图标覆盖
    }

    // ========== Linux 实现 ==========

    private fun initializeLinux() {
        // Linux 文件管理器扩展（Nautilus/Files, Dolphin等）
        // TODO: 实现 Linux 支持
    }
}

/**
 * 文件同步状态（用于状态图标）
 */
enum class FileSyncStatus {
    SYNCED,         // 绿色勾
    SYNCING,        // 蓝色同步中
    PENDING_UPLOAD, // 黄色等待上传
    PENDING_DOWNLOAD, // 黄色等待下载
    CONFLICT,       // 红色感叹号
    ERROR           // 红色叉
}

/**
 * 原生桥接接口
 */
interface NativeBridge {
    fun sendMessage(message: String)
    fun setListener(listener: (String) -> Unit)
}
