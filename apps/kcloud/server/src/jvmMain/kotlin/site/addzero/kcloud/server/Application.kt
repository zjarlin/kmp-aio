package site.addzero.kcloud

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.routing
import org.koin.core.module.Module as KoinModule
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.kcloud.plugins.mcuconsole.mcuConsoleRoutes
import site.addzero.vibepocket.routes.vibePocketRoutes
import site.addzero.starter.koin.installKoin
import site.addzero.starter.koin.runStarters
import java.io.File

private const val DEFAULT_EMBEDDED_ENV = "dev"
private const val DESKTOP_APP_DIRECTORY_NAME = "KCloud"
private const val EMBEDDED_DESKTOP_MODE_PROPERTY = "kcloud.embedded.desktop"
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
fun Application.module(
    overrideModules: List<KoinModule> = emptyList(),
) {
    val config = embeddedApplicationConfigOverride ?: environment.config
    installKoin {
        withConfiguration<KCloudServerStarterKoinApplication>()
        if (overrideModules.isNotEmpty()) {
            modules(overrideModules)
        }
        properties(mapOf("kcloud.applicationConfig" to config))
    }
    runStarters()
    routing {
        mcuConsoleRoutes()
        vibePocketRoutes()
    }
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
    val desktopPaths = resolveEmbeddedDesktopPaths()
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
        embeddedDesktopRuntimeOverrides(desktopPaths)
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
        modules = [site.addzero.kcloud.ApplicationKt.module]
      }
    }
    banner {
      text = "KCLOUD [DESKTOP]"
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
        url = "jdbc:sqlite:kcloud-desktop.db"
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

private fun embeddedDesktopRuntimeOverrides(paths: EmbeddedDesktopPaths) =
    ConfigFactory.empty()
        .withValue("datasources.sqlite.enabled", ConfigValueFactory.fromAnyRef(true))
        .withValue("datasources.sqlite.url", ConfigValueFactory.fromAnyRef(paths.sqliteJdbcUrl))
        .withValue("datasources.sqlite.driver", ConfigValueFactory.fromAnyRef("org.sqlite.SQLiteDriver"))
        .withValue("datasources.postgres.enabled", ConfigValueFactory.fromAnyRef(false))
        .withValue("kcloud.runtime.sqlitePath", ConfigValueFactory.fromAnyRef(paths.sqliteFile.absolutePath))
        .withValue("kcloud.runtime.dataDir", ConfigValueFactory.fromAnyRef(paths.dataDir.absolutePath))
        .withValue("kcloud.runtime.cacheDir", ConfigValueFactory.fromAnyRef(paths.cacheDir.absolutePath))

private data class EmbeddedDesktopPaths(
    val dataDir: File,
    val cacheDir: File,
    val sqliteFile: File,
) {
    val sqliteJdbcUrl: String
        get() = "jdbc:sqlite:${sqliteFile.absolutePath}"
}

private fun resolveEmbeddedDesktopPaths(): EmbeddedDesktopPaths {
    val dataDir = preferredDesktopDataDir().ensureDirectory()
    val cacheDir = preferredDesktopCacheDir().ensureDirectory()
    val sqliteFile = File(dataDir, "kcloud.db").absoluteFile
    migrateLegacyDatabaseIfNeeded(sqliteFile)
    return EmbeddedDesktopPaths(
        dataDir = dataDir,
        cacheDir = cacheDir,
        sqliteFile = sqliteFile,
    )
}

private fun preferredDesktopDataDir(): File {
    val userHome = desktopUserHome()
    val osName = System.getProperty("os.name").orEmpty()

    return when {
        osName.contains("Mac", ignoreCase = true) -> {
            File(userHome, "Library/Application Support/$DESKTOP_APP_DIRECTORY_NAME")
        }

        osName.contains("Windows", ignoreCase = true) -> {
            File(
                System.getenv("LOCALAPPDATA")
                    ?: System.getenv("APPDATA")
                    ?: userHome,
                DESKTOP_APP_DIRECTORY_NAME,
            )
        }

        else -> {
            val xdgDataHome = System.getenv("XDG_DATA_HOME")
                ?.takeIf { it.isNotBlank() }
                ?: File(userHome, ".local/share").absolutePath
            File(xdgDataHome, DESKTOP_APP_DIRECTORY_NAME.lowercase())
        }
    }
}

private fun preferredDesktopCacheDir(): File {
    val userHome = desktopUserHome()
    val osName = System.getProperty("os.name").orEmpty()

    return when {
        osName.contains("Mac", ignoreCase = true) -> {
            File(userHome, "Library/Caches/$DESKTOP_APP_DIRECTORY_NAME")
        }

        osName.contains("Windows", ignoreCase = true) -> {
            File(
                System.getenv("LOCALAPPDATA")
                    ?: System.getenv("APPDATA")
                    ?: userHome,
                "$DESKTOP_APP_DIRECTORY_NAME/Cache",
            )
        }

        else -> {
            val xdgCacheHome = System.getenv("XDG_CACHE_HOME")
                ?.takeIf { it.isNotBlank() }
                ?: File(userHome, ".cache").absolutePath
            File(xdgCacheHome, DESKTOP_APP_DIRECTORY_NAME.lowercase())
        }
    }
}

private fun File.ensureDirectory(): File {
    if (exists()) {
        require(isDirectory) {
            "路径不是目录：$absolutePath"
        }
        return this
    }

    check(mkdirs() || exists()) {
        "无法创建目录：$absolutePath"
    }
    return this
}

private fun desktopUserHome(): String {
    return System.getProperty("user.home").orEmpty()
}

private fun migrateLegacyDatabaseIfNeeded(targetFile: File) {
    if (targetFile.exists()) {
        return
    }

    val legacySource = legacyDatabaseCandidates()
        .firstOrNull { candidate ->
            candidate.exists() &&
                candidate.isFile &&
                candidate.length() > 0L &&
                candidate.absolutePath != targetFile.absolutePath
        }
        ?: return

    runCatching {
        legacySource.copyTo(targetFile, overwrite = false)
    }.onFailure {
        println("Failed to migrate legacy kcloud database from ${legacySource.absolutePath}: ${it.message}")
    }
}

private fun legacyDatabaseCandidates(): List<File> {
    val workingDir = File(System.getProperty("user.dir").orEmpty()).absoluteFile
    val parentDir = workingDir.parentFile

    return buildList {
        add(File(workingDir, "kcloud-dev.db"))
        add(File(workingDir, "kcloud.db"))
        add(File(workingDir, "kcloud-desktop.db"))
        add(File(workingDir, "server/kcloud.db"))
        add(File(workingDir, "vibepocket-dev.db"))
        add(File(workingDir, "vibepocket.db"))
        add(File(workingDir, "vibepocket-desktop.db"))
        add(File(workingDir, "server/vibepocket.db"))
        if (parentDir != null) {
            add(File(parentDir, "kcloud-dev.db"))
            add(File(parentDir, "kcloud.db"))
            add(File(parentDir, "kcloud-desktop.db"))
            add(File(parentDir, "vibepocket-dev.db"))
            add(File(parentDir, "vibepocket.db"))
            add(File(parentDir, "vibepocket-desktop.db"))
        }
    }.distinctBy { it.absolutePath }
}
