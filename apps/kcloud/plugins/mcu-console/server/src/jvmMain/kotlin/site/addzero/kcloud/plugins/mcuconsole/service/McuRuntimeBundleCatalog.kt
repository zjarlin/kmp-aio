package site.addzero.kcloud.plugins.mcuconsole.service

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.mcuconsole.*

@Single
class McuRuntimeBundleCatalog(
    private val json: Json,
) {
    private val classLoader = javaClass.classLoader
    private val index by lazy { loadIndex() }
    private val manifests by lazy { loadManifests() }

    fun listBundles(): McuRuntimeBundlesResponse {
        return McuRuntimeBundlesResponse(
            items = manifests.map { it.toSummary() },
            defaultBundleId = resolveDefaultBundleId(),
        )
    }

    fun resolveSummary(
        bundleId: String?,
    ): McuRuntimeBundleSummary {
        return resolveManifest(bundleId).toSummary()
    }

    fun resolveManifest(
        bundleId: String?,
    ): McuRuntimeBundleManifest {
        val normalized = bundleId?.trim().orEmpty()
        if (normalized.isBlank()) {
            return manifests.firstOrNull { it.bundleId == resolveDefaultBundleId() } ?: manifests.first()
        }
        return manifests.firstOrNull { it.bundleId == normalized }
            ?: throw IllegalArgumentException("未知运行时包: $normalized")
    }

    fun resolveResourcePath(
        bundleId: String,
        relativePath: String,
    ): String {
        return "runtime-bundles/$bundleId/$relativePath"
    }

    private fun resolveDefaultBundleId(): String {
        return index.defaultBundleId
            .takeIf { it.isNotBlank() && manifests.any { manifest -> manifest.bundleId == it } }
            ?: manifests.firstOrNull()?.bundleId
            ?: "micropython-default-generic"
    }

    private fun loadIndex(): RuntimeBundleIndex {
        return readJson("runtime-bundles/index.json")
    }

    private fun loadManifests(): List<McuRuntimeBundleManifest> {
        return index.bundleIds.map { bundleId ->
            readJson("runtime-bundles/$bundleId/manifest.json")
        }
    }

    private inline fun <reified T> readJson(
        path: String,
    ): T {
        val stream = classLoader.getResourceAsStream(path)
            ?: throw IllegalStateException("缺少运行时资源: $path")
        return stream.use { input ->
            json.decodeFromString<T>(input.readBytes().decodeToString())
        }
    }
}

@Serializable
private data class RuntimeBundleIndex(
    val defaultBundleId: String = "micropython-default-generic",
    val bundleIds: List<String> = emptyList(),
)

@Serializable
data class McuRuntimeBundleManifest(
    val bundleId: String = "",
    val title: String = "",
    val runtimeKind: McuFlashRuntimeKind = McuFlashRuntimeKind.MICROPYTHON,
    val mcuFamily: String = "generic",
    val defaultFlashProfileId: String = "",
    val defaultBaudRate: Int = 115200,
    val artifactRelativePath: String = "",
    val resourceFiles: List<String> = emptyList(),
    val atomicCommands: List<McuAtomicCommandDefinition> = emptyList(),
    val scriptExamples: List<McuScriptExample> = emptyList(),
    val widgetTemplates: List<McuWidgetTemplate> = emptyList(),
) {
    fun toSummary(): McuRuntimeBundleSummary {
        return McuRuntimeBundleSummary(
            bundleId = bundleId,
            title = title,
            runtimeKind = runtimeKind,
            mcuFamily = mcuFamily,
            defaultFlashProfileId = defaultFlashProfileId,
            defaultBaudRate = defaultBaudRate,
            artifactRelativePath = artifactRelativePath,
            atomicCommands = atomicCommands,
            scriptExamples = scriptExamples,
            widgetTemplates = widgetTemplates,
        )
    }
}
