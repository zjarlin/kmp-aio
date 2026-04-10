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
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.mp.KoinPlatform
import site.addzero.kcloud.jimmer.scalarprovider.sqllite.SqliteInstantScalarProvider
import site.addzero.kcloud.jimmer.scalarprovider.sqllite.SqliteLocalDateTimeScalarProvider
import site.addzero.kcloud.jimmer.spi.DatasourceProperties
import site.addzero.kcloud.jimmer.spi.DatasourcePropertiesSpi
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource

fun <T> KSqlClient.withConnection(
    block: (Connection) -> T,
): T {
    return javaClient.connectionManager.execute(block)
}

fun <T> KSqlClient.withTransaction(
    block: (Connection) -> T,
): T {
    return transaction {
        withConnection(block)
    }
}

fun KSqlClient.queryForList(
    sql: String,
    params: List<Any?> = emptyList(),
): List<Map<String, Any?>> {
    return query(sql, params) { resultSet ->
        resultSet.toRowMap()
    }
}

fun KSqlClient.queryForList(
    sql: String,
    vararg params: Any?,
): List<Map<String, Any?>> {
    return queryForList(sql, params.toList())
}

fun KSqlClient.queryForList(
    connection: Connection,
    sql: String,
    params: List<Any?> = emptyList(),
): List<Map<String, Any?>> {
    return query(connection, sql, params) { resultSet ->
        resultSet.toRowMap()
    }
}

fun KSqlClient.queryForList(
    connection: Connection,
    sql: String,
    vararg params: Any?,
): List<Map<String, Any?>> {
    return queryForList(connection, sql, params.toList())
}

fun <T> KSqlClient.query(
    sql: String,
    params: List<Any?> = emptyList(),
    mapper: (ResultSet) -> T,
): List<T> {
    return withConnection { connection ->
        query(connection, sql, params, mapper)
    }
}

fun <T> KSqlClient.query(
    sql: String,
    vararg params: Any?,
    mapper: (ResultSet) -> T,
): List<T> {
    return query(sql, params.toList(), mapper)
}

fun <T> KSqlClient.query(
    connection: Connection,
    sql: String,
    params: List<Any?> = emptyList(),
    mapper: (ResultSet) -> T,
): List<T> {
    prepareStatement(connection, sql, params).use { statement ->
        statement.executeQuery().use { resultSet ->
            return buildList {
                while (resultSet.next()) {
                    add(mapper(resultSet))
                }
            }
        }
    }
}

fun <T> KSqlClient.query(
    connection: Connection,
    sql: String,
    vararg params: Any?,
    mapper: (ResultSet) -> T,
): List<T> {
    return query(connection, sql, params.toList(), mapper)
}

fun KSqlClient.queryIds(
    sql: String,
    params: List<Any?> = emptyList(),
): MutableList<Long> {
    return query(sql, params) { resultSet ->
        resultSet.getLong(1)
    }.toMutableList()
}

fun KSqlClient.queryIds(
    sql: String,
    vararg params: Any?,
): MutableList<Long> {
    return queryIds(sql, params.toList())
}

fun KSqlClient.queryIds(
    connection: Connection,
    sql: String,
    params: List<Any?> = emptyList(),
): MutableList<Long> {
    return query(connection, sql, params) { resultSet ->
        resultSet.getLong(1)
    }.toMutableList()
}

fun KSqlClient.queryIds(
    connection: Connection,
    sql: String,
    vararg params: Any?,
): MutableList<Long> {
    return queryIds(connection, sql, params.toList())
}

fun KSqlClient.queryCount(
    sql: String,
    params: List<Any?> = emptyList(),
): Long {
    return query(sql, params) { resultSet ->
        resultSet.getLong(1)
    }.firstOrNull() ?: 0L
}

fun KSqlClient.queryCount(
    sql: String,
    vararg params: Any?,
): Long {
    return queryCount(sql, params.toList())
}

fun KSqlClient.queryCount(
    connection: Connection,
    sql: String,
    params: List<Any?> = emptyList(),
): Long {
    return query(connection, sql, params) { resultSet ->
        resultSet.getLong(1)
    }.firstOrNull() ?: 0L
}

