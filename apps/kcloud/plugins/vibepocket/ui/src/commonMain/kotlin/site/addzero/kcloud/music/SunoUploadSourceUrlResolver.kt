package site.addzero.kcloud.music

import site.addzero.kcloud.api.ApiProvider
import site.addzero.vibepocket.music.UploadCoverSourcePrepareRequest

private const val DEFAULT_UPLOAD_COVER_PLAYBACK_RATE = 1.06

internal suspend fun prepareSunoUploadSourceUrl(rawUrl: String): String {
    val normalizedUrl = rawUrl.trim()
    require(normalizedUrl.isNotBlank()) { "请输入音频 URL" }
    return ApiProvider.musicSearchApi.prepareUploadCoverSource(
        UploadCoverSourcePrepareRequest(
            sourceUrl = normalizedUrl,
            playbackRate = DEFAULT_UPLOAD_COVER_PLAYBACK_RATE,
        )
    ).preparedUrl
}
