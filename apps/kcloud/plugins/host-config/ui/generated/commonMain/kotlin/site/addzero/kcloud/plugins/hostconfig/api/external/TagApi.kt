package site.addzero.kcloud.plugins.hostconfig.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.hostconfig.api.common.PageResponse
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.ReplaceTagValueTextsRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagPositionUpdateRequest

/**
 * 原始Controller: site.addzero.kcloud.plugins.hostconfig.routes.tag.TagController
 * 基础路径: /api/host-config/v1
 */
interface TagApi {

/**
 * listTags
 * HTTP方法: GET
 * 路径: /api/host-config/v1/devices/{deviceId}/tags
 * 参数:
 *   - deviceId: kotlin.Long (PathVariable)
 *   - offset: kotlin.Int (RequestParam)
 *   - size: kotlin.Int (RequestParam)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.common.PageResponse<site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse>
 */
    @GET("/api/host-config/v1/devices/{deviceId}/tags")    suspend fun listTags(
        @Path("deviceId") deviceId: kotlin.Long,
        @Query("offset") offset: kotlin.Int,
        @Query("size") size: kotlin.Int
    ): site.addzero.kcloud.plugins.hostconfig.api.common.PageResponse<site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse>

/**
 * getTag
 * HTTP方法: GET
 * 路径: /api/host-config/v1/tags/{tagId}
 * 参数:
 *   - tagId: kotlin.Long (PathVariable)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse
 */
    @GET("/api/host-config/v1/tags/{tagId}")    suspend fun getTag(
        @Path("tagId") tagId: kotlin.Long
    ): site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse

/**
 * createTag
 * HTTP方法: POST
 * 路径: /api/host-config/v1/devices/{deviceId}/tags
 * 参数:
 *   - deviceId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.tag.TagCreateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse
 */
    @POST("/api/host-config/v1/devices/{deviceId}/tags")    suspend fun createTag(
        @Path("deviceId") deviceId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.tag.TagCreateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse

/**
 * updateTag
 * HTTP方法: PUT
 * 路径: /api/host-config/v1/tags/{tagId}
 * 参数:
 *   - tagId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.tag.TagUpdateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse
 */
    @PUT("/api/host-config/v1/tags/{tagId}")    suspend fun updateTag(
        @Path("tagId") tagId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.tag.TagUpdateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse

/**
 * replaceValueTexts
 * HTTP方法: PUT
 * 路径: /api/host-config/v1/tags/{tagId}/value-texts
 * 参数:
 *   - tagId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.tag.ReplaceTagValueTextsRequest (RequestBody)
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.plugins.hostconfig.api.tag.TagValueTextResponse>
 */
    @PUT("/api/host-config/v1/tags/{tagId}/value-texts")    suspend fun replaceValueTexts(
        @Path("tagId") tagId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.tag.ReplaceTagValueTextsRequest
    ): kotlin.collections.List<site.addzero.kcloud.plugins.hostconfig.api.tag.TagValueTextResponse>

/**
 * updateTagPosition
 * HTTP方法: PUT
 * 路径: /api/host-config/v1/tags/{tagId}/position
 * 参数:
 *   - tagId: kotlin.Long (PathVariable)
 *   - request: site.addzero.kcloud.plugins.hostconfig.api.tag.TagPositionUpdateRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse
 */
    @PUT("/api/host-config/v1/tags/{tagId}/position")    suspend fun updateTagPosition(
        @Path("tagId") tagId: kotlin.Long,
        @Body request: site.addzero.kcloud.plugins.hostconfig.api.tag.TagPositionUpdateRequest
    ): site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse

/**
 * deleteTag
 * HTTP方法: DELETE
 * 路径: /api/host-config/v1/tags/{tagId}
 * 参数:
 *   - tagId: kotlin.Long (PathVariable)
 * 返回类型: kotlin.Unit
 */
    @DELETE("/api/host-config/v1/tags/{tagId}")    suspend fun deleteTag(
        @Path("tagId") tagId: kotlin.Long
    ): kotlin.Unit

}