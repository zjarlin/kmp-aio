package site.addzero.kcloud.jimmer.di

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.babyfish.jimmer.sql.DraftInterceptor
import org.babyfish.jimmer.sql.dialect.DefaultDialect
import org.babyfish.jimmer.sql.dialect.Dialect
import org.babyfish.jimmer.sql.dialect.MySqlDialect
import org.babyfish.jimmer.sql.dialect.PostgresDialect
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import org.babyfish.jimmer.sql.runtime.AbstractScalarProvider
import org.babyfish.jimmer.sql.runtime.ConnectionManager.simpleConnectionManager
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy.LOWER_CASE
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.mp.KoinPlatform
import site.addzero.kcloud.jimmer.scalarprovider.sqllite.SqliteInstantScalarProvider
import site.addzero.kcloud.jimmer.scalarprovider.sqllite.SqliteLocalDateTimeScalarProvider
import site.addzero.kcloud.jimmer.spi.DatasourcePropertiesSpi
import java.sql.DriverManager
import javax.sql.DataSource

// 假设你的 DatasourcePropertiesSpi 数据类定义（如果未定义需补充）
internal fun DatasourcePropertiesSpi.toDatasource(): DataSource {
    // 1. 校验核心参数
    require(url.isNotBlank()) { "数据库URL不能为空" }

    // 2. 解析URL并适配不同数据库类型
    val jdbcUrl = url.trim()
    return when {
        // PostgreSQL 处理
        isPg(jdbcUrl) -> {
            createHikariDataSource(
                jdbcUrl = jdbcUrl,
                username = user,
                password = password,
                driverClassName = driverClassName ?: "org.postgresql.Driver"
            )
        }
        // MySQL 处理（兼容 mariadb）
        isMysq(jdbcUrl) -> {
            createHikariDataSource(
                jdbcUrl = jdbcUrl,
                username = user,
                password = password,
                driverClassName = driverClassName ?: "com.mysql.cj.jdbc.Driver",
                // MySQL 特有配置
                extraProperties = mapOf(
                    "useUnicode" to "true",
                    "characterEncoding" to "UTF-8",
                    "serverTimezone" to "UTC",
                    "allowPublicKeyRetrieval" to "true",
                    "useSSL" to "false"
                )
            )
        }
        // SQLite 处理（无需用户名密码）
        isSqlLite(jdbcUrl) -> {
            createSqliteDataSource(jdbcUrl, driverClassName ?: "org.sqlite.JDBC")
        }

        else -> throw IllegalArgumentException("不支持的数据库类型，URL: $jdbcUrl")
    }
}

// 核心方法实现（复用已有的判断方法）
internal fun guessDialect(jdbcUrl: String): Dialect {
    return when {
        isSqlLite(jdbcUrl) -> SQLiteDialect()
        isMysq(jdbcUrl) -> MySqlDialect()
        isPg(jdbcUrl) -> PostgresDialect()
        else -> DefaultDialect.INSTANCE
    }
}

internal fun isSqlLite(jdbcUrl: String): Boolean = jdbcUrl.startsWith("jdbc:sqlite:")

internal fun isMysq(jdbcUrl: String): Boolean = jdbcUrl.startsWith("jdbc:mysql:") || jdbcUrl.startsWith("jdbc:mariadb:")

internal fun isPg(jdbcUrl: String): Boolean {
    val startsWith = jdbcUrl.startsWith("jdbc:postgresql:")
    return startsWith
}

/**
 * 创建 HikariCP 连接池（适配 PG/MySQL）
 */
internal fun createHikariDataSource(
    jdbcUrl: String,
    username: String?,
    password: String?,
    driverClassName: String,
    extraProperties: Map<String, String> = emptyMap()
): HikariDataSource {
    val config = HikariConfig().apply {
        this.jdbcUrl = jdbcUrl
        this.username = username
        this.password = password
        this.driverClassName = driverClassName

        // 基础连接池配置（可根据需求调整）
        maximumPoolSize = 10
        minimumIdle = 2
        idleTimeout = 300000 // 5分钟
        connectionTimeout = 30000 // 30秒
        poolName = "db-pool-${jdbcUrl.substringAfterLast(":").substringBefore("/")}"
        // 附加属性
        extraProperties.forEach { (key, value) ->
            addDataSourceProperty(key, value)
        }
    }
    return HikariDataSource(config)
}

/**
 * 创建 SQLite 数据源（SQLite 无需连接池，直接返回基础 DataSource）
 */
internal fun createSqliteDataSource(jdbcUrl: String, driverClassName: String): DataSource {
    // 加载驱动
    Class.forName(driverClassName)

    // SQLite 简单 DataSource 实现（也可使用 HikariCP，此处简化）
    return object : DataSource {
        override fun getConnection() = DriverManager.getConnection(jdbcUrl)
        override fun getConnection(username: String?, password: String?) = getConnection()
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


/**
 * 动态数据源管理器（运行时按 dbType + url 创建临时连接，同样走 SPI）
 */
@Single
class DataSourceManager(
    private val interceptors: List<DraftInterceptor<*, *>>,
    private val datasourcePropertiesSpis: List<DatasourcePropertiesSpi>,
    private val scalarProviders: List<AbstractScalarProvider<*, *>>
) {


    fun DatasourcePropertiesSpi.toKsqlClient(): KSqlClient {
        val url = this.url
        val guessDialect = guessDialect(url)
        val toDatasource = this.toDatasource()
        val newKSqlClient = newKSqlClient {
            setDialect(guessDialect)
            //设置拦截器
            interceptors.forEach { addDraftInterceptor(it) }
            //转换器
            scalarProviders.forEach { addScalarProvider(it) }
            setDatabaseNamingStrategy(LOWER_CASE)

            if (guessDialect is SQLiteDialect) {
                addScalarProvider(KoinPlatform.getKoin().get<SqliteInstantScalarProvider>())
                addScalarProvider(KoinPlatform.getKoin().get<SqliteLocalDateTimeScalarProvider>())
            }
            setConnectionManager(simpleConnectionManager(toDatasource))
        }
        return newKSqlClient

    }

    @Single
    fun dataSource(sqlclient: KSqlClient): DataSource {
        val toDatasource = defaultDatasourcePropertiesSpi().toDatasource()
        return toDatasource
    }


    @Single
    fun sqlClient(): KSqlClient {
        val toKsqlClient = defaultDatasourcePropertiesSpi().toKsqlClient()
        return toKsqlClient

    }

    private fun defaultDatasourcePropertiesSpi(): DatasourcePropertiesSpi =
        datasourcePropertiesSpis.filter { it.default }.firstOrNull()
            ?: throw IllegalArgumentException("No enabled datasource found, check your DatasourcePropertiesSpi")

    @Single
    fun sqlClients(): List<KSqlClient> {
        val map = datasourcePropertiesSpis.filterNot { it.default }.map { it.toKsqlClient() }
        return map
    }
}

