package site.addzero.kcloud.api

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.vibepocket.routes.FavoriteResponse
import site.addzero.kcloud.vibepocket.routes.FavoriteRequest
import site.addzero.kcloud.vibepocket.dto.OkResponse

/**
 * 原始文件: site.addzero.kcloud.vibepocket.routes.Favorite.kt
 * 基础路径: 
 */
interface FavoriteApi {

/**
 * getFavorites
 * HTTP方法: GET
 * 路径: /api/favorites
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.vibepocket.routes.FavoriteResponse>
 */
    @GET("/api/favorites")    suspend fun getFavorites(): kotlin.collections.List<site.addzero.kcloud.vibepocket.routes.FavoriteResponse>

/**
 * addFavorite
 * HTTP方法: POST
 * 路径: /api/favorites
 * 参数:
 *   - request: site.addzero.kcloud.vibepocket.routes.FavoriteRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.vibepocket.routes.FavoriteResponse
 */
    @POST("/api/favorites")    suspend fun addFavorite(
        @Body request: site.addzero.kcloud.vibepocket.routes.FavoriteRequest
    ): site.addzero.kcloud.vibepocket.routes.FavoriteResponse

/**
 * removeFavorite
 * HTTP方法: DELETE
 * 路径: /api/favorites/{trackId}
 * 参数:
 *   - trackId: kotlin.String (PathVariable)
 * 返回类型: site.addzero.kcloud.vibepocket.dto.OkResponse
 */
    @DELETE("/api/favorites/{trackId}")    suspend fun removeFavorite(
        @Path("trackId") trackId: kotlin.String
    ): site.addzero.kcloud.vibepocket.dto.OkResponse

}