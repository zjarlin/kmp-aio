package site.addzero.kcloud.server

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import org.koin.dsl.module
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.configcenter.ConfigCenter
import site.addzero.configcenter.CONFIG_CENTER_ADMIN_BASE_URL_PROPERTY
import site.addzero.kcloud.config.AppConfigKeys
import site.addzero.kcloud.jimmer.di.JIMMER_EMBEDDED_DESKTOP_MODE_PROPERTY
import site.addzero.starter.installConfigCenterAdminIfEnabled
import site.addzero.starter.installEffectiveConfig
import site.addzero.starter.normalizeConfigCenterActive
import site.addzero.starter.koin.installKoin
import site.addzero.starter.koin.runStarters
import site.addzero.starter.withConfigCenterOverrides
import java.io.File
import java.net.ServerSocket
import org.koin.core.KoinApplication as CoreKoinApplication
import org.koin.core.module.Module as KoinModule

private const val EMBEDDED_DESKTOP_MODE_PROPERTY = "kcloud.embedded.desktop"
private const val VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY = "vibepocket.embedded.desktop"
private const val KCLOUD_CONFIG_CENTER_NAMESPACE = "kcloud"
@Volatile
private var embeddedApplicationConfigOverride: ApplicationConfig? = null
@Volatile
private var embeddedDesktopKoinConfigurer: (CoreKoinApplication.() -> Unit)? = null
@Volatile
private var embeddedDesktopBaseUrl: String = buildEmbeddedDesktopBaseUrl(
    publicHost = defaultDesktopPublicHost(),
    port = defaultDesktopPort(),
)

interface EmbeddedDesktopServerHandle : AutoCloseable {
    val baseUrl: String
}


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
    installEffectiveConfig(config)
    installConfigCenterAdminIfEnabled(config)
    val desktopKoinConfigurer = embeddedDesktopKoinConfigurer
    val runtimeModules = buildList {
        add(
            module {
                single<ApplicationConfig> { config }
            },
        )
        addAll(overrideModules)
    }
    installKoin {
        withConfiguration<KCloudServerStarterKoinApplication>()
        desktopKoinConfigurer?.invoke(this)
        if (runtimeModules.isNotEmpty()) {
            modules(runtimeModules)
        }
    }
    runStarters()
    routing {
        registerKCloudPluginRoutes()
    }
}

