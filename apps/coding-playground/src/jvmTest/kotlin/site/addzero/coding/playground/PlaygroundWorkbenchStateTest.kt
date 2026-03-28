package site.addzero.coding.playground

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy
import org.sqlite.SQLiteDataSource
import site.addzero.coding.playground.server.config.PlaygroundJdbcTransactionContext
import site.addzero.coding.playground.server.config.SqliteLocalDateTimeScalarProvider
import site.addzero.coding.playground.server.config.initDatabase
import site.addzero.coding.playground.server.service.CodeRenderAndSyncServiceImpl
import site.addzero.coding.playground.server.service.CodegenPathResolver
import site.addzero.coding.playground.server.service.CodegenProjectServiceImpl
import site.addzero.coding.playground.server.service.CodegenServiceSupport
import site.addzero.coding.playground.server.service.DeclarationServiceImpl
import site.addzero.coding.playground.server.service.GenerationTargetServiceImpl
import site.addzero.coding.playground.server.service.MetadataPersistenceSupport
import site.addzero.coding.playground.server.service.SourceFileServiceImpl
import site.addzero.coding.playground.shared.dto.CreateCodegenProjectRequest
import site.addzero.coding.playground.shared.dto.CreateGenerationTargetRequest
import site.addzero.coding.playground.shared.dto.DeclarationKind
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.sql.DataSource
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.requireNotNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PlaygroundWorkbenchStateTest {
    @Test
    fun workbenchStateSupportsCodegenSmokeFlow() = withWorkbenchState { runtime ->
        runBlocking {
            runtime.state.refreshAll()
            assertTrue(runtime.state.projects.isEmpty())

            runtime.state.saveProject(
                selectedId = null,
                request = CreateCodegenProjectRequest(
                    name = "desktop-project",
                    description = "桌面状态烟雾测试",
                ),
            )
            assertEquals(1, runtime.state.projects.size)
            assertNotNull(runtime.state.selectedProjectId)

            runtime.state.saveTarget(
                selectedId = null,
                request = CreateGenerationTargetRequest(
                    projectId = runtime.state.selectedProjectId!!,
                    name = "desktop-target",
                    rootDir = runtime.root.resolve("generated").toString(),
                    sourceSet = "main",
                    basePackage = "site.addzero.generated.desktop",
                    indexPackage = "site.addzero.generated.desktop.index",
                    kspEnabled = true,
                ),
            )
            assertEquals(1, runtime.state.targets.size)
            assertNotNull(runtime.state.selectedTargetId)

            runtime.state.createPreset(
                kind = DeclarationKind.DATA_CLASS,
                declarationName = "DesktopItem",
                packageName = "site.addzero.generated.desktop.model",
            )
            assertNotNull(runtime.state.selectedFileId)
            assertNotNull(runtime.state.selectedDeclarationId)
            assertTrue(runtime.state.declarations.any { it.name == "DesktopItem" })

            runtime.state.addProperty("enabled", "Boolean", mutable = false, initializer = "true")
            runtime.state.addFunction(
                name = "displayName",
                returnType = "String",
                parametersText = "",
            )

            runtime.state.refreshPreview()
            assertTrue(runtime.state.sourcePreview?.content?.contains("data class DesktopItem") == true)
            assertTrue(runtime.state.kspPreview?.content?.contains("GeneratedCodeIndex") == true)
            assertTrue(runtime.state.targetPathPreview?.sourceRoot?.endsWith("/src/main/kotlin") == true)
            assertTrue(runtime.state.sourcePreview?.content?.contains("@constructor 创建[DesktopItem]") == true)
            assertTrue(runtime.state.sourcePreview?.content?.contains("@param [id]") == true)

            runtime.state.exportSelectedFile()
            assertTrue(runtime.state.artifacts.isNotEmpty())
            val outputPath = Paths.get(requireNotNull(runtime.state.sourcePreview).outputPath)
            assertTrue(outputPath.exists())

            runtime.state.importSelectedFile()
            assertTrue(runtime.state.statusMessage.contains("已导回"))
            assertTrue(runtime.state.fileAggregate?.declarations?.any { it.name == "DesktopItem" } == true)
        }
    }

    @Test
    fun workbenchStateAutoSyncWritesAndImportsExternalChanges() = withWorkbenchState { runtime ->
        runBlocking {
            runtime.state.refreshAll()
            runtime.state.saveProject(
                selectedId = null,
                request = CreateCodegenProjectRequest(
                    name = "auto-sync-project",
                    description = "自动同步烟雾测试",
                ),
            )
            runtime.state.saveTarget(
                selectedId = null,
                request = CreateGenerationTargetRequest(
                    projectId = runtime.state.selectedProjectId!!,
                    name = "auto-sync-target",
                    rootDir = runtime.root.resolve("generated").toString(),
                    sourceSet = "main",
                    basePackage = "site.addzero.generated.auto",
                    indexPackage = "site.addzero.generated.auto.index",
                    kspEnabled = true,
                ),
            )
            runtime.state.createPreset(
                kind = DeclarationKind.DATA_CLASS,
                declarationName = "AutoSyncItem",
                packageName = "site.addzero.generated.auto.model",
            )
            runtime.state.addProperty("enabled", "Boolean", mutable = false, initializer = "true")
            runtime.state.awaitAutoSyncSettled()

            val preview = requireNotNull(runtime.state.sourcePreview)
            val outputPath = Paths.get(preview.outputPath)
            assertTrue(outputPath.exists())
            assertTrue(outputPath.readText().contains("val enabled: Boolean = true"))

            val externalContent = preview.content.replace(
                "name: String = \"\"",
                "name: String = \"外部改动\"",
            )
            assertTrue(externalContent.contains("name: String = \"外部改动\""))
            outputPath.writeText(externalContent)

            val deadline = System.currentTimeMillis() + 8_000
            while (System.currentTimeMillis() < deadline) {
                val importedDefault = runtime.state.fileAggregate?.constructorParams
                    ?.firstOrNull { it.name == "name" }
                    ?.defaultValue
                if (importedDefault == "\"外部改动\"") {
                    break
                }
                delay(300)
            }

            val importedDefault = runtime.state.fileAggregate?.constructorParams
                ?.firstOrNull { it.name == "name" }
                ?.defaultValue
            if (importedDefault != "\"外部改动\"") {
                runtime.state.refreshFileScope()
            }
            val refreshedDefault = runtime.state.fileAggregate?.constructorParams
                ?.firstOrNull { it.name == "name" }
                ?.defaultValue
            assertEquals(
                "\"外部改动\"",
                refreshedDefault,
                "状态=${runtime.state.statusMessage}，错误=${runtime.state.statusIsError}，冲突=${runtime.state.conflicts.joinToString { it.message }}",
            )
        }
    }

    @Test
    fun workbenchStateSupportsOtherDeclarationPresetFlows() = withWorkbenchState { runtime ->
        runBlocking {
            runtime.state.refreshAll()
            runtime.state.saveProject(
                selectedId = null,
                request = CreateCodegenProjectRequest(
                    name = "multi-kind-project",
                    description = "多声明类型状态测试",
                ),
            )
            runtime.state.saveTarget(
                selectedId = null,
                request = CreateGenerationTargetRequest(
                    projectId = runtime.state.selectedProjectId!!,
                    name = "multi-kind-target",
                    rootDir = runtime.root.resolve("generated").toString(),
                    sourceSet = "main",
                    basePackage = "site.addzero.generated.multikind",
                    indexPackage = "site.addzero.generated.multikind.index",
                    kspEnabled = true,
                ),
            )

            val cases = listOf(
                DeclarationKind.ENUM_CLASS to "WorkbenchStatus",
                DeclarationKind.INTERFACE to "WorkbenchGateway",
                DeclarationKind.OBJECT to "WorkbenchDefaults",
                DeclarationKind.ANNOTATION_CLASS to "WorkbenchManaged",
            )

            cases.forEach { (kind, name) ->
                runtime.state.createPreset(
                    kind = kind,
                    declarationName = name,
                    packageName = "site.addzero.generated.multikind.${kind.name.lowercase()}",
                )
                runtime.state.refreshPreview()
                val preview = requireNotNull(runtime.state.sourcePreview)
                assertTrue(preview.content.contains(name), "预览没有声明 $name")
                runtime.state.exportSelectedFile()
                runtime.state.importSelectedFile()
                assertTrue(
                    runtime.state.fileAggregate?.declarations?.any { it.name == name && it.kind == kind } == true,
                    "导回后没有保留声明 $name / $kind",
                )
            }

            val preview = requireNotNull(runtime.state.kspPreview)
            cases.forEach { (_, name) ->
                assertTrue(preview.content.contains(name), "索引预览缺少 $name")
            }
        }
    }
}

