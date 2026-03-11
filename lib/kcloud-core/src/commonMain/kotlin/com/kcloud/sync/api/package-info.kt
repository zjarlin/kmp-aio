/**
 * 存储客户端 API - 存储后端的抽象接口
 *
 * ## 职责
 * - 定义存储操作的统一接口 ([StorageClient])
 * - 定义远程对象元数据 ([RemoteObject])
 * - 定义上传/下载结果类型
 *
 * ## 设计原则
 * - 接口设计遵循最小可用原则
 * - 所有操作支持挂起函数 (suspend)
 * - 进度回调使用 (transferred, total) 参数
 *
 * ## 实现类
 * - [com.kcloud.desktop.storage.S3StorageClient] - S3 协议实现
 * - [com.kcloud.desktop.storage.SSHStorageClient] - SSH/SFTP 实现
 * - `FailoverStorageClient`（jvmMain）- 故障转移包装器
 *
 * ## 扩展方式
 * 实现 [StorageClient] 接口即可添加新的存储后端：
 * - WebDAV
 * - 阿里云 OSS
 * - 腾讯云 COS
 * - 本地磁盘（用于测试）
 *
 * @author zjarlin
 * @since 1.0.0
 */
package com.kcloud.sync.api
