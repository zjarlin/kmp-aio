package site.addzero.kcloud.server

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.config.withFallback
import io.ktor.server.engine.*
import io.ktor.server.engine.EngineConnectorBuilder
import io.ktor.server.engine.applicationEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.*
import io.ktor.server.netty.Netty
import io.ktor.server.routing.*
import org.koin.dsl.module
import org.koin.plugin.module.dsl.withConfiguration
import site.addzero.configcenter.ConfigCenterBeanFactory
import site.addzero.configcenter.ConfigCenterEnv
import site.addzero.configcenter.configCenterJdbcSettingsOrNull
import site.addzero.configcenter.env
import site.addzero.kcloud.config.AppConfigKeys
import site.addzero.kcloud.config.KcloudFrontendRuntimeConfig
import site.addzero.kcloud.jimmer.di.JIMMER_EMBEDDED_DESKTOP_MODE_PROPERTY
import site.addzero.configcenter.normalizeConfigCenterActive
import site.addzero.configcenter.withConfigCenterOverrides
import site.addzero.starter.installConfigCenterAdminIfEnabled
import site.addzero.starter.installEffectiveConfig
import site.addzero.starter.koin.installKoin
import site.addzero.starter.koin.runStarters
import java.io.File
import java.net.ServerSocket
import org.koin.core.KoinApplication as CoreKoinApplication
import org.koin.core.module.Module as KoinModule

private const val EMBEDDED_DESKTOP_MODE_PROPERTY = "kcloud.embedded.desktop"
/**  */
private const val VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY = "vibepocket.embedded.desktop"
internal const val KCLOUD_CONFIG_CENTER_NAMESPACE = "kcloud"

interface EmbeddedDesktopServerHandle : AutoCloseable {
    val frontendRuntimeConfig: KcloudFrontendRuntimeConfig
}

@Volatile
internal var embeddedApplicationConfigOverride: ApplicationConfig? = null
@Volatile
internal var embeddedDesktopKoinConfigurer: (CoreKoinApplication.() -> Unit)? = null
@Volatile
private var embeddedDesktopBaseUrl: String? = null

private val config: HoconApplicationConfig by lazy {
    loadServerConfig(configPath = null)
}



/**
 * Server 入口。
 */
fun main(args: Array<String>) {
    val active = resolveConfigCenterActive(config)
    val runtimeSettings = resolveRuntimeSettings(
        config = config,
        active = active,
    )
    val effectiveConfig = HoconApplicationConfig(
        serverRuntimeOverrides().resolve(),
    ).withFallback(
        config.withConfigCenterOverrides(
            namespace = KCLOUD_CONFIG_CENTER_NAMESPACE,
            active = active,
        ),
    )
    val environment = applicationEnvironment {
        this.config = effectiveConfig
    }
    embeddedServer(
        factory = Netty,
        environment = environment,
        configure = {
            connectors += EngineConnectorBuilder().apply {
                host = runtimeSettings.serverHost
                port = runtimeSettings.serverPort
            }
        },
        module = Application::module,
    ).start(wait = true)
}

/**
 * 由 Ktor 启动配置指定调用。
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

/**
 * 桌面端内嵌启动入口（非阻塞），返回 server 实例。
 * 从配置中心读取桌面端内嵌启动参数。
 */
