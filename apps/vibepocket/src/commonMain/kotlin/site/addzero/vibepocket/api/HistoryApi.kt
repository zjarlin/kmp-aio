package site.addzero.vibepocket.api

import de.jensklingenberg.ktorfit.http.*
import site.addzero.vibepocket.model.*

/**
 * 历史记录相关接口
 */
interface HistoryApi {

    @POST("api/suno/history")
    suspend fun saveHistory(@Body request: MusicHistorySaveRequest): MusicHistoryItem

    @GET("api/suno/history")
    suspend fun getHistory(): List<MusicHistoryItem>
}
