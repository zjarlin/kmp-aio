package site.addzero.kcloud.server.context

import org.koin.core.annotation.Single
import site.addzero.configcenter.ConfigCenterEnv
import site.addzero.configcenter.ConfigCenterScopedEnv
import site.addzero.starter.flyway.FlywayConfigPlan
import site.addzero.starter.flyway.FlywayConfigSpi
import site.addzero.starter.flyway.FlywayDatasourcePlan
import site.addzero.starter.flyway.FlywayDefaults

@Single
class FlywayConfig(
    private val env: ConfigCenterEnv,
) : FlywayConfigSpi {
    override val order = 0

    override fun plan(): FlywayConfigPlan {
        val datasourceRoot = env.path("datasources")
        val datasources = datasourceRoot.keys().map { name ->
            val datasourceEnv = datasourceRoot.child(name)
            FlywayDatasourcePlan(
                name = name,
                enabled = datasourceEnv.boolean("enabled", false) == true,
                url = datasourceEnv.string("url").orEmpty(),
                user = datasourceEnv.string("user").orEmpty(),
                password = datasourceEnv.string("password").orEmpty(),
                driver = datasourceEnv.string("driver").orEmpty(),
                flywayEnabled = datasourceEnv.child("flyway").boolean("enabled", true) != false,
                defaults = datasourceEnv.child("flyway").toFlywayDefaults(),
            )
        }
        return FlywayConfigPlan(
            enabled = env.path("flyway").boolean("enabled", true) != false,
            defaults = env.path("flyway").toFlywayDefaults(),
            datasources = datasources,
        )
    }
}

private fun ConfigCenterScopedEnv.toFlywayDefaults(): FlywayDefaults {
    return FlywayDefaults(
        locations = list("locations"),
        baselineOnMigrate = boolean("baseline-on-migrate", false),
        baselineVersion = string("baseline-version", "0"),
        cleanDisabled = boolean("clean-disabled", true),
        connectRetries = int("connect-retries", 0),
        group = boolean("group", false),
        installedBy = string("installed-by"),
        mixed = boolean("mixed", false),
        outOfOrder = boolean("out-of-order", false),
        placeholderReplacement = boolean("placeholder-replacement", true),
        placeholderPrefix = string("placeholder-prefix", "\${"),
        placeholderSuffix = string("placeholder-suffix", "}"),
        placeholders = child("placeholders").map(),
        schemas = list("schemas"),
        skipDefaultCallbacks = boolean("skip-default-callbacks", false),
        skipDefaultResolvers = boolean("skip-default-resolvers", false),
        sqlMigrationPrefix = string("sql-migration-prefix", "V"),
        sqlMigrationSeparator = string("sql-migration-separator", "__"),
        sqlMigrationSuffixes = list("sql-migration-suffixes"),
        table = string("table", "flyway_schema_history"),
        target = string("target", "latest"),
        validateOnMigrate = boolean("validate-on-migrate", true),
    )
}
