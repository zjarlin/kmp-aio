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
                user = "root",
                password = "test123456",
            ),
            DatasourceProperties(
                name = "sqlite",
                enabled = false,
                default = false,
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

private const val KCLOUD_SERVER_MYSQL_SCHEMA = "okmy_dics"

private fun serverMysqlJdbcUrl(
    schema: String,
): String {
    return "jdbc:mysql://192.168.31.133:3306/$schema?createDatabaseIfNotExist=true"
}
