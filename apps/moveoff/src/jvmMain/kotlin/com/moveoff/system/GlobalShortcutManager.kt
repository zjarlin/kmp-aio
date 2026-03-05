package com.moveoff.system

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import com.moveoff.event.EventBus
import com.moveoff.event.UIEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.logging.Level
import java.util.logging.Logger

/**
 * 全局快捷键管理器
 *
 * 使用 JNativeHook 库实现系统级快捷键监听
 * 支持 Windows、macOS、Linux
 */
class GlobalShortcutManager {

    private var isRegistered = false
    private val scope = CoroutineScope(Dispatchers.Default)

    companion object {
        // 快捷键定义
        const val SHORTCUT_SHOW_WINDOW = "SHOW_WINDOW"      // Cmd/Ctrl+Shift+M
        const val SHORTCUT_SYNC_NOW = "SYNC_NOW"            // Cmd/Ctrl+Shift+S
        const val SHORTCUT_PAUSE_RESUME = "PAUSE_RESUME"    // Cmd/Ctrl+Shift+P
        const val SHORTCUT_OPEN_SETTINGS = "OPEN_SETTINGS"  // Cmd/Ctrl+Shift+,

        init {
            // 禁用 JNativeHook 的日志输出
            Logger.getLogger(GlobalScreen::class.java.`package`.name).apply {
                level = Level.OFF
                setLevel(Level.OFF)
            }
        }
    }

    private val keyListener = object : NativeKeyListener {
        private val pressedKeys = mutableSetOf<Int>()

        override fun nativeKeyPressed(e: NativeKeyEvent) {
            pressedKeys.add(e.keyCode)
            checkShortcuts()
        }

        override fun nativeKeyReleased(e: NativeKeyEvent) {
            pressedKeys.remove(e.keyCode)
        }

        override fun nativeKeyTyped(e: NativeKeyEvent?) {}

        private fun checkShortcuts() {
            val hasMeta = pressedKeys.contains(NativeKeyEvent.VC_META) ||  // macOS Cmd
                    pressedKeys.contains(NativeKeyEvent.VC_CONTROL)          // Windows/Linux Ctrl
            val hasShift = pressedKeys.contains(NativeKeyEvent.VC_SHIFT)

            if (hasMeta && hasShift) {
                when {
                    // Cmd/Ctrl+Shift+M - 显示/隐藏窗口
                    pressedKeys.contains(NativeKeyEvent.VC_M) -> {
                        scope.launch {
                            EventBus.emit(UIEvent.WindowShouldToggle)
                        }
                    }
                    // Cmd/Ctrl+Shift+S - 立即同步
                    pressedKeys.contains(NativeKeyEvent.VC_S) -> {
                        scope.launch {
                            EventBus.emit(UIEvent.SyncStarted(0, "快捷键触发同步"))
                        }
                    }
                    // Cmd/Ctrl+Shift+P - 暂停/恢复
                    pressedKeys.contains(NativeKeyEvent.VC_P) -> {
                        scope.launch {
                            // TODO: 判断当前状态发送暂停或恢复事件
                            EventBus.emit(UIEvent.SyncPaused())
                        }
                    }
                    // Cmd/Ctrl+Shift+, - 打开设置
                    pressedKeys.contains(NativeKeyEvent.VC_COMMA) -> {
                        scope.launch {
                            EventBus.emit(UIEvent.SettingsShouldOpen)
                        }
                    }
                }
            }
        }
    }

    /**
     * 注册全局快捷键
     */
    fun register(): Boolean {
        return try {
            if (!GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.registerNativeHook()
            }
            GlobalScreen.addNativeKeyListener(keyListener)
            isRegistered = true
            println("全局快捷键已注册")
            println("  Cmd/Ctrl+Shift+M - 显示/隐藏窗口")
            println("  Cmd/Ctrl+Shift+S - 立即同步")
            println("  Cmd/Ctrl+Shift+P - 暂停/恢复同步")
            println("  Cmd/Ctrl+Shift+, - 打开设置")
            true
        } catch (e: Exception) {
            System.err.println("注册全局快捷键失败: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * 注销全局快捷键
     */
    fun unregister() {
        try {
            if (isRegistered) {
                GlobalScreen.removeNativeKeyListener(keyListener)
                GlobalScreen.unregisterNativeHook()
                isRegistered = false
                println("全局快捷键已注销")
            }
        } catch (e: Exception) {
            System.err.println("注销全局快捷键失败: ${e.message}")
        }
    }

    /**
     * 是否已注册
     */
    fun isRegistered(): Boolean = isRegistered
}

/**
 * 全局快捷键管理器单例
 */
object GlobalShortcutManagerInstance {
    private var instance: GlobalShortcutManager? = null

    fun get(): GlobalShortcutManager {
        if (instance == null) {
            instance = GlobalShortcutManager()
        }
        return instance!!
    }

    fun register(): Boolean = get().register()
    fun unregister() = get().unregister()
}
