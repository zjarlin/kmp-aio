package site.addzero.vibepocket

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.routing.*
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.starter.installEffectiveConfig
import site.addzero.starter.koin.installKoin
import site.addzero.starter.koin.runStarters
import site.addzero.vibepocket.di.AppKoinApplication
import site.addzero.vibepocket.generated.springktor.registerGeneratedSpringRoutes
import site.addzero.vibepocket.runtime.VibePocketDesktopStorage
import java.io.File

private const val DEFAULT_EMBEDDED_ENV = "dev"
private const val EMBEDDED_DESKTOP_MODE_PROPERTY = "vibepocket.embedded.desktop"
private const val EMBEDDED_SQLITE_URL_PROPERTY = "vibepocket.embedded.sqlite.url"
@Volatile
private var embeddedApplicationConfigOverride: ApplicationConfig? = null

/**
 * Server 入口。
 */
fun main(args: Array<String>) {
    val cli = parseServerCliArgs(args)
    serverApplication(
        configPath = cli.configPath,
        host = cli.host,
        port = cli.port,
    ).start(wait = true)
}

/**
 * 由 application.conf 中 ktor.application.modules 指定调用。
 */
fun Application.module() {
    val config = embeddedApplicationConfigOverride ?: environment.config
    installEffectiveConfig(config)
    installKoin {
        val koinProperties = mutableMapOf<String, Any>(
            "vibepocket.applicationConfig" to config,
        )
        config.propertyOrNull("suno.apiToken")
            ?.getString()
            ?.takeIf { it.isNotBlank() }
            ?.let { koinProperties["suno.apiToken"] = it }
        properties(
            koinProperties,
        )
        withConfiguration<AppKoinApplication>()
    }
    runStarters()
    routing { registerGeneratedSpringRoutes() }
}

fun serverApplication(
    configPath: String? = null,
    host: String? = null,
    port: Int? = null,
): EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> {
    embeddedApplicationConfigOverride = null
    val config = loadServerConfig(configPath)

    val finalHost = host
        ?: System.getenv("SERVER_HOST")
        ?: config.propertyOrNull("ktor.deployment.host")?.getString()
        ?: "0.0.0.0"

    val finalPort = port
        ?: System.getenv("SERVER_PORT")?.toIntOrNull()
        ?: config.propertyOrNull("ktor.deployment.port")?.getString()?.toIntOrNull()
        ?: 8080

    val environment = applicationEnvironment {
        this.config = config
    }

    return embeddedServer(
        factory = Netty,
        environment = environment,
        configure = {
            connectors += EngineConnectorBuilder().apply {
                this.host = finalHost
                this.port = finalPort
            }
        },
        module = Application::module,
    )
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
    val config = loadEmbeddedConfig(configPath)
    embeddedApplicationConfigOverride = config
    System.setProperty(EMBEDDED_DESKTOP_MODE_PROPERTY, "true")
    config.propertyOrNull("datasources.sqlite.url")
        ?.getString()
        ?.takeIf { it.isNotBlank() }
        ?.let { System.setProperty(EMBEDDED_SQLITE_URL_PROPERTY, it) }

    // 优先级：参数 > 环境变量 > 配置文件 > 默认值
    val finalHost = host
        ?: System.getenv("SERVER_HOST")
        ?: config.propertyOrNull("ktor.deployment.host")?.getString()
        ?: "0.0.0.0"

    val finalPort = port
        ?: System.getenv("SERVER_PORT")?.toIntOrNull()
        ?: config.propertyOrNull("ktor.deployment.port")?.getString()?.toIntOrNull()
        ?: 8080

    val environment = applicationEnvironment {
        this.config = config
    }

    return embeddedServer(
        factory = Netty,
        environment = environment,
        configure = {
            connectors += EngineConnectorBuilder().apply {
                this.host = finalHost
                this.port = finalPort
            }
        },
        module = Application::module,
    )
}

