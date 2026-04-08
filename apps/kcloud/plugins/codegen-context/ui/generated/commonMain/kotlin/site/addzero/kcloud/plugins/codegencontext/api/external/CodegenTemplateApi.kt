package site.addzero.kcloud.plugins.codegencontext.api.external

import de.jensklingenberg.ktorfit.http.*

/**
 * 原始Controller: site.addzero.kcloud.plugins.codegencontext.codegen_context.routes.CodegenTemplateController
 * 基础路径: /api/codegen-context/v1/templates
 */
interface CodegenTemplateApi {

/**
 * listProtocolTemplates
 * HTTP方法: GET
 * 路径: /api/codegen-context/v1/templates/protocols
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.codegencontext.api.context.ProtocolTemplateOptionDto>
 */
    @GET("/api/codegen-context/v1/templates/protocols")
    suspend fun listProtocolTemplates(): kotlin.collections.List<site.addzero.kcloud.plugins.codegencontext.api.context.ProtocolTemplateOptionDto>

}