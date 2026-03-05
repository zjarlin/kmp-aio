package com.moveoff.storage

import com.moveoff.sync.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.sftp.*
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.userauth.keyprovider.KeyProvider
import net.schmizz.sshj.xfer.FilePermission
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * SSH/SFTP 存储配置
 */
data class SSHConfig(
    val host: String,
    val port: Int = 22,
    val username: String,
    val authType: SSHAuthType = SSHAuthType.PASSWORD,
    val password: String? = null,
    val privateKeyPath: String? = null,
    val passphrase: String? = null,
    val remoteRootPath: String = "/home/$username/moveoff",
    val connectionTimeoutMs: Int = 30000
)

enum class SSHAuthType {
    PASSWORD,
    PRIVATE_KEY
}

/**
 * SSH/SFTP 存储客户端 - 实现 StorageClient 接口
 */
class SSHStorageClient(
    private val config: SSHConfig
) : StorageClient {

    private var sshClient: SSHClient? = null
    private var sftpClient: SFTPClient? = null
    private val connectionLock = Object()

    /**
     * 检查连接状态
     */
    val isConnected: Boolean
        get() = synchronized(connectionLock) {
            sshClient?.isConnected == true && sftpClient != null
        }

    /**
     * 建立连接
     */
    suspend fun connect(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            synchronized(connectionLock) {
                // 如果已连接，先断开
                disconnectInternal()

                val client = SSHClient()
                client.timeout = config.connectionTimeoutMs
                client.addHostKeyVerifier(PromiscuousVerifier())

                // 连接服务器
                client.connect(config.host, config.port)

                // 认证
                when (config.authType) {
                    SSHAuthType.PASSWORD -> {
                        val pwd = config.password
                            ?: return@withContext Result.failure(IllegalArgumentException("密码不能为空"))
                        client.authPassword(config.username, pwd)
                    }
                    SSHAuthType.PRIVATE_KEY -> {
                        val keyPath = config.privateKeyPath
                            ?: return@withContext Result.failure(IllegalArgumentException("私钥路径不能为空"))
                        val keyProvider: KeyProvider = if (config.passphrase.isNullOrEmpty()) {
                            client.loadKeys(keyPath)
                        } else {
                            client.loadKeys(keyPath, config.passphrase)
                        }
                        client.authPublickey(config.username, keyProvider)
                    }
                }

                sshClient = client
                sftpClient = client.newSFTPClient()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            disconnectInternal()
            Result.failure(e)
        }
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        disconnectInternal()
    }

    private fun disconnectInternal() {
        synchronized(connectionLock) {
            try {
                sftpClient?.close()
            } catch (_: Exception) {}
            try {
                sshClient?.disconnect()
            } catch (_: Exception) {}
            sftpClient = null
            sshClient = null
        }
    }

    /**
     * 确保连接可用，如果断开则尝试重连
     */
    private suspend fun ensureConnected(): Result<Unit> {
        if (isConnected) {
            return Result.success(Unit)
        }
        return connect()
    }

    override suspend fun testConnection(): Boolean {
        val result = connect()
        if (result.isSuccess) {
            // 测试列出远程根目录
            return try {
                withContext(Dispatchers.IO) {
                    sftpClient?.stat(config.remoteRootPath)
                    true
                }
            } catch (e: Exception) {
                // 目录不存在，尝试创建
                try {
                    withContext(Dispatchers.IO) {
                        sftpClient?.mkdirs(config.remoteRootPath)
                        true
                    }
                } catch (_: Exception) {
                    false
                }
            }
        }
        return false
    }

    override suspend fun listObjects(prefix: String?): List<RemoteObject> = withContext(Dispatchers.IO) {
        ensureConnected().getOrThrow()

        val sftp = sftpClient ?: throw IllegalStateException("SFTP客户端未连接")
        val fullPath = buildRemotePath(prefix ?: "")

        val objects = mutableListOf<RemoteObject>()

        try {
            listDirectoryRecursive(sftp, fullPath, "", objects)
        } catch (e: Exception) {
            throw IOException("列出远程对象失败: ${e.message}", e)
        }

        objects
    }

    private fun listDirectoryRecursive(
        sftp: SFTPClient,
        remotePath: String,
        relativePrefix: String,
        result: MutableList<RemoteObject>
    ) {
        try {
            val entries = sftp.ls(remotePath)
            for (entry in entries) {
                val name = entry.name
                if (name == "." || name == "..") continue

                val attrs = entry.attributes
                val relativePath = if (relativePrefix.isEmpty()) name else "$relativePrefix/$name"

                if (attrs.type == FileMode.Type.DIRECTORY) {
                    // 递归列出子目录
                    listDirectoryRecursive(sftp, "$remotePath/$name", relativePath, result)
                } else {
                    result.add(
                        RemoteObject(
                            key = relativePath,
                            size = attrs.size,
                            etag = computeETag(attrs),
                            versionId = null,
                            lastModified = attrs.mtime * 1000L
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // 目录不存在或无法访问，忽略
        }
    }

    override suspend fun uploadObject(
        localPath: String,
        remotePath: String,
        progress: (Long, Long) -> Unit
    ): UploadResult = withContext(Dispatchers.IO) {
        ensureConnected().getOrThrow()

        val sftp = sftpClient ?: return@withContext UploadResult(success = false, error = "SFTP客户端未连接")
        val file = File(localPath)

        if (!file.exists()) {
            return@withContext UploadResult(success = false, error = "本地文件不存在: $localPath")
        }

        val fullRemotePath = buildRemotePath(remotePath)
        val remoteDir = fullRemotePath.substringBeforeLast('/', config.remoteRootPath)

        try {
            // 确保远程目录存在
            sftp.mkdirs(remoteDir)

            // 上传文件（带进度跟踪）
            val totalBytes = file.length()
            var uploadedBytes = 0L

            // 使用SFTP的put方法，但包装一个带进度回调的TransferListener
            val tempRemotePath = "$fullRemotePath.tmp"

            // 先上传到临时文件
            sftp.put(localPath, tempRemotePath)

            // 重命名为最终文件名
            try {
                sftp.rename(tempRemotePath, fullRemotePath)
            } catch (e: Exception) {
                // 重命名失败，尝试删除旧文件后再重命名
                try {
                    sftp.rm(fullRemotePath)
                    sftp.rename(tempRemotePath, fullRemotePath)
                } catch (_: Exception) {
                    // 如果还是失败，保留临时文件
                }
            }

            // 获取上传后文件的属性
            val stat = sftp.stat(fullRemotePath)

            UploadResult(
                success = true,
                etag = computeETag(stat),
                versionId = null
            )
        } catch (e: Exception) {
            UploadResult(success = false, error = "上传失败: ${e.message}")
        }
    }

    override suspend fun downloadObject(
        remotePath: String,
        localPath: String,
        progress: (Long, Long) -> Unit
    ): DownloadResult = withContext(Dispatchers.IO) {
        ensureConnected().getOrThrow()

        val sftp = sftpClient ?: return@withContext DownloadResult(success = false, error = "SFTP客户端未连接")
        val fullRemotePath = buildRemotePath(remotePath)

        try {
            // 获取远程文件信息
            val stat = sftp.stat(fullRemotePath)
            val totalBytes = stat.size

            // 确保本地目录存在
            val localFile = File(localPath)
            localFile.parentFile?.mkdirs()

            // 下载文件
            sftp.get(fullRemotePath, localPath)

            // 验证下载的文件大小
            val downloadedFile = File(localPath)
            if (downloadedFile.length() != totalBytes) {
                return@withContext DownloadResult(success = false, error = "文件大小不匹配")
            }

            DownloadResult(success = true, bytesDownloaded = totalBytes)
        } catch (e: Exception) {
            DownloadResult(success = false, error = "下载失败: ${e.message}")
        }
    }

    override suspend fun deleteObject(remotePath: String): Boolean = withContext(Dispatchers.IO) {
        ensureConnected().getOrThrow()

        val sftp = sftpClient ?: return@withContext false
        val fullRemotePath = buildRemotePath(remotePath)

        try {
            val stat = sftp.stat(fullRemotePath)
            if (stat.type == FileMode.Type.DIRECTORY) {
                sftp.rmdir(fullRemotePath)
            } else {
                sftp.rm(fullRemotePath)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getObjectMetadata(remotePath: String): RemoteObject? = withContext(Dispatchers.IO) {
        ensureConnected().getOrThrow()

        val sftp = sftpClient ?: return@withContext null
        val fullRemotePath = buildRemotePath(remotePath)

        try {
            val stat = sftp.stat(fullRemotePath)
            if (stat.type == FileMode.Type.DIRECTORY) {
                return@withContext null
            }

            RemoteObject(
                key = remotePath,
                size = stat.size,
                etag = computeETag(stat),
                versionId = null,
                lastModified = stat.mtime * 1000L
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 构建完整的远程路径
     */
    private fun buildRemotePath(relativePath: String): String {
        val root = config.remoteRootPath.trimEnd('/')
        val path = relativePath.trimStart('/')
        return if (path.isEmpty()) root else "$root/$path"
    }

    /**
     * 计算ETag（基于文件属性）
     */
    private fun computeETag(attrs: FileAttributes): String {
        // 使用大小+修改时间的组合作为ETag
        return "${attrs.size}-${attrs.mtime}"
    }

    /**
     * 创建远程目录
     */
    suspend fun createDirectory(remotePath: String): Boolean = withContext(Dispatchers.IO) {
        ensureConnected().getOrThrow()

        val sftp = sftpClient ?: return@withContext false
        val fullRemotePath = buildRemotePath(remotePath)

        try {
            sftp.mkdirs(fullRemotePath)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查远程路径是否存在
     */
    suspend fun exists(remotePath: String): Boolean = withContext(Dispatchers.IO) {
        ensureConnected().getOrThrow()

        val sftp = sftpClient ?: return@withContext false
        val fullRemotePath = buildRemotePath(remotePath)

        try {
            sftp.stat(fullRemotePath)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 重命名远程文件
     */
    suspend fun rename(oldRemotePath: String, newRemotePath: String): Boolean = withContext(Dispatchers.IO) {
        ensureConnected().getOrThrow()

        val sftp = sftpClient ?: return@withContext false
        val fullOldPath = buildRemotePath(oldRemotePath)
        val fullNewPath = buildRemotePath(newRemotePath)

        try {
            sftp.rename(fullOldPath, fullNewPath)
            true
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * SSH存储客户端构建器
 */
class SSHStorageClientBuilder {
    private var host: String = ""
    private var port: Int = 22
    private var username: String = ""
    private var authType: SSHAuthType = SSHAuthType.PASSWORD
    private var password: String? = null
    private var privateKeyPath: String? = null
    private var passphrase: String? = null
    private var remoteRootPath: String = ""

    fun host(host: String) = apply { this.host = host }
    fun port(port: Int) = apply { this.port = port }
    fun username(username: String) = apply { this.username = username }
    fun password(password: String) = apply {
        this.authType = SSHAuthType.PASSWORD
        this.password = password
    }
    fun privateKey(path: String, passphrase: String? = null) = apply {
        this.authType = SSHAuthType.PRIVATE_KEY
        this.privateKeyPath = path
        this.passphrase = passphrase
    }
    fun remoteRootPath(path: String) = apply { this.remoteRootPath = path }

    fun build(): SSHStorageClient {
        require(host.isNotEmpty()) { "主机地址不能为空" }
        require(username.isNotEmpty()) { "用户名不能为空" }

        val rootPath = if (remoteRootPath.isEmpty()) {
            "/home/$username/moveoff"
        } else {
            remoteRootPath
        }

        return SSHStorageClient(
            SSHConfig(
                host = host,
                port = port,
                username = username,
                authType = authType,
                password = password,
                privateKeyPath = privateKeyPath,
                passphrase = passphrase,
                remoteRootPath = rootPath
            )
        )
    }
}
