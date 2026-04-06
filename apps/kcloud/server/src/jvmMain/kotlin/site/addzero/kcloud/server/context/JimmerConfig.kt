package site.addzero.kcloud.server.context

import org.koin.core.annotation.Single
import site.addzero.kcloud.jimmer.spi.DatasourceProperties
import site.addzero.kcloud.jimmer.spi.DatasourcePropertiesSpi

@Single
class JimmerConfig : DatasourcePropertiesSpi {
    override fun datasources(): List<DatasourceProperties> {
        return listOf(
            DatasourceProperties(
                name = "sqlite",
                enabled = true,
                default = true,
                url = serverSqliteJdbcUrl(),
                driverClassName = "org.sqlite.JDBC",
                user = "",
                password = "",
            ),
            DatasourceProperties(
                name = "postgres",
                enabled = false,
                default = false,
                url = "",
                driverClassName = "org.postgresql.Driver",
                user = "",
                password = "",
            ),
        )
    }
}
