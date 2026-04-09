package site.addzero.kcloud.jimmer.di

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import site.addzero.util.db.SqlExecutor
import site.addzero.kcloud.jimmer.spi.DatasourceProperties

class DataSourceJdbcExecutorTest {
    @Test
    fun shouldCommitTransactionAndSupportCommonJdbcOperations() {
        val databasePath = Files.createTempFile("jdbc-executor-commit-", ".db")
        val executor = createExecutor(databasePath.toString())
        try {
            executor.withTransaction { connection ->
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
                val firstId = executor.insertAndReturnId(connection, "INSERT INTO sample(name) VALUES (?)", "alpha")
                val secondId = executor.insertAndReturnId(connection, "INSERT INTO sample(name) VALUES (?)", "beta")

                assertEquals(
                    listOf(firstId, secondId),
                    executor.queryIds(connection, "SELECT id FROM sample ORDER BY id"),
                )
                assertEquals(2L, executor.queryCount(connection, "SELECT COUNT(1) FROM sample"))
                assertEquals(1, executor.update(connection, "UPDATE sample SET name = ? WHERE id = ?", "gamma", firstId))
            }

            executor.withTransaction { connection ->
                assertEquals(2L, executor.queryCount(connection, "SELECT COUNT(1) FROM sample"))
                assertEquals(listOf(1L, 2L), executor.queryIds(connection, "SELECT id FROM sample ORDER BY id"))
            }
        } finally {
            Files.deleteIfExists(databasePath)
        }
    }

    @Test
    fun shouldRollbackTransactionWhenBlockFails() {
        val databasePath = Files.createTempFile("jdbc-executor-rollback-", ".db")
        val executor = createExecutor(databasePath.toString())
        try {
            executor.withTransaction { connection ->
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
                executor.withTransaction { connection ->
                    executor.insertAndReturnId(connection, "INSERT INTO sample(name) VALUES (?)", "alpha")
                    throw IllegalStateException("rollback")
                }
            }

            executor.withTransaction { connection ->
                assertEquals(0L, executor.queryCount(connection, "SELECT COUNT(1) FROM sample"))
            }
        } finally {
            Files.deleteIfExists(databasePath)
        }
    }

    private fun createExecutor(
        databasePath: String,
    ): SqlExecutor {
        val dataSource = DatasourceProperties(
            name = "sqlite-test",
            url = "jdbc:sqlite:$databasePath",
            driverClassName = "",
            user = "",
            password = "",
        ).toDatasource()
        return SqlExecutor(dataSource)
    }
}
