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
import site.addzero.kcloud.jimmer.spi.DatasourceProperties
import site.addzero.kcloud.jimmer.spi.DatasourcePropertiesSpi
import java.sql.DriverManager
import javax.sql.DataSource

internal fun DatasourceProperties.toDatasource(): DataSource {
    require(url.isNotBlank()) { "数据库URL不能为空" }

    val jdbcUrl = url.trim()
    return when {
        isPg(jdbcUrl) -> {
            createHikariDataSource(
                jdbcUrl = jdbcUrl,
                username = user,
                password = password,
                driverClassName = driverClassName.ifBlank { "org.postgresql.Driver" }
            )
        }

        isMysq(jdbcUrl) -> {
            createHikariDataSource(
                jdbcUrl = jdbcUrl,
                username = user,
                password = password,
                driverClassName = driverClassName.ifBlank { "com.mysql.cj.jdbc.Driver" },
                extraProperties = mapOf(
                    "useUnicode" to "true",
                    "characterEncoding" to "UTF-8",
                    "serverTimezone" to "UTC",
                    "allowPublicKeyRetrieval" to "true",
                    "useSSL" to "false"
                )
            )
        }

        isSqlLite(jdbcUrl) -> {
            createSqliteDataSource(
                jdbcUrl = jdbcUrl,
                driverClassName = driverClassName.ifBlank { "org.sqlite.JDBC" },
            )
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

        maximumPoolSize = 10
        minimumIdle = 2
        idleTimeout = 300000
        connectionTimeout = 30000
        poolName = "db-pool-${jdbcUrl.substringAfterLast(":").substringBefore("/")}"
        extraProperties.forEach { (key, value) ->
            addDataSourceProperty(key, value)
        }
    }
    return HikariDataSource(config)
}

/**
 */
internal fun createSqliteDataSource(jdbcUrl: String, driverClassName: String): DataSource {
    Class.forName(driverClassName)

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
    private val datasourcePropertiesSpi: DatasourcePropertiesSpi,
    private val scalarProviders: List<AbstractScalarProvider<*, *>>
) {
    fun DatasourceProperties.toKsqlClient(): KSqlClient {
        val url = this.url
        val guessDialect = guessDialect(url)
        val toDatasource = this.toDatasource()
        val newKSqlClient = newKSqlClient {
            setDialect(guessDialect)
            interceptors.forEach { addDraftInterceptor(it) }
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

    private fun defaultDatasourcePropertiesSpi(): DatasourceProperties {
        val configured = datasourceProperties()
        return configured.firstOrNull { it.default }
            ?: configured.firstOrNull()
            ?: throw IllegalArgumentException("No enabled datasource found, check your DatasourcePropertiesSpi")
    }

    @Single
    fun sqlClients(): List<KSqlClient> {
        return datasourceProperties()
            .filterNot { it.default }
            .map { it.toKsqlClient() }
    }

    private fun datasourceProperties(): List<DatasourceProperties> {
        return datasourcePropertiesSpi.datasources()
            .filter { datasource ->
                datasource.enabled && datasource.url.isNotBlank()
            }
    }
}
