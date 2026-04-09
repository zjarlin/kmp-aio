package site.addzero.kcloud.jimmer.jdbc

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.sql.DataSource
import org.koin.core.annotation.Single

@Single
class DataSourceJdbcExecutor(
    private val dataSource: DataSource,
) : JdbcExecutor {
    override fun <T> withTransaction(
        block: (Connection) -> T,
    ): T {
        dataSource.connection.use { connection ->
            val originalAutoCommit = connection.autoCommit
            connection.autoCommit = false
            return try {
                val result = block(connection)
                connection.commit()
                result
            } catch (throwable: Throwable) {
                connection.rollback()
                throw throwable
            } finally {
                connection.autoCommit = originalAutoCommit
            }
        }
    }

    override fun queryIds(
        connection: Connection,
        sql: String,
        vararg args: Any?,
    ): List<Long> {
        connection.prepareStatement(sql).use { statement ->
            bindArgs(statement, args.asList())
            statement.executeQuery().use { resultSet ->
                val rows = mutableListOf<Long>()
                while (resultSet.next()) {
                    rows += resultSet.getLong(1)
                }
                return rows
            }
        }
    }

    override fun queryCount(
        connection: Connection,
        sql: String,
        vararg args: Any?,
    ): Long {
        connection.prepareStatement(sql).use { statement ->
            bindArgs(statement, args.asList())
            statement.executeQuery().use { resultSet ->
                return if (resultSet.next()) {
                    resultSet.getLong(1)
                } else {
                    0L
                }
            }
        }
    }

    override fun <T> query(
        connection: Connection,
        sql: String,
        vararg args: Any?,
        mapper: (ResultSet) -> T,
    ): List<T> {
        connection.prepareStatement(sql).use { statement ->
            bindArgs(statement, args.asList())
            statement.executeQuery().use { resultSet ->
                val rows = mutableListOf<T>()
                while (resultSet.next()) {
                    rows += mapper(resultSet)
                }
                return rows
            }
        }
    }

    override fun queryForList(
        connection: Connection,
        sql: String,
        vararg args: Any?,
    ): List<Map<String, Any?>> {
        return query(connection, sql, *args) { resultSet ->
            val meta = resultSet.metaData
            val row = linkedMapOf<String, Any?>()
            for (index in 1..meta.columnCount) {
                row[meta.getColumnLabel(index)] = resultSet.getObject(index)
            }
            row
        }
    }

    override fun update(
        connection: Connection,
        sql: String,
        vararg args: Any?,
    ): Int {
        connection.prepareStatement(sql).use { statement ->
            bindArgs(statement, args.asList())
            return statement.executeUpdate()
        }
    }

    override fun batchUpdate(
        connection: Connection,
        sql: String,
        batchParams: List<List<Any?>>,
    ): IntArray {
        if (batchParams.isEmpty()) {
            return intArrayOf()
        }
        connection.prepareStatement(sql).use { statement ->
            batchParams.forEach { args ->
                statement.clearParameters()
                bindArgs(statement, args)
                statement.addBatch()
            }
            return statement.executeBatch()
        }
    }

    override fun insertAndReturnId(
        connection: Connection,
        sql: String,
        vararg args: Any?,
    ): Long {
        connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS).use { statement ->
            bindArgs(statement, args.asList())
            statement.executeUpdate()
            statement.generatedKeys.use { generatedKeys ->
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1)
                }
            }
        }
        error("Insert did not return generated id")
    }

    private fun bindArgs(
        statement: PreparedStatement,
        args: List<Any?>,
    ) {
        args.forEachIndexed { index, arg ->
            statement.setObject(index + 1, arg)
        }
    }
}
