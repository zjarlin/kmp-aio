package site.addzero.kcloud.plugins.hostconfig.service

import java.io.File
import java.sql.DriverManager
import javax.sql.DataSource
import kotlin.random.Random
import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.runtime.ConnectionManager.simpleConnectionManager
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy.LOWER_CASE
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.DeviceResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolResponse
import site.addzero.util.db.SqlExecutor

/**
 * 表示项目服务testfixture。
 */
internal class ProjectServiceTestFixture : AutoCloseable {
    private val databaseFile =
        File(
            System.getProperty("java.io.tmpdir"),
            "host_config_test_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}.sqlite",
        )

    val dataSource: DataSource = sqliteDataSource(databaseFile)
    val sql: KSqlClient =
        newKSqlClient {
            setDialect(SQLiteDialect())
            setDatabaseNamingStrategy(LOWER_CASE)
            setConnectionManager(simpleConnectionManager(dataSource))
        }
    val jdbc = SqlExecutor(dataSource)
    val service = ProjectService(sql, jdbc)

    init {
        migrateSchema()
    }

    /**
     * 创建项目。
     *
     * @param name 名称。
     */
    fun createProject(name: String): ProjectResponse {
        return service.createProject(
            ProjectCreateRequest(
                name = name,
                description = "$name 描述",
                remark = "$name 备注",
                sortIndex = 0,
            ),
        )
    }

    /**
     * 创建协议。
     *
     * @param projectId 项目 ID。
     * @param name 名称。
     */
    fun createProtocol(
        projectId: Long,
        name: String,
    ): ProtocolResponse {
        return service.createProtocol(
            projectId = projectId,
            request =
                ProtocolCreateRequest(
                    name = name,
                    protocolTemplateId = 1,
                    pollingIntervalMs = 1_000,
                    sortIndex = 0,
                ),
        )
    }

    /**
     * 创建模块。
     *
     * @param deviceId 设备 ID。
     * @param name 名称。
     */
    fun createModule(
        deviceId: Long,
        name: String,
    ): ModuleResponse {
        return service.createModule(
            deviceId = deviceId,
            request =
                ModuleCreateRequest(
                    name = name,
                    moduleTemplateId = 1,
                    sortIndex = 0,
                ),
        )
    }

    /**
     * 创建设备。
     *
     * @param protocolId 协议 ID。
     * @param name 名称。
     */
    fun createDevice(
        protocolId: Long,
        name: String,
    ): DeviceResponse {
        return service.createDevice(
            protocolId = protocolId,
            request =
                DeviceCreateRequest(
                    name = name,
                    deviceTypeId = 1,
                    stationNo = 1,
                    sortIndex = 0,
                ),
        )
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
        ensureHostConfigSqliteSchema(dataSource)
    }
}

/**
 * 处理sqlite数据来源。
 *
 * @param databaseFile 数据库文件。
 */
private fun sqliteDataSource(databaseFile: File): DataSource {
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
