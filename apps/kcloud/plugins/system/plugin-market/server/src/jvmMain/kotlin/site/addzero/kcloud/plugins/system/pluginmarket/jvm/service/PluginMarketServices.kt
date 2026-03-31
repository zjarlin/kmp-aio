package site.addzero.kcloud.plugins.system.pluginmarket.jvm.service

import org.babyfish.jimmer.kt.new
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.configcenter.ConfigCenterService
import site.addzero.kcloud.plugins.system.pluginmarket.jvm.entity.*
import site.addzero.kcloud.plugins.system.pluginmarket.model.*
import site.addzero.kcloud.plugins.system.pluginmarket.service.*
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

private const val CONFIG_NAMESPACE = "kcloud-plugin-market"

@Single(
    binds = [
        PluginPackageService::class,
    ],
)
class PluginPackageServiceImpl(
    private val support: PluginMarketSupport,
    private val presetService: PluginPresetService,
) : PluginPackageService {
    override suspend fun create(request: CreatePluginPackageRequest): PluginPackageAggregateDto {
        require(request.pluginId.isNotBlank()) { "插件 ID 不能为空" }
        require(request.name.isNotBlank()) { "插件名称不能为空" }
        require(request.basePackage.isNotBlank()) { "基础包名不能为空" }
        if (support.listPackages().any { it.pluginId.equals(request.pluginId, ignoreCase = true) }) {
            throw IllegalArgumentException("插件 ID 已存在: ${request.pluginId}")
        }
        val now = support.now()
        val entity = new(PluginPackage::class).by {
            id = support.newId()
            pluginId = request.pluginId.trim()
            name = request.name.trim()
            description = request.description?.trim()?.ifBlank { null }
            version = request.version.trim().ifBlank { "0.1.0" }
            pluginGroup = request.pluginGroup?.trim()?.ifBlank { null }
            enabled = request.enabled
            moduleDir = request.moduleDir?.trim()?.ifBlank { defaultModuleDir(request) } ?: defaultModuleDir(request)
            basePackage = request.basePackage.trim()
            managedByDb = true
            composeKoinModuleClass = request.composeKoinModuleClass?.trim()?.ifBlank {
                defaultComposeKoinModuleClass(request)
            } ?: defaultComposeKoinModuleClass(request)
            serverKoinModuleClass = request.serverKoinModuleClass?.trim()?.ifBlank {
                defaultServerKoinModuleClass(request)
            } ?: defaultServerKoinModuleClass(request)
            routeRegistrarImport = request.routeRegistrarImport?.trim()?.ifBlank {
                defaultRouteRegistrarImport(request)
            } ?: defaultRouteRegistrarImport(request)
            routeRegistrarCall = request.routeRegistrarCall?.trim()?.ifBlank {
                defaultRouteRegistrarCall(request)
            } ?: defaultRouteRegistrarCall(request)
            createdAt = now
            updatedAt = now
        }
        val saved = support.sqlClient.save(entity).modifiedEntity
        presetService.applyPreset(saved.id, request.presetKind)
        return support.buildAggregate(saved.id)
    }

    override suspend fun list(search: PluginMarketSearchRequest): List<PluginPackageDto> {
        return support.listPackages()
            .filter { plugin ->
                !search.managedOnly || plugin.managedByDb
            }
            .filter { plugin ->
                val query = search.query
                query.isNullOrBlank() ||
                    plugin.pluginId.contains(query, ignoreCase = true) ||
                    plugin.name.contains(query, ignoreCase = true) ||
                    plugin.moduleDir.contains(query, ignoreCase = true)
            }
            .map { support.toDto(it) }
    }

    override suspend fun get(id: String): PluginPackageDto {
        return support.toDto(support.packageOrThrow(id))
    }

    override suspend fun aggregate(id: String): PluginPackageAggregateDto {
        return support.buildAggregate(id)
    }

    override suspend fun update(id: String, request: UpdatePluginPackageRequest): PluginPackageDto {
        val existing = support.packageOrThrow(id)
        val entity = new(PluginPackage::class).by {
            this.id = id
            pluginId = existing.pluginId
            name = request.name.trim()
            description = request.description?.trim()?.ifBlank { null }
            version = request.version.trim().ifBlank { "0.1.0" }
            pluginGroup = request.pluginGroup?.trim()?.ifBlank { null }
            enabled = request.enabled
            moduleDir = request.moduleDir.trim()
            basePackage = request.basePackage.trim()
            managedByDb = existing.managedByDb
            composeKoinModuleClass = request.composeKoinModuleClass?.trim()?.ifBlank { null }
            serverKoinModuleClass = request.serverKoinModuleClass?.trim()?.ifBlank { null }
            routeRegistrarImport = request.routeRegistrarImport?.trim()?.ifBlank { null }
            routeRegistrarCall = request.routeRegistrarCall?.trim()?.ifBlank { null }
            createdAt = existing.createdAt
            updatedAt = support.now()
        }
        val saved = support.sqlClient.save(entity).modifiedEntity
        support.syncEnabledMarker(saved)
        return support.toDto(saved)
    }

    override suspend fun enable(id: String): PluginPackageDto {
        val existing = support.packageOrThrow(id)
        if (existing.enabled) {
            support.syncEnabledMarker(existing)
            return support.toDto(existing)
        }
        return update(
            id,
            UpdatePluginPackageRequest(
                name = existing.name,
                pluginGroup = existing.pluginGroup,
                description = existing.description,
                version = existing.version,
                basePackage = existing.basePackage,
                moduleDir = existing.moduleDir,
                enabled = true,
                composeKoinModuleClass = existing.composeKoinModuleClass,
                serverKoinModuleClass = existing.serverKoinModuleClass,
                routeRegistrarImport = existing.routeRegistrarImport,
                routeRegistrarCall = existing.routeRegistrarCall,
            ),
        )
    }

    override suspend fun disable(id: String): PluginPackageDto {
        val existing = support.packageOrThrow(id)
        if (!existing.enabled) {
            support.syncEnabledMarker(existing)
            return support.toDto(existing)
        }
        return update(
            id,
            UpdatePluginPackageRequest(
                name = existing.name,
                pluginGroup = existing.pluginGroup,
                description = existing.description,
                version = existing.version,
                basePackage = existing.basePackage,
                moduleDir = existing.moduleDir,
                enabled = false,
                composeKoinModuleClass = existing.composeKoinModuleClass,
                serverKoinModuleClass = existing.serverKoinModuleClass,
                routeRegistrarImport = existing.routeRegistrarImport,
                routeRegistrarCall = existing.routeRegistrarCall,
            ),
        )
    }

    override suspend fun deleteCheck(id: String): PluginDeleteCheckResultDto {
        val aggregate = support.buildAggregate(id)
        return PluginDeleteCheckResultDto(
            id = id,
            canDelete = true,
            warnings = listOf(
                "将删除 ${aggregate.files.size} 个源码文件",
                "将删除 ${aggregate.jobs.size} 条部署记录",
            ),
        )
    }

    override suspend fun delete(id: String) {
        val pluginPackage = support.packageOrThrow(id)
        support.inTransaction {
            if (pluginPackage.managedByDb) {
                support.uninstallManagedModule(pluginPackage)
            }
            support.deletePackageCascade(id)
        }
    }

    override suspend fun uninstall(id: String) {
        delete(id)
    }

    private fun defaultModuleDir(request: CreatePluginPackageRequest): String {
        val groupSegment = request.pluginGroup?.trim()?.ifBlank { null }?.replace(".", "/")
        return buildString {
            append("apps/kcloud/plugins")
            if (groupSegment != null) {
                append("/")
                append(groupSegment)
            }
            append("/")
            append(request.pluginId.trim())
        }
    }

    private fun defaultComposeKoinModuleClass(request: CreatePluginPackageRequest): String {
        return "${defaultPluginPackage(request)}.${request.pluginId.trim().toPascalCase()}ComposeKoinModule"
    }

    private fun defaultServerKoinModuleClass(request: CreatePluginPackageRequest): String {
        return "${defaultPluginPackage(request)}.${request.pluginId.trim().toPascalCase()}ServerKoinModule"
    }

    private fun defaultRouteRegistrarImport(request: CreatePluginPackageRequest): String {
        return "${defaultPluginPackage(request)}.${request.pluginId.trim().camelCase()}Routes"
    }

    private fun defaultRouteRegistrarCall(request: CreatePluginPackageRequest): String {
        return "${request.pluginId.trim().camelCase()}Routes()"
    }

    private fun defaultPluginPackage(request: CreatePluginPackageRequest): String {
        return request.basePackage.trim().appendSegment(request.pluginId.trim().toPascalCase().lowercase())
    }
}

