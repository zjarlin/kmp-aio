package site.addzero.kcloud.vibepocket.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import site.addzero.kcloud.api.music.MusicLyric
import site.addzero.kcloud.api.music.MusicResolvedAsset
import site.addzero.kcloud.api.music.MusicTrack
import site.addzero.kcloud.vibepocket.service.MusicCatalogService
import site.addzero.kcloud.vibepocket.service.UploadCoverSourcePreparationService
import site.addzero.vibepocket.music.UploadCoverSourcePrepareRequest
import site.addzero.vibepocket.music.UploadCoverSourcePrepareResponse

/**
 * 音乐搜索相关路由
 */
@GetMapping("/api/music/search")
suspend fun search(
    @RequestParam("provider") provider: String?,
    @RequestParam("keyword") keyword: String?,
): List<MusicTrack> {
    return KoinPlatform.getKoin().get<MusicCatalogService>().search(provider.orEmpty(), keyword.orEmpty())
}

@GetMapping("/api/music/lyrics")
suspend fun getLyrics(
    @RequestParam("provider") provider: String?,
    @RequestParam("songId") songId: String?,
): MusicLyric {
    return KoinPlatform.getKoin().get<MusicCatalogService>().getLyrics(provider.orEmpty(), songId.orEmpty())
}

@PostMapping("/api/music/resolve")
suspend fun resolve(
    @RequestBody track: MusicTrack,
): MusicResolvedAsset {
    return KoinPlatform.getKoin().get<MusicCatalogService>().resolve(track)
}

@PostMapping("/api/music/upload-cover-source/prepare")
suspend fun prepareUploadCoverSource(
    @RequestBody request: UploadCoverSourcePrepareRequest,
): UploadCoverSourcePrepareResponse {
    return KoinPlatform.getKoin().get<UploadCoverSourcePreparationService>().prepare(
        sourceUrl = request.sourceUrl,
        playbackRate = request.playbackRate,
    )
}
