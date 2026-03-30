package site.addzero.coding.playground.server.config

import io.ktor.server.config.MapApplicationConfig
import java.nio.file.Path
import java.nio.file.Paths
import site.addzero.starter.normalizeConfigCenterActive
import site.addzero.starter.readConfigCenterValues
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
    val overrideValues = buildPlaygroundBootstrapConfig().readConfigCenterValues(
        namespace = PLAYGROUND_CONFIG_NAMESPACE,
        active = resolvePlaygroundConfigCenterActive(),
    )
    val dataDirectory = Paths.get(
        overrideValues[PLAYGROUND_DATA_DIR_KEY]
            ?: System.getProperty(PLAYGROUND_DATA_DIR_KEY)
            ?: defaultPlaygroundDataDirectory().toString(),
    ).toAbsolutePath().normalize()
    dataDirectory.createDirectories()
    val sqliteUrl = overrideValues[PLAYGROUND_DB_URL_KEY]
        ?: System.getProperty(PLAYGROUND_DB_URL_KEY)
        ?: "jdbc:sqlite:${dataDirectory.resolve("coding-playground.db")}"
    val serverHost = overrideValues[PLAYGROUND_SERVER_HOST_KEY]
        ?: System.getProperty(PLAYGROUND_SERVER_HOST_KEY)
        ?: "127.0.0.1"
    val serverPort = overrideValues[PLAYGROUND_SERVER_PORT_KEY]?.toIntOrNull()
        ?: System.getProperty(PLAYGROUND_SERVER_PORT_KEY)?.toIntOrNull()
        ?: 18181
    val httpServerEnabled = overrideValues[PLAYGROUND_HTTP_ENABLED_KEY]?.toBooleanStrictOrNull()
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
