package site.addzero.kcloud.plugins.hostconfig.api.external

import de.jensklingenberg.ktorfit.http.*

/**
 * 原始Controller: site.addzero.kcloud.plugins.hostconfig.routes.template.TemplateController
 * 基础路径: /api/host-config/v1/templates
 */
interface TemplateApi {

/**
 * listProtocolTemplates
 * HTTP方法: GET
 * 路径: /api/host-config/v1/templates/protocols
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.hostconfig.api.template.TemplateOptionResponse>
 */
    @GET("/api/host-config/v1/templates/protocols")
    suspend fun listProtocolTemplates(): kotlin.collections.List<site.addzero.kcloud.plugins.hostconfig.api.template.TemplateOptionResponse>

/**
 * listModuleTemplates
 * HTTP方法: GET
 * 路径: /api/host-config/v1/templates/modules
 * 参数:
 *   - protocolTemplateId: kotlin.Long (RequestParam)
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.hostconfig.api.template.ModuleTemplateOptionResponse>
 */
    @GET("/api/host-config/v1/templates/modules")
    suspend fun listModuleTemplates(
        @Query("protocolTemplateId") protocolTemplateId: kotlin.Long
    ): kotlin.collections.List<site.addzero.kcloud.plugins.hostconfig.api.template.ModuleTemplateOptionResponse>

/**
 * listDeviceTypes
 * HTTP方法: GET
 * 路径: /api/host-config/v1/templates/device-types
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.hostconfig.api.template.TemplateOptionResponse>
 */
    @GET("/api/host-config/v1/templates/device-types")
    suspend fun listDeviceTypes(): kotlin.collections.List<site.addzero.kcloud.plugins.hostconfig.api.template.TemplateOptionResponse>

/**
 * listRegisterTypes
 * HTTP方法: GET
 * 路径: /api/host-config/v1/templates/register-types
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.hostconfig.api.template.TemplateOptionResponse>
 */
    @GET("/api/host-config/v1/templates/register-types")
    suspend fun listRegisterTypes(): kotlin.collections.List<site.addzero.kcloud.plugins.hostconfig.api.template.TemplateOptionResponse>

/**
 * listDataTypes
 * HTTP方法: GET
 * 路径: /api/host-config/v1/templates/data-types
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.hostconfig.api.template.TemplateOptionResponse>
 */
    @GET("/api/host-config/v1/templates/data-types")
    suspend fun listDataTypes(): kotlin.collections.List<site.addzero.kcloud.plugins.hostconfig.api.template.TemplateOptionResponse>

}