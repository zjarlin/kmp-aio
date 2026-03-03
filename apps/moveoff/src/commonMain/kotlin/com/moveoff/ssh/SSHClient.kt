package com.moveoff.ssh

import com.moveoff.model.RemoteFile
import com.moveoff.model.ServerConfig
import com.moveoff.progress.StageUpdate
import com.moveoff.progress.TransferStage
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient as SSHJClient
import net.schmizz.sshj.common.IOUtils
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.sftp.*
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.userauth.keyprovider.KeyProvider
import net.schmizz.sshj.xfer.*
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

class SSHClient(private val config: ServerConfig) {
    private var sshClient: SSHJClient? = null
    private var sftpClient: SFTPClient? = null

    val isConnected: Boolean
        get() = sshClient?.isConnected == true && sftpClient != null

    suspend fun connect(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            logger.info { "Connecting to ${config.host}:${config.port} as ${config.username}" }

            val client = SSHJClient()
            client.addHostKeyVerifier(PromiscuousVerifier())

            client.connect(config.host, config.port)

            when (config.authType) {
                com.moveoff.model.AuthType.PASSWORD -> {
                    client.authPassword(config.username, config.password ?: "")
                }
                com.moveoff.model.AuthType.PRIVATE_KEY -> {
                    val keyPath = config.privateKeyPath
                        ?: return@withContext Result.failure(IllegalArgumentException("Private key path not provided"))
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

            logger.info { "Successfully connected to ${config.host}" }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Failed to connect to ${config.host}" }
            disconnect()
            Result.failure(e)
        }
    }

    fun disconnect() {
        try {
            sftpClient?.close()
            sshClient?.disconnect()
            logger.info { "Disconnected from ${config.host}" }
        } catch (e: Exception) {
            logger.error(e) { "Error during disconnect" }
        } finally {
            sftpClient = null
            sshClient = null
        }
    }

    suspend fun listRemoteDirectory(remotePath: String): Result<List<RemoteFile>> = withContext(Dispatchers.IO) {
        try {
            val sftp = sftpClient ?: return@withContext Result.failure(IllegalStateException("Not connected"))

            val entries = sftp.ls(remotePath)
            val files = entries.map { entry ->
                val attrs = entry.attributes
                RemoteFile(
                    name = entry.name,
                    path = "$remotePath/${entry.name}",
                    size = attrs.size,
                    isDirectory = attrs.type == FileMode.Type.DIRECTORY,
                    modifiedTime = attrs.mtime * 1000L,
                    extension = entry.name.substringAfterLast('.', "").takeIf { it.isNotEmpty() }
                )
            }.filter { it.name != "." && it.name != ".." }

            Result.success(files)
        } catch (e: Exception) {
            logger.error(e) { "Failed to list directory: $remotePath" }
            Result.failure(e)
        }
    }

    suspend fun uploadFile(
        localFile: File,
        remotePath: String,
        onProgress: suspend (StageUpdate) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val taskId = "upload_${System.currentTimeMillis()}_${localFile.name}"
        val totalBytes = localFile.length()

        try {
            val sftp = sftpClient ?: return@withContext Result.failure(IllegalStateException("Not connected"))

            // PRECHECK
            onProgress(StageUpdate(
                taskId = taskId,
                fileName = localFile.name,
                stage = TransferStage.PRECHECK,
                stageProgress = 1.0,
                totalBytes = totalBytes
            ))

            // SCAN_LOCAL
            onProgress(StageUpdate(
                taskId = taskId,
                fileName = localFile.name,
                stage = TransferStage.SCAN_LOCAL,
                stageProgress = 1.0,
                totalBytes = totalBytes
            ))

            // Ensure remote directory exists
            ensureRemoteDirectory(remotePath)

            val remoteFilePath = "$remotePath/${localFile.name}"

            // TRANSFER - simplified without progress callback
            sftp.put(localFile.absolutePath, remoteFilePath)

            // VERIFY
            onProgress(StageUpdate(
                taskId = taskId,
                fileName = localFile.name,
                stage = TransferStage.VERIFY,
                stageProgress = 1.0,
                transferredBytes = totalBytes,
                totalBytes = totalBytes
            ))

            // FINALIZE
            onProgress(StageUpdate(
                taskId = taskId,
                fileName = localFile.name,
                stage = TransferStage.FINALIZE,
                stageProgress = 1.0,
                transferredBytes = totalBytes,
                totalBytes = totalBytes
            ))

            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Failed to upload file: ${localFile.name}" }
            onProgress(StageUpdate(
                taskId = taskId,
                fileName = localFile.name,
                stage = TransferStage.TRANSFER,
                stageProgress = 0.0,
                totalBytes = totalBytes,
                errorMessage = e.message
            ))
            Result.failure(e)
        }
    }

    suspend fun downloadFile(
        remotePath: String,
        localFile: File,
        onProgress: suspend (StageUpdate) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val taskId = "download_${System.currentTimeMillis()}_${localFile.name}"

        try {
            val sftp = sftpClient ?: return@withContext Result.failure(IllegalStateException("Not connected"))

            val remoteFile = sftp.stat(remotePath)
            val totalBytes = remoteFile.size

            // PRECHECK
            onProgress(StageUpdate(
                taskId = taskId,
                fileName = localFile.name,
                stage = TransferStage.PRECHECK,
                stageProgress = 1.0,
                totalBytes = totalBytes
            ))

            // SCAN_LOCAL
            onProgress(StageUpdate(
                taskId = taskId,
                fileName = localFile.name,
                stage = TransferStage.SCAN_LOCAL,
                stageProgress = 1.0,
                totalBytes = totalBytes
            ))

            localFile.parentFile?.mkdirs()

            // TRANSFER - simplified without progress callback
            sftp.get(remotePath, localFile.absolutePath)

            // VERIFY
            onProgress(StageUpdate(
                taskId = taskId,
                fileName = localFile.name,
                stage = TransferStage.VERIFY,
                stageProgress = 1.0,
                transferredBytes = totalBytes,
                totalBytes = totalBytes
            ))

            // FINALIZE
            onProgress(StageUpdate(
                taskId = taskId,
                fileName = localFile.name,
                stage = TransferStage.FINALIZE,
                stageProgress = 1.0,
                transferredBytes = totalBytes,
                totalBytes = totalBytes
            ))

            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Failed to download file: $remotePath" }
            onProgress(StageUpdate(
                taskId = taskId,
                fileName = localFile.name,
                stage = TransferStage.TRANSFER,
                stageProgress = 0.0,
                errorMessage = e.message
            ))
            Result.failure(e)
        }
    }

    suspend fun deleteRemoteFile(remotePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val sftp = sftpClient ?: return@withContext Result.failure(IllegalStateException("Not connected"))

            val stat = sftp.stat(remotePath)
            if (stat.type == FileMode.Type.DIRECTORY) {
                sftp.rmdir(remotePath)
            } else {
                sftp.rm(remotePath)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete remote file: $remotePath" }
            Result.failure(e)
        }
    }

    suspend fun renameRemoteFile(oldPath: String, newPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val sftp = sftpClient ?: return@withContext Result.failure(IllegalStateException("Not connected"))
            sftp.rename(oldPath, newPath)
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Failed to rename remote file from $oldPath to $newPath" }
            Result.failure(e)
        }
    }

