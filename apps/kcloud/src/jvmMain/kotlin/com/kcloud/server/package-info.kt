/**
 * 本地服务器层 - 提供 HTTP API 和 WebSocket 服务
 *
 * ## 职责
 * - 提供 RESTful API 供外部工具调用
 * - WebSocket 实时推送同步事件
 * - 支持第三方集成和脚本操作
 *
 * ## API 列表
 * ### 同步相关
 * - `GET /api/sync/status` - 获取同步状态
 * - `POST /api/sync/start` - 触发同步
 * - `POST /api/sync/pause` - 暂停同步
 *
 * ### 文件相关
 * - `GET /api/files` - 获取文件列表
 * - `GET /api/files/{path}` - 获取文件元数据
 * - `DELETE /api/files/{path}` - 删除文件
 *
 * ### 冲突相关
 * - `GET /api/conflicts` - 获取冲突列表
 * - `POST /api/conflicts/{id}/resolve` - 解决冲突
 *
 * ## WebSocket 事件
 * - `sync.progress` - 同步进度
 * - `sync.completed` - 同步完成
 * - `conflict.detected` - 冲突检测
 *
 * ## 安全考虑
 * - 仅绑定到 127.0.0.1（本地访问）
 * - 可考虑添加简单的 Token 验证
 *
 * @author zjarlin
 * @since 1.0.0
 */
package com.kcloud.server
