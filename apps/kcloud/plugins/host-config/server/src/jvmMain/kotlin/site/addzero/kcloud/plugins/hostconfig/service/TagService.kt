package site.addzero.kcloud.plugins.hostconfig.service

import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.ne
import org.babyfish.jimmer.sql.kt.exists
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import site.addzero.kcloud.plugins.hostconfig.api.common.BusinessValidationException
import site.addzero.kcloud.plugins.hostconfig.api.common.ConflictException
import site.addzero.kcloud.plugins.hostconfig.api.common.PageResponse
import site.addzero.kcloud.plugins.hostconfig.api.tag.ReplaceTagValueTextsRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagPositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagValueTextResponse
import site.addzero.kcloud.plugins.hostconfig.model.entity.*
import site.addzero.kcloud.plugins.hostconfig.repository.DeviceRepository
import site.addzero.kcloud.plugins.hostconfig.repository.TagRepository
import site.addzero.kcloud.plugins.hostconfig.repository.TagValueTextRepository

@Service
class TagService(
    private val sql: KSqlClient,
    private val jdbcTemplate: JdbcTemplate,
    private val deviceRepository: DeviceRepository,
    private val tagRepository: TagRepository,
    private val tagValueTextRepository: TagValueTextRepository,
) {

    fun getTag(tagId: Long): TagResponse = loadTag(tagId).toResponse()

    fun listTags(deviceId: Long, offset: Int, size: Int): PageResponse<TagResponse> {
        ensureDeviceExists(deviceId)
        val safeSize = size.coerceAtLeast(1)
        val safeOffset = offset.coerceAtLeast(0)
        val query = sql.createQuery(Tag::class) {
            where(table.device.id eq deviceId)
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.tagDetail))
        }
        val total = query.fetchUnlimitedCount()
        val rows = query.limit(safeSize, safeOffset.toLong()).execute()
        val pages = if (total == 0L) {
            0
        } else {
            ((total + safeSize - 1) / safeSize).toInt()
        }
        return PageResponse(
            d = rows.map { it.toResponse() },
            t = total,
            p = pages,
        )
    }

    @Transactional
    fun createTag(deviceId: Long, request: TagCreateRequest): TagResponse {
        ensureDeviceExists(deviceId)
        ensureDataTypeExists(request.dataTypeId)
        ensureRegisterTypeExists(request.registerTypeId)
        request.forwardRegisterTypeId?.let(::ensureRegisterTypeExists)
        validateTagRequest(request.scalingEnabled, request.rawMin != null, request.rawMax != null, request.engMin != null, request.engMax != null, request.forwardEnabled, request.forwardRegisterTypeId, request.forwardRegisterAddress)
        val name = request.name.trim()
        ensureTagNameUnique(deviceId, name, null)
        ensureTagAddressUnique(deviceId, request.registerTypeId, request.registerAddress, null)
        val now = now()
        val entity = new(Tag::class).by {
            this.deviceId = deviceId
            this.dataTypeId = request.dataTypeId
            this.registerTypeId = request.registerTypeId
            this.forwardRegisterTypeId = request.forwardRegisterTypeId
            this.name = name
            this.description = request.description.cleanNullable()
            this.registerAddress = request.registerAddress
            this.enabled = request.enabled
            this.defaultValue = request.defaultValue.cleanNullable()
            this.exceptionValue = request.exceptionValue.cleanNullable()
            this.pointType = request.pointType
            this.debounceMs = request.debounceMs
            this.sortIndex = request.sortIndex
            this.scalingEnabled = request.scalingEnabled
            this.scalingOffset = request.scalingOffset
            this.rawMin = request.rawMin
            this.rawMax = request.rawMax
            this.engMin = request.engMin
            this.engMax = request.engMax
            this.forwardEnabled = request.forwardEnabled
            this.forwardRegisterAddress = request.forwardRegisterAddress
            this.createdAt = now
            this.updatedAt = now
        }
        val tag = tagRepository.saveCommand(entity) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        return loadTag(tag.id).toResponse()
    }

    @Transactional
    fun updateTag(tagId: Long, request: TagUpdateRequest): TagResponse {
        val current = loadTag(tagId)
        ensureDataTypeExists(request.dataTypeId)
        ensureRegisterTypeExists(request.registerTypeId)
        request.forwardRegisterTypeId?.let(::ensureRegisterTypeExists)
        validateTagRequest(request.scalingEnabled, request.rawMin != null, request.rawMax != null, request.engMin != null, request.engMax != null, request.forwardEnabled, request.forwardRegisterTypeId, request.forwardRegisterAddress)
        val name = request.name.trim()
        ensureTagNameUnique(current.device.id, name, tagId)
        ensureTagAddressUnique(current.device.id, request.registerTypeId, request.registerAddress, tagId)
        val entity = new(Tag::class).by {
            id = tagId
            deviceId = current.device.id
            dataTypeId = request.dataTypeId
            registerTypeId = request.registerTypeId
            forwardRegisterTypeId = request.forwardRegisterTypeId
            this.name = name
            this.description = request.description.cleanNullable()
            this.registerAddress = request.registerAddress
            this.enabled = request.enabled
            this.defaultValue = request.defaultValue.cleanNullable()
            this.exceptionValue = request.exceptionValue.cleanNullable()
            this.pointType = request.pointType
            this.debounceMs = request.debounceMs
            this.sortIndex = request.sortIndex
            this.scalingEnabled = request.scalingEnabled
            this.scalingOffset = request.scalingOffset
            this.rawMin = request.rawMin
            this.rawMax = request.rawMax
            this.engMin = request.engMin
            this.engMax = request.engMax
            this.forwardEnabled = request.forwardEnabled
            this.forwardRegisterAddress = request.forwardRegisterAddress
            this.updatedAt = now()
        }
        val tag = tagRepository.saveCommand(entity) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute().modifiedEntity
        return loadTag(tag.id).toResponse()
    }

    @Transactional
    fun replaceValueTexts(tagId: Long, request: ReplaceTagValueTextsRequest): List<TagValueTextResponse> {
        loadTag(tagId)
        sql.createDelete(TagValueText::class) {
            where(table.tag.id eq tagId)
        }.execute()
        if (request.items.isNotEmpty()) {
            val now = now()
            val entities = request.items.map { item ->
                new(TagValueText::class).by {
                    this.tagId = tagId
                    this.rawValue = item.rawValue.trim()
                    this.displayText = item.displayText.trim()
                    this.sortIndex = item.sortIndex
                    this.createdAt = now
                    this.updatedAt = now
                }
            }
            tagValueTextRepository.saveEntitiesCommand(entities) {
                setMode(SaveMode.INSERT_ONLY)
            }.execute()
        }
        return loadTag(tagId).toResponse().valueTexts
    }

    @Transactional
    fun updateTagPosition(tagId: Long, request: TagPositionUpdateRequest): TagResponse {
        val current = loadTag(tagId)
        ensureDeviceExists(request.deviceId)
        ensureTagNameUnique(request.deviceId, current.name, tagId)
        ensureTagAddressUnique(request.deviceId, current.registerType.id, current.registerAddress, tagId)
        moveTag(tagId, current.device.id, request.deviceId, request.sortIndex)
        return loadTag(tagId).toResponse()
    }

    @Transactional
    fun deleteTag(tagId: Long) {
        if (!tagRepository.existsById(tagId)) {
            throw site.addzero.kcloud.plugins.hostconfig.api.common.NotFoundException("Tag not found")
        }
        jdbcTemplate.update("DELETE FROM tag WHERE id = ?", tagId)
    }

    private fun validateTagRequest(
        scalingEnabled: Boolean,
        hasRawMin: Boolean,
        hasRawMax: Boolean,
        hasEngMin: Boolean,
        hasEngMax: Boolean,
        forwardEnabled: Boolean,
        forwardRegisterTypeId: Long?,
        forwardRegisterAddress: Int?,
    ) {
        if (scalingEnabled && (!hasRawMin || !hasRawMax || !hasEngMin || !hasEngMax)) {
            throw BusinessValidationException("Scaling fields are incomplete")
        }
        if (forwardEnabled && (forwardRegisterTypeId == null || forwardRegisterAddress == null)) {
            throw BusinessValidationException("Forward register settings are incomplete")
        }
    }

    private fun ensureTagNameUnique(deviceId: Long, name: String, excludeId: Long?) {
        val exists = sql.exists(Tag::class) {
            where(table.device.id eq deviceId)
            where(table.name eq name)
            if (excludeId != null) {
                where(table.id ne excludeId)
            }
        }
        if (exists) {
            throw ConflictException("Tag name already exists")
        }
    }

    private fun ensureTagAddressUnique(deviceId: Long, registerTypeId: Long, registerAddress: Int, excludeId: Long?) {
        val exists = sql.exists(Tag::class) {
            where(table.device.id eq deviceId)
            where(table.registerType.id eq registerTypeId)
            where(table.registerAddress eq registerAddress)
            if (excludeId != null) {
                where(table.id ne excludeId)
            }
        }
        if (exists) {
            throw ConflictException("Tag register address already exists")
        }
    }

    private fun ensureDeviceExists(deviceId: Long) {
        if (!deviceRepository.existsById(deviceId)) {
            throw site.addzero.kcloud.plugins.hostconfig.api.common.NotFoundException("Device not found")
        }
    }

    private fun ensureDataTypeExists(dataTypeId: Long) {
        val exists = sql.exists(DataType::class) {
            where(table.id eq dataTypeId)
        }
        if (!exists) {
            throw site.addzero.kcloud.plugins.hostconfig.api.common.NotFoundException("Data type not found")
        }
    }

    private fun ensureRegisterTypeExists(registerTypeId: Long) {
        val exists = sql.exists(RegisterType::class) {
            where(table.id eq registerTypeId)
        }
        if (!exists) {
            throw site.addzero.kcloud.plugins.hostconfig.api.common.NotFoundException("Register type not found")
        }
    }

    private fun loadTag(tagId: Long): Tag {
        return tagRepository
            .findById(tagId, Fetchers.tagDetail)
            .orElseThrow { site.addzero.kcloud.plugins.hostconfig.api.common.NotFoundException("Tag not found") }
    }

    private fun moveTag(tagId: Long, currentDeviceId: Long, targetDeviceId: Long, sortIndex: Int) {
        if (currentDeviceId == targetDeviceId) {
            val orderedIds = reorderIds(
                queryIds("SELECT id FROM tag WHERE device_id = ? ORDER BY sort_index ASC, id ASC", currentDeviceId),
                tagId,
                sortIndex,
            )
            batchUpdateSort("UPDATE tag SET sort_index = ?, updated_at = ? WHERE id = ?", orderedIds)
            return
        }

        jdbcTemplate.update(
            "UPDATE tag SET device_id = ?, updated_at = ? WHERE id = ?",
            targetDeviceId,
            now(),
            tagId,
        )
        val oldIds = queryIds("SELECT id FROM tag WHERE device_id = ? ORDER BY sort_index ASC, id ASC", currentDeviceId)
        val newIds = reorderIds(
            queryIds("SELECT id FROM tag WHERE device_id = ? ORDER BY sort_index ASC, id ASC", targetDeviceId),
            tagId,
            sortIndex,
        )
        batchUpdateSort("UPDATE tag SET sort_index = ?, updated_at = ? WHERE id = ?", oldIds)
        batchUpdateSort("UPDATE tag SET sort_index = ?, updated_at = ? WHERE id = ?", newIds)
    }

    private fun queryIds(sql: String, vararg args: Any): MutableList<Long> =
        jdbcTemplate.query(sql, { rs, _ -> rs.getLong(1) }, *args).toMutableList()

    private fun reorderIds(ids: MutableList<Long>, movedId: Long, targetIndex: Int): MutableList<Long> {
        ids.remove(movedId)
        ids.add(targetIndex.coerceIn(0, ids.size), movedId)
        return ids
    }

    private fun batchUpdateSort(sql: String, orderedIds: List<Long>) {
        if (orderedIds.isEmpty()) {
            return
        }
        val now = now()
        val batchArgs = orderedIds.mapIndexed { index, id -> arrayOf<Any>(index, now, id) }
        jdbcTemplate.batchUpdate(sql, batchArgs)
    }

    private fun Tag.toResponse(): TagResponse =
        TagResponse(
            id = id,
            name = name,
            description = description,
            dataTypeId = dataType.id,
            dataTypeCode = dataType.code,
            dataTypeName = dataType.name,
            registerTypeId = registerType.id,
            registerTypeCode = registerType.code,
            registerTypeName = registerType.name,
            registerAddress = registerAddress,
            enabled = enabled,
            defaultValue = defaultValue,
            exceptionValue = exceptionValue,
            pointType = pointType,
            debounceMs = debounceMs,
            sortIndex = sortIndex,
            scalingEnabled = scalingEnabled,
            scalingOffset = scalingOffset,
            rawMin = rawMin,
            rawMax = rawMax,
            engMin = engMin,
            engMax = engMax,
            forwardEnabled = forwardEnabled,
            forwardRegisterTypeId = forwardRegisterType?.id,
            forwardRegisterTypeCode = forwardRegisterType?.code,
            forwardRegisterTypeName = forwardRegisterType?.name,
            forwardRegisterAddress = forwardRegisterAddress,
            valueTexts = valueTexts
                .sortedWith(compareBy(TagValueText::sortIndex, TagValueText::id))
                .map {
                    TagValueTextResponse(
                        id = it.id,
                        rawValue = it.rawValue,
                        displayText = it.displayText,
                        sortIndex = it.sortIndex,
                    )
                },
        )

    private fun String?.cleanNullable(): String? =
        this?.trim()?.ifBlank { null }

    private fun now(): Long = System.currentTimeMillis()
}
