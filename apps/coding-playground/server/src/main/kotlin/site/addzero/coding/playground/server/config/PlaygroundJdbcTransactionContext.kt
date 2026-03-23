package site.addzero.coding.playground.server.config

import java.sql.Connection
import javax.sql.DataSource

/**
 * 让 Jimmer 与原生 JDBC 在同一个线程事务里共享连接。
 */
object PlaygroundJdbcTransactionContext {
    private val currentConnection = ThreadLocal<Connection?>()

    fun connectionOrNull(): Connection? = currentConnection.get()

    fun <T> withTransaction(dataSource: DataSource, block: () -> T): T {
        val existing = currentConnection.get()
        if (existing != null) {
            return block()
        }
        return dataSource.connection.use { connection ->
            connection.autoCommit = false
            currentConnection.set(connection)
            try {
                val result = block()
                connection.commit()
                result
            } catch (throwable: Throwable) {
                connection.rollback()
                throw throwable
            } finally {
                currentConnection.remove()
                connection.autoCommit = true
            }
        }
    }
}