fun KSqlClient.queryCount(
    connection: Connection,
    sql: String,
    vararg params: Any?,
): Long {
    return queryCount(connection, sql, params.toList())
}

fun KSqlClient.executeUpdate(
    sql: String,
    params: List<Any?> = emptyList(),
): Int {
    return withConnection { connection ->
        executeUpdate(connection, sql, params)
    }
}

fun KSqlClient.executeUpdate(
    sql: String,
    vararg params: Any?,
): Int {
    return executeUpdate(sql, params.toList())
}

fun KSqlClient.executeUpdate(
    connection: Connection,
    sql: String,
    params: List<Any?> = emptyList(),
): Int {
    prepareStatement(connection, sql, params).use { statement ->
        return statement.executeUpdate()
    }
}

fun KSqlClient.executeUpdate(
    connection: Connection,
    sql: String,
    vararg params: Any?,
): Int {
    return executeUpdate(connection, sql, params.toList())
}

fun KSqlClient.update(
    sql: String,
    params: List<Any?> = emptyList(),
): Int {
    return executeUpdate(sql, params)
}

fun KSqlClient.update(
    sql: String,
    vararg params: Any?,
): Int {
    return executeUpdate(sql, params.toList())
}

fun KSqlClient.update(
    connection: Connection,
    sql: String,
    params: List<Any?> = emptyList(),
): Int {
    return executeUpdate(connection, sql, params)
}

fun KSqlClient.update(
    connection: Connection,
    sql: String,
    vararg params: Any?,
): Int {
    return executeUpdate(connection, sql, params.toList())
}

fun KSqlClient.batchUpdate(
    sql: String,
    batchParams: List<List<Any?>>,
): IntArray {
    if (batchParams.isEmpty()) {
        return intArrayOf()
    }
    return withConnection { connection ->
        batchUpdate(connection, sql, batchParams)
    }
}

fun KSqlClient.batchUpdate(
    connection: Connection,
    sql: String,
    batchParams: List<List<Any?>>,
): IntArray {
    if (batchParams.isEmpty()) {
        return intArrayOf()
    }
    connection.prepareStatement(sql).use { statement ->
        batchParams.forEach { params ->
            statement.clearParameters()
            statement.bindParams(params)
            statement.addBatch()
        }
        return statement.executeBatch()
    }
}

fun KSqlClient.execute(
    sql: String,
    params: List<Any?> = emptyList(),
) {
    withConnection { connection ->
        execute(connection, sql, params)
    }
}

fun KSqlClient.execute(
    sql: String,
    vararg params: Any?,
) {
    execute(sql, params.toList())
}

fun KSqlClient.execute(
    connection: Connection,
    sql: String,
    params: List<Any?> = emptyList(),
) {
    prepareStatement(connection, sql, params).use { statement ->
        statement.execute()
    }
}

fun KSqlClient.execute(
    connection: Connection,
    sql: String,
    vararg params: Any?,
) {
    execute(connection, sql, params.toList())
}

fun KSqlClient.insertAndReturnId(
    sql: String,
    params: List<Any?> = emptyList(),
): Long {
    return withConnection { connection ->
        insertAndReturnId(connection, sql, params)
    }
}

fun KSqlClient.insertAndReturnId(
    sql: String,
    vararg params: Any?,
): Long {
    return insertAndReturnId(sql, params.toList())
}

fun KSqlClient.insertAndReturnId(
    connection: Connection,
    sql: String,
    params: List<Any?> = emptyList(),
): Long {
    connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS).use { statement ->
        statement.bindParams(params)
        statement.executeUpdate()
        statement.generatedKeys.use { generatedKeys ->
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1)
            }
        }
    }
    error("Insert did not return generated id")
}

fun KSqlClient.insertAndReturnId(
    connection: Connection,
    sql: String,
    vararg params: Any?,
): Long {
    return insertAndReturnId(connection, sql, params.toList())
}

