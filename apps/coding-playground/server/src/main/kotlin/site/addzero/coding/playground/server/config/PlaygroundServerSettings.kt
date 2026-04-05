package site.addzero.coding.playground.server.config

import io.ktor.server.config.MapApplicationConfig
import java.nio.file.Path
import java.nio.file.Paths
import site.addzero.configcenter.ConfigCenterBeanFactory
import site.addzero.configcenter.configCenterJdbcSettingsOrNull
import site.addzero.configcenter.ConfigCenterEnv
import site.addzero.configcenter.env
import site.addzero.configcenter.normalizeConfigCenterActive
import kotlin.io.path.createDirectories

data class PlaygroundServerSettings(
    val dataDirectory: Path,
    val sqliteUrl: String,
    val serverHost: String,
    val serverPort: Int,
) {
    companion object {
        fun fromSystem(): PlaygroundServerSettings {
            val runtimeSettings = loadPlaygroundRuntimeSettings()
            return PlaygroundServerSettings(
                dataDirectory = runtimeSettings.dataDirectory,
                sqliteUrl = runtimeSettings.sqliteUrl,
                serverHost = runtimeSettings.serverHost,
                serverPort = runtimeSettings.serverPort,
            )
        }
    }
}

fun defaultPlaygroundHttpServerEnabled(): Boolean {
    return loadPlaygroundRuntimeSettings().httpServerEnabled
}

private data class PlaygroundRuntimeSettings(
    val dataDirectory: Path,
    val sqliteUrl: String,
    val serverHost: String,
    val serverPort: Int,
    val httpServerEnabled: Boolean,
)

private fun loadPlaygroundRuntimeSettings(): PlaygroundRuntimeSettings {
    val active = resolvePlaygroundConfigCenterActive()
    val env = buildPlaygroundBootstrapEnv(
        active = active,
    )
    val dataDirectory = Paths.get(
        env.string(PLAYGROUND_DATA_DIR_KEY)
            ?: System.getProperty(PLAYGROUND_DATA_DIR_KEY)
            ?: defaultPlaygroundDataDirectory().toString(),
    ).toAbsolutePath().normalize()
    dataDirectory.createDirectories()
    val sqliteUrl = env.string(PLAYGROUND_DB_URL_KEY)
        ?: System.getProperty(PLAYGROUND_DB_URL_KEY)
        ?: "jdbc:sqlite:${dataDirectory.resolve("coding-playground.db")}"
    val serverHost = env.string(PLAYGROUND_SERVER_HOST_KEY)
        ?: System.getProperty(PLAYGROUND_SERVER_HOST_KEY)
        ?: "127.0.0.1"
    val serverPort = env.string(PLAYGROUND_SERVER_PORT_KEY)?.toIntOrNull()
        ?: System.getProperty(PLAYGROUND_SERVER_PORT_KEY)?.toIntOrNull()
        ?: 18181
    val httpServerEnabled = env.string(PLAYGROUND_HTTP_ENABLED_KEY)?.toBooleanStrictOrNull()
        ?: System.getProperty(PLAYGROUND_HTTP_ENABLED_KEY)?.toBooleanStrictOrNull()
        ?: false
    return PlaygroundRuntimeSettings(
        dataDirectory = dataDirectory,
        sqliteUrl = sqliteUrl,
        serverHost = serverHost,
        serverPort = serverPort,
        httpServerEnabled = httpServerEnabled,
    )
}

private fun buildPlaygroundBootstrapEnv(
    active: String,
): ConfigCenterEnv {
    val bootstrapConfig = buildPlaygroundBootstrapConfig()
    val jdbcSettings = bootstrapConfig.configCenterJdbcSettingsOrNull()
        ?: error("缺少 coding-playground 配置中心 JDBC 启动参数。")
    return ConfigCenterBeanFactory.env(
        settings = jdbcSettings,
        namespace = PLAYGROUND_CONFIG_NAMESPACE,
        active = active,
    )
}

private fun buildPlaygroundBootstrapConfig(): MapApplicationConfig {
    val bootstrapDataDirectory = Paths.get(
        System.getProperty(
            PLAYGROUND_DATA_DIR_KEY,
            defaultPlaygroundDataDirectory().toString(),
        ),
    ).toAbsolutePath().normalize()
    val bootstrapSqliteUrl = System.getProperty(
        PLAYGROUND_DB_URL_KEY,
        "jdbc:sqlite:${bootstrapDataDirectory.resolve("coding-playground.db")}",
    )
    return MapApplicationConfig(
        "datasources.sqlite.enabled" to "true",
        "datasources.sqlite.driver" to "org.sqlite.JDBC",
        "datasources.sqlite.url" to bootstrapSqliteUrl,
    )
}

private fun defaultPlaygroundDataDirectory(): Path {
    return Paths.get(System.getProperty("user.home")).resolve(".coding-playground")
}

private fun resolvePlaygroundConfigCenterActive(): String {
    return normalizeConfigCenterActive(
        System.getProperty(PLAYGROUND_ACTIVE_KEY)
            ?: System.getenv("KTOR_ENV")
            ?: "dev",
    )
}

private const val PLAYGROUND_CONFIG_NAMESPACE = "coding-playground"
private const val PLAYGROUND_ACTIVE_KEY = "coding.playground.active"
private const val PLAYGROUND_DATA_DIR_KEY = "coding.playground.data.dir"
private const val PLAYGROUND_DB_URL_KEY = "coding.playground.db.url"
private const val PLAYGROUND_SERVER_HOST_KEY = "coding.playground.server.host"
private const val PLAYGROUND_SERVER_PORT_KEY = "coding.playground.server.port"
private const val PLAYGROUND_HTTP_ENABLED_KEY = "coding.playground.http.enabled"
