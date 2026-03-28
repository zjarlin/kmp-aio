package site.addzero.kcloud.jimmer.di

import io.ktor.server.config.ApplicationConfig
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy
import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Property
import org.koin.core.annotation.Single
import site.addzero.kcloud.jimmer.interceptor.BaseEntityDraftInterceptor
import site.addzero.kcloud.jimmer.spi.DatabaseDriverSpi
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource

private const val APPLICATION_CONFIG_PROPERTY = "vibepocket.applicationConfig"
private const val EMBEDDED_DESKTOP_MODE_PROPERTY = "vibepocket.embedded.desktop"
private const val DEFAULT_EMBEDDED_SQLITE_URL = "jdbc:sqlite:vibepocket-desktop.db"

/**
 * 数据源属性（从 application.conf 的 datasources.xxx 读取）
 */
data class DatasourceProperties(
    val name: String = "",
    val enabled: Boolean = false,
    val url: String = "",
    val driver: String = "",
    val user: String = "",
    val password: String = "",
)

@Module
@Configuration("vibepocket")
@ComponentScan("site.addzero.kcloud.jimmer")
class JimmerKoinModule {
    @Single
    fun provideDefaultDataSource(registry: DatasourceRegistry): DataSource {
        return registry.defaultDataSource()
    }

    @Single
    fun provideDefaultSqlClient(registry: DatasourceRegistry): KSqlClient {
        return registry.defaultSqlClient()
    }
}

@Single
class DatabaseDriverResolver(
    private val drivers: List<DatabaseDriverSpi>,
) {
    fun resolve(driver: String): DatabaseDriverSpi {
        return drivers.firstOrNull { it.supports(driver) }
            ?: throw IllegalArgumentException(
                "No DatabaseDriverSpi found for driver '$driver'. Available: ${drivers.map { it::class.simpleName }}",
            )
    }
}

/**
 * 公共 KSqlClient 构建器
 */
private fun buildSqlClient(
    dataSource: DataSource,
    spi: DatabaseDriverSpi,
    interceptor: BaseEntityDraftInterceptor,
): KSqlClient = newKSqlClient {
    val dialect = spi.dialect()
    setDialect(dialect)
    addDraftInterceptor(interceptor)
    setDatabaseNamingStrategy(DefaultDatabaseNamingStrategy.LOWER_CASE)
    if (dialect is SQLiteDialect) {
        addScalarProvider(SqliteLocalDateTimeScalarProvider)
    }
    setConnectionManager { dataSource.connection.use { proceed(it) } }
}

private fun loadSqlFile(filename: String): String =
    object {}.javaClass.getResource("/sql/$filename")?.readText() ?: ""

/**
 * 执行 schema 初始化脚本
 */
private fun runSchema(dataSource: DataSource, schemaFile: String) {
    val sql = loadSqlFile(schemaFile)
    if (sql.isBlank()) return
    dataSource.connection.use { conn ->
        conn.createStatement().use { stmt ->
            sql.split(";")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .forEach { stmt.executeUpdate(it) }
        }
    }
}

private fun normalizeSqliteEpochDateTimeColumns(dataSource: DataSource) {
    val tableColumns = mapOf(
        "music_task" to listOf("created_at", "updated_at"),
        "favorite_track" to listOf("created_at"),
        "music_history" to listOf("created_at"),
        "persona_record" to listOf("created_at"),
        "suno_task_resource" to listOf("created_at", "updated_at"),
    )
    dataSource.connection.use { conn ->
        conn.createStatement().use { stmt ->
            tableColumns.forEach { (table, columns) ->
                columns.forEach { column ->
                    stmt.executeUpdate(
                        """
                        UPDATE $table
                        SET $column = datetime(CAST($column AS INTEGER) / 1000, 'unixepoch', 'localtime')
                        WHERE trim($column) GLOB '[0-9]*'
                          AND length(trim($column)) >= 10
                          AND instr($column, '-') = 0
                          AND instr($column, ':') = 0
                        """.trimIndent()
                    )
                }
            }
        }
    }
}

private data class DatasourceRuntime(
    val properties: DatasourceProperties,
    val dataSource: DataSource,
    val sqlClient: KSqlClient,
)

