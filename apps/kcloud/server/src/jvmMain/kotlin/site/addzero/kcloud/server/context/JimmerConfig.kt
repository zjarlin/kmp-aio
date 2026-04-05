package site.addzero.kcloud.server.context

import org.koin.core.annotation.Single
import site.addzero.configcenter.ConfigCenterEnv
import site.addzero.kcloud.jimmer.spi.DatasourceProperties
import site.addzero.kcloud.jimmer.spi.DatasourcePropertiesSpi

@Single
class JimmerConfig(
    private val env: ConfigCenterEnv,
) : DatasourcePropertiesSpi {
    override fun datasources(): List<DatasourceProperties> {
        val datasourceRoot = env.path("datasources")
        val datasourceNames = datasourceRoot.keys().sorted()
        val enabledNames = datasourceNames.filter { name ->
            datasourceRoot.child(name).boolean("enabled", false) == true
        }
        val defaultName = when {
            "sqlite" in enabledNames -> "sqlite"
            "postgres" in enabledNames -> "postgres"
            else -> enabledNames.firstOrNull()
        }

        return datasourceNames.map { name ->
            val datasourceEnv = datasourceRoot.child(name)
            DatasourceProperties(
                name = name,
                enabled = datasourceEnv.boolean("enabled", false) == true,
                default = name == defaultName,
                url = datasourceEnv.string("url").orEmpty(),
                driverClassName = datasourceEnv.string("driver").orEmpty(),
                user = datasourceEnv.string("user").orEmpty(),
                password = datasourceEnv.string("password").orEmpty(),
            )
        }
    }
}
