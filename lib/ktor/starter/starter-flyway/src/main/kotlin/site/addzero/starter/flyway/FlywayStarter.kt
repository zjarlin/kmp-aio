package site.addzero.starter.flyway

import io.ktor.server.application.*
import io.ktor.server.config.*
import org.flywaydb.core.Flyway
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import site.addzero.configcenter.ConfigCenterKeyDefinition
import site.addzero.configcenter.boolean
import site.addzero.configcenter.intOrNull
import site.addzero.configcenter.listOrNull
import site.addzero.configcenter.stringOrNull
import site.addzero.kcloud.jimmer.di.DatasourceConfigKeys
import site.addzero.starter.AppStarter
import site.addzero.starter.effectiveConfig

@Module
@Configuration("vibepocket")
@ComponentScan("site.addzero.starter.flyway")
class FlywayStarterKoinModule

/**
 * Flyway 数据库迁移 Starter（支持多数据源）
 *
 * 配置示例 (application.conf):
 * ```hocon
 * # 全局 Flyway 开关
 * flyway {
 *     enabled = true
 * }
 *
 * # 多数据源配置（按配置顺序执行迁移）
 * datasources {
 *     primary {
 *         enabled = true
 *         url = "jdbc:postgresql://localhost:5432/mydb"
 *         driver = "postgresql"
 *         user = "postgres"
 *         password = "secret"
 *
 *         # 该数据源的 Flyway 配置（可选，继承全局配置）
 *         flyway {
 *             enabled = true
 *             locations = ["classpath:db/migration/primary"]
 *             table = "flyway_schema_history"
 *         }
 *     }
 *
 *     secondary {
 *         enabled = true
 *         url = "jdbc:mysql://localhost:3306/mydb"
 *         driver = "mysql"
 *         user = "root"
 *         password = "secret"
 *
 *         flyway {
 *             enabled = true
 *             locations = ["filesystem:/path/to/migrations"]
 *         }
 *     }
 * }
 *
 * # 全局 Flyway 默认配置（可被各数据源覆盖）
 * flyway {
 *     locations = ["classpath:db/migration"]
 *     baseline-on-migrate = false
 *     baseline-version = "0"
 *     clean-disabled = true
 *     connect-retries = 0
 *     group = false
 *     installed-by = ""
 *     mixed = false
 *     out-of-order = false
 *     placeholder-prefix = "${"
 *     placeholder-replacement = true
 *     placeholder-suffix = "}"
 *     placeholders = {}
 *     schemas = []
 *     skip-default-callbacks = false
 *     skip-default-resolvers = false
 *     sql-migration-prefix = "V"
 *     sql-migration-separator = "__"
 *     sql-migration-suffixes = [".sql"]
 *     table = "flyway_schema_history"
 *     target = "latest"
 *     validate-on-migrate = true
 * }
 * ```
 */
@Named("flywayStarter")
@Single
class FlywayStarter : AppStarter {

    override val order: Int = 200

    override fun Application.enable(): Boolean {
        return effectiveConfig().boolean(FlywayConfigKeys.enabled) != false
    }

    override fun Application.onInstall() {
        val config = effectiveConfig()

        // 读取所有启用的数据源，按配置顺序执行
        val datasources = config.getDatasources()
            .filter { it.enabled }
            .filter { it.flywayEnabled }

        if (datasources.isEmpty()) {
            log.warn("No datasources configured for Flyway migration")
            return
        }

        // 全局默认配置
        val globalDefaults = FlywayDefaults.fromGlobalConfig(config)

        // 按数据源配置顺序执行迁移
        for (ds in datasources) {
            val dsSpecificDefaults = FlywayDefaults.fromDatasourceConfig(config, ds.name)
            val mergedDefaults = dsSpecificDefaults.mergeWith(globalDefaults)

            executeMigration(ds, mergedDefaults)
        }
    }

