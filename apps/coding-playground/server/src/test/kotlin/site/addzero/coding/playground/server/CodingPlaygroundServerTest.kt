package site.addzero.coding.playground.server

import kotlinx.coroutines.runBlocking
import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy
import org.sqlite.SQLiteDataSource
import site.addzero.coding.playground.server.config.PlaygroundJdbcTransactionContext
import site.addzero.coding.playground.server.config.SqliteLocalDateTimeScalarProvider
import site.addzero.coding.playground.server.config.initDatabase
import site.addzero.coding.playground.server.service.*
import site.addzero.coding.playground.shared.dto.*
import java.nio.file.Files
import java.nio.file.Path
import javax.sql.DataSource
import kotlin.io.path.absolutePathString
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CodingPlaygroundServerTest {
    @Test
    fun llvmCrudExportSnapshotAndCompileHooksWork() = withRuntime { runtime ->
        runBlocking {
            val module = runtime.moduleService.create(
                CreateLlvmModuleRequest(
                    name = "catalog",
                    sourceFilename = "catalog.ll",
                    targetTriple = "x86_64-unknown-linux-gnu",
                    dataLayout = "e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128",
                    description = "LLVM module for tests",
                ),
            )
            val intType = runtime.typeService.create(
                CreateLlvmTypeRequest(
                    moduleId = module.id,
                    name = "Int32",
                    symbol = "Int32",
                    kind = LlvmTypeKind.INTEGER,
                    primitiveWidth = 32,
                ),
            )
            runtime.globalValueService.createGlobal(
                CreateLlvmGlobalVariableRequest(
                    moduleId = module.id,
                    name = "answer",
                    symbol = "answer",
                    typeText = "i32",
                    typeRefId = intType.id,
                    initializerText = "42",
                    constant = true,
                ),
            )
            val function = runtime.functionService.create(
                CreateLlvmFunctionRequest(
                    moduleId = module.id,
                    name = "main",
                    symbol = "main",
                    returnTypeText = "i32",
                    returnTypeRefId = intType.id,
                ),
            )
            val block = runtime.functionService.createBlock(
                CreateLlvmBasicBlockRequest(functionId = function.id, name = "entry", label = "entry"),
            )
            runtime.functionService.createInstruction(
                CreateLlvmInstructionRequest(
                    blockId = block.id,
                    opcode = LlvmInstructionOpcode.RET,
                    typeText = "i32",
                    terminator = true,
                ),
            )
            val metadataNode = runtime.metadataService.createNode(
                CreateLlvmMetadataNodeRequest(moduleId = module.id, name = "dbg.cu", kind = LlvmMetadataKind.DEBUG_COMPILE_UNIT),
            )
            runtime.metadataService.createField(
                CreateLlvmMetadataFieldRequest(
                    metadataNodeId = metadataNode.id,
                    valueKind = LlvmMetadataValueKind.STRING,
                    valueText = "catalog-unit",
                ),
            )

            val exportPath = runtime.root.resolve("catalog.ll")
            val export = runtime.exportService.exportModule(module.id, exportPath.absolutePathString())
            assertTrue(export.content.contains("define external default i32 @main"))
            assertTrue(exportPath.readText().contains("@answer"))

            val snapshot = runtime.snapshotService.exportModule(module.id)
            assertEquals(1, snapshot.modules.size)
            assertEquals(1, snapshot.functions.size)

            val profile = runtime.compileProfileService.create(
                CreateLlvmCompileProfileRequest(
                    moduleId = module.id,
                    name = "export-only",
                    targetPlatform = "host",
                    outputDirectory = runtime.root.resolve("out").absolutePathString(),
                ),
            )
            val job = runtime.compileJobService.create(
                CreateLlvmCompileJobRequest(
                    moduleId = module.id,
                    profileId = profile.id,
                    runNow = false,
                ),
            )
            val result = runtime.compileJobService.execute(job.id)
            assertEquals(LlvmCompileJobStatus.SUCCEEDED, result.job.status)
            assertTrue(result.artifacts.any { it.kind == LlvmCompileArtifactKind.LLVM_IR })
        }
    }

    @Test
    fun deleteChecksBlockReferencedTypeAndFunction() = withRuntime { runtime ->
        runBlocking {
            val module = runtime.moduleService.create(
                CreateLlvmModuleRequest(
                    name = "refs",
                    sourceFilename = "refs.ll",
                    targetTriple = "x86_64-unknown-linux-gnu",
                    dataLayout = "layout",
                ),
            )
            val intType = runtime.typeService.create(
                CreateLlvmTypeRequest(moduleId = module.id, name = "Int32", symbol = "Int32", kind = LlvmTypeKind.INTEGER, primitiveWidth = 32),
            )
            val callee = runtime.functionService.create(
                CreateLlvmFunctionRequest(moduleId = module.id, name = "callee", symbol = "callee", returnTypeText = "i32", returnTypeRefId = intType.id),
            )
            val caller = runtime.functionService.create(
                CreateLlvmFunctionRequest(moduleId = module.id, name = "caller", symbol = "caller", returnTypeText = "i32", returnTypeRefId = intType.id),
            )
            val block = runtime.functionService.createBlock(CreateLlvmBasicBlockRequest(caller.id, "entry", "entry"))
            val callInst = runtime.functionService.createInstruction(
                CreateLlvmInstructionRequest(block.id, LlvmInstructionOpcode.CALL, resultSymbol = "tmp", typeText = "i32"),
            )
            runtime.functionService.createOperand(
                CreateLlvmOperandRequest(
                    instructionId = callInst.id,
                    kind = LlvmOperandKind.SYMBOL,
                    text = "@callee",
                    referencedFunctionId = callee.id,
                ),
            )

            val typeCheck = runtime.typeService.deleteCheck(intType.id)
            val functionCheck = runtime.functionService.deleteCheck(callee.id)
            assertFalse(typeCheck.deletable)
            assertFalse(functionCheck.deletable)
        }
    }

    @Test
    fun snapshotImportRoundTripKeepsModuleReachable() = withRuntime { runtime ->
        runBlocking {
            val module = runtime.moduleService.create(
                CreateLlvmModuleRequest(
                    name = "roundtrip",
                    sourceFilename = "roundtrip.ll",
                    targetTriple = "wasm32-unknown-unknown",
                    dataLayout = "e-m:e-p:32:32",
                ),
            )
            val snapshot = runtime.snapshotService.exportModule(module.id)
            val importResult = runtime.snapshotService.importSnapshot(snapshot)
            assertEquals(1, importResult.importedModules)
            assertTrue(runtime.moduleService.list().any { it.id == module.id })
        }
    }
}

