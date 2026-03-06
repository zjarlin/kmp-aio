package com.moveoff.audit

import com.moveoff.team.ActivityAction
import com.moveoff.team.ActivityLogEntry
import com.moveoff.team.AuditLogQuery
import com.moveoff.team.AuditLogResult
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

/**
 * 审计日志管理器
 *
 * 记录和查询用户操作，用于：
 * - 安全审计
 * - 操作追溯
 * - 合规性检查
 */
class AuditLogger(
    private val logDir: File = File(System.getProperty("user.home"), ".moveoff/audit"),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val maxLogSize: Long = 10 * 1024 * 1024, // 10MB
    private val maxLogFiles: Int = 10
) {
    private val logBuffer = mutableListOf<ActivityLogEntry>()
    private val bufferLock = Object()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    // 当前日志文件
    private var currentLogFile: File? = null

    // 实时日志流
    private val _logStream = MutableSharedFlow<ActivityLogEntry>(extraBufferCapacity = 100)
    val logStream: SharedFlow<ActivityLogEntry> = _logStream.asSharedFlow()

    init {
        // 确保日志目录存在
        logDir.mkdirs()
        rotateLogFile()

        // 启动定时刷新任务
        scope.launch {
            while (isActive) {
                delay(5000) // 每5秒刷新一次
                flushBuffer()
            }
        }
    }

    /**
     * 记录操作日志
     */
    fun log(entry: ActivityLogEntry) {
        synchronized(bufferLock) {
            logBuffer.add(entry)
        }
        _logStream.tryEmit(entry)

        // 如果缓冲区太大，立即刷新
        if (logBuffer.size >= 100) {
            scope.launch { flushBuffer() }
        }
    }

    /**
     * 记录操作（简化接口）
     */
    fun log(
        action: ActivityAction,
        userId: String,
        userName: String,
        spaceId: String? = null,
        targetPath: String? = null,
        details: String? = null
    ) {
        log(
            ActivityLogEntry(
                id = generateId(),
                spaceId = spaceId ?: "",
                userId = userId,
                userName = userName,
                action = action,
                targetPath = targetPath,
                details = details
            )
        )
    }

    /**
     * 刷新缓冲区到文件
     */
    private suspend fun flushBuffer() = withContext(Dispatchers.IO) {
        val entriesToFlush = synchronized(bufferLock) {
            if (logBuffer.isEmpty()) return@withContext
            val copy = logBuffer.toList()
            logBuffer.clear()
            copy
        }

        try {
            currentLogFile?.appendText(
                entriesToFlush.joinToString("\n") { entryToString(it) } + "\n"
            )

            // 检查日志文件大小
            if (currentLogFile?.length() ?: 0 > maxLogSize) {
                rotateLogFile()
            }
        } catch (e: Exception) {
            logger.error(e) { "写入审计日志失败" }
        }
    }

    /**
     * 轮换日志文件
     */
    private fun rotateLogFile() {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        currentLogFile = File(logDir, "audit_$timestamp.log")

        // 清理旧日志文件
        cleanupOldLogs()
    }

    /**
     * 清理旧日志文件
     */
    private fun cleanupOldLogs() {
        val logFiles = logDir.listFiles { f -> f.name.startsWith("audit_") && f.name.endsWith(".log") }
            ?.sortedBy { it.lastModified() }
            ?: return

        if (logFiles.size > maxLogFiles) {
            logFiles.take(logFiles.size - maxLogFiles).forEach {
                try {
                    it.delete()
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * 查询审计日志
     */
    suspend fun query(query: AuditLogQuery): AuditLogResult = withContext(Dispatchers.IO) {
        try {
            // 先刷新缓冲区
            flushBuffer()

            val allEntries = mutableListOf<ActivityLogEntry>()

            // 读取所有日志文件
            logDir.listFiles { f -> f.name.startsWith("audit_") && f.name.endsWith(".log") }
                ?.sortedByDescending { it.lastModified() }
                ?.forEach { file ->
                    file.readLines().forEach { line ->
                        parseEntry(line)?.let { entry ->
                            // 应用过滤条件
                            if (matchesQuery(entry, query)) {
                                allEntries.add(entry)
                            }
                        }
                    }
                }

            // 排序（最新的在前）
            val sortedEntries = allEntries.sortedByDescending { it.timestamp }

            // 分页
            val startIndex = (query.page - 1) * query.pageSize
            val endIndex = minOf(startIndex + query.pageSize, sortedEntries.size)
            val pageEntries = if (startIndex < sortedEntries.size) {
                sortedEntries.subList(startIndex, endIndex)
            } else {
                emptyList()
            }

            AuditLogResult(
                entries = pageEntries,
                totalCount = sortedEntries.size,
                page = query.page,
                pageSize = query.pageSize,
                hasMore = endIndex < sortedEntries.size
            )
        } catch (e: Exception) {
            logger.error(e) { "查询审计日志失败" }
            AuditLogResult(emptyList(), 0, query.page, query.pageSize, false)
        }
    }

    /**
     * 检查条目是否匹配查询条件
     */
    private fun matchesQuery(entry: ActivityLogEntry, query: AuditLogQuery): Boolean {
        query.spaceId?.let {
            if (entry.spaceId != it) return false
        }
        query.userId?.let {
            if (entry.userId != it) return false
        }
        query.actions?.let {
            if (entry.action !in it) return false
        }
        query.startTime?.let {
            if (entry.timestamp < it) return false
        }
        query.endTime?.let {
            if (entry.timestamp > it) return false
        }
        return true
    }

    /**
     * 导出日志到CSV
     */
    suspend fun exportToCsv(
        query: AuditLogQuery,
        outputFile: File
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = query(query.copy(page = 1, pageSize = Int.MAX_VALUE))

            outputFile.writeText("Time,User,Action,Path,Details\n")
            result.entries.forEach { entry ->
                outputFile.appendText(
                    "${formatTimestamp(entry.timestamp)}," +
                    "${escapeCsv(entry.userName)}," +
                    "${entry.action}," +
                    "${escapeCsv(entry.targetPath ?: "")}," +
                    "${escapeCsv(entry.details ?: "")}\n"
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取操作统计
     */
    suspend fun getStatistics(
        spaceId: String? = null,
        startTime: Long? = null,
        endTime: Long? = null
    ): AuditStatistics = withContext(Dispatchers.IO) {
        try {
            val query = AuditLogQuery(
                spaceId = spaceId,
                startTime = startTime,
                endTime = endTime,
                page = 1,
                pageSize = Int.MAX_VALUE
            )
            val result = query(query)

            val actionCounts = result.entries.groupingBy { it.action }.eachCount()
            val userActivity = result.entries.groupingBy { it.userId }.eachCount()
            val hourlyDistribution = result.entries.groupingBy {
                LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(it.timestamp),
                    java.time.ZoneId.systemDefault()
                ).hour
            }.eachCount()

            AuditStatistics(
                totalActions = result.entries.size,
                uniqueUsers = userActivity.size,
                actionCounts = actionCounts,
                topActiveUsers = userActivity.entries
                    .sortedByDescending { it.value }
                    .take(10)
                    .map { it.key to it.value },
                hourlyDistribution = hourlyDistribution
            )
        } catch (e: Exception) {
            AuditStatistics(0, 0, emptyMap(), emptyList(), emptyMap())
        }
    }

    /**
     * 关闭日志管理器
     */
    suspend fun close() {
        flushBuffer()
    }

    private fun entryToString(entry: ActivityLogEntry): String {
        return buildString {
            append(entry.timestamp)
            append("|")
            append(entry.id)
            append("|")
            append(entry.spaceId)
            append("|")
            append(entry.userId)
            append("|")
            append(escape(entry.userName))
            append("|")
            append(entry.action)
            append("|")
            append(escape(entry.targetPath ?: ""))
            append("|")
            append(escape(entry.targetUserId ?: ""))
            append("|")
            append(escape(entry.details ?: ""))
            append("|")
            append(entry.ipAddress ?: "")
        }
    }

    private fun parseEntry(line: String): ActivityLogEntry? {
        return try {
            val parts = line.split("|", limit = 10)
            if (parts.size < 9) return null

            ActivityLogEntry(
                timestamp = parts[0].toLong(),
                id = parts[1],
                spaceId = parts[2],
                userId = parts[3],
                userName = unescape(parts[4]),
                action = ActivityAction.valueOf(parts[5]),
                targetPath = unescape(parts[6]).takeIf { it.isNotEmpty() },
                targetUserId = unescape(parts[7]).takeIf { it.isNotEmpty() },
                details = unescape(parts[8]).takeIf { it.isNotEmpty() },
                ipAddress = parts.getOrNull(9)
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun escape(s: String): String {
        return s.replace("|", "\\|").replace("\n", "\\n")
    }

    private fun unescape(s: String): String {
        return s.replace("\\n", "\n").replace("\\|", "|")
    }

    private fun escapeCsv(s: String): String {
        return if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            "\"${s.replace("\"", "\"")}\""
        } else s
    }

    private fun formatTimestamp(timestamp: Long): String {
        return LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            java.time.ZoneId.systemDefault()
        ).format(formatter)
    }

    private fun generateId(): String {
        return "${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

/**
 * 审计统计
 */
data class AuditStatistics(
    val totalActions: Int,
    val uniqueUsers: Int,
    val actionCounts: Map<ActivityAction, Int>,
    val topActiveUsers: List<Pair<String, Int>>,
    val hourlyDistribution: Map<Int, Int>
)