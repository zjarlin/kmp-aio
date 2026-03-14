package site.addzero.vibepocket.music

import site.addzero.vibepocket.api.ServerApiClient
import site.addzero.vibepocket.api.music.MusicLyric
import site.addzero.vibepocket.api.music.MusicResolvedAsset
import site.addzero.vibepocket.api.music.MusicTrack

internal interface MusicSearchGateway {
    suspend fun search(
        provider: String,
        keyword: String,
    ): List<MusicTrack>

    suspend fun getLyrics(
        provider: String,
        songId: String,
    ): MusicLyric

    suspend fun resolve(track: MusicTrack): MusicResolvedAsset
}

private object ServerMusicSearchGateway : MusicSearchGateway {
    override suspend fun search(
        provider: String,
        keyword: String,
    ): List<MusicTrack> = ServerApiClient.searchMusic(provider, keyword)

    override suspend fun getLyrics(
        provider: String,
        songId: String,
    ): MusicLyric = ServerApiClient.getMusicLyrics(provider, songId)

    override suspend fun resolve(track: MusicTrack): MusicResolvedAsset =
        ServerApiClient.resolveMusic(track)
}

object MusicSearchService {
    private val resolvedAssetCache = linkedMapOf<String, MusicResolvedAsset>()
    internal var gateway: MusicSearchGateway = ServerMusicSearchGateway

    suspend fun search(
        provider: String,
        keyword: String,
    ): List<MusicTrack> {
        val normalizedProvider = normalizeProvider(provider)
        val normalizedKeyword = keyword.trim()
        require(normalizedKeyword.isNotBlank()) { "请输入歌曲关键词" }

        return runCatching {
            gateway.search(normalizedProvider, normalizedKeyword)
        }.getOrElse { error ->
            throw IllegalStateException(error.userMessage("搜索歌曲失败"))
        }
    }

    suspend fun getLyrics(track: MusicTrack): MusicLyric {
        val provider = normalizeProvider(track.platform)
        val songId = track.id.trim()
        require(songId.isNotBlank()) { "歌曲 ID 不能为空" }

        return runCatching {
            gateway.getLyrics(provider, songId)
        }.getOrElse { error ->
            throw IllegalStateException(error.userMessage("获取歌词失败"))
        }
    }

    suspend fun resolve(track: MusicTrack): MusicResolvedAsset {
        val cacheKey = cacheKey(track)
        resolvedAssetCache[cacheKey]?.let { return it }

        val resolved = runCatching {
            gateway.resolve(track.copy(platform = normalizeProvider(track.platform)))
        }.getOrElse { error ->
            throw IllegalStateException(error.userMessage("解析音频失败"))
        }
        resolvedAssetCache[cacheKey] = resolved
        return resolved
    }

    fun playbackId(track: MusicTrack): String = "music:${normalizeProvider(track.platform)}:${track.id}"

    internal fun resetForTests() {
        gateway = ServerMusicSearchGateway
        resolvedAssetCache.clear()
    }

    private fun cacheKey(track: MusicTrack): String = "${normalizeProvider(track.platform)}:${track.id}"

    private fun normalizeProvider(provider: String): String {
        return when (provider.trim().lowercase()) {
            "netease", "163", "网易", "网易云" -> "netease"
            "qq", "qqmusic", "qq音乐", "tencent" -> "qq"
            else -> throw IllegalArgumentException("暂不支持的音源: $provider")
        }
    }

    private fun Throwable.userMessage(fallback: String): String {
        val raw = message?.trim().orEmpty()
        return when {
            raw.isBlank() -> fallback
            raw.contains("502") -> "$fallback，音源服务当前不可用"
            raw.contains("400") -> raw.substringAfter(':', raw).ifBlank { fallback }
            else -> raw
        }
    }
}
