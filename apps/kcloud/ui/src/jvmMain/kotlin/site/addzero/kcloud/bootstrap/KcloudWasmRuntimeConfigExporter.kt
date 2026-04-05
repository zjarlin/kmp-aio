package site.addzero.kcloud.bootstrap

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.HoconApplicationConfig
import kotlinx.serialization.encodeToString
import site.addzero.configcenter.ConfigCenterBeanFactory
import site.addzero.configcenter.configCenterJdbcSettingsOrNull
import site.addzero.configcenter.env
import site.addzero.core.network.json.prettyJson
import site.addzero.kcloud.config.KcloudFrontendPublicConfigKeys
import site.addzero.kcloud.config.KcloudFrontendRuntimeConfig
import site.addzero.kcloud.plugins.system.configcenter.spi.requireRuntimeConfigCenterActive
import java.io.File

private const val KTOR_ENVIRONMENT_KEY = "ktor.environment"
private const val CONFIG_CENTER_JDBC_URL_KEY = "config-center.jdbc.url"
private const val CONFIG_CENTER_JDBC_USER_KEY = "config-center.jdbc.user"
private const val CONFIG_CENTER_JDBC_PASSWORD_KEY = "config-center.jdbc.password"
private const val CONFIG_CENTER_JDBC_DRIVER_KEY = "config-center.jdbc.driver"
private const val CONFIG_CENTER_JDBC_AUTO_DDL_KEY = "config-center.jdbc.auto-ddl"
private const val DATASOURCE_SQLITE_ENABLED_KEY = "datasources.sqlite.enabled"
private const val DATASOURCE_SQLITE_URL_KEY = "datasources.sqlite.url"
private const val DATASOURCE_POSTGRES_ENABLED_KEY = "datasources.postgres.enabled"
private const val DATASOURCE_POSTGRES_URL_KEY = "datasources.postgres.url"

private val exporterJson = prettyJson

fun main(
    args: Array<String>,
) {
    val options = parseExporterOptions(args)
    val applicationConfig = buildExporterApplicationConfig(options.configPath)
    val runtimeConfig = resolveWasmRuntimeConfig(applicationConfig)
    val outputFile = File(options.outputPath).absoluteFile
    outputFile.parentFile?.mkdirs()
    outputFile.writeText(
        exporterJson.encodeToString(runtimeConfig),
    )
}

internal fun resolveWasmRuntimeConfig(
    applicationConfig: ApplicationConfig,
): KcloudFrontendRuntimeConfig {
    requireExporterBootstrapInputs(applicationConfig)
    val active = requireRuntimeConfigCenterActive(
        applicationConfig.propertyOrNull(KTOR_ENVIRONMENT_KEY)?.getString(),
    )
    val jdbcSettings = applicationConfig.configCenterJdbcSettingsOrNull()
        ?: error("导出 KCloud Wasm runtime config 缺少配置中心 JDBC。")
    val env = ConfigCenterBeanFactory.env(
        settings = jdbcSettings,
        namespace = KcloudFrontendPublicConfigKeys.NAMESPACE,
        active = active,
    )
    val apiBaseUrl = env.string(KcloudFrontendPublicConfigKeys.FRONTEND_API_BASE_URL)
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: error(
            "配置中心缺少前端公开配置 namespace=${KcloudFrontendPublicConfigKeys.NAMESPACE} " +
                "active=$active key=${KcloudFrontendPublicConfigKeys.FRONTEND_API_BASE_URL}",
        )
    return KcloudFrontendRuntimeConfig(
        apiBaseUrl = apiBaseUrl,
    )
}

private data class ExporterOptions(
    val outputPath: String,
    val configPath: String? = null,
)

