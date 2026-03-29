package site.addzero.kcloud.plugins.system.pluginmarket.jvm.service

import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.*
import site.addzero.kcloud.plugins.system.pluginmarket.model.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.*
import javax.sql.DataSource
import kotlin.io.path.*

private const val DISABLED_MARKER_FILE = ".kcloud-plugin-disabled"
private const val COMPOSE_DEP_BLOCK = "plugin-market-compose-deps"
private const val SERVER_DEP_BLOCK = "plugin-market-server-deps"
private const val SHARED_ROUTE_TASK_BLOCK = "plugin-market-route-tasks"
private const val COMPOSE_KOIN_BLOCK = "plugin-market-compose-koin"
private const val DESKTOP_KOIN_BLOCK = "plugin-market-desktop-koin"
private const val SERVER_KOIN_BLOCK = "plugin-market-server-koin"
private const val SERVER_ROUTE_IMPORT_BLOCK = "plugin-market-server-route-imports"
private const val SERVER_ROUTE_BLOCK = "plugin-market-server-routes"
private val INFRA_MODULE_NAMES = setOf("ui", "server", "shared")

@Single
class PluginMarketSupport(
    val sqlClient: KSqlClient,
    private val dataSource: DataSource,
) {
    fun newId(): String = UUID.randomUUID().toString()

    fun now(): LocalDateTime = LocalDateTime.now()

    fun hashContent(content: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(content.toByteArray())
            .joinToString("") { byte -> "%02x".format(byte) }
    }

    fun packageRef(id: String): PluginPackage = new(PluginPackage::class).by { this.id = id }

    fun jobRef(id: String): PluginDeploymentJob = new(PluginDeploymentJob::class).by { this.id = id }

    fun <T> inTransaction(block: () -> T): T {
        dataSource.connection.use { connection ->
            val previousAutoCommit = connection.autoCommit
            connection.autoCommit = false
            try {
                val result = block()
                connection.commit()
                return result
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = previousAutoCommit
            }
        }
    }

    fun packageOrThrow(id: String): PluginPackage {
        return sqlClient.findById(PluginPackage::class, id)
            ?: throw NoSuchElementException("插件包不存在: $id")
    }

    fun fileOrThrow(id: String): PluginSourceFile {
        return sqlClient.findById(PluginSourceFile::class, id)
            ?: throw NoSuchElementException("插件源码文件不存在: $id")
    }

    fun jobOrThrow(id: String): PluginDeploymentJob {
        return sqlClient.findById(PluginDeploymentJob::class, id)
            ?: throw NoSuchElementException("插件部署任务不存在: $id")
    }

    fun listPackages(): List<PluginPackage> {
        return sqlClient.createQuery(PluginPackage::class) { select(table) }
            .execute()
            .sortedWith(compareBy<PluginPackage> { it.pluginGroup.orEmpty() }.thenBy { it.pluginId })
    }

    fun listFiles(packageId: String? = null): List<PluginSourceFile> {
        return sqlClient.createQuery(PluginSourceFile::class) { select(table) }
            .execute()
            .filter { packageId == null || it.pluginPackage.id == packageId }
            .sortedWith(compareBy<PluginSourceFile> { it.orderIndex }.thenBy { it.relativePath })
    }

    fun listPresetBindings(packageId: String? = null): List<PluginPresetBinding> {
        return sqlClient.createQuery(PluginPresetBinding::class) { select(table) }
            .execute()
            .filter { packageId == null || it.pluginPackage.id == packageId }
            .sortedByDescending { it.appliedAt }
    }

    fun listJobs(packageId: String? = null): List<PluginDeploymentJob> {
        return sqlClient.createQuery(PluginDeploymentJob::class) { select(table) }
            .execute()
            .filter { packageId == null || it.pluginPackage.id == packageId }
            .sortedByDescending { it.updatedAt }
    }

    fun listArtifacts(jobId: String? = null, packageId: String? = null): List<PluginDeploymentArtifact> {
        val jobsById = listJobs(packageId).associateBy { it.id }
        return sqlClient.createQuery(PluginDeploymentArtifact::class) { select(table) }
            .execute()
            .filter { artifact -> jobId == null || artifact.deploymentJob.id == jobId }
            .filter { artifact -> packageId == null || artifact.deploymentJob.id in jobsById }
            .sortedByDescending { it.createdAt }
    }

    fun listImportRecords(packageId: String? = null): List<PluginImportRecord> {
        return sqlClient.createQuery(PluginImportRecord::class) { select(table) }
            .execute()
            .filter { packageId == null || it.pluginPackage.id == packageId }
            .sortedByDescending { it.importedAt }
    }

    fun buildAggregate(packageId: String): PluginPackageAggregateDto {
        val pluginPackage = packageOrThrow(packageId)
        return PluginPackageAggregateDto(
            pluginPackage = toDto(pluginPackage),
            files = listFiles(packageId).map { it.toDto() },
            presetBindings = listPresetBindings(packageId).map { it.toDto() },
            jobs = listJobs(packageId).map { it.toDto() },
            artifacts = listArtifacts(packageId = packageId).map { it.toDto() },
            importRecords = listImportRecords(packageId).map { it.toDto() },
        )
    }

    fun deletePackageCascade(packageId: String) {
        listArtifacts(packageId = packageId).forEach { sqlClient.deleteById(PluginDeploymentArtifact::class, it.id) }
        listJobs(packageId).forEach { sqlClient.deleteById(PluginDeploymentJob::class, it.id) }
        listImportRecords(packageId).forEach { sqlClient.deleteById(PluginImportRecord::class, it.id) }
        listPresetBindings(packageId).forEach { sqlClient.deleteById(PluginPresetBinding::class, it.id) }
        listFiles(packageId).forEach { sqlClient.deleteById(PluginSourceFile::class, it.id) }
        sqlClient.deleteById(PluginPackage::class, packageId)
    }

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
        } else {
            markerPath.parent.createDirectories()
            if (!markerPath.exists()) {
                markerPath.writeText(
                    """
                    disabledBy=plugin-market
                    pluginId=${pluginPackage.pluginId}
                    reason=package-enabled-flag-false
                    """.trimIndent()
                )
            }
        }
    }

    fun uninstallManagedModule(pluginPackage: PluginPackage) {
        val moduleRoot = moduleRoot(pluginPackage)
        if (!moduleRoot.exists()) {
            return
        }
        val importedFromDisk = listImportRecords(pluginPackage.id).isNotEmpty()
        if (importedFromDisk) {
            disabledMarkerPath(pluginPackage).apply {
                parent.createDirectories()
                writeText(
                    """
                    disabledBy=plugin-market
                    pluginId=${pluginPackage.pluginId}
                    reason=package-deleted-from-db
                    """.trimIndent()
                )
            }
            return
        }
        moduleRoot.toFile().deleteRecursively()
    }

    fun discoverPluginModules(searchQuery: String? = null): List<PluginDiscoveryItemDto> {
        val pluginsRoot = Paths.get("apps", "kcloud", "plugins")
        if (!pluginsRoot.exists()) {
            return emptyList()
        }
        return discoverPluginModulesUnder(
            pluginsRoot = pluginsRoot,
            managedModuleDirs = listPackages().map { Paths.get(it.moduleDir).normalize().toString() }.toSet(),
            searchQuery = searchQuery,
        )
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

    fun renderManagedBlocks(dbPackages: List<PluginPackage>): ManagedIntegrationSnapshot {
        val fixedComposeDeps = listOf(
            """implementation(project(":apps:kcloud:plugins:mcu-console:ui"))""",
            """implementation(project(":apps:kcloud:plugins:system:ai-chat:ui"))""",
            """implementation(project(":apps:kcloud:plugins:system:config-center"))""",
            """implementation(project(":apps:kcloud:plugins:system:knowledge-base:ui"))""",
            """implementation(project(":apps:kcloud:plugins:system:rbac:ui"))""",
            """implementation(project(":apps:kcloud:plugins:system:plugin-market:ui"))""",
            """implementation(project(":apps:kcloud:plugins:vibepocket:ui"))""",
        )
        val fixedServerDeps = listOf(
            """implementation(project(":apps:kcloud:plugins:mcu-console:server"))""",
            """implementation(project(":apps:kcloud:plugins:system:ai-chat:server"))""",
            """implementation(project(":apps:kcloud:plugins:system:knowledge-base:server"))""",
            """implementation(project(":apps:kcloud:plugins:system:rbac:server"))""",
            """implementation(project(":apps:kcloud:plugins:system:plugin-market:server"))""",
            """implementation(project(":apps:kcloud:plugins:vibepocket:server"))""",
        )
        val fixedRouteTasks = listOf(
            """:apps:kcloud:plugins:mcu-console:ui:compileKotlinJvm""",
            """:apps:kcloud:plugins:system:ai-chat:ui:compileKotlinJvm""",
            """:apps:kcloud:plugins:system:config-center:compileKotlinJvm""",
            """:apps:kcloud:plugins:system:knowledge-base:ui:compileKotlinJvm""",
            """:apps:kcloud:plugins:system:plugin-market:ui:compileKotlinJvm""",
            """:apps:kcloud:plugins:system:rbac:ui:compileKotlinJvm""",
            """:apps:kcloud:plugins:vibepocket:ui:compileKotlinJvm""",
        )
        val fixedComposeModules = listOf(
            "site.addzero.kcloud.plugins.mcuconsole.McuConsoleComposeKoinModule::class",
            "site.addzero.kcloud.plugins.system.configcenter.ConfigCenterComposeKoinModule::class",
            "site.addzero.kcloud.plugins.system.aichat.AiChatKoinModule::class",
            "site.addzero.kcloud.plugins.system.knowledgebase.KnowledgeBaseKoinModule::class",
            "site.addzero.kcloud.plugins.system.pluginmarket.PluginMarketComposeKoinModule::class",
            "site.addzero.kcloud.plugins.system.rbac.RbacKoinModule::class",
            "site.addzero.kcloud.vibepocket.VibePocketComposeKoinModule::class",
            "site.addzero.kcloud.app.KCloudWorkbenchKoinModule::class",
        )
        val fixedDesktopModules = listOf(
            "site.addzero.kcloud.app.KCloudWorkbenchKoinModule::class",
        )
        val fixedServerModules = listOf(
            "site.addzero.kcloud.server.KCloudServerScanKoinModule::class",
            "site.addzero.kcloud.jimmer.di.JimmerKoinModule::class",
            "site.addzero.kcloud.plugins.mcuconsole.McuConsoleServerKoinModule::class",
            "site.addzero.configcenter.ktor.ConfigCenterKtorKoinModule::class",
            "site.addzero.starter.banner.BannerStarterKoinModule::class",
            "site.addzero.starter.flyway.FlywayStarterKoinModule::class",
            "site.addzero.starter.openapi.OpenApiStarterKoinModule::class",
            "site.addzero.starter.serialization.SerializationStarterKoinModule::class",
            "site.addzero.starter.statuspages.StatusPagesStarterKoinModule::class",
        )
        val fixedRouteImports = listOf(
            "site.addzero.configcenter.ktor.configCenterRoutes",
            "site.addzero.kcloud.plugins.mcuconsole.mcuConsoleRoutes",
            "site.addzero.kcloud.plugins.system.rbac.rbacRoutes",
            "site.addzero.kcloud.plugins.system.aichat.aiChatRoutes",
            "site.addzero.kcloud.plugins.system.knowledgebase.knowledgeBaseRoutes",
            "site.addzero.kcloud.plugins.system.pluginmarket.pluginMarketRoutes",
            "site.addzero.kcloud.vibepocket.routes.vibePocketRoutes",
        )
        val fixedRouteCalls = listOf(
            "configCenterRoutes()",
            "rbacRoutes()",
            "aiChatRoutes()",
            "knowledgeBaseRoutes()",
            "mcuConsoleRoutes()",
            "pluginMarketRoutes()",
            "vibePocketRoutes()",
        )

        val dynamicComposeDeps = dbPackages.map { packageEntity ->
            """implementation(project("${gradlePathFor(packageEntity)}"))"""
        }
        val dynamicServerDeps = dbPackages.filter { it.serverKoinModuleClass != null || it.routeRegistrarCall != null }
            .map { packageEntity -> """implementation(project("${gradlePathFor(packageEntity)}"))""" }
        val dynamicRouteTasks = dbPackages.map { packageEntity ->
            """"${gradlePathFor(packageEntity)}:compileKotlinJvm""""
        }
        val dynamicComposeModules = dbPackages.mapNotNull { it.composeKoinModuleClass?.let { fqcn -> "$fqcn::class" } }
        val dynamicServerModules = dbPackages.mapNotNull { it.serverKoinModuleClass?.let { fqcn -> "$fqcn::class" } }
        val dynamicRouteImports = dbPackages.mapNotNull { it.routeRegistrarImport?.takeIf(String::isNotBlank) }
        val dynamicRouteCalls = dbPackages.mapNotNull { it.routeRegistrarCall?.takeIf(String::isNotBlank) }

        return ManagedIntegrationSnapshot(
            composeDependencies = (fixedComposeDeps + dynamicComposeDeps).distinct().sorted(),
            serverDependencies = (fixedServerDeps + dynamicServerDeps).distinct().sorted(),
            sharedRouteTasks = (fixedRouteTasks + dynamicRouteTasks).distinct().sorted(),
            composeModules = (fixedComposeModules + dynamicComposeModules).distinct(),
            desktopModules = (fixedDesktopModules + dynamicComposeModules).distinct(),
            serverModules = (fixedServerModules + dynamicServerModules).distinct(),
            serverRouteImports = (fixedRouteImports + dynamicRouteImports).distinct().sorted(),
            serverRouteCalls = (fixedRouteCalls + dynamicRouteCalls).distinct(),
        )
    }

    fun updateManagedIntegrationFiles(snapshot: ManagedIntegrationSnapshot): ManagedIntegrationResult {
        return ManagedIntegrationResult(
            changedFiles = emptyList(),
            diffText = buildString {
                appendLine("KCloud shell aggregation is now handled by Gradle convention plugin `site.addzero.buildlogic.kmp.cmp-kcloud-aio`.")
                appendLine("No shell source files were rewritten; exporting the plugin module and rerunning Gradle is enough.")
                appendLine()
                appendLine("Compose 依赖：")
                snapshot.composeDependencies.forEach { appendLine(it) }
                appendLine()
                appendLine("Server 依赖：")
                snapshot.serverDependencies.forEach { appendLine(it) }
                appendLine()
                appendLine("共享路由任务：")
                snapshot.sharedRouteTasks.forEach { appendLine(it) }
                appendLine()
                appendLine("Server 路由导入：")
                snapshot.serverRouteImports.forEach { appendLine(it) }
                appendLine()
                appendLine("Server 路由聚合：")
                snapshot.serverRouteCalls.forEach { appendLine(it) }
            },
        )
    }

    private fun isTextFile(relativePath: String): Boolean {
        return listOf(".kt", ".kts", ".md", ".txt", ".yaml", ".yml", ".json", ".properties", ".conf")
            .any { relativePath.endsWith(it) }
    }

    private fun gradlePathFor(pluginPackage: PluginPackage): String {
        val group = pluginPackage.pluginGroup?.split("/")?.filter { it.isNotBlank() }.orEmpty()
        return listOf(":apps", "kcloud", "plugins", *group.toTypedArray(), pluginPackage.pluginId)
            .joinToString(":")
    }

    private fun ensureManagedRouteFile(path: Path) {
        if (path.exists()) {
            return
        }
        path.parent.createDirectories()
        path.writeText(
            """
            package site.addzero.kcloud

            import io.ktor.server.routing.Route
            // <managed:$SERVER_ROUTE_IMPORT_BLOCK:start>
            import site.addzero.configcenter.ktor.configCenterRoutes
            import site.addzero.kcloud.plugins.mcuconsole.mcuConsoleRoutes
            import site.addzero.kcloud.plugins.system.rbac.rbacRoutes
            import site.addzero.kcloud.plugins.system.aichat.aiChatRoutes
            import site.addzero.kcloud.plugins.system.knowledgebase.knowledgeBaseRoutes
            import site.addzero.kcloud.plugins.system.pluginmarket.pluginMarketRoutes
            import site.addzero.kcloud.vibepocket.routes.vibePocketRoutes
            // <managed:$SERVER_ROUTE_IMPORT_BLOCK:end>

            /**
             * 由插件市场维护的服务端插件路由聚合入口。
             */
            fun Route.registerKCloudPluginRoutes() {
                // <managed:$SERVER_ROUTE_BLOCK:start>
                configCenterRoutes()
                rbacRoutes()
                aiChatRoutes()
                knowledgeBaseRoutes()
                mcuConsoleRoutes()
                pluginMarketRoutes()
                vibePocketRoutes()
                // <managed:$SERVER_ROUTE_BLOCK:end>
            }
            """.trimIndent()
        )
    }

    private fun replaceManagedBlock(path: Path, blockName: String, body: String): Boolean {
        val content = path.readText()
        val startMarker = "// <managed:$blockName:start>"
        val endMarker = "// <managed:$blockName:end>"
        val startIndex = content.indexOf(startMarker)
        val endIndex = content.indexOf(endMarker)
        if (startIndex < 0 || endIndex < 0 || endIndex <= startIndex) {
            throw IllegalStateException("文件缺少受管区块 $blockName: $path")
        }
        val replaced = buildString {
            append(content.substring(0, startIndex + startMarker.length))
            appendLine()
            if (body.isNotBlank()) {
                append(body.trimEnd())
                appendLine()
            }
            append(content.substring(endIndex))
        }
        if (replaced == content) {
            return false
        }
        path.writeText(replaced)
        return true
    }
}

