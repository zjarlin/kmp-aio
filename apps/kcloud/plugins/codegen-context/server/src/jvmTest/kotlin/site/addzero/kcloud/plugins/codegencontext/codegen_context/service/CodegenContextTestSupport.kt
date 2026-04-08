package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import java.nio.file.Files
import java.nio.file.Path
import java.sql.DriverManager
import javax.sql.DataSource
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText
import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.runtime.ConnectionManager.simpleConnectionManager
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy.LOWER_CASE
import site.addzero.kcloud.jimmer.jdbc.DataSourceJdbcExecutor
import site.addzero.kcloud.jimmer.scalarprovider.sqllite.SqliteInstantScalarProvider
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDetailDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenFieldDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenSchemaDto
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenConsumerTarget
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenFunctionCode
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenSchemaDirection
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenTransportType

internal class CodegenContextTestFixture : AutoCloseable {
    private val databasePath = Files.createTempFile("codegen-context-", ".sqlite")

    val dataSource: DataSource = sqliteDataSource(databasePath)
    val sql: KSqlClient =
        newKSqlClient {
            setDialect(SQLiteDialect())
            addScalarProvider(SqliteInstantScalarProvider())
            setDatabaseNamingStrategy(LOWER_CASE)
            setConnectionManager(simpleConnectionManager(dataSource))
        }
    val jdbc = DataSourceJdbcExecutor(dataSource)
    val generator = CodegenContextContractGenerator()
    val service = CodegenContextService(sql, jdbc, generator)
    val templateService = CodegenTemplateService(sql)

    init {
        createProtocolTemplateTable()
        seedProtocolTemplate(
            id = 1L,
            code = "MODBUS_RTU_CLIENT",
            name = "Modbus RTU Client",
            sortIndex = 0,
        )
        seedProtocolTemplate(
            id = 2L,
            code = "MODBUS_TCP_CLIENT",
            name = "Modbus TCP Client",
            sortIndex = 10,
        )
        CodegenContextSchemaBootstrap(dataSource)
    }

    override fun close() {
        databasePath.deleteIfExists()
    }

