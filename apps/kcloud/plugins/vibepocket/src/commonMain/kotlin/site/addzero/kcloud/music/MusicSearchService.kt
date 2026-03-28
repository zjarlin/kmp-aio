package site.addzero.vibepocket.music

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import site.addzero.vibepocket.api.MusicSearchApi
import site.addzero.vibepocket.api.ServerApiClient
import site.addzero.kcloud.api.music.MusicLyric
import site.addzero.kcloud.api.music.MusicResolvedAsset
import site.addzero.kcloud.api.music.MusicTrack

object MusicSearchService {
    private val supportedProviders = listOf("netease", "qq")
    private val resolvedAssetCache = linkedMapOf<String, MusicResolvedAsset>()
    internal var api: MusicSearchApi = ServerApiClient.musicApi

    suspend fun search(keyword: String): List<MusicTrack> {
        val normalizedKeyword = keyword.trim()
        require(normalizedKeyword.isNotBlank()) { "请输入歌曲关键词" }

        val resultsByProvider = coroutineScope {
            supportedProviders.map { provider ->
                async {
                    provider to runCatching {
                        api.search(provider, normalizedKeyword)
                    }
                }
            }.awaitAll()
        }

        val successes = resultsByProvider.mapNotNull { (_, result) ->
            result.getOrNull()
        }
        if (successes.isNotEmpty()) {
            return successes
                .flatten()
                .distinctBy(::playbackId)
        }

        val messages = resultsByProvider.mapNotNull { (provider, result) ->
            result.exceptionOrNull()?.userMessage("${provider.displayName()} 搜索失败")
        }
        throw IllegalStateException(messages.firstOrNull() ?: "搜索歌曲失败")
    }

    suspend fun getLyrics(track: MusicTrack): MusicLyric {
        val provider = normalizeProvider(track.platform)
        val songId = track.id.trim()
        require(songId.isNotBlank()) { "歌曲 ID 不能为空" }

        return runMusicRequest("获取歌词失败") {
            api.getLyrics(provider, songId)
        }
    }

    suspend fun resolve(track: MusicTrack): MusicResolvedAsset {
        val cacheKey = cacheKey(track)
        resolvedAssetCache[cacheKey]?.let { return it }

        val resolved = runMusicRequest("解析音频失败") {
            api.resolve(track.copy(platform = normalizeProvider(track.platform)))
        }
        resolvedAssetCache[cacheKey] = resolved
        return resolved
    }

    fun playbackId(track: MusicTrack): String = "music:${normalizeProvider(track.platform)}:${track.id}"

    internal fun resetForTests() {
        api = ServerApiClient.musicApi
        resolvedAssetCache.clear()
    }

    private suspend fun <T> runMusicRequest(
        fallback: String,
        block: suspend () -> T,
    ): T {
        return runCatching {
            block()
        }.getOrElse { error ->
            throw IllegalStateException(error.userMessage(fallback))
        }
    }

    private fun cacheKey(track: MusicTrack): String = "${normalizeProvider(track.platform)}:${track.id}"

    private fun normalizeProvider(provider: String): String {
        return when (provider.trim().lowercase()) {
            "netease", "163", "网易", "网易云" -> "netease"
            "qq", "qqmusic", "qq音乐", "tencent" -> "qq"
            else -> throw IllegalArgumentException("暂不支持的音源: $provider")
        }
    }

    private fun String.displayName(): String {
        return when (this) {
            "netease" -> "网易云"
            "qq" -> "QQ 音乐"
            else -> this
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
