package site.addzero.network.call.musiclib

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*
import site.addzero.kcloud.api.netease.MusicSearchClient
import site.addzero.kcloud.api.netease.SearchType as NeteaseSearchType
import site.addzero.network.call.musiclib.model.Song
import site.addzero.network.call.qqmusic.QQMusic
import site.addzero.network.call.qqmusic.createQQMusicMainApi
import site.addzero.network.call.qqmusic.createQQMusicQzoneApi
import site.addzero.network.call.qqmusic.model.SearchType as QQSearchType

object MusicAPIFactory {
    fun create(provider: String): MusicProvider {
        return when (provider.trim().lowercase()) {
            "netease" -> NeteaseMusicProvider
            "qq" -> QQMusicProvider
            else -> throw IllegalArgumentException("Unsupported provider: $provider")
        }
    }
}

interface MusicProvider {
    suspend fun search(keyword: String): List<Song>
    suspend fun getLyrics(song: Song): String
    suspend fun getDownloadURL(song: Song): String
}

private object NeteaseMusicProvider : MusicProvider {
    override suspend fun search(keyword: String): List<Song> {
        return MusicSearchClient.musicApi.search(
            s = keyword,
            type = NeteaseSearchType.SONG.value,
            limit = 20,
            offset = 0,
        ).result?.songs.orEmpty().map { song ->
            Song(
                id = song.id.toString(),
                name = song.name,
                artist = song.artistNames,
                album = song.album?.name.orEmpty(),
                duration = (song.duration / 1000L).toInt(),
                source = "netease",
                cover = song.coverUrl.orEmpty(),
                link = "https://music.163.com/#/song?id=${song.id}",
                extra = mapOf("song_id" to song.id.toString()),
            )
        }
    }

    override suspend fun getLyrics(song: Song): String {
        val lyric = MusicSearchClient.musicApi.getLyric(song.id.toLong())
        return buildString {
            lyric.lrc?.lyric?.takeIf { it.isNotBlank() }?.let(::append)
            lyric.tlyric?.lyric?.takeIf { it.isNotBlank() }?.let {
                if (isNotEmpty()) {
                    append("\n")
                }
                append(it)
            }
        }
    }

    override suspend fun getDownloadURL(song: Song): String {
        return song.link.ifBlank {
            throw IllegalStateException("网易云暂不支持直链解析")
        }
    }
}

private object QQMusicProvider : MusicProvider {
    private val qqJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(qqJson, contentType = ContentType.Text.Plain)
            json(qqJson, contentType = ContentType.Text.Html)
            json(qqJson, contentType = ContentType.Application.Json)
        }
    }

    private val client by lazy {
        val mainApi = Ktorfit.Builder()
            .baseUrl("https://u.y.qq.com/")
            .httpClient(httpClient)
            .build()
            .createQQMusicMainApi()
        val qzoneApi = Ktorfit.Builder()
            .baseUrl("https://i.y.qq.com/")
            .httpClient(httpClient)
            .build()
            .createQQMusicQzoneApi()
        QQMusic(mainApi, qzoneApi)
    }

    override suspend fun search(keyword: String): List<Song> {
        val body = client.search(
            keyword = keyword,
            searchType = QQSearchType.SONG,
            resultNum = 20,
        ) ?: return emptyList()
        val items = body.jsonObject["list"]?.jsonArray.orEmpty()
        return items.mapNotNull { item ->
            item.toQQSongOrNull()
        }
    }

    override suspend fun getLyrics(song: Song): String {
        return client.getLyric(song.extra["songmid"] ?: song.id)
    }

    override suspend fun getDownloadURL(song: Song): String {
        return client.getMusicUrl(song.extra["songmid"] ?: song.id)
            ?: throw IllegalStateException("QQ 音乐未返回可用音频地址")
    }

    private fun JsonElement.toQQSongOrNull(): Song? {
        val obj = this as? JsonObject ?: return null
        val songMid = obj.string("mid") ?: return null
        val songName = obj.string("title") ?: return null
        val singers = obj.array("singer")
            .mapNotNull { singer -> (singer as? JsonObject)?.string("name") }
            .joinToString(", ")
        val album = obj.objectValue("album")
        val albumName = album?.string("title").orEmpty()
        val albumMid = album?.string("mid").orEmpty()
        val duration = obj.string("interval")?.toIntOrNull() ?: 0
        val cover = albumMid.takeIf { it.isNotBlank() }
            ?.let { "https://y.gtimg.cn/music/photo_new/T002R300x300M000${it}.jpg" }
            .orEmpty()
        return Song(
            id = songMid,
            name = songName,
            artist = singers,
            album = albumName,
            duration = duration,
            source = "qq",
            cover = cover,
            link = "https://y.qq.com/n/ryqq/songDetail/$songMid",
            extra = mapOf("songmid" to songMid),
        )
    }
}

private fun JsonObject.string(key: String): String? {
    return this[key]?.jsonPrimitive?.contentOrNull
}

private fun JsonObject.objectValue(key: String): JsonObject? {
    return this[key]?.jsonObject
}

private fun JsonObject.array(key: String): JsonArray {
    return this[key]?.jsonArray ?: JsonArray(emptyList())
}
