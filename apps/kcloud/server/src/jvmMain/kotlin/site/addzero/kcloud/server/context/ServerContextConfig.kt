package site.addzero.kcloud.server.context

import site.addzero.starter.flyway.FlywayDefaults
import java.io.File

data class ServerContextConfig(
    val network: ServerNetworkConfig,
    val banner: ServerBannerSettings,
    val openApi: ServerOpenApiSettings,
    val flyway: ServerFlywaySettings,
    val s3: ServerS3Settings,
    val datasources: List<ServerDatasourceConfig>,
)

data class ServerNetworkConfig(
    val host: String,
    val port: Int,
)

data class ServerBannerSettings(
    val enabled: Boolean,
    val text: String,
    val subtitle: String,
)

data class ServerOpenApiSettings(
    val enabled: Boolean,
    val path: String,
    val spec: String,
)

data class ServerFlywaySettings(
    val enabled: Boolean,
    val defaults: FlywayDefaults,
)

data class ServerS3Settings(
    val enabled: Boolean,
    val endpoint: String,
    val region: String,
    val bucket: String,
    val accessKey: String,
    val secretKey: String,
)

data class ServerDatasourceConfig(
    val name: String,
    val enabled: Boolean,
    val default: Boolean,
    val url: String,
    val driverClassName: String,
    val user: String = "",
    val password: String = "",
    val flywayEnabled: Boolean = false,
    val flywayDefaults: FlywayDefaults = FlywayDefaults(),
)

internal fun hardcodedServerContextConfig(): ServerContextConfig {
    return HardcodedServerContextHolder.value
}

private object HardcodedServerContextHolder {
    val value: ServerContextConfig = buildServerContextConfig()
}

private fun buildServerContextConfig(): ServerContextConfig {
    val serverDataDirectory = ensureServerDataDirectory()
    val sqliteFile = File(serverDataDirectory, "kcloud.sqlite").absoluteFile

    return ServerContextConfig(
        network = ServerNetworkConfig(
            host = "0.0.0.0",
            port = 8080,
        ),
        banner = ServerBannerSettings(
            enabled = true,
            text = "KCLOUD",
            subtitle = "Workbench",
        ),
        openApi = ServerOpenApiSettings(
            enabled = false,
            path = "/openapi",
            spec = "openapi/documentation.yaml",
        ),
        flyway = ServerFlywaySettings(
            enabled = false,
            defaults = FlywayDefaults(
                locations = listOf("classpath:db/migration"),
                cleanDisabled = true,
                validateOnMigrate = true,
            ),
        ),
        s3 = ServerS3Settings(
            enabled = false,
            endpoint = "http://127.0.0.1:9000",
            region = "us-east-1",
            bucket = "kcloud",
            accessKey = "",
            secretKey = "",
        ),
        datasources = listOf(
            ServerDatasourceConfig(
                name = "sqlite",
                enabled = true,
                default = true,
                url = "jdbc:sqlite:${sqliteFile.absolutePath}",
                driverClassName = "org.sqlite.JDBC",
                flywayEnabled = false,
            ),
            ServerDatasourceConfig(
                name = "postgres",
                enabled = false,
                default = false,
                url = "",
                driverClassName = "org.postgresql.Driver",
                flywayEnabled = false,
            ),
        ),
    )
}

/**
 * 统一把本地服务的数据目录固定到用户目录，避免跟工作目录耦合。
 */
private fun ensureServerDataDirectory(): File {
    val directory = File(System.getProperty("user.home"), ".kcloud/server").absoluteFile
    check(directory.mkdirs() || directory.isDirectory) {
        "无法创建 KCloud 服务数据目录：${directory.absolutePath}"
    }
    return directory
}
