package site.addzero.kcloud.plugins.hostconfig.routes.catalog

import org.koin.core.annotation.Single
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogSnapshotResponse
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
import site.addzero.kcloud.plugins.hostconfig.catalog.CatalogService

/**
 * IoT 产品建模目录路由。
 *
 * 这里承载“产品定义 / 设备定义 / 属性定义 / 功能定义 / 标签定义”这套元模型，
 * 与工程树里的设备实例和点位配置保持分离，避免产品模板与现场实例耦合。
 */
@Single
@RestController
@RequestMapping("/api/host-config/v1/catalog")
class CatalogController(
    private val catalogService: CatalogService,
) {
    /** 返回整个产品建模工作区快照，供前端一次性读取树和元数据。 */
    @GetMapping("/snapshot")
    fun getSnapshot(): CatalogSnapshotResponse =
        catalogService.getSnapshot()

    /** 返回实体级字段描述，供前端按元数据渲染表单、详情和列表。 */
    @GetMapping("/metadata")
    fun getMetadata(): CatalogMetadataResponse =
        catalogService.getMetadata()

    /** 返回标签定义字典，用于分类和多选关联。 */
    @GetMapping("/labels")
    fun listLabels(): List<LabelDefinitionResponse> =
        catalogService.listLabels()

    /** 新增一个产品标签定义。 */
    @PostMapping("/labels")
    fun createLabel(
        @RequestBody request: LabelDefinitionCreateRequest,
    ): LabelDefinitionResponse =
        catalogService.createLabel(request)

    /**
     * 更新一个标签定义。
     *
     * 这里保留 path id + body 的组合，是为了让资源地址保持稳定，
     * 同时不为 update 再造一层只多一个 id 的请求包装。
     */
    @PutMapping("/labels/{labelId}")
    fun updateLabel(
        @PathVariable labelId: Long,
        @RequestBody request: LabelDefinitionUpdateRequest,
    ): LabelDefinitionResponse =
        catalogService.updateLabel(labelId, request)

    /** 删除一个标签定义，并级联清理产品上的标签关联。 */
    @DeleteMapping("/labels/{labelId}")
    fun deleteLabel(
        @PathVariable labelId: Long,
    ) {
        catalogService.deleteLabel(labelId)
    }

    /** 新增产品定义。 */
    @PostMapping("/products")
    fun createProduct(
        @RequestBody request: ProductDefinitionCreateRequest,
    ): ProductDefinitionTreeResponse =
        catalogService.createProduct(request)

    /**
     * 更新产品定义。
     *
     * 这里同样保留 path id + body，避免把资源标识塞回请求体污染领域模型。
     */
    @PutMapping("/products/{productId}")
    fun updateProduct(
        @PathVariable productId: Long,
        @RequestBody request: ProductDefinitionUpdateRequest,
    ): ProductDefinitionTreeResponse =
        catalogService.updateProduct(productId, request)

    /** 删除一个产品定义，并级联删除其设备、属性和功能模板。 */
    @DeleteMapping("/products/{productId}")
    fun deleteProduct(
        @PathVariable productId: Long,
    ) {
        catalogService.deleteProduct(productId)
    }

    /** 在产品下新增设备定义。 */
    @PostMapping("/products/{productId}/devices")
    fun createDeviceDefinition(
        @PathVariable productId: Long,
        @RequestBody request: DeviceDefinitionCreateRequest,
    ): DeviceDefinitionTreeResponse =
        catalogService.createDeviceDefinition(productId, request)

    /** 更新设备定义。 */
    @PutMapping("/devices/{deviceDefinitionId}")
    fun updateDeviceDefinition(
        @PathVariable deviceDefinitionId: Long,
        @RequestBody request: DeviceDefinitionUpdateRequest,
    ): DeviceDefinitionTreeResponse =
        catalogService.updateDeviceDefinition(deviceDefinitionId, request)

    /** 删除设备定义。 */
    @DeleteMapping("/devices/{deviceDefinitionId}")
    fun deleteDeviceDefinition(
        @PathVariable deviceDefinitionId: Long,
    ) {
        catalogService.deleteDeviceDefinition(deviceDefinitionId)
    }

    /** 在设备定义下新增属性定义。 */
    @PostMapping("/devices/{deviceDefinitionId}/properties")
    fun createPropertyDefinition(
        @PathVariable deviceDefinitionId: Long,
        @RequestBody request: PropertyDefinitionCreateRequest,
    ): PropertyDefinitionResponse =
        catalogService.createPropertyDefinition(deviceDefinitionId, request)

    /** 更新属性定义。 */
    @PutMapping("/properties/{propertyDefinitionId}")
    fun updatePropertyDefinition(
        @PathVariable propertyDefinitionId: Long,
        @RequestBody request: PropertyDefinitionUpdateRequest,
    ): PropertyDefinitionResponse =
        catalogService.updatePropertyDefinition(propertyDefinitionId, request)

    /** 删除属性定义。 */
    @DeleteMapping("/properties/{propertyDefinitionId}")
    fun deletePropertyDefinition(
        @PathVariable propertyDefinitionId: Long,
    ) {
        catalogService.deletePropertyDefinition(propertyDefinitionId)
    }

    /** 在设备定义下新增功能定义。 */
    @PostMapping("/devices/{deviceDefinitionId}/features")
    fun createFeatureDefinition(
        @PathVariable deviceDefinitionId: Long,
        @RequestBody request: FeatureDefinitionCreateRequest,
    ): FeatureDefinitionResponse =
        catalogService.createFeatureDefinition(deviceDefinitionId, request)

    /** 更新功能定义。 */
    @PutMapping("/features/{featureDefinitionId}")
    fun updateFeatureDefinition(
        @PathVariable featureDefinitionId: Long,
        @RequestBody request: FeatureDefinitionUpdateRequest,
    ): FeatureDefinitionResponse =
        catalogService.updateFeatureDefinition(featureDefinitionId, request)

    /** 删除功能定义。 */
    @DeleteMapping("/features/{featureDefinitionId}")
    fun deleteFeatureDefinition(
        @PathVariable featureDefinitionId: Long,
    ) {
        catalogService.deleteFeatureDefinition(featureDefinitionId)
    }

    /** 返回 spec-iot 兼容的属性定义，供 telemetry 和协议层直接消费。 */
    @GetMapping("/devices/{deviceDefinitionId}/spec-iot-properties")
    fun listSpecIotProperties(
        @PathVariable deviceDefinitionId: Long,
    ): List<SpecIotPropertyResponse> =
        catalogService.listSpecIotProperties(deviceDefinitionId)
}
