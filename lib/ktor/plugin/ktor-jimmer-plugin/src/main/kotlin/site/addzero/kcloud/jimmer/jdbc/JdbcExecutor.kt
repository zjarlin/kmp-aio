package site.addzero.kcloud.jimmer.jdbc

import java.sql.Connection
import java.sql.ResultSet

interface JdbcExecutor {
    fun <T> withTransaction(
        block: (Connection) -> T,
    ): T

    fun queryIds(
        connection: Connection,
        sql: String,
        vararg args: Any?,
    ): List<Long>

    fun queryCount(
        connection: Connection,
        sql: String,
        vararg args: Any?,
    ): Long

    fun <T> query(
        connection: Connection,
        sql: String,
        vararg args: Any?,
        mapper: (ResultSet) -> T,
    ): List<T>

    fun queryForList(
        connection: Connection,
        sql: String,
        vararg args: Any?,
    ): List<Map<String, Any?>>

    fun update(
        connection: Connection,
        sql: String,
        vararg args: Any?,
    ): Int

    fun batchUpdate(
        connection: Connection,
        sql: String,
        batchParams: List<List<Any?>>,
    ): IntArray

    fun insertAndReturnId(
        connection: Connection,
        sql: String,
        vararg args: Any?,
    ): Long
}
