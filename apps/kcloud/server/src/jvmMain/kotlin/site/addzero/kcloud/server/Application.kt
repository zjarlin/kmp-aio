package site.addzero.kcloud.server

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.config.withFallback
import io.ktor.server.engine.EngineConnectorBuilder
import io.ktor.server.engine.applicationEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.routing.routing
import org.koin.core.KoinApplication
import org.koin.dsl.module
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.configcenter.ConfigCenterBeanFactory
import site.addzero.configcenter.ConfigCenterEnv
import site.addzero.configcenter.ConfigCenterJdbcSettings
import site.addzero.configcenter.env
import site.addzero.configcenter.normalizeConfigCenterActive
import site.addzero.configcenter.withConfigCenterOverrides
import site.addzero.kcloud.config.AppConfigKeys
import site.addzero.starter.effectiveConfig
import site.addzero.starter.installConfigCenterAdminIfEnabled
import site.addzero.starter.koin.installKoin
import site.addzero.starter.koin.runStarters
import java.io.File
import java.net.ServerSocket

const val KCLOUD_CONFIG_CENTER_NAMESPACE = "kcloud"
const val VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY = "vibepocket.embedded.desktop.mode"

private const val DEFAULT_CONFIG_CENTER_JDBC_URL = "jdbc:sqlite:./config-center.sqlite"

var embeddedApplicationConfigOverride: ApplicationConfig? = null
var embeddedDesktopBaseUrl: String? = null
var embeddedDesktopKoinConfigurer: (KoinApplication.() -> Unit)? = null

fun main(
    args: Array<String>,
) {
    val cliArgs = parseServerCliArgs(args)
    val config = loadServerConfig(cliArgs.configPath)
    val active = resolveConfigCenterActive(config)
    val runtimeSettings = resolveRuntimeSettings(config, active)
    val effectiveConfig = config.withConfigCenterOverrides(
        namespace = KCLOUD_CONFIG_CENTER_NAMESPACE,
        active = active,
    )
    val serverHost = cliArgs.host ?: runtimeSettings.serverHost
    val serverPort = cliArgs.port ?: runtimeSettings.serverPort
    embeddedServer(
        factory = Netty,
        environment = applicationEnvironment {
            this.config = effectiveConfig
        },
        configure = {
            connectors += EngineConnectorBuilder().apply {
                host = serverHost
                port = serverPort
            }
        },
        module = Application::module,
    ).start(wait = true)
}

fun Application.module() {
    installKoin {
        withConfiguration<KCloudServerStarterKoinApplication>()
        modules(
            module {
                single<ApplicationConfig> { effectiveConfig() }
            },
        )
        embeddedDesktopKoinConfigurer?.invoke(this)
    }
    installConfigCenterAdminIfEnabled(effectiveConfig())
    runStarters()
    routing {
        registerKCloudPluginRoutes()
    }
}

fun ktorApplication(
    configPath: String? = null,
    host: String? = null,
    port: Int? = null,
): io.ktor.server.engine.EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> {
    val config = loadEmbeddedConfig(configPath)
    val active = resolveConfigCenterActive(config)
    val runtimeSettings = resolveRuntimeSettings(config, active)
    val desktopServerHost = host ?: runtimeSettings.serverHost
    val desktopServerPort = port ?: runtimeSettings.desktopServerPort
    ensurePortAvailable(desktopServerPort)

    val desktopPaths = resolveEmbeddedDesktopPaths(runtimeSettings)
    val effectiveConfig = HoconApplicationConfig(
        embeddedDesktopRuntimeOverrides(
            paths = desktopPaths,
            settings = runtimeSettings,
            serverHost = desktopServerHost,
            serverPort = desktopServerPort,
        ).withFallback(
            embeddedDesktopOverrides(),
        ).resolve(),
    ).withFallback(
        config.withConfigCenterOverrides(
            namespace = KCLOUD_CONFIG_CENTER_NAMESPACE,
            active = active,
        ),
    )

    embeddedApplicationConfigOverride = effectiveConfig
    embeddedDesktopBaseUrl = buildEmbeddedDesktopBaseUrl(
        publicHost = runtimeSettings.desktopServerPublicHost,
        port = desktopServerPort,
    )
    System.setProperty(VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY, "true")

    return embeddedServer(
        factory = Netty,
        environment = applicationEnvironment {
            this.config = effectiveConfig
        },
        configure = {
            connectors += EngineConnectorBuilder().apply {
                this.host = desktopServerHost
                this.port = desktopServerPort
            }
        },
        module = Application::module,
    )
}