private fun parseExporterOptions(
    args: Array<String>,
): ExporterOptions {
    val outputPath = args.firstOrNull { it.startsWith("--output=") }
        ?.substringAfter('=')
        ?.trim()
        .orEmpty()
    require(outputPath.isNotBlank()) {
        "缺少 --output 参数。"
    }
    val configPath = args.firstOrNull { it.startsWith("--config=") }
        ?.substringAfter('=')
        ?.trim()
        ?.ifBlank { null }
    return ExporterOptions(
        outputPath = outputPath,
        configPath = configPath,
    )
}

internal fun buildExporterApplicationConfig(
    configPath: String?,
    systemProperties: Map<String, String> = currentSystemProperties(),
    environmentVariables: Map<String, String> = currentEnvironmentVariables(),
): ApplicationConfig {
    val fileConfig = configPath?.let { path ->
        ConfigFactory.parseFile(resolveConfigFile(path))
    } ?: ConfigFactory.empty()
    val overrideConfig = buildExporterOverrideConfig(
        systemProperties = systemProperties,
        environmentVariables = environmentVariables,
    )
    val resolved = overrideConfig
        .withFallback(fileConfig)
        .withFallback(ConfigFactory.load())
        .resolve()
    return HoconApplicationConfig(resolved)
}

private fun requireExporterBootstrapInputs(
    applicationConfig: ApplicationConfig,
) {
    val environment = applicationConfig.propertyOrNull(KTOR_ENVIRONMENT_KEY)
        ?.getString()
        ?.trim()
        .orEmpty()
    check(environment.isNotBlank()) {
        "导出 KCloud Wasm runtime config 缺少 $KTOR_ENVIRONMENT_KEY。请传 --config=<file>，" +
            "或提供 -PkcloudKtorEnvironment / -Pktor.environment / KCLOUD_KTOR_ENVIRONMENT。"
    }
    check(hasExporterJdbcBootstrap(applicationConfig)) {
        "导出 KCloud Wasm runtime config 缺少配置中心 JDBC。请传 --config=<file>，" +
            "或提供 -PkcloudConfigCenterJdbcUrl / -Pconfig-center.jdbc.url / KCLOUD_CONFIG_CENTER_JDBC_URL。"
    }
}

private fun hasExporterJdbcBootstrap(
    applicationConfig: ApplicationConfig,
): Boolean {
    if (!applicationConfig.propertyOrNull(CONFIG_CENTER_JDBC_URL_KEY)?.getString().isNullOrBlank()) {
        return true
    }
    val sqliteEnabled = applicationConfig.propertyOrNull(DATASOURCE_SQLITE_ENABLED_KEY)
        ?.getString()
        ?.trim()
        ?.equals("true", ignoreCase = true) == true
    if (sqliteEnabled && !applicationConfig.propertyOrNull(DATASOURCE_SQLITE_URL_KEY)?.getString().isNullOrBlank()) {
        return true
    }
    val postgresEnabled = applicationConfig.propertyOrNull(DATASOURCE_POSTGRES_ENABLED_KEY)
        ?.getString()
        ?.trim()
        ?.equals("true", ignoreCase = true) == true
    return postgresEnabled &&
        !applicationConfig.propertyOrNull(DATASOURCE_POSTGRES_URL_KEY)?.getString().isNullOrBlank()
}

