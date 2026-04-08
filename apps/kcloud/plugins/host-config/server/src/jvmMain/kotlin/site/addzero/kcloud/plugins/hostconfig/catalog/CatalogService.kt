package site.addzero.kcloud.plugins.hostconfig.catalog

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.ne
import org.babyfish.jimmer.sql.kt.exists
import org.koin.core.annotation.Single
import site.addzero.biz.spec.iot.IotPropertySpec
import site.addzero.biz.spec.iot.IotValueType
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogDetailFieldMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogEntityMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogEntityType
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogFieldMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogFieldOptionResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogFieldWidgetType
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogOptionSetResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogSnapshotResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogValueRenderType
import site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionTreeResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionTreeResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.SpecIotPropertyResponse
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.*
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.DeviceDefinition
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.FeatureDefinition
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.LabelDefinition
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.ProductDefinition
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.ProductDefinitionLabelLink
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.PropertyDefinition
import site.addzero.kcloud.plugins.hostconfig.routes.common.ConflictException
import site.addzero.kcloud.plugins.hostconfig.routes.common.NotFoundException
import site.addzero.kcloud.plugins.hostconfig.model.entity.*
import site.addzero.kcloud.plugins.hostconfig.model.entity.DataType
import site.addzero.kcloud.plugins.hostconfig.model.entity.DeviceType
import site.addzero.kcloud.plugins.hostconfig.service.Fetchers

