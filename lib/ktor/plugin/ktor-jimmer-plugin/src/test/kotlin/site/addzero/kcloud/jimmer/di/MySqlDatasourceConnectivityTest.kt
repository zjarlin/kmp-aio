package site.addzero.kcloud.jimmer.di

import org.junit.jupiter.api.Assumptions.assumeTrue
import kotlin.test.Test
import kotlin.test.assertEquals
import site.addzero.kcloud.jimmer.spi.DatasourceProperties

class MySqlDatasourceConnectivityTest {
    @Test
    fun shouldConnectToConfiguredMysqlServer() {
        val jdbcUrl = readConfigValue(
            propertyName = MYSQL_URL_PROPERTY,
            envName = MYSQL_URL_ENV,
        )
        assumeTrue(jdbcUrl.isNotBlank(), "未配置 MySQL 测试连接信息，跳过远程连通性验证")

        val dataSource = DatasourceProperties(
            name = "mysql-test",
            url = jdbcUrl,
            driverClassName = "",
            user = readConfigValue(
                propertyName = MYSQL_USER_PROPERTY,
                envName = MYSQL_USER_ENV,
            ),
            password = readConfigValue(
                propertyName = MYSQL_PASSWORD_PROPERTY,
                envName = MYSQL_PASSWORD_ENV,
            ),
        ).toDatasource()

        try {
            dataSource.connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeQuery("select 1").use { resultSet ->
                        require(resultSet.next()) { "MySQL 连通性校验未返回结果" }
                        assertEquals(1, resultSet.getInt(1))
                    }
                }
            }
        } finally {
            (dataSource as? AutoCloseable)?.close()
        }
    }

    companion object {
        const val MYSQL_URL_PROPERTY: String = "ktor.jimmer.test.mysql.url"
        const val MYSQL_USER_PROPERTY: String = "ktor.jimmer.test.mysql.user"
        const val MYSQL_PASSWORD_PROPERTY: String = "ktor.jimmer.test.mysql.password"
        const val MYSQL_URL_ENV: String = "KTOR_JIMMER_TEST_MYSQL_URL"
        const val MYSQL_USER_ENV: String = "KTOR_JIMMER_TEST_MYSQL_USER"
        const val MYSQL_PASSWORD_ENV: String = "KTOR_JIMMER_TEST_MYSQL_PASSWORD"
    }
}

private fun readConfigValue(propertyName: String, envName: String): String {
    return System.getProperty(propertyName)
        ?.takeIf(String::isNotBlank)
        ?: System.getenv(envName).orEmpty()
}