internal fun loadServerConfig(
    configPath: String? = null,
): ApplicationConfig {
    embeddedApplicationConfigOverride?.let { override ->
        return override
    }
    val baseConfig = when {
        configPath.isNullOrBlank() -> HoconApplicationConfig(ConfigFactory.load())
        else -> {
            val file = File(configPath).absoluteFile
            check(file.isFile) { "找不到 server 配置文件: ${file.absolutePath}" }
            HoconApplicationConfig(
                ConfigFactory.parseFile(file)
                    .withFallback(ConfigFactory.load())
                    .resolve(),
            )
        }
    }
    return baseConfig.withFallback(
        HoconApplicationConfig(serverRuntimeOverrides().resolve()),
    )
}

private fun loadEmbeddedConfig(
    configPath: String? = null,
): ApplicationConfig {
    return loadServerConfig(configPath)
}

internal fun serverRuntimeOverrides(): Config {
    return ConfigFactory.empty()
        .withValue("config-center.enabled", ConfigValueFactory.fromAnyRef(true))
        .withValue("config-center.jdbc.url", ConfigValueFactory.fromAnyRef(DEFAULT_CONFIG_CENTER_JDBC_URL))
        .withValue("config-center.jdbc.auto-ddl", ConfigValueFactory.fromAnyRef(true))
        .withValue("config-center.admin.enabled", ConfigValueFactory.fromAnyRef(true))
}

private fun embeddedDesktopOverrides(): Config {
    return serverRuntimeOverrides()
}

private fun ensurePortAvailable(
    port: Int,
) {
    check(isPortAvailable(port)) {
        "配置中心中的桌面端端口 ${AppConfigKeys.DESKTOP_SERVER_PORT}=$port 当前不可用，请调整配置后重试。"
    }
}

private fun isPortAvailable(
    port: Int,
): Boolean {
    return runCatching {
        ServerSocket(port).use { socket ->
            socket.reuseAddress = true
        }
    }.isSuccess
}

internal fun resolveConfigCenterActive(
    config: ApplicationConfig,
): String {
    val active = config.propertyOrNull(AppConfigKeys.KTOR_ENVIRONMENT)
        ?.getString()
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: error("缺少启动配置 ${AppConfigKeys.KTOR_ENVIRONMENT}，无法确定配置中心 active。")
    return normalizeConfigCenterActive(active)
}

internal fun resolveConfigCenterJdbcSettings(
    config: ApplicationConfig,
): ConfigCenterJdbcSettings {
    val url = config.propertyOrNull("config-center.jdbc.url")
        ?.getString()
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: DEFAULT_CONFIG_CENTER_JDBC_URL
    return ConfigCenterJdbcSettings(
        url = url,
        username = config.propertyOrNull("config-center.jdbc.username")?.getString()
            ?: config.propertyOrNull("config-center.jdbc.user")?.getString(),
        password = config.propertyOrNull("config-center.jdbc.password")?.getString(),
        driver = config.propertyOrNull("config-center.jdbc.driver")?.getString()
            ?: ConfigCenterJdbcSettings.inferDriver(url),
        autoDdl = config.propertyOrNull("config-center.jdbc.auto-ddl")
            ?.getString()
            ?.trim()
            ?.lowercase()
            ?.let { value ->
                when (value) {
                    "true" -> true
                    "false" -> false
                    else -> null
                }
            }
            ?: true,
    )
}

private data class ServerCliArgs(
    val configPath: String? = null,
    val host: String? = null,
    val port: Int? = null,
)

private fun parseServerCliArgs(
    args: Array<String>,
): ServerCliArgs {
    fun readValue(vararg names: String): String? {
        return names.asSequence()
            .mapNotNull { name ->
                args.firstOrNull { arg -> arg.startsWith("$name=") }
                    ?.substringAfter('=')
                    ?.trim()
                    ?.takeIf(String::isNotBlank)
            }
            .firstOrNull()
    }

    return ServerCliArgs(
        configPath = readValue("--config", "-config"),
        host = readValue("--host", "-host"),
        port = readValue("--port", "-port")?.toIntOrNull(),
    )
}

