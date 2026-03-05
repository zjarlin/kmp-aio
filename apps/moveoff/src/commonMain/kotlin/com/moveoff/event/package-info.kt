/**
 * 事件总线层 - 跨组件通信机制
 *
 * ## 职责
 * - 提供松耦合的组件间通信
 * - 支持 UI 更新、状态变更、错误通知等场景
 * - 所有事件通过 [EventBus] 统一分发
 *
 * ## 使用方式
 * ```kotlin
 * // 发送事件
 * EventBus.emit(UIEvent.SyncStarted(10, "开始同步"))
 *
 * // 订阅事件
 * EventBus.subscribe(scope) { event ->
 *     when (event) {
 *         is UIEvent.SyncStarted -> handleSync(event)
 *         else -> {}
 *     }
 * }
 * ```
 *
 * ## 事件分类
 * - 同步事件: SyncStarted, SyncProgress, SyncCompleted...
 * - 冲突事件: ConflictDetected, ConflictResolved
 * - 窗口事件: WindowShouldShow, NavigateTo
 * - 连接事件: ConnectionLost, ConnectionRestored
 * - 更新事件: UpdateAvailable, UpdateDownloaded
 *
 * ## 注意事项
 * - 事件总线使用 SharedFlow，支持多订阅者
 * - UI 事件应在主线程消费
 * - 避免在事件处理中发送新事件造成循环
 *
 * @author zjarlin
 * @since 1.0.0
 */
package com.moveoff.event
