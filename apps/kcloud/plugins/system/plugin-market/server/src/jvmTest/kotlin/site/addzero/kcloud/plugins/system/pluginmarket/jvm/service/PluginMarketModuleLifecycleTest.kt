package site.addzero.kcloud.plugins.system.pluginmarket.jvm.service

import kotlinx.coroutines.runBlocking
import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.runtime.ConnectionManager
import org.sqlite.SQLiteDataSource
import site.addzero.kcloud.jimmer.di.SqliteInstantScalarProvider
import site.addzero.kcloud.jimmer.di.SqliteLocalDateTimeScalarProvider
import site.addzero.kcloud.jimmer.support.JimmerSqlScriptSupport
import site.addzero.kcloud.plugins.system.pluginmarket.model.CreatePluginPackageRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.DeployPluginPackageRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginDeploymentStatus
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginActivationState
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginMarketConfigDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.SavePluginSourceFileRequest
import site.addzero.kcloud.plugins.system.pluginmarket.model.UpdatePluginMarketConfigRequest
import site.addzero.kcloud.plugins.system.pluginmarket.service.PluginMarketConfigService
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.sql.DataSource
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PluginMarketModuleLifecycleTest {
    @Test
    fun activationStateAndMarkerActionsStayConsistent() = withPluginMarketFixture { fixture ->
        val moduleDir = fixture.tempRoot.resolve("plugins/system/sample-plugin")
        val created = fixture.packageService.create(
            CreatePluginPackageRequest(
                pluginId = "sample-plugin",
                name = "示例插件",
                pluginGroup = "system",
                basePackage = "site.addzero.kcloud.plugins.system",
                moduleDir = moduleDir.toString(),
            ),
        ).pluginPackage

        assertEquals(PluginActivationState.NOT_INSTALLED, created.activationState)
        assertFalse(created.moduleInstalled)

        moduleDir.createDirectories()
        val enabled = fixture.packageService.enable(created.id)
        assertEquals(PluginActivationState.ENABLED, enabled.activationState)
        assertTrue(enabled.moduleInstalled)
        assertTrue(fixture.markerPath(enabled.id).notExists())

        val disabled = fixture.packageService.disable(created.id)
        assertEquals(PluginActivationState.DISABLED, disabled.activationState)
        assertTrue(fixture.markerPath(disabled.id).exists())
        assertTrue(fixture.markerPath(disabled.id).readText().contains("reason=package-enabled-flag-false"))

        val reEnabled = fixture.packageService.enable(created.id)
        assertEquals(PluginActivationState.ENABLED, reEnabled.activationState)
        assertTrue(fixture.markerPath(reEnabled.id).notExists())
    }

    @Test
    fun uninstallDeletesDbManagedModuleDirectory() = withPluginMarketFixture { fixture ->
        val moduleDir = fixture.tempRoot.resolve("plugins/system/db-managed")
        val created = fixture.packageService.create(
            CreatePluginPackageRequest(
                pluginId = "db-managed",
                name = "数据库托管插件",
                pluginGroup = "system",
                basePackage = "site.addzero.kcloud.plugins.system",
                moduleDir = moduleDir.toString(),
            ),
        ).pluginPackage

        moduleDir.resolve("src/commonMain/kotlin/Test.kt").apply {
            parent.createDirectories()
            writeText("package demo")
        }

        fixture.packageService.uninstall(created.id)

        assertTrue(moduleDir.notExists())
        assertTrue(fixture.catalog.listPackages().none { it.id == created.id })
    }

    @Test
    fun uninstallImportedPluginKeepsDirectoryAndWritesDisableMarker() = withPluginMarketFixture { fixture ->
        val moduleDir = fixture.tempRoot.resolve("plugins/system/imported")
        val created = fixture.packageService.create(
            CreatePluginPackageRequest(
                pluginId = "imported-plugin",
                name = "导入插件",
                pluginGroup = "system",
                basePackage = "site.addzero.kcloud.plugins.system",
                moduleDir = moduleDir.toString(),
            ),
        ).pluginPackage

        moduleDir.resolve("README.md").apply {
            parent.createDirectories()
            writeText("# imported")
        }
        fixture.attachImportRecord(created.id, moduleDir.toString())

        fixture.packageService.uninstall(created.id)

        val markerPath = moduleDir.resolve(".kcloud-plugin-disabled")
        assertTrue(moduleDir.exists())
        assertTrue(markerPath.exists())
        assertTrue(markerPath.readText().contains("reason=package-deleted-from-db"))
        assertTrue(fixture.catalog.listPackages().none { it.id == created.id })
    }

    @Test
    fun deployCreatesManagedModuleAndRefreshesActivationState() = withPluginMarketFixture { fixture ->
        val enabledModuleDir = fixture.tempRoot.resolve("plugins/system/exported-enabled")
        val enabledPackage = fixture.packageService.create(
            CreatePluginPackageRequest(
                pluginId = "exported-enabled",
                name = "已启用导出插件",
                pluginGroup = "system",
                basePackage = "site.addzero.kcloud.plugins.system",
                moduleDir = enabledModuleDir.toString(),
                enabled = true,
            ),
        ).pluginPackage
        fixture.fileService.save(
            SavePluginSourceFileRequest(
                packageId = enabledPackage.id,
                relativePath = "README.md",
                content = "# enabled",
                fileGroup = "meta",
            ),
        )

        val enabledJob = fixture.deploymentService.deploy(
            DeployPluginPackageRequest(packageId = enabledPackage.id, runBuild = false),
        )

        assertEquals(PluginDeploymentStatus.EXPORTED, enabledJob.status)
        assertTrue(enabledModuleDir.resolve("README.md").exists())
        assertEquals(PluginActivationState.ENABLED, fixture.packageService.get(enabledPackage.id).activationState)
        assertTrue(fixture.markerPath(enabledPackage.id).notExists())

        val disabledModuleDir = fixture.tempRoot.resolve("plugins/system/exported-disabled")
        val disabledPackage = fixture.packageService.create(
            CreatePluginPackageRequest(
                pluginId = "exported-disabled",
                name = "已停用导出插件",
                pluginGroup = "system",
                basePackage = "site.addzero.kcloud.plugins.system",
                moduleDir = disabledModuleDir.toString(),
                enabled = false,
            ),
        ).pluginPackage
        fixture.fileService.save(
            SavePluginSourceFileRequest(
                packageId = disabledPackage.id,
                relativePath = "README.md",
                content = "# disabled",
                fileGroup = "meta",
            ),
        )

        fixture.deploymentService.deploy(
            DeployPluginPackageRequest(packageId = disabledPackage.id, runBuild = false),
        )

        assertTrue(disabledModuleDir.resolve("README.md").exists())
        assertEquals(PluginActivationState.DISABLED, fixture.packageService.get(disabledPackage.id).activationState)
        assertTrue(fixture.markerPath(disabledPackage.id).exists())
    }
}