private fun embeddedDesktopRuntimeOverrides(
    paths: EmbeddedDesktopPaths,
    settings: KCloudRuntimeSettings,
    serverHost: String,
    serverPort: Int,
): Config =
    ConfigFactory.empty()
        .withValue(AppConfigKeys.SERVER_HOST, ConfigValueFactory.fromAnyRef(serverHost))
        .withValue(AppConfigKeys.SERVER_PORT, ConfigValueFactory.fromAnyRef(serverPort))
        .withValue(
            AppConfigKeys.DESKTOP_SERVER_PUBLIC_HOST,
            ConfigValueFactory.fromAnyRef(settings.desktopServerPublicHost),
        )
        .withValue(AppConfigKeys.DESKTOP_SERVER_PORT, ConfigValueFactory.fromAnyRef(serverPort))
        .withValue(
            AppConfigKeys.DESKTOP_APP_DIRECTORY_NAME,
            ConfigValueFactory.fromAnyRef(settings.desktopAppDirectoryName),
        )
        .withValue(
            AppConfigKeys.DESKTOP_SQLITE_FILE_NAME,
            ConfigValueFactory.fromAnyRef(settings.desktopSqliteFileName),
        )
        .withValue("banner.text", ConfigValueFactory.fromAnyRef(settings.desktopBannerText))
        .withValue("banner.subtitle", ConfigValueFactory.fromAnyRef(settings.desktopBannerSubtitle))
        .withValue("openapi.enabled", ConfigValueFactory.fromAnyRef(settings.desktopOpenapiEnabled))
        .withValue("openapi.path", ConfigValueFactory.fromAnyRef(settings.desktopOpenapiPath))
        .withValue("openapi.spec", ConfigValueFactory.fromAnyRef(settings.desktopOpenapiSpec))
        .withValue("flyway.enabled", ConfigValueFactory.fromAnyRef(settings.desktopFlywayEnabled))
        .withValue("s3.enabled", ConfigValueFactory.fromAnyRef(settings.desktopS3Enabled))
        .withValue(AppConfigKeys.SQLITE_ENABLED, ConfigValueFactory.fromAnyRef(settings.sqliteEnabled))
        .withValue(AppConfigKeys.SQLITE_URL, ConfigValueFactory.fromAnyRef(paths.sqliteJdbcUrl))
        .withValue(AppConfigKeys.SQLITE_DRIVER, ConfigValueFactory.fromAnyRef(settings.sqliteDriver))
        .withValue(AppConfigKeys.POSTGRES_ENABLED, ConfigValueFactory.fromAnyRef(settings.postgresEnabled))
        .withValue("kcloud.runtime.sqlitePath", ConfigValueFactory.fromAnyRef(paths.sqliteFile.absolutePath))
        .withValue("kcloud.runtime.dataDir", ConfigValueFactory.fromAnyRef(paths.dataDir.absolutePath))
        .withValue("kcloud.runtime.cacheDir", ConfigValueFactory.fromAnyRef(paths.cacheDir.absolutePath))

private data class EmbeddedDesktopPaths(
    val dataDir: File,
    val cacheDir: File,
    val sqliteFile: File,
) {
    val sqliteJdbcUrl
        get() = "jdbc:sqlite:${sqliteFile.absolutePath}"
}

internal data class KCloudRuntimeSettings(
    val serverHost: String,
    val serverPort: Int,
    val desktopServerPublicHost: String,
    val desktopServerPort: Int,
    val desktopAppDirectoryName: String,
    val desktopSqliteFileName: String,
    val desktopBannerText: String,
    val desktopBannerSubtitle: String,
    val desktopOpenapiEnabled: Boolean,
    val desktopOpenapiPath: String,
    val desktopOpenapiSpec: String,
    val desktopFlywayEnabled: Boolean,
    val desktopS3Enabled: Boolean,
    val sqliteEnabled: Boolean,
    val sqliteDriver: String,
    val postgresEnabled: Boolean,
)

internal fun resolveRuntimeSettings(
    config: ApplicationConfig,
    active: String,
): KCloudRuntimeSettings {
    val env = ConfigCenterBeanFactory.env(
        settings = resolveConfigCenterJdbcSettings(config),
        namespace = KCLOUD_CONFIG_CENTER_NAMESPACE,
        active = active,
    )
    return KCloudRuntimeSettings(
        serverHost = env.requireNonBlank(AppConfigKeys.SERVER_HOST, active),
        serverPort = env.requireInt(AppConfigKeys.SERVER_PORT, active),
        desktopServerPublicHost = env.requireNonBlank(AppConfigKeys.DESKTOP_SERVER_PUBLIC_HOST, active),
        desktopServerPort = env.requireInt(AppConfigKeys.DESKTOP_SERVER_PORT, active),
        desktopAppDirectoryName = env.requireNonBlank(AppConfigKeys.DESKTOP_APP_DIRECTORY_NAME, active),
        desktopSqliteFileName = env.requireNonBlank(AppConfigKeys.DESKTOP_SQLITE_FILE_NAME, active),
        desktopBannerText = env.requireNonBlank(AppConfigKeys.DESKTOP_BANNER_TEXT, active),
        desktopBannerSubtitle = env.requireNonBlank(AppConfigKeys.DESKTOP_BANNER_SUBTITLE, active),
        desktopOpenapiEnabled = env.requireBoolean(AppConfigKeys.DESKTOP_OPENAPI_ENABLED, active),
        desktopOpenapiPath = env.requireNonBlank(AppConfigKeys.DESKTOP_OPENAPI_PATH, active),
        desktopOpenapiSpec = env.requireNonBlank(AppConfigKeys.DESKTOP_OPENAPI_SPEC, active),
        desktopFlywayEnabled = env.requireBoolean(AppConfigKeys.DESKTOP_FLYWAY_ENABLED, active),
        desktopS3Enabled = env.requireBoolean(AppConfigKeys.DESKTOP_S3_ENABLED, active),
        sqliteEnabled = env.requireBoolean(AppConfigKeys.SQLITE_ENABLED, active),
        sqliteDriver = env.requireNonBlank(AppConfigKeys.SQLITE_DRIVER, active),
        postgresEnabled = env.requireBoolean(AppConfigKeys.POSTGRES_ENABLED, active),
    )
}

