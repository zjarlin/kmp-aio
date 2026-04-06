package site.addzero.kcloud.server.context

import org.koin.core.annotation.Single
import site.addzero.kcloud.jimmer.spi.DatasourceProperties
import site.addzero.kcloud.jimmer.spi.DatasourcePropertiesSpi

@Single
class JimmerConfig(
    private val config: ServerContextConfig,
) : DatasourcePropertiesSpi {
    override fun datasources(): List<DatasourceProperties> {
        return config.datasources.map { datasource ->
            DatasourceProperties(
                name = datasource.name,
                enabled = datasource.enabled,
                default = datasource.default,
                url = datasource.url,
                driverClassName = datasource.driverClassName,
                user = datasource.user,
                password = datasource.password,
            )
        }
    }
}
