package site.addzero.kcloud.server.context

import org.koin.core.annotation.Single
import site.addzero.starter.flyway.FlywayConfigPlan
import site.addzero.starter.flyway.FlywayConfigSpi
import site.addzero.starter.flyway.FlywayDefaults
import site.addzero.starter.flyway.FlywayDatasourcePlan

@Single
class FlywayConfig : FlywayConfigSpi {
    override val order = 0

    override fun plan(): FlywayConfigPlan {
        return FlywayConfigPlan(
            enabled = false,
            defaults = FlywayDefaults(),
            datasources = emptyList<FlywayDatasourcePlan>(),
        )
    }
}