private data class WorkbenchStateRuntime(
    val root: Path,
    val state: PlaygroundWorkbenchState,
)

private fun withWorkbenchState(block: (WorkbenchStateRuntime) -> Unit) {
    val root = Files.createTempDirectory("coding-playground-workbench-test")
    val dataSource = sqliteDataSource(root.resolve("coding-playground.db"))
    initDatabase(dataSource)
    val sqlClient = newKSqlClient {
        setDialect(SQLiteDialect())
        setDatabaseNamingStrategy(DefaultDatabaseNamingStrategy.LOWER_CASE)
        addScalarProvider(SqliteLocalDateTimeScalarProvider)
        setConnectionManager {
            val transactionalConnection = PlaygroundJdbcTransactionContext.connectionOrNull()
            if (transactionalConnection != null) {
                proceed(transactionalConnection)
            } else {
                dataSource.connection.use { proceed(it) }
            }
        }
    }
    val support = MetadataPersistenceSupport(sqlClient, dataSource)
    val serviceSupport = CodegenServiceSupport()
    val pathResolver = CodegenPathResolver()
    val declarationService = DeclarationServiceImpl(support, serviceSupport)
    val fileService = SourceFileServiceImpl(support, serviceSupport, declarationService)
    val renderSyncService = CodeRenderAndSyncServiceImpl(
        support = support,
        pathResolver = pathResolver,
        json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        },
    )
    val state = PlaygroundWorkbenchState(
        projectService = CodegenProjectServiceImpl(support, serviceSupport),
        targetService = GenerationTargetServiceImpl(support, serviceSupport, pathResolver),
        fileService = fileService,
        declarationService = declarationService,
        renderService = renderSyncService,
        artifactService = renderSyncService,
        syncService = renderSyncService,
        kspIndexService = renderSyncService,
        managedFileSupport = JvmManagedFileSupport(),
    )
    state.startBackgroundSync()
    try {
        block(WorkbenchStateRuntime(root = root, state = state))
    } finally {
        state.stopBackgroundSync()
    }
}

private fun sqliteDataSource(path: Path): DataSource {
    return SQLiteDataSource().apply {
        url = "jdbc:sqlite:${path.toAbsolutePath()}"
    }
}
