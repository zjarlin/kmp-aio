/**
 * KCloud - 插件化私有云工作台
 *
 * ## 应用入口
 * [main] 函数是应用启动入口，负责按顺序初始化所有子系统：
 *
 * ### 初始化顺序
 * 1. 数据库 (SQLite) - [com.kcloud.db.DatabaseManager]
 * 2. 存储客户端 (S3/SSH) - [com.kcloud.plugins.servermanagement.server.storage.S3StorageClient], [com.kcloud.storage.SSHStorageClient]
 * 3. 同步引擎 - [com.kcloud.sync.SyncEngineManager]
 * 4. 本地服务器 (HTTP API) - [com.kcloud.server.LocalServerManager]
 * 5. 自动更新检查 - [com.kcloud.update.UpdateCheckerManager]
 * 6. 原生系统集成 - [com.kcloud.system.NativeIntegration]
 * 7. 全局快捷键 - [com.kcloud.plugins.desktop.system.GlobalShortcutManagerInstance]
 * 8. 系统托盘 - [com.kcloud.system.EnhancedTrayManager]
 * 9. UI 窗口管理 - [com.kcloud.plugins.desktop.system.WindowManager]
 *
 * ### 资源清理
 * 应用退出时按相反顺序清理资源，确保数据完整性。
 *
 * ### 架构层次
 * ```
 * UI 层 (Compose Desktop)
 *    ↓
 * 系统层 (托盘、快捷键、原生集成)
 *    ↓
 * 服务层 (本地HTTP服务器)
 *    ↓
 * 同步层 (SyncEngine)
 *    ↓
 * 存储层 (S3/SSH客户端)
 *    ↓
 * 数据层 (SQLite数据库)
 * ```
 *
 * @author zjarlin
 * @since 1.0.0
 */
package com.kcloud
