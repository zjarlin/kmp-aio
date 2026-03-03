package site.addzero.system.fileupload.feature

import site.addzero.system.common.dto.PageResult
import site.addzero.system.common.exception.DuplicateResourceException
import site.addzero.system.common.exception.ResourceNotFoundException
import site.addzero.system.fileupload.dto.*
import site.addzero.system.fileupload.spi.FileRecordSpi
import site.addzero.system.fileupload.spi.StorageType
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * 基于内存的文件记录服务默认实现
 */
open class InMemoryFileRecordService : FileRecordSpi {

    protected val recordStore = ConcurrentHashMap<String, FileRecordDTO>()
    protected val bizIndex = ConcurrentHashMap<Pair<String, String>, MutableSet<String>>()
    protected val idGenerator = java.util.concurrent.atomic.AtomicLong(1)

    override fun create(record: FileRecordCreateRequest): FileRecordDTO {
        if (recordStore.values.any { it.fileId == record.fileId }) {
            throw DuplicateResourceException("FileRecord", "fileId")
        }

        val dto = FileRecordDTO(
            id = idGenerator.getAndIncrement().toString(),
            fileId = record.fileId,
            originalFilename = record.originalFilename,
            storagePath = record.storagePath,
            storageType = record.storageType,
            bucket = record.bucket,
            size = record.size,
            contentType = record.contentType,
            hash = record.hash,
            uploaderId = record.uploaderId,
            bizType = record.bizType,
            bizId = record.bizId,
            status = FileStatus.ACTIVE,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        recordStore[dto.id] = dto

        // 建立业务索引
        if (record.bizType != null && record.bizId != null) {
            val key = record.bizType to record.bizId
            bizIndex.getOrPut(key) { mutableSetOf() }.add(dto.id)
        }

        return dto
    }

    override fun getById(id: String): FileRecordDTO? = recordStore[id]

    override fun getByBizKey(bizType: String, bizId: String): List<FileRecordDTO> {
        val ids = bizIndex[bizType to bizId] ?: return emptyList()
        return ids.mapNotNull { recordStore[it] }
            .filter { it.status != FileStatus.DELETED }
    }

    override fun page(query: FileRecordQuery): PageResult<FileRecordDTO> {
        val filtered = recordStore.values.filter { record ->
            (query.keyword == null ||
                    record.originalFilename.contains(query.keyword, ignoreCase = true)) &&
                    (query.bucket == null || record.bucket == query.bucket) &&
                    (query.bizType == null || record.bizType == query.bizType) &&
                    (query.bizId == null || record.bizId == query.bizId) &&
                    (query.uploaderId == null || record.uploaderId == query.uploaderId) &&
                    (query.status == null || record.status == query.status) &&
                    (query.startTime == null || record.createdAt >= query.startTime) &&
                    (query.endTime == null || record.createdAt <= query.endTime) &&
                    record.status != FileStatus.DELETED
        }.sortedByDescending { it.createdAt }

        val total = filtered.size.toLong()
        val offset = query.offset().toInt()
        val limit = query.limit()
        val list = filtered.drop(offset).take(limit)

        return PageResult(list, total, query.pageNum, query.pageSize)
    }

    override fun update(id: String, request: FileRecordUpdateRequest): FileRecordDTO {
        val existing = recordStore[id]
            ?: throw ResourceNotFoundException("FileRecord", id)

        // 如果业务关联变更，更新索引
        if (request.bizType != null && request.bizId != null &&
            (request.bizType != existing.bizType || request.bizId != existing.bizId)) {
            // 移除旧索引
            if (existing.bizType != null && existing.bizId != null) {
                bizIndex[existing.bizType to existing.bizId]?.remove(id)
            }
            // 添加新索引
            bizIndex.getOrPut(request.bizType to request.bizId) { mutableSetOf() }.add(id)
        }

        val updated = existing.copy(
            originalFilename = request.originalFilename ?: existing.originalFilename,
            bizType = request.bizType ?: existing.bizType,
            bizId = request.bizId ?: existing.bizId,
            status = request.status ?: existing.status,
            updatedAt = Instant.now()
        )

        recordStore[id] = updated
        return updated
    }

    override fun bindBiz(fileId: String, bizType: String, bizId: String) {
        val record = recordStore.values.find { it.fileId == fileId }
            ?: throw ResourceNotFoundException("FileRecord", fileId)

        val key = bizType to bizId
        bizIndex.getOrPut(key) { mutableSetOf() }.add(record.id)

        recordStore[record.id] = record.copy(
            bizType = bizType,
            bizId = bizId,
            updatedAt = Instant.now()
        )
    }

    override fun unbindBiz(fileId: String, bizType: String, bizId: String) {
        val record = recordStore.values.find { it.fileId == fileId } ?: return
        bizIndex[bizType to bizId]?.remove(record.id)

        recordStore[record.id] = record.copy(
            bizType = null,
            bizId = null,
            updatedAt = Instant.now()
        )
    }

    override fun delete(id: String) {
        val record = recordStore[id] ?: throw ResourceNotFoundException("FileRecord", id)
        recordStore[id] = record.copy(status = FileStatus.DELETED, updatedAt = Instant.now())
    }

    override fun deleteBatch(ids: List<String>) {
        ids.forEach { delete(it) }
    }

    override fun getStorageStats(bucket: String): StorageStats {
        val files = recordStore.values.filter { it.bucket == bucket && it.status == FileStatus.ACTIVE }
        val totalSize = files.sumOf { it.size }

        return StorageStats(
            bucket = bucket,
            totalFiles = files.size.toLong(),
            totalSize = totalSize,
            avgFileSize = if (files.isNotEmpty()) totalSize / files.size else 0
        )
    }
}
