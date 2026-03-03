package site.addzero.system.fileupload.feature

import site.addzero.system.common.exception.ResourceNotFoundException
import site.addzero.system.fileupload.dto.FileMetadata
import site.addzero.system.fileupload.dto.FileStorageResult
import site.addzero.system.fileupload.dto.FileUploadRequest
import site.addzero.system.fileupload.spi.FileStorageSpi
import site.addzero.system.fileupload.spi.StorageType
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.time.Instant
import java.util.*

/**
 * 本地文件存储服务默认实现
 * @param basePath 文件存储根目录
 */
open class LocalFileStorageService(
    private val basePath: String
) : FileStorageSpi {

    private val storageDir: Path = Paths.get(basePath).toAbsolutePath().normalize()

    init {
        // 确保存储目录存在
        Files.createDirectories(storageDir)
    }

    override fun store(inputStream: InputStream, request: FileUploadRequest): FileStorageResult {
        val fileId = generateFileId()
        val relativePath = buildStoragePath(request, fileId)
        val fullPath = storageDir.resolve(relativePath)

        // 确保父目录存在
        Files.createDirectories(fullPath.parent)

        // 写入文件并计算哈希
        val digest = MessageDigest.getInstance("SHA-256")
        var size = 0L

        FileOutputStream(fullPath.toFile()).use { fos ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                fos.write(buffer, 0, bytesRead)
                digest.update(buffer, 0, bytesRead)
                size += bytesRead
            }
        }

        val hash = Base64.getEncoder().encodeToString(digest.digest())

        // 保存元数据文件
        saveMetadata(fileId, request, size, hash)

        return FileStorageResult(
            fileId = fileId,
            originalFilename = request.filename,
            storagePath = relativePath.toString(),
            storageType = StorageType.LOCAL,
            size = size,
            contentType = request.contentType ?: detectContentType(request.filename),
            storedAt = Instant.now()
        )
    }

    override fun retrieve(fileId: String): InputStream? {
        val path = findFileById(fileId) ?: return null
        return try {
            FileInputStream(path.toFile())
        } catch (e: FileNotFoundException) {
            null
        }
    }

    override fun delete(fileId: String): Boolean {
        val path = findFileById(fileId) ?: return false
        val metaPath = getMetadataPath(fileId)

        return try {
            Files.deleteIfExists(path)
            Files.deleteIfExists(metaPath)
            true
        } catch (e: IOException) {
            false
        }
    }

    override fun deleteBatch(fileIds: List<String>): Map<String, Boolean> {
        return fileIds.associateWith { delete(it) }
    }

    override fun exists(fileId: String): Boolean {
        return findFileById(fileId) != null
    }

    override fun getMetadata(fileId: String): FileMetadata? {
        val metaPath = getMetadataPath(fileId)
        if (!Files.exists(metaPath)) return null

        return try {
            Properties().apply {
                load(FileInputStream(metaPath.toFile()))
            }.let { props ->
                FileMetadata(
                    fileId = fileId,
                    filename = props.getProperty("filename", ""),
                    size = props.getProperty("size", "0").toLong(),
                    contentType = props.getProperty("contentType"),
                    hash = props.getProperty("hash"),
                    lastModified = Instant.parse(props.getProperty("lastModified", Instant.now().toString())),
                    customMetadata = props.stringPropertyNames()
                        .filter { !it in setOf("filename", "size", "contentType", "hash", "lastModified") }
                        .associateWith { props.getProperty(it) }
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun generateAccessUrl(fileId: String, expireSeconds: Int): String? {
        // 本地存储返回相对路径，由应用层处理实际访问URL
        val path = findFileById(fileId) ?: return null
        return "/files/$fileId"
    }

    override fun getStorageType(): StorageType = StorageType.LOCAL

    // === 私有方法 ===

    private fun generateFileId(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    private fun buildStoragePath(request: FileUploadRequest, fileId: String): Path {
        val dateDir = java.time.LocalDate.now().toString().replace("-", "/")
        val ext = request.filename.substringAfterLast(".", "")
            .takeIf { it.isNotEmpty() } ?: "bin"
        val path = request.path ?: dateDir
        return Paths.get(path, "$fileId.$ext")
    }

    private fun detectContentType(filename: String): String {
        return try {
            Files.probeContentType(Paths.get(filename)) ?: "application/octet-stream"
        } catch (e: Exception) {
            "application/octet-stream"
        }
    }

    private fun saveMetadata(fileId: String, request: FileUploadRequest, size: Long, hash: String) {
        val metaPath = getMetadataPath(fileId)
        Properties().apply {
            setProperty("filename", request.filename)
            setProperty("size", size.toString())
            setProperty("contentType", request.contentType ?: "")
            setProperty("hash", hash)
            setProperty("lastModified", Instant.now().toString())
            request.metadata.forEach { (k, v) -> setProperty(k, v) }
            store(FileOutputStream(metaPath.toFile()), "File Metadata")
        }
    }

    private fun getMetadataPath(fileId: String): Path {
        return storageDir.resolve(".meta/$fileId.properties")
    }

    private fun findFileById(fileId: String): Path? {
        // 简单的查找策略：遍历日期目录
        return try {
            Files.walk(storageDir)
                .filter { it.fileName.toString().startsWith(fileId) }
                .filter { !it.toString().contains(".meta") }
                .findFirst()
                .orElse(null)
        } catch (e: IOException) {
            null
        }
    }
}
