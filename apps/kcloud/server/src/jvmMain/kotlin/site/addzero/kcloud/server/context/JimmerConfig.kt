package site.addzero.kcloud.server.context

import org.koin.core.annotation.Single
import site.addzero.kcloud.jimmer.spi.DatasourceProperties
import site.addzero.kcloud.jimmer.spi.DatasourcePropertiesSpi

@Single
class JimmerConfig : DatasourcePropertiesSpi {
    override fun datasources(): List<DatasourceProperties> {
        return listOf(
            DatasourceProperties(
                name = "mysql",
                enabled = true,
                default = true,
                url = serverMysqlJdbcUrl(schema = KCLOUD_SERVER_MYSQL_SCHEMA),
                driverClassName = "com.mysql.cj.jdbc.Driver",
                user = KCLOUD_SERVER_MYSQL_USER,
                password = KCLOUD_SERVER_MYSQL_PASSWORD,
            ),
        )
    }
}
