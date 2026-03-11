/**
 * UI 层 - Jetpack Compose 桌面端界面
 *
 * ## 职责
 * - 提供用户交互界面
 * - 订阅状态变化并响应式更新
 * - 处理用户输入和事件
 *
 * ## 架构设计
 * ```
 * ┌─────────────────────────────────────────┐
 * │  screens/                               │
 * │    ├── FileManagerScreen               │
 * │    ├── SettingsScreen                  │
 * │    ├── QuickTransferScreen             │
 * │    └── TransferHistoryScreen           │
 * ├─────────────────────────────────────────┤
 * │  components/                            │
 * │    ├── FileManagerComponents           │
 * │    ├── DragAndDropComponents           │
 * │    └── CommonComponents                │
 * ├─────────────────────────────────────────┤
 * │  theme/                                 │
 * │    ├── Color                           │
 * │    ├── Theme                           │
 * │    └── Type                            │
 * └─────────────────────────────────────────┘
 * ```
 *
 * ## 状态管理
 * - 使用 Compose 的 `collectAsState()` 订阅 [AppStateManager]
 * - 所有 UI 状态来源于单一数据源
 * - 用户操作转换为 EventBus 事件
 *
 * ## 窗口类型
 * - 主窗口: 文件管理器界面
 * - 设置窗口: 配置面板
 * - 冲突解决窗口: 冲突选择对话框
 * - 悬浮进度窗口: 全局进度显示
 *
 * @author zjarlin
 * @since 1.0.0
 */
package com.kcloud.ui
