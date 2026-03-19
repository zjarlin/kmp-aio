package site.addzero.vibepocket.music

import kotlinx.serialization.Serializable

@Serializable
data class UploadCoverSourcePrepareRequest(
    val sourceUrl: String,
    val playbackRate: Double? = null,
)

@Serializable
data class UploadCoverSourcePrepareResponse(
    val originalUrl: String,
    val preparedUrl: String,
    val playbackRate: Double,
    val fileName: String,
    val contentType: String? = null,
)
