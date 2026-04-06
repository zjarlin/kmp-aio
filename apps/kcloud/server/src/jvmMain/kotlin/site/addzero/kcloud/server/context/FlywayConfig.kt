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
        val datasources = listOf(
            FlywayDatasourcePlan(
                name = "sqlite",
                enabled = true,
                url = serverSqliteJdbcUrl(),
                user = "",
                password = "",
                driver = "org.sqlite.JDBC",
                flywayEnabled = false,
                defaults = FlywayDefaults(),
            ),
            FlywayDatasourcePlan(
                name = "postgres",
                enabled = false,
                url = "",
                user = "",
                password = "",
                driver = "org.postgresql.Driver",
                flywayEnabled = false,
                defaults = FlywayDefaults(),
            ),
        )
        return FlywayConfigPlan(
            enabled = false,
            defaults = FlywayDefaults(
                locations = listOf("classpath:db/migration"),
                cleanDisabled = true,
                validateOnMigrate = true,
            ),
            datasources = datasources,
        )
    }
}
