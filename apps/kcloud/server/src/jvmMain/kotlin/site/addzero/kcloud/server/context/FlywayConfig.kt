package site.addzero.kcloud.server.context

import org.koin.core.annotation.Single
import site.addzero.starter.flyway.FlywayConfigPlan
import site.addzero.starter.flyway.FlywayConfigSpi
import site.addzero.starter.flyway.FlywayDatasourcePlan

@Single
class FlywayConfig(
    private val config: ServerContextConfig,
) : FlywayConfigSpi {
    override val order = 0

    override fun plan(): FlywayConfigPlan {
        val datasources = config.datasources.map { datasource ->
            FlywayDatasourcePlan(
                name = datasource.name,
                enabled = datasource.enabled,
                url = datasource.url,
                user = datasource.user,
                password = datasource.password,
                driver = datasource.driverClassName,
                flywayEnabled = datasource.flywayEnabled,
                defaults = datasource.flywayDefaults,
            )
        }
        return FlywayConfigPlan(
            enabled = config.flyway.enabled,
            defaults = config.flyway.defaults,
            datasources = datasources,
        )
    }
}
