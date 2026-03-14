package site.addzero.vibepocket

import com.typesafe.config.ConfigFactory
import io.ktor.serialization.kotlinx.json.*
import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.ktor.ext.getKoin
import org.koin.plugin.module.dsl.withConfiguration
import org.sqlite.SQLiteDataSource
import site.addzero.starter.koin.installKoin
import site.addzero.starter.koin.runStarters
import site.addzero.vibepocket.di.AppKoinApplication
import site.addzero.vibepocket.routes.ioc.generated.iocModule
import site.addzero.vibepocket.service.MusicCatalogService
import site.addzero.vibepocket.service.MusicLibCatalogService
import java.io.File
import javax.sql.DataSource

/**
 * EngineMain 入口 — Ktor 自动加载 application.conf，
 * 读取 ktor.application.modules 配置调用 Application.module()。
 */
fun main(args: Array<String>) = EngineMain.main(args)

/**
 * 由 application.conf 中 ktor.application.modules 指定调用。
 */
fun Application.module() {
    // 1. Koin 必须最先初始化（发现机制依赖它）
    installKoin {
        withConfiguration<AppKoinApplication>()
    }
    ensureEmbeddedSqlClient()
    ensureMusicCatalogService()
    // 2. 自动发现并执行所有 Starter
    runStarters()
    ensureContentNegotiation()
    // 3. 路由聚合（iocModule 由 @Bean KSP 生成）
    routing { iocModule() }
}

/**
 * 桌面端内嵌启动入口（非阻塞），返回 server 实例。
 * 从 application.conf 读取端口配置，支持环境变量覆盖。
 */
fun ktorApplication(
    configPath: String? = null,
    host: String? = null,
    port: Int? = null,
): EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> {
    // 加载配置（优先使用指定路径，否则从 classpath 加载 application.conf）
    val config = configPath?.let {
        HoconApplicationConfig(
            ConfigFactory.parseFile(File(it)).withFallback(ConfigFactory.load()).resolve()
        )
    } ?: HoconApplicationConfig(ConfigFactory.load())

    // 优先级：参数 > 环境变量 > 配置文件 > 默认值
    val finalHost = host
        ?: System.getenv("SERVER_HOST")
        ?: config.propertyOrNull("ktor.deployment.host")?.getString()
        ?: "0.0.0.0"

    val finalPort = port
        ?: System.getenv("SERVER_PORT")?.toIntOrNull()
        ?: config.propertyOrNull("ktor.deployment.port")?.getString()?.toIntOrNull()
        ?: 8080

    return embeddedServer(Netty, port = finalPort, host = finalHost, module = Application::module)
}

private fun Application.ensureEmbeddedSqlClient() {
    val koin = getKoin()
    val existing = runCatching { koin.get<KSqlClient>() }.getOrNull()
    if (existing != null) {
        return
    }

    val jdbcUrl = environment.config
        .propertyOrNull("datasources.sqlite.url")
        ?.getString()
        ?.ifBlank { null }
        ?: "jdbc:sqlite:vibepocket.db"

    val dataSource = SQLiteDataSource().apply {
        url = jdbcUrl
    }
    runSqlSchema(dataSource, "schema-sqlite.sql")

    val sqlClient = newKSqlClient {
        setDialect(SQLiteDialect())
        setDatabaseNamingStrategy(DefaultDatabaseNamingStrategy.LOWER_CASE)
        setConnectionManager { dataSource.connection.use { proceed(it) } }
    }

    koin.declare<DataSource>(dataSource)
    koin.declare<KSqlClient>(sqlClient)
}

private fun Application.ensureMusicCatalogService() {
    val koin = getKoin()
    val existing = runCatching { koin.get<MusicCatalogService>() }.getOrNull()
    if (existing != null) {
        return
    }

    koin.declare<MusicCatalogService>(MusicLibCatalogService())
}

private fun Application.ensureContentNegotiation() {
    if (pluginOrNull(ContentNegotiation) != null) {
        return
    }

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
                explicitNulls = false
                isLenient = true
            },
        )
    }
}

private fun runSqlSchema(
    dataSource: DataSource,
    schemaFile: String,
) {
    val sql = object {}.javaClass.getResource("/sql/$schemaFile")?.readText().orEmpty()
    if (sql.isBlank()) {
        return
    }

    dataSource.connection.use { connection ->
        connection.createStatement().use { statement ->
            sql.split(";")
                .map(String::trim)
                .filter(String::isNotEmpty)
                .forEach(statement::executeUpdate)
        }
    }
}
