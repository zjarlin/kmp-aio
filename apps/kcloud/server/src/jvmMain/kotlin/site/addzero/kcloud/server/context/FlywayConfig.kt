package site.addzero.kcloud.server.context

import org.koin.core.annotation.Single
import site.addzero.configcenter.ConfigCenterEnv
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
                defaults = FlywayDefaults.fromDatasourceEnv(env, name),
            )
        }
        return FlywayConfigPlan(
            enabled = env.path("flyway").boolean("enabled", true) != false,
            defaults = FlywayDefaults.fromGlobalEnv(env),
            datasources = datasources,
        )
    }
}
