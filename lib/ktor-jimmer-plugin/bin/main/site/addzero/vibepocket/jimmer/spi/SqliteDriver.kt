package site.addzero.vibepocket.jimmer.spi

import org.babyfish.jimmer.sql.dialect.Dialect
import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import org.koin.core.annotation.Single
import org.sqlite.SQLiteDataSource
import site.addzero.vibepocket.jimmer.di.DatasourceProperties
import javax.sql.DataSource

@Single
class SqliteDriver : DatabaseDriverSpi {

    override fun supports(driver: String): Boolean =
        driver.contains("sqlite", ignoreCase = true)

    override fun createDataSource(props: DatasourceProperties): DataSource =
        SQLiteDataSource().apply { url = props.url }

    override fun dialect(): Dialect = SQLiteDialect()

    override fun schemaFile(): String = "schema-sqlite.sql"
}
