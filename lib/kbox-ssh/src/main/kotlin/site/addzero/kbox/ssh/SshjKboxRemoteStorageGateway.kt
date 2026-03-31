package site.addzero.kbox.ssh

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.sftp.FileMode
import net.schmizz.sshj.sftp.OpenMode
import net.schmizz.sshj.sftp.RemoteFile
import net.schmizz.sshj.sftp.RemoteResourceInfo
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxRemoteFileInfo
import site.addzero.kbox.core.model.KboxRemoteStorageGateway
import site.addzero.kbox.core.model.KboxSshAuthMode
import site.addzero.kbox.core.model.KboxSshConfig
import java.io.File
import java.security.MessageDigest
import java.util.EnumSet

@Single
class SshjKboxRemoteStorageGateway : KboxRemoteStorageGateway {
    override fun validate(config: KboxSshConfig) {
        withClient(config) {
            Unit
        }
    }

    override fun ensureDirectory(
        remoteAbsolutePath: String,
        config: KboxSshConfig,
    ) {
        val normalizedRemotePath = normalizeRemotePath(remoteAbsolutePath)
        if (normalizedRemotePath == "/") {
            return
        }
        withClient(config) { client ->
            client.newSFTPClient().use { sftp ->
                sftp.mkdirs(normalizedRemotePath)
            }
        }
    }

    override fun listFiles(
        remoteRootAbsolutePath: String,
        config: KboxSshConfig,
    ): List<KboxRemoteFileInfo> {
        return withClient(config) { client ->
            client.newSFTPClient().use { sftp ->
                val normalizedRoot = normalizeRemotePath(remoteRootAbsolutePath)
                val attributes = sftp.statExistence(normalizedRoot) ?: return@use emptyList()
                check(attributes.type == FileMode.Type.DIRECTORY) {
                    "Remote root is not a directory: $normalizedRoot"
                }
                val files = mutableListOf<KboxRemoteFileInfo>()
                walkRemoteDirectory(
                    sftp = sftp,
                    rootPath = normalizedRoot,
                    currentPath = normalizedRoot,
                    sink = files,
                )
                files
            }
        }
    }

