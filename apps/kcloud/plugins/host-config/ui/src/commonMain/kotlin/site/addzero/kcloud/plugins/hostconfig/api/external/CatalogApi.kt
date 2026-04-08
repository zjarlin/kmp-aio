package site.addzero.kcloud.plugins.hostconfig.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogSnapshotResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionTreeResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionTreeResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionUpdateRequest

/**
 * 原始Controller: site.addzero.kcloud.plugins.hostconfig.routes.catalog.CatalogController
 * 基础路径: /api/host-config/v1/catalog
 */
interface CatalogApi {

/**
 * getSnapshot
 * HTTP方法: GET
 * 路径: /api/host-config/v1/catalog/snapshot
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogSnapshotResponse
 */
    @GET("/api/host-config/v1/catalog/snapshot")
    suspend fun getSnapshot(): site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogSnapshotResponse

/**
 * getMetadata
 * HTTP方法: GET
 * 路径: /api/host-config/v1/catalog/metadata
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogMetadataResponse
 */
    @GET("/api/host-config/v1/catalog/metadata")
    suspend fun getMetadata(): site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogMetadataResponse

/**
 * listLabels
 * HTTP方法: GET
 * 路径: /api/host-config/v1/catalog/labels
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionResponse>
 */
    @GET("/api/host-config/v1/catalog/labels")
    suspend fun listLabels(): kotlin.collections.List<site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionResponse>

/**
 * createLabel
 * HTTP方法: POST
 * 路径: /api/host-config/v1/catalog/labels
 * 参数:
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionCreateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionResponse
 */
    @POST("/api/host-config/v1/catalog/labels")
    @Headers("Content-Type: application/json")
    suspend fun createLabel(
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionCreateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionResponse

/**
 * updateLabel
 * HTTP方法: PUT
 * 路径: /api/host-config/v1/catalog/labels/{labelId}
 * 参数:
 *   - labelId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionUpdateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionResponse
 */
    @PUT("/api/host-config/v1/catalog/labels/{labelId}")
    @Headers("Content-Type: application/json")
    suspend fun updateLabel(
        @Path("labelId") labelId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionUpdateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionResponse

/**
 * deleteLabel
 * HTTP方法: DELETE
 * 路径: /api/host-config/v1/catalog/labels/{labelId}
 * 参数:
 *   - labelId: kotlin.Long (PathVariable)
 * 返回类型: kotlin.Unit
 */
    @DELETE("/api/host-config/v1/catalog/labels/{labelId}")
    suspend fun deleteLabel(
        @Path("labelId") labelId: kotlin.Long
    ): kotlin.Unit

/**
 * createProduct
 * HTTP方法: POST
 * 路径: /api/host-config/v1/catalog/products
 * 参数:
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionCreateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionTreeResponse
 */
    @POST("/api/host-config/v1/catalog/products")
    @Headers("Content-Type: application/json")
    suspend fun createProduct(
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionCreateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionTreeResponse

/**
 * updateProduct
 * HTTP方法: PUT
 * 路径: /api/host-config/v1/catalog/products/{productId}
 * 参数:
 *   - productId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionUpdateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionTreeResponse
 */
    @PUT("/api/host-config/v1/catalog/products/{productId}")
    @Headers("Content-Type: application/json")
    suspend fun updateProduct(
        @Path("productId") productId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionUpdateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionTreeResponse

/**
 * deleteProduct
 * HTTP方法: DELETE
 * 路径: /api/host-config/v1/catalog/products/{productId}
 * 参数:
 *   - productId: kotlin.Long (PathVariable)
 * 返回类型: kotlin.Unit
 */
    @DELETE("/api/host-config/v1/catalog/products/{productId}")
    suspend fun deleteProduct(
        @Path("productId") productId: kotlin.Long
    ): kotlin.Unit

/**
 * createDeviceDefinition
 * HTTP方法: POST
 * 路径: /api/host-config/v1/catalog/products/{productId}/devices
 * 参数:
 *   - productId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionCreateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionTreeResponse
 */
    @POST("/api/host-config/v1/catalog/products/{productId}/devices")
    @Headers("Content-Type: application/json")
    suspend fun createDeviceDefinition(
        @Path("productId") productId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionCreateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionTreeResponse

/**
 * updateDeviceDefinition
 * HTTP方法: PUT
 * 路径: /api/host-config/v1/catalog/devices/{deviceDefinitionId}
 * 参数:
 *   - deviceDefinitionId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionUpdateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionTreeResponse
 */
    @PUT("/api/host-config/v1/catalog/devices/{deviceDefinitionId}")
    @Headers("Content-Type: application/json")
    suspend fun updateDeviceDefinition(
        @Path("deviceDefinitionId") deviceDefinitionId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionUpdateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionTreeResponse

/**
 * deleteDeviceDefinition
 * HTTP方法: DELETE
 * 路径: /api/host-config/v1/catalog/devices/{deviceDefinitionId}
 * 参数:
 *   - deviceDefinitionId: kotlin.Long (PathVariable)
 * 返回类型: kotlin.Unit
 */
    @DELETE("/api/host-config/v1/catalog/devices/{deviceDefinitionId}")
    suspend fun deleteDeviceDefinition(
        @Path("deviceDefinitionId") deviceDefinitionId: kotlin.Long
    ): kotlin.Unit

/**
 * createPropertyDefinition
 * HTTP方法: POST
 * 路径: /api/host-config/v1/catalog/devices/{deviceDefinitionId}/properties
 * 参数:
 *   - deviceDefinitionId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionCreateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionResponse
 */
    @POST("/api/host-config/v1/catalog/devices/{deviceDefinitionId}/properties")
    @Headers("Content-Type: application/json")
    suspend fun createPropertyDefinition(
        @Path("deviceDefinitionId") deviceDefinitionId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionCreateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionResponse

/**
 * updatePropertyDefinition
 * HTTP方法: PUT
 * 路径: /api/host-config/v1/catalog/properties/{propertyDefinitionId}
 * 参数:
 *   - propertyDefinitionId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionUpdateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionResponse
 */
    @PUT("/api/host-config/v1/catalog/properties/{propertyDefinitionId}")
    @Headers("Content-Type: application/json")
    suspend fun updatePropertyDefinition(
        @Path("propertyDefinitionId") propertyDefinitionId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionUpdateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionResponse

/**
 * deletePropertyDefinition
 * HTTP方法: DELETE
 * 路径: /api/host-config/v1/catalog/properties/{propertyDefinitionId}
 * 参数:
 *   - propertyDefinitionId: kotlin.Long (PathVariable)
 * 返回类型: kotlin.Unit
 */
    @DELETE("/api/host-config/v1/catalog/properties/{propertyDefinitionId}")
    suspend fun deletePropertyDefinition(
        @Path("propertyDefinitionId") propertyDefinitionId: kotlin.Long
    ): kotlin.Unit

/**
 * createFeatureDefinition
 * HTTP方法: POST
 * 路径: /api/host-config/v1/catalog/devices/{deviceDefinitionId}/features
 * 参数:
 *   - deviceDefinitionId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionCreateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionResponse
 */
    @POST("/api/host-config/v1/catalog/devices/{deviceDefinitionId}/features")
    @Headers("Content-Type: application/json")
    suspend fun createFeatureDefinition(
        @Path("deviceDefinitionId") deviceDefinitionId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionCreateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionResponse

/**
 * updateFeatureDefinition
 * HTTP方法: PUT
 * 路径: /api/host-config/v1/catalog/features/{featureDefinitionId}
 * 参数:
 *   - featureDefinitionId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionUpdateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionResponse
 */
    @PUT("/api/host-config/v1/catalog/features/{featureDefinitionId}")
    @Headers("Content-Type: application/json")
    suspend fun updateFeatureDefinition(
        @Path("featureDefinitionId") featureDefinitionId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionUpdateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionResponse

/**
 * deleteFeatureDefinition
 * HTTP方法: DELETE
 * 路径: /api/host-config/v1/catalog/features/{featureDefinitionId}
 * 参数:
 *   - featureDefinitionId: kotlin.Long (PathVariable)
 * 返回类型: kotlin.Unit
 */
    @DELETE("/api/host-config/v1/catalog/features/{featureDefinitionId}")
    suspend fun deleteFeatureDefinition(
        @Path("featureDefinitionId") featureDefinitionId: kotlin.Long
    ): kotlin.Unit

/**
 * listSpecIotProperties
 * HTTP方法: GET
 * 路径: /api/host-config/v1/catalog/devices/{deviceDefinitionId}/spec-iot-properties
 * 参数:
 *   - deviceDefinitionId: kotlin.Long (PathVariable)
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.hostconfig.api.catalog.SpecIotPropertyResponse>
 */
    @GET("/api/host-config/v1/catalog/devices/{deviceDefinitionId}/spec-iot-properties")
    suspend fun listSpecIotProperties(
        @Path("deviceDefinitionId") deviceDefinitionId: kotlin.Long
    ): kotlin.collections.List<site.addzero.kcloud.plugins.hostconfig.api.catalog.SpecIotPropertyResponse>

}