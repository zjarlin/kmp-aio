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
}

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