@Single(
    binds = [
        PluginSourceFileService::class,
    ],
)
class PluginSourceFileServiceImpl(
    private val support: PluginMarketSupport,
) : PluginSourceFileService {
    override suspend fun list(packageId: String): List<PluginSourceFileDto> {
        support.packageOrThrow(packageId)
        return support.listFiles(packageId).map { it.toDto() }
    }

    override suspend fun save(request: SavePluginSourceFileRequest): PluginSourceFileDto {
        require(request.relativePath.isNotBlank()) { "相对路径不能为空" }
        val pluginPackage = support.packageOrThrow(request.packageId)
        val existing = support.listFiles(request.packageId).firstOrNull { it.relativePath == request.relativePath }
        val now = support.now()
        val entity = new(PluginSourceFile::class).by {
            id = existing?.id ?: support.newId()
            this.pluginPackage = support.packageRef(pluginPackage.id)
            relativePath = request.relativePath.trim()
            content = request.content
            contentHash = support.hashContent(request.content)
            fileGroup = request.fileGroup.trim().ifBlank { "source" }
            readOnly = existing?.readOnly ?: false
            orderIndex = existing?.orderIndex ?: ((support.listFiles(request.packageId).maxOfOrNull { it.orderIndex } ?: -1) + 1)
            createdAt = existing?.createdAt ?: now
            updatedAt = now
        }
        return support.sqlClient.save(entity).modifiedEntity.toDto()
    }

    override suspend fun delete(fileId: String) {
        support.fileOrThrow(fileId)
        support.sqlClient.deleteById(PluginSourceFile::class, fileId)
    }
}

