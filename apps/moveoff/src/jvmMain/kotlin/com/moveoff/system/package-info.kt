/**
 * 系统集成层 - 操作系统级功能集成
 *
 * ## 职责
 * - 系统托盘管理 ([EnhancedTrayManager])
 * - 全局快捷键注册 ([GlobalShortcutManager])
 * - 窗口管理 ([WindowManager])
 * - 原生系统集成 ([NativeIntegration])
 *
 * ## 平台支持
 * ### macOS
 * - Finder Sync Extension (Swift)
 * - 状态图标叠加
 * - 右键菜单集成
 *
 * ### Windows
 * - Shell Icon Overlay (COM)
 * - 右键菜单扩展
 * - 系统托盘
 *
 * ### Linux
 * - AppIndicator / StatusNotifier
 * - Nautilus/Dolphin 扩展 (可选)
 *
 * ## 依赖库
 * - JNativeHook - 全局快捷键
 * - Compose Desktop - 系统托盘 API
 * - JNA/JNI - 原生系统集成
 *
 * @author zjarlin
 * @since 1.0.0
 */
package com.moveoff.system
