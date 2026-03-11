/**
 * UI 页面层 - 各功能页面的完整实现
 *
 * ## 职责
 * - 实现具体功能页面
 * - 组合 components 构建完整界面
 * - 处理页面级别的状态和逻辑
 *
 * ## 页面列表
 * - [FileManagerScreen] - 文件管理主界面（列表/网格视图、状态图标、右键菜单）
 * - [SettingsScreen] - 设置面板（S3/SSH配置、同步策略、主题设置）
 * - [QuickTransferScreen] - 快速传输界面（拖拽上传、快捷操作）
 * - [TransferHistoryScreen] - 传输历史记录
 * - [ServerManagementScreen] - 服务器管理（多服务器配置）
 *
 * ## 设计规范
 * - 每个 Screen 都是一个 Composable 函数
 * - 通过参数接收状态和回调，保持可测试性
 * - 复杂逻辑委托给 ViewModel 或 Manager
 *
 * @author zjarlin
 * @since 1.0.0
 */
package com.kcloud.ui.screens