data class ManagedIntegrationSnapshot(
    val composeDependencies: List<String>,
    val serverDependencies: List<String>,
    val sharedRouteTasks: List<String>,
    val composeModules: List<String>,
    val desktopModules: List<String>,
    val serverModules: List<String>,
    val serverRouteImports: List<String>,
    val serverRouteCalls: List<String>,
)

data class ManagedIntegrationResult(
    val changedFiles: List<String>,
    val diffText: String,
)

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

internal fun PluginMarketSupport.toDto(pluginPackage: PluginPackage): PluginPackageDto {
    return PluginPackageDto(
        id = pluginPackage.id,
        pluginId = pluginPackage.pluginId,
        name = pluginPackage.name,
        pluginGroup = pluginPackage.pluginGroup,
        description = pluginPackage.description,
        version = pluginPackage.version,
        enabled = pluginPackage.enabled,
        moduleDir = pluginPackage.moduleDir,
        basePackage = pluginPackage.basePackage,
        managedByDb = pluginPackage.managedByDb,
        composeKoinModuleClass = pluginPackage.composeKoinModuleClass,
        serverKoinModuleClass = pluginPackage.serverKoinModuleClass,
        routeRegistrarImport = pluginPackage.routeRegistrarImport,
        routeRegistrarCall = pluginPackage.routeRegistrarCall,
        activationState = activationStateOf(pluginPackage),
        moduleInstalled = isModuleInstalled(pluginPackage),
        disabledByMarker = isDisabledByMarker(pluginPackage),
        createdAt = pluginPackage.createdAt.toString(),
        updatedAt = pluginPackage.updatedAt.toString(),
    )
}

