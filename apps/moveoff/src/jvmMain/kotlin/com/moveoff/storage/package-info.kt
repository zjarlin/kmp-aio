/**
 * 存储客户端实现 - 具体存储后端实现
 *
 * ## 职责
 * - 提供 [StorageClient] 接口的具体实现
 * - 管理存储连接的生命周期
 * - 处理存储特定的错误和重试逻辑
 *
 * ## 实现类
 * - [S3StorageClient] - AWS S3 / MinIO / 阿里云 OSS 兼容实现
 * - [SSHStorageClient] - SSH/SFTP 协议实现
 * - [com.moveoff.sync.FailoverStorageClient] - 故障转移包装器（S3 优先，SSH 备用）
 *
 * ## 配置类
 * - [S3Config] - S3 连接配置
 * - [SSHConfig] - SSH 连接配置
 * - [SSHAuthType] - SSH 认证类型枚举
 *
 * ## 选择策略
 * - 生产环境：使用 [com.moveoff.sync.FailoverStorageClient] 保证可用性
 * - 局域网：可直接使用 [SSHStorageClient] 获得更好性能
 * - 测试环境：使用内存实现或本地目录实现
 *
 * @author zjarlin
 * @since 1.0.0
 */
package com.moveoff.storage
