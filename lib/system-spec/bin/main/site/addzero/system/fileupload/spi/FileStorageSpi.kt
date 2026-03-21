package site.addzero.system.fileupload.spi

import site.addzero.system.common.dto.PageQuery
import site.addzero.system.common.dto.PageResult
import site.addzero.system.fileupload.dto.*
import java.io.InputStream

/**
 * 文件存储服务SPI
 * 提供文件的存储、读取、删除等基础操作
 */
interface FileStorageSpi {

    /**
     * 存储文件
     * @param inputStream 文件输入流
     * @param request 上传请求
     * @return 文件存储结果
     */
    fun store(inputStream: InputStream, request: FileUploadRequest): FileStorageResult

    /**
     * 根据文件ID获取文件内容
     * @param fileId 文件唯一标识
     * @return 文件输入流，不存在返回null
     */
    fun retrieve(fileId: String): InputStream?

    /**
     * 删除文件
     * @param fileId 文件唯一标识
     * @return 是否删除成功
     */
    fun delete(fileId: String): Boolean

    /**
     * 批量删除文件
     * @param fileIds 文件ID列表
     * @return 删除结果映射
     */
    fun deleteBatch(fileIds: List<String>): Map<String, Boolean>

    /**
     * 检查文件是否存在
     * @param fileId 文件唯一标识
     */
    fun exists(fileId: String): Boolean

    /**
     * 获取文件元数据
     * @param fileId 文件唯一标识
     */
    fun getMetadata(fileId: String): FileMetadata?

    /**
     * 生成文件访问URL（如预签名URL）
     * @param fileId 文件唯一标识
     * @param expireSeconds URL过期时间（秒）
     */
    fun generateAccessUrl(fileId: String, expireSeconds: Int = 3600): String?

    /**
     * 获取存储类型标识
     */
    fun getStorageType(): StorageType
}

enum class StorageType {
    LOCAL,      // 本地存储
    OSS,        // 阿里云OSS
    S3,         // AWS S3
    MINIO,      // MinIO
    COS,        // 腾讯云COS
    OBS         // 华为云OBS
}