    suspend fun createRemoteDirectory(remotePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val sftp = sftpClient ?: return@withContext Result.failure(IllegalStateException("Not connected"))
            sftp.mkdirs(remotePath)
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Failed to create remote directory: $remotePath" }
            Result.failure(e)
        }
    }

    suspend fun getRemoteDiskUsage(remotePath: String): Result<Pair<Long, Long>> = withContext(Dispatchers.IO) {
        try {
            val client = sshClient ?: return@withContext Result.failure(IllegalStateException("Not connected"))

            val session = client.startSession()
            val cmd = session.exec("df -B1 $remotePath | tail -1 | awk '{print \$3, \$2}'")
            cmd.join(5, TimeUnit.SECONDS)

            val output = IOUtils.readFully(cmd.inputStream).toString().trim()
            val parts = output.split(" ")

            if (parts.size >= 2) {
                val used = parts[0].toLongOrNull() ?: 0L
                val total = parts[1].toLongOrNull() ?: 0L
                Result.success(used to total)
            } else {
                Result.failure(IOException("Failed to parse disk usage"))
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to get remote disk usage" }
            Result.failure(e)
        }
    }

    private fun ensureRemoteDirectory(remotePath: String) {
        try {
            sftpClient?.mkdirs(remotePath)
        } catch (e: Exception) {
            // Directory might already exist
        }
    }
}

class SSHClientPool {
    private val clients = mutableMapOf<String, SSHClient>()

    suspend fun getClient(config: ServerConfig): SSHClient {
        return clients.getOrPut(config.id) {
            SSHClient(config).also { it.connect() }
        }
    }

    fun removeClient(serverId: String) {
        clients.remove(serverId)?.disconnect()
    }

    fun disconnectAll() {
        clients.values.forEach { it.disconnect() }
        clients.clear()
    }
}
