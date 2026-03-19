package com.kcloud.features.ssh.server

import com.kcloud.feature.KCloudLocalPaths
import com.kcloud.feature.readKCloudJson
import com.kcloud.feature.writeKCloudJson
import com.kcloud.features.ssh.RemotePathRequest
import com.kcloud.features.ssh.SshActionResult
import com.kcloud.features.ssh.SshAuthMode
import com.kcloud.features.ssh.SshConnectionConfig
import com.kcloud.features.ssh.SshDirectoryEntry
import com.kcloud.features.ssh.SshWorkspaceService
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.io.File
import org.koin.core.annotation.Single

private const val SSH_FEATURE_ID = "ssh"

@Single
class SshWorkspaceServiceImpl : SshWorkspaceService {
    private val settingsFile = File(KCloudLocalPaths.featureDir(SSH_FEATURE_ID), "settings.json")

    override fun loadSettings(): SshConnectionConfig {
        return readKCloudJson(settingsFile) {
            SshConnectionConfig()
        }
    }

    override fun saveSettings(settings: SshConnectionConfig): SshConnectionConfig {
        writeKCloudJson(settingsFile, settings)
        return settings
    }

    override fun testConnection(settings: SshConnectionConfig): SshActionResult {
        return runCatching {
            withSftpClient(settings) { }
            SshActionResult(success = true, message = "SSH 连接成功")
        }.getOrElse { throwable ->
            SshActionResult(success = false, message = throwable.message ?: "SSH 连接失败")
        }
    }

    override fun listDirectory(path: String): List<SshDirectoryEntry> {
        val settings = loadSettings()
        return runCatching {
            withSftpClient(settings) { sftp ->
                sftp.ls(path.ifBlank { settings.remoteRootPath.ifBlank { "." } })
                    .filterNot { file -> file.name == "." || file.name == ".." }
                    .map { file ->
                        SshDirectoryEntry(
                            name = file.name,
                            path = file.path,
                            directory = file.attributes.type?.name?.contains("DIRECTORY") == true,
                            size = file.attributes.size,
                            modifiedAt = file.attributes.mtime * 1000L
                        )
                    }
            }
        }.getOrDefault(emptyList())
    }

    override fun createDirectory(path: String): SshActionResult {
        val settings = loadSettings()
        return runCatching {
            withSftpClient(settings) { sftp ->
                sftp.mkdirs(path)
            }
            SshActionResult(success = true, message = "已创建目录 $path")
        }.getOrElse { throwable ->
            SshActionResult(success = false, message = throwable.message ?: "创建目录失败")
        }
    }

    override fun deletePath(path: String): SshActionResult {
        val settings = loadSettings()
        return runCatching {
            withSftpClient(settings) { sftp ->
                val stat = sftp.statExistence(path)
                if (stat?.type?.name?.contains("DIRECTORY") == true) {
                    sftp.rmdir(path)
                } else {
                    sftp.rm(path)
                }
            }
            SshActionResult(success = true, message = "已删除 $path")
        }.getOrElse { throwable ->
            SshActionResult(success = false, message = throwable.message ?: "删除失败")
        }
    }

    private fun <T> withSftpClient(
        settings: SshConnectionConfig,
        block: (SFTPClient) -> T
    ): T {
        require(settings.host.isNotBlank()) { "Host 不能为空" }
        require(settings.username.isNotBlank()) { "用户名不能为空" }

        val client = SSHClient().apply {
            addHostKeyVerifier(PromiscuousVerifier())
            connect(settings.host, settings.port)
            when (settings.authMode) {
                SshAuthMode.PASSWORD -> authPassword(settings.username, settings.password)
                SshAuthMode.PRIVATE_KEY -> authPublickey(settings.username, settings.privateKeyPath)
            }
        }

        return client.use { ssh ->
            ssh.newSFTPClient().use { sftp ->
                block(sftp)
            }
        }
    }
}
