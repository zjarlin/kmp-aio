/**
 * 版本历史层 - 文件版本控制与管理
 *
 * ## 职责
 * - 管理文件版本历史（基于S3版本控制）
 * - 支持版本恢复、删除、清理
 * - 提供版本对比功能
 *
 * ## 核心组件
 * - [FileVersion] - 文件版本信息
 * - [VersionHistoryManager] - 版本历史管理器
 * - [S3VersionedStorageClient] - 支持版本的存储客户端接口
 *
 * ## 使用场景
 * - 用户需要恢复误删除或误修改的文件
 * - 清理过期版本节省存储空间
 * - 查看文件修改历史
 *
 * ## 设计要点
 * - 依赖存储后端的版本控制功能
 * - 提供版本保留策略配置
 * - 支持批量操作
 *
 * @author zjarlin
 * @since 1.0.0
 */
package com.moveoff.version
