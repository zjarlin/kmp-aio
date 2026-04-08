package site.addzero.kcloud.plugins.codegencontext.codegen_context.service

import java.sql.Connection
import java.sql.PreparedStatement
import javax.sql.DataSource
import org.koin.core.annotation.Single

@Single
class CodegenContextJdbc(
    private val dataSource: DataSource,
) {
    fun <T> withTransaction(
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

    fun queryIds(
        connection: Connection,
        sql: String,
        vararg args: Any?,
    ): List<Long> {
        connection.prepareStatement(sql).use { statement ->
            bindArgs(statement, args.asList())
            statement.executeQuery().use { rs ->
                val rows = mutableListOf<Long>()
                while (rs.next()) {
                    rows += rs.getLong(1)
                }
                return rows
            }
        }
    }

    fun queryCount(
        connection: Connection,
        sql: String,
        vararg args: Any?,
    ): Long {
        connection.prepareStatement(sql).use { statement ->
            bindArgs(statement, args.asList())
            statement.executeQuery().use { rs ->
                return if (rs.next()) rs.getLong(1) else 0L
            }
        }
    }

    fun update(
        connection: Connection,
        sql: String,
        vararg args: Any?,
    ): Int {
        connection.prepareStatement(sql).use { statement ->
            bindArgs(statement, args.asList())
            return statement.executeUpdate()
        }
    }

    fun insertAndReturnId(
        connection: Connection,
        sql: String,
        vararg args: Any?,
    ): Long {
        connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS).use { statement ->
            bindArgs(statement, args.asList())
            statement.executeUpdate()
            statement.generatedKeys.use { keys ->
                if (keys.next()) {
                    return keys.getLong(1)
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
