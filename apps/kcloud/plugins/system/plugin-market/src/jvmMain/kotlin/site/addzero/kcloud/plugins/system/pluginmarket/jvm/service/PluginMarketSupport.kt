package site.addzero.kcloud.plugins.system.pluginmarket.jvm.service

import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.PluginDeploymentArtifact
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.PluginDeploymentJob
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.PluginImportRecord
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.PluginPackage
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.PluginPresetBinding
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.PluginSourceFile
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.by
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDeploymentArtifactDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDeploymentJobDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDeploymentStatus
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDiscoveryItemDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginImportRecordDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPackageAggregateDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPackageDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPresetBindingDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginPresetKind
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginSourceFileDto
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.UUID
import javax.sql.DataSource
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.walk
import kotlin.io.path.writeText

private const val COMPOSE_DEP_BLOCK = "plugin-market-compose-deps"
private const val SERVER_DEP_BLOCK = "plugin-market-server-deps"
private const val SHARED_ROUTE_TASK_BLOCK = "plugin-market-route-tasks"
private const val COMPOSE_KOIN_BLOCK = "plugin-market-compose-koin"
private const val DESKTOP_KOIN_BLOCK = "plugin-market-desktop-koin"
private const val SERVER_KOIN_BLOCK = "plugin-market-server-koin"
private const val SERVER_ROUTE_IMPORT_BLOCK = "plugin-market-server-route-imports"
private const val SERVER_ROUTE_BLOCK = "plugin-market-server-routes"

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
            pluginPackage = pluginPackage.toDto(),
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

    fun discoverPluginModules(searchQuery: String? = null): List<PluginDiscoveryItemDto> {
        val pluginsRoot = Paths.get("apps", "kcloud", "plugins")
        if (!pluginsRoot.exists()) {
            return emptyList()
        }
        val managedDirs = listPackages().associateBy { Paths.get(it.moduleDir).normalize().toString() }
        return pluginsRoot.walk()
            .filter { path -> path != pluginsRoot && path.isDirectory() && Files.exists(path.resolve("build.gradle.kts")) }
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
                    managedByDb = managedDirs.containsKey(moduleDirString),
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
            """implementation(project(":apps:kcloud:plugins:mcu-console"))""",
            """implementation(project(":apps:kcloud:plugins:system:ai-chat"))""",
            """implementation(project(":apps:kcloud:plugins:system:config-center"))""",
            """implementation(project(":apps:kcloud:plugins:system:knowledge-base"))""",
            """implementation(project(":apps:kcloud:plugins:system:rbac"))""",
            """implementation(project(":apps:kcloud:plugins:system:plugin-market"))""",
            """implementation(project(":apps:kcloud:plugins:vibepocket"))""",
        )
        val fixedServerDeps = listOf(
            """implementation(project(":apps:kcloud:plugins:mcu-console"))""",
            """implementation(project(":apps:kcloud:plugins:system:ai-chat"))""",
            """implementation(project(":apps:kcloud:plugins:system:knowledge-base"))""",
            """implementation(project(":apps:kcloud:plugins:system:rbac"))""",
            """implementation(project(":apps:kcloud:plugins:system:plugin-market"))""",
            """implementation(project(":apps:kcloud:plugins:vibepocket"))""",
        )
        val fixedRouteTasks = listOf(
            """:apps:kcloud:plugins:mcu-console:kspCommonMainKotlinMetadata""",
            """:apps:kcloud:plugins:system:ai-chat:kspCommonMainKotlinMetadata""",
            """:apps:kcloud:plugins:system:config-center:kspCommonMainKotlinMetadata""",
            """:apps:kcloud:plugins:system:knowledge-base:kspCommonMainKotlinMetadata""",
            """:apps:kcloud:plugins:system:plugin-market:kspCommonMainKotlinMetadata""",
            """:apps:kcloud:plugins:system:rbac:kspCommonMainKotlinMetadata""",
            """:apps:kcloud:plugins:vibepocket:kspCommonMainKotlinMetadata""",
        )
        val fixedComposeModules = listOf(
            "site.addzero.kcloud.app.KCloudWorkbenchKoinModule::class",
            "site.addzero.kcloud.plugins.mcuconsole.McuConsoleComposeKoinModule::class",
            "site.addzero.kcloud.plugins.system.aichat.AiChatKoinModule::class",
            "site.addzero.kcloud.plugins.system.configcenter.ConfigCenterComposeKoinModule::class",
            "site.addzero.kcloud.plugins.system.knowledgebase.KnowledgeBaseKoinModule::class",
            "site.addzero.kcloud.plugins.system.pluginmarket.PluginMarketComposeKoinModule::class",
            "site.addzero.kcloud.plugins.rbac.RbacKoinModule::class",
            "site.addzero.vibepocket.VibePocketKoinModule::class",
        )
        val fixedDesktopModules = listOf(
            "site.addzero.kcloud.app.KCloudWorkbenchKoinModule::class",
            "site.addzero.kcloud.plugins.mcuconsole.McuConsoleComposeKoinModule::class",
            "site.addzero.kcloud.plugins.system.configcenter.ConfigCenterComposeKoinModule::class",
            "site.addzero.kcloud.plugins.system.pluginmarket.PluginMarketComposeKoinModule::class",
        )
        val fixedServerModules = listOf(
            "site.addzero.vibepocket.VibePocketKoinModule::class",
            "site.addzero.kcloud.plugins.mcuconsole.McuConsoleServerKoinModule::class",
            "site.addzero.kcloud.plugins.rbac.RbacKoinModule::class",
            "site.addzero.kcloud.plugins.system.aichat.AiChatKoinModule::class",
            "site.addzero.kcloud.plugins.system.knowledgebase.KnowledgeBaseKoinModule::class",
            "site.addzero.configcenter.ktor.ConfigCenterKtorKoinModule::class",
            "site.addzero.kcloud.plugins.system.pluginmarket.PluginMarketServerKoinModule::class",
        )
        val fixedRouteImports = listOf(
            "site.addzero.configcenter.ktor.configCenterRoutes",
            "site.addzero.kcloud.plugins.mcuconsole.mcuConsoleRoutes",
            "site.addzero.kcloud.plugins.rbac.rbacRoutes",
            "site.addzero.kcloud.plugins.system.aichat.aiChatRoutes",
            "site.addzero.kcloud.plugins.system.knowledgebase.knowledgeBaseRoutes",
            "site.addzero.kcloud.plugins.system.pluginmarket.pluginMarketRoutes",
            "site.addzero.vibepocket.routes.vibePocketRoutes",
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
            """"${gradlePathFor(packageEntity)}:kspCommonMainKotlinMetadata""""
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
        val composeBuild = Paths.get("apps", "kcloud", "composeApp", "build.gradle.kts")
        val serverBuild = Paths.get("apps", "kcloud", "server", "build.gradle.kts")
        val sharedBuild = Paths.get("apps", "kcloud", "shared", "build.gradle.kts")
        val composeKoin = Paths.get("apps", "kcloud", "composeApp", "src", "commonMain", "kotlin", "site", "addzero", "kcloud", "KCloudComposeKoinApplication.kt")
        val desktopKoin = Paths.get("apps", "kcloud", "composeApp", "src", "commonMain", "kotlin", "site", "addzero", "kcloud", "KCloudDesktopSupplementKoinApplication.kt")
        val serverKoin = Paths.get("apps", "kcloud", "server", "src", "jvmMain", "kotlin", "site", "addzero", "kcloud", "server", "KCloudServerStarterKoinApplication.kt")
        val serverRoutes = Paths.get("apps", "kcloud", "server", "src", "jvmMain", "kotlin", "site", "addzero", "kcloud", "server", "GeneratedPluginServerRoutes.kt")
        ensureManagedRouteFile(serverRoutes)

        val changedFiles = buildList {
            if (replaceManagedBlock(composeBuild, COMPOSE_DEP_BLOCK, snapshot.composeDependencies.joinToString("\n            "))) add(composeBuild)
            if (replaceManagedBlock(serverBuild, SERVER_DEP_BLOCK, snapshot.serverDependencies.joinToString("\n            "))) add(serverBuild)
            if (replaceManagedBlock(sharedBuild, SHARED_ROUTE_TASK_BLOCK, snapshot.sharedRouteTasks.joinToString(",\n    "))) add(sharedBuild)
            if (replaceManagedBlock(composeKoin, COMPOSE_KOIN_BLOCK, snapshot.composeModules.joinToString(",\n        "))) add(composeKoin)
            if (replaceManagedBlock(desktopKoin, DESKTOP_KOIN_BLOCK, snapshot.desktopModules.joinToString(",\n        "))) add(desktopKoin)
            if (replaceManagedBlock(serverKoin, SERVER_KOIN_BLOCK, snapshot.serverModules.joinToString(",\n        "))) add(serverKoin)
            if (replaceManagedBlock(serverRoutes, SERVER_ROUTE_IMPORT_BLOCK, snapshot.serverRouteImports.joinToString("\n"))) add(serverRoutes)
            if (replaceManagedBlock(serverRoutes, SERVER_ROUTE_BLOCK, snapshot.serverRouteCalls.joinToString("\n        "))) add(serverRoutes)
        }

        return ManagedIntegrationResult(
            changedFiles = changedFiles.map { it.toString() },
            diffText = buildString {
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
            import site.addzero.kcloud.plugins.rbac.rbacRoutes
            import site.addzero.kcloud.plugins.system.aichat.aiChatRoutes
            import site.addzero.kcloud.plugins.system.knowledgebase.knowledgeBaseRoutes
            import site.addzero.kcloud.plugins.system.pluginmarket.pluginMarketRoutes
            import site.addzero.vibepocket.routes.vibePocketRoutes
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

internal fun PluginPackage.toDto(): PluginPackageDto {
    return PluginPackageDto(
        id = id,
        pluginId = pluginId,
        name = name,
        pluginGroup = pluginGroup,
        description = description,
        version = version,
        enabled = enabled,
        moduleDir = moduleDir,
        basePackage = basePackage,
        managedByDb = managedByDb,
        composeKoinModuleClass = composeKoinModuleClass,
        serverKoinModuleClass = serverKoinModuleClass,
        routeRegistrarImport = routeRegistrarImport,
        routeRegistrarCall = routeRegistrarCall,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
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
