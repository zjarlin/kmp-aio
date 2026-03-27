package site.addzero.coding.playground

import kotlinx.coroutines.runBlocking
import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy
import org.sqlite.SQLiteDataSource
import site.addzero.coding.playground.server.config.PlaygroundJdbcTransactionContext
import site.addzero.coding.playground.server.config.SqliteLocalDateTimeScalarProvider
import site.addzero.coding.playground.server.config.initDatabase
import site.addzero.coding.playground.server.service.LlvmAttributeServiceImpl
import site.addzero.coding.playground.server.service.LlvmCompileJobServiceImpl
import site.addzero.coding.playground.server.service.LlvmCompileProfileServiceImpl
import site.addzero.coding.playground.server.service.LlvmFunctionServiceImpl
import site.addzero.coding.playground.server.service.LlvmGlobalValueServiceImpl
import site.addzero.coding.playground.server.service.LlvmLlExportServiceImpl
import site.addzero.coding.playground.server.service.LlvmMetadataServiceImpl
import site.addzero.coding.playground.server.service.LlvmModuleServiceImpl
import site.addzero.coding.playground.server.service.LlvmSnapshotServiceImpl
import site.addzero.coding.playground.server.service.LlvmTypeServiceImpl
import site.addzero.coding.playground.server.service.LlvmValidationServiceImpl
import site.addzero.coding.playground.server.service.MetadataPersistenceSupport
import site.addzero.coding.playground.shared.dto.CreateLlvmBasicBlockRequest
import site.addzero.coding.playground.shared.dto.CreateLlvmCompileProfileRequest
import site.addzero.coding.playground.shared.dto.CreateLlvmFunctionRequest
import site.addzero.coding.playground.shared.dto.CreateLlvmInstructionRequest
import site.addzero.coding.playground.shared.dto.CreateLlvmModuleRequest
import site.addzero.coding.playground.shared.dto.CreateLlvmTypeRequest
import site.addzero.coding.playground.shared.dto.LlvmCompileJobStatus
import site.addzero.coding.playground.shared.dto.LlvmInstructionOpcode
import site.addzero.coding.playground.shared.dto.LlvmTypeKind
import java.nio.file.Files
import java.nio.file.Path
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PlaygroundWorkbenchStateTest {
    @Test
    fun workbenchStateSupportsSmokeFlow() = withWorkbenchState { runtime ->
        runBlocking {
            runtime.state.refreshAll()
            assertTrue(runtime.state.modules.isEmpty())

            runtime.state.saveModule(
                selectedId = null,
                request = CreateLlvmModuleRequest(
                    name = "desktop-smoke",
                    sourceFilename = "desktop-smoke.ll",
                    targetTriple = "x86_64-unknown-linux-gnu",
                    dataLayout = "e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128",
                    description = "Workbench smoke test module",
                ),
            )
            assertEquals(1, runtime.state.modules.size)
            assertTrue(runtime.state.selectedModuleId != null)

            runtime.state.saveType(
                selectedId = null,
                request = CreateLlvmTypeRequest(
                    moduleId = runtime.state.selectedModuleId!!,
                    name = "Int32",
                    symbol = "Int32",
                    kind = LlvmTypeKind.INTEGER,
                    primitiveWidth = 32,
                ),
            )
            assertTrue(runtime.state.selectedTypeId != null)

            runtime.state.saveFunction(
                selectedId = null,
                request = CreateLlvmFunctionRequest(
                    moduleId = runtime.state.selectedModuleId!!,
                    name = "main",
                    symbol = "main",
                    returnTypeText = "i32",
                    returnTypeRefId = runtime.state.selectedTypeId,
                ),
            )
            runtime.state.saveBlock(selectedId = null, name = "entry", label = "entry")
            runtime.state.saveInstruction(
                selectedId = null,
                request = CreateLlvmInstructionRequest(
                    blockId = runtime.state.selectedBlockId!!,
                    opcode = LlvmInstructionOpcode.RET,
                    typeText = "i32",
                    terminator = true,
                ),
            )

            runtime.state.validateSelectedModule()
            assertTrue(runtime.state.validationIssues.isEmpty())

            runtime.state.exportSelectedModule()
            assertTrue(runtime.state.exportPreviewText.contains("define external default i32 @main"))

            val originalLanguage = runtime.state.uiLanguage
            runtime.state.toggleLanguage()
            assertNotEquals(originalLanguage, runtime.state.uiLanguage)
            runtime.state.toggleLanguage()

            runtime.state.exportSnapshot()
            assertTrue(runtime.state.snapshotEditorText.contains("\"modules\""))

            runtime.state.saveCompileProfile(
                selectedId = null,
                request = CreateLlvmCompileProfileRequest(
                    moduleId = runtime.state.selectedModuleId!!,
                    name = "export-only",
                    targetPlatform = "host",
                    outputDirectory = runtime.root.resolve("out").toString(),
                ),
            )
            runtime.state.createAndRunCompileJob()
            assertEquals(LlvmCompileJobStatus.SUCCEEDED, runtime.state.lastCompileResult?.job?.status)

            runtime.state.removeSelectedModule()
            assertTrue(runtime.state.modules.isEmpty())

            runtime.state.importSnapshot()
            assertEquals(1, runtime.state.modules.size)
            assertEquals("快照已导入", runtime.state.statusMessage)
        }
    }
}

private data class WorkbenchStateRuntime(
    val root: Path,
    val state: PlaygroundWorkbenchState,
)

private fun withWorkbenchState(block: (WorkbenchStateRuntime) -> Unit) {
    val root = Files.createTempDirectory("llvm-playground-workbench-test")
    val dataSource = sqliteDataSource(root.resolve("llvm.db"))
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
    val validationService = LlvmValidationServiceImpl(support)
    val moduleService = LlvmModuleServiceImpl(support, validationService)
    val typeService = LlvmTypeServiceImpl(support)
    val globalValueService = LlvmGlobalValueServiceImpl(support)
    val functionService = LlvmFunctionServiceImpl(support)
    val metadataService = LlvmMetadataServiceImpl(support)
    val attributeService = LlvmAttributeServiceImpl(support)
    val snapshotService = LlvmSnapshotServiceImpl(support)
    val exportService = LlvmLlExportServiceImpl(support)
    val compileProfileService = LlvmCompileProfileServiceImpl(support)
    val compileJobService = LlvmCompileJobServiceImpl(support, exportService)
    val state = PlaygroundWorkbenchState(
        moduleService = moduleService,
        typeService = typeService,
        globalValueService = globalValueService,
        functionService = functionService,
        metadataService = metadataService,
        attributeService = attributeService,
        validationService = validationService,
        snapshotService = snapshotService,
        exportService = exportService,
        compileProfileService = compileProfileService,
        compileJobService = compileJobService,
    )
    block(WorkbenchStateRuntime(root = root, state = state))
}

private fun sqliteDataSource(path: Path): DataSource {
    return SQLiteDataSource().apply {
        url = "jdbc:sqlite:${path.toAbsolutePath()}"
    }
}