private fun buildExporterOverrideConfig(
    systemProperties: Map<String, String>,
    environmentVariables: Map<String, String>,
): com.typesafe.config.Config {
    val entries = linkedMapOf<String, String>()
    putExporterOverride(
        target = entries,
        configKey = KTOR_ENVIRONMENT_KEY,
        systemProperties = systemProperties,
        environmentVariables = environmentVariables,
        systemKeys = arrayOf(
            KTOR_ENVIRONMENT_KEY,
            "kcloudKtorEnvironment",
        ),
        environmentKeys = arrayOf(
            "KCLOUD_KTOR_ENVIRONMENT",
        ),
    )
    putExporterOverride(
        target = entries,
        configKey = CONFIG_CENTER_JDBC_URL_KEY,
        systemProperties = systemProperties,
        environmentVariables = environmentVariables,
        systemKeys = arrayOf(
            CONFIG_CENTER_JDBC_URL_KEY,
            "kcloudConfigCenterJdbcUrl",
        ),
        environmentKeys = arrayOf(
            "KCLOUD_CONFIG_CENTER_JDBC_URL",
        ),
    )
    putExporterOverride(
        target = entries,
        configKey = CONFIG_CENTER_JDBC_USER_KEY,
        systemProperties = systemProperties,
        environmentVariables = environmentVariables,
        systemKeys = arrayOf(
            CONFIG_CENTER_JDBC_USER_KEY,
            "config-center.jdbc.username",
            "kcloudConfigCenterJdbcUser",
            "kcloudConfigCenterJdbcUsername",
        ),
        environmentKeys = arrayOf(
            "KCLOUD_CONFIG_CENTER_JDBC_USER",
            "KCLOUD_CONFIG_CENTER_JDBC_USERNAME",
        ),
    )
    putExporterOverride(
        target = entries,
        configKey = CONFIG_CENTER_JDBC_PASSWORD_KEY,
        systemProperties = systemProperties,
        environmentVariables = environmentVariables,
        systemKeys = arrayOf(
            CONFIG_CENTER_JDBC_PASSWORD_KEY,
            "kcloudConfigCenterJdbcPassword",
        ),
        environmentKeys = arrayOf(
            "KCLOUD_CONFIG_CENTER_JDBC_PASSWORD",
        ),
    )
    putExporterOverride(
        target = entries,
        configKey = CONFIG_CENTER_JDBC_DRIVER_KEY,
        systemProperties = systemProperties,
        environmentVariables = environmentVariables,
        systemKeys = arrayOf(
            CONFIG_CENTER_JDBC_DRIVER_KEY,
            "kcloudConfigCenterJdbcDriver",
        ),
        environmentKeys = arrayOf(
            "KCLOUD_CONFIG_CENTER_JDBC_DRIVER",
        ),
    )
    putExporterOverride(
        target = entries,
        configKey = CONFIG_CENTER_JDBC_AUTO_DDL_KEY,
        systemProperties = systemProperties,
        environmentVariables = environmentVariables,
        systemKeys = arrayOf(
            CONFIG_CENTER_JDBC_AUTO_DDL_KEY,
            "kcloudConfigCenterJdbcAutoDdl",
        ),
        environmentKeys = arrayOf(
            "KCLOUD_CONFIG_CENTER_JDBC_AUTO_DDL",
        ),
    )

    var config = ConfigFactory.empty()
    for ((key, value) in entries) {
        config = config.withValue(
            key,
            ConfigValueFactory.fromAnyRef(value),
        )
    }
    return config
}

private fun putExporterOverride(
    target: MutableMap<String, String>,
    configKey: String,
    systemProperties: Map<String, String>,
    environmentVariables: Map<String, String>,
    systemKeys: Array<String>,
    environmentKeys: Array<String>,
) {
    val value = systemKeys.firstNotNullOfOrNull { key ->
        systemProperties[key]?.trim()?.takeIf(String::isNotBlank)
    } ?: environmentKeys.firstNotNullOfOrNull { key ->
        environmentVariables[key]?.trim()?.takeIf(String::isNotBlank)
    } ?: return
    target[configKey] = value
}

private fun currentSystemProperties(): Map<String, String> {
    return System.getProperties().stringPropertyNames().associateWith(System::getProperty)
}

private fun currentEnvironmentVariables(): Map<String, String> {
    return System.getenv()
}

private fun resolveConfigFile(
    path: String,
): File {
    val direct = File(path)
    if (direct.exists()) {
        return direct
    }

    var current: File? = File(System.getProperty("user.dir").orEmpty()).absoluteFile
    while (current != null) {
        val candidate = File(current, path)
        if (candidate.exists()) {
            return candidate
        }
        current = current.parentFile
    }

    return direct
}
