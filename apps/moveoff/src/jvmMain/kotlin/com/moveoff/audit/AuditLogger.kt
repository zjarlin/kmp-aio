package com.moveoff.audit

import com.moveoff.team.ActivityAction
import com.moveoff.team.ActivityLogEntry
import com.moveoff.team.AuditLogQuery
import com.moveoff.team.AuditLogResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MutableSharedFlow
import kotlinx.coroutines.SharedFlow
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asSharedFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.logging.Level
import java.util.logging.Logger

private val logger: Logger = Logger.getLogger(AuditLogger::class.java.name)

class AuditLogger(
    private val logDir: File = File(System.getProperty("user.home"), ".moveoff/audit"),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val maxLogSize: Long = 10 * 1024 * 1024,
    private val maxLogFiles: Int = 10
) {
    private val logBuffer = mutableListOf<ActivityLogEntry>()
    private val bufferLock = Any()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    private var currentLogFile: File? = null

    private val _logStream = MutableSharedFlow<ActivityLogEntry>(extraBufferCapacity = 100)
    val logStream: SharedFlow<ActivityLogEntry> = _logStream.asSharedFlow()

    init {
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        rotateLogFile()

        scope.launch {
            while (isActive) {
                delay(5000)
                flushBuffer()
            }
        }
    }

    fun log(entry: ActivityLogEntry) {
        synchronized(bufferLock) {
            logBuffer.add(entry)
        }
        _logStream.tryEmit(entry)

        if (logBuffer.size >= 100) {
            scope.launch { flushBuffer() }
        }
    }

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

    private suspend fun flushBuffer() = withContext(Dispatchers.IO) {
        val entriesToFlush = synchronized(bufferLock) {
            if (logBuffer.isEmpty()) {
                emptyList()
            } else {
                logBuffer.toList().also { logBuffer.clear() }
            }
        }
        if (entriesToFlush.isEmpty()) {
            return@withContext
        }

        try {
            val targetFile = currentLogFile ?: return@withContext
            targetFile.appendText(entriesToFlush.joinToString("\n") { entryToString(it) } + "\n")
            if (targetFile.length() > maxLogSize) {
                rotateLogFile()
            }
        } catch (e: Exception) {
            logger.log(Level.WARNING, "写入审计日志失败", e)
        }
    }

    private fun rotateLogFile() {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        currentLogFile = File(logDir, "audit_$timestamp.log")
        cleanupOldLogs()
    }

    private fun cleanupOldLogs() {
        val logFiles = logDir
            .listFiles { file -> file.name.startsWith("audit_") && file.name.endsWith(".log") }
            ?.sortedBy { file -> file.lastModified() }
            ?: return

        if (logFiles.size > maxLogFiles) {
            logFiles.take(logFiles.size - maxLogFiles).forEach { file ->
                runCatching { file.delete() }
            }
        }
    }

    suspend fun query(query: AuditLogQuery): AuditLogResult = withContext(Dispatchers.IO) {
        try {
            flushBuffer()
            val allEntries = mutableListOf<ActivityLogEntry>()

            logDir.listFiles { file -> file.name.startsWith("audit_") && file.name.endsWith(".log") }
                ?.sortedByDescending { file -> file.lastModified() }
                ?.forEach { file ->
                    file.readLines().forEach { line ->
                        val entry = parseEntry(line) ?: return@forEach
                        if (matchesQuery(entry, query)) {
                            allEntries.add(entry)
                        }
                    }
                }

            val sortedEntries = allEntries.sortedByDescending { it.timestamp }
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
            logger.log(Level.WARNING, "查询审计日志失败", e)
            AuditLogResult(emptyList(), 0, query.page, query.pageSize, false)
        }
    }

    private fun matchesQuery(entry: ActivityLogEntry, query: AuditLogQuery): Boolean {
        query.spaceId?.let { if (entry.spaceId != it) return false }
        query.userId?.let { if (entry.userId != it) return false }
        query.actions?.let { if (entry.action !in it) return false }
        query.startTime?.let { if (entry.timestamp < it) return false }
        query.endTime?.let { if (entry.timestamp > it) return false }
        return true
    }

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
            val hourlyDistribution = result.entries.groupingBy { entry ->
                LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(entry.timestamp),
                    ZoneId.systemDefault()
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
            logger.log(Level.WARNING, "统计审计日志失败", e)
            AuditStatistics(0, 0, emptyMap(), emptyList(), emptyMap())
        }
    }

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
            if (parts.size < 9) {
                return null
            }
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
        } catch (_: Exception) {
            null
        }
    }

    private fun escape(value: String): String {
        return value.replace("|", "\\|").replace("\n", "\\n")
    }

    private fun unescape(value: String): String {
        return value.replace("\\n", "\n").replace("\\|", "|")
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        ).format(formatter)
    }

    private fun generateId(): String {
        return "${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

data class AuditStatistics(
    val totalActions: Int,
    val uniqueUsers: Int,
    val actionCounts: Map<ActivityAction, Int>,
    val topActiveUsers: List<Pair<String, Int>>,
    val hourlyDistribution: Map<Int, Int>
)
