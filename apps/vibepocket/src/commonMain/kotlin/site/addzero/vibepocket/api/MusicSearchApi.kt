package site.addzero.vibepocket.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query
import site.addzero.vibepocket.api.music.MusicLyric
import site.addzero.vibepocket.api.music.MusicResolvedAsset
import site.addzero.vibepocket.api.music.MusicTrack

interface MusicSearchApi {

    @GET("api/music/search")
    suspend fun search(
        @Query("provider") provider: String,
        @Query("keyword") keyword: String,
    ): List<MusicTrack>

    @GET("api/music/lyrics")
    suspend fun getLyrics(
        @Query("provider") provider: String,
        @Query("songId") songId: String,
    ): MusicLyric

    @Headers("Content-Type: application/json")
    @POST("api/music/resolve")
    suspend fun resolve(@Body track: MusicTrack): MusicResolvedAsset
}
