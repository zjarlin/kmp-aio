package site.addzero.kcloud.feature

import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelope<T>(
    val code: Int = 200,
    val data: T,
    val msg: String? = null,
)

@Serializable
data class KCloudSceneDto(
    val sceneId: String,
    val displayName: String,
    val sort: Int,
)

@Serializable
data class KCloudScenePageDto(
    val pageId: String,
    val title: String,
    val path: String,
)

@Serializable
data class KCloudSceneSummary(
    val sceneId: String,
    val displayName: String,
    val pages: List<KCloudScenePageDto>,
)
