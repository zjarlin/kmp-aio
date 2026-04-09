package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import java.nio.file.Files
import java.nio.file.Path
import java.sql.DriverManager
import javax.sql.DataSource
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.random.Random
import org.babyfish.jimmer.sql.dialect.MySqlDialect
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.runtime.ConnectionManager.simpleConnectionManager
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy.LOWER_CASE
import org.flywaydb.core.Flyway
import site.addzero.kcloud.jimmer.jdbc.DataSourceJdbcExecutor
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenClassDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextBindingDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextBindingValueDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDefinitionDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDetailDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMethodDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenPropertyDto
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenClassKind
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenConsumerTarget

/**
 * 表示代码生成上下文testfixture。
 */
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

    /**
     * 处理关闭。
     */
    override fun close() {
        dropDatabase(databaseName)
    }

    /**
     * 处理migrate结构。
     */
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

/**
 * 处理generic上下文请求。
 *
 * @param protocolTemplateId 协议模板 ID。
 * @param availableDefinitions 可用定义。
 * @param code 编码。
 */
internal fun genericContextRequest(
    protocolTemplateId: Long,
    availableDefinitions: List<CodegenContextDefinitionDto>,
    code: String = "CTX_GENERIC_SAMPLE",
): CodegenContextDetailDto =
    CodegenContextDetailDto(
        code = code,
        name = "Generic Sample Context",
        description = "测试泛化上下文。",
        enabled = true,
        consumerTarget = CodegenConsumerTarget.MCU_CONSOLE,
        protocolTemplateId = protocolTemplateId,
        availableContextDefinitions = availableDefinitions,
        classes =
            listOf(
                CodegenClassDto(
                    name = "设备读写服务",
                    description = "聚合设备读写方法。",
                    sortIndex = 0,
                    classKind = CodegenClassKind.SERVICE,
                    className = "DeviceContract",
                    methods =
                        listOf(
                            CodegenMethodDto(
                                name = "读取板卡快照",
                                description = "读取板卡快照寄存器。",
                                sortIndex = 0,
                                methodName = "readBoardSnapshot",
                                responseClassName = "BoardSnapshot",
                                bindings =
                                    listOf(
                                        binding(
                                            MODBUS_OPERATION_DEFINITION_CODE,
                                            "direction" to "READ",
                                            "functionCode" to "READ_HOLDING_REGISTERS",
                                            "baseAddress" to "10",
                                        ),
                                    ),
                            ),
                            CodegenMethodDto(
                                name = "写入板卡配置",
                                description = "写入板卡配置寄存器。",
                                sortIndex = 10,
                                methodName = "writeBoardConfig",
                                requestClassName = "WriteBoardConfigRequest",
                                bindings =
                                    listOf(
                                        binding(
                                            MODBUS_OPERATION_DEFINITION_CODE,
                                            "direction" to "WRITE",
                                            "functionCode" to "WRITE_MULTIPLE_REGISTERS",
                                            "baseAddress" to "30",
                                        ),
                                    ),
                            ),
                        ),
                ),
                CodegenClassDto(
                    name = "板卡快照",
                    description = "读操作响应模型。",
                    sortIndex = 10,
                    classKind = CodegenClassKind.MODEL,
                    className = "BoardSnapshot",
                    properties =
                        listOf(
                            CodegenPropertyDto(
                                name = "Uptime",
                                description = "运行时长，单位毫秒。",
                                sortIndex = 0,
                                propertyName = "uptimeMs",
                                typeName = "Int",
                                bindings =
                                    listOf(
                                        binding(
                                            MODBUS_FIELD_DEFINITION_CODE,
                                            "transportType" to "U32_BE",
                                            "registerOffset" to "0",
                                            "length" to "1",
                                        ),
                                    ),
                            ),
                            CodegenPropertyDto(
                                name = "Device Label",
                                description = "设备标签。",
                                sortIndex = 1,
                                propertyName = "deviceLabel",
                                typeName = "String",
                                bindings =
                                    listOf(
                                        binding(
                                            MODBUS_FIELD_DEFINITION_CODE,
                                            "transportType" to "STRING_ASCII",
                                            "registerOffset" to "2",
                                            "length" to "4",
                                        ),
                                    ),
                            ),
                        ),
                ),
                CodegenClassDto(
                    name = "写板卡配置请求",
                    description = "写操作请求模型。",
                    sortIndex = 20,
                    classKind = CodegenClassKind.MODEL,
                    className = "WriteBoardConfigRequest",
                    properties =
                        listOf(
                            CodegenPropertyDto(
                                name = "Threshold",
                                description = "阈值。",
                                sortIndex = 0,
                                propertyName = "threshold",
                                typeName = "Int",
                                bindings =
                                    listOf(
                                        binding(
                                            MODBUS_FIELD_DEFINITION_CODE,
                                            "transportType" to "U16",
                                            "registerOffset" to "0",
                                        ),
                                    ),
                            ),
                            CodegenPropertyDto(
                                name = "Note",
                                description = "备注。",
                                sortIndex = 1,
                                propertyName = "note",
                                typeName = "String",
                                bindings =
                                    listOf(
                                        binding(
                                            MODBUS_FIELD_DEFINITION_CODE,
                                            "transportType" to "STRING_UTF8",
                                            "registerOffset" to "1",
                                            "length" to "2",
                                        ),
                                    ),
                            ),
                        ),
                ),
            ),
    )

