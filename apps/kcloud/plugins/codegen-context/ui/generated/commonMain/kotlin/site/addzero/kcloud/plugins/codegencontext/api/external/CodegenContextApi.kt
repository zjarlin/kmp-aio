package site.addzero.kcloud.plugins.codegencontext.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDetailDto
import site.addzero.kcloud.plugins.codegencontext.api.context.GenerateContractsResponseDto

/**
 * 原始Controller: site.addzero.kcloud.plugins.codegencontext.codegen_context.routes.CodegenContextController
 * 基础路径: /api/codegen-context/v1/contexts
 */
interface CodegenContextApi {

/**
 * listContexts
 * HTTP方法: GET
 * 路径: /api/codegen-context/v1/contexts
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextSummaryDto>
 */
    @GET("/api/codegen-context/v1/contexts")
    suspend fun listContexts(): kotlin.collections.List<site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextSummaryDto>

/**
 * getContext
 * HTTP方法: GET
 * 路径: /api/codegen-context/v1/contexts/{contextId}
 * 参数:
 *   - contextId: kotlin.Long (PathVariable)
 * 返回类型: site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDetailDto
 */
    @GET("/api/codegen-context/v1/contexts/{contextId}")
    suspend fun getContext(
        @Path("contextId") contextId: kotlin.Long
    ): site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDetailDto

/**
 * saveContext
 * HTTP方法: POST
 * 路径: /api/codegen-context/v1/contexts/save
 * 参数:
 *   - request: site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDetailDto (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDetailDto
 */
    @POST("/api/codegen-context/v1/contexts/save")
    @Headers("Content-Type: application/json")
    suspend fun saveContext(
        @Body request: site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDetailDto
    ): site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDetailDto

/**
 * deleteContext
 * HTTP方法: DELETE
 * 路径: /api/codegen-context/v1/contexts/{contextId}
 * 参数:
 *   - contextId: kotlin.Long (PathVariable)
 * 返回类型: kotlin.Unit
 */
    @DELETE("/api/codegen-context/v1/contexts/{contextId}")
    suspend fun deleteContext(
        @Path("contextId") contextId: kotlin.Long
    ): kotlin.Unit

/**
 * generateContext
 * HTTP方法: POST
 * 路径: /api/codegen-context/v1/contexts/{contextId}/generate
 * 参数:
 *   - contextId: kotlin.Long (PathVariable)
 * 返回类型: site.addzero.kcloud.plugins.codegencontext.api.context.GenerateContractsResponseDto
 */
    @POST("/api/codegen-context/v1/contexts/{contextId}/generate")
    suspend fun generateContext(
        @Path("contextId") contextId: kotlin.Long
    ): site.addzero.kcloud.plugins.codegencontext.api.context.GenerateContractsResponseDto

}