@Single(
    binds = [
        PluginPresetService::class,
    ],
)
class PluginPresetServiceImpl(
    private val support: PluginMarketSupport,
) : PluginPresetService {
    override suspend fun applyPreset(packageId: String, presetKind: PluginPresetKind): PluginPackageAggregateDto {
        val pluginPackage = support.packageOrThrow(packageId)
        val now = support.now()
        val packageDto = support.toDto(pluginPackage)
        val existingPaths = support.listFiles(packageId).associateBy { it.relativePath }
        presetFiles(packageDto, presetKind).forEachIndexed { index, (path, content, group) ->
            val file = existingPaths[path]
            val entity = new(PluginSourceFile::class).by {
                id = file?.id ?: support.newId()
                this.pluginPackage = support.packageRef(packageId)
                relativePath = path
                this.content = content
                contentHash = support.hashContent(content)
                fileGroup = group
                readOnly = false
                orderIndex = file?.orderIndex ?: index
                createdAt = file?.createdAt ?: now
                updatedAt = now
            }
            support.sqlClient.save(entity)
        }
        val binding = new(PluginPresetBinding::class).by {
            id = support.newId()
            this.pluginPackage = support.packageRef(packageId)
            this.presetKind = presetKind.name
            appliedAt = now
        }
        support.sqlClient.save(binding)
        return support.buildAggregate(packageId)
    }

    private fun presetFiles(
        pluginPackage: PluginPackageDto,
        presetKind: PluginPresetKind,
    ): List<PresetFile> {
        val packageRoot = pluginPackage.basePackage.appendSegment(pluginPackage.pluginId.toPascalCase().lowercase())
        val screenPackage = "$packageRoot.screen"
        val serverPackage = packageRoot
        val classPrefix = pluginPackage.pluginId.toPascalCase()
        val routePath = buildString {
            append(pluginPackage.pluginGroup?.replace("/", "/")?.let { "system/$it/" } ?: "system/")
            append(pluginPackage.pluginId)
        }.replace("system/system/", "system/")
        val title = when (presetKind) {
            PluginPresetKind.BLANK -> "空白插件"
            PluginPresetKind.TOOL -> "工具插件"
            PluginPresetKind.ADMIN -> "管理后台插件"
        }
        return listOf(
            PresetFile(
                "README.md",
                """
                # ${pluginPackage.name}

                ${pluginPackage.description ?: "由插件市场生成的 KCloud 插件模块。"}
                """.trimIndent(),
                "meta",
            ),
            PresetFile(
                "build.gradle.kts",
                buildGradleTemplate(),
                "meta",
            ),
            PresetFile(
                "src/commonMain/kotlin/${packageRoot.toPath()}/${classPrefix}ComposeKoinModule.kt",
                """
                package $serverPackage

                import org.koin.core.annotation.ComponentScan
                import org.koin.core.annotation.Module

                @Module
                @ComponentScan("$serverPackage")
                class ${classPrefix}ComposeKoinModule
                """.trimIndent(),
                "common",
            ),
            PresetFile(
                "src/commonMain/kotlin/${screenPackage.toPath()}/${classPrefix}Screen.kt",
                """
                package $screenPackage

                import androidx.compose.foundation.layout.Arrangement
                import androidx.compose.foundation.layout.Column
                import androidx.compose.foundation.layout.fillMaxSize
                import androidx.compose.foundation.layout.padding
                import androidx.compose.material3.MaterialTheme
                import androidx.compose.material3.Text
                import androidx.compose.runtime.Composable
                import androidx.compose.ui.Modifier
                import androidx.compose.ui.unit.dp
                import site.addzero.annotation.Route
                import site.addzero.annotation.RoutePlacement
                import site.addzero.annotation.RouteScene

                @Route(
                    title = "$title",
                    routePath = "$routePath",
                    icon = "Apps",
                    order = 50.0,
                    placement = RoutePlacement(
                        scene = RouteScene(
                            name = "${pluginPackage.name}",
                            icon = "Apps",
                            order = 500,
                        ),
                        defaultInScene = true,
                    ),
                )
                @Composable
                fun ${classPrefix}Screen() {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("${pluginPackage.name}", style = MaterialTheme.typography.titleMedium)
                        Text("$title 页面已经接入路由聚合。")
                    }
                }
                """.trimIndent(),
                "common",
            ),
            PresetFile(
                "src/jvmMain/kotlin/${serverPackage.toPath()}/${classPrefix}ServerKoinModule.kt",
                """
                package $serverPackage

                import org.koin.core.annotation.Module

                @Module
                class ${classPrefix}ServerKoinModule
                """.trimIndent(),
                "jvm",
            ),
            PresetFile(
                "src/jvmMain/kotlin/${serverPackage.toPath()}/${classPrefix}Routes.kt",
                """
                package $serverPackage

                import io.ktor.server.routing.Route
                import $serverPackage.routes.generated.springktor.registerGeneratedSpringRoutes

                /**
                 * ${pluginPackage.name} 服务端路由入口。
                 */
                fun Route.${pluginPackage.pluginId.camelCase()}Routes() {
                    registerGeneratedSpringRoutes()
                }
                """.trimIndent(),
                "jvm",
            ),
            PresetFile(
                "src/jvmMain/kotlin/${serverPackage.toPath()}/routes/${classPrefix}RouteHandlers.kt",
                """
                package $serverPackage.routes

                import org.springframework.web.bind.annotation.GetMapping

                /**
                 * ${pluginPackage.name} 健康检查路由。
                 */
                @GetMapping("/api/${pluginPackage.pluginId}/health")
                fun ${pluginPackage.pluginId.camelCase()}Health(): Map<String, String> {
                    return mapOf("pluginId" to "${pluginPackage.pluginId}", "status" to "ok")
                }
                """.trimIndent(),
                "jvm",
            ),
        )
    }

    private fun buildGradleTemplate(): String {
        return """
            plugins {
                id("site.addzero.buildlogic.kmp.cmp-lib")
                id("site.addzero.buildlogic.kmp.kmp-koin")
                id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
            }

            val libs = versionCatalogs.named("libs")
            val sharedSourceDir = project(":apps:kcloud:shared")
                .extensions
                .getByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()
                .sourceSets
                .getByName("commonMain")
                .kotlin
                .srcDirs
                .first()
                .absolutePath
            val routeOwnerModuleDir = project(":apps:kcloud:composeApp")
                .extensions
                .getByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()
                .sourceSets
                .getByName("commonMain")
                .kotlin
                .srcDirs
                .first()
                .absolutePath

            ksp {
                arg("sharedSourceDir", sharedSourceDir)
                arg("routeGenPkg", "site.addzero.generated")
                arg("routeOwnerModule", routeOwnerModuleDir)
                arg("routeModuleKey", project.path)
            }

            dependencies {
                add("kspCommonMainMetadata", libs.findLibrary("site-addzero-route-processor").get())
                add("kspJvm", libs.findLibrary("site-addzero-route-processor").get())
                add("kspJvm", libs.findLibrary("spring2ktor-server-processor").get())
            }

            kotlin {
                sourceSets {
                    commonMain.dependencies {
                        implementation(project(":lib:compose:scaffold-spi"))
                        implementation(libs.findLibrary("site-addzero-route-core").get())
                    }
                    jvmMain.dependencies {
                        implementation(libs.findLibrary("io-ktor-ktor-server-core-jvm").get())
                        implementation(libs.findLibrary("spring2ktor-server-core").get())
                        compileOnly(libs.findLibrary("org-springframework-spring-web").get())
                    }
                }
            }
        """.trimIndent()
    }
}

