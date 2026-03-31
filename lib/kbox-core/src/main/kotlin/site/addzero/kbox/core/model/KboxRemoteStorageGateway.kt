package site.addzero.kbox.core.model

import java.io.File

interface KboxRemoteStorageGateway {
    fun validate(config: KboxSshConfig)

    fun ensureDirectory(
        remoteAbsolutePath: String,
        config: KboxSshConfig,
    )

    fun listFiles(
        remoteRootAbsolutePath: String,
        config: KboxSshConfig,
    ): List<KboxRemoteFileInfo>

    fun uploadFile(
        localFile: File,
        remoteAbsolutePath: String,
        config: KboxSshConfig,
        onProgress: ((Long, Long) -> Unit)? = null,
    )

    fun downloadFile(
        remoteAbsolutePath: String,
        localFile: File,
        config: KboxSshConfig,
        onProgress: ((Long, Long) -> Unit)? = null,
    )

    fun readPreview(
        remoteAbsolutePath: String,
        maxBytes: Int,
        config: KboxSshConfig,
    ): ByteArray
}