fun ktorApplication(
    configPath: String? = null,
    host: String? = null,
    port: Int? = null,
): EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> {
    require(host == null && port == null) {
        "host/port 覆盖已禁用，请改为从配置中心提供 ${AppConfigKeys.SERVER_HOST} 与 ${AppConfigKeys.DESKTOP_SERVER_PORT}。"
    }
    val config = loadEmbeddedConfig(configPath)
    val active = resolveConfigCenterActive(config)
    val runtimeSettings = resolveRuntimeSettings(
        config = config,
        active = active,
    )
    ensurePortAvailable(runtimeSettings.desktopServerPort)
    val desktopPaths = resolveEmbeddedDesktopPaths(runtimeSettings)
    val effectiveConfig = HoconApplicationConfig(
        embeddedDesktopRuntimeOverrides(
            paths = desktopPaths,
            settings = runtimeSettings,
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
    System.setProperty(JIMMER_EMBEDDED_DESKTOP_MODE_PROPERTY, "true")
    System.setProperty(VIBEPOCKET_EMBEDDED_DESKTOP_MODE_PROPERTY, "true")
    embeddedDesktopBaseUrl = buildEmbeddedDesktopBaseUrl(
        publicHost = runtimeSettings.desktopServerPublicHost,
        port = runtimeSettings.desktopServerPort,
    )

    val environment = applicationEnvironment {
        this.config = effectiveConfig
    }

    val embeddedServer = embeddedServer(
        factory = Netty,
        environment = environment,
        configure = {
            connectors += EngineConnectorBuilder().apply {
                this.host = runtimeSettings.serverHost
                this.port = runtimeSettings.desktopServerPort
            }
        },
        module = Application::module,
    )
    return embeddedServer
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
    val baseUrl = requireNotNull(embeddedDesktopBaseUrl) {
        "embeddedDesktopBaseUrl 尚未初始化。"
    }
    val frontendRuntimeConfig = KcloudFrontendRuntimeConfig(
        apiBaseUrl = baseUrl,
    )

    return object : EmbeddedDesktopServerHandle {
        override val frontendRuntimeConfig = frontendRuntimeConfig

        override fun close() {
            server.stop(
                gracePeriodMillis = 1_000,
                timeoutMillis = 5_000,
            )
            embeddedDesktopKoinConfigurer = null
            embeddedApplicationConfigOverride = null
            embeddedDesktopBaseUrl = null
            System.clearProperty(JIMMER_EMBEDDED_DESKTOP_MODE_PROPERTY)
        }
    }
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

private data class ServerCliArgs(
    val configPath: String? = null,
    val host: String? = null,
    val port: Int? = null,
)

private fun loadEmbeddedConfig(configPath: String?): HoconApplicationConfig {
    val resolvedConfig = configPath?.let { path ->
        ConfigFactory.parseFile(resolveConfigFile(path)).withFallback(ConfigFactory.load())
    } ?: ConfigFactory.load()

    return HoconApplicationConfig(
        embeddedDesktopOverrides()
            .withFallback(resolvedConfig)
            .resolve(),
    )
}

internal fun loadServerConfig(configPath: String?): HoconApplicationConfig {
    val resolvedConfig = configPath?.let { path ->
        ConfigFactory.parseFile(resolveConfigFile(path)).withFallback(ConfigFactory.load())
    } ?: ConfigFactory.load()

    return HoconApplicationConfig(
        serverRuntimeOverrides()
            .withFallback(resolvedConfig)
            .resolve(),
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

internal fun serverRuntimeOverrides() = ConfigFactory.parseString(
    """
    ktor {
      application {
        modules = []
      }
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

private fun embeddedDesktopRuntimeOverrides(
    paths: EmbeddedDesktopPaths,
    settings: KCloudRuntimeSettings,
) =
    ConfigFactory.empty()
        .withValue(AppConfigKeys.SERVER_HOST, ConfigValueFactory.fromAnyRef(settings.serverHost))
        .withValue(AppConfigKeys.SERVER_PORT, ConfigValueFactory.fromAnyRef(settings.desktopServerPort))
        .withValue(AppConfigKeys.DESKTOP_SERVER_PUBLIC_HOST, ConfigValueFactory.fromAnyRef(settings.desktopServerPublicHost))
        .withValue(AppConfigKeys.DESKTOP_SERVER_PORT, ConfigValueFactory.fromAnyRef(settings.desktopServerPort))
        .withValue(AppConfigKeys.DESKTOP_APP_DIRECTORY_NAME, ConfigValueFactory.fromAnyRef(settings.desktopAppDirectoryName))
        .withValue(AppConfigKeys.DESKTOP_SQLITE_FILE_NAME, ConfigValueFactory.fromAnyRef(settings.desktopSqliteFileName))
        .withValue("banner.text", ConfigValueFactory.fromAnyRef(settings.desktopBannerText))
        .withValue("banner.subtitle", ConfigValueFactory.fromAnyRef(settings.desktopBannerSubtitle))
        .withValue("openapi.enabled", ConfigValueFactory.fromAnyRef(settings.desktopOpenapiEnabled))
        .withValue("openapi.path", ConfigValueFactory.fromAnyRef(settings.desktopOpenapiPath))
        .withValue("openapi.spec", ConfigValueFactory.fromAnyRef(settings.desktopOpenapiSpec))
        .withValue("flyway.enabled", ConfigValueFactory.fromAnyRef(settings.desktopFlywayEnabled))
        .withValue(AppConfigKeys.SQLITE_ENABLED, ConfigValueFactory.fromAnyRef(settings.sqliteEnabled))
        .withValue(AppConfigKeys.SQLITE_URL, ConfigValueFactory.fromAnyRef(paths.sqliteJdbcUrl))
        .withValue(
            AppConfigKeys.SQLITE_DRIVER,
            ConfigValueFactory.fromAnyRef(settings.sqliteDriver),
        )
        .withValue(AppConfigKeys.POSTGRES_ENABLED, ConfigValueFactory.fromAnyRef(settings.postgresEnabled))
        .withValue("s3.enabled", ConfigValueFactory.fromAnyRef(settings.desktopS3Enabled))
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
    val jdbcSettings = config.configCenterJdbcSettingsOrNull()
        ?: error(
            "缺少配置中心 JDBC，无法读取运行时配置。请提供 config-center.jdbc.* 或 datasources.sqlite/postgres.*。",
        )
    val env = ConfigCenterBeanFactory.env(
        settings = jdbcSettings,
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
    }.distinctBy { it.absolutePath }
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
