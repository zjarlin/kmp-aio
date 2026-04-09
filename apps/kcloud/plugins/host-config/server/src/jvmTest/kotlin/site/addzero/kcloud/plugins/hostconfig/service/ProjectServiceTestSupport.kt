package site.addzero.kcloud.plugins.hostconfig.service

import java.sql.DriverManager
import javax.sql.DataSource
import kotlin.random.Random
import org.babyfish.jimmer.sql.dialect.MySqlDialect
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.runtime.ConnectionManager.simpleConnectionManager
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy.LOWER_CASE
import org.flywaydb.core.Flyway
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ModuleResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolResponse
import site.addzero.util.db.SqlExecutor

/**
 * 表示项目服务testfixture。
 */
internal class ProjectServiceTestFixture : AutoCloseable {
    private val databaseName = "host_config_test_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"

    val dataSource: DataSource = mysqlDataSource(databaseName)
    val sql: KSqlClient =
        newKSqlClient {
            setDialect(MySqlDialect())
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
     * @param protocolId 协议 ID。
     * @param name 名称。
     */
    fun createModule(
        protocolId: Long,
        name: String,
    ): ModuleResponse {
        return service.createModule(
            protocolId = protocolId,
            request =
                ModuleCreateRequest(
                    name = name,
                    moduleTemplateId = 1,
                    sortIndex = 0,
                ),
        )
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
 * 处理mysql数据来源。
 *
 * @param databaseName 数据库名称。
 */
private fun mysqlDataSource(databaseName: String): DataSource {
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
private fun databaseJdbcUrl(databaseName: String): String {
    return "jdbc:mysql://192.168.31.133:3306/$databaseName?createDatabaseIfNotExist=true"
}

/**
 * 处理dropdatabase。
 *
 * @param databaseName 数据库名称。
 */
private fun dropDatabase(databaseName: String) {
    DriverManager.getConnection("jdbc:mysql://192.168.31.133:3306/mysql", MYSQL_USER, MYSQL_PASSWORD).use { connection ->
        connection.createStatement().use { statement ->
            statement.executeUpdate("DROP DATABASE IF EXISTS `$databaseName`")
        }
    }
}

private const val MYSQL_USER = "root"
private const val MYSQL_PASSWORD = "test123456"
