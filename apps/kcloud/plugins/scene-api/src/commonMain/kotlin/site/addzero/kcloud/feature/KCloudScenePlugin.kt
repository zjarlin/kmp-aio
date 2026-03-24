package site.addzero.kcloud.feature

interface KCloudScenePlugin {
    val sceneId: String
    val displayName: String
    val sort: Int
    val pages: List<KCloudScenePageDto>
        get() = emptyList()
}

data class DefaultKCloudScenePlugin(
    override val sceneId: String,
    override val displayName: String,
    override val sort: Int,
    override val pages: List<KCloudScenePageDto> = emptyList(),
) : KCloudScenePlugin
