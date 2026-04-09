package site.addzero.kcloud.server.context

import org.koin.core.annotation.Single
import site.addzero.kcloud.jimmer.spi.DatasourceProperties
import site.addzero.kcloud.jimmer.spi.DatasourcePropertiesSpi

@Single
class JimmerConfig : DatasourcePropertiesSpi {
    override fun datasources(): List<DatasourceProperties> {
        return when (resolveKCloudServerDbMode()) {
            KCloudServerDbMode.Sqlite ->
                listOf(
                    DatasourceProperties(
                        name = KCLOUD_SERVER_DB_MODE_SQLITE,
                        enabled = true,
                        default = true,
                        url = serverSqliteJdbcUrl(),
                        driverClassName = "org.sqlite.JDBC",
                    ),
                )

            KCloudServerDbMode.Mysql ->
                listOf(
                    DatasourceProperties(
                        name = KCLOUD_SERVER_DB_MODE_MYSQL,
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
}
