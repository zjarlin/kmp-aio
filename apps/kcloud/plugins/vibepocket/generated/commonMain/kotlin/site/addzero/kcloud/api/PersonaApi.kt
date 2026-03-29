package site.addzero.kcloud.api

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.vibepocket.routes.PersonaResponse
import site.addzero.kcloud.vibepocket.routes.PersonaSaveRequest

/**
 * 原始文件: site.addzero.kcloud.vibepocket.routes.Persona.kt
 * 基础路径: 
 */
interface PersonaApi {

/**
 * getPersonas
 * HTTP方法: GET
 * 路径: /api/personas
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.vibepocket.routes.PersonaResponse>
 */
    @GET("/api/personas")
    suspend fun getPersonas(): kotlin.collections.List<site.addzero.kcloud.vibepocket.routes.PersonaResponse>

/**
 * savePersona
 * HTTP方法: POST
 * 路径: /api/personas
 * 参数:
 *   - request: site.addzero.kcloud.vibepocket.routes.PersonaSaveRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.vibepocket.routes.PersonaResponse
 */
    @POST("/api/personas")
    @Headers("Content-Type: application/json")
    suspend fun savePersona(
        @Body request: site.addzero.kcloud.vibepocket.routes.PersonaSaveRequest
    ): site.addzero.kcloud.vibepocket.routes.PersonaResponse

}