@Single(
    binds = [
        PluginDiscoveryService::class,
    ],
)
class PluginDiscoveryServiceImpl(
    private val support: PluginMarketSupport,
) : PluginDiscoveryService {
    override suspend fun discover(search: PluginMarketSearchRequest): List<PluginDiscoveryItemDto> {
        return support.discoverPluginModules(search.query)
    }

    override suspend fun importDiscovered(request: ImportDiscoveredPluginRequest): PluginPackageAggregateDto {
        val discovery = support.discoverPluginModules()
            .firstOrNull { it.discoveryId == request.discoveryId }
            ?: throw NoSuchElementException("未找到发现模块: ${request.discoveryId}")
        require(discovery.issues.isEmpty()) {
            "发现模块结构不完整，暂不支持导入：${discovery.issues.joinToString("；")}"
        }
        require(
            support.listPackages().none { it.pluginId.equals(request.managedPluginId.trim(), ignoreCase = true) }
        ) { "插件 ID 已存在: ${request.managedPluginId.trim()}" }
        val now = support.now()
        val packageId = support.newId()
        val pluginPackage = new(PluginPackage::class).by {
            id = packageId
            pluginId = request.managedPluginId.trim()
            name = request.managedName.trim()
            description = "从磁盘导入: ${discovery.moduleDir}"
            version = "0.1.0"
            pluginGroup = discovery.pluginGroup
            enabled = true
            moduleDir = discovery.moduleDir
            basePackage = discovery.packageName?.substringBeforeLast(".").orEmpty().ifBlank {
                "site.addzero.kcloud.plugins.${request.managedPluginId.trim().toPascalCase().lowercase()}"
            }
            managedByDb = true
            composeKoinModuleClass = discovery.composeKoinModuleClass
            serverKoinModuleClass = discovery.serverKoinModuleClass
            routeRegistrarImport = discovery.routeRegistrarImport
            routeRegistrarCall = discovery.routeRegistrarCall
            createdAt = now
            updatedAt = now
        }
        support.sqlClient.save(pluginPackage)
        support.collectImportableFiles(Paths.get(discovery.moduleDir)).forEachIndexed { index, (relativePath, content) ->
            support.sqlClient.save(
                new(PluginSourceFile::class).by {
                    id = support.newId()
                    this.pluginPackage = support.packageRef(packageId)
                    this.relativePath = relativePath
                    this.content = content
                    contentHash = support.hashContent(content)
                    fileGroup = inferFileGroup(relativePath)
                    readOnly = false
                    orderIndex = index
                    createdAt = now
                    updatedAt = now
                }
            )
        }
        support.sqlClient.save(
            new(PluginImportRecord::class).by {
                id = support.newId()
                this.pluginPackage = support.packageRef(packageId)
                sourceModuleDir = discovery.moduleDir
                sourceGradlePath = discovery.gradlePath
                importedAt = now
            }
        )
        return support.buildAggregate(packageId)
    }

    private fun inferFileGroup(path: String): String {
        return when {
            path.startsWith("src/commonMain") -> "common"
            path.startsWith("src/jvmMain") -> "jvm"
            else -> "meta"
        }
    }
}

