/**
 * MoveOff - S3优先的跨平台文件同步工具
 *
 * ## 应用入口
 * [main] 函数是应用启动入口，负责按顺序初始化所有子系统：
 *
 * ### 初始化顺序
 * 1. 数据库 (SQLite) - [com.moveoff.db.DatabaseManager]
 * 2. 存储客户端 (S3/SSH) - [com.moveoff.storage.S3StorageClient], [com.moveoff.storage.SSHStorageClient]
 * 3. 同步引擎 - [com.moveoff.sync.SyncEngineManager]
 * 4. 本地服务器 (HTTP API) - [com.moveoff.server.LocalServerManager]
 * 5. 自动更新检查 - [com.moveoff.update.UpdateCheckerManager]
 * 6. 原生系统集成 - [com.moveoff.system.NativeIntegration]
 * 7. 全局快捷键 - [com.moveoff.system.GlobalShortcutManagerInstance]
 * 8. 系统托盘 - [com.moveoff.system.EnhancedTrayManager]
 * 9. UI 窗口管理 - [com.moveoff.system.WindowManager]
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
package com.moveoff
