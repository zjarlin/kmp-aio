package site.addzero.vibepocket.music

import site.addzero.vibepocket.api.ServerApiClient

private const val DEFAULT_UPLOAD_COVER_PLAYBACK_RATE = 1.06

internal suspend fun prepareSunoUploadSourceUrl(rawUrl: String): String {
    val normalizedUrl = rawUrl.trim()
    require(normalizedUrl.isNotBlank()) { "请输入音频 URL" }
    return ServerApiClient.musicApi.prepareUploadCoverSource(
        UploadCoverSourcePrepareRequest(
            sourceUrl = normalizedUrl,
            playbackRate = DEFAULT_UPLOAD_COVER_PLAYBACK_RATE,
        )
    ).preparedUrl
}