    private fun createProtocolTemplateTable() {
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(
                    """
                    CREATE TABLE host_config_protocol_template (
                        id INTEGER PRIMARY KEY,
                        code TEXT NOT NULL UNIQUE,
                        name TEXT NOT NULL,
                        description TEXT,
                        sort_index INTEGER NOT NULL,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }
    }

    private fun seedProtocolTemplate(
        id: Long,
        code: String,
        name: String,
        sortIndex: Int,
    ) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                INSERT INTO host_config_protocol_template (
                    id,
                    code,
                    name,
                    description,
                    sort_index,
                    created_at,
                    updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
            ).use { statement ->
                statement.setLong(1, id)
                statement.setString(2, code)
                statement.setString(3, name)
                statement.setString(4, "$name description")
                statement.setInt(5, sortIndex)
                statement.setLong(6, 1_712_534_400_000L)
                statement.setLong(7, 1_712_534_400_000L)
                statement.executeUpdate()
            }
        }
    }
}

internal fun baseContextRequest(
    protocolTemplateId: Long,
    code: String = "CTX_SAMPLE",
): CodegenContextDetailDto =
    CodegenContextDetailDto(
        code = code,
        name = "Sample Context",
        description = "Context for tests",
        enabled = true,
        consumerTarget = CodegenConsumerTarget.MCU_CONSOLE,
        protocolTemplateId = protocolTemplateId,
        schemas =
            listOf(
                CodegenSchemaDto(
                    name = "Read Board Snapshot",
                    description = "Read board snapshot registers",
                    sortIndex = 0,
                    direction = CodegenSchemaDirection.READ,
                    functionCode = CodegenFunctionCode.READ_HOLDING_REGISTERS,
                    baseAddress = 10,
                    methodName = "readBoardSnapshot",
                    modelName = "BoardSnapshot",
                    fields =
                        listOf(
                            CodegenFieldDto(
                                name = "Uptime",
                                sortIndex = 0,
                                propertyName = "uptimeMs",
                                transportType = CodegenTransportType.U32_BE,
                                registerOffset = 0,
                            ),
                            CodegenFieldDto(
                                name = "Device Label",
                                sortIndex = 1,
                                propertyName = "deviceLabel",
                                transportType = CodegenTransportType.STRING_ASCII,
                                registerOffset = 2,
                                length = 4,
                            ),
                        ),
                ),
                CodegenSchemaDto(
                    name = "Write Board Config",
                    description = "Write board config registers",
                    sortIndex = 10,
                    direction = CodegenSchemaDirection.WRITE,
                    functionCode = CodegenFunctionCode.WRITE_MULTIPLE_REGISTERS,
                    baseAddress = 30,
                    methodName = "writeBoardConfig",
                    fields =
                        listOf(
                            CodegenFieldDto(
                                name = "Threshold",
                                sortIndex = 0,
                                propertyName = "threshold",
                                transportType = CodegenTransportType.U16,
                                registerOffset = 0,
                            ),
                            CodegenFieldDto(
                                name = "Note",
                                sortIndex = 1,
                                propertyName = "note",
                                transportType = CodegenTransportType.STRING_UTF8,
                                registerOffset = 1,
                                length = 2,
                            ),
                        ),
                ),
            ),
    )

internal fun createGeneratorWorkspace(): Path {
    val workspaceRoot = Files.createTempDirectory("codegen-context-workspace-")
    workspaceRoot.resolve("settings.gradle.kts").writeText("rootProject.name = \"codegen-context-test\"")
    workspaceRoot.resolve("apps/kcloud/plugins/mcu-console/server/generated/jvmMain/kotlin").createDirectories()
    workspaceRoot.resolve("apps/kcloud/plugins/mcu-console/shared/generated/commonMain/kotlin").createDirectories()
    workspaceRoot.resolve(
        "apps/kcloud/plugins/mcu-console/server/src/jvmMain/kotlin/" +
            "site/addzero/kcloud/plugins/mcuconsole/modbus/device",
    ).createDirectories()
    workspaceRoot.resolve(
        "apps/kcloud/plugins/mcu-console/shared/src/commonMain/kotlin/" +
            "site/addzero/kcloud/plugins/mcuconsole/modbus/device",
    ).createDirectories()
    return workspaceRoot
}

internal fun <T> withRepoRoot(
    workspaceRoot: Path,
    block: () -> T,
): T {
    val previous = System.getProperty("codegen.context.repoRoot")
    System.setProperty("codegen.context.repoRoot", workspaceRoot.toString())
    return try {
        block()
    } finally {
        if (previous == null) {
            System.clearProperty("codegen.context.repoRoot")
        } else {
            System.setProperty("codegen.context.repoRoot", previous)
        }
    }
}

internal fun deleteWorkspace(
    workspaceRoot: Path,
) {
    workspaceRoot.toFile().deleteRecursively()
}

internal fun readGolden(
    resourceName: String,
): String =
    checkNotNull(CodegenContextTestFixture::class.java.getResource("/golden/$resourceName")) {
        "Missing golden resource $resourceName"
    }.readText()

private fun sqliteDataSource(
    databasePath: Path,
): DataSource {
    Class.forName("org.sqlite.JDBC")
    val jdbcUrl = "jdbc:sqlite:$databasePath"
    return object : DataSource {
        override fun getConnection() =
            DriverManager.getConnection(jdbcUrl).apply {
                createStatement().use { statement ->
                    statement.execute("PRAGMA foreign_keys = ON")
                }
            }

        override fun getConnection(
            username: String?,
            password: String?,
        ) = getConnection()

        override fun getLogWriter() = null

        override fun setLogWriter(out: java.io.PrintWriter?) = Unit

        override fun setLoginTimeout(seconds: Int) = Unit

        override fun getLoginTimeout() = 0

        override fun getParentLogger() = java.util.logging.Logger.getLogger("org.sqlite")

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any?> unwrap(iface: Class<T>) = this as T

        override fun isWrapperFor(iface: Class<*>): Boolean = iface.isInstance(this)
    }
}
