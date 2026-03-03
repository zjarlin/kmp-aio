package site.addzero.vibepocket.api

import de.jensklingenberg.ktorfit.http.*
import site.addzero.vibepocket.model.*

/**
 * 收藏相关接口
 */
interface FavoriteApi {

    @POST("api/favorites")
    suspend fun addFavorite(@Body request: FavoriteRequest): FavoriteItem

    @DELETE("api/favorites/{trackId}")
    suspend fun removeFavorite(@Path("trackId") trackId: String)

    @GET("api/favorites")
    suspend fun getFavorites(): List<FavoriteItem>
}
