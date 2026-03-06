@file:Suppress("SqlNoDataSourceInspection", "SqlResolve")

package site.addzero.notes.server.store

import site.addzero.notes.server.model.NotePayload
import java.sql.Connection
import java.sql.DriverManager

class JdbcNoteStore(
    private val source: String,
    private val driverClassName: String,
    private val jdbcUrl: String,
    private val username: String?,
    private val password: String?,
    enabled: Boolean
) {
    private var ready = false
    private var initError = ""

    init {
        if (!enabled) {
            ready = false
            initError = "数据源被禁用"
        } else {
            runCatching {
                Class.forName(driverClassName)
                initializeSchema()
                ready = true
            }.onFailure { throwable ->
                ready = false
                initError = throwable.message.orEmpty().ifBlank { "初始化失败" }
            }
        }
    }

    fun healthMessage(): String {
        return if (ready) {
            "ok"
        } else {
            initError
        }
    }

    fun isReady(): Boolean {
        return ready
    }

    fun listNotes(): List<NotePayload> {
        check(ready) { "数据源不可用：$source - $initError" }
        return openConnection().use { connection ->
            connection.prepareStatement(SELECT_SQL).use { statement ->
                val rs = statement.executeQuery()
                val notes = mutableListOf<NotePayload>()
                while (rs.next()) {
                    notes += NotePayload(
                        id = rs.getString("id"),
                        path = rs.getString("path"),
                        title = rs.getString("title"),
                        markdown = rs.getString("markdown"),
                        pinned = rs.getBoolean("pinned"),
                        version = rs.getLong("version")
                    )
                }
                notes
            }
        }
    }

    fun upsertNote(note: NotePayload): NotePayload {
        check(ready) { "数据源不可用：$source - $initError" }
        openConnection().use { connection ->
            connection.prepareStatement(UPSERT_SQL).use { statement ->
                statement.setString(1, note.id)
                statement.setString(2, note.path)
                statement.setString(3, note.title)
                statement.setString(4, note.markdown)
                statement.setBoolean(5, note.pinned)
                statement.setLong(6, note.version)
                statement.executeUpdate()
            }
        }
        return note
    }

    fun deleteNote(noteId: String) {
        if (!ready) {
            return
        }
        openConnection().use { connection ->
            connection.prepareStatement(DELETE_SQL).use { statement ->
                statement.setString(1, noteId)
                statement.executeUpdate()
            }
        }
    }

    private fun initializeSchema() {
        openConnection().use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(CREATE_TABLE_SQL)
            }
        }
    }

    private fun openConnection(): Connection {
        return if (username == null) {
            DriverManager.getConnection(jdbcUrl)
        } else {
            DriverManager.getConnection(jdbcUrl, username, password)
        }
    }

    private companion object {
        private const val CREATE_TABLE_SQL = """
            create table if not exists notes (
                id varchar(64) primary key,
                path text not null unique,
                title text not null,
                markdown text not null,
                pinned boolean not null default false,
                version bigint not null default 1
            )
        """

        private const val SELECT_SQL = """
            select id, path, title, markdown, pinned, version
            from notes
            order by pinned desc, version desc, title
        """

        private const val UPSERT_SQL = """
            insert into notes (id, path, title, markdown, pinned, version)
            values (?, ?, ?, ?, ?, ?)
            on conflict(id) do update set
                path = excluded.path,
                title = excluded.title,
                markdown = excluded.markdown,
                pinned = excluded.pinned,
                version = excluded.version
        """

        private const val DELETE_SQL = """
            delete from notes where id = ?
        """
    }
}