    private fun Application.executeMigration(
        ds: DatasourceConfig,
        defaults: FlywayDefaults,
    ) {
        log.info("Executing Flyway migration for datasource: ${ds.name}")

        val flywayConfig = Flyway.configure()
            .dataSource(ds.url, ds.user, ds.password)
            .apply {
                // 迁移文件位置
                val locations = defaults.locations ?: listOf("classpath:db/migration")
                locations(*locations.toTypedArray())

                // 应用所有可选配置
                defaults.baselineOnMigrate?.let { baselineOnMigrate(it) }
                defaults.baselineVersion?.let { baselineVersion(it) }
                defaults.cleanDisabled?.let { cleanDisabled(it) }
                defaults.connectRetries?.let { connectRetries(it) }
                defaults.group?.let { group(it) }
                defaults.installedBy?.let { installedBy(it) }
                defaults.mixed?.let { mixed(it) }
                defaults.outOfOrder?.let { outOfOrder(it) }
                defaults.placeholderReplacement?.let { placeholderReplacement(it) }
                defaults.placeholderPrefix?.let { placeholderPrefix(it) }
                defaults.placeholderSuffix?.let { placeholderSuffix(it) }
                defaults.skipDefaultCallbacks?.let { skipDefaultCallbacks(it) }
                defaults.skipDefaultResolvers?.let { skipDefaultResolvers(it) }
                defaults.sqlMigrationPrefix?.let { sqlMigrationPrefix(it) }
                defaults.sqlMigrationSeparator?.let { sqlMigrationSeparator(it) }
                defaults.table?.let { table(it) }
                defaults.target?.let { target(it) }
                defaults.validateOnMigrate?.let { validateOnMigrate(it) }

                // 列表配置
                defaults.schemas?.let { schemas(*it.toTypedArray()) }
                defaults.sqlMigrationSuffixes?.let { sqlMigrationSuffixes(*it.toTypedArray()) }
                defaults.placeholders?.let { placeholders(it) }
            }

        val flyway = flywayConfig.load()
        val result = flyway.migrate()

        log.info("Datasource [${ds.name}] migration completed: ${result.migrationsExecuted} migrations executed")
    }
}

/**
 * 数据源配置
 */
private data class DatasourceConfig(
    val name: String,
    val enabled: Boolean,
    val url: String,
    val user: String,
    val password: String,
    val driver: String,
    val flywayEnabled: Boolean,
)

/**
 * Flyway 默认配置（可合并）
 */
