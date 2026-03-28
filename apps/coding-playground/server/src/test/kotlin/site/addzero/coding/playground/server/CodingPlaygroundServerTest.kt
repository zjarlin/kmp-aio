package site.addzero.coding.playground.server

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
import site.addzero.coding.playground.shared.dto.CreateDeclarationPresetRequest
import site.addzero.coding.playground.shared.dto.CreateGenerationTargetRequest
import site.addzero.coding.playground.shared.dto.DeclarationKind
import site.addzero.coding.playground.shared.dto.SyncExportRequest
import site.addzero.coding.playground.shared.dto.SyncImportRequest
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.sql.DataSource
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CodingPlaygroundServerTest {
    @Test
    fun kotlinCodegenCrudRenderSyncAndIndexPreviewWork() = withRuntime { runtime ->
        runBlocking {
            val project = runtime.projectService.create(
                CreateCodegenProjectRequest(
                    name = "catalog-project",
                    description = "服务端代码生成烟雾测试",
                ),
            )
            val target = runtime.targetService.create(
                CreateGenerationTargetRequest(
                    projectId = project.id,
                    name = "desktop-target",
                    rootDir = runtime.root.resolve("generated").toString(),
                    sourceSet = "main",
                    basePackage = "site.addzero.generated.catalog",
                    indexPackage = "site.addzero.generated.catalog.index",
                    kspEnabled = true,
                    variables = mapOf("HOME" to runtime.root.toString()),
                ),
            )

            val aggregate = runtime.fileService.createPreset(
                CreateDeclarationPresetRequest(
                    targetId = target.id,
                    packageName = "site.addzero.generated.catalog.model",
                    declarationName = "CatalogItem",
                    kind = DeclarationKind.DATA_CLASS,
                ),
            )

            val preview = runtime.renderService.previewFile(aggregate.file.id)
            assertTrue(preview.content.contains("data class CatalogItem"))
            assertTrue(preview.content.contains("@GeneratedManagedDeclaration"))
            assertTrue(preview.content.contains("@author"))
            assertTrue(preview.content.contains("@constructor 创建[CatalogItem]"))
            assertTrue(preview.content.contains("@param [id]"))
            assertTrue(preview.content.contains("@param [name]"))

            val export = runtime.renderService.export(SyncExportRequest(fileId = aggregate.file.id))
            assertEquals(1, export.artifacts.size)
            val artifact = export.artifacts.single()
            val exportedPath = Paths.get(artifact.absolutePath)
            assertTrue(exportedPath.exists())
            assertTrue(exportedPath.readText().contains("data class CatalogItem"))
            assertTrue(exportedPath.readText().contains("@constructor 创建[CatalogItem]"))

            val importResult = runtime.renderService.importSource(SyncImportRequest(fileId = aggregate.file.id))
            assertEquals(1, importResult.files.size)
            assertTrue(importResult.files.single().declarations.any { it.name == "CatalogItem" })

            val pathPreview = runtime.targetService.previewPath(target.id)
            assertTrue(pathPreview.sourceRoot.endsWith("/src/main/kotlin"))

            val indexPreview = runtime.renderService.previewIndex(target.id)
            assertTrue(indexPreview.content.contains("object GeneratedCodeIndex"))
            assertTrue(indexPreview.content.contains("CatalogItem"))
        }
    }

    @Test
    fun kotlinCodegenSupportsAllDeclarationPresets() = withRuntime { runtime ->
        runBlocking {
            val project = runtime.projectService.create(
                CreateCodegenProjectRequest(
                    name = "preset-project",
                    description = "五种声明预设烟雾测试",
                ),
            )
            val target = runtime.targetService.create(
                CreateGenerationTargetRequest(
                    projectId = project.id,
                    name = "preset-target",
                    rootDir = runtime.root.resolve("generated").toString(),
                    sourceSet = "main",
                    basePackage = "site.addzero.generated.presets",
                    indexPackage = "site.addzero.generated.presets.index",
                    kspEnabled = true,
                ),
            )

            val cases = listOf(
                PresetCase(
                    kind = DeclarationKind.ENUM_CLASS,
                    declarationName = "OrderStatus",
                    packageName = "site.addzero.generated.presets.enums",
                    expectedSnippets = listOf(
                        "enum class OrderStatus",
                        "DEFAULT",
                        "DISABLED",
                        "@author",
                    ),
                ),
                PresetCase(
                    kind = DeclarationKind.INTERFACE,
                    declarationName = "CatalogGateway",
                    packageName = "site.addzero.generated.presets.api",
                    expectedSnippets = listOf(
                        "interface CatalogGateway",
                        "fun load(id: String): CatalogGateway",
                        "@author",
                    ),
                ),
                PresetCase(
                    kind = DeclarationKind.OBJECT,
                    declarationName = "CatalogDefaults",
                    packageName = "site.addzero.generated.presets.support",
                    expectedSnippets = listOf(
                        "object CatalogDefaults",
                        "val version: String = \"1.0.0\"",
                        "@author",
                    ),
                ),
                PresetCase(
                    kind = DeclarationKind.ANNOTATION_CLASS,
                    declarationName = "ManagedCatalog",
                    packageName = "site.addzero.generated.presets.annotation",
                    expectedSnippets = listOf(
                        "annotation class ManagedCatalog",
                        "@constructor 创建[ManagedCatalog]",
                        "val value: String = \"\"",
                        "@param [value]",
                    ),
                ),
            )

            val createdFiles = mutableListOf<String>()
            cases.forEach { case ->
                val aggregate = runtime.fileService.createPreset(
                    CreateDeclarationPresetRequest(
                        targetId = target.id,
                        packageName = case.packageName,
                        declarationName = case.declarationName,
                        kind = case.kind,
                    ),
                )
                createdFiles += aggregate.file.id

                val preview = runtime.renderService.previewFile(aggregate.file.id)
                case.expectedSnippets.forEach { snippet ->
                    assertTrue(
                        preview.content.contains(snippet),
                        "预览缺少片段 `$snippet`，kind=${case.kind}，content=\n${preview.content}",
                    )
                }

                val export = runtime.renderService.export(SyncExportRequest(fileId = aggregate.file.id))
                val outputPath = Paths.get(export.artifacts.single().absolutePath)
                assertTrue(outputPath.exists())
                val exportedText = outputPath.readText()
                case.expectedSnippets.forEach { snippet ->
                    assertTrue(
                        exportedText.contains(snippet),
                        "写盘缺少片段 `$snippet`，kind=${case.kind}，content=\n$exportedText",
                    )
                }

                val importResult = runtime.renderService.importSource(SyncImportRequest(fileId = aggregate.file.id))
                val importedDeclaration = importResult.files.single().declarations.single()
                assertEquals(case.kind, importedDeclaration.kind)
                assertEquals(case.declarationName, importedDeclaration.name)
            }

            val indexPreview = runtime.renderService.previewIndex(target.id)
            cases.forEach { case ->
                assertTrue(indexPreview.content.contains(case.declarationName))
            }
            assertEquals(cases.size, createdFiles.distinct().size)
        }
    }
}

private data class PresetCase(
    val kind: DeclarationKind,
    val declarationName: String,
    val packageName: String,
    val expectedSnippets: List<String>,
)

private data class ServerRuntime(
    val root: Path,
    val projectService: CodegenProjectServiceImpl,
    val targetService: GenerationTargetServiceImpl,
    val fileService: SourceFileServiceImpl,
    val declarationService: DeclarationServiceImpl,
    val renderService: CodeRenderAndSyncServiceImpl,
)

private fun withRuntime(block: (ServerRuntime) -> Unit) {
    val root = Files.createTempDirectory("coding-playground-server-test")
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
    val runtime = ServerRuntime(
        root = root,
        projectService = CodegenProjectServiceImpl(support, serviceSupport),
        targetService = GenerationTargetServiceImpl(support, serviceSupport, pathResolver),
        fileService = fileService,
        declarationService = declarationService,
        renderService = CodeRenderAndSyncServiceImpl(
            support = support,
            pathResolver = pathResolver,
            json = Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            },
        ),
    )
    block(runtime)
}

private fun sqliteDataSource(path: Path): DataSource {
    return SQLiteDataSource().apply {
        url = "jdbc:sqlite:${path.toAbsolutePath()}"
    }
}
