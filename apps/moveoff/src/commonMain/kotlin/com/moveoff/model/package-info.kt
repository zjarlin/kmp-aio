/**
 * 数据模型层 - 定义核心业务实体
 *
 * ## 职责
 * - 定义所有跨层传输的数据结构
 * - 提供序列化支持 (Kotlinx Serialization)
 * - 保持与平台无关，可在任何 Kotlin 平台使用
 *
 * ## 设计原则
 * - 数据类应为不可变 (val) 优先
 * - 集合属性使用空列表而非 null
 * - ID 生成由业务层控制，不依赖数据库自增
 *
 * ## 主要模型
 * - [ServerConfig] - SSH/S3 服务器配置
 * - [AppSettings] - 应用全局设置
 * - [TransferTask] - 传输任务
 * - [Conflict] - 冲突信息
 *
 * @author zjarlin
 * @since 1.0.0
 */
package com.moveoff.model
