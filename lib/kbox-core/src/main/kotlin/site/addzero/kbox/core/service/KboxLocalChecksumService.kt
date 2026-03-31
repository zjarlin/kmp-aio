package site.addzero.kbox.core.service

import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxComparePreviewMode
import site.addzero.kbox.core.model.KboxSyncFileSnapshot
import site.addzero.kbox.core.model.KboxSyncMappingConfig
import site.addzero.kbox.core.support.fileMd5
import site.addzero.kbox.core.support.looksLikeText
import site.addzero.kbox.core.support.normalizeRelativePath
import site.addzero.kbox.core.support.readFilePreview
import site.addzero.kbox.core.support.walkRegularFiles
import java.io.File

@Single
class KboxLocalChecksumService {
    fun scanLocalFiles(
        mapping: KboxSyncMappingConfig,
    ): Map<String, KboxSyncFileSnapshot> {
        val root = File(mapping.localRoot).absoluteFile
        if (!root.exists()) {
            root.mkdirs()
        }
        if (!root.isDirectory) {
            return emptyMap()
        }
        val files = linkedMapOf<String, KboxSyncFileSnapshot>()
        walkRegularFiles(listOf(root)) { file ->
            val relativePath = normalizeRelativePath(
                root.toPath().relativize(file.toPath()).toString(),
            )
            if (relativePath.isBlank()) {
                return@walkRegularFiles
            }
            files[relativePath] = snapshotFile(
                file = file,
                relativePath = relativePath,
            )
        }
        return files
    }

    fun snapshotFile(
        file: File,
        relativePath: String,
    ): KboxSyncFileSnapshot {
        return KboxSyncFileSnapshot(
            absolutePath = file.absolutePath,
            relativePath = normalizeRelativePath(relativePath),
            sizeBytes = file.length(),
            lastModifiedMillis = file.lastModified(),
            md5 = fileMd5(file),
        )
    }

    fun readPreview(
        file: File,
        maxBytes: Int,
    ): ByteArray {
        if (!file.isFile) {
            return ByteArray(0)
        }
        return readFilePreview(file, maxBytes)
    }

    fun previewMode(
        bytes: ByteArray,
    ): KboxComparePreviewMode {
        return if (looksLikeText(bytes)) {
            KboxComparePreviewMode.TEXT
        } else {
            KboxComparePreviewMode.BINARY
        }
    }
}
