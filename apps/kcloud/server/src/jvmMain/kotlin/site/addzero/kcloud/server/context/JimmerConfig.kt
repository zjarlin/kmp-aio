package site.addzero.kcloud.server.context

import org.koin.core.annotation.Single
import site.addzero.kcloud.jimmer.spi.DatasourcePropertiesSpi

@Single
class SqlLiteDatasourcePropertiesSpiImpl : DatasourcePropertiesSpi {
    override val url: String
        get() = "jdbc:sqlite:./config-center.sqlite"
    override val driverClassName: String
        get() = "org.sqlite.JDBC"
    override val user: String
        get() = ""
    override val password: String
        get() = ""
}


@Single
class PgDatasourcePropertiesSpiImpl : DatasourcePropertiesSpi {
    override val url: String
        get() = "jdbc:sqlite:./config-center.sqlite"
    override val driverClassName: String
        get() = "org.sqlite.JDBC"
    override val user: String
        get() = ""
    override val password: String
        get() = ""
}


