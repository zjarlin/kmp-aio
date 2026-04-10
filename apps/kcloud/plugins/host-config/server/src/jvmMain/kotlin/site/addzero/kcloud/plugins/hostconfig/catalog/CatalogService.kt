package site.addzero.kcloud.plugins.hostconfig.catalog

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
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
import site.addzero.kcloud.plugins.hostconfig.catalog.model.entity.AssetNodeLabelLink
import site.addzero.kcloud.plugins.hostconfig.model.entity.DataType
import site.addzero.kcloud.plugins.hostconfig.model.entity.DeviceType
import site.addzero.kcloud.plugins.hostconfig.model.entity.ProtocolTemplate
import site.addzero.kcloud.plugins.hostconfig.model.enums.AssetNodeType
import site.addzero.kcloud.plugins.hostconfig.service.Fetchers
import site.addzero.kmp.exp.ConflictException
import site.addzero.kmp.exp.NotFoundException

@Single
/**
 * 提供统一资产树目录服务。
 *
 * 这里不再依赖“产品定义 / 设备定义 / 属性定义 / 功能定义”多表树结构，
 * 而是统一落到 `host_config_asset_node` 这棵树里。
 * 对外接口先保持兼容，避免前端和 controller2api 一次性大爆炸。
 *
 * @property sql Jimmer SQL 客户端。
 */