fun KSqlClient.sql(
    cmd: String,
    params: List<Any?> = emptyList(),
): List<Map<String, Any?>> {
    return queryForList(cmd, params)
}

fun KSqlClient.useDatasource(
    name: String,
): KSqlClient {
    val registry = KoinPlatform.getKoin().get<DatasourceRegistry>()
    return registry.sqlClient(name)
}

fun KSqlClient.useDatasource(
    properties: DatasourceProperties,
): KSqlClient {
    val manager = KoinPlatform.getKoin().get<DataSourceManager>()
    return manager.getSqlClient(properties)
}

fun KSqlClient.useTemporaryDatasource(
    name: String = "dynamic",
    url: String,
    driverClassName: String,
    user: String = "",
    password: String = "",
): KSqlClient {
    return useDatasource(
        DatasourceProperties(
            name = name,
            url = url,
            driverClassName = driverClassName,
            user = user,
            password = password,
        ),
    )
}

fun DataSource.toRawKSqlClient(
    dialect: Dialect,
    interceptors: List<DraftInterceptor<*, *>> = emptyList(),
    scalarProviders: List<AbstractScalarProvider<*, *>> = emptyList(),
): KSqlClient {
    return newKSqlClient {
        setDialect(dialect)
        interceptors.forEach(::addDraftInterceptor)
        scalarProviders.forEach(::addScalarProvider)
        setDatabaseNamingStrategy(LOWER_CASE)
        setConnectionManager(simpleConnectionManager(this@toRawKSqlClient))
    }
}

@Module
@Configuration
//@ComponentScan("site.addzero.kcloud.jimmer")
class JimmerKoinModule {
    @Single(createdAtStart = true)
    fun datasourceRegistry(
        datasourcePropertiesSpi: DatasourcePropertiesSpi,
    ): DatasourceRegistry {
        return DatasourceRegistry(
            datasourcePropertiesSpi = datasourcePropertiesSpi,
            interceptors = resolveDraftInterceptors(),
            scalarProviders = resolveScalarProviders(),
        )
    }

    @Single
    fun dataSourceManager(): DataSourceManager {
        return DataSourceManager(
            interceptors = resolveDraftInterceptors(),
            scalarProviders = resolveScalarProviders(),
        )
    }

    @Single
    fun dataSource(
        registry: DatasourceRegistry,
    ): DataSource {
        return registry.defaultDataSource()
    }

    @Single
    fun sqlClient(
        registry: DatasourceRegistry,
    ): KSqlClient {
        return registry.defaultSqlClient()
    }

    @Single
    fun sqlClients(
        registry: DatasourceRegistry,
    ): List<KSqlClient> {
        return registry.sqlClients()
    }
}

private data class DatasourceRuntime(
    val properties: DatasourceProperties,
    val dataSource: DataSource,
    val sqlClient: KSqlClient,
)

class DatasourceRegistry(
    private val datasourcePropertiesSpi: DatasourcePropertiesSpi,
    private val interceptors: List<DraftInterceptor<*, *>>,
    private val scalarProviders: List<AbstractScalarProvider<*, *>>,
) {
    private val runtimesByName: LinkedHashMap<String, DatasourceRuntime> = buildRuntimeMap()
    private val defaultRuntime: DatasourceRuntime = resolveDefaultRuntime(runtimesByName.values.toList())

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

    fun sqlClients(): List<KSqlClient> {
        return runtimesByName.values.map(DatasourceRuntime::sqlClient)
    }

    fun datasourceNames(): List<String> {
        return runtimesByName.keys.toList()
    }

    private fun runtime(name: String): DatasourceRuntime {
        return runtimesByName[name]
            ?: throw IllegalArgumentException(
                "Datasource '$name' is not configured. Available: ${runtimesByName.keys.joinToString()}",
            )
    }

    private fun buildRuntimeMap(): LinkedHashMap<String, DatasourceRuntime> {
        val runtimes = linkedMapOf<String, DatasourceRuntime>()
        enabledDatasourceProperties(datasourcePropertiesSpi).forEach { properties ->
            val dataSource = properties.toDatasource()
            val sqlClient = properties.toKsqlClient(
                dataSource = dataSource,
                interceptors = interceptors,
                scalarProviders = scalarProviders,
            )
            runtimes[properties.name] = DatasourceRuntime(
                properties = properties,
                dataSource = dataSource,
                sqlClient = sqlClient,
            )
        }
        return runtimes
    }

    private fun resolveDefaultRuntime(
        runtimes: List<DatasourceRuntime>,
    ): DatasourceRuntime {
        return runtimes.firstOrNull { runtime -> runtime.properties.default }
            ?: runtimes.firstOrNull()
            ?: error("No enabled datasource found, check your DatasourcePropertiesSpi")
    }
}

