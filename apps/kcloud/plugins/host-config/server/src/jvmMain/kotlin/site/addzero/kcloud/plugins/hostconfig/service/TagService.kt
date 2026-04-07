package site.addzero.kcloud.plugins.hostconfig.service

import java.math.BigDecimal
import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.ne
import org.babyfish.jimmer.sql.kt.exists
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.hostconfig.routes.common.BusinessValidationException
import site.addzero.kcloud.plugins.hostconfig.routes.common.ConflictException
import site.addzero.kcloud.plugins.hostconfig.routes.common.NotFoundException
import site.addzero.kcloud.plugins.hostconfig.routes.common.PageResponse
import site.addzero.kcloud.plugins.hostconfig.routes.tag.ReplaceTagValueTextsRequest
import site.addzero.kcloud.plugins.hostconfig.routes.tag.TagCreateRequest
import site.addzero.kcloud.plugins.hostconfig.routes.tag.TagPositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.routes.tag.TagResponse
import site.addzero.kcloud.plugins.hostconfig.routes.tag.TagUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.routes.tag.TagValueTextResponse
import site.addzero.kcloud.plugins.hostconfig.model.entity.*

@Single
class TagService(
    private val sql: KSqlClient,
    private val jdbc: HostConfigJdbc,
) {
    fun getTag(tagId: Long): TagResponse = loadTag(tagId).toResponse()

    fun listTags(
        deviceId: Long,
        offset: Int,
        size: Int,
    ): PageResponse<TagResponse> {
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
        val pages = if (total == 0L) 0 else ((total + safeSize - 1) / safeSize).toInt()
        return PageResponse(
            d = rows.map { it.toResponse() },
            t = total,
            p = pages,
        )
    }

    fun createTag(
        deviceId: Long,
        request: TagCreateRequest,
    ): TagResponse {
        ensureDeviceExists(deviceId)
        ensureDataTypeExists(request.dataTypeId)
        ensureRegisterTypeExists(request.registerTypeId)
        request.forwardRegisterTypeId?.let(::ensureRegisterTypeExists)
        validateTagRequest(
            scalingEnabled = request.scalingEnabled,
            hasRawMin = request.rawMin != null,
            hasRawMax = request.rawMax != null,
            hasEngMin = request.engMin != null,
            hasEngMax = request.engMax != null,
            forwardEnabled = request.forwardEnabled,
            forwardRegisterTypeId = request.forwardRegisterTypeId,
            forwardRegisterAddress = request.forwardRegisterAddress,
        )
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
            this.scalingOffset = request.scalingOffset.toDecimalOrNull()
            this.rawMin = request.rawMin.toDecimalOrNull()
            this.rawMax = request.rawMax.toDecimalOrNull()
            this.engMin = request.engMin.toDecimalOrNull()
            this.engMax = request.engMax.toDecimalOrNull()
            this.forwardEnabled = request.forwardEnabled
            this.forwardRegisterAddress = request.forwardRegisterAddress
            this.createdAt = now
            this.updatedAt = now
        }
        val tag = sql.saveCommand(entity) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        return loadTag(tag.id).toResponse()
    }

    fun updateTag(
        tagId: Long,
        request: TagUpdateRequest,
    ): TagResponse {
        val current = loadTag(tagId)
        ensureDataTypeExists(request.dataTypeId)
        ensureRegisterTypeExists(request.registerTypeId)
        request.forwardRegisterTypeId?.let(::ensureRegisterTypeExists)
        validateTagRequest(
            scalingEnabled = request.scalingEnabled,
            hasRawMin = request.rawMin != null,
            hasRawMax = request.rawMax != null,
            hasEngMin = request.engMin != null,
            hasEngMax = request.engMax != null,
            forwardEnabled = request.forwardEnabled,
            forwardRegisterTypeId = request.forwardRegisterTypeId,
            forwardRegisterAddress = request.forwardRegisterAddress,
        )
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
            this.scalingOffset = request.scalingOffset.toDecimalOrNull()
            this.rawMin = request.rawMin.toDecimalOrNull()
            this.rawMax = request.rawMax.toDecimalOrNull()
            this.engMin = request.engMin.toDecimalOrNull()
            this.engMax = request.engMax.toDecimalOrNull()
            this.forwardEnabled = request.forwardEnabled
            this.forwardRegisterAddress = request.forwardRegisterAddress
            this.updatedAt = now()
        }
        sql.saveCommand(entity) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute()
        return loadTag(tagId).toResponse()
    }

    fun replaceValueTexts(
        tagId: Long,
        request: ReplaceTagValueTextsRequest,
    ): List<TagValueTextResponse> {
        loadTag(tagId)
        sql.createDelete(TagValueText::class) {
            where(table.tag.id eq tagId)
        }.execute()
        val now = now()
        request.items.forEach { item ->
            sql.saveCommand(
                new(TagValueText::class).by {
                    this.tagId = tagId
                    this.rawValue = item.rawValue.trim()
                    this.displayText = item.displayText.trim()
                    this.sortIndex = item.sortIndex
                    this.createdAt = now
                    this.updatedAt = now
                },
            ) {
                setMode(SaveMode.INSERT_ONLY)
            }.execute()
        }
        return loadTag(tagId).toResponse().valueTexts
    }

    fun updateTagPosition(
        tagId: Long,
        request: TagPositionUpdateRequest,
    ): TagResponse {
        val current = loadTag(tagId)
        ensureDeviceExists(request.deviceId)
        ensureTagNameUnique(request.deviceId, current.name, tagId)
        ensureTagAddressUnique(request.deviceId, current.registerType.id, current.registerAddress, tagId)
        moveTag(tagId, current.device.id, request.deviceId, request.sortIndex)
        return loadTag(tagId).toResponse()
    }

    fun deleteTag(tagId: Long) {
        loadTag(tagId)
        jdbc.update("DELETE FROM host_config_tag WHERE id = ?", tagId)
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

    private fun ensureTagNameUnique(
        deviceId: Long,
        name: String,
        excludeId: Long?,
    ) {
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

    private fun ensureTagAddressUnique(
        deviceId: Long,
        registerTypeId: Long,
        registerAddress: Int,
        excludeId: Long?,
    ) {
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
        val exists = sql.exists(Device::class) {
            where(table.id eq deviceId)
        }
        if (!exists) {
            throw NotFoundException("Device not found")
        }
    }

    private fun ensureDataTypeExists(dataTypeId: Long) {
        val exists = sql.exists(DataType::class) {
            where(table.id eq dataTypeId)
        }
        if (!exists) {
            throw NotFoundException("Data type not found")
        }
    }

    private fun ensureRegisterTypeExists(registerTypeId: Long) {
        val exists = sql.exists(RegisterType::class) {
            where(table.id eq registerTypeId)
        }
        if (!exists) {
            throw NotFoundException("Register type not found")
        }
    }

    private fun loadTag(tagId: Long): Tag =
        sql.createQuery(Tag::class) {
            where(table.id eq tagId)
            select(table.fetch(Fetchers.tagDetail))
        }.execute().firstOrNull() ?: throw NotFoundException("Tag not found")

    private fun moveTag(
        tagId: Long,
        currentDeviceId: Long,
        targetDeviceId: Long,
        sortIndex: Int,
    ) {
        if (currentDeviceId == targetDeviceId) {
            val orderedIds = reorderIds(
                jdbc.queryIds(
                    "SELECT id FROM host_config_tag WHERE device_id = ? ORDER BY sort_index ASC, id ASC",
                    currentDeviceId,
                ),
                tagId,
                sortIndex,
            )
            batchUpdateSort("UPDATE host_config_tag SET sort_index = ?, updated_at = ? WHERE id = ?", orderedIds)
            return
        }

        jdbc.update(
            "UPDATE host_config_tag SET device_id = ?, updated_at = ? WHERE id = ?",
            targetDeviceId,
            now(),
            tagId,
        )
        val oldIds = jdbc.queryIds(
            "SELECT id FROM host_config_tag WHERE device_id = ? ORDER BY sort_index ASC, id ASC",
            currentDeviceId,
        )
        val newIds = reorderIds(
            jdbc.queryIds(
                "SELECT id FROM host_config_tag WHERE device_id = ? ORDER BY sort_index ASC, id ASC",
                targetDeviceId,
            ),
            tagId,
            sortIndex,
        )
        batchUpdateSort("UPDATE host_config_tag SET sort_index = ?, updated_at = ? WHERE id = ?", oldIds)
        batchUpdateSort("UPDATE host_config_tag SET sort_index = ?, updated_at = ? WHERE id = ?", newIds)
    }

    private fun reorderIds(
        ids: MutableList<Long>,
        movedId: Long,
        targetIndex: Int,
    ): MutableList<Long> {
        ids.remove(movedId)
        ids.add(targetIndex.coerceIn(0, ids.size), movedId)
        return ids
    }

    private fun batchUpdateSort(
        sql: String,
        orderedIds: List<Long>,
    ) {
        jdbc.batchUpdateSort(
            sql = sql,
            orderedIds = orderedIds,
            updatedAt = now(),
        )
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
            scalingOffset = scalingOffset.toApiDecimal(),
            rawMin = rawMin.toApiDecimal(),
            rawMax = rawMax.toApiDecimal(),
            engMin = engMin.toApiDecimal(),
            engMax = engMax.toApiDecimal(),
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

    private fun String?.toDecimalOrNull(): BigDecimal? =
        this.cleanNullable()?.toBigDecimalOrNull()

    private fun BigDecimal?.toApiDecimal(): String? =
        this?.stripTrailingZeros()?.toPlainString()

    private fun now(): Long = System.currentTimeMillis()
}
