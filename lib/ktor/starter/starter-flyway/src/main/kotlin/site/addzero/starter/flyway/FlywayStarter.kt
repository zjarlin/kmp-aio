package site.addzero.starter.flyway

import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import site.addzero.starter.AppStarter

@Module
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
class FlywayStarter(
    private val configProviders: List<FlywayConfigSpi>,
) : AppStarter {
    override val order = 200

    override fun Application.enable(): Boolean {
        return resolvePlan()?.enabled ?: false
    }

    override fun Application.onInstall() {
        val configPlan = resolvePlan() ?: run {
            log.warn("No FlywayConfigSpi registered, skipping Flyway starter")
            return
        }
        val datasources = resolveDatasources(configPlan)
            .filter { it.enabled }
            .filter { it.flywayEnabled }

        if (datasources.isEmpty()) {
            log.warn("No datasources configured for Flyway migration")
            return
        }

        val globalDefaults = configPlan.defaults

        // 按数据源配置顺序执行迁移
        for (ds in datasources) {
            val mergedDefaults = ds.defaults.mergeWith(globalDefaults)

            executeMigration(ds, mergedDefaults)
        }
    }

    private fun Application.resolvePlan(): FlywayConfigPlan? {
        if (configProviders.isEmpty()) {
            return null
        }
        val providers = configProviders.sortedBy(FlywayConfigSpi::order)
        if (providers.size > 1) {
            log.warn(
                "Multiple FlywayConfigSpi registered, using ${providers.first()::class.qualifiedName}",
            )
        }
        return providers.first().plan()
    }

    private fun Application.resolveDatasources(
        configPlan: FlywayConfigPlan,
    ): List<FlywayDatasourcePlan> {
        val resolved = LinkedHashMap<String, FlywayDatasourcePlan>()
        configPlan.datasources
            .forEach { datasource ->
                val previous = resolved.putIfAbsent(datasource.name, datasource)
                require(previous == null) {
                    "Duplicate Flyway datasource config detected for '${datasource.name}'"
                }
            }
        return resolved.values.toList()
    }

    private fun Application.executeMigration(
        ds: FlywayDatasourcePlan,
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