private fun loadEmbeddedConfig(configPath: String?): HoconApplicationConfig {
    val storage = VibePocketDesktopStorage.resolve()
    val resolvedConfig = configPath?.let { path ->
        ConfigFactory.parseFile(resolveConfigFile(path))
    } ?: run {
        val env = System.getenv("KTOR_ENV")
            ?.trim()
            ?.ifBlank { null }
            ?: DEFAULT_EMBEDDED_ENV
        ConfigFactory
            .parseResources("application-$env.conf")
            .withFallback(embeddedDesktopDefaults())
            .withFallback(ConfigFactory.load())
    }

    return HoconApplicationConfig(
        embeddedDesktopRuntimeOverrides(storage)
            .withFallback(embeddedDesktopOverrides())
            .withFallback(resolvedConfig)
            .resolve()
    )
}

private fun loadServerConfig(configPath: String?): HoconApplicationConfig {
    val resolved = configPath?.let { path ->
        ConfigFactory.parseFile(resolveConfigFile(path)).withFallback(ConfigFactory.load())
    } ?: ConfigFactory.load()

    return HoconApplicationConfig(
        serverRuntimeOverrides()
            .withFallback(resolved)
            .resolve()
    )
}

private data class ServerCliArgs(
    val configPath: String? = null,
    val host: String? = null,
    val port: Int? = null,
)

private fun parseServerCliArgs(args: Array<String>): ServerCliArgs {
    val normalizedArgs = args
        .flatMap { raw -> raw.split(Regex("\\s+")).filter { it.isNotBlank() } }

    fun findValue(name: String): String? {
        val prefixes = listOf("-$name=", "--$name=")
        return normalizedArgs.firstNotNullOfOrNull { arg ->
            prefixes.firstNotNullOfOrNull { prefix ->
                arg.removePrefix(prefix).takeIf { arg.startsWith(prefix) }
            }
        }
    }

    return ServerCliArgs(
        configPath = findValue("config"),
        host = findValue("host"),
        port = findValue("port")?.toIntOrNull(),
    )
}

private fun resolveConfigFile(path: String): File {
    val direct = File(path)
    if (direct.exists()) return direct

    var current: File? = File(System.getProperty("user.dir")).absoluteFile
    while (current != null) {
        val candidate = File(current, path)
        if (candidate.exists()) {
            return candidate
        }
        current = current.parentFile
    }

    return direct
}

private fun serverRuntimeOverrides() = ConfigFactory.parseString(
    """
    ktor {
      application {
        modules = []
      }
    }
    """.trimIndent()
)

private fun embeddedDesktopDefaults() = ConfigFactory.parseString(
    """
    ktor {
      deployment {
        port = 8080
        host = "127.0.0.1"
      }
      application {
        modules = [site.addzero.vibepocket.ApplicationKt.module]
      }
    }
    banner {
      text = "VIBEPOCKET [DESKTOP]"
      subtitle = "Embedded Dev Server"
    }
    openapi {
      enabled = false
      path = "/swagger"
      spec = "openapi/documentation.yaml"
    }
    flyway {
      enabled = false
    }
    datasources {
      sqlite {
        enabled = true
        url = "jdbc:sqlite:vibepocket-desktop.db"
        driver = "org.sqlite.SQLiteDriver"
      }
      postgres {
        enabled = false
      }
    }
    s3 {
      enabled = false
    }
    """.trimIndent()
)

private fun embeddedDesktopOverrides() = ConfigFactory.parseString(
    """
    ktor {
      application {
        modules = []
      }
    }
    """.trimIndent()
)

private fun embeddedDesktopRuntimeOverrides(storage: site.addzero.vibepocket.runtime.VibePocketDesktopStoragePaths) =
    ConfigFactory.empty()
        .withValue("datasources.sqlite.enabled", ConfigValueFactory.fromAnyRef(true))
        .withValue("datasources.sqlite.url", ConfigValueFactory.fromAnyRef(storage.sqliteJdbcUrl))
        .withValue("datasources.sqlite.driver", ConfigValueFactory.fromAnyRef("org.sqlite.SQLiteDriver"))
        .withValue("datasources.postgres.enabled", ConfigValueFactory.fromAnyRef(false))
        .withValue("vibepocket.runtime.sqlitePath", ConfigValueFactory.fromAnyRef(storage.sqliteFile.absolutePath))
        .withValue("vibepocket.runtime.dataDir", ConfigValueFactory.fromAnyRef(storage.dataDir.absolutePath))
        .withValue("vibepocket.runtime.cacheDir", ConfigValueFactory.fromAnyRef(storage.cacheDir.absolutePath))
