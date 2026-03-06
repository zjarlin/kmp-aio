package com.moveoff.dedup

import com.moveoff.db.Database
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.security.MessageDigest

/**
 * 重复文件信息
 *
 * @param hash 文件内容哈希
 * @param size 文件大小
 * @param files 具有相同内容的文件路径列表
 */
data class DuplicateGroup(
    val hash: String,
    val size: Long,
    val files: List<DuplicateFile>
) {
    /**
     * 计算可节省的空间
     */
    fun potentialSavings(): Long {
        return if (files.size > 1) size * (files.size - 1) else 0
    }
}

/**
 * 重复文件条目
 */
data class DuplicateFile(
    val path: String,
    val lastModified: Long,
    val isOriginal: Boolean = false
)

/**
 * 去重扫描结果
 */
data class DedupScanResult(
    val totalFiles: Int,
    val scannedFiles: Int,
    val duplicateGroups: List<DuplicateGroup>,
    val totalDuplicates: Int,
    val potentialSavings: Long,
    val scanTime: Long
)

/**
 * 去重进度
 */
data class DedupProgress(
    val currentFile: String,
    val processedCount: Int,
    val totalCount: Int,
    val currentHash: String?,
    val stage: DedupStage
) {
    val progress: Float
        get() = if (totalCount > 0) processedCount.toFloat() / totalCount else 0f
}

enum class DedupStage {
    SCANNING,      // 扫描文件
    HASHING,       // 计算哈希
    ANALYZING,     // 分析重复
    COMPLETED      // 完成
}

/**
 * 去重操作结果
 */
sealed class DedupActionResult {
    data class Success(val deletedCount: Int, val savedSpace: Long) : DedupActionResult()
    data class Error(val message: String) : DedupActionResult()
}

/**
 * 文件去重管理器
 *
 * 基于内容哈希检测和删除重复文件
 */
