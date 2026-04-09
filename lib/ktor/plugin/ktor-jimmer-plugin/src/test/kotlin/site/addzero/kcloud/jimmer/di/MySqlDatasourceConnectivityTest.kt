package site.addzero.kcloud.jimmer.di

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import site.addzero.kcloud.jimmer.spi.DatasourceProperties

class SqliteDatasourceConnectivityTest {
    @Test
    fun shouldConnectToSqliteDatasource() {
        val databaseFile = File(System.getProperty("java.io.tmpdir"), "ktor-jimmer-connectivity.sqlite")
        if (databaseFile.exists()) {
            databaseFile.delete()
        }
        val dataSource = DatasourceProperties(
            name = "sqlite-test",
            url = "jdbc:sqlite:${databaseFile.absolutePath}",
            driverClassName = "",
        ).toDatasource()

        try {
            dataSource.connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeQuery("select 1").use { resultSet ->
                        require(resultSet.next()) { "SQLite 连通性校验未返回结果" }
                        assertEquals(1, resultSet.getInt(1))
                    }
                }
            }
        } finally {
            (dataSource as? AutoCloseable)?.close()
            databaseFile.delete()
        }
    }
}
