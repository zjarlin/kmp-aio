package site.addzero.system.audit.feature

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import site.addzero.system.audit.dto.AuditLogDTO
import site.addzero.system.audit.dto.AuditLogQuery
import site.addzero.system.audit.dto.AuditOperationRequest
import site.addzero.system.audit.spi.AuditLogSpi
import site.addzero.system.audit.spi.ExportFormat
import site.addzero.system.audit.spi.OperationStatus
import site.addzero.system.common.dto.PageResult
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.GZIPOutputStream

/**
 * 基于内存的审计日志服务默认实现
 * 适用于开发测试，生产环境建议使用持久化存储
 */
open class InMemoryAuditLogService : AuditLogSpi {

    protected val logStore = ConcurrentHashMap<String, AuditLogDTO>()
    protected val idGenerator = java.util.concurrent.atomic.AtomicLong(1)
    protected val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    override fun record(request: AuditOperationRequest): String {
        val id = generateId()
        val dto = AuditLogDTO(
            id = id,
            traceId = request.traceId,
            userId = request.userId,
            username = request.username,
            userIp = request.userIp,
            userAgent = request.userAgent,
            operationType = request.operationType,
            operationModule = request.operationModule,
            operationAction = request.operationAction,
            description = request.description,
            requestParams = request.requestParams?.let { json.encodeToString(it) },
            responseData = request.responseData?.let { json.encodeToString(it) },
            oldValue = request.oldValue?.let { json.encodeToString(it) },
            newValue = request.newValue?.let { json.encodeToString(it) },
            bizType = request.bizType,
            bizId = request.bizId,
            status = request.status,
            errorMsg = request.errorMsg,
            durationMs = request.durationMs,
            serverIp = request.serverIp,
            createdAt = Instant.now()
        )
        logStore[id] = dto
        return id
    }

    override fun getById(id: String): AuditLogDTO? = logStore[id]

    override fun page(query: AuditLogQuery): PageResult<AuditLogDTO> {
        val filtered = logStore.values.filter { log ->
            (query.userId == null || log.userId == query.userId) &&
                    (query.username == null || log.username?.contains(query.username, ignoreCase = true) == true) &&
                    (query.operationType == null || log.operationType == query.operationType) &&
                    (query.operationModule == null || log.operationModule == query.operationModule) &&
                    (query.operationAction == null || log.operationAction == query.operationAction) &&
                    (query.bizType == null || log.bizType == query.bizType) &&
                    (query.bizId == null || log.bizId == query.bizId) &&
                    (query.status == null || log.status == query.status) &&
                    (query.userIp == null || log.userIp == query.userIp) &&
                    (query.startTime == null || log.createdAt >= query.startTime) &&
                    (query.endTime == null || log.createdAt <= query.endTime)
        }.sortedByDescending { it.createdAt }

        val total = filtered.size.toLong()
        val offset = query.offset().toInt()
        val limit = query.limit()
        val list = filtered.drop(offset).take(limit)

        return PageResult(list, total, query.pageNum, query.pageSize)
    }

    override fun getByUserId(userId: String, limit: Int): List<AuditLogDTO> {
        return logStore.values
            .filter { it.userId == userId }
            .sortedByDescending { it.createdAt }
            .take(limit)
    }

    override fun getByBizKey(bizType: String, bizId: String): List<AuditLogDTO> {
        return logStore.values
            .filter { it.bizType == bizType && it.bizId == bizId }
            .sortedByDescending { it.createdAt }
    }

    override fun clean(before: Instant): Int {
        val toDelete = logStore.values.filter { it.createdAt.isBefore(before) }
        toDelete.forEach { logStore.remove(it.id) }
        return toDelete.size
    }

    override fun export(query: AuditLogQuery, format: ExportFormat): ByteArray {
        val logs = page(query.copy(pageNum = 1, pageSize = 10000)).list

        return when (format) {
            ExportFormat.CSV -> exportToCsv(logs)
            ExportFormat.JSON -> exportToJson(logs)
            ExportFormat.EXCEL -> exportToCsv(logs) // 简化处理，CSV可被Excel打开
        }
    }

    private fun generateId(): String {
        return "AL${System.currentTimeMillis()}${idGenerator.getAndIncrement()}"
    }

    private fun exportToCsv(logs: List<AuditLogDTO>): ByteArray {
        val header = "ID,时间,用户,模块,动作,类型,状态,耗时(ms),描述\n"
        val rows = logs.joinToString("\n") { log ->
            "${log.id},${log.createdAt},${log.username ?: ""},${log.operationModule},${log.operationAction},${log.operationType},${log.status},${log.durationMs},${log.description ?: ""}"
        }
        return (header + rows).toByteArray(StandardCharsets.UTF_8)
    }

    private fun exportToJson(logs: List<AuditLogDTO>): ByteArray {
        return json.encodeToString(logs).toByteArray(StandardCharsets.UTF_8)
    }
}
