/**
 * 数据库实现层 - SQLite 具体实现
 *
 * ## 职责
 * - 实现 [com.moveoff.db.Database] 接口
 * - 管理数据库连接池 (HikariCP)
 * - 处理数据库迁移和版本管理
 *
 * ## 技术栈
 * - SQLite (jdbc-sqlite)
 * - HikariCP (连接池)
 * - kotlinx.coroutines (异步支持)
 *
 * ## 数据库文件位置
 * - Windows: `%LOCALAPPDATA%/MoveOff/moveoff.db`
 * - macOS: `~/Library/Application Support/MoveOff/moveoff.db`
 * - Linux: `~/.config/MoveOff/moveoff.db`
 *
 * ## Schema 版本管理
 * 使用 Flyway 或手动版本控制进行数据库迁移
 *
 * @author zjarlin
 * @since 1.0.0
 */
package com.moveoff.db