private data class TestRuntime(
    val root: Path,
    val support: MetadataPersistenceSupport,
    val validationService: LlvmValidationServiceImpl,
    val moduleService: LlvmModuleServiceImpl,
    val typeService: LlvmTypeServiceImpl,
    val globalValueService: LlvmGlobalValueServiceImpl,
    val functionService: LlvmFunctionServiceImpl,
    val metadataService: LlvmMetadataServiceImpl,
    val attributeService: LlvmAttributeServiceImpl,
    val snapshotService: LlvmSnapshotServiceImpl,
    val exportService: LlvmLlExportServiceImpl,
    val compileProfileService: LlvmCompileProfileServiceImpl,
    val compileJobService: LlvmCompileJobServiceImpl,
)

private fun withRuntime(block: (TestRuntime) -> Unit) {
    val root = Files.createTempDirectory("llvm-playground-test")
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
    val validation = LlvmValidationServiceImpl(support)
    val moduleService = LlvmModuleServiceImpl(support, validation)
    val typeService = LlvmTypeServiceImpl(support)
    val globalValueService = LlvmGlobalValueServiceImpl(support)
    val functionService = LlvmFunctionServiceImpl(support)
    val metadataService = LlvmMetadataServiceImpl(support)
    val attributeService = LlvmAttributeServiceImpl(support)
    val exportService = LlvmLlExportServiceImpl(support)
    val snapshotService = LlvmSnapshotServiceImpl(support)
    val compileProfileService = LlvmCompileProfileServiceImpl(support)
    val compileJobService = LlvmCompileJobServiceImpl(support, exportService)
    block(
        TestRuntime(
            root = root,
            support = support,
            validationService = validation,
            moduleService = moduleService,
            typeService = typeService,
            globalValueService = globalValueService,
            functionService = functionService,
            metadataService = metadataService,
            attributeService = attributeService,
            snapshotService = snapshotService,
            exportService = exportService,
            compileProfileService = compileProfileService,
            compileJobService = compileJobService,
        ),
    )
}

private fun sqliteDataSource(path: Path): DataSource {
    return SQLiteDataSource().apply {
        url = "jdbc:sqlite:${path.toAbsolutePath()}"
    }
}
