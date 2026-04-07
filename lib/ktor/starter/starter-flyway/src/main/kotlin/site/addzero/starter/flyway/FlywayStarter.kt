package site.addzero.starter.flyway

import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import site.addzero.starter.AppStarter

@Module
@Configuration
@ComponentScan("site.addzero.starter.flyway")
class FlywayStarterKoinModule
@Named("flywayStarter")
@Single
class FlywayStarter(
    private val configProviders: List<FlywayConfigSpi>,
) : AppStarter {
    override val order = 200
    override val enable: Boolean
        get() = configProviders.isNotEmpty()

    override fun onInstall(application: Application) {
        val configPlan = resolvePlan(application) ?: run {
            return
        }
        if (!configPlan.enabled) {
            application.log.info("Flyway starter disabled by config")
            return
        }
        val datasources = resolveDatasources(configPlan)
            .filter { it.enabled }
            .filter { it.flywayEnabled }

        if (datasources.isEmpty()) {
            application.log.warn("No datasources configured for Flyway migration")
            return
        }

        val globalDefaults = configPlan.defaults

        // 按数据源配置顺序执行迁移
        for (ds in datasources) {
            val mergedDefaults = ds.defaults.mergeWith(globalDefaults)

            executeMigration(application, ds, mergedDefaults)
        }
    }

    private fun resolvePlan(application: Application): FlywayConfigPlan? {
        if (configProviders.isEmpty()) {
            application.log.warn("No FlywayConfigSpi registered, skipping Flyway starter")
            return null
        }
        val providers = configProviders.sortedBy(FlywayConfigSpi::order)
        if (providers.size > 1) {
            application.log.warn(
                "Multiple FlywayConfigSpi registered, using ${providers.first()::class.qualifiedName}",
            )
        }
        return providers.first().plan()
    }

    private fun resolveDatasources(
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

    private fun executeMigration(
        application: Application,
        ds: FlywayDatasourcePlan,
        defaults: FlywayDefaults,
    ) {
        application.log.info("Executing Flyway migration for datasource: ${ds.name}")

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

        application.log.info("Datasource [${ds.name}] migration completed: ${result.migrationsExecuted} migrations executed")
    }
}
