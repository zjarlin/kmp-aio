package site.addzero.kcloud.jimmer.di

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.babyfish.jimmer.sql.DraftInterceptor
import org.babyfish.jimmer.sql.dialect.DefaultDialect
import org.babyfish.jimmer.sql.dialect.Dialect
import org.babyfish.jimmer.sql.dialect.PostgresDialect
import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.runtime.AbstractScalarProvider
import org.babyfish.jimmer.sql.runtime.ConnectionManager.simpleConnectionManager
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy.LOWER_CASE
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.util.db.SqlExecutor
import site.addzero.kcloud.jimmer.scalarprovider.sqllite.SqliteInstantScalarProvider
import site.addzero.kcloud.jimmer.scalarprovider.sqllite.SqliteLocalDateTimeScalarProvider
import site.addzero.kcloud.jimmer.spi.DatasourceProperties
import site.addzero.kcloud.jimmer.spi.DatasourcePropertiesSpi
import java.sql.DriverManager
import javax.sql.DataSource

@Module
@Configuration
//@ComponentScan("site.addzero.kcloud.jimmer")
class JimmerKoinModule {
    @Single
    fun dataSource(
        datasourcePropertiesSpi: DatasourcePropertiesSpi,
    ): DataSource {
        return defaultDatasourceProperties(datasourcePropertiesSpi).toDatasource()
    }

    @Single
    fun sqlClient(
        datasourcePropertiesSpi: DatasourcePropertiesSpi,
        interceptors: List<DraftInterceptor<*, *>>,
        scalarProviders: List<AbstractScalarProvider<*, *>>,
    ): KSqlClient {
        return defaultDatasourceProperties(datasourcePropertiesSpi).toKsqlClient(
            interceptors = interceptors,
            scalarProviders = scalarProviders,
        )
    }

    @Single
    fun sqlClients(
        datasourcePropertiesSpi: DatasourcePropertiesSpi,
        interceptors: List<DraftInterceptor<*, *>>,
        scalarProviders: List<AbstractScalarProvider<*, *>>,
    ): List<KSqlClient> {
        return enabledDatasourceProperties(datasourcePropertiesSpi)
            .filterNot(DatasourceProperties::default)
            .map { datasource ->
                datasource.toKsqlClient(
                    interceptors = interceptors,
                    scalarProviders = scalarProviders,
                )
            }
    }

    @Single
    fun sqlExecutor(
        dataSource: DataSource,
    ): SqlExecutor {
        return SqlExecutor(dataSource)
    }
}

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
        isPg(jdbcUrl) -> PostgresDialect()
        else -> DefaultDialect.INSTANCE
    }
}

internal fun isSqlLite(jdbcUrl: String): Boolean = jdbcUrl.startsWith("jdbc:sqlite:")

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
    return runCatching { HikariDataSource(config) }
        .getOrElse { throwable ->
            throw IllegalStateException(
                buildString {
                    append("数据库连接初始化失败: url=")
                    append(jdbcUrl)
                    append(", user=")
                    append(username ?: "<empty>")
                    append(", driver=")
                    append(driverClassName)
                },
                throwable,
            )
        }
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
private fun DatasourceProperties.toKsqlClient(
    interceptors: List<DraftInterceptor<*, *>>,
    scalarProviders: List<AbstractScalarProvider<*, *>>,
): KSqlClient {
    val jdbcUrl = url.trim()
    val dialect = guessDialect(jdbcUrl)
    val dataSource = toDatasource()
    val activeScalarProviders = when (dialect) {
        is SQLiteDialect -> scalarProviders
        else -> scalarProviders.filterNot { provider ->
            provider is SqliteInstantScalarProvider || provider is SqliteLocalDateTimeScalarProvider
        }
    }
    return newKSqlClient {
        setDialect(dialect)
        interceptors.forEach(::addDraftInterceptor)
        activeScalarProviders.forEach(::addScalarProvider)
        setDatabaseNamingStrategy(LOWER_CASE)
        setConnectionManager(simpleConnectionManager(dataSource))
    }
}

private fun defaultDatasourceProperties(
    datasourcePropertiesSpi: DatasourcePropertiesSpi,
): DatasourceProperties {
    val configured = enabledDatasourceProperties(datasourcePropertiesSpi)
    return configured.firstOrNull(DatasourceProperties::default)
        ?: configured.firstOrNull()
        ?: error("No enabled datasource found, check your DatasourcePropertiesSpi")
}

private fun enabledDatasourceProperties(
    datasourcePropertiesSpi: DatasourcePropertiesSpi,
): List<DatasourceProperties> {
    return datasourcePropertiesSpi.datasources()
        .filter { datasource ->
            datasource.enabled && datasource.url.isNotBlank()
        }
}