    override fun uploadFile(
        localFile: File,
        remoteAbsolutePath: String,
        config: KboxSshConfig,
        onProgress: ((Long, Long) -> Unit)?,
    ) {
        require(localFile.isFile) {
            "Local file does not exist: ${localFile.absolutePath}"
        }
        withClient(config) { client ->
            client.newSFTPClient().use { sftp ->
                val normalizedRemotePath = normalizeRemotePath(remoteAbsolutePath)
                val remoteParent = normalizedRemotePath.substringBeforeLast('/', "")
                if (remoteParent.isNotBlank()) {
                    sftp.mkdirs(remoteParent)
                }
                val totalBytes = localFile.length()
                onProgress?.invoke(0, totalBytes)
                sftp.open(
                    normalizedRemotePath,
                    EnumSet.of(OpenMode.WRITE, OpenMode.CREAT, OpenMode.TRUNC),
                ).use { remoteFile ->
                    localFile.inputStream().buffered().use { input ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var remoteOffset = 0L
                        var transferred = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read <= 0) {
                                break
                            }
                            remoteFile.write(remoteOffset, buffer, 0, read)
                            remoteOffset += read
                            transferred += read
                            onProgress?.invoke(transferred, totalBytes)
                        }
                    }
                }
            }
        }
    }

    override fun downloadFile(
        remoteAbsolutePath: String,
        localFile: File,
        config: KboxSshConfig,
        onProgress: ((Long, Long) -> Unit)?,
    ) {
        withClient(config) { client ->
            client.newSFTPClient().use { sftp ->
                val normalizedRemotePath = normalizeRemotePath(remoteAbsolutePath)
                val totalBytes = sftp.stat(normalizedRemotePath).size
                localFile.parentFile?.mkdirs()
                onProgress?.invoke(0, totalBytes)
                sftp.open(normalizedRemotePath).use { remoteFile ->
                    localFile.outputStream().buffered().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var remoteOffset = 0L
                        var transferred = 0L
                        while (true) {
                            val read = remoteFile.read(remoteOffset, buffer, 0, buffer.size)
                            if (read <= 0) {
                                break
                            }
                            output.write(buffer, 0, read)
                            remoteOffset += read
                            transferred += read
                            onProgress?.invoke(transferred, totalBytes)
                        }
                        output.flush()
                    }
                    localFile.setLastModified(remoteFile.fetchAttributes().mtime * 1000L)
                }
            }
        }
    }

    override fun readPreview(
        remoteAbsolutePath: String,
        maxBytes: Int,
        config: KboxSshConfig,
    ): ByteArray {
        return withClient(config) { client ->
            client.newSFTPClient().use { sftp ->
                sftp.open(normalizeRemotePath(remoteAbsolutePath)).use { remoteFile ->
                    readRemoteBytes(remoteFile, maxBytes)
                }
            }
        }
    }

    private fun walkRemoteDirectory(
        sftp: SFTPClient,
        rootPath: String,
        currentPath: String,
        sink: MutableList<KboxRemoteFileInfo>,
    ) {
        sftp.ls(currentPath).forEach { info ->
            if (info.name == "." || info.name == "..") {
                return@forEach
            }
            val normalizedPath = normalizeRemotePath(info.path)
            when {
                info.isDirectory -> walkRemoteDirectory(
                    sftp = sftp,
                    rootPath = rootPath,
                    currentPath = normalizedPath,
                    sink = sink,
                )

                info.isRegularFile -> sink += info.toRemoteFileInfo(
                    sftp = sftp,
                    rootPath = rootPath,
                    absolutePath = normalizedPath,
                )
            }
        }
    }

    private fun RemoteResourceInfo.toRemoteFileInfo(
        sftp: SFTPClient,
        rootPath: String,
        absolutePath: String,
    ): KboxRemoteFileInfo {
        val md5 = sftp.open(absolutePath).use { remoteFile ->
            remoteFileMd5(remoteFile)
        }
        return KboxRemoteFileInfo(
            absolutePath = absolutePath,
            relativePath = absolutePath.removePrefix(rootPath).trim('/'),
            sizeBytes = attributes.size,
            lastModifiedMillis = attributes.mtime * 1000L,
            md5 = md5,
        )
    }

    private fun remoteFileMd5(
        remoteFile: RemoteFile,
    ): String {
        val digest = MessageDigest.getInstance("MD5")
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var offset = 0L
        while (true) {
            val read = remoteFile.read(offset, buffer, 0, buffer.size)
            if (read <= 0) {
                break
            }
            digest.update(buffer, 0, read)
            offset += read
        }
        return digest.digest().joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    private fun readRemoteBytes(
        remoteFile: RemoteFile,
        maxBytes: Int,
    ): ByteArray {
        if (maxBytes <= 0) {
            return ByteArray(0)
        }
        val target = ByteArray(maxBytes)
        var offset = 0L
        var totalRead = 0
        while (totalRead < maxBytes) {
            val read = remoteFile.read(offset, target, totalRead, maxBytes - totalRead)
            if (read <= 0) {
                break
            }
            offset += read
            totalRead += read
        }
        return target.copyOf(totalRead)
    }

    private fun <T> withClient(
        config: KboxSshConfig,
        action: (SSHClient) -> T,
    ): T {
        require(config.host.isNotBlank()) {
            "SSH host must not be blank"
        }
        require(config.username.isNotBlank()) {
            "SSH username must not be blank"
        }
        val client = SSHClient()
        if (config.strictHostKeyChecking) {
            val knownHosts = File(System.getProperty("user.home").orEmpty(), ".ssh/known_hosts")
            check(knownHosts.isFile) {
                "known_hosts was not found: ${knownHosts.absolutePath}"
            }
            client.loadKnownHosts(knownHosts)
        } else {
            client.addHostKeyVerifier(PromiscuousVerifier())
        }
        client.connect(config.host, config.port)
        try {
            authenticate(client, config)
            return action(client)
        } finally {
            client.close()
        }
    }

    private fun authenticate(
        client: SSHClient,
        config: KboxSshConfig,
    ) {
        when (config.authMode) {
            KboxSshAuthMode.PASSWORD -> {
                require(config.password.isNotBlank()) {
                    "password must not be blank when password auth is selected"
                }
                client.authPassword(config.username, config.password)
            }

            KboxSshAuthMode.PRIVATE_KEY -> {
                require(config.privateKeyPath.isNotBlank()) {
                    "privateKeyPath must not be blank when private key auth is selected"
                }
                val keyProvider = if (config.privateKeyPassphrase.isBlank()) {
                    client.loadKeys(config.privateKeyPath)
                } else {
                    client.loadKeys(
                        config.privateKeyPath,
                        config.privateKeyPassphrase,
                    )
                }
                client.authPublickey(config.username, keyProvider)
            }
        }
    }

    private fun normalizeRemotePath(
        path: String,
    ): String {
        return path.replace('\\', '/')
            .replace(Regex("/{2,}"), "/")
            .ifBlank { "/" }
    }
}