private data class PluginMarketFixture(
    val tempRoot: Path,
    val dataSource: DataSource,
    val catalog: PluginMarketCatalogSupport,
    val workspace: PluginPackageWorkspaceSupport,
    val packageService: PluginPackageServiceImpl,
    val fileService: PluginSourceFileServiceImpl,
    val deploymentService: PluginDeploymentServiceImpl,
) {
    fun markerPath(packageId: String): Path {
        return workspace.disabledMarkerPath(catalog.packageOrThrow(packageId))
    }

    fun attachImportRecord(packageId: String, sourceModuleDir: String) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                insert into plugin_import_record (
                    id,
                    package_id,
                    source_module_dir,
                    source_gradle_path,
                    imported_at
                ) values (?, ?, ?, ?, ?)
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, catalog.newId())
                statement.setString(2, packageId)
                statement.setString(3, sourceModuleDir)
                statement.setString(4, ":apps:kcloud:plugins:system:imported-plugin")
                statement.setString(5, catalog.now().toString())
                statement.executeUpdate()
            }
        }
    }
}

private fun withPluginMarketFixture(block: suspend (PluginMarketFixture) -> Unit) {
    val tempRoot = Files.createTempDirectory("plugin-market-service-test-")
    try {
        val dataSource = SQLiteDataSource().apply {
            url = "jdbc:sqlite:${tempRoot.resolve("plugin-market.sqlite")}"
        }
        JimmerSqlScriptSupport.executeStatements(
            dataSource = dataSource,
            sql = locateRepoRoot()
                .resolve("apps/kcloud/server/src/jvmMain/resources/sql/schema-sqlite.sql")
                .toFile()
                .readText(),
        )
        val sqlClient = newKSqlClient {
            setDialect(SQLiteDialect())
            addScalarProvider(SqliteLocalDateTimeScalarProvider)
            addScalarProvider(SqliteInstantScalarProvider)
            setConnectionManager(ConnectionManager.simpleConnectionManager(dataSource))
        }
        val catalog = PluginMarketCatalogSupport(sqlClient)
        val workspace = PluginPackageWorkspaceSupport(catalog)
        val aggregateSupport = PluginPackageAggregateSupport(catalog, workspace)
        val managedIntegrationSupport = PluginManagedIntegrationSupport()
        val fileService = PluginSourceFileServiceImpl(sqlClient, catalog)
        val presetService = PluginPresetServiceImpl(
            sqlClient = sqlClient,
            catalog = catalog,
            aggregateSupport = aggregateSupport,
        )
        val packageService = PluginPackageServiceImpl(
            sqlClient = sqlClient,
            catalog = catalog,
            aggregateSupport = aggregateSupport,
            workspaceSupport = workspace,
            presetService = presetService,
        )
        val configService = object : PluginMarketConfigService {
            override suspend fun read(): PluginMarketConfigDto = PluginMarketConfigDto(
                exportRootDir = tempRoot.resolve("plugins").toString(),
                gradleCommand = "./gradlew",
                gradleTasks = emptyList(),
                autoBuildEnabled = false,
            )

            override suspend fun update(request: UpdatePluginMarketConfigRequest): PluginMarketConfigDto {
                return PluginMarketConfigDto(
                    exportRootDir = request.exportRootDir,
                    gradleCommand = request.gradleCommand,
                    gradleTasks = request.gradleTasks,
                    javaHome = request.javaHome,
                    environmentLines = request.environmentLines,
                    autoBuildEnabled = request.autoBuildEnabled,
                )
            }
        }
        val deploymentService = PluginDeploymentServiceImpl(
            sqlClient = sqlClient,
            catalog = catalog,
            workspaceSupport = workspace,
            managedIntegrationSupport = managedIntegrationSupport,
            configService = configService,
            commandRunner = object : PluginBuildCommandRunner {
                override fun run(
                    commandLine: String,
                    workingDirectory: java.io.File,
                    environment: Map<String, String>,
                    javaHome: String?,
                ): PluginBuildCommandResult {
                    return PluginBuildCommandResult(
                        exitCode = 0,
                        stdout = "ok",
                        stderr = "",
                        command = commandLine,
                    )
                }
            },
        )
        runBlocking {
            block(
                PluginMarketFixture(
                    tempRoot = tempRoot,
                    dataSource = dataSource,
                    catalog = catalog,
                    workspace = workspace,
                    packageService = packageService,
                    fileService = fileService,
                    deploymentService = deploymentService,
                ),
            )
        }
    } finally {
        tempRoot.toFile().deleteRecursively()
    }
}

private fun locateRepoRoot(): Path {
    return generateSequence(Paths.get("").toAbsolutePath().normalize()) { current -> current.parent }
        .firstOrNull { candidate -> Files.isRegularFile(candidate.resolve("settings.gradle.kts")) }
        ?: error("未找到仓库根目录")
}
