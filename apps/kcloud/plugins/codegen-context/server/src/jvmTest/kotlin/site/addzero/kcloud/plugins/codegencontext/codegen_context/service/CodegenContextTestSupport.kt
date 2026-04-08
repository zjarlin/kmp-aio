package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import java.nio.file.Files
import java.nio.file.Path
import java.sql.DriverManager
import javax.sql.DataSource
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText
import kotlin.random.Random
import org.babyfish.jimmer.sql.dialect.MySqlDialect
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.runtime.ConnectionManager.simpleConnectionManager
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy.LOWER_CASE
import org.flywaydb.core.Flyway
import site.addzero.kcloud.jimmer.jdbc.DataSourceJdbcExecutor
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDetailDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenFieldDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenSchemaDto
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenConsumerTarget
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenFunctionCode
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenSchemaDirection
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenTransportType

internal class CodegenContextTestFixture : AutoCloseable {
    private val databaseName = "codegen_context_test_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"

    val dataSource: DataSource = mysqlDataSource(databaseName)
    val sql: KSqlClient =
        newKSqlClient {
            setDialect(MySqlDialect())
            setDatabaseNamingStrategy(LOWER_CASE)
            setConnectionManager(simpleConnectionManager(dataSource))
        }
    val jdbc = DataSourceJdbcExecutor(dataSource)
    val generator = CodegenContextContractGenerator(dataSource)
    val service = CodegenContextService(sql, jdbc, generator)
    val templateService = CodegenTemplateService(sql)

    init {
        migrateSchema()
    }

    override fun close() {
        dropDatabase(databaseName)
    }

    private fun migrateSchema() {
        Flyway.configure()
            .dataSource(databaseJdbcUrl(databaseName), MYSQL_USER, MYSQL_PASSWORD)
            .locations("classpath:db/migration/mysql")
            .baselineOnMigrate(true)
            .baselineVersion("0")
            .cleanDisabled(true)
            .validateOnMigrate(true)
            .load()
            .migrate()
    }
}

internal fun baseContextRequest(
    protocolTemplateId: Long,
    code: String = "CTX_SAMPLE",
): CodegenContextDetailDto =
    CodegenContextDetailDto(
        code = code,
        name = "Sample Context",
        description = "测试上下文。",
        enabled = true,
        consumerTarget = CodegenConsumerTarget.MCU_CONSOLE,
        protocolTemplateId = protocolTemplateId,
        schemas =
            listOf(
                CodegenSchemaDto(
                    name = "读取板卡快照",
                    description = "读取板卡快照寄存器。",
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
                                description = "运行时长，单位毫秒。",
                                sortIndex = 0,
                                propertyName = "uptimeMs",
                                transportType = CodegenTransportType.U32_BE,
                                registerOffset = 0,
                            ),
                            CodegenFieldDto(
                                name = "Device Label",
                                description = "设备标签。",
                                sortIndex = 1,
                                propertyName = "deviceLabel",
                                transportType = CodegenTransportType.STRING_ASCII,
                                registerOffset = 2,
                                length = 4,
                            ),
                        ),
                ),
                CodegenSchemaDto(
                    name = "写入板卡配置",
                    description = "写入板卡配置寄存器。",
                    sortIndex = 10,
                    direction = CodegenSchemaDirection.WRITE,
                    functionCode = CodegenFunctionCode.WRITE_MULTIPLE_REGISTERS,
                    baseAddress = 30,
                    methodName = "writeBoardConfig",
                    fields =
                        listOf(
                            CodegenFieldDto(
                                name = "Threshold",
                                description = "阈值。",
                                sortIndex = 0,
                                propertyName = "threshold",
                                transportType = CodegenTransportType.U16,
                                registerOffset = 0,
                            ),
                            CodegenFieldDto(
                                name = "Note",
                                description = "备注。",
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
    workspaceRoot.resolve("apps/kcloud/plugins/mcu-console/server/build/generated/codegen-context/jvmMain/kotlin").createDirectories()
    workspaceRoot.resolve("apps/kcloud/plugins/mcu-console/shared/build/generated/codegen-context/commonMain/kotlin").createDirectories()
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

private fun mysqlDataSource(
    databaseName: String,
): DataSource {
    Class.forName("com.mysql.cj.jdbc.Driver")
    val jdbcUrl = databaseJdbcUrl(databaseName)
    return object : DataSource {
        override fun getConnection() =
            DriverManager.getConnection(jdbcUrl, MYSQL_USER, MYSQL_PASSWORD)

        override fun getConnection(
            username: String?,
            password: String?,
        ) = DriverManager.getConnection(jdbcUrl, username ?: MYSQL_USER, password ?: MYSQL_PASSWORD)

        override fun getLogWriter() = null

        override fun setLogWriter(out: java.io.PrintWriter?) = Unit

        override fun setLoginTimeout(seconds: Int) = Unit

        override fun getLoginTimeout() = 0

        override fun getParentLogger() = java.util.logging.Logger.getLogger("com.mysql")

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any?> unwrap(iface: Class<T>) = this as T

        override fun isWrapperFor(iface: Class<*>): Boolean = iface.isInstance(this)
    }
}

private fun databaseJdbcUrl(
    databaseName: String,
): String {
    return "jdbc:mysql://192.168.31.133:3306/$databaseName?createDatabaseIfNotExist=true"
}

private fun dropDatabase(
    databaseName: String,
) {
    DriverManager.getConnection("jdbc:mysql://192.168.31.133:3306/mysql", MYSQL_USER, MYSQL_PASSWORD).use { connection ->
        connection.createStatement().use { statement ->
            statement.executeUpdate("DROP DATABASE IF EXISTS `$databaseName`")
        }
    }
}

private const val MYSQL_USER = "root"
private const val MYSQL_PASSWORD = "test123456"