/**
 * 创建generatorworkspace。
 */
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

/**
 * 删除workspace。
 *
 * @param workspaceRoot workspace根目录。
 */
internal fun deleteWorkspace(
    workspaceRoot: Path,
) {
    workspaceRoot.toFile().deleteRecursively()
}

/**
 * 读取golden。
 *
 * @param resourceName resource名称。
 */
internal fun readGolden(
    resourceName: String,
): String =
    checkNotNull(CodegenContextTestFixture::class.java.getResource("/golden/$resourceName")) {
        "Missing golden resource $resourceName"
    }.readText()

/**
 * 处理mysql数据来源。
 *
 * @param databaseName 数据库名称。
 */
private fun mysqlDataSource(
    databaseName: String,
): DataSource {
    Class.forName("com.mysql.cj.jdbc.Driver")
    val jdbcUrl = databaseJdbcUrl(databaseName)
    return object : DataSource {
        /**
         * 获取connection。
         */
        override fun getConnection() =
            DriverManager.getConnection(jdbcUrl, MYSQL_USER, MYSQL_PASSWORD)

        /**
         * 获取connection。
         *
         * @param username 用户名。
         * @param password 密码。
         */
        override fun getConnection(
            username: String?,
            password: String?,
        ) = DriverManager.getConnection(jdbcUrl, username ?: MYSQL_USER, password ?: MYSQL_PASSWORD)

        /**
         * 获取logwriter。
         */
        override fun getLogWriter() = null

        /**
         * 处理setlogwriter。
         *
         * @param out 输出流。
         */
        override fun setLogWriter(out: java.io.PrintWriter?) = Unit

        /**
         * 处理setlogin超时。
         *
         * @param seconds 秒数。
         */
        override fun setLoginTimeout(seconds: Int) = Unit

        /**
         * 获取login超时。
         */
        override fun getLoginTimeout() = 0

        /**
         * 获取parentlogger。
         */
        override fun getParentLogger() = java.util.logging.Logger.getLogger("com.mysql")

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any?> unwrap(iface: Class<T>) = this as T

        /**
         * 处理iswrapperfor。
         *
         * @param iface 接口类型。
         */
        override fun isWrapperFor(iface: Class<*>): Boolean = iface.isInstance(this)
    }
}

/**
 * 处理databaseJDBC地址。
 *
 * @param databaseName 数据库名称。
 */
private fun databaseJdbcUrl(
    databaseName: String,
): String {
    return "jdbc:mysql://192.168.31.133:3306/$databaseName?createDatabaseIfNotExist=true"
}

/**
 * 处理dropdatabase。
 *
 * @param databaseName 数据库名称。
 */
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

/**
 * 处理绑定。
 *
 * @param definitionCode 定义编码。
 * @param values 值。
 */
private fun binding(
    definitionCode: String,
    vararg values: Pair<String, String>,
): CodegenContextBindingDto =
    CodegenContextBindingDto(
        definitionCode = definitionCode,
        values =
            values.map { (paramCode, value) ->
                CodegenContextBindingValueDto(
                    paramCode = paramCode,
                    value = value,
                )
            },
    )