@Single
class CatalogService(
    private val sql: KSqlClient,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun getSnapshot(): CatalogSnapshotResponse {
        val products = sql.createQuery(ProductDefinition::class) {
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.productDefinitionTree))
        }.execute().map { it.toTreeResponse() }
        val labels = loadAllLabels().map { it.toResponse() }
        return CatalogSnapshotResponse(
            products = products,
            labels = labels,
            metadata = buildMetadata(labels),
        )
    }

    fun getMetadata(): CatalogMetadataResponse {
        return buildMetadata(loadAllLabels().map { it.toResponse() })
    }

    fun listLabels(): List<LabelDefinitionResponse> {
        return loadAllLabels().map { it.toResponse() }
    }

    fun createLabel(request: LabelDefinitionCreateRequest): LabelDefinitionResponse {
        val code = request.code.trim()
        ensureLabelCodeUnique(code, null)
        val now = now()
        val entity = LabelDefinition {
            this.code = code
            this.name = request.name.trim()
            this.description = request.description.cleanNullable()
            this.colorHex = request.colorHex.cleanNullable()
            this.sortIndex = request.sortIndex
            this.createdAt = now
            this.updatedAt = now
        }
        val label = sql.saveCommand(entity) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        return loadLabel(label.id).toResponse()
    }

    fun updateLabel(
        labelId: Long,
        request: LabelDefinitionUpdateRequest,
    ): LabelDefinitionResponse {
        ensureLabelExists(labelId)
        val code = request.code.trim()
        ensureLabelCodeUnique(code, labelId)
        val entity = LabelDefinition {
            id = labelId
            this.code = code
            this.name = request.name.trim()
            this.description = request.description.cleanNullable()
            this.colorHex = request.colorHex.cleanNullable()
            this.sortIndex = request.sortIndex
            this.updatedAt = now()
        }
        sql.saveCommand(entity) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute()
        return loadLabel(labelId).toResponse()
    }

    fun deleteLabel(labelId: Long) {
        ensureLabelExists(labelId)
        sql.createDelete(LabelDefinition::class) {
            where(table.id eq labelId)
        }.execute()
    }

    fun createProduct(request: ProductDefinitionCreateRequest): ProductDefinitionTreeResponse {
        val code = request.code.trim()
        ensureProductCodeUnique(code, null)
        ensureLabelIdsExist(request.labelIds)
        val now = now()
        val entity = ProductDefinition {
            this.code = code
            this.name = request.name.trim()
            this.description = request.description.cleanNullable()
            this.vendor = request.vendor.cleanNullable()
            this.category = request.category.cleanNullable()
            this.enabled = request.enabled
            this.sortIndex = request.sortIndex
            this.createdAt = now
            this.updatedAt = now
        }
        val product = sql.saveCommand(entity) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        replaceProductLabels(product.id, request.labelIds)
        return loadProduct(product.id).toTreeResponse()
    }

    fun updateProduct(
        productId: Long,
        request: ProductDefinitionUpdateRequest,
    ): ProductDefinitionTreeResponse {
        ensureProductExists(productId)
        val code = request.code.trim()
        ensureProductCodeUnique(code, productId)
        ensureLabelIdsExist(request.labelIds)
        val entity = ProductDefinition {
            id = productId
            this.code = code
            this.name = request.name.trim()
            this.description = request.description.cleanNullable()
            this.vendor = request.vendor.cleanNullable()
            this.category = request.category.cleanNullable()
            this.enabled = request.enabled
            this.sortIndex = request.sortIndex
            this.updatedAt = now()
        }
        sql.saveCommand(entity) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute()
        replaceProductLabels(productId, request.labelIds)
        return loadProduct(productId).toTreeResponse()
    }

    fun deleteProduct(productId: Long) {
        ensureProductExists(productId)
        sql.createDelete(ProductDefinition::class) {
            where(table.id eq productId)
        }.execute()
    }

    fun createDeviceDefinition(
        productId: Long,
        request: DeviceDefinitionCreateRequest,
    ): DeviceDefinitionTreeResponse {
        ensureProductExists(productId)
        request.deviceTypeId?.let(::ensureDeviceTypeExists)
        val code = request.code.trim()
        ensureDeviceDefinitionCodeUnique(productId, code, null)
        val now = now()
        val entity = DeviceDefinition {
            this.productId = productId
            this.deviceTypeId = request.deviceTypeId
            this.code = code
            this.name = request.name.trim()
            this.description = request.description.cleanNullable()
            this.supportsTelemetry = request.supportsTelemetry
            this.supportsControl = request.supportsControl
            this.sortIndex = request.sortIndex
            this.createdAt = now
            this.updatedAt = now
        }
        val device = sql.saveCommand(entity) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        return loadDeviceDefinition(device.id).toTreeResponse()
    }

    fun updateDeviceDefinition(
        deviceDefinitionId: Long,
        request: DeviceDefinitionUpdateRequest,
    ): DeviceDefinitionTreeResponse {
        val current = loadDeviceDefinition(deviceDefinitionId)
        request.deviceTypeId?.let(::ensureDeviceTypeExists)
        val code = request.code.trim()
        ensureDeviceDefinitionCodeUnique(current.product.id, code, deviceDefinitionId)
        val entity = DeviceDefinition {
            id = deviceDefinitionId
            productId = current.product.id
            deviceTypeId = request.deviceTypeId
            this.code = code
            this.name = request.name.trim()
            this.description = request.description.cleanNullable()
            this.supportsTelemetry = request.supportsTelemetry
            this.supportsControl = request.supportsControl
            this.sortIndex = request.sortIndex
            this.updatedAt = now()
        }
        sql.saveCommand(entity) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute()
        return loadDeviceDefinition(deviceDefinitionId).toTreeResponse()
    }

    fun deleteDeviceDefinition(deviceDefinitionId: Long) {
        ensureDeviceDefinitionExists(deviceDefinitionId)
        sql.createDelete(DeviceDefinition::class) {
            where(table.id eq deviceDefinitionId)
        }.execute()
    }

    fun createPropertyDefinition(
        deviceDefinitionId: Long,
        request: PropertyDefinitionCreateRequest,
    ): PropertyDefinitionResponse {
        ensureDeviceDefinitionExists(deviceDefinitionId)
        ensureDataTypeExists(request.dataTypeId)
        val identifier = request.identifier.trim()
        ensurePropertyIdentifierUnique(deviceDefinitionId, identifier, null)
        val now = now()
        val entity = PropertyDefinition {
            this.deviceDefinitionId = deviceDefinitionId
            this.dataTypeId = request.dataTypeId
            this.identifier = identifier
            this.name = request.name.trim()
            this.description = request.description.cleanNullable()
            this.unit = request.unit.cleanNullable()
            this.required = request.required
            this.writable = request.writable
            this.telemetry = request.telemetry
            this.nullable = request.nullable
            this.length = request.length
            this.attributesJson = encodeAttributes(request.attributes)
            this.sortIndex = request.sortIndex
            this.createdAt = now
            this.updatedAt = now
        }
        val property = sql.saveCommand(entity) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        return loadPropertyDefinition(property.id).toResponse()
    }

    fun updatePropertyDefinition(
        propertyDefinitionId: Long,
        request: PropertyDefinitionUpdateRequest,
    ): PropertyDefinitionResponse {
        val current = loadPropertyDefinition(propertyDefinitionId)
        ensureDataTypeExists(request.dataTypeId)
        val identifier = request.identifier.trim()
        ensurePropertyIdentifierUnique(current.deviceDefinition.id, identifier, propertyDefinitionId)
        val entity = PropertyDefinition {
            id = propertyDefinitionId
            deviceDefinitionId = current.deviceDefinition.id
            dataTypeId = request.dataTypeId
            this.identifier = identifier
            this.name = request.name.trim()
            this.description = request.description.cleanNullable()
            this.unit = request.unit.cleanNullable()
            this.required = request.required
            this.writable = request.writable
            this.telemetry = request.telemetry
            this.nullable = request.nullable
            this.length = request.length
            this.attributesJson = encodeAttributes(request.attributes)
            this.sortIndex = request.sortIndex
            this.updatedAt = now()
        }
        sql.saveCommand(entity) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute()
        return loadPropertyDefinition(propertyDefinitionId).toResponse()
    }

    fun deletePropertyDefinition(propertyDefinitionId: Long) {
        ensurePropertyDefinitionExists(propertyDefinitionId)
        sql.createDelete(PropertyDefinition::class) {
            where(table.id eq propertyDefinitionId)
        }.execute()
    }

    fun createFeatureDefinition(
        deviceDefinitionId: Long,
        request: FeatureDefinitionCreateRequest,
    ): FeatureDefinitionResponse {
        ensureDeviceDefinitionExists(deviceDefinitionId)
        val identifier = request.identifier.trim()
        ensureFeatureIdentifierUnique(deviceDefinitionId, identifier, null)
        val now = now()
        val entity = FeatureDefinition {
            this.deviceDefinitionId = deviceDefinitionId
            this.identifier = identifier
            this.name = request.name.trim()
            this.description = request.description.cleanNullable()
            this.inputSchema = request.inputSchema.cleanNullable()
            this.outputSchema = request.outputSchema.cleanNullable()
            this.asynchronous = request.asynchronous
            this.sortIndex = request.sortIndex
            this.createdAt = now
            this.updatedAt = now
        }
        val feature = sql.saveCommand(entity) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        return loadFeatureDefinition(feature.id).toResponse()
    }

    fun updateFeatureDefinition(
        featureDefinitionId: Long,
        request: FeatureDefinitionUpdateRequest,
    ): FeatureDefinitionResponse {
        val current = loadFeatureDefinition(featureDefinitionId)
        val identifier = request.identifier.trim()
        ensureFeatureIdentifierUnique(current.deviceDefinition.id, identifier, featureDefinitionId)
        val entity = FeatureDefinition {
            id = featureDefinitionId
            deviceDefinitionId = current.deviceDefinition.id
            this.identifier = identifier
            this.name = request.name.trim()
            this.description = request.description.cleanNullable()
            this.inputSchema = request.inputSchema.cleanNullable()
            this.outputSchema = request.outputSchema.cleanNullable()
            this.asynchronous = request.asynchronous
            this.sortIndex = request.sortIndex
            this.updatedAt = now()
        }
        sql.saveCommand(entity) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute()
        return loadFeatureDefinition(featureDefinitionId).toResponse()
    }

    fun deleteFeatureDefinition(featureDefinitionId: Long) {
        ensureFeatureDefinitionExists(featureDefinitionId)
        sql.createDelete(FeatureDefinition::class) {
            where(table.id eq featureDefinitionId)
        }.execute()
    }

    fun listSpecIotProperties(deviceDefinitionId: Long): List<SpecIotPropertyResponse> {
        val deviceDefinition = loadDeviceDefinition(deviceDefinitionId)
        return deviceDefinition.properties
            .sortedWith(compareBy(PropertyDefinition::sortIndex, PropertyDefinition::id))
            .map { property ->
                val spec = IotPropertySpec.builder()
                    .identifier(property.identifier)
                    .name(property.name)
                    .description(property.description)
                    .unit(property.unit)
                    .valueType(property.dataType.code.toIotValueType())
                    .length(property.length)
                    .attribute("dataTypeCode", property.dataType.code)
                    .attribute("nullable", property.nullable.toString())
                    .attribute("required", property.required.toString())
                    .attribute("writable", property.writable.toString())
                    .attribute("telemetry", property.telemetry.toString())
                    .apply {
                        decodeAttributes(property.attributesJson).forEach { (key, value) ->
                            attribute(key, value)
                        }
                    }
                    .build()
                SpecIotPropertyResponse(
                    identifier = spec.identifier,
                    name = spec.name,
                    description = spec.description,
                    unit = spec.unit,
                    valueType = spec.valueType.name,
                    length = spec.length,
                    attributes = spec.attributes,
                )
            }
    }

    private fun buildMetadata(
        labels: List<LabelDefinitionResponse>,
    ): CatalogMetadataResponse {
        val deviceTypeOptions = sql.createQuery(DeviceType::class) {
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.deviceType))
        }.execute().map {
            CatalogFieldOptionResponse(
                value = it.id.toString(),
                label = it.name,
                description = it.description,
            )
        }
        val dataTypeOptions = sql.createQuery(DataType::class) {
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.dataType))
        }.execute().map {
            CatalogFieldOptionResponse(
                value = it.id.toString(),
                label = it.name,
                description = it.description,
            )
        }
        return CatalogMetadataResponse(
            entities = listOf(
                CatalogEntityMetadataResponse(
                    entityType = CatalogEntityType.PRODUCT,
                    title = "产品定义",
                    subtitle = "管理产品级别的型号、供应商、分类和标签。",
                    formFields = listOf(
                        CatalogFieldMetadataResponse("code", "产品编码", CatalogFieldWidgetType.TEXT, required = true),
                        CatalogFieldMetadataResponse("name", "产品名称", CatalogFieldWidgetType.TEXT, required = true),
                        CatalogFieldMetadataResponse("description", "产品描述", CatalogFieldWidgetType.TEXTAREA),
                        CatalogFieldMetadataResponse("vendor", "供应商", CatalogFieldWidgetType.TEXT),
                        CatalogFieldMetadataResponse("category", "产品分类", CatalogFieldWidgetType.TEXT),
                        CatalogFieldMetadataResponse("enabled", "是否启用", CatalogFieldWidgetType.BOOLEAN),
                        CatalogFieldMetadataResponse("sortIndex", "排序", CatalogFieldWidgetType.NUMBER),
                        CatalogFieldMetadataResponse(
                            key = "labelIds",
                            label = "标签",
                            widget = CatalogFieldWidgetType.MULTI_SELECT,
                            optionSource = "labels",
                        ),
                    ),
                    detailFields = listOf(
                        CatalogDetailFieldMetadataResponse("code", "产品编码", CatalogValueRenderType.CODE),
                        CatalogDetailFieldMetadataResponse("name", "产品名称"),
                        CatalogDetailFieldMetadataResponse("vendor", "供应商"),
                        CatalogDetailFieldMetadataResponse("category", "产品分类"),
                        CatalogDetailFieldMetadataResponse("enabled", "启用状态", CatalogValueRenderType.BOOLEAN),
                        CatalogDetailFieldMetadataResponse("labels", "标签", CatalogValueRenderType.TAGS),
                        CatalogDetailFieldMetadataResponse("updatedAt", "最近更新", CatalogValueRenderType.DATETIME),
                    ),
                ),
                CatalogEntityMetadataResponse(
                    entityType = CatalogEntityType.DEVICE,
                    title = "设备定义",
                    subtitle = "产品下的具体设备型号能力，绑定设备类型和控制能力。",
                    formFields = listOf(
                        CatalogFieldMetadataResponse("code", "设备编码", CatalogFieldWidgetType.TEXT, required = true),
                        CatalogFieldMetadataResponse("name", "设备名称", CatalogFieldWidgetType.TEXT, required = true),
                        CatalogFieldMetadataResponse("description", "设备描述", CatalogFieldWidgetType.TEXTAREA),
                        CatalogFieldMetadataResponse(
                            key = "deviceTypeId",
                            label = "设备类型",
                            widget = CatalogFieldWidgetType.SELECT,
                            optionSource = "deviceTypes",
                        ),
                        CatalogFieldMetadataResponse("supportsTelemetry", "支持遥测", CatalogFieldWidgetType.BOOLEAN),
                        CatalogFieldMetadataResponse("supportsControl", "支持控制", CatalogFieldWidgetType.BOOLEAN),
                        CatalogFieldMetadataResponse("sortIndex", "排序", CatalogFieldWidgetType.NUMBER),
                    ),
                    detailFields = listOf(
                        CatalogDetailFieldMetadataResponse("code", "设备编码", CatalogValueRenderType.CODE),
                        CatalogDetailFieldMetadataResponse("name", "设备名称"),
                        CatalogDetailFieldMetadataResponse("deviceTypeName", "设备类型"),
                        CatalogDetailFieldMetadataResponse("supportsTelemetry", "支持遥测", CatalogValueRenderType.BOOLEAN),
                        CatalogDetailFieldMetadataResponse("supportsControl", "支持控制", CatalogValueRenderType.BOOLEAN),
                        CatalogDetailFieldMetadataResponse("updatedAt", "最近更新", CatalogValueRenderType.DATETIME),
                    ),
                ),
                CatalogEntityMetadataResponse(
                    entityType = CatalogEntityType.PROPERTY,
                    title = "属性定义",
                    subtitle = "描述设备遥测、状态和控制属性，并提供 spec-iot 兼容字段。",
                    formFields = listOf(
                        CatalogFieldMetadataResponse("identifier", "属性标识", CatalogFieldWidgetType.TEXT, required = true),
                        CatalogFieldMetadataResponse("name", "属性名称", CatalogFieldWidgetType.TEXT, required = true),
                        CatalogFieldMetadataResponse("description", "属性描述", CatalogFieldWidgetType.TEXTAREA),
                        CatalogFieldMetadataResponse(
                            key = "dataTypeId",
                            label = "数据类型",
                            widget = CatalogFieldWidgetType.SELECT,
                            required = true,
                            optionSource = "dataTypes",
                        ),
                        CatalogFieldMetadataResponse("unit", "单位", CatalogFieldWidgetType.TEXT),
                        CatalogFieldMetadataResponse("required", "是否必填", CatalogFieldWidgetType.BOOLEAN),
                        CatalogFieldMetadataResponse("writable", "是否可写", CatalogFieldWidgetType.BOOLEAN),
                        CatalogFieldMetadataResponse("telemetry", "是否遥测", CatalogFieldWidgetType.BOOLEAN),
                        CatalogFieldMetadataResponse("nullable", "是否可空", CatalogFieldWidgetType.BOOLEAN),
                        CatalogFieldMetadataResponse("length", "长度", CatalogFieldWidgetType.NUMBER),
                        CatalogFieldMetadataResponse("sortIndex", "排序", CatalogFieldWidgetType.NUMBER),
                        CatalogFieldMetadataResponse("attributes", "扩展属性", CatalogFieldWidgetType.JSON),
                    ),
                    detailFields = listOf(
                        CatalogDetailFieldMetadataResponse("identifier", "属性标识", CatalogValueRenderType.CODE),
                        CatalogDetailFieldMetadataResponse("name", "属性名称"),
                        CatalogDetailFieldMetadataResponse("dataTypeName", "数据类型"),
                        CatalogDetailFieldMetadataResponse("unit", "单位"),
                        CatalogDetailFieldMetadataResponse("required", "是否必填", CatalogValueRenderType.BOOLEAN),
                        CatalogDetailFieldMetadataResponse("writable", "是否可写", CatalogValueRenderType.BOOLEAN),
                        CatalogDetailFieldMetadataResponse("telemetry", "是否遥测", CatalogValueRenderType.BOOLEAN),
                        CatalogDetailFieldMetadataResponse("updatedAt", "最近更新", CatalogValueRenderType.DATETIME),
                    ),
                ),
                CatalogEntityMetadataResponse(
                    entityType = CatalogEntityType.FEATURE,
                    title = "功能定义",
                    subtitle = "描述设备可调用功能及其入参、出参结构。",
                    formFields = listOf(
                        CatalogFieldMetadataResponse("identifier", "功能标识", CatalogFieldWidgetType.TEXT, required = true),
                        CatalogFieldMetadataResponse("name", "功能名称", CatalogFieldWidgetType.TEXT, required = true),
                        CatalogFieldMetadataResponse("description", "功能描述", CatalogFieldWidgetType.TEXTAREA),
                        CatalogFieldMetadataResponse("inputSchema", "输入 JSON Schema", CatalogFieldWidgetType.JSON),
                        CatalogFieldMetadataResponse("outputSchema", "输出 JSON Schema", CatalogFieldWidgetType.JSON),
                        CatalogFieldMetadataResponse("asynchronous", "异步执行", CatalogFieldWidgetType.BOOLEAN),
                        CatalogFieldMetadataResponse("sortIndex", "排序", CatalogFieldWidgetType.NUMBER),
                    ),
                    detailFields = listOf(
                        CatalogDetailFieldMetadataResponse("identifier", "功能标识", CatalogValueRenderType.CODE),
                        CatalogDetailFieldMetadataResponse("name", "功能名称"),
                        CatalogDetailFieldMetadataResponse("asynchronous", "异步执行", CatalogValueRenderType.BOOLEAN),
                        CatalogDetailFieldMetadataResponse("updatedAt", "最近更新", CatalogValueRenderType.DATETIME),
                    ),
                ),
                CatalogEntityMetadataResponse(
                    entityType = CatalogEntityType.LABEL,
                    title = "标签定义",
                    subtitle = "产品和设备的分类标签字典。",
                    formFields = listOf(
                        CatalogFieldMetadataResponse("code", "标签编码", CatalogFieldWidgetType.TEXT, required = true),
                        CatalogFieldMetadataResponse("name", "标签名称", CatalogFieldWidgetType.TEXT, required = true),
                        CatalogFieldMetadataResponse("description", "标签描述", CatalogFieldWidgetType.TEXTAREA),
                        CatalogFieldMetadataResponse("colorHex", "颜色", CatalogFieldWidgetType.TEXT),
                        CatalogFieldMetadataResponse("sortIndex", "排序", CatalogFieldWidgetType.NUMBER),
                    ),
                    detailFields = listOf(
                        CatalogDetailFieldMetadataResponse("code", "标签编码", CatalogValueRenderType.CODE),
                        CatalogDetailFieldMetadataResponse("name", "标签名称"),
                        CatalogDetailFieldMetadataResponse("colorHex", "颜色"),
                        CatalogDetailFieldMetadataResponse("updatedAt", "最近更新", CatalogValueRenderType.DATETIME),
                    ),
                ),
            ),
            optionSets = listOf(
                CatalogOptionSetResponse(
                    key = "labels",
                    options = labels.map {
                        CatalogFieldOptionResponse(
                            value = it.id.toString(),
                            label = it.name,
                            description = it.description,
                        )
                    },
                ),
                CatalogOptionSetResponse(
                    key = "deviceTypes",
                    options = deviceTypeOptions,
                ),
                CatalogOptionSetResponse(
                    key = "dataTypes",
                    options = dataTypeOptions,
                ),
            ),
        )
    }

    private fun replaceProductLabels(
        productId: Long,
        labelIds: List<Long>,
    ) {
        sql.createDelete(ProductDefinitionLabelLink::class) {
            where(table.product.id eq productId)
        }.execute()
        val now = now()
        labelIds.distinct().forEachIndexed { index, labelId ->
            sql.saveCommand(
                ProductDefinitionLabelLink {
                    this.productId = productId
                    this.labelId = labelId
                    this.sortIndex = index
                    this.createdAt = now
                    this.updatedAt = now
                },
            ) {
                setMode(SaveMode.INSERT_ONLY)
            }.execute()
        }
    }

    private fun loadAllLabels(): List<LabelDefinition> {
        return sql.createQuery(LabelDefinition::class) {
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.labelDefinition))
        }.execute()
    }

    private fun loadProduct(productId: Long): ProductDefinition {
        return sql.createQuery(ProductDefinition::class) {
            where(table.id eq productId)
            select(table.fetch(Fetchers.productDefinitionTree))
        }.execute().firstOrNull() ?: throw NotFoundException("Product definition not found")
    }

    private fun loadDeviceDefinition(deviceDefinitionId: Long): DeviceDefinition {
        return sql.createQuery(DeviceDefinition::class) {
            where(table.id eq deviceDefinitionId)
            select(table.fetch(Fetchers.deviceDefinitionDetail))
        }.execute().firstOrNull() ?: throw NotFoundException("Device definition not found")
    }

    private fun loadPropertyDefinition(propertyDefinitionId: Long): PropertyDefinition {
        return sql.createQuery(PropertyDefinition::class) {
            where(table.id eq propertyDefinitionId)
            select(table.fetch(Fetchers.propertyDefinitionDetail))
        }.execute().firstOrNull() ?: throw NotFoundException("Property definition not found")
    }

    private fun loadFeatureDefinition(featureDefinitionId: Long): FeatureDefinition {
        return sql.createQuery(FeatureDefinition::class) {
            where(table.id eq featureDefinitionId)
            select(table.fetch(Fetchers.featureDefinitionDetail))
        }.execute().firstOrNull() ?: throw NotFoundException("Feature definition not found")
    }

    private fun loadLabel(labelId: Long): LabelDefinition {
        return sql.createQuery(LabelDefinition::class) {
            where(table.id eq labelId)
            select(table.fetch(Fetchers.labelDefinition))
        }.execute().firstOrNull() ?: throw NotFoundException("Label definition not found")
    }

    private fun ensureProductCodeUnique(
        code: String,
        excludeId: Long?,
    ) {
        val exists = sql.exists(ProductDefinition::class) {
            where(table.code eq code)
            if (excludeId != null) {
                where(table.id ne excludeId)
            }
        }
        if (exists) {
            throw ConflictException("Product definition code already exists")
        }
    }

    private fun ensureDeviceDefinitionCodeUnique(
        productId: Long,
        code: String,
        excludeId: Long?,
    ) {
        val exists = sql.exists(DeviceDefinition::class) {
            where(table.product.id eq productId)
            where(table.code eq code)
            if (excludeId != null) {
                where(table.id ne excludeId)
            }
        }
        if (exists) {
            throw ConflictException("Device definition code already exists")
        }
    }

    private fun ensurePropertyIdentifierUnique(
        deviceDefinitionId: Long,
        identifier: String,
        excludeId: Long?,
    ) {
        val exists = sql.exists(PropertyDefinition::class) {
            where(table.deviceDefinition.id eq deviceDefinitionId)
            where(table.identifier eq identifier)
            if (excludeId != null) {
                where(table.id ne excludeId)
            }
        }
        if (exists) {
            throw ConflictException("Property identifier already exists")
        }
    }

    private fun ensureFeatureIdentifierUnique(
        deviceDefinitionId: Long,
        identifier: String,
        excludeId: Long?,
    ) {
        val exists = sql.exists(FeatureDefinition::class) {
            where(table.deviceDefinition.id eq deviceDefinitionId)
            where(table.identifier eq identifier)
            if (excludeId != null) {
                where(table.id ne excludeId)
            }
        }
        if (exists) {
            throw ConflictException("Feature identifier already exists")
        }
    }

    private fun ensureLabelCodeUnique(
        code: String,
        excludeId: Long?,
    ) {
        val exists = sql.exists(LabelDefinition::class) {
            where(table.code eq code)
            if (excludeId != null) {
                where(table.id ne excludeId)
            }
        }
        if (exists) {
            throw ConflictException("Label definition code already exists")
        }
    }

    private fun ensureProductExists(productId: Long) {
        val exists = sql.exists(ProductDefinition::class) {
            where(table.id eq productId)
        }
        if (!exists) {
            throw NotFoundException("Product definition not found")
        }
    }

    private fun ensureDeviceDefinitionExists(deviceDefinitionId: Long) {
        val exists = sql.exists(DeviceDefinition::class) {
            where(table.id eq deviceDefinitionId)
        }
        if (!exists) {
            throw NotFoundException("Device definition not found")
        }
    }

    private fun ensurePropertyDefinitionExists(propertyDefinitionId: Long) {
        val exists = sql.exists(PropertyDefinition::class) {
            where(table.id eq propertyDefinitionId)
        }
        if (!exists) {
            throw NotFoundException("Property definition not found")
        }
    }

    private fun ensureFeatureDefinitionExists(featureDefinitionId: Long) {
        val exists = sql.exists(FeatureDefinition::class) {
            where(table.id eq featureDefinitionId)
        }
        if (!exists) {
            throw NotFoundException("Feature definition not found")
        }
    }

    private fun ensureLabelExists(labelId: Long) {
        val exists = sql.exists(LabelDefinition::class) {
            where(table.id eq labelId)
        }
        if (!exists) {
            throw NotFoundException("Label definition not found")
        }
    }

    private fun ensureLabelIdsExist(labelIds: List<Long>) {
        labelIds.distinct().forEach(::ensureLabelExists)
    }

    private fun ensureDeviceTypeExists(deviceTypeId: Long) {
        val exists = sql.exists(DeviceType::class) {
            where(table.id eq deviceTypeId)
        }
        if (!exists) {
            throw NotFoundException("Device type not found")
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

    private fun ProductDefinition.toTreeResponse(): ProductDefinitionTreeResponse {
        return ProductDefinitionTreeResponse(
            id = id,
            code = code,
            name = name,
            description = description,
            vendor = vendor,
            category = category,
            enabled = enabled,
            sortIndex = sortIndex,
            labels = labelLinks
                .sortedWith(compareBy(ProductDefinitionLabelLink::sortIndex, ProductDefinitionLabelLink::id))
                .map { it.label.toResponse() },
            devices = devices
                .sortedWith(compareBy(DeviceDefinition::sortIndex, DeviceDefinition::id))
                .map { it.toTreeResponse() },
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun DeviceDefinition.toTreeResponse(): DeviceDefinitionTreeResponse {
        return DeviceDefinitionTreeResponse(
            id = id,
            productId = product.id,
            code = code,
            name = name,
            description = description,
            deviceTypeId = deviceType?.id,
            deviceTypeCode = deviceType?.code,
            deviceTypeName = deviceType?.name,
            supportsTelemetry = supportsTelemetry,
            supportsControl = supportsControl,
            sortIndex = sortIndex,
            properties = properties
                .sortedWith(compareBy(PropertyDefinition::sortIndex, PropertyDefinition::id))
                .map { it.toResponse() },
            features = features
                .sortedWith(compareBy(FeatureDefinition::sortIndex, FeatureDefinition::id))
                .map { it.toResponse() },
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun PropertyDefinition.toResponse(): PropertyDefinitionResponse {
        return PropertyDefinitionResponse(
            id = id,
            deviceDefinitionId = deviceDefinition.id,
            identifier = identifier,
            name = name,
            description = description,
            dataTypeId = dataType.id,
            dataTypeCode = dataType.code,
            dataTypeName = dataType.name,
            unit = unit,
            required = required,
            writable = writable,
            telemetry = telemetry,
            nullable = nullable,
            length = length,
            attributes = decodeAttributes(attributesJson),
            sortIndex = sortIndex,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun FeatureDefinition.toResponse(): FeatureDefinitionResponse {
        return FeatureDefinitionResponse(
            id = id,
            deviceDefinitionId = deviceDefinition.id,
            identifier = identifier,
            name = name,
            description = description,
            inputSchema = inputSchema,
            outputSchema = outputSchema,
            asynchronous = asynchronous,
            sortIndex = sortIndex,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun LabelDefinition.toResponse(): LabelDefinitionResponse {
        return LabelDefinitionResponse(
            id = id,
            code = code,
            name = name,
            description = description,
            colorHex = colorHex,
            sortIndex = sortIndex,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun encodeAttributes(
        attributes: Map<String, String>,
    ): String? {
        val cleaned = attributes.entries
            .mapNotNull { (key, value) ->
                val cleanKey = key.trim()
                val cleanValue = value.trim()
                if (cleanKey.isBlank() || cleanValue.isBlank()) {
                    null
                } else {
                    cleanKey to cleanValue
                }
            }
            .toMap(linkedMapOf())
        if (cleaned.isEmpty()) {
            return null
        }
        return json.encodeToString(cleaned)
    }

    private fun decodeAttributes(
        attributesJson: String?,
    ): Map<String, String> {
        if (attributesJson.isNullOrBlank()) {
            return emptyMap()
        }
        return runCatching {
            json.decodeFromString<Map<String, String>>(attributesJson)
        }.getOrElse {
            emptyMap()
        }
    }

    private fun String.toIotValueType(): IotValueType {
        return when (uppercase()) {
            "BOOLEAN" -> IotValueType.BOOLEAN
            "FLOAT", "DOUBLE" -> IotValueType.FLOAT32
            else -> IotValueType.INT32
        }
    }
}

private fun now(): Long = System.currentTimeMillis()

private fun String?.cleanNullable(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