fun serverApplication(
    configPath: String? = null,
    host: String? = null,
    port: Int? = null,
): EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> {
    embeddedApplicationConfigOverride = null
    embeddedDesktopKoinConfigurer = null
    System.clearProperty(CONFIG_CENTER_ADMIN_BASE_URL_PROPERTY)
    val config = loadServerConfig(configPath)

    val effectiveConfig = config.withConfigCenterOverrides(
        namespace = KCLOUD_CONFIG_CENTER_NAMESPACE,
        active = resolveConfigCenterActive(config),
    )
    val env = ConfigCenter.getEnv(effectiveConfig)
    val deploymentEnv = env.path("ktor", "deployment")

    val finalHost = host
        ?: System.getenv("SERVER_HOST")
        ?: deploymentEnv.string("host", AppConfigKeys.serverHost.defaultValue)
        ?: defaultServerHost()

    val finalPort = port
        ?: System.getenv("SERVER_PORT")?.toIntOrNull()
        ?: deploymentEnv.int("port", AppConfigKeys.serverPort.defaultValue?.toIntOrNull())
        ?: defaultServerPort()

    val environment = applicationEnvironment {
        this.config = effectiveConfig
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
    val effectiveConfig = config.withConfigCenterOverrides(
        namespace = KCLOUD_CONFIG_CENTER_NAMESPACE,
        active = resolveConfigCenterActive(config),
    )
    val env = ConfigCenter.getEnv(effectiveConfig)
    val deploymentEnv = env.path("ktor", "deployment")
    embeddedApplicationConfigOverride = effectiveConfig
    System.setProperty(JIMMER_EMBEDDED_DESKTOP_MODE_PROPERTY, "true")
    System.setProperty(EMBEDDED_DESKTOP_MODE_PROPERTY, "true")
    System.setProperty(VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY, "true")

    // 优先级：参数 > 环境变量 > 配置文件 > 默认值
    val requestedHost = host
        ?: System.getenv("SERVER_HOST")
        ?: deploymentEnv.string("host", AppConfigKeys.serverHost.defaultValue)
        ?: defaultServerHost()

    val requestedPort = port
        ?: System.getenv("SERVER_PORT")?.toIntOrNull()
        ?: deploymentEnv.int("port", AppConfigKeys.serverPort.defaultValue?.toIntOrNull())
        ?: defaultDesktopPort()
    val finalPort = resolveEmbeddedDesktopPort(requestedPort)
    embeddedDesktopBaseUrl = buildEmbeddedDesktopBaseUrl(
        publicHost = defaultDesktopPublicHost(),
        port = finalPort,
    )
    System.setProperty(
        CONFIG_CENTER_ADMIN_BASE_URL_PROPERTY,
        embeddedDesktopBaseUrl.removeSuffix("/"),
    )

    val environment = applicationEnvironment {
        this.config = effectiveConfig
    }

    val embeddedServer = embeddedServer(
        factory = Netty,
        environment = environment,
        configure = {
            connectors += EngineConnectorBuilder().apply {
                this.host = requestedHost
                this.port = finalPort
            }
        },
        module = Application::module,
    )
    return embeddedServer
}

private fun resolveEmbeddedDesktopPort(
    requestedPort: Int,
): Int {
    if (isLoopbackPortAvailable(requestedPort)) {
        return requestedPort
    }
    return ServerSocket(0).use { socket ->
        socket.localPort
    }
}

private fun isLoopbackPortAvailable(
    port: Int,
): Boolean {
    return runCatching {
        ServerSocket(port).use { socket ->
            socket.reuseAddress = true
        }
    }.isSuccess
}

/**
 * 桌面端使用的内嵌 server 句柄，避免把 Ktor engine 类型暴露给 UI 模块。
 */
fun startEmbeddedDesktopServer(
    configPath: String? = null,
    host: String? = null,
    port: Int? = null,
    configureKoin: (CoreKoinApplication.() -> Unit)? = null,
): EmbeddedDesktopServerHandle {
    embeddedDesktopKoinConfigurer = configureKoin
    val server = ktorApplication(
        configPath = configPath,
        host = host,
        port = port,
    ).start(wait = false)
    val baseUrl = embeddedDesktopBaseUrl

    return object : EmbeddedDesktopServerHandle {
        override val baseUrl: String = baseUrl

        override fun close() {
            server.stop(
                gracePeriodMillis = 1_000,
                timeoutMillis = 5_000,
            )
            embeddedDesktopKoinConfigurer = null
            embeddedApplicationConfigOverride = null
            System.clearProperty(JIMMER_EMBEDDED_DESKTOP_MODE_PROPERTY)
            System.clearProperty(EMBEDDED_DESKTOP_MODE_PROPERTY)
            System.clearProperty(VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY)
            System.clearProperty(CONFIG_CENTER_ADMIN_BASE_URL_PROPERTY)
        }
    }
}

private fun loadEmbeddedConfig(configPath: String?): HoconApplicationConfig {
    val desktopPaths = resolveEmbeddedDesktopPaths()
    val resolvedConfig = configPath?.let { path ->
        ConfigFactory.parseFile(resolveConfigFile(path))
    } ?: run {
        val env = System.getenv("KTOR_ENV")
            ?.trim()
            ?.ifBlank { null }
            ?: defaultKtorEnvironment()
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

private fun resolveConfigCenterActive(
    config: ApplicationConfig,
): String {
    val env = ConfigCenter.getEnv(config).path("ktor")
    val active = env.string("environment", AppConfigKeys.ktorEnvironment.defaultValue)
        ?: System.getenv("KTOR_ENV")
        ?: defaultKtorEnvironment()
    return normalizeConfigCenterActive(active)
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
        port = ${defaultDesktopPort()}
        host = "${defaultServerHost()}"
      }
      application {
        modules = [site.addzero.kcloud.ApplicationKt.module]
      }
    }
    banner {
      text = "${defaultDesktopBannerText()}"
      subtitle = "${defaultDesktopBannerSubtitle()}"
    }
    openapi {
      enabled = ${defaultDesktopOpenapiEnabled()}
      path = "${defaultDesktopOpenapiPath()}"
      spec = "${defaultDesktopOpenapiSpec()}"
    }
    flyway {
      enabled = ${defaultDesktopFlywayEnabled()}
    }
    datasources {
      sqlite {
        enabled = ${defaultSqliteEnabled()}
        url = "jdbc:sqlite:${defaultDesktopSqliteFileName()}"
        driver = "${defaultSqliteDriver()}"
      }
      postgres {
        enabled = ${defaultPostgresEnabled()}
      }
    }
    s3 {
      enabled = ${defaultDesktopS3Enabled()}
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
        .withValue(AppConfigKeys.SQLITE_ENABLED, ConfigValueFactory.fromAnyRef(defaultSqliteEnabled()))
        .withValue(AppConfigKeys.SQLITE_URL, ConfigValueFactory.fromAnyRef(paths.sqliteJdbcUrl))
        .withValue(
            AppConfigKeys.SQLITE_DRIVER,
            ConfigValueFactory.fromAnyRef(defaultSqliteDriver()),
        )
        .withValue(AppConfigKeys.POSTGRES_ENABLED, ConfigValueFactory.fromAnyRef(defaultPostgresEnabled()))
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
    val sqliteFile = File(dataDir, defaultDesktopSqliteFileName()).absoluteFile
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
    val appDirectoryName = defaultDesktopAppDirectoryName()

    return when {
        osName.contains("Mac", ignoreCase = true) -> {
            File(userHome, "Library/Application Support/$appDirectoryName")
        }

        osName.contains("Windows", ignoreCase = true) -> {
            File(
                System.getenv("LOCALAPPDATA")
                    ?: System.getenv("APPDATA")
                    ?: userHome,
                appDirectoryName,
            )
        }

        else -> {
            val xdgDataHome = System.getenv("XDG_DATA_HOME")
                ?.takeIf { it.isNotBlank() }
                ?: File(userHome, ".local/share").absolutePath
            File(xdgDataHome, appDirectoryName.lowercase())
        }
    }
}

private fun preferredDesktopCacheDir(): File {
    val userHome = desktopUserHome()
    val osName = System.getProperty("os.name").orEmpty()
    val appDirectoryName = defaultDesktopAppDirectoryName()

    return when {
        osName.contains("Mac", ignoreCase = true) -> {
            File(userHome, "Library/Caches/$appDirectoryName")
        }

        osName.contains("Windows", ignoreCase = true) -> {
            File(
                System.getenv("LOCALAPPDATA")
                    ?: System.getenv("APPDATA")
                    ?: userHome,
                "$appDirectoryName/Cache",
            )
        }

        else -> {
            val xdgCacheHome = System.getenv("XDG_CACHE_HOME")
                ?.takeIf { it.isNotBlank() }
                ?: File(userHome, ".cache").absolutePath
            File(xdgCacheHome, appDirectoryName.lowercase())
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
    val desktopSqliteFileName = defaultDesktopSqliteFileName()

    return buildList {
        add(File(workingDir, "kcloud-dev.db"))
        add(File(workingDir, desktopSqliteFileName))
        add(File(workingDir, "kcloud-desktop.db"))
        add(File(workingDir, "server/kcloud.db"))
        add(File(workingDir, "vibepocket-dev.db"))
        add(File(workingDir, "vibepocket.db"))
        add(File(workingDir, "vibepocket-desktop.db"))
        add(File(workingDir, "server/vibepocket.db"))
        if (parentDir != null) {
            add(File(parentDir, "kcloud-dev.db"))
            add(File(parentDir, desktopSqliteFileName))
            add(File(parentDir, "kcloud-desktop.db"))
            add(File(parentDir, "vibepocket-dev.db"))
            add(File(parentDir, "vibepocket.db"))
            add(File(parentDir, "vibepocket-desktop.db"))
        }
    }.distinctBy { it.absolutePath }
}

private fun buildEmbeddedDesktopBaseUrl(
    publicHost: String,
    port: Int,
): String {
    return "http://$publicHost:$port/"
}

private fun defaultServerHost(): String {
    return requireNotNull(AppConfigKeys.serverHost.defaultValue)
}

private fun defaultServerPort(): Int {
    return requireNotNull(AppConfigKeys.serverPort.defaultValue?.toIntOrNull())
}

private fun defaultKtorEnvironment(): String {
    return requireNotNull(AppConfigKeys.ktorEnvironment.defaultValue)
}

private fun defaultDesktopPublicHost(): String {
    return requireNotNull(AppConfigKeys.desktopServerPublicHost.defaultValue)
}

private fun defaultDesktopPort(): Int {
    return AppConfigKeys.desktopServerPort.defaultValue?.toIntOrNull() ?: defaultServerPort()
}

private fun defaultDesktopAppDirectoryName(): String {
    return requireNotNull(AppConfigKeys.desktopAppDirectoryName.defaultValue)
}

private fun defaultDesktopSqliteFileName(): String {
    return requireNotNull(AppConfigKeys.desktopSqliteFileName.defaultValue)
}

private fun defaultDesktopBannerText(): String {
    return requireNotNull(AppConfigKeys.desktopBannerText.defaultValue)
}

private fun defaultDesktopBannerSubtitle(): String {
    return requireNotNull(AppConfigKeys.desktopBannerSubtitle.defaultValue)
}

private fun defaultDesktopOpenapiEnabled(): Boolean {
    return AppConfigKeys.desktopOpenapiEnabled.defaultValue?.toBooleanStrictOrNull() ?: false
}

private fun defaultDesktopOpenapiPath(): String {
    return requireNotNull(AppConfigKeys.desktopOpenapiPath.defaultValue)
}

private fun defaultDesktopOpenapiSpec(): String {
    return requireNotNull(AppConfigKeys.desktopOpenapiSpec.defaultValue)
}

private fun defaultDesktopFlywayEnabled(): Boolean {
    return AppConfigKeys.desktopFlywayEnabled.defaultValue?.toBooleanStrictOrNull() ?: false
}

private fun defaultDesktopS3Enabled(): Boolean {
    return AppConfigKeys.desktopS3Enabled.defaultValue?.toBooleanStrictOrNull() ?: false
}

private fun defaultSqliteEnabled(): Boolean {
    return AppConfigKeys.sqliteEnabled.defaultValue?.toBooleanStrictOrNull() ?: true
}

private fun defaultSqliteDriver(): String {
    return requireNotNull(AppConfigKeys.sqliteDriver.defaultValue)
}

private fun defaultPostgresEnabled(): Boolean {
    return AppConfigKeys.postgresEnabled.defaultValue?.toBooleanStrictOrNull() ?: false
}
