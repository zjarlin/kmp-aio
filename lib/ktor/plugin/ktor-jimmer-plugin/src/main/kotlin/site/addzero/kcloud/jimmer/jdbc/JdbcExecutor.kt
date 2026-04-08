package site.addzero.kcloud.jimmer.jdbc

import java.sql.Connection

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

    fun update(
        connection: Connection,
        sql: String,
        vararg args: Any?,
    ): Int

    fun insertAndReturnId(
        connection: Connection,
        sql: String,
        vararg args: Any?,
    ): Long
}