private data class FlywayDefaults(
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
    /**
     * 用另一个配置覆盖当前配置（非空字段优先）
     */
    fun mergeWith(other: FlywayDefaults): FlywayDefaults {
        return FlywayDefaults(
            locations = this.locations ?: other.locations,
            baselineOnMigrate = this.baselineOnMigrate ?: other.baselineOnMigrate,
            baselineVersion = this.baselineVersion ?: other.baselineVersion,
            cleanDisabled = this.cleanDisabled ?: other.cleanDisabled,
            connectRetries = this.connectRetries ?: other.connectRetries,
            group = this.group ?: other.group,
            installedBy = this.installedBy ?: other.installedBy,
            mixed = this.mixed ?: other.mixed,
            outOfOrder = this.outOfOrder ?: other.outOfOrder,
            placeholderReplacement = this.placeholderReplacement ?: other.placeholderReplacement,
            placeholderPrefix = this.placeholderPrefix ?: other.placeholderPrefix,
            placeholderSuffix = this.placeholderSuffix ?: other.placeholderSuffix,
            placeholders = this.placeholders ?: other.placeholders,
            schemas = this.schemas ?: other.schemas,
            skipDefaultCallbacks = this.skipDefaultCallbacks ?: other.skipDefaultCallbacks,
            skipDefaultResolvers = this.skipDefaultResolvers ?: other.skipDefaultResolvers,
            sqlMigrationPrefix = this.sqlMigrationPrefix ?: other.sqlMigrationPrefix,
            sqlMigrationSeparator = this.sqlMigrationSeparator ?: other.sqlMigrationSeparator,
            sqlMigrationSuffixes = this.sqlMigrationSuffixes ?: other.sqlMigrationSuffixes,
            table = this.table ?: other.table,
            target = this.target ?: other.target,
            validateOnMigrate = this.validateOnMigrate ?: other.validateOnMigrate,
        )
    }

    companion object {
        fun fromGlobalConfig(config: ApplicationConfig): FlywayDefaults {
            return fromDefinitions(
                config = config,
                locations = FlywayConfigKeys.locations,
                baselineOnMigrate = FlywayConfigKeys.baselineOnMigrate,
                baselineVersion = FlywayConfigKeys.baselineVersion,
                cleanDisabled = FlywayConfigKeys.cleanDisabled,
                connectRetries = FlywayConfigKeys.connectRetries,
                group = FlywayConfigKeys.group,
                installedBy = FlywayConfigKeys.installedBy,
                mixed = FlywayConfigKeys.mixed,
                outOfOrder = FlywayConfigKeys.outOfOrder,
                placeholderReplacement = FlywayConfigKeys.placeholderReplacement,
                placeholderPrefix = FlywayConfigKeys.placeholderPrefix,
                placeholderSuffix = FlywayConfigKeys.placeholderSuffix,
                placeholderPath = "flyway.placeholders",
                schemas = FlywayConfigKeys.schemas,
                skipDefaultCallbacks = FlywayConfigKeys.skipDefaultCallbacks,
                skipDefaultResolvers = FlywayConfigKeys.skipDefaultResolvers,
                sqlMigrationPrefix = FlywayConfigKeys.sqlMigrationPrefix,
                sqlMigrationSeparator = FlywayConfigKeys.sqlMigrationSeparator,
                sqlMigrationSuffixes = FlywayConfigKeys.sqlMigrationSuffixes,
                table = FlywayConfigKeys.table,
                target = FlywayConfigKeys.target,
                validateOnMigrate = FlywayConfigKeys.validateOnMigrate,
            )
        }

        fun fromDatasourceConfig(
            config: ApplicationConfig,
            datasourceName: String,
        ): FlywayDefaults {
            return fromDefinitions(
                config = config,
                locations = DatasourceFlywayConfigKeys.locationsDefinition(datasourceName),
                baselineOnMigrate = DatasourceFlywayConfigKeys.baselineOnMigrateDefinition(datasourceName),
                baselineVersion = DatasourceFlywayConfigKeys.baselineVersionDefinition(datasourceName),
                cleanDisabled = DatasourceFlywayConfigKeys.cleanDisabledDefinition(datasourceName),
                connectRetries = DatasourceFlywayConfigKeys.connectRetriesDefinition(datasourceName),
                group = DatasourceFlywayConfigKeys.groupDefinition(datasourceName),
                installedBy = DatasourceFlywayConfigKeys.installedByDefinition(datasourceName),
                mixed = DatasourceFlywayConfigKeys.mixedDefinition(datasourceName),
                outOfOrder = DatasourceFlywayConfigKeys.outOfOrderDefinition(datasourceName),
                placeholderReplacement = DatasourceFlywayConfigKeys.placeholderReplacementDefinition(datasourceName),
                placeholderPrefix = DatasourceFlywayConfigKeys.placeholderPrefixDefinition(datasourceName),
                placeholderSuffix = DatasourceFlywayConfigKeys.placeholderSuffixDefinition(datasourceName),
                placeholderPath = "datasources.$datasourceName.flyway.placeholders",
                schemas = DatasourceFlywayConfigKeys.schemasDefinition(datasourceName),
                skipDefaultCallbacks = DatasourceFlywayConfigKeys.skipDefaultCallbacksDefinition(datasourceName),
                skipDefaultResolvers = DatasourceFlywayConfigKeys.skipDefaultResolversDefinition(datasourceName),
                sqlMigrationPrefix = DatasourceFlywayConfigKeys.sqlMigrationPrefixDefinition(datasourceName),
                sqlMigrationSeparator = DatasourceFlywayConfigKeys.sqlMigrationSeparatorDefinition(datasourceName),
                sqlMigrationSuffixes = DatasourceFlywayConfigKeys.sqlMigrationSuffixesDefinition(datasourceName),
                table = DatasourceFlywayConfigKeys.tableDefinition(datasourceName),
                target = DatasourceFlywayConfigKeys.targetDefinition(datasourceName),
                validateOnMigrate = DatasourceFlywayConfigKeys.validateOnMigrateDefinition(datasourceName),
            )
        }
    }
}