class DataSourceManager(
    private val interceptors: List<DraftInterceptor<*, *>>,
    private val scalarProviders: List<AbstractScalarProvider<*, *>>,
) {
    private val cache = ConcurrentHashMap<String, KSqlClient>()

    fun getSqlClient(
        properties: DatasourceProperties,
    ): KSqlClient {
        val cacheKey = properties.cacheKey()
        return cache.getOrPut(cacheKey) {
            properties.toKsqlClient(
                interceptors = interceptors,
                scalarProviders = scalarProviders,
            )
        }
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
                driverClassName = driverClassName.ifBlank { "org.postgresql.Driver" })
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
    return runCatching { HikariDataSource(config) }.getOrElse { throwable ->
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
    dataSource: DataSource,
    interceptors: List<DraftInterceptor<*, *>>,
    scalarProviders: List<AbstractScalarProvider<*, *>>,
): KSqlClient {
    val jdbcUrl = url.trim()
    val dialect = guessDialect(jdbcUrl)
    val activeScalarProviders = when (dialect) {
        is SQLiteDialect -> scalarProviders
        else -> scalarProviders.filterNot { provider ->
            provider is SqliteInstantScalarProvider || provider is SqliteLocalDateTimeScalarProvider
        }
    }
    return dataSource.toRawKSqlClient(
        dialect = dialect,
        interceptors = interceptors,
        scalarProviders = activeScalarProviders,
    )
}

private fun DatasourceProperties.toKsqlClient(
    interceptors: List<DraftInterceptor<*, *>>,
    scalarProviders: List<AbstractScalarProvider<*, *>>,
): KSqlClient {
    return toKsqlClient(
        dataSource = toDatasource(),
        interceptors = interceptors,
        scalarProviders = scalarProviders,
    )
}

private fun enabledDatasourceProperties(
    datasourcePropertiesSpi: DatasourcePropertiesSpi,
): List<DatasourceProperties> {
    return datasourcePropertiesSpi.datasources().filter { datasource ->
        datasource.enabled && datasource.url.isNotBlank()
    }
}

private fun DatasourceProperties.cacheKey(): String {
    return listOf(
        name,
        enabled.toString(),
        default.toString(),
        url,
        driverClassName,
        user,
        password,
    ).joinToString("|")
}

@OptIn(KoinInternalApi::class)
private fun resolveDraftInterceptors(): List<DraftInterceptor<*, *>> {
    return KoinPlatform.getKoin().scopeRegistry.rootScope.getAll(DraftInterceptor::class)
}

@OptIn(KoinInternalApi::class)
private fun resolveScalarProviders(): List<AbstractScalarProvider<*, *>> {
    return KoinPlatform.getKoin().scopeRegistry.rootScope.getAll(AbstractScalarProvider::class)
}

private fun prepareStatement(
    connection: Connection,
    sql: String,
    params: List<Any?>,
): PreparedStatement {
    return connection.prepareStatement(sql).apply {
        bindParams(params)
    }
}

private fun PreparedStatement.bindParams(
    params: List<Any?>,
) {
    params.forEachIndexed { index, value ->
        setObject(index + 1, value)
    }
}

private fun ResultSet.toRowMap(): Map<String, Any?> {
    val row = linkedMapOf<String, Any?>()
    for (index in 1..metaData.columnCount) {
        row[metaData.getColumnLabel(index)] = getObject(index)
    }
    return row
}
