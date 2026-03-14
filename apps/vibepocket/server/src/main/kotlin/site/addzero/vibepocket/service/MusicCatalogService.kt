package site.addzero.vibepocket.service

import org.koin.core.annotation.Single
import site.addzero.network.call.musiclib.MusicAPIFactory
import site.addzero.network.call.musiclib.model.Song
import site.addzero.vibepocket.api.music.MusicLyric
import site.addzero.vibepocket.api.music.MusicResolvedAsset
import site.addzero.vibepocket.api.music.MusicTrack

interface MusicCatalogService {
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

@Single
class MusicLibCatalogService : MusicCatalogService {

    override suspend fun search(
        provider: String,
        keyword: String,
    ): List<MusicTrack> {
        val normalizedProvider = normalizeProvider(provider)
        val normalizedKeyword = keyword.trim()
        require(normalizedKeyword.isNotBlank()) { "keyword is required" }

        val musicProvider = createProvider(normalizedProvider)
        return musicProvider.search(normalizedKeyword).map(::toTrack)
    }

    override suspend fun getLyrics(
        provider: String,
        songId: String,
    ): MusicLyric {
        val normalizedProvider = normalizeProvider(provider)
        val normalizedSongId = songId.trim()
        require(normalizedSongId.isNotBlank()) { "songId is required" }

        val song = Song(
            id = normalizedSongId,
            source = normalizedProvider,
            extra = sourceIdKey(normalizedProvider, normalizedSongId),
        )
        val lrc = createProvider(normalizedProvider).getLyrics(song)
        return MusicLyric(lrc = lrc)
    }

    override suspend fun resolve(track: MusicTrack): MusicResolvedAsset {
        val normalizedProvider = normalizeProvider(track.platform)
        val song = track.toSong(normalizedProvider)
        val resolvedUrl = createProvider(normalizedProvider).getDownloadURL(song)
        val fileName = song.copy(url = resolvedUrl).filename()
        return MusicResolvedAsset(
            url = resolvedUrl,
            fileName = fileName,
            contentType = guessContentType(fileName),
        )
    }

    private fun createProvider(provider: String) = MusicAPIFactory.create(provider)

    private fun normalizeProvider(provider: String): String {
        return when (provider.trim().lowercase()) {
            "netease", "163", "网易", "网易云", "wangyiyun" -> "netease"
            "qq", "qq音乐", "qqmusic", "tencent" -> "qq"
            else -> throw IllegalArgumentException("Unsupported provider: $provider")
        }
    }

    private fun toTrack(song: Song): MusicTrack {
        return MusicTrack(
            id = song.id,
            name = song.name,
            artist = song.artist,
            album = song.album,
            coverUrl = song.cover.ifBlank { null },
            durationMs = song.duration * 1000,
            platform = normalizeProvider(song.source),
            link = song.link.ifBlank { null },
            extra = song.extra,
        )
    }

    private fun MusicTrack.toSong(provider: String): Song {
        val mergedExtra = extra + sourceIdKey(provider, id)
        return Song(
            id = id,
            name = name,
            artist = artist,
            album = album,
            duration = durationMs / 1000,
            source = provider,
            cover = coverUrl.orEmpty(),
            link = link.orEmpty(),
            extra = mergedExtra,
        )
    }

    private fun sourceIdKey(
        provider: String,
        songId: String,
    ): Map<String, String> {
        return when (provider) {
            "netease" -> mapOf("song_id" to songId)
            "qq" -> mapOf("songmid" to songId)
            else -> emptyMap()
        }
    }

    private fun guessContentType(fileName: String): String? {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "m4a" -> "audio/mp4"
            "flac" -> "audio/flac"
            else -> null
        }
    }
}
