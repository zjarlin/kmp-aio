package site.addzero.kcloud.plugins.system.configcenter

import io.ktor.server.config.ApplicationConfig
import java.sql.DriverManager

class ConfigCenterBootstrapBridge(
    private val applicationConfig: ApplicationConfig,
    private val namespace: String = "kcloud",
    private val profile: String = "default",
) {
    fun getString(
        key: String,
        defaultValue: String? = null,
    ): String? {
        return runCatching {
            openConnection().use { connection ->
                connection.prepareStatement(
                    """
                    SELECT secret.value_text
                    FROM config_center_secret secret
                    JOIN config_center_config cfg ON cfg.id = secret.config_id
                    JOIN config_center_environment env ON env.id = cfg.environment_id
                    JOIN config_center_project project ON project.id = cfg.project_id
                    WHERE project.slug = ?
                      AND env.slug = ?
                      AND cfg.config_type = ?
                      AND cfg.enabled = ?
                      AND secret.enabled = ?
                      AND secret.deleted = ?
                      AND secret.name = ?
                    ORDER BY secret.update_time DESC, secret.id DESC
                    LIMIT 1
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, namespace.trim())
                    statement.setString(2, resolveEnvironmentSlug(profile))
                    statement.setString(3, "ROOT")
                    statement.setBoolean(4, true)
                    statement.setBoolean(5, true)
                    statement.setBoolean(6, false)
                    statement.setString(7, key)
                    statement.executeQuery().use { resultSet ->
                        if (resultSet.next()) {
                            resultSet.getString(1)
                        } else {
                            defaultValue
                        }
                    }
                }
            }
        }.getOrDefault(defaultValue)
    }

    fun getInt(
        key: String,
        defaultValue: Int,
    ): Int {
        return getString(key, defaultValue.toString())
            ?.toIntOrNull()
            ?: defaultValue
    }

    private fun openConnection() = DriverManager.getConnection(
        readJdbcUrl(),
        readJdbcUsername(),
        readJdbcPassword(),
    )

    private fun readJdbcUrl(): String {
        val sqliteEnabled = applicationConfig.propertyOrNull("datasources.sqlite.enabled")
            ?.getString()
            ?.toBooleanStrictOrNull() == true
        val postgresEnabled = applicationConfig.propertyOrNull("datasources.postgres.enabled")
            ?.getString()
            ?.toBooleanStrictOrNull() == true
        return when {
            sqliteEnabled -> {
                val driver = applicationConfig.propertyOrNull("datasources.sqlite.driver")
                    ?.getString()
                    ?.ifBlank { "org.sqlite.JDBC" }
                    ?: "org.sqlite.JDBC"
                Class.forName(driver)
                applicationConfig.property("datasources.sqlite.url").getString()
            }

            postgresEnabled -> {
                val driver = applicationConfig.propertyOrNull("datasources.postgres.driver")
                    ?.getString()
                    ?.ifBlank { "org.postgresql.Driver" }
                    ?: "org.postgresql.Driver"
                Class.forName(driver)
                applicationConfig.property("datasources.postgres.url").getString()
            }

            else -> error("未启用可用于配置中心引导的数据库")
        }
    }

    private fun readJdbcUsername(): String? {
        return applicationConfig.propertyOrNull("datasources.postgres.username")?.getString()
    }

    private fun readJdbcPassword(): String? {
        return applicationConfig.propertyOrNull("datasources.postgres.password")?.getString()
    }

    private fun resolveEnvironmentSlug(
        rawProfile: String,
    ): String {
        val normalized = rawProfile.trim().ifBlank { "default" }
        return if (normalized == "default") {
            "dev"
        } else {
            normalized
        }
    }
}
