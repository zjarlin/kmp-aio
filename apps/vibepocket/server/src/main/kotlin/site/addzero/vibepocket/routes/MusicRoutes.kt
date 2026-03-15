package site.addzero.vibepocket.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import site.addzero.springktor.runtime.SpringRouteResult
import site.addzero.springktor.runtime.springBadGateway
import site.addzero.springktor.runtime.springBadRequest
import site.addzero.springktor.runtime.springOk
import site.addzero.starter.statuspages.ErrorResponse
import site.addzero.vibepocket.api.music.MusicTrack
import site.addzero.vibepocket.service.MusicCatalogService

/**
 * 音乐搜索相关路由
 */
@GetMapping("/api/music/search")
suspend fun searchMusic(
    @RequestParam("provider") provider: String?,
    @RequestParam("keyword") keyword: String?,
): SpringRouteResult<Any> {
    try {
        return springOk(musicCatalogService().search(provider.orEmpty(), keyword.orEmpty()))
    } catch (error: IllegalArgumentException) {
        return springBadRequest(ErrorResponse(400, error.message ?: "Invalid request"))
    } catch (error: Exception) {
        return springBadGateway(ErrorResponse(502, error.message ?: "Music provider request failed"))
    }
}

@GetMapping("/api/music/lyrics")
suspend fun readLyrics(
    @RequestParam("provider") provider: String?,
    @RequestParam("songId") songId: String?,
): SpringRouteResult<Any> {
    try {
        return springOk(musicCatalogService().getLyrics(provider.orEmpty(), songId.orEmpty()))
    } catch (error: IllegalArgumentException) {
        return springBadRequest(ErrorResponse(400, error.message ?: "Invalid request"))
    } catch (error: Exception) {
        return springBadGateway(ErrorResponse(502, error.message ?: "Music provider request failed"))
    }
}

@PostMapping("/api/music/resolve")
suspend fun resolveMusic(
    @RequestBody track: MusicTrack,
): SpringRouteResult<Any> {
    try {
        return springOk(musicCatalogService().resolve(track))
    } catch (error: IllegalArgumentException) {
        return springBadRequest(ErrorResponse(400, error.message ?: "Invalid request"))
    } catch (error: Exception) {
        return springBadGateway(ErrorResponse(502, error.message ?: "Music provider request failed"))
    }
}

private fun musicCatalogService(): MusicCatalogService {
    return KoinPlatform.getKoin().get()
}
