package site.addzero.coding.playground

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.kcloud.api.netease.MusicSearchClient
import site.addzero.kcloud.api.netease.NeteaseSearchSong
import site.addzero.kcloud.api.netease.SearchType

@Single
class NeteaseDemoState(
    private val musicSearchClient: MusicSearchClient,
) {
    var token by mutableStateOf("")
        private set
    var query by mutableStateOf("周杰伦")
        private set
    var songs by mutableStateOf<List<NeteaseSearchSong>>(emptyList())
        private set
    var selectedSongId by mutableStateOf<Long?>(null)
        private set
    var lyricPreview by mutableStateOf("这里会展示所选歌曲的歌词。")
        private set
    var statusMessage by mutableStateOf("Inspector 已接入 api-netease，可直接发起搜索。")
        private set
    var loading by mutableStateOf(false)
        private set
    var lyricLoading by mutableStateOf(false)
        private set

    fun updateToken(value: String) {
        token = value
    }

    fun updateQuery(value: String) {
        query = value
    }

    suspend fun searchSongs() {
        val keyword = query.trim()
        if (keyword.isBlank()) {
            songs = emptyList()
            selectedSongId = null
            lyricPreview = "请输入搜索关键词。"
            statusMessage = "关键词不能为空。"
            return
        }
        loading = true
        statusMessage = "正在请求网易云搜索…"
        lyricPreview = "正在等待搜索结果…"
        runCatching {
            applyToken()
            musicSearchClient.musicApi.search(
                s = keyword,
                type = SearchType.SONG.value,
                limit = 8,
                offset = 0,
            )
        }.onSuccess { response ->
            val loadedSongs = response.result?.songs.orEmpty()
            songs = loadedSongs
            selectedSongId = loadedSongs.firstOrNull()?.id
            statusMessage = if (loadedSongs.isEmpty()) {
                "没有找到结果。"
            } else {
                "已加载 ${loadedSongs.size} 首歌曲。"
            }
            if (loadedSongs.isEmpty()) {
                lyricPreview = "当前关键词没有返回歌曲。"
            } else {
                loadLyric(loadedSongs.first().id)
            }
        }.onFailure { error ->
            songs = emptyList()
            selectedSongId = null
            lyricPreview = "搜索失败。"
            statusMessage = error.message ?: error::class.simpleName.orEmpty()
        }
        loading = false
    }

    suspend fun loadLyric(songId: Long) {
        lyricLoading = true
        selectedSongId = songId
        lyricPreview = "正在加载歌词…"
        runCatching {
            applyToken()
            musicSearchClient.musicApi.getLyric(songId)
        }.onSuccess { response ->
            lyricPreview = response.lrc?.lyric?.takeIf { it.isNotBlank() } ?: "该歌曲没有返回歌词。"
            statusMessage = "歌词已加载。"
        }.onFailure { error ->
            lyricPreview = "歌词加载失败。"
            statusMessage = error.message ?: error::class.simpleName.orEmpty()
        }
        lyricLoading = false
    }

    private fun applyToken() {
        musicSearchClient.mytoken = token.trim().ifBlank { null }
    }
}
