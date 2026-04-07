package site.addzero.kcloud.plugins.hostconfig.service

import java.sql.PreparedStatement
import javax.sql.DataSource
import org.koin.core.annotation.Single

/**
 * 宿主配置插件自己的 JDBC 辅助层。
 *
 * 这里集中收口排序调整、级联清理、备份导出这类更适合直接写 SQL 的逻辑，
 * 避免在服务里散落大量重复的 `Connection` 样板代码。
 */
@Single
class HostConfigJdbc(
    private val dataSource: DataSource,
) {
    fun update(
        sql: String,
        vararg args: Any?,
    ): Int {
        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                bindArgs(statement, args.asList())
                return statement.executeUpdate()
            }
        }
    }

    fun <T> queryList(
        sql: String,
        vararg args: Any?,
        mapper: (java.sql.ResultSet) -> T,
    ): List<T> {
        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                bindArgs(statement, args.asList())
                statement.executeQuery().use { rs ->
                    val rows = mutableListOf<T>()
                    while (rs.next()) {
                        rows += mapper(rs)
                    }
                    return rows
                }
            }
        }
    }

    fun queryIds(
        sql: String,
        vararg args: Any?,
    ): MutableList<Long> {
        return queryList(sql, *args) { rs -> rs.getLong(1) }.toMutableList()
    }

    fun queryCount(
        sql: String,
        vararg args: Any?,
    ): Long {
        return queryList(sql, *args) { rs -> rs.getLong(1) }.firstOrNull() ?: 0L
    }

    fun queryRows(
        sql: String,
        vararg args: Any?,
    ): List<Map<String, Any?>> {
        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                bindArgs(statement, args.asList())
                statement.executeQuery().use { rs ->
                    val meta = rs.metaData
                    val rows = mutableListOf<Map<String, Any?>>()
                    while (rs.next()) {
                        val row = linkedMapOf<String, Any?>()
                        for (index in 1..meta.columnCount) {
                            row[meta.getColumnLabel(index)] = rs.getObject(index)
                        }
                        rows += row
                    }
                    return rows
                }
            }
        }
    }

    fun batchUpdateSort(
        sql: String,
        orderedIds: List<Long>,
        updatedAt: Long,
    ) {
        if (orderedIds.isEmpty()) {
            return
        }
        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                orderedIds.forEachIndexed { index, id ->
                    statement.setInt(1, index)
                    statement.setLong(2, updatedAt)
                    statement.setLong(3, id)
                    statement.addBatch()
                }
                statement.executeBatch()
            }
        }
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