@Single(
    binds = [
        PluginMarketConfigService::class,
    ],
)
class PluginMarketConfigServiceImpl(
    private val configCenterService: ConfigCenterService,
) : PluginMarketConfigService {
    override suspend fun read(): PluginMarketConfigDto {
        ensureDefaults()
        return PluginMarketConfigDto(
            exportRootDir = readValue("export.rootDir") ?: "apps/kcloud/plugins",
            gradleCommand = readValue("gradle.command") ?: "./gradlew",
            gradleTasks = readValue("gradle.tasks")
                ?.split(" ")
                ?.map(String::trim)
                ?.filter(String::isNotBlank)
                ?: defaultGradleTasks(),
            javaHome = readValue("java.home"),
            environmentLines = readValue("environment.lines")
                ?.lineSequence()
                ?.map(String::trim)
                ?.filter(String::isNotBlank)
                ?.toList()
                ?: emptyList(),
            autoBuildEnabled = readValue("autoBuild.enabled")?.toBooleanStrictOrNull() ?: false,
        )
    }

    override suspend fun update(request: UpdatePluginMarketConfigRequest): PluginMarketConfigDto {
        saveValue("export.rootDir", request.exportRootDir, "插件源码导出根目录")
        saveValue("gradle.command", request.gradleCommand, "插件验证构建命令")
        saveValue("gradle.tasks", request.gradleTasks.joinToString(" "), "插件验证构建任务列表")
        saveValue("java.home", request.javaHome.orEmpty(), "插件验证构建 JAVA_HOME")
        saveValue("environment.lines", request.environmentLines.joinToString("\n"), "插件验证构建环境变量")
        saveValue("autoBuild.enabled", request.autoBuildEnabled.toString(), "插件导出后是否自动触发验证构建")
        return read()
    }

    private suspend fun ensureDefaults() {
        saveIfMissing("export.rootDir", "apps/kcloud/plugins", "插件源码导出根目录")
        saveIfMissing("gradle.command", "./gradlew", "插件验证构建命令")
        saveIfMissing("gradle.tasks", defaultGradleTasks().joinToString(" "), "插件验证构建任务列表")
        saveIfMissing("java.home", "", "插件验证构建 JAVA_HOME")
        saveIfMissing("environment.lines", "", "插件验证构建环境变量")
        saveIfMissing("autoBuild.enabled", "false", "插件导出后是否自动触发验证构建")
    }

    private suspend fun saveIfMissing(key: String, value: String, description: String) {
        if (readValue(key) == null) {
            saveValue(key, value, description)
        }
    }

    private suspend fun saveValue(key: String, value: String, _description: String) {
        configCenterService.writeValue(
            namespace = CONFIG_NAMESPACE,
            key = key,
            value = value,
        )
    }

    private fun readValue(
        key: String,
    ): String? {
        return configCenterService.readValue(CONFIG_NAMESPACE, key).value
    }

    private fun defaultGradleTasks(): List<String> {
        return listOf(
            ":apps:kcloud:shared:compileCommonMainKotlinMetadata",
            ":apps:kcloud:composeApp:compileKotlinJvm",
            ":apps:kcloud:server:compileKotlinJvm",
        )
    }
}

