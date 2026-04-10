package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.sql.DriverManager
import javax.sql.DataSource
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.random.Random
import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.runtime.ConnectionManager.simpleConnectionManager
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy.LOWER_CASE
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenClassDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextBindingDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextBindingValueDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDefinitionDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDetailDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenMethodDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenPropertyDto
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenClassKind
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenConsumerTarget

private const val HOST_CONFIG_SQLITE_SCHEMA_RESOURCE = "/db/sqlite/host-config-local-schema.sql"

/**
 * 表示代码生成上下文testfixture。
 */
internal class CodegenContextTestFixture : AutoCloseable {
    private val databaseFile =
        File(
            System.getProperty("java.io.tmpdir"),
            "codegen_context_test_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}.sqlite",
        )

    val dataSource: DataSource = sqliteDataSource(databaseFile)
    val sql: KSqlClient =
        newKSqlClient {
            setDialect(SQLiteDialect())
            setDatabaseNamingStrategy(LOWER_CASE)
            setConnectionManager(simpleConnectionManager(dataSource))
        }
    val generator = CodegenContextContractGenerator(dataSource)
    val service = CodegenContextService(sql, generator)
    val templateService = CodegenTemplateService(sql)

    init {
        migrateSchema()
    }

    /**
     * 处理关闭。
     */
    override fun close() {
        databaseFile.delete()
    }

    /**
     * 处理migrate结构。
     */
    private fun migrateSchema() {
        ensureHostConfigReferenceSchema(dataSource)
        ensureCodegenContextSqliteSchema(dataSource)
    }
}

/**
 * 创建位于家目录下的临时目录。
 */
internal fun createHomeScopedTempDirectory(
    prefix: String,
): Path {
    return Files.createTempDirectory(Path.of(System.getProperty("user.home")), prefix)
}

/**
 * 转换为以家目录开头的缩写路径。
 */
internal fun Path.toHomeTokenPath(
    token: String,
): String {
    val homeDirectory = Path.of(System.getProperty("user.home")).toAbsolutePath().normalize()
    val normalized = toAbsolutePath().normalize()
    val relative = homeDirectory.relativize(normalized)
    val separator = if (relative.nameCount == 0) "" else "/"
    val suffix = relative.joinToString("/") { segment -> segment.toString() }
    return token + separator + suffix
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
    workspaceRoot.resolve(".mxproject").writeText("mxproject")
    workspaceRoot.resolve("apps/kcloud/plugins/mcu-console/server/generated/jvmMain/kotlin").createDirectories()
    workspaceRoot.resolve("apps/kcloud/plugins/mcu-console/shared/generated/commonMain/kotlin").createDirectories()
    workspaceRoot.resolve("apps/kcloud/plugins/mcu-console/server/build/generated/source/codegen-context/jvmMain/kotlin").createDirectories()
    workspaceRoot.resolve("apps/kcloud/plugins/mcu-console/shared/build/generated/source/codegen-context/commonMain/kotlin").createDirectories()
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
 * 处理sqlite数据来源。
 *
 * @param databaseFile 数据库文件。
 */
private fun sqliteDataSource(
    databaseFile: File,
): DataSource {
    databaseFile.parentFile?.mkdirs()
    if (databaseFile.exists()) {
        databaseFile.delete()
    }
    val jdbcUrl = "jdbc:sqlite:${databaseFile.absolutePath}"
    Class.forName("org.sqlite.JDBC")
    return object : DataSource {
        override fun getConnection() = DriverManager.getConnection(jdbcUrl)

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

private fun ensureHostConfigReferenceSchema(
    dataSource: DataSource,
) {
    val script =
        checkNotNull(CodegenContextTestFixture::class.java.getResource(HOST_CONFIG_SQLITE_SCHEMA_RESOURCE)) {
            "Missing host-config SQLite schema resource: $HOST_CONFIG_SQLITE_SCHEMA_RESOURCE"
        }.readText()
    dataSource.connection.use { connection ->
        connection.createStatement().use { statement ->
            statement.execute("PRAGMA foreign_keys = ON")
            script.splitToSequence(';')
                .map(String::trim)
                .filter(String::isNotBlank)
                .forEach(statement::execute)
        }
    }
}

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