class CatalogService(
    private val sql: KSqlClient,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * 获取快照。
     */
    fun getSnapshot(): CatalogSnapshotResponse {
        val graph = loadCatalogGraph()
        val labels = graph.labelNodes().map { it.toLabelResponse() }
        return CatalogSnapshotResponse(
            products = graph.rootAssets().map { product -> product.toProductResponse(graph) },
            labels = labels,
            metadata = buildMetadata(labels),
        )
    }

    /**
     * 获取 metadata。
     */
    fun getMetadata(): CatalogMetadataResponse {
        return buildMetadata(listLabels())
    }

    /**
     * 列出标签。
     */
    fun listLabels(): List<LabelDefinitionResponse> {
        return loadCatalogGraph()
            .labelNodes()
            .map { label -> label.toLabelResponse() }
    }

    /**
     * 创建标签。
     *
     * @param request 请求参数。
     */
    fun createLabel(request: LabelDefinitionCreateRequest): LabelDefinitionResponse {
        val code = request.code.trim()
        ensureLabelCodeUnique(code, excludeId = null)
        val now = now()
        val entity = AssetNode {
            nodeType = AssetNodeType.LABEL
            this.code = code
            this.name = request.name.trim()
            this.description = request.description.cleanNullable()
            this.colorHex = request.colorHex.cleanNullable()
            this.enabled = true
            this.sortIndex = request.sortIndex
            this.required = false
            this.writable = false
            this.telemetry = false
            this.nullable = true
            this.supportsTelemetry = true
            this.supportsControl = false
            this.asynchronous = false
            this.createdAt = now
            this.updatedAt = now
        }
        val label = sql.saveCommand(entity) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        return loadLabelNode(label.id).toLabelResponse()
    }

    /**
     * 更新标签。
     *
     * @param labelId 标签 ID。
     * @param request 请求参数。
     */
    fun updateLabel(
        labelId: Long,
        request: LabelDefinitionUpdateRequest,
    ): LabelDefinitionResponse {
        val current = loadLabelNode(labelId)
        val code = request.code.trim()
        ensureLabelCodeUnique(code, excludeId = labelId)
        val entity = AssetNode {
            id = labelId
            nodeType = current.nodeType
            this.code = code
            this.name = request.name.trim()
            this.description = request.description.cleanNullable()
            this.colorHex = request.colorHex.cleanNullable()
            this.enabled = current.enabled
            this.sortIndex = request.sortIndex
            this.vendor = current.vendor
            this.category = current.category
            this.identifier = current.identifier
            this.unit = current.unit
            this.required = current.required
            this.writable = current.writable
            this.telemetry = current.telemetry
            this.nullable = current.nullable
            this.length = current.length
            this.supportsTelemetry = current.supportsTelemetry
            this.supportsControl = current.supportsControl
            this.attributesJson = current.attributesJson
            this.inputSchema = current.inputSchema
            this.outputSchema = current.outputSchema
            this.asynchronous = current.asynchronous
            this.parentId = current.parent?.id
            this.inheritFromId = current.inheritFrom?.id
            this.protocolTemplateId = current.protocolTemplate?.id
            this.deviceTypeId = current.deviceType?.id
            this.dataTypeId = current.dataType?.id
            this.createdAt = current.createdAt
            this.updatedAt = now()
        }
        sql.saveCommand(entity) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute()
        return loadLabelNode(labelId).toLabelResponse()
    }

    /**
     * 删除标签。
     *
     * @param labelId 标签 ID。
     */
    fun deleteLabel(labelId: Long) {
        loadLabelNode(labelId)
        sql.createDelete(AssetNode::class) {
            where(table.id eq labelId)
        }.execute()
    }

    /**
     * 创建产品。
     *
     * @param request 请求参数。
     */
    fun createProduct(request: ProductDefinitionCreateRequest): ProductDefinitionTreeResponse {
        val code = request.code.trim()
        ensureProductCodeUnique(code, excludeId = null)
        ensureLabelIdsExist(request.labelIds)
        request.protocolTemplateId?.let(::ensureProtocolTemplateExists)
        val now = now()
        val entity = AssetNode {
            nodeType = AssetNodeType.ASSET
            this.code = code
            this.name = request.name.trim()
            this.description = request.description.cleanNullable()
            this.vendor = request.vendor.cleanNullable()
            this.category = request.category.cleanNullable()
            this.enabled = request.enabled
            this.sortIndex = request.sortIndex
            this.protocolTemplateId = request.protocolTemplateId
            this.required = false
            this.writable = false
            this.telemetry = false
            this.nullable = true
            this.supportsTelemetry = true
            this.supportsControl = false
            this.asynchronous = false
            this.createdAt = now
            this.updatedAt = now
        }
        val product = sql.saveCommand(entity) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        replaceAssetLabels(product.id, request.labelIds)
        return loadProductResponse(product.id)
    }

    /**
     * 更新产品。
     *
     * @param productId 产品 ID。
     * @param request 请求参数。
     */
    fun updateProduct(
        productId: Long,
        request: ProductDefinitionUpdateRequest,
    ): ProductDefinitionTreeResponse {
        val current = loadProductNode(productId)
        val code = request.code.trim()
        ensureProductCodeUnique(code, excludeId = productId)
        ensureLabelIdsExist(request.labelIds)
        request.protocolTemplateId?.let(::ensureProtocolTemplateExists)
        val entity = AssetNode {
            id = productId
            nodeType = current.nodeType
            this.code = code
            this.name = request.name.trim()
            this.description = request.description.cleanNullable()
            this.vendor = request.vendor.cleanNullable()
            this.category = request.category.cleanNullable()
            this.enabled = request.enabled
            this.sortIndex = request.sortIndex
            this.protocolTemplateId = request.protocolTemplateId
            this.required = current.required
            this.writable = current.writable
            this.telemetry = current.telemetry
            this.nullable = current.nullable
            this.length = current.length
            this.supportsTelemetry = current.supportsTelemetry
            this.supportsControl = current.supportsControl
            this.attributesJson = current.attributesJson
            this.inputSchema = current.inputSchema
            this.outputSchema = current.outputSchema
            this.asynchronous = current.asynchronous
            this.colorHex = current.colorHex
            this.identifier = current.identifier
            this.unit = current.unit
            this.parentId = null
            this.inheritFromId = current.inheritFrom?.id
            this.deviceTypeId = current.deviceType?.id
            this.dataTypeId = current.dataType?.id
            this.createdAt = current.createdAt
            this.updatedAt = now()
        }
        sql.saveCommand(entity) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute()
        replaceAssetLabels(productId, request.labelIds)
        return loadProductResponse(productId)
    }

    /**
     * 删除产品。
     *
     * @param productId 产品 ID。
     */
    fun deleteProduct(productId: Long) {
        loadProductNode(productId)
        sql.createDelete(AssetNode::class) {
            where(table.id eq productId)
        }.execute()
    }

    /**
     * 创建设备定义。
     *
     * @param productId 产品 ID。
     * @param request 请求参数。
     */
    fun createDeviceDefinition(
        productId: Long,
        request: DeviceDefinitionCreateRequest,
    ): DeviceDefinitionTreeResponse {
        loadProductNode(productId)
        request.deviceTypeId?.let(::ensureDeviceTypeExists)
        val code = request.code.trim()
        ensureDeviceDefinitionCodeUnique(productId, code, excludeId = null)
        val now = now()
        val entity = AssetNode {
            nodeType = AssetNodeType.ASSET
            parentId = productId
            this.code = code
            this.name = request.name.trim()
            this.description = request.description.cleanNullable()
            this.enabled = true
            this.sortIndex = request.sortIndex
            this.deviceTypeId = request.deviceTypeId
            this.required = false
            this.writable = false
            this.telemetry = false
            this.nullable = true
            this.supportsTelemetry = request.supportsTelemetry
            this.supportsControl = request.supportsControl
            this.asynchronous = false
            this.createdAt = now
            this.updatedAt = now
        }
        val device = sql.saveCommand(entity) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        return loadDeviceResponse(device.id)
    }

    /**
     * 更新设备定义。
     *
     * @param deviceDefinitionId 设备定义 ID。
     * @param request 请求参数。
     */
    fun updateDeviceDefinition(
        deviceDefinitionId: Long,
        request: DeviceDefinitionUpdateRequest,
    ): DeviceDefinitionTreeResponse {
        val current = loadDeviceNode(deviceDefinitionId)
        val productId = current.parent?.id ?: throw NotFoundException("Device definition not found")
        request.deviceTypeId?.let(::ensureDeviceTypeExists)
        val code = request.code.trim()
        ensureDeviceDefinitionCodeUnique(productId, code, excludeId = deviceDefinitionId)
        val entity = AssetNode {
            id = deviceDefinitionId
            nodeType = current.nodeType
            parentId = productId
            this.code = code
            this.name = request.name.trim()
            this.description = request.description.cleanNullable()
            this.enabled = current.enabled
            this.sortIndex = request.sortIndex
            this.deviceTypeId = request.deviceTypeId
            this.required = current.required
            this.writable = current.writable
            this.telemetry = current.telemetry
            this.nullable = current.nullable
            this.length = current.length
            this.supportsTelemetry = request.supportsTelemetry
            this.supportsControl = request.supportsControl
            this.attributesJson = current.attributesJson
            this.inputSchema = current.inputSchema
            this.outputSchema = current.outputSchema
            this.asynchronous = current.asynchronous
            this.vendor = current.vendor
            this.category = current.category
            this.colorHex = current.colorHex
            this.identifier = current.identifier
            this.unit = current.unit
            this.inheritFromId = current.inheritFrom?.id
            this.protocolTemplateId = current.protocolTemplate?.id
            this.dataTypeId = current.dataType?.id
            this.createdAt = current.createdAt
            this.updatedAt = now()
        }
        sql.saveCommand(entity) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute()
        return loadDeviceResponse(deviceDefinitionId)
    }

    /**
     * 删除设备定义。
     *
     * @param deviceDefinitionId 设备定义 ID。
     */
    fun deleteDeviceDefinition(deviceDefinitionId: Long) {
        loadDeviceNode(deviceDefinitionId)
        sql.createDelete(AssetNode::class) {
            where(table.id eq deviceDefinitionId)
        }.execute()
    }

    /**
     * 创建属性定义。
     *
     * @param deviceDefinitionId 设备定义 ID。
     * @param request 请求参数。
     */
    fun createPropertyDefinition(
        deviceDefinitionId: Long,
        request: PropertyDefinitionCreateRequest,
    ): PropertyDefinitionResponse {
        loadDeviceNode(deviceDefinitionId)
        ensureDataTypeExists(request.dataTypeId)
        val identifier = request.identifier.trim()
        ensurePropertyIdentifierUnique(deviceDefinitionId, identifier, excludeId = null)
        val now = now()
        val entity = AssetNode {
            nodeType = AssetNodeType.PROPERTY
            parentId = deviceDefinitionId
            code = identifier
            this.name = request.name.trim()
            this.description = request.description.cleanNullable()
            this.identifier = identifier
            this.dataTypeId = request.dataTypeId
            this.unit = request.unit.cleanNullable()
            this.enabled = true
            this.sortIndex = request.sortIndex
            this.required = request.required
            this.writable = request.writable
            this.telemetry = request.telemetry
            this.nullable = request.nullable
            this.length = request.length
            this.supportsTelemetry = true
            this.supportsControl = false
            this.attributesJson = encodeAttributes(request.attributes)
            this.asynchronous = false
            this.createdAt = now
            this.updatedAt = now
        }
        val property = sql.saveCommand(entity) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        return loadPropertyNode(property.id).toPropertyResponse()
    }

    /**
     * 更新属性定义。
     *
     * @param propertyDefinitionId 属性定义 ID。
     * @param request 请求参数。
     */
    fun updatePropertyDefinition(
        propertyDefinitionId: Long,
        request: PropertyDefinitionUpdateRequest,
    ): PropertyDefinitionResponse {
        val current = loadPropertyNode(propertyDefinitionId)
        val deviceDefinitionId = current.parent?.id ?: throw NotFoundException("Property definition not found")
        ensureDataTypeExists(request.dataTypeId)
        val identifier = request.identifier.trim()
        ensurePropertyIdentifierUnique(deviceDefinitionId, identifier, excludeId = propertyDefinitionId)
        val entity = AssetNode {
            id = propertyDefinitionId
            nodeType = current.nodeType
            parentId = deviceDefinitionId
            code = identifier
            this.name = request.name.trim()
            this.description = request.description.cleanNullable()
            this.identifier = identifier
            this.dataTypeId = request.dataTypeId
            this.unit = request.unit.cleanNullable()
            this.enabled = current.enabled
            this.sortIndex = request.sortIndex
            this.required = request.required
            this.writable = request.writable
            this.telemetry = request.telemetry
            this.nullable = request.nullable
            this.length = request.length
            this.supportsTelemetry = current.supportsTelemetry
            this.supportsControl = current.supportsControl
            this.attributesJson = encodeAttributes(request.attributes)
            this.asynchronous = current.asynchronous
            this.vendor = current.vendor
            this.category = current.category
            this.colorHex = current.colorHex
            this.inputSchema = current.inputSchema
            this.outputSchema = current.outputSchema
            this.inheritFromId = current.inheritFrom?.id
            this.protocolTemplateId = current.protocolTemplate?.id
            this.deviceTypeId = current.deviceType?.id
            this.createdAt = current.createdAt
            this.updatedAt = now()
        }
        sql.saveCommand(entity) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute()
        return loadPropertyNode(propertyDefinitionId).toPropertyResponse()
    }

    /**
     * 删除属性定义。
     *
     * @param propertyDefinitionId 属性定义 ID。
     */
    fun deletePropertyDefinition(propertyDefinitionId: Long) {
        loadPropertyNode(propertyDefinitionId)
        sql.createDelete(AssetNode::class) {
            where(table.id eq propertyDefinitionId)
        }.execute()
    }

    /**
     * 创建功能定义。
     *
     * @param deviceDefinitionId 设备定义 ID。
     * @param request 请求参数。
     */
    fun createFeatureDefinition(
        deviceDefinitionId: Long,
        request: FeatureDefinitionCreateRequest,
    ): FeatureDefinitionResponse {
        loadDeviceNode(deviceDefinitionId)
        val identifier = request.identifier.trim()
        ensureFeatureIdentifierUnique(deviceDefinitionId, identifier, excludeId = null)
        val now = now()
        val entity = AssetNode {
            nodeType = AssetNodeType.SERVICE
            parentId = deviceDefinitionId
            code = identifier
            this.name = request.name.trim()
            this.description = request.description.cleanNullable()
            this.identifier = identifier
            this.enabled = true
            this.sortIndex = request.sortIndex
            this.required = false
            this.writable = false
            this.telemetry = false
            this.nullable = true
            this.supportsTelemetry = true
            this.supportsControl = false
            this.inputSchema = request.inputSchema.cleanNullable()
            this.outputSchema = request.outputSchema.cleanNullable()
            this.asynchronous = request.asynchronous
            this.createdAt = now
            this.updatedAt = now
        }
        val feature = sql.saveCommand(entity) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute().modifiedEntity
        return loadFeatureNode(feature.id).toFeatureResponse()
    }

    /**
     * 更新功能定义。
     *
     * @param featureDefinitionId 功能定义 ID。
     * @param request 请求参数。
     */
    fun updateFeatureDefinition(
        featureDefinitionId: Long,
        request: FeatureDefinitionUpdateRequest,
    ): FeatureDefinitionResponse {
        val current = loadFeatureNode(featureDefinitionId)
        val deviceDefinitionId = current.parent?.id ?: throw NotFoundException("Feature definition not found")
        val identifier = request.identifier.trim()
        ensureFeatureIdentifierUnique(deviceDefinitionId, identifier, excludeId = featureDefinitionId)
        val entity = AssetNode {
            id = featureDefinitionId
            nodeType = current.nodeType
            parentId = deviceDefinitionId
            code = identifier
            this.name = request.name.trim()
            this.description = request.description.cleanNullable()
            this.identifier = identifier
            this.enabled = current.enabled
            this.sortIndex = request.sortIndex
            this.required = current.required
            this.writable = current.writable
            this.telemetry = current.telemetry
            this.nullable = current.nullable
            this.length = current.length
            this.supportsTelemetry = current.supportsTelemetry
            this.supportsControl = current.supportsControl
            this.attributesJson = current.attributesJson
            this.inputSchema = request.inputSchema.cleanNullable()
            this.outputSchema = request.outputSchema.cleanNullable()
            this.asynchronous = request.asynchronous
            this.vendor = current.vendor
            this.category = current.category
            this.colorHex = current.colorHex
            this.unit = current.unit
            this.inheritFromId = current.inheritFrom?.id
            this.protocolTemplateId = current.protocolTemplate?.id
            this.deviceTypeId = current.deviceType?.id
            this.dataTypeId = current.dataType?.id
            this.createdAt = current.createdAt
            this.updatedAt = now()
        }
        sql.saveCommand(entity) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute()
        return loadFeatureNode(featureDefinitionId).toFeatureResponse()
    }

    /**
     * 删除功能定义。
     *
     * @param featureDefinitionId 功能定义 ID。
     */
    fun deleteFeatureDefinition(featureDefinitionId: Long) {
        loadFeatureNode(featureDefinitionId)
        sql.createDelete(AssetNode::class) {
            where(table.id eq featureDefinitionId)
        }.execute()
    }

    /**
     * 列出 spec-iot 属性。
     *
     * @param deviceDefinitionId 设备定义 ID。
     */
    fun listSpecIotProperties(deviceDefinitionId: Long): List<SpecIotPropertyResponse> {
        val device = loadDeviceNode(deviceDefinitionId)
        val graph = loadCatalogGraph()
        return graph.propertyNodes(device.id).map { property ->
            val dataType = property.dataType ?: throw NotFoundException("Data type not found")
            val spec = IotPropertySpec.builder()
                .identifier(property.identifier ?: property.code)
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

    /**
     * 构建 metadata。
     *
     * @param labels 标签定义。
     */
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
                    title = "资产模型",
                    subtitle = "统一资产树里的根节点，用来承载型号、协议模板、标签和设备定义。",
                    formFields = listOf(
                        CatalogFieldMetadataResponse("code", "模型编码", CatalogFieldWidgetType.TEXT, required = true),
                        CatalogFieldMetadataResponse("name", "模型名称", CatalogFieldWidgetType.TEXT, required = true),
                        CatalogFieldMetadataResponse("description", "模型描述", CatalogFieldWidgetType.TEXTAREA),
                        CatalogFieldMetadataResponse("vendor", "供应商", CatalogFieldWidgetType.TEXT),
                        CatalogFieldMetadataResponse("category", "模型分类", CatalogFieldWidgetType.TEXT),
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
                        CatalogDetailFieldMetadataResponse("code", "模型编码", CatalogValueRenderType.CODE),
                        CatalogDetailFieldMetadataResponse("name", "模型名称"),
                        CatalogDetailFieldMetadataResponse("vendor", "供应商"),
                        CatalogDetailFieldMetadataResponse("category", "模型分类"),
                        CatalogDetailFieldMetadataResponse("protocolTemplateName", "协议模板"),
                        CatalogDetailFieldMetadataResponse("enabled", "启用状态", CatalogValueRenderType.BOOLEAN),
                        CatalogDetailFieldMetadataResponse("labels", "标签", CatalogValueRenderType.TAGS),
                        CatalogDetailFieldMetadataResponse("updatedAt", "最近更新", CatalogValueRenderType.DATETIME),
                    ),
                ),
                CatalogEntityMetadataResponse(
                    entityType = CatalogEntityType.DEVICE,
                    title = "设备定义",
                    subtitle = "资产模型下的设备类节点，绑定设备类型与能力特征。",
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
                    subtitle = "资产树里的属性节点，描述遥测、状态与控制字段。",
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
                    subtitle = "资产树里的功能节点，描述可调用动作及输入输出结构。",
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
                    subtitle = "统一资产树里的标签节点，可关联到任意资产模型。",
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

    /**
     * 加载统一目录图。
     */
    private fun loadCatalogGraph(): CatalogGraph {
        val nodes = sql.createQuery(AssetNode::class) {
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.assetNodeDetail))
        }.execute()
        val labelLinks = sql.createQuery(AssetNodeLabelLink::class) {
            orderBy(table.sortIndex.asc(), table.id.asc())
            select(table.fetch(Fetchers.assetNodeLabelLinkDetail))
        }.execute()
        return CatalogGraph(nodes, labelLinks)
    }

    /**
     * 替换资产标签。
     *
     * @param assetId 资产节点 ID。
     * @param labelIds 标签节点 ID 列表。
     */
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

    /**
     * 加载产品响应。
     *
     * @param productId 产品节点 ID。
     */
    private fun loadProductResponse(productId: Long): ProductDefinitionTreeResponse {
        val graph = loadCatalogGraph()
        val product = graph.requireNode(productId)
        ensureProductNode(product)
        return product.toProductResponse(graph)
    }

    /**
     * 加载设备响应。
     *
     * @param deviceDefinitionId 设备节点 ID。
     */
    private fun loadDeviceResponse(deviceDefinitionId: Long): DeviceDefinitionTreeResponse {
        val graph = loadCatalogGraph()
        val device = graph.requireNode(deviceDefinitionId)
        ensureDeviceNode(device)
        return device.toDeviceResponse(graph)
    }

    /**
     * 加载产品节点。
     *
     * @param productId 产品节点 ID。
     */
    private fun loadProductNode(productId: Long): AssetNode {
        val product = loadAssetNode(productId)
        ensureProductNode(product)
        return product
    }

    /**
     * 加载设备节点。
     *
     * @param deviceDefinitionId 设备节点 ID。
     */
    private fun loadDeviceNode(deviceDefinitionId: Long): AssetNode {
        val device = loadAssetNode(deviceDefinitionId)
        ensureDeviceNode(device)
        return device
    }

    /**
     * 加载属性节点。
     *
     * @param propertyDefinitionId 属性节点 ID。
     */
    private fun loadPropertyNode(propertyDefinitionId: Long): AssetNode {
        val property = loadAssetNode(propertyDefinitionId)
        ensurePropertyNode(property)
        return property
    }

    /**
     * 加载功能节点。
     *
     * @param featureDefinitionId 功能节点 ID。
     */
    private fun loadFeatureNode(featureDefinitionId: Long): AssetNode {
        val feature = loadAssetNode(featureDefinitionId)
        ensureServiceNode(feature)
        return feature
    }

    /**
     * 加载标签节点。
     *
     * @param labelId 标签节点 ID。
     */
    private fun loadLabelNode(labelId: Long): AssetNode {
        val label = loadAssetNode(labelId)
        ensureLabelNode(label)
        return label
    }

    /**
     * 加载资产节点。
     *
     * @param nodeId 节点 ID。
     */
    private fun loadAssetNode(nodeId: Long): AssetNode {
        return sql.createQuery(AssetNode::class) {
            where(table.id eq nodeId)
            select(table.fetch(Fetchers.assetNodeDetail))
        }.execute().firstOrNull() ?: throw NotFoundException("Asset node not found")
    }

    /**
     * 确保产品编码唯一。
     *
     * @param code 编码。
     * @param excludeId 需要排除的节点 ID。
     */
    private fun ensureProductCodeUnique(
        code: String,
        excludeId: Long?,
    ) {
        val exists = loadCatalogGraph()
            .rootAssets()
            .any { asset -> asset.code == code && asset.id != excludeId }
        if (exists) {
            throw ConflictException("Product definition code already exists")
        }
    }

    /**
     * 确保设备定义编码唯一。
     *
     * @param productId 产品节点 ID。
     * @param code 编码。
     * @param excludeId 需要排除的节点 ID。
     */
    private fun ensureDeviceDefinitionCodeUnique(
        productId: Long,
        code: String,
        excludeId: Long?,
    ) {
        val exists = loadCatalogGraph()
            .childAssetNodes(productId)
            .any { asset -> asset.code == code && asset.id != excludeId }
        if (exists) {
            throw ConflictException("Device definition code already exists")
        }
    }

    /**
     * 确保属性标识唯一。
     *
     * @param deviceDefinitionId 设备节点 ID。
     * @param identifier 标识。
     * @param excludeId 需要排除的节点 ID。
     */
    private fun ensurePropertyIdentifierUnique(
        deviceDefinitionId: Long,
        identifier: String,
        excludeId: Long?,
    ) {
        val exists = loadCatalogGraph()
            .propertyNodes(deviceDefinitionId)
            .any { property -> (property.identifier ?: property.code) == identifier && property.id != excludeId }
        if (exists) {
            throw ConflictException("Property identifier already exists")
        }
    }

    /**
     * 确保功能标识唯一。
     *
     * @param deviceDefinitionId 设备节点 ID。
     * @param identifier 标识。
     * @param excludeId 需要排除的节点 ID。
     */
    private fun ensureFeatureIdentifierUnique(
        deviceDefinitionId: Long,
        identifier: String,
        excludeId: Long?,
    ) {
        val exists = loadCatalogGraph()
            .serviceNodes(deviceDefinitionId)
            .any { service -> (service.identifier ?: service.code) == identifier && service.id != excludeId }
        if (exists) {
            throw ConflictException("Feature identifier already exists")
        }
    }

    /**
     * 确保标签编码唯一。
     *
     * @param code 编码。
     * @param excludeId 需要排除的节点 ID。
     */
    private fun ensureLabelCodeUnique(
        code: String,
        excludeId: Long?,
    ) {
        val exists = loadCatalogGraph()
            .labelNodes()
            .any { label -> label.code == code && label.id != excludeId }
        if (exists) {
            throw ConflictException("Label definition code already exists")
        }
    }

    /**
     * 确保标签节点都存在。
     *
     * @param labelIds 标签节点 ID 列表。
     */
    private fun ensureLabelIdsExist(labelIds: List<Long>) {
        labelIds.distinct().forEach { labelId ->
            loadLabelNode(labelId)
        }
    }

    /**
     * 确保设备类型存在。
     *
     * @param deviceTypeId 设备类型 ID。
     */
    private fun ensureDeviceTypeExists(deviceTypeId: Long) {
        val exists = sql.exists(DeviceType::class) {
            where(table.id eq deviceTypeId)
        }
        if (!exists) {
            throw NotFoundException("Device type not found")
        }
    }

    /**
     * 确保数据类型存在。
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
     * 确保协议模板存在。
     *
     * @param protocolTemplateId 协议模板 ID。
     */
    private fun ensureProtocolTemplateExists(protocolTemplateId: Long) {
        val exists = sql.exists(ProtocolTemplate::class) {
            where(table.id eq protocolTemplateId)
        }
        if (!exists) {
            throw NotFoundException("Protocol template not found")
        }
    }

    /**
     * 确保节点是产品节点。
     *
     * @param node 节点。
     */
    private fun ensureProductNode(node: AssetNode) {
        if (node.nodeType != AssetNodeType.ASSET || node.parent != null) {
            throw NotFoundException("Product definition not found")
        }
    }

    /**
     * 确保节点是设备节点。
     *
     * @param node 节点。
     */
    private fun ensureDeviceNode(node: AssetNode) {
        if (node.nodeType != AssetNodeType.ASSET || node.parent == null) {
            throw NotFoundException("Device definition not found")
        }
    }

    /**
     * 确保节点是属性节点。
     *
     * @param node 节点。
     */
    private fun ensurePropertyNode(node: AssetNode) {
        if (node.nodeType != AssetNodeType.PROPERTY) {
            throw NotFoundException("Property definition not found")
        }
    }

    /**
     * 确保节点是功能节点。
     *
     * @param node 节点。
     */
    private fun ensureServiceNode(node: AssetNode) {
        if (node.nodeType != AssetNodeType.SERVICE) {
            throw NotFoundException("Feature definition not found")
        }
    }

    /**
     * 确保节点是标签节点。
     *
     * @param node 节点。
     */
    private fun ensureLabelNode(node: AssetNode) {
        if (node.nodeType != AssetNodeType.LABEL) {
            throw NotFoundException("Label definition not found")
        }
    }

    /**
     * 编码扩展属性。
     *
     * @param attributes 扩展属性。
     */
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

    /**
     * 解码扩展属性。
     *
     * @param attributesJson 扩展属性 JSON。
     */
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

    /**
     * 转成 IoT 属性值类型。
     */
    private fun String.toIotValueType(): IotValueType {
        return when (uppercase()) {
            "BOOLEAN" -> IotValueType.BOOLEAN
            "FLOAT", "DOUBLE" -> IotValueType.FLOAT32
            else -> IotValueType.INT32
        }
    }

    /**
     * 目录图。
     */
    private data class CatalogGraph(
        val nodes: List<AssetNode>,
        val labelLinks: List<AssetNodeLabelLink>,
    ) {
        private val nodesById = nodes.associateBy { node -> node.id }
        private val childrenByParentId = nodes.groupBy { node -> node.parent?.id }
        private val labelLinksByAssetId = labelLinks.groupBy { link -> link.asset.id }

        fun requireNode(nodeId: Long): AssetNode {
            return nodesById[nodeId] ?: throw NotFoundException("Asset node not found")
        }

        fun rootAssets(): List<AssetNode> {
            return childrenByParentId[null].orEmpty()
                .filter { node -> node.nodeType == AssetNodeType.ASSET }
                .sortedByCatalogOrder()
        }

        fun labelNodes(): List<AssetNode> {
            return childrenByParentId[null].orEmpty()
                .filter { node -> node.nodeType == AssetNodeType.LABEL }
                .sortedByCatalogOrder()
        }

        fun childAssetNodes(parentId: Long): List<AssetNode> {
            return childrenByParentId[parentId].orEmpty()
                .filter { node -> node.nodeType == AssetNodeType.ASSET }
                .sortedByCatalogOrder()
        }

        fun propertyNodes(parentId: Long): List<AssetNode> {
            return childrenByParentId[parentId].orEmpty()
                .filter { node -> node.nodeType == AssetNodeType.PROPERTY }
                .sortedByCatalogOrder()
        }

        fun serviceNodes(parentId: Long): List<AssetNode> {
            return childrenByParentId[parentId].orEmpty()
                .filter { node -> node.nodeType == AssetNodeType.SERVICE }
                .sortedByCatalogOrder()
        }

        fun labelsOf(assetId: Long): List<AssetNode> {
            return labelLinksByAssetId[assetId].orEmpty()
                .sortedWith(compareBy(AssetNodeLabelLink::sortIndex, AssetNodeLabelLink::id))
                .map { link -> requireNode(link.label.id) }
        }
    }

    /**
     * 处理产品响应。
     */
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
            labels = graph.labelsOf(id).map { label -> label.toLabelResponse() },
            devices = graph.childAssetNodes(id).map { device -> device.toDeviceResponse(graph) },
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    /**
     * 处理设备响应。
     */
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
            properties = graph.propertyNodes(id).map { property -> property.toPropertyResponse() },
            features = graph.serviceNodes(id).map { service -> service.toFeatureResponse() },
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    /**
     * 处理属性响应。
     */
    private fun AssetNode.toPropertyResponse(): PropertyDefinitionResponse {
        val resolvedDataType = dataType ?: throw NotFoundException("Data type not found")
        return PropertyDefinitionResponse(
            id = id,
            deviceDefinitionId = parent?.id ?: 0L,
            identifier = identifier ?: code,
            name = name,
            description = description,
            dataTypeId = resolvedDataType.id,
            dataTypeCode = resolvedDataType.code,
            dataTypeName = resolvedDataType.name,
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

    /**
     * 处理功能响应。
     */
    private fun AssetNode.toFeatureResponse(): FeatureDefinitionResponse {
        return FeatureDefinitionResponse(
            id = id,
            deviceDefinitionId = parent?.id ?: 0L,
            identifier = identifier ?: code,
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

    /**
     * 处理标签响应。
     */
    private fun AssetNode.toLabelResponse(): LabelDefinitionResponse {
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

    /**
     * 按目录顺序排序。
     */
    private fun List<AssetNode>.sortedByCatalogOrder(): List<AssetNode> {
        return sortedWith(compareBy(AssetNode::sortIndex, AssetNode::id))
    }
}

/**
 * 获取当前时间戳。
 */
private fun now(): Long = System.currentTimeMillis()

/**
 * 清洗可空字符串。
 */
private fun String?.cleanNullable(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
