package com.moveoff.version

import com.moveoff.sync.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.*

/**
 * 文件版本信息
 *
 * @param versionId 版本ID（S3版本控制）
 * @param lastModified 最后修改时间
 * @param size 文件大小
 * @param etag 实体标签
 * @param isLatest 是否是最新版本
 * @param storageClass 存储类别（STANDARD/GLACIER等）
 */
data class FileVersion(
    val versionId: String,
    val lastModified: Long,
    val size: Long,
    val etag: String,
    val isLatest: Boolean = false,
    val storageClass: String = "STANDARD"
) {
    /**
     * 格式化文件大小
     */
    fun formatSize(): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }

    /**
     * 格式化修改时间
     */
    fun formatTime(): String {
        val instant = Instant.fromEpochMilliseconds(lastModified)
        return instant.toString()
    }
}

/**
 * 版本历史管理器
 *
 * 管理文件版本历史，支持：
 * - 获取文件版本列表
 * - 恢复到指定版本
 * - 删除指定版本
 * - 清理过期版本
 */
class VersionHistoryManager(
    private val storageClient: StorageClient
) {

    /**
     * 获取文件的版本历史
     *
     * @param remotePath 远程文件路径
     * @return 版本列表（按时间倒序）
     */
    suspend fun getVersions(remotePath: String): Result<List<FileVersion>> = withContext(Dispatchers.IO) {
        try {
            // 这里需要存储客户端支持版本控制
            // S3通过ListObjectVersions API获取
            val versions = when (storageClient) {
                is S3VersionedStorageClient -> {
                    storageClient.listVersions(remotePath)
                }
                else -> {
                    // 非版本控制存储，返回空列表
                    emptyList()
                }
            }

            Result.success(versions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取文件的最新版本
     */
    suspend fun getLatestVersion(remotePath: String): Result<FileVersion?> = withContext(Dispatchers.IO) {
        getVersions(remotePath).map { versions ->
            versions.find { it.isLatest }
        }
    }

    /**
     * 恢复文件到指定版本
     *
     * @param remotePath 远程文件路径
     * @param versionId 目标版本ID
     * @param localPath 本地保存路径（null表示覆盖当前）
     * @return 恢复结果
     */
    suspend fun restoreVersion(
        remotePath: String,
        versionId: String,
        localPath: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            when (storageClient) {
                is S3VersionedStorageClient -> {
                    // 下载指定版本
                    val tempPath = localPath ?: "$remotePath.restoring"
                    val downloadResult = storageClient.downloadVersion(
                        remotePath = remotePath,
                        versionId = versionId,
                        localPath = tempPath
                    ) { transferred, total ->
                        // 进度回调
                    }

                    if (!downloadResult.success) {
                        return@withContext Result.failure(
                            IllegalStateException("下载版本失败: ${downloadResult.error}")
                        )
                    }

                    // 如果未指定本地路径，覆盖当前文件
                    if (localPath == null) {
                        java.io.File(tempPath).renameTo(java.io.File(remotePath))
                    }

                    Result.success(Unit)
                }
                else -> {
                    Result.failure(UnsupportedOperationException("当前存储不支持版本控制"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除指定版本
     *
     * @param remotePath 远程文件路径
     * @param versionId 要删除的版本ID
     * @return 删除结果
     */
    suspend fun deleteVersion(
        remotePath: String,
        versionId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            when (storageClient) {
                is S3VersionedStorageClient -> {
                    storageClient.deleteVersion(remotePath, versionId)
                    Result.success(Unit)
                }
                else -> {
                    Result.failure(UnsupportedOperationException("当前存储不支持版本控制"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 清理过期版本
     *
     * @param remotePath 远程文件路径
     * @param keepCount 保留版本数量（默认10个）
     * @param keepDays 保留天数（默认30天）
     * @return 清理结果
     */
    suspend fun cleanupVersions(
        remotePath: String,
        keepCount: Int = 10,
        keepDays: Int = 30
    ): Result<CleanupResult> = withContext(Dispatchers.IO) {
        try {
            val versions = getVersions(remotePath).getOrElse {
                return@withContext Result.failure(it)
            }

            val now = System.currentTimeMillis()
            val keepMillis = keepDays * 24 * 60 * 60 * 1000L

            // 按时间倒序排序
            val sortedVersions = versions.sortedByDescending { it.lastModified }

            val toDelete = sortedVersions.filterIndexed { index, version ->
                // 保留最新的N个版本
                if (index < keepCount) return@filterIndexed false

                // 保留指定天数内的版本
                val age = now - version.lastModified
                age > keepMillis
            }

            var deletedCount = 0
            var failedCount = 0

            toDelete.forEach { version ->
                deleteVersion(remotePath, version.versionId).fold(
                    onSuccess = { deletedCount++ },
                    onFailure = { failedCount++ }
                )
            }

            Result.success(
                CleanupResult(
                    totalVersions = versions.size,
                    deletedCount = deletedCount,
                    failedCount = failedCount,
                    remainingCount = versions.size - deletedCount
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 比较两个版本的差异
     *
     * @param remotePath 远程文件路径
     * @param versionId1 版本1 ID
     * @param versionId2 版本2 ID（null表示与当前版本比较）
     * @return 差异信息
     */
    suspend fun compareVersions(
        remotePath: String,
        versionId1: String,
        versionId2: String? = null
    ): Result<VersionComparison> = withContext(Dispatchers.IO) {
        try {
            val versions = getVersions(remotePath).getOrElse {
                return@withContext Result.failure(it)
            }

            val v1 = versions.find { it.versionId == versionId1 }
                ?: return@withContext Result.failure(IllegalArgumentException("版本不存在: $versionId1"))

            val v2 = if (versionId2 != null) {
                versions.find { it.versionId == versionId2 }
                    ?: return@withContext Result.failure(IllegalArgumentException("版本不存在: $versionId2"))
            } else {
                // 使用最新版本
                versions.find { it.isLatest }
                    ?: return@withContext Result.failure(IllegalStateException("无法获取最新版本"))
            }

            Result.success(
                VersionComparison(
                    version1 = v1,
                    version2 = v2,
                    sizeDiff = v2.size - v1.size,
                    timeDiff = v2.lastModified - v1.lastModified
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 支持版本控制的存储客户端接口
 */
interface S3VersionedStorageClient : StorageClient {
    /**
     * 获取文件版本列表
     */
    suspend fun listVersions(remotePath: String): List<FileVersion>

    /**
     * 下载指定版本
     */
    suspend fun downloadVersion(
        remotePath: String,
        versionId: String,
        localPath: String,
        progress: (Long, Long) -> Unit
    ): DownloadResult

    /**
     * 删除指定版本
     */
    suspend fun deleteVersion(remotePath: String, versionId: String): Boolean
}

/**
 * 清理结果
 */
data class CleanupResult(
    val totalVersions: Int,
    val deletedCount: Int,
    val failedCount: Int,
    val remainingCount: Int
)

/**
 * 版本比较结果
 */
data class VersionComparison(
    val version1: FileVersion,
    val version2: FileVersion,
    val sizeDiff: Long,
    val timeDiff: Long
) {
    /**
     * 格式化大小差异
     */
    fun formatSizeDiff(): String {
        val sign = if (sizeDiff >= 0) "+" else ""
        val absSize = kotlin.math.abs(sizeDiff)
        val sizeStr = when {
            absSize < 1024 -> "$absSize B"
            absSize < 1024 * 1024 -> "${absSize / 1024} KB"
            absSize < 1024 * 1024 * 1024 -> "${absSize / (1024 * 1024)} MB"
            else -> "${absSize / (1024 * 1024 * 1024)} GB"
        }
        return "$sign$sizeStr"
    }

    /**
     * 格式化时间差异
     */
    fun formatTimeDiff(): String {
        val days = timeDiff / (24 * 60 * 60 * 1000)
        val hours = (timeDiff % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)

        return when {
            days > 0 -> "$days 天 ${hours} 小时"
            hours > 0 -> "$hours 小时"
            else -> "小于1小时"
        }
    }
}
