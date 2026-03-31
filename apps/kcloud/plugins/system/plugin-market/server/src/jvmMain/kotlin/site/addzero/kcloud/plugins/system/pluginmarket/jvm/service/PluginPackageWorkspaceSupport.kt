package site.addzero.kcloud.plugins.system.pluginmarket.jvm.service

import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.PluginPackage
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginActivationState
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.walk
import kotlin.io.path.writeText

private const val DISABLED_MARKER_FILE = ".kcloud-plugin-disabled"

@Single
class PluginPackageWorkspaceSupport(
    private val catalog: PluginMarketCatalogSupport,
) {
    fun moduleRoot(pluginPackage: PluginPackage): Path = Paths.get(pluginPackage.moduleDir).normalize()

    fun disabledMarkerPath(pluginPackage: PluginPackage): Path = moduleRoot(pluginPackage).resolve(DISABLED_MARKER_FILE)

    fun isModuleInstalled(pluginPackage: PluginPackage): Boolean = moduleRoot(pluginPackage).exists()

    fun isDisabledByMarker(pluginPackage: PluginPackage): Boolean = disabledMarkerPath(pluginPackage).isRegularFile()

    fun activationStateOf(pluginPackage: PluginPackage): PluginActivationState {
        if (!isModuleInstalled(pluginPackage)) {
            return PluginActivationState.NOT_INSTALLED
        }
        return if (isDisabledByMarker(pluginPackage)) {
            PluginActivationState.DISABLED
        } else {
            PluginActivationState.ENABLED
        }
    }

    fun syncEnabledMarker(pluginPackage: PluginPackage) {
        val moduleRoot = moduleRoot(pluginPackage)
        if (!moduleRoot.exists()) {
            return
        }
        val markerPath = disabledMarkerPath(pluginPackage)
        if (pluginPackage.enabled) {
            markerPath.deleteIfExists()
            return
        }
        markerPath.parent.createDirectories()
        if (!markerPath.exists()) {
            markerPath.writeText(
                """
                disabledBy=plugin-market
                pluginId=${pluginPackage.pluginId}
                reason=package-enabled-flag-false
                """.trimIndent(),
            )
        }
    }

    fun uninstallManagedModule(pluginPackage: PluginPackage) {
        val moduleRoot = moduleRoot(pluginPackage)
        if (!moduleRoot.exists()) {
            return
        }
        val importedFromDisk = catalog.listImportRecords(pluginPackage.id).isNotEmpty()
        if (importedFromDisk) {
            disabledMarkerPath(pluginPackage).apply {
                parent.createDirectories()
                writeText(
                    """
                    disabledBy=plugin-market
                    pluginId=${pluginPackage.pluginId}
                    reason=package-deleted-from-db
                    """.trimIndent(),
                )
            }
            return
        }
        moduleRoot.toFile().deleteRecursively()
    }

    fun collectImportableFiles(moduleDir: Path): List<Pair<String, String>> {
        return moduleDir.walk()
            .filter { path -> Files.isRegularFile(path) }
            .filterNot { path -> path.toString().contains("/build/") || path.toString().contains("\\build\\") }
            .mapNotNull { file ->
                val relative = file.relativeTo(moduleDir).toString().replace("\\", "/")
                if (!isTextFile(relative)) {
                    return@mapNotNull null
                }
                relative to file.readText()
            }
            .sortedBy { it.first }
            .toList()
    }

    private fun isTextFile(relativePath: String): Boolean {
        return listOf(".kt", ".kts", ".md", ".txt", ".yaml", ".yml", ".json", ".properties", ".conf")
            .any { relativePath.endsWith(it) }
    }
}
