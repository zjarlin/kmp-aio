package site.addzero.kcloud.plugins.system.pluginmarket.jvm.service

import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDiscoveryItemDto
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.walk

private val INFRA_MODULE_NAMES = setOf("ui", "server", "shared")

@Single
class PluginModuleDiscoverySupport(
    private val catalog: PluginMarketCatalogSupport,
) {
    fun discoverPluginModules(searchQuery: String? = null): List<PluginDiscoveryItemDto> {
        val pluginsRoot = Paths.get("apps", "kcloud", "plugins")
        if (!pluginsRoot.exists()) {
            return emptyList()
        }
        return discoverPluginModulesUnder(
            pluginsRoot = pluginsRoot,
            managedModuleDirs = catalog.listPackages().map { Paths.get(it.moduleDir).normalize().toString() }.toSet(),
            searchQuery = searchQuery,
        )
    }
}

internal fun discoverPluginModulesUnder(
    pluginsRoot: Path,
    managedModuleDirs: Set<String>,
    searchQuery: String? = null,
): List<PluginDiscoveryItemDto> {
    if (!pluginsRoot.exists()) {
        return emptyList()
    }
    return pluginsRoot.walk()
        .filter { path -> path != pluginsRoot && path.isDirectory() && Files.exists(path.resolve("build.gradle.kts")) }
        .filterNot(::isPluginInfrastructureModule)
        .map { moduleDir ->
            val relative = moduleDir.relativeTo(pluginsRoot).toString().replace("\\", "/")
            val parts = relative.split("/").filter { it.isNotBlank() }
            val pluginId = parts.lastOrNull().orEmpty()
            val pluginGroup = parts.dropLast(1).joinToString("/").ifBlank { null }
            val moduleDirString = moduleDir.normalize().toString()
            val composeKoinModuleClass = detectKoinModuleClass(moduleDir, "ComposeKoinModule")
            val serverKoinModuleClass = detectKoinModuleClass(moduleDir, "ServerKoinModule")
            val routeRegistrarCall = detectRouteRegistrarCall(moduleDir)
            val routeRegistrarImport = routeRegistrarCall?.let { detectRouteRegistrarImport(moduleDir, it) }
            val packageName = detectFirstPackage(moduleDir)
            val issues = buildList {
                if (composeKoinModuleClass == null) add("未找到 Compose Koin 模块类")
                if (routeRegistrarCall == null) add("未找到 Route 注册函数")
            }
            PluginDiscoveryItemDto(
                discoveryId = relative,
                pluginId = pluginId,
                pluginGroup = pluginGroup,
                moduleDir = moduleDirString,
                gradlePath = ":apps:kcloud:plugins:${parts.joinToString(":")}",
                packageName = packageName,
                composeKoinModuleClass = composeKoinModuleClass,
                serverKoinModuleClass = serverKoinModuleClass,
                routeRegistrarImport = routeRegistrarImport,
                routeRegistrarCall = routeRegistrarCall,
                managedByDb = moduleDirString in managedModuleDirs,
                issues = issues,
            )
        }
        .filter { item ->
            searchQuery.isNullOrBlank() ||
                item.pluginId.contains(searchQuery, ignoreCase = true) ||
                item.moduleDir.contains(searchQuery, ignoreCase = true)
        }
        .sortedWith(compareBy<PluginDiscoveryItemDto> { it.pluginGroup.orEmpty() }.thenBy { it.pluginId })
        .toList()
}

private fun isPluginInfrastructureModule(moduleDir: Path): Boolean {
    return moduleDir.name in INFRA_MODULE_NAMES &&
        moduleDir.parent?.resolve("build.gradle.kts")?.let(Files::exists) == true
}

private fun detectKoinModuleClass(moduleDir: Path, suffix: String): String? {
    return moduleDir.walk()
        .filter { Files.isRegularFile(it) && it.name.endsWith(".kt") }
        .firstNotNullOfOrNull { file ->
            val content = file.readText()
            val packageName = Regex("""package\s+([A-Za-z0-9_.]+)""").find(content)?.groupValues?.get(1)
            val className = Regex("""class\s+([A-Za-z0-9_]+$suffix)""").find(content)?.groupValues?.get(1)
            if (packageName != null && className != null) "$packageName.$className" else null
        }
}

private fun detectRouteRegistrarCall(moduleDir: Path): String? {
    return moduleDir.walk()
        .filter { Files.isRegularFile(it) && it.name.endsWith(".kt") }
        .firstNotNullOfOrNull { file ->
            Regex("""fun\s+Route\.([A-Za-z0-9_]+)\(""")
                .find(file.readText())
                ?.groupValues
                ?.get(1)
                ?.plus("()")
        }
}

private fun detectRouteRegistrarImport(moduleDir: Path, call: String): String? {
    val functionName = call.removeSuffix("()")
    return moduleDir.walk()
        .filter { Files.isRegularFile(it) && it.name.endsWith(".kt") }
        .firstNotNullOfOrNull { file ->
            val content = file.readText()
            val packageName = Regex("""package\s+([A-Za-z0-9_.]+)""").find(content)?.groupValues?.get(1)
            val found = Regex("""fun\s+Route\.($functionName)\(""").find(content)
            if (packageName != null && found != null) "$packageName.$functionName" else null
        }
}

private fun detectFirstPackage(moduleDir: Path): String? {
    return moduleDir.walk()
        .filter { Files.isRegularFile(it) && it.name.endsWith(".kt") }
        .firstNotNullOfOrNull { file ->
            Regex("""package\s+([A-Za-z0-9_.]+)""")
                .find(file.readText())
                ?.groupValues
                ?.get(1)
        }
}