@Single(
    binds = [
        PluginDeploymentService::class,
    ],
)
class PluginDeploymentServiceImpl(
    private val support: PluginMarketSupport,
    private val configService: PluginMarketConfigService,
    private val commandRunner: PluginBuildCommandRunner,
) : PluginDeploymentService {
    override suspend fun deploy(request: DeployPluginPackageRequest): PluginDeploymentJobDto {
        val pluginPackage = support.packageOrThrow(request.packageId)
        val config = configService.read()
        val moduleRoot = resolveModuleRoot(pluginPackage.moduleDir, config.exportRootDir)
        val files = support.listFiles(pluginPackage.id)
        val now = support.now()

        moduleRoot.createDirectories()
        files.forEach { file ->
            val targetFile = moduleRoot.resolve(file.relativePath)
            targetFile.parent?.createDirectories()
            targetFile.writeText(file.content)
        }
        support.syncEnabledMarker(pluginPackage)
        val integration = support.renderManagedBlocks(
            support.listPackages().filter { it.enabled },
        )
        val integrationResult = support.updateManagedIntegrationFiles(integration)

        val buildRequested = request.runBuild ?: config.autoBuildEnabled
        val status = if (buildRequested) PluginDeploymentStatus.BUILDING else PluginDeploymentStatus.EXPORTED
        val job = new(PluginDeploymentJob::class).by {
            id = support.newId()
            this.pluginPackage = support.packageRef(pluginPackage.id)
            this.status = status.name
            exportedModuleDir = moduleRoot.toString()
            buildCommand = null
            stdoutText = null
            stderrText = null
            summaryText = buildString {
                appendLine("已导出 ${files.size} 个文件到 $moduleRoot")
                appendLine()
                append(integrationResult.diffText)
            }
            createdAt = now
            updatedAt = now
        }
        val savedJob = support.sqlClient.save(job).modifiedEntity
        files.forEach { file ->
            support.sqlClient.save(
                new(PluginDeploymentArtifact::class).by {
                    id = support.newId()
                    this.deploymentJob = support.jobRef(savedJob.id)
                    relativePath = file.relativePath
                    absolutePath = moduleRoot.resolve(file.relativePath).toString()
                    contentHash = file.contentHash
                    createdAt = now
                }
            )
        }
        if (buildRequested) {
            return runBuild(RunPluginBuildRequest(pluginPackage.id))
        }
        return savedJob.toDto()
    }

    override suspend fun runBuild(request: RunPluginBuildRequest): PluginDeploymentJobDto {
        val pluginPackage = support.packageOrThrow(request.packageId)
        val config = configService.read()
        val command = buildString {
            append(config.gradleCommand)
            if (config.gradleTasks.isNotEmpty()) {
                append(" ")
                append(config.gradleTasks.joinToString(" "))
            }
        }
        val result = commandRunner.run(
            commandLine = command,
            workingDirectory = Paths.get(".").toAbsolutePath().normalize().toFile(),
            environment = parseEnvironmentLines(config.environmentLines),
            javaHome = config.javaHome,
        )
        val now = support.now()
        val job = new(PluginDeploymentJob::class).by {
            id = support.newId()
            this.pluginPackage = support.packageRef(pluginPackage.id)
            status = if (result.exitCode == 0) {
                PluginDeploymentStatus.RESTART_REQUIRED.name
            } else {
                PluginDeploymentStatus.FAILED.name
            }
            exportedModuleDir = pluginPackage.moduleDir
            buildCommand = result.command
            stdoutText = result.stdout
            stderrText = result.stderr
            summaryText = if (result.exitCode == 0) {
                "Gradle 构建完成，等待重启 KCloud 生效"
            } else {
                "Gradle 构建失败，退出码 ${result.exitCode}"
            }
            createdAt = now
            updatedAt = now
        }
        return support.sqlClient.save(job).modifiedEntity.toDto()
    }

    override suspend fun listJobs(packageId: String?): List<PluginDeploymentJobDto> {
        return support.listJobs(packageId).map { it.toDto() }
    }

    private fun resolveModuleRoot(moduleDir: String, exportRootDir: String): Path {
        val modulePath = Paths.get(moduleDir)
        if (modulePath.isAbsolute) {
            return modulePath
        }
        val exportRoot = Paths.get(exportRootDir)
        if (moduleDir.startsWith("apps/")) {
            return Paths.get(moduleDir)
        }
        return exportRoot.resolve(moduleDir.substringAfterLast("plugins/"))
    }

    private fun parseEnvironmentLines(lines: List<String>): Map<String, String> {
        return lines.mapNotNull { line ->
            val index = line.indexOf("=")
            if (index <= 0) {
                null
            } else {
                line.substring(0, index).trim() to line.substring(index + 1).trim()
            }
        }.toMap()
    }
}

private data class PresetFile(
    val path: String,
    val content: String,
    val group: String,
)

private fun String.toPascalCase(): String {
    return split("-", "_", ".", "/")
        .filter { it.isNotBlank() }
        .joinToString("") { part -> part.replaceFirstChar { it.uppercase() } }
}

private fun String.camelCase(): String {
    val pascal = toPascalCase()
    return pascal.replaceFirstChar { it.lowercase() }
}

private fun String.appendSegment(segment: String): String {
    return if (isBlank()) segment else "$this.$segment"
}

private fun String.toPath(): String = replace(".", "/")
