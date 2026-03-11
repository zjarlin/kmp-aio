/**
 * 数据库接口层 - 数据持久化抽象
 *
 * ## 职责
 * - 定义数据库操作的契约接口 ([Database])
 * - 定义实体类 ([FileRecord], [SyncQueueItem])
 * - 定义枚举类型 (SyncState, SyncOperation, QueueStatus)
 * - 提供变更检测的数据结构 ([FileChange])
 *
 * ## 分层说明
 * - `commonMain`: 仅包含接口定义和实体类（平台无关）
 * - `jvmMain`: 提供具体实现 ([com.kcloud.desktop.db.DatabaseImpl])
 *
 * ## 核心表
 * - files - 文件元数据记录
 * - sync_queue - 待同步任务队列
 *
 * ## 设计原则
 * - 接口方法命名清晰，见名知意
 * - 返回 Flow 的方法支持 UI 实时更新
 * - 所有变更通过 FileChange 密封类表达
 *
 * @author zjarlin
 * @since 1.0.0
 */
package com.kcloud.db
