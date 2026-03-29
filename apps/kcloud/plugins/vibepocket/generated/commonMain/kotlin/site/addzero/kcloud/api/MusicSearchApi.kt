package site.addzero.kcloud.api

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.api.music.MusicLyric
import site.addzero.kcloud.api.music.MusicResolvedAsset
import site.addzero.kcloud.api.music.MusicTrack
import site.addzero.vibepocket.music.UploadCoverSourcePrepareResponse
import site.addzero.vibepocket.music.UploadCoverSourcePrepareRequest

/**
 * 原始文件: site.addzero.kcloud.vibepocket.routes.MusicSearch.kt
 * 基础路径: 
 */
interface MusicSearchApi {

/**
 * search
 * HTTP方法: GET
 * 路径: /api/music/search
 * 参数:
 *   - provider: kotlin.String? (RequestParam)
 *   - keyword: kotlin.String? (RequestParam)
 * 返回类型: kotlin.collections.List<site.addzero.kcloud.api.music.MusicTrack>
 */
    @GET("/api/music/search")    suspend fun search(
        @Query("provider") provider: kotlin.String?,
        @Query("keyword") keyword: kotlin.String?
    ): kotlin.collections.List<site.addzero.kcloud.api.music.MusicTrack>

/**
 * getLyrics
 * HTTP方法: GET
 * 路径: /api/music/lyrics
 * 参数:
 *   - provider: kotlin.String? (RequestParam)
 *   - songId: kotlin.String? (RequestParam)
 * 返回类型: site.addzero.kcloud.api.music.MusicLyric
 */
    @GET("/api/music/lyrics")    suspend fun getLyrics(
        @Query("provider") provider: kotlin.String?,
        @Query("songId") songId: kotlin.String?
    ): site.addzero.kcloud.api.music.MusicLyric

/**
 * resolve
 * HTTP方法: POST
 * 路径: /api/music/resolve
 * 参数:
 *   - track: site.addzero.kcloud.api.music.MusicTrack (RequestBody)
 * 返回类型: site.addzero.kcloud.api.music.MusicResolvedAsset
 */
    @POST("/api/music/resolve")    suspend fun resolve(
        @Body track: site.addzero.kcloud.api.music.MusicTrack
    ): site.addzero.kcloud.api.music.MusicResolvedAsset

/**
 * prepareUploadCoverSource
 * HTTP方法: POST
 * 路径: /api/music/upload-cover-source/prepare
 * 参数:
 *   - request: site.addzero.vibepocket.music.UploadCoverSourcePrepareRequest (RequestBody)
 * 返回类型: site.addzero.vibepocket.music.UploadCoverSourcePrepareResponse
 */
    @POST("/api/music/upload-cover-source/prepare")    suspend fun prepareUploadCoverSource(
        @Body request: site.addzero.vibepocket.music.UploadCoverSourcePrepareRequest
    ): site.addzero.vibepocket.music.UploadCoverSourcePrepareResponse

}