internal fun PluginSourceFile.toDto(): PluginSourceFileDto {
    return PluginSourceFileDto(
        id = id,
        packageId = pluginPackage.id,
        relativePath = relativePath,
        content = content,
        contentHash = contentHash,
        fileGroup = fileGroup,
        readOnly = readOnly,
        orderIndex = orderIndex,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
}

internal fun PluginPresetBinding.toDto(): PluginPresetBindingDto {
    return PluginPresetBindingDto(
        id = id,
        packageId = pluginPackage.id,
        presetKind = PluginPresetKind.valueOf(presetKind),
        appliedAt = appliedAt.toString(),
    )
}

internal fun PluginDeploymentJob.toDto(): PluginDeploymentJobDto {
    return PluginDeploymentJobDto(
        id = id,
        packageId = pluginPackage.id,
        status = PluginDeploymentStatus.valueOf(status),
        exportedModuleDir = exportedModuleDir,
        buildCommand = buildCommand,
        stdoutText = stdoutText,
        stderrText = stderrText,
        summaryText = summaryText,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
}

internal fun PluginDeploymentArtifact.toDto(): PluginDeploymentArtifactDto {
    return PluginDeploymentArtifactDto(
        id = id,
        jobId = deploymentJob.id,
        relativePath = relativePath,
        absolutePath = absolutePath,
        contentHash = contentHash,
        createdAt = createdAt.toString(),
    )
}

internal fun PluginImportRecord.toDto(): PluginImportRecordDto {
    return PluginImportRecordDto(
        id = id,
        packageId = pluginPackage.id,
        sourceModuleDir = sourceModuleDir,
        sourceGradlePath = sourceGradlePath,
        importedAt = importedAt.toString(),
    )
}
