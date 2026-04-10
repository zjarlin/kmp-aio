package site.addzero.kcloud.jimmer.di

import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import site.addzero.kcloud.jimmer.spi.DatasourceProperties

class DataSourceJdbcExecutorTest {
    @Test
    fun shouldCommitTransactionAndSupportCommonJdbcOperations() {
        val databasePath = Files.createTempFile("jdbc-executor-commit-", ".db")
        val sqlClient = createSqlClient(databasePath.toString())
        try {
            sqlClient.withTransaction { connection ->
                connection.createStatement().use { statement ->
                    statement.executeUpdate(
                        """
                        CREATE TABLE sample (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL
                        )
                        """.trimIndent(),
                    )
                }
                val firstId = sqlClient.insertAndReturnId(connection, "INSERT INTO sample(name) VALUES (?)", "alpha")
                val secondId = sqlClient.insertAndReturnId(connection, "INSERT INTO sample(name) VALUES (?)", "beta")

                assertEquals(
                    listOf(firstId, secondId),
                    sqlClient.queryIds(connection, "SELECT id FROM sample ORDER BY id"),
                )
                assertEquals(2L, sqlClient.queryCount(connection, "SELECT COUNT(1) FROM sample"))
                assertEquals(1, sqlClient.update(connection, "UPDATE sample SET name = ? WHERE id = ?", "gamma", firstId))
            }

            sqlClient.withTransaction { connection ->
                assertEquals(2L, sqlClient.queryCount(connection, "SELECT COUNT(1) FROM sample"))
                assertEquals(listOf(1L, 2L), sqlClient.queryIds(connection, "SELECT id FROM sample ORDER BY id"))
            }
        } finally {
            Files.deleteIfExists(databasePath)
        }
    }

    @Test
    fun shouldRollbackTransactionWhenBlockFails() {
        val databasePath = Files.createTempFile("jdbc-executor-rollback-", ".db")
        val sqlClient = createSqlClient(databasePath.toString())
        try {
            sqlClient.withTransaction { connection ->
                connection.createStatement().use { statement ->
                    statement.executeUpdate(
                        """
                        CREATE TABLE sample (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL
                        )
                        """.trimIndent(),
                    )
                }
            }

            assertFailsWith<IllegalStateException> {
                sqlClient.withTransaction { connection ->
                    sqlClient.insertAndReturnId(connection, "INSERT INTO sample(name) VALUES (?)", "alpha")
                    throw IllegalStateException("rollback")
                }
            }

            sqlClient.withTransaction { connection ->
                assertEquals(0L, sqlClient.queryCount(connection, "SELECT COUNT(1) FROM sample"))
            }
        } finally {
            Files.deleteIfExists(databasePath)
        }
    }

    private fun createSqlClient(
        databasePath: String,
    ) = DatasourceProperties(
        name = "sqlite-test",
        url = "jdbc:sqlite:$databasePath",
        driverClassName = "",
        user = "",
        password = "",
    ).toDatasource().toRawKSqlClient(SQLiteDialect())
}
