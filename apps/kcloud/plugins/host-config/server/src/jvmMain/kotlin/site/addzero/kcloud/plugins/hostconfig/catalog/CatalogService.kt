package site.addzero.kcloud.plugins.hostconfig.catalog

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
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
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.AssetNode
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.AssetNodeDraft
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.AssetNodeLabelLink
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.FeatureDefinition
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.FeatureDefinitionDraft
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.LabelDefinition
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.PropertyDefinition
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.PropertyDefinitionDraft
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.asset
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.id
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.node
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.nodeType
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.parent
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.sortIndex
import site.addzero.kcloud.plugins.hostconfig.model.entity.*
import site.addzero.kcloud.plugins.hostconfig.model.enums.AssetNodeType
import site.addzero.kcloud.plugins.hostconfig.service.Fetchers
import site.addzero.kmp.exp.ConflictException
import site.addzero.kmp.exp.NotFoundException

@Single
/**
 * 统一资产主树目录服务。
 *
 * `AssetNode` 只承载产品、设备、模块三类主节点；
 * 标签、属性、功能继续独立建模，并通过 `nodeId` 围绕主树关联。
 */
class CatalogService(
    private val sql: KSqlClient,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun getSnapshot(): CatalogSnapshotResponse {
        val labels = listLabels()
        val graph = loadCatalogGraph()
        return CatalogSnapshotResponse(
            products = graph.rootProducts().map { product -> product.toProductResponse(graph) },
            labels = labels,
            metadata = buildMetadata(labels),
        )
    }

    fun getMetadata(): CatalogMetadataResponse =
        buildMetadata(listLabels())

    fun listLabels(): List<LabelDefinitionResponse> {
        return sql.createQuery(LabelDefinition::class) {
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.labelDefinition))
        }.execute().map { label ->
            label.toLabelResponse()
        }
    }

    fun createLabel(request: LabelDefinitionCreateRequest): LabelDefinitionResponse {
        val code = request.code.trim()
        ensureLabelCodeUnique(code, excludeId = null)
        val now = now()
        val label = sql.saveCommand(
            LabelDefinition {
                this.code = code
                this.name = request.name.trim()
                this.description = request.description.cleanNullable()
                this.colorHex = request.colorHex.cleanNullable()
                this.sortIndex = request.sortIndex
                this.createdAt = now
                this.updatedAt = now
            },
        ) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        return loadLabel(label.id).toLabelResponse()
    }

    fun updateLabel(
        labelId: Long,
        request: LabelDefinitionUpdateRequest,
    ): LabelDefinitionResponse {
        val current = loadLabel(labelId)
        val code = request.code.trim()
        ensureLabelCodeUnique(code, excludeId = labelId)
        sql.saveCommand(
            LabelDefinition {
                id = labelId
                this.code = code
                this.name = request.name.trim()
                this.description = request.description.cleanNullable()
                this.colorHex = request.colorHex.cleanNullable()
                this.sortIndex = request.sortIndex
                this.createdAt = current.createdAt
                this.updatedAt = now()
            },
        ) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute()
        return loadLabel(labelId).toLabelResponse()
    }

    fun deleteLabel(labelId: Long) {
        loadLabel(labelId)
        sql.createDelete(LabelDefinition::class) {
            where(table.id eq labelId)
        }.execute()
    }

    fun createProduct(request: ProductDefinitionCreateRequest): ProductDefinitionTreeResponse {
        val code = request.code.trim()
        ensureProductCodeUnique(code, excludeId = null)
        ensureLabelIdsExist(request.labelIds)
        request.protocolTemplateId?.let(::ensureProtocolTemplateExists)
        val now = now()
        val product = sql.saveCommand(
            AssetNode {
                applyProductFields(
                    code = code,
                    name = request.name.trim(),
                    description = request.description.cleanNullable(),
                    enabled = request.enabled,
                    sortIndex = request.sortIndex,
                    vendor = request.vendor.cleanNullable(),
                    category = request.category.cleanNullable(),
                    protocolTemplateId = request.protocolTemplateId,
                )
                this.createdAt = now
                this.updatedAt = now
            },
        ) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        replaceAssetLabels(product.id, request.labelIds)
        return loadProductResponse(product.id)
    }

    fun updateProduct(
        productId: Long,
        request: ProductDefinitionUpdateRequest,
    ): ProductDefinitionTreeResponse {
        loadProductNode(productId)
        val code = request.code.trim()
        ensureProductCodeUnique(code, excludeId = productId)
        ensureLabelIdsExist(request.labelIds)
        request.protocolTemplateId?.let(::ensureProtocolTemplateExists)
        val current = loadAssetNode(productId)
        sql.saveCommand(
            AssetNode {
                id = productId
                applyProductFields(
                    code = code,
                    name = request.name.trim(),
                    description = request.description.cleanNullable(),
                    enabled = request.enabled,
                    sortIndex = request.sortIndex,
                    vendor = request.vendor.cleanNullable(),
                    category = request.category.cleanNullable(),
                    protocolTemplateId = request.protocolTemplateId,
                )
                this.createdAt = current.createdAt
                this.updatedAt = now()
            },
        ) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute()
        replaceAssetLabels(productId, request.labelIds)
        return loadProductResponse(productId)
    }

    fun deleteProduct(productId: Long) {
        loadProductNode(productId)
        sql.createDelete(AssetNode::class) {
            where(table.id eq productId)
        }.execute()
    }

    fun createDeviceDefinition(
        productId: Long,
        request: DeviceDefinitionCreateRequest,
    ): DeviceDefinitionTreeResponse {
        loadProductNode(productId)
        val code = request.code.trim()
        ensureDeviceDefinitionCodeUnique(productId, code, excludeId = null)
        request.deviceTypeId?.let(::ensureDeviceTypeExists)
        val now = now()
        val device = sql.saveCommand(
            AssetNode {
                parentId = productId
                nodeType = AssetNodeType.DEVICE
                this.code = code
                this.name = request.name.trim()
                this.description = request.description.cleanNullable()
                this.enabled = true
                this.sortIndex = request.sortIndex
                this.supportsTelemetry = request.supportsTelemetry
                this.supportsControl = request.supportsControl
                this.deviceTypeId = request.deviceTypeId
                this.createdAt = now
                this.updatedAt = now
            },
        ) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        return loadDeviceResponse(device.id)
    }

    fun updateDeviceDefinition(
        deviceDefinitionId: Long,
        request: DeviceDefinitionUpdateRequest,
    ): DeviceDefinitionTreeResponse {
        val current = loadDeviceNode(deviceDefinitionId)
        val productId = current.parent?.id ?: throw NotFoundException("Device definition not found")
        val code = request.code.trim()
        ensureDeviceDefinitionCodeUnique(productId, code, excludeId = deviceDefinitionId)
        request.deviceTypeId?.let(::ensureDeviceTypeExists)
        sql.saveCommand(
            AssetNode {
                id = deviceDefinitionId
                parentId = productId
                nodeType = AssetNodeType.DEVICE
                this.code = code
                this.name = request.name.trim()
                this.description = request.description.cleanNullable()
                this.enabled = current.enabled
                this.sortIndex = request.sortIndex
                this.vendor = current.vendor
                this.category = current.category
                this.supportsTelemetry = request.supportsTelemetry
                this.supportsControl = request.supportsControl
                this.protocolTemplateId = current.protocolTemplate?.id
                this.deviceTypeId = request.deviceTypeId
                this.moduleTemplateId = current.moduleTemplate?.id
                this.createdAt = current.createdAt
                this.updatedAt = now()
            },
        ) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute()
        return loadDeviceResponse(deviceDefinitionId)
    }

    fun deleteDeviceDefinition(deviceDefinitionId: Long) {
        loadDeviceNode(deviceDefinitionId)
        sql.createDelete(AssetNode::class) {
            where(table.id eq deviceDefinitionId)
        }.execute()
    }

    fun createPropertyDefinition(
        deviceDefinitionId: Long,
        request: PropertyDefinitionCreateRequest,
    ): PropertyDefinitionResponse {
        loadDeviceNode(deviceDefinitionId)
        ensureDataTypeExists(request.dataTypeId)
        val identifier = request.identifier.trim()
        ensurePropertyIdentifierUnique(deviceDefinitionId, identifier, excludeId = null)
        val now = now()
        val property = sql.saveCommand(
            PropertyDefinition {
                applyPropertyFields(
                    nodeId = deviceDefinitionId,
                    deviceDefinitionId = null,
                    identifier = identifier,
                    name = request.name.trim(),
                    description = request.description.cleanNullable(),
                    dataTypeId = request.dataTypeId,
                    unit = request.unit.cleanNullable(),
                    required = request.required,
                    writable = request.writable,
                    telemetry = request.telemetry,
                    nullable = request.nullable,
                    length = request.length,
                    attributesJson = encodeAttributes(request.attributes),
                    sortIndex = request.sortIndex,
                )
                this.createdAt = now
                this.updatedAt = now
            },
        ) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        return loadPropertyDefinition(property.id).toPropertyResponse()
    }

    fun updatePropertyDefinition(
        propertyDefinitionId: Long,
        request: PropertyDefinitionUpdateRequest,
    ): PropertyDefinitionResponse {
        val current = loadPropertyDefinition(propertyDefinitionId)
        val nodeId = current.node.id
        val identifier = request.identifier.trim()
        ensurePropertyIdentifierUnique(nodeId, identifier, excludeId = propertyDefinitionId)
        ensureDataTypeExists(request.dataTypeId)
        sql.saveCommand(
            PropertyDefinition {
                id = propertyDefinitionId
                applyPropertyFields(
                    nodeId = nodeId,
                    deviceDefinitionId = current.deviceDefinition?.id,
                    identifier = identifier,
                    name = request.name.trim(),
                    description = request.description.cleanNullable(),
                    dataTypeId = request.dataTypeId,
                    unit = request.unit.cleanNullable(),
                    required = request.required,
                    writable = request.writable,
                    telemetry = request.telemetry,
                    nullable = request.nullable,
                    length = request.length,
                    attributesJson = encodeAttributes(request.attributes),
                    sortIndex = request.sortIndex,
                )
                this.createdAt = current.createdAt
                this.updatedAt = now()
            },
        ) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute()
        return loadPropertyDefinition(propertyDefinitionId).toPropertyResponse()
    }

    fun deletePropertyDefinition(propertyDefinitionId: Long) {
        loadPropertyDefinition(propertyDefinitionId)
        sql.createDelete(PropertyDefinition::class) {
            where(table.id eq propertyDefinitionId)
        }.execute()
    }

    fun createFeatureDefinition(
        deviceDefinitionId: Long,
        request: FeatureDefinitionCreateRequest,
    ): FeatureDefinitionResponse {
        loadDeviceNode(deviceDefinitionId)
        val identifier = request.identifier.trim()
        ensureFeatureIdentifierUnique(deviceDefinitionId, identifier, excludeId = null)
        val now = now()
        val feature = sql.saveCommand(
            FeatureDefinition {
                applyFeatureFields(
                    nodeId = deviceDefinitionId,
                    deviceDefinitionId = null,
                    identifier = identifier,
                    name = request.name.trim(),
                    description = request.description.cleanNullable(),
                    inputSchema = request.inputSchema.cleanNullable(),
                    outputSchema = request.outputSchema.cleanNullable(),
                    asynchronous = request.asynchronous,
                    sortIndex = request.sortIndex,
                )
                this.createdAt = now
                this.updatedAt = now
            },
        ) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        return loadFeatureDefinition(feature.id).toFeatureResponse()
    }

    fun updateFeatureDefinition(
        featureDefinitionId: Long,
        request: FeatureDefinitionUpdateRequest,
    ): FeatureDefinitionResponse {
        val current = loadFeatureDefinition(featureDefinitionId)
        val nodeId = current.node.id
        val identifier = request.identifier.trim()
        ensureFeatureIdentifierUnique(nodeId, identifier, excludeId = featureDefinitionId)
        sql.saveCommand(
            FeatureDefinition {
                id = featureDefinitionId
                applyFeatureFields(
                    nodeId = nodeId,
                    deviceDefinitionId = current.deviceDefinition?.id,
                    identifier = identifier,
                    name = request.name.trim(),
                    description = request.description.cleanNullable(),
                    inputSchema = request.inputSchema.cleanNullable(),
                    outputSchema = request.outputSchema.cleanNullable(),
                    asynchronous = request.asynchronous,
                    sortIndex = request.sortIndex,
                )
                this.createdAt = current.createdAt
                this.updatedAt = now()
            },
        ) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute()
        return loadFeatureDefinition(featureDefinitionId).toFeatureResponse()
    }

    fun deleteFeatureDefinition(featureDefinitionId: Long) {
        loadFeatureDefinition(featureDefinitionId)
        sql.createDelete(FeatureDefinition::class) {
            where(table.id eq featureDefinitionId)
        }.execute()
    }

    fun listSpecIotProperties(deviceDefinitionId: Long): List<SpecIotPropertyResponse> {
        val device = loadDeviceNode(deviceDefinitionId)
        val graph = loadCatalogGraph()
        return graph.mergedProperties(device.id).map { property ->
            val dataType = property.dataType
            val spec = IotPropertySpec.builder()
                .identifier(property.identifier)
                .name(property.name)
                .description(property.description)
                .unit(property.unit)
                .valueType(dataType.code.toIotValueType())
                .length(property.length)
                .attribute("dataTypeCode", dataType.code)
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
        }.execute().map { item ->
            CatalogFieldOptionResponse(
                value = item.id.toString(),
                label = item.name,
                description = item.description,
            )
        }
        val dataTypeOptions = sql.createQuery(DataType::class) {
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.dataType))
        }.execute().map { item ->
            CatalogFieldOptionResponse(
                value = item.id.toString(),
                label = item.name,
                description = item.description,
            )
        }
        val protocolTemplateOptions = sql.createQuery(ProtocolTemplate::class) {
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.protocolTemplate))
        }.execute().map { item ->
            CatalogFieldOptionResponse(
                value = item.id.toString(),
                label = item.name,
                description = item.description,
            )
        }
        return CatalogMetadataResponse(
            entities = listOf(
                CatalogEntityMetadataResponse(
                    entityType = CatalogEntityType.PRODUCT,
                    title = "产品节点",
                    subtitle = "资产主树的根节点，承载产品级协议、标签和设备子节点。",
                    formFields = listOf(
                        CatalogFieldMetadataResponse("code", "产品编码", CatalogFieldWidgetType.TEXT, required = true),
                        CatalogFieldMetadataResponse("name", "产品名称", CatalogFieldWidgetType.TEXT, required = true),
                        CatalogFieldMetadataResponse("description", "产品描述", CatalogFieldWidgetType.TEXTAREA),
                        CatalogFieldMetadataResponse("vendor", "供应商", CatalogFieldWidgetType.TEXT),
                        CatalogFieldMetadataResponse("category", "产品分类", CatalogFieldWidgetType.TEXT),
                        CatalogFieldMetadataResponse(
                            key = "protocolTemplateId",
                            label = "协议模板",
                            widget = CatalogFieldWidgetType.SELECT,
                            optionSource = "protocolTemplates",
                        ),
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
                        CatalogDetailFieldMetadataResponse("protocolTemplateName", "协议模板"),
                        CatalogDetailFieldMetadataResponse("enabled", "启用状态", CatalogValueRenderType.BOOLEAN),
                        CatalogDetailFieldMetadataResponse("labels", "标签", CatalogValueRenderType.TAGS),
                        CatalogDetailFieldMetadataResponse("updatedAt", "最近更新", CatalogValueRenderType.DATETIME),
                    ),
                ),
                CatalogEntityMetadataResponse(
                    entityType = CatalogEntityType.DEVICE,
                    title = "设备节点",
                    subtitle = "产品节点下的设备主节点，负责挂接设备类型、属性和功能。",
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
                    subtitle = "属性定义独立存储，通过 nodeId 挂到主树节点，当前界面先挂到设备节点。",
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
                    subtitle = "功能定义独立存储，通过 nodeId 挂到主树节点，当前界面先挂到设备节点。",
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
                    subtitle = "独立标签字典，可关联到产品、设备或模块节点。",
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
                    options = labels.map { item ->
                        CatalogFieldOptionResponse(
                            value = item.id.toString(),
                            label = item.name,
                            description = item.description,
                        )
                    },
                ),
                CatalogOptionSetResponse(
                    key = "protocolTemplates",
                    options = protocolTemplateOptions,
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

    private fun loadCatalogGraph(): CatalogGraph {
        val nodes = sql.createQuery(AssetNode::class) {
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.assetNodeDetail))
        }.execute()
        val labelLinks = sql.createQuery(AssetNodeLabelLink::class) {
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.assetNodeLabelLinkDetail))
        }.execute()
        val properties = sql.createQuery(PropertyDefinition::class) {
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.propertyDefinitionDetail))
        }.execute()
        val features = sql.createQuery(FeatureDefinition::class) {
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.featureDefinitionDetail))
        }.execute()
        return CatalogGraph(
            nodes = nodes,
            labelLinks = labelLinks,
            properties = properties,
            features = features,
        )
    }

    private fun replaceAssetLabels(
        assetId: Long,
        labelIds: List<Long>,
    ) {
        sql.createDelete(AssetNodeLabelLink::class) {
            where(table.asset.id eq assetId)
        }.execute()
        val now = now()
        labelIds.distinct().forEachIndexed { index, labelId ->
            sql.saveCommand(
                AssetNodeLabelLink {
                    this.assetId = assetId
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

    private fun loadProductResponse(productId: Long): ProductDefinitionTreeResponse {
        val graph = loadCatalogGraph()
        val product = graph.requireNode(productId)
        ensureProductNode(product)
        return product.toProductResponse(graph)
    }

    private fun loadDeviceResponse(deviceDefinitionId: Long): DeviceDefinitionTreeResponse {
        val graph = loadCatalogGraph()
        val device = graph.requireNode(deviceDefinitionId)
        ensureDeviceNode(device)
        return device.toDeviceResponse(graph)
    }

    private fun loadProductNode(productId: Long): AssetNode {
        val product = loadAssetNode(productId)
        ensureProductNode(product)
        return product
    }

    private fun loadDeviceNode(deviceDefinitionId: Long): AssetNode {
        val device = loadAssetNode(deviceDefinitionId)
        ensureDeviceNode(device)
        return device
    }

    private fun loadAssetNode(nodeId: Long): AssetNode {
        return sql.createQuery(AssetNode::class) {
            where(table.id eq nodeId)
            select(table.fetch(Fetchers.assetNodeDetail))
        }.execute().firstOrNull() ?: throw NotFoundException("Asset node not found")
    }

    private fun loadLabel(labelId: Long): LabelDefinition {
        return sql.createQuery(LabelDefinition::class) {
            where(table.id eq labelId)
            select(table.fetch(Fetchers.labelDefinition))
        }.execute().firstOrNull() ?: throw NotFoundException("Label definition not found")
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

    private fun ensureProductCodeUnique(
        code: String,
        excludeId: Long?,
    ) {
        val exists = sql.createQuery(AssetNode::class) {
            where(table.nodeType eq AssetNodeType.PRODUCT)
            select(table.fetch(Fetchers.assetNodeDetail))
        }.execute().any { node ->
            node.code == code && node.id != excludeId
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
        val exists = sql.createQuery(AssetNode::class) {
            where(table.nodeType eq AssetNodeType.DEVICE)
            where(table.parent.id eq productId)
            select(table.fetch(Fetchers.assetNodeDetail))
        }.execute().any { node ->
            node.code == code && node.id != excludeId
        }
        if (exists) {
            throw ConflictException("Device definition code already exists")
        }
    }

    private fun ensurePropertyIdentifierUnique(
        nodeId: Long,
        identifier: String,
        excludeId: Long?,
    ) {
        val exists = sql.createQuery(PropertyDefinition::class) {
            where(table.node.id eq nodeId)
            select(table.fetch(Fetchers.propertyDefinitionDetail))
        }.execute().any { property ->
            property.identifier == identifier && property.id != excludeId
        }
        if (exists) {
            throw ConflictException("Property identifier already exists")
        }
    }

    private fun ensureFeatureIdentifierUnique(
        nodeId: Long,
        identifier: String,
        excludeId: Long?,
    ) {
        val exists = sql.createQuery(FeatureDefinition::class) {
            where(table.node.id eq nodeId)
            select(table.fetch(Fetchers.featureDefinitionDetail))
        }.execute().any { feature ->
            feature.identifier == identifier && feature.id != excludeId
        }
        if (exists) {
            throw ConflictException("Feature identifier already exists")
        }
    }

    private fun ensureLabelCodeUnique(
        code: String,
        excludeId: Long?,
    ) {
        val exists = sql.createQuery(LabelDefinition::class) {
            select(table.fetch(Fetchers.labelDefinition))
        }.execute().any { label ->
            label.code == code && label.id != excludeId
        }
        if (exists) {
            throw ConflictException("Label definition code already exists")
        }
    }

    private fun ensureLabelIdsExist(labelIds: List<Long>) {
        labelIds.distinct().forEach(::loadLabel)
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

    private fun ensureProtocolTemplateExists(protocolTemplateId: Long) {
        val exists = sql.exists(ProtocolTemplate::class) {
            where(table.id eq protocolTemplateId)
        }
        if (!exists) {
            throw NotFoundException("Protocol template not found")
        }
    }

    private fun ensureProductNode(node: AssetNode) {
        if (node.nodeType != AssetNodeType.PRODUCT || node.parent != null) {
            throw NotFoundException("Product definition not found")
        }
    }

    private fun ensureDeviceNode(node: AssetNode) {
        if (node.nodeType != AssetNodeType.DEVICE || node.parent == null) {
            throw NotFoundException("Device definition not found")
        }
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

    private data class CatalogGraph(
        val nodes: List<AssetNode>,
        val labelLinks: List<AssetNodeLabelLink>,
        val properties: List<PropertyDefinition>,
        val features: List<FeatureDefinition>,
    ) {
        private val nodesById = nodes.associateBy { node -> node.id }
        private val childrenByParentId = nodes.groupBy { node -> node.parent?.id }
        private val labelLinksByAssetId = labelLinks.groupBy { link -> link.asset.id }
        private val propertiesByNodeId = properties.groupBy { property -> property.node.id }
        private val featuresByNodeId = features.groupBy { feature -> feature.node.id }

        fun requireNode(nodeId: Long): AssetNode {
            return nodesById[nodeId] ?: throw NotFoundException("Asset node not found")
        }

        fun rootProducts(): List<AssetNode> {
            return childrenByParentId[null].orEmpty()
                .filter { node -> node.nodeType == AssetNodeType.PRODUCT }
                .sortedAssetNodesByCatalogOrder()
        }

        fun childDevices(parentId: Long): List<AssetNode> {
            return childrenByParentId[parentId].orEmpty()
                .filter { node -> node.nodeType == AssetNodeType.DEVICE }
                .sortedAssetNodesByCatalogOrder()
        }

        fun mergedLabels(nodeId: Long): List<LabelDefinition> {
            val merged = linkedMapOf<Long, LabelDefinition>()
            ancestorChain(nodeId).forEach { node ->
                labelLinksByAssetId[node.id].orEmpty()
                    .sortedWith(compareBy(AssetNodeLabelLink::sortIndex, AssetNodeLabelLink::id))
                    .forEach { link ->
                        if (merged.containsKey(link.label.id)) {
                            merged.remove(link.label.id)
                        }
                        merged[link.label.id] = link.label
                    }
            }
            return merged.values.toList()
        }

        fun mergedProperties(nodeId: Long): List<PropertyDefinition> {
            return mergeInheritedByKey(
                nodeId = nodeId,
                itemsByNodeId = propertiesByNodeId,
                sortItems = { sortedPropertiesByCatalogOrder() },
                keySelector = PropertyDefinition::identifier,
            )
        }

        fun mergedFeatures(nodeId: Long): List<FeatureDefinition> {
            return mergeInheritedByKey(
                nodeId = nodeId,
                itemsByNodeId = featuresByNodeId,
                sortItems = { sortedFeaturesByCatalogOrder() },
                keySelector = FeatureDefinition::identifier,
            )
        }

        private fun ancestorChain(nodeId: Long): List<AssetNode> {
            val chain = mutableListOf<AssetNode>()
            var current: AssetNode? = requireNode(nodeId)
            while (current != null) {
                chain += current
                current = current.parent?.let { parent -> requireNode(parent.id) }
            }
            return chain.asReversed()
        }

        private fun <T, K> mergeInheritedByKey(
            nodeId: Long,
            itemsByNodeId: Map<Long, List<T>>,
            sortItems: List<T>.() -> List<T>,
            keySelector: (T) -> K,
        ): List<T> {
            val merged = linkedMapOf<K, T>()
            ancestorChain(nodeId).forEach { node ->
                itemsByNodeId[node.id].orEmpty()
                    .sortItems()
                    .forEach { item ->
                        val key = keySelector(item)
                        if (merged.containsKey(key)) {
                            merged.remove(key)
                        }
                        merged[key] = item
                    }
            }
            return merged.values.toList()
        }
    }

    private fun AssetNode.toProductResponse(
        graph: CatalogGraph,
    ): ProductDefinitionTreeResponse {
        return ProductDefinitionTreeResponse(
            id = id,
            code = code,
            name = name,
            description = description,
            vendor = vendor,
            category = category,
            enabled = enabled,
            protocolTemplateId = protocolTemplate?.id,
            protocolTemplateCode = protocolTemplate?.code,
            protocolTemplateName = protocolTemplate?.name,
            sortIndex = sortIndex,
            labels = graph.mergedLabels(id).map { label -> label.toLabelResponse() },
            devices = graph.childDevices(id).map { device -> device.toDeviceResponse(graph) },
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun AssetNode.toDeviceResponse(
        graph: CatalogGraph,
    ): DeviceDefinitionTreeResponse {
        return DeviceDefinitionTreeResponse(
            id = id,
            productId = parent?.id ?: 0L,
            code = code,
            name = name,
            description = description,
            deviceTypeId = deviceType?.id,
            deviceTypeCode = deviceType?.code,
            deviceTypeName = deviceType?.name,
            supportsTelemetry = supportsTelemetry,
            supportsControl = supportsControl,
            sortIndex = sortIndex,
            properties = graph.mergedProperties(id).map { property -> property.toPropertyResponse() },
            features = graph.mergedFeatures(id).map { feature -> feature.toFeatureResponse() },
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun PropertyDefinition.toPropertyResponse(): PropertyDefinitionResponse {
        return PropertyDefinitionResponse(
            id = id,
            deviceDefinitionId = node.id,
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

    private fun FeatureDefinition.toFeatureResponse(): FeatureDefinitionResponse {
        return FeatureDefinitionResponse(
            id = id,
            deviceDefinitionId = node.id,
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

    private fun LabelDefinition.toLabelResponse(): LabelDefinitionResponse {
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
}

private fun List<AssetNode>.sortedAssetNodesByCatalogOrder(): List<AssetNode> =
    sortedWith(compareBy(AssetNode::sortIndex, AssetNode::id))

private fun List<PropertyDefinition>.sortedPropertiesByCatalogOrder(): List<PropertyDefinition> =
    sortedWith(compareBy(PropertyDefinition::sortIndex, PropertyDefinition::id))

private fun List<FeatureDefinition>.sortedFeaturesByCatalogOrder(): List<FeatureDefinition> =
    sortedWith(compareBy(FeatureDefinition::sortIndex, FeatureDefinition::id))

private fun AssetNodeDraft.applyProductFields(
    code: String,
    name: String,
    description: String?,
    enabled: Boolean,
    sortIndex: Int,
    vendor: String?,
    category: String?,
    protocolTemplateId: Long?,
) {
    nodeType = AssetNodeType.PRODUCT
    this.code = code
    this.name = name
    this.description = description
    this.enabled = enabled
    this.sortIndex = sortIndex
    this.vendor = vendor
    this.category = category
    supportsTelemetry = false
    supportsControl = false
    this.protocolTemplateId = protocolTemplateId
}

private fun PropertyDefinitionDraft.applyPropertyFields(
    nodeId: Long,
    deviceDefinitionId: Long?,
    identifier: String,
    name: String,
    description: String?,
    dataTypeId: Long,
    unit: String?,
    required: Boolean,
    writable: Boolean,
    telemetry: Boolean,
    nullable: Boolean,
    length: Int?,
    attributesJson: String?,
    sortIndex: Int,
) {
    this.deviceDefinitionId = deviceDefinitionId
    this.nodeId = nodeId
    this.identifier = identifier
    this.name = name
    this.description = description
    this.dataTypeId = dataTypeId
    this.unit = unit
    this.required = required
    this.writable = writable
    this.telemetry = telemetry
    this.nullable = nullable
    this.length = length
    this.attributesJson = attributesJson
    this.sortIndex = sortIndex
}

private fun FeatureDefinitionDraft.applyFeatureFields(
    nodeId: Long,
    deviceDefinitionId: Long?,
    identifier: String,
    name: String,
    description: String?,
    inputSchema: String?,
    outputSchema: String?,
    asynchronous: Boolean,
    sortIndex: Int,
) {
    this.deviceDefinitionId = deviceDefinitionId
    this.nodeId = nodeId
    this.identifier = identifier
    this.name = name
    this.description = description
    this.inputSchema = inputSchema
    this.outputSchema = outputSchema
    this.asynchronous = asynchronous
    this.sortIndex = sortIndex
}

private fun now(): Long = System.currentTimeMillis()

private fun String?.cleanNullable(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