class DeduplicationManager(
    private val database: Database,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    private val _progress = MutableStateFlow<DedupProgress?>(null)
    val progress: StateFlow<DedupProgress?> = _progress.asStateFlow()

    private var scanJob: Job? = null

    /**
     * 扫描重复文件
     *
     * @param rootDir 扫描根目录
     * @param minFileSize 最小文件大小（字节，默认1KB）
     * @param excludePatterns 排除模式列表
     * @return 扫描结果
     */
    suspend fun scanDuplicates(
        rootDir: String,
        minFileSize: Long = 1024,
        excludePatterns: List<String> = emptyList()
    ): Result<DedupScanResult> = withContext(Dispatchers.IO) {
        try {
            val root = File(rootDir)
            if (!root.exists() || !root.isDirectory) {
                return@withContext Result.failure(IllegalArgumentException("无效的目录: $rootDir"))
            }

            val startTime = System.currentTimeMillis()
            _progress.value = DedupProgress("", 0, 0, null, DedupStage.SCANNING)

            // 1. 收集所有文件
            val allFiles = mutableListOf<File>()
            root.walkTopDown()
                .filter { it.isFile }
                .filter { it.length() >= minFileSize }
                .filter { file ->
                    excludePatterns.none { pattern ->
                        file.path.matches(Regex(pattern.replace("*", ".*")))
                    }
                }
                .toCollection(allFiles)

            val totalFiles = allFiles.size
            var processedCount = 0

            // 2. 按大小分组（快速预筛选）
            val sizeGroups = allFiles.groupBy { it.length() }
                .filter { it.value.size > 1 }

            // 3. 计算完整哈希
            _progress.value = DedupProgress("", 0, totalFiles, null, DedupStage.HASHING)

            val hashMap = mutableMapOf<String, MutableList<File>>()

            sizeGroups.values.flatten().forEach { file ->
                processedCount++
                _progress.value = DedupProgress(
                    currentFile = file.name,
                    processedCount = processedCount,
                    totalCount = totalFiles,
                    currentHash = null,
                    stage = DedupStage.HASHING
                )

                try {
                    val hash = computeFileHash(file)
                    hashMap.getOrPut(hash) { mutableListOf() }.add(file)
                } catch (e: Exception) {
                    // 忽略无法读取的文件
                }

                // 每处理100个文件yield一次，避免阻塞
                if (processedCount % 100 == 0) {
                    yield()
                }
            }

            // 4. 分析结果
            _progress.value = DedupProgress("", processedCount, totalFiles, null, DedupStage.ANALYZING)

            val duplicateGroups = hashMap
                .filter { it.value.size > 1 }
                .map { (hash, files) ->
                    // 选择最老的文件作为原始文件
                    val sortedFiles = files.sortedBy { it.lastModified() }
                    DuplicateGroup(
                        hash = hash,
                        size = sortedFiles.first().length(),
                        files = sortedFiles.mapIndexed { index, file ->
                            DuplicateFile(
                                path = file.path,
                                lastModified = file.lastModified(),
                                isOriginal = index == 0
                            )
                        }
                    )
                }
                .sortedByDescending { it.potentialSavings() }

            val scanTime = System.currentTimeMillis() - startTime

            _progress.value = DedupProgress("", processedCount, totalFiles, null, DedupStage.COMPLETED)

            Result.success(
                DedupScanResult(
                    totalFiles = totalFiles,
                    scannedFiles = processedCount,
                    duplicateGroups = duplicateGroups,
                    totalDuplicates = duplicateGroups.sumOf { it.files.size - 1 },
                    potentialSavings = duplicateGroups.sumOf { it.potentialSavings() },
                    scanTime = scanTime
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除重复文件（保留每个组的第一个文件作为原始文件）
     *
     * @param groups 要处理的重复组
     * @param dryRun 是否仅模拟运行（不实际删除）
     * @return 操作结果
     */
    suspend fun removeDuplicates(
        groups: List<DuplicateGroup>,
        dryRun: Boolean = false
    ): DedupActionResult = withContext(Dispatchers.IO) {
        try {
            var deletedCount = 0
            var savedSpace = 0L

            groups.forEach { group ->
                val duplicates = group.files.filter { !it.isOriginal }

                duplicates.forEach { duplicate ->
                    val file = File(duplicate.path)
                    if (file.exists()) {
                        if (!dryRun) {
                            // 安全删除：先移到回收站/临时目录
                            val deleted = safeDelete(file)
                            if (deleted) {
                                deletedCount++
                                savedSpace += group.size
                            }
                        } else {
                            // 模拟运行，只计数
                            deletedCount++
                            savedSpace += group.size
                        }
                    }
                }
            }

            DedupActionResult.Success(deletedCount, savedSpace)
        } catch (e: Exception) {
            DedupActionResult.Error(e.message ?: "未知错误")
        }
    }

    /**
     * 智能删除重复文件
     *
     * 策略：
     * 1. 优先保留在同步目录内的文件
     * 2. 优先保留文件名更短的（通常是原始文件）
     * 3. 优先保留修改时间更早的
     *
     * @param groups 重复组
     * @param syncRoot 同步根目录（优先保留此目录内的文件）
     * @param dryRun 是否仅模拟
     */
    suspend fun smartRemoveDuplicates(
        groups: List<DuplicateGroup>,
        syncRoot: String,
        dryRun: Boolean = false
    ): DedupActionResult = withContext(Dispatchers.IO) {
        try {
            var deletedCount = 0
            var savedSpace = 0L

            groups.forEach { group ->
                // 智能选择保留的文件
                val filesWithScores = group.files.map { dupFile ->
                    val file = File(dupFile.path)
                    var score = 0

                    // 在同步目录内 +10分
                    if (dupFile.path.startsWith(syncRoot)) score += 10

                    // 文件名短 +5分（通常是原始文件）
                    score -= file.name.length / 5

                    // 修改时间早 +3分
                    score -= (System.currentTimeMillis() - dupFile.lastModified).toInt() / (24 * 60 * 60 * 1000) / 10

                    dupFile to score
                }

                // 按分数排序，保留分数最高的
                val bestFile = filesWithScores.maxByOrNull { it.second }?.first
                    ?: group.files.first()

                val toDelete = group.files.filter { it.path != bestFile.path }

                toDelete.forEach { duplicate ->
                    val file = File(duplicate.path)
                    if (file.exists()) {
                        if (!dryRun) {
                            val deleted = safeDelete(file)
                            if (deleted) {
                                deletedCount++
                                savedSpace += group.size
                            }
                        } else {
                            deletedCount++
                            savedSpace += group.size
                        }
                    }
                }
            }

            DedupActionResult.Success(deletedCount, savedSpace)
        } catch (e: Exception) {
            DedupActionResult.Error(e.message ?: "未知错误")
        }
    }

    /**
     * 用硬链接替换重复文件（节省空间但保留文件结构）
     *
     * @param groups 重复组
     * @return 操作结果
     */
    suspend fun hardLinkDuplicates(
        groups: List<DuplicateGroup>
    ): DedupActionResult = withContext(Dispatchers.IO) {
        try {
            var linkedCount = 0
            var savedSpace = 0L

            groups.forEach { group ->
                val original = group.files.find { it.isOriginal }
                    ?: group.files.first()
                val duplicates = group.files.filter { it.path != original.path }

                val originalFile = File(original.path)
                if (!originalFile.exists()) return@forEach

                duplicates.forEach { duplicate ->
                    val dupFile = File(duplicate.path)
                    if (dupFile.exists()) {
                        // 删除重复文件并创建硬链接
                        val backupPath = duplicate.path + ".backup"
                        if (dupFile.renameTo(File(backupPath))) {
                            try {
                                java.nio.file.Files.createLink(
                                    java.nio.file.Paths.get(duplicate.path),
                                    java.nio.file.Paths.get(original.path)
                                )
                                File(backupPath).delete()
                                linkedCount++
                                savedSpace += group.size
                            } catch (e: Exception) {
                                // 恢复备份
                                File(backupPath).renameTo(dupFile)
                            }
                        }
                    }
                }
            }

            DedupActionResult.Success(linkedCount, savedSpace)
        } catch (e: Exception) {
            DedupActionResult.Error(e.message ?: "未知错误")
        }
    }

    /**
     * 计算文件哈希（SHA-256）
     */
    private fun computeFileHash(file: File): String {
        return MessageDigest.getInstance("SHA-256").use { digest ->
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var read: Int
                while (input.read(buffer).also { read = it } > 0) {
                    digest.update(buffer, 0, read)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        }
    }

    /**
     * 安全删除文件（移到回收站或临时目录）
     */
    private fun safeDelete(file: File): Boolean {
        return try {
            // 尝试使用系统回收站（macOS/Linux）
            val homeDir = System.getProperty("user.home")
            val trashDir = File(homeDir, ".local/share/Trash/files")

            if (trashDir.exists() || trashDir.mkdirs()) {
                val destFile = File(trashDir, file.name + "." + System.currentTimeMillis())
                file.renameTo(destFile)
            } else {
                file.delete()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 取消当前扫描
     */
    fun cancelScan() {
        scanJob?.cancel()
        _progress.value = null
    }
}

/**
 * 格式化文件大小
 */
fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
    }
}
