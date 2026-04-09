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
        if (resolveKCloudServerDbMode() == KCloudServerDbMode.Sqlite) {
            return FlywayConfigPlan(enabled = false)
        }

        val datasources =
            listOf(
                FlywayDatasourcePlan(
                    name = KCLOUD_SERVER_DB_MODE_MYSQL,
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
                // 兼容早期单体阶段已入库、但已不再随当前代码分发的版本化迁移。
                ignoreMigrationPatterns = listOf("versioned:missing"),
            ),
            datasources = datasources,
        )
    }
}