private fun fromDefinitions(
    config: ApplicationConfig,
    locations: ConfigCenterKeyDefinition,
    baselineOnMigrate: ConfigCenterKeyDefinition,
    baselineVersion: ConfigCenterKeyDefinition,
    cleanDisabled: ConfigCenterKeyDefinition,
    connectRetries: ConfigCenterKeyDefinition,
    group: ConfigCenterKeyDefinition,
    installedBy: ConfigCenterKeyDefinition,
    mixed: ConfigCenterKeyDefinition,
    outOfOrder: ConfigCenterKeyDefinition,
    placeholderReplacement: ConfigCenterKeyDefinition,
    placeholderPrefix: ConfigCenterKeyDefinition,
    placeholderSuffix: ConfigCenterKeyDefinition,
    placeholderPath: String,
    schemas: ConfigCenterKeyDefinition,
    skipDefaultCallbacks: ConfigCenterKeyDefinition,
    skipDefaultResolvers: ConfigCenterKeyDefinition,
    sqlMigrationPrefix: ConfigCenterKeyDefinition,
    sqlMigrationSeparator: ConfigCenterKeyDefinition,
    sqlMigrationSuffixes: ConfigCenterKeyDefinition,
    table: ConfigCenterKeyDefinition,
    target: ConfigCenterKeyDefinition,
    validateOnMigrate: ConfigCenterKeyDefinition,
): FlywayDefaults {
    return FlywayDefaults(
        locations = config.listOrNull(locations),
        baselineOnMigrate = config.boolean(baselineOnMigrate),
        baselineVersion = config.stringOrNull(baselineVersion) ?: baselineVersion.defaultValue,
        cleanDisabled = config.boolean(cleanDisabled),
        connectRetries = config.intOrNull(connectRetries) ?: connectRetries.defaultValue?.toIntOrNull(),
        group = config.boolean(group),
        installedBy = config.stringOrNull(installedBy) ?: installedBy.defaultValue,
        mixed = config.boolean(mixed),
        outOfOrder = config.boolean(outOfOrder),
        placeholderReplacement = config.boolean(placeholderReplacement),
        placeholderPrefix = config.stringOrNull(placeholderPrefix) ?: placeholderPrefix.defaultValue,
        placeholderSuffix = config.stringOrNull(placeholderSuffix) ?: placeholderSuffix.defaultValue,
        placeholders = config.configMap(placeholderPath),
        schemas = config.listOrNull(schemas),
        skipDefaultCallbacks = config.boolean(skipDefaultCallbacks),
        skipDefaultResolvers = config.boolean(skipDefaultResolvers),
        sqlMigrationPrefix = config.stringOrNull(sqlMigrationPrefix) ?: sqlMigrationPrefix.defaultValue,
        sqlMigrationSeparator = config.stringOrNull(sqlMigrationSeparator) ?: sqlMigrationSeparator.defaultValue,
        sqlMigrationSuffixes = config.listOrNull(sqlMigrationSuffixes),
        table = config.stringOrNull(table) ?: table.defaultValue,
        target = config.stringOrNull(target) ?: target.defaultValue,
        validateOnMigrate = config.boolean(validateOnMigrate),
    )
}

/**
 * 从配置中读取所有数据源（保持配置顺序）
 */
private fun ApplicationConfig.getDatasources(): List<DatasourceConfig> {
    val datasources = mutableListOf<DatasourceConfig>()

    try {
        val dsConfig = config("datasources")
        val keys = dsConfig.toMap().keys

        for (name in keys) {
            try {
                datasources.add(
                    DatasourceConfig(
                        name = name,
                        enabled = boolean(DatasourceConfigKeys.enabledDefinition(name)) == true,
                        url = stringOrNull(DatasourceConfigKeys.urlDefinition(name)).orEmpty(),
                        user = stringOrNull(DatasourceConfigKeys.userDefinition(name)).orEmpty(),
                        password = stringOrNull(DatasourceConfigKeys.passwordDefinition(name)).orEmpty(),
                        driver = stringOrNull(DatasourceConfigKeys.driverDefinition(name)).orEmpty(),
                        flywayEnabled = boolean(DatasourceFlywayConfigKeys.enabledDefinition(name)) != false,
                    ),
                )
            } catch (e: Exception) {
                // 跳过无效配置
            }
        }
    } catch (e: Exception) {
        // 无 datasources 配置
    }

    return datasources
}

/**
 * 辅助函数：获取嵌套配置映射
 */
private fun ApplicationConfig.configMap(path: String): Map<String, String>? {
    return try {
        val cfg = config(path)
        val keys = cfg.keys()
        keys.associateWith { cfg.property(it).getString() }
    } catch (e: Exception) {
        null
    }
}
