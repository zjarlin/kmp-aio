package site.addzero.kcloud.plugins.hostconfig.service

import java.math.BigDecimal
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.ne
import org.babyfish.jimmer.sql.kt.exists
import org.koin.core.annotation.Single
import site.addzero.kcloud.jimmer.di.batchUpdate
import site.addzero.kcloud.jimmer.di.executeUpdate
import site.addzero.kcloud.jimmer.di.queryIds
import site.addzero.kcloud.jimmer.di.withTransaction
import site.addzero.kcloud.plugins.hostconfig.api.common.PageResponse
import site.addzero.kcloud.plugins.hostconfig.api.tag.ReplaceTagValueTextsRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagPositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagValueTextResponse
import site.addzero.kmp.exp.BusinessValidationException
import site.addzero.kmp.exp.ConflictException
import site.addzero.kmp.exp.NotFoundException
import site.addzero.kcloud.plugins.hostconfig.model.entity.*

@Single
/**
 * 提供标签相关服务。
 *
 * @property sql Jimmer SQL 客户端。
 */
class TagService(
    private val sql: KSqlClient,
) {
    /**
     * 获取标签。
     *
     * @param tagId 标签 ID。
     */
    fun getTag(tagId: Long): TagResponse = loadTag(tagId).toResponse()

    /**
     * 列出标签。
     *
     * @param deviceId 设备 ID。
     * @param offset offset。
     * @param size size。
     */
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

    /**
     * 创建标签。
     *
     * @param deviceId 设备 ID。
     * @param request 请求参数。
     */
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
        val entity = Tag {
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

    /**
     * 更新标签。
     *
     * @param tagId 标签 ID。
     * @param request 请求参数。
     */
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
        val entity = Tag {
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

    /**
     * 替换值texts。
     *
     * @param tagId 标签 ID。
     * @param request 请求参数。
     */
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
                TagValueText {
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

    /**
     * 更新标签位置。
     *
     * @param tagId 标签 ID。
     * @param request 请求参数。
     */
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

    /**
     * 删除标签。
     *
     * @param tagId 标签 ID。
     */
    fun deleteTag(tagId: Long) {
        loadTag(tagId)
        executeUpdate("DELETE FROM host_config_tag WHERE id = ?", tagId)
    }

    /**
     * 校验标签请求。
     *
     * @param scalingEnabled 缩放启用状态。
     * @param hasRawMin has原始min。
     * @param hasRawMax has原始max。
     * @param hasEngMin has工程min。
     * @param hasEngMax has工程max。
     * @param forwardEnabled 转发启用状态。
     * @param forwardRegisterTypeId 转发寄存器类型 ID。
     * @param forwardRegisterAddress 转发寄存器地址。
     */
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

    /**
     * 确保标签名称唯一性。
     *
     * @param deviceId 设备 ID。
     * @param name 名称。
     * @param excludeId 需要排除的对象 ID。
     */
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

    /**
     * 确保标签地址唯一性。
     *
     * @param deviceId 设备 ID。
     * @param registerTypeId 寄存器类型 ID。
     * @param registerAddress 寄存器地址。
     * @param excludeId 需要排除的对象 ID。
     */
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

    /**
     * 确保设备存在性。
     *
     * @param deviceId 设备 ID。
     */
    private fun ensureDeviceExists(deviceId: Long) {
        val exists = sql.exists(Device::class) {
            where(table.id eq deviceId)
        }
        if (!exists) {
            throw NotFoundException("Device not found")
        }
    }

    /**
     * 确保数据类型存在性。
     *
     * @param dataTypeId 数据类型 ID。
     */
    private fun ensureDataTypeExists(dataTypeId: Long) {
        val exists = sql.exists(DataType::class) {
            where(table.id eq dataTypeId)
        }
        if (!exists) {
            throw NotFoundException("Data type not found")
        }
    }

    /**
     * 确保register类型存在性。
     *
     * @param registerTypeId 寄存器类型 ID。
     */
    private fun ensureRegisterTypeExists(registerTypeId: Long) {
        val exists = sql.exists(RegisterType::class) {
            where(table.id eq registerTypeId)
        }
        if (!exists) {
            throw NotFoundException("Register type not found")
        }
    }

    /**
     * 加载标签。
     *
     * @param tagId 标签 ID。
     */
    private fun loadTag(tagId: Long): Tag =
        sql.createQuery(Tag::class) {
            where(table.id eq tagId)
            select(table.fetch(Fetchers.tagDetail))
        }.execute().firstOrNull() ?: throw NotFoundException("Tag not found")

    /**
     * 移动标签。
     *
     * @param tagId 标签 ID。
     * @param currentDeviceId 当前设备 ID。
     * @param targetDeviceId 目标设备 ID。
     * @param sortIndex 目标排序序号。
     */
    private fun moveTag(
        tagId: Long,
        currentDeviceId: Long,
        targetDeviceId: Long,
        sortIndex: Int,
    ) {
        sql.withTransaction {
            if (currentDeviceId == targetDeviceId) {
                val orderedIds = reorderIds(
                    queryIds(
                        "SELECT id FROM host_config_tag WHERE device_id = ? ORDER BY sort_index ASC, id ASC",
                        currentDeviceId,
                    ),
                    tagId,
                    sortIndex,
                )
                batchUpdateSort(
                    "UPDATE host_config_tag SET sort_index = ?, updated_at = ? WHERE id = ?",
                    orderedIds,
                )
                return@withTransaction
            }

            sql.executeUpdate(
                "UPDATE host_config_tag SET device_id = ?, updated_at = ? WHERE id = ?",
                targetDeviceId,
                now(),
                tagId,
            )
            val oldIds = queryIds(
                "SELECT id FROM host_config_tag WHERE device_id = ? ORDER BY sort_index ASC, id ASC",
                currentDeviceId,
            )
            val newIds = reorderIds(
                queryIds(
                    "SELECT id FROM host_config_tag WHERE device_id = ? ORDER BY sort_index ASC, id ASC",
                    targetDeviceId,
                ),
                tagId,
                sortIndex,
            )
            batchUpdateSort("UPDATE host_config_tag SET sort_index = ?, updated_at = ? WHERE id = ?", oldIds)
            batchUpdateSort("UPDATE host_config_tag SET sort_index = ?, updated_at = ? WHERE id = ?", newIds)
        }
    }

    private fun executeUpdate(
        sql: String,
        vararg args: Any?,
    ): Int = this.sql.withTransaction { _ ->
        this.sql.executeUpdate(sql, *args)
    }

    private fun queryIds(
        sql: String,
        vararg args: Any?,
    ): MutableList<Long> = this.sql.withTransaction { _ ->
        this.sql.queryIds(sql, *args).toMutableList()
    }

    /**
     * 按目标位置重排 ID 列表。
     *
     * @param ids ID 列表。
     * @param movedId 需要移动的 ID。
     * @param targetIndex 目标位置索引。
     */
    private fun reorderIds(
        ids: MutableList<Long>,
        movedId: Long,
        targetIndex: Int,
    ): MutableList<Long> {
        ids.remove(movedId)
        ids.add(targetIndex.coerceIn(0, ids.size), movedId)
        return ids
    }

    /**
     * 批量更新排序字段。
     *
     * @param sql SQL 语句。
     * @param orderedIds 排序后的 ID 列表。
     */
    private fun batchUpdateSort(
        sql: String,
        orderedIds: List<Long>,
    ) {
        val updatedAt = now()
        this.sql.withTransaction {
            this.sql.batchUpdate(
                sql,
                orderedIds.mapIndexed { index, id ->
                    listOf(index, updatedAt, id)
                },
            )
        }
    }

    /**
     * 处理标签。
     */
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

    /**
     * 处理string。
     */
    private fun String?.cleanNullable(): String? =
        this?.trim()?.ifBlank { null }

    /**
     * 处理string。
     */
    private fun String?.toDecimalOrNull(): BigDecimal? =
        this.cleanNullable()?.toBigDecimalOrNull()

    /**
     * 处理bigdecimal。
     */
    private fun BigDecimal?.toApiDecimal(): String? =
        this?.stripTrailingZeros()?.toPlainString()

    /**
     * 获取当前时间戳。
     */
    private fun now(): Long = System.currentTimeMillis()
}
