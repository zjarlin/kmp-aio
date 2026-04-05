package site.addzero.starter.flyway

import io.ktor.server.config.ApplicationConfig
import site.addzero.configcenter.ConfigCenter
import site.addzero.configcenter.ConfigCenterEnv
import site.addzero.configcenter.ConfigCenterScopedEnv

data class FlywayDefaults(
    val locations: List<String>? = null,
    val baselineOnMigrate: Boolean? = null,
    val baselineVersion: String? = null,
    val cleanDisabled: Boolean? = null,
    val connectRetries: Int? = null,
    val group: Boolean? = null,
    val installedBy: String? = null,
    val mixed: Boolean? = null,
    val outOfOrder: Boolean? = null,
    val placeholderReplacement: Boolean? = null,
    val placeholderPrefix: String? = null,
    val placeholderSuffix: String? = null,
    val placeholders: Map<String, String>? = null,
    val schemas: List<String>? = null,
    val skipDefaultCallbacks: Boolean? = null,
    val skipDefaultResolvers: Boolean? = null,
    val sqlMigrationPrefix: String? = null,
    val sqlMigrationSeparator: String? = null,
    val sqlMigrationSuffixes: List<String>? = null,
    val table: String? = null,
    val target: String? = null,
    val validateOnMigrate: Boolean? = null,
) {
    fun mergeWith(other: FlywayDefaults): FlywayDefaults {
        return FlywayDefaults(
            locations = locations ?: other.locations,
            baselineOnMigrate = baselineOnMigrate ?: other.baselineOnMigrate,
            baselineVersion = baselineVersion ?: other.baselineVersion,
            cleanDisabled = cleanDisabled ?: other.cleanDisabled,
            connectRetries = connectRetries ?: other.connectRetries,
            group = group ?: other.group,
            installedBy = installedBy ?: other.installedBy,
            mixed = mixed ?: other.mixed,
            outOfOrder = outOfOrder ?: other.outOfOrder,
            placeholderReplacement = placeholderReplacement ?: other.placeholderReplacement,
            placeholderPrefix = placeholderPrefix ?: other.placeholderPrefix,
            placeholderSuffix = placeholderSuffix ?: other.placeholderSuffix,
            placeholders = placeholders ?: other.placeholders,
            schemas = schemas ?: other.schemas,
            skipDefaultCallbacks = skipDefaultCallbacks ?: other.skipDefaultCallbacks,
            skipDefaultResolvers = skipDefaultResolvers ?: other.skipDefaultResolvers,
            sqlMigrationPrefix = sqlMigrationPrefix ?: other.sqlMigrationPrefix,
            sqlMigrationSeparator = sqlMigrationSeparator ?: other.sqlMigrationSeparator,
            sqlMigrationSuffixes = sqlMigrationSuffixes ?: other.sqlMigrationSuffixes,
            table = table ?: other.table,
            target = target ?: other.target,
            validateOnMigrate = validateOnMigrate ?: other.validateOnMigrate,
        )
    }

    companion object {
        fun fromGlobalConfig(config: ApplicationConfig): FlywayDefaults {
            return fromEnv(ConfigCenter.getEnv(config).path("flyway"))
        }

        fun fromGlobalEnv(env: ConfigCenterEnv): FlywayDefaults {
            return fromEnv(env.path("flyway"))
        }

        fun fromDatasourceConfig(
            config: ApplicationConfig,
            datasourceName: String,
        ): FlywayDefaults {
            return fromEnv(ConfigCenter.getEnv(config).path("datasources", datasourceName, "flyway"))
        }

        fun fromDatasourceEnv(
            env: ConfigCenterEnv,
            datasourceName: String,
        ): FlywayDefaults {
            return fromEnv(env.path("datasources", datasourceName, "flyway"))
        }
    }
}

private fun fromEnv(
    env: ConfigCenterScopedEnv,
): FlywayDefaults {
    return FlywayDefaults(
        locations = env.list("locations"),
        baselineOnMigrate = env.boolean("baseline-on-migrate", false),
        baselineVersion = env.string("baseline-version", "0"),
        cleanDisabled = env.boolean("clean-disabled", true),
        connectRetries = env.int("connect-retries", 0),
        group = env.boolean("group", false),
        installedBy = env.string("installed-by"),
        mixed = env.boolean("mixed", false),
        outOfOrder = env.boolean("out-of-order", false),
        placeholderReplacement = env.boolean("placeholder-replacement", true),
        placeholderPrefix = env.string("placeholder-prefix", "\${"),
        placeholderSuffix = env.string("placeholder-suffix", "}"),
        placeholders = env.child("placeholders").map(),
        schemas = env.list("schemas"),
        skipDefaultCallbacks = env.boolean("skip-default-callbacks", false),
        skipDefaultResolvers = env.boolean("skip-default-resolvers", false),
        sqlMigrationPrefix = env.string("sql-migration-prefix", "V"),
        sqlMigrationSeparator = env.string("sql-migration-separator", "__"),
        sqlMigrationSuffixes = env.list("sql-migration-suffixes"),
        table = env.string("table", "flyway_schema_history"),
        target = env.string("target", "latest"),
        validateOnMigrate = env.boolean("validate-on-migrate", true),
    )
}
