package site.addzero.kcloud.api.netease

import io.ktor.client.HttpClient
import io.ktor.http.*
import site.addzero.core.network.HttpClientFactory

internal expect fun buildNeteaseApi(
    baseUrl: String,
    httpClient: HttpClient,
): NeteaseApi

/**
 * 网易云音乐 API 客户端
 *
 * HTTP 层由 Ktorfit 声明式接口 [NeteaseApi] 处理，
 * 本 object 提供业务便捷方法（歌名+歌手搜索、歌词片段搜索等）。
 */
object MusicSearchClient {
    private const val httpClientProfile = "netease-music"
    private const val BASE_URL = "https://music.163.com/api/"
    private val httpClientFactory: HttpClientFactory
        get() = HttpClientFactory.shared()

    var mytoken: String? = null
        set(value) {
            field = value
            httpClientFactory.putHeader(httpClientProfile, HttpHeaders.Authorization, value)
        }

    private val httpClient: HttpClient
        get() = httpClientFactory.get(httpClientProfile)

    init {
        mytoken = null
    }

    val musicApi: NeteaseApi
        get() = buildNeteaseApi(
            baseUrl = BASE_URL,
            httpClient = httpClient,
        )

//
//    /**
//     * 根据歌名获取歌词
//     *
//     * @param songName 歌名
//     * @param artistName 歌手名（可选，用于精确匹配）
//     * @return 歌词响应，找不到返回 null
//     */
//    suspend fun getLyricBySongName(songName: String, artistName: String? = null): NeteaseLyricResponse? {
//        val songs = searchBySongAndArtist(songName, artistName)
//        if (songs.isEmpty()) return null
//        return musicApi.getLyric(songs.first().id)
//    }

//    /**
//     * 根据歌词片段获取完整歌词
//     *
//     * @param lyricFragment 歌词片段
//     * @param limit 返回数量限制
//     * @param filterEmpty 是否过滤空歌词
//     * @return 歌曲与歌词组合列表
//     */
//    suspend fun getLyricsByFragment(
//        lyricFragment: String,
//        limit: Int = 5,
//        filterEmpty: Boolean = true,
//    ): List<SongWithLyric> {
//        val songs = musicApi.searchByLyric(lyricFragment, limit = limit).result?.songs
//        return songs?.mapNotNull { lyricSong ->
//            try {
//                val lyric = musicApi.getLyric(lyricSong.id)
//                if (filterEmpty && lyric.lrc?.lyric.isNullOrBlank()) {
//                    null
//                } else {
//                    val song = NeteaseSearchSong(
//                        id = lyricSong.id,
//                        name = lyricSong.name,
//                        artists = lyricSong.artists,
//                        album = lyricSong.album,
//                        duration = lyricSong.duration,
//                    )
//                    SongWithLyric(song, lyric)
//                }
//            } catch (_: Exception) {
//                null
//            }
//        }.orEmpty()
//    }
}