private fun resolveEmbeddedDesktopPaths(
    settings: KCloudRuntimeSettings,
): EmbeddedDesktopPaths {
    val dataDir = preferredDesktopDataDir(settings.desktopAppDirectoryName).ensureDirectory()
    val cacheDir = preferredDesktopCacheDir(settings.desktopAppDirectoryName).ensureDirectory()
    val sqliteFile = File(dataDir, settings.desktopSqliteFileName).absoluteFile
    migrateLegacyDatabaseIfNeeded(
        targetFile = sqliteFile,
        sqliteFileName = settings.desktopSqliteFileName,
    )
    return EmbeddedDesktopPaths(
        dataDir = dataDir,
        cacheDir = cacheDir,
        sqliteFile = sqliteFile,
    )
}

private fun preferredDesktopDataDir(
    appDirectoryName: String,
): File {
    val userHome = desktopUserHome()
    val osName = System.getProperty("os.name").orEmpty()

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

private fun preferredDesktopCacheDir(
    appDirectoryName: String,
): File {
    val userHome = desktopUserHome()
    val osName = System.getProperty("os.name").orEmpty()

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

private fun migrateLegacyDatabaseIfNeeded(
    targetFile: File,
    sqliteFileName: String,
) {
    if (targetFile.exists()) {
        return
    }

    val legacySource = legacyDatabaseCandidates(sqliteFileName)
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

private fun legacyDatabaseCandidates(
    sqliteFileName: String,
): List<File> {
    val workingDir = File(System.getProperty("user.dir").orEmpty()).absoluteFile
    val parentDir = workingDir.parentFile

    return buildList {
        add(File(workingDir, "kcloud-dev.db"))
        add(File(workingDir, sqliteFileName))
        add(File(workingDir, "kcloud-desktop.db"))
        add(File(workingDir, "server/kcloud.db"))
        add(File(workingDir, "vibepocket-dev.db"))
        add(File(workingDir, "vibepocket.db"))
        add(File(workingDir, "vibepocket-desktop.db"))
        add(File(workingDir, "server/vibepocket.db"))
        if (parentDir != null) {
            add(File(parentDir, "kcloud-dev.db"))
            add(File(parentDir, sqliteFileName))
            add(File(parentDir, "kcloud-desktop.db"))
            add(File(parentDir, "vibepocket-dev.db"))
            add(File(parentDir, "vibepocket.db"))
            add(File(parentDir, "vibepocket-desktop.db"))
        }
    }.distinctBy(File::getAbsolutePath)
}

private fun buildEmbeddedDesktopBaseUrl(
    publicHost: String,
    port: Int,
): String {
    return "http://$publicHost:$port/"
}

private fun ConfigCenterEnv.requireNonBlank(
    key: String,
    active: String,
): String {
    return string(key)
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: error("配置中心缺少必填项 namespace=$KCLOUD_CONFIG_CENTER_NAMESPACE active=$active key=$key")
}

private fun ConfigCenterEnv.requireInt(
    key: String,
    active: String,
): Int {
    return requireNonBlank(key, active).toIntOrNull()
        ?: error("配置中心项格式错误 namespace=$KCLOUD_CONFIG_CENTER_NAMESPACE active=$active key=$key，期望 Int。")
}

private fun ConfigCenterEnv.requireBoolean(
    key: String,
    active: String,
): Boolean {
    return when (requireNonBlank(key, active).lowercase()) {
        "true" -> true
        "false" -> false
        else -> error("配置中心项格式错误 namespace=$KCLOUD_CONFIG_CENTER_NAMESPACE active=$active key=$key，期望 Boolean。")
    }
}
