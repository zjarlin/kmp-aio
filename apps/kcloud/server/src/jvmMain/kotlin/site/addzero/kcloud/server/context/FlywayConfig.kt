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
        val datasources =
            listOf(
                FlywayDatasourcePlan(
                    name = "mysql",
                    enabled = true,
                    url = serverMysqlJdbcUrl(),
                    user = KCLOUD_SERVER_MYSQL_USER,
                    password = KCLOUD_SERVER_MYSQL_PASSWORD,
                    driver = "com.mysql.cj.jdbc.Driver",
                    flywayEnabled = true,
                    defaults = FlywayDefaults(),
                ),
            )
        return FlywayConfigPlan(
            enabled = true,
            defaults = FlywayDefaults(
                locations = listOf("classpath:db/migration/mysql"),
                baselineOnMigrate = true,
                baselineVersion = "0",
                cleanDisabled = true,
                validateOnMigrate = true,
            ),
            datasources = datasources,
        )
    }
}