@Single(createdAtStart = true)
class DatasourceRegistry(
    @Property(APPLICATION_CONFIG_PROPERTY)
    private val config: ApplicationConfig,
    private val interceptor: BaseEntityDraftInterceptor,
    private val driverResolver: DatabaseDriverResolver,
) {
    private val runtimesByName: LinkedHashMap<String, DatasourceRuntime> = buildRuntimeMap(config)
    private val defaultRuntime: DatasourceRuntime = runtimesByName.values.firstOrNull()
        ?: throw IllegalStateException(
            "No enabled datasource configured. Please configure at least one datasources.*.enabled=true entry.",
        )

    fun defaultDataSource(): DataSource {
        return defaultRuntime.dataSource
    }

    fun defaultSqlClient(): KSqlClient {
        return defaultRuntime.sqlClient
    }

    fun dataSource(name: String): DataSource {
        return runtime(name).dataSource
    }

    fun sqlClient(name: String): KSqlClient {
        return runtime(name).sqlClient
    }

    private fun runtime(name: String): DatasourceRuntime {
        return runtimesByName[name]
            ?: throw IllegalArgumentException(
                "Datasource '$name' is not configured. Available: ${runtimesByName.keys.joinToString()}",
            )
    }

    private fun buildRuntimeMap(config: ApplicationConfig): LinkedHashMap<String, DatasourceRuntime> {
        val datasources = loadAllDatasources(config)
            .filter { it.enabled }
            .ifEmpty {
                fallbackEmbeddedDesktopDatasource(config)?.let(::listOf) ?: emptyList()
            }

        val runtimes = LinkedHashMap<String, DatasourceRuntime>()
        for (props in datasources) {
            val spi = driverResolver.resolve(props.driver)
            val dataSource = spi.createDataSource(props)
            spi.schemaFile()?.let { runSchema(dataSource, it) }
            if (spi.dialect() is SQLiteDialect) {
                normalizeSqliteEpochDateTimeColumns(dataSource)
            }
            val sqlClient = buildSqlClient(dataSource, spi, interceptor)
            runtimes[props.name] = DatasourceRuntime(props, dataSource, sqlClient)
        }
        return runtimes
    }
}

private fun fallbackEmbeddedDesktopDatasource(config: ApplicationConfig): DatasourceProperties? {
    val isEmbeddedDesktop = System.getProperty(EMBEDDED_DESKTOP_MODE_PROPERTY)
        ?.toBooleanStrictOrNull()
        ?: false
    if (!isEmbeddedDesktop) {
        return null
    }

    val sqliteUrl = config.propertyOrNull("datasources.sqlite.url")
        ?.getString()
        ?.takeIf { it.isNotBlank() }
        ?: DEFAULT_EMBEDDED_SQLITE_URL
    val sqliteDriver = config.propertyOrNull("datasources.sqlite.driver")
        ?.getString()
        ?.takeIf { it.isNotBlank() }
        ?: "org.sqlite.SQLiteDriver"

    return DatasourceProperties(
        name = "sqlite",
        enabled = true,
        url = sqliteUrl,
        driver = sqliteDriver,
    )
}

/**
 * 对指定 DataSource 执行 SQLite schema 初始化（测试用）
 */
fun initDatabase(dataSource: DataSource) {
    runSchema(dataSource, "schema-sqlite.sql")
}

/**
 * 动态数据源管理器（运行时按 dbType + url 创建临时连接，同样走 SPI）
 */
@Single
class DataSourceManager(
    private val interceptor: BaseEntityDraftInterceptor,
    private val driverResolver: DatabaseDriverResolver,
) {
    private val cache = ConcurrentHashMap<String, KSqlClient>()

    fun getSqlClient(dbType: String, url: String): KSqlClient {
        val key = "$dbType:$url"
        return cache.getOrPut(key) {
            val spi = driverResolver.resolve(dbType)
            val props = DatasourceProperties(name = key, enabled = true, url = url, driver = dbType)
            val dataSource = spi.createDataSource(props)
            buildSqlClient(dataSource, spi, interceptor)
        }
    }
}

private fun loadAllDatasources(config: ApplicationConfig): List<DatasourceProperties> {
    val datasources = mutableListOf<DatasourceProperties>()
    val datasourceConfig = runCatching { config.config("datasources") }.getOrNull() ?: return emptyList()

    for (name in datasourceConfig.toMap().keys) {
        val section = runCatching { datasourceConfig.config(name) }.getOrNull() ?: continue
        datasources += DatasourceProperties(
            name = name,
            enabled = section.propertyOrNull("enabled")?.getString()?.toBoolean() == true,
            url = section.propertyOrNull("url")?.getString().orEmpty(),
            driver = section.propertyOrNull("driver")?.getString().orEmpty(),
            user = section.propertyOrNull("user")?.getString().orEmpty(),
            password = section.propertyOrNull("password")?.getString().orEmpty(),
        )
    }

    return datasources
}
