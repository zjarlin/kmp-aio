package site.addzero.notes.server.store

import org.koin.core.annotation.Single

@Single
class JdbcNoteStoreFactory {
    fun createSqlite(path: String): JdbcNoteStore {
        return JdbcNoteStore(
            source = NoteStorageSettingsState.SOURCE_SQLITE,
            driverClassName = "org.sqlite.JDBC",
            jdbcUrl = sqlitePathToJdbcUrl(path),
            username = null,
            password = null,
            enabled = true,
        )
    }

    fun createPostgres(
        url: String,
        username: String,
        password: String,
    ): JdbcNoteStore {
        if (url.isBlank()) {
            return JdbcNoteStore(
                source = NoteStorageSettingsState.SOURCE_POSTGRES,
                driverClassName = "org.postgresql.Driver",
                jdbcUrl = "jdbc:postgresql://127.0.0.1:5432/vibenotes",
                username = "postgres",
                password = "postgres",
                enabled = false,
            )
        }

        return JdbcNoteStore(
            source = NoteStorageSettingsState.SOURCE_POSTGRES,
            driverClassName = "org.postgresql.Driver",
            jdbcUrl = url,
            username = username,
            password = password,
            enabled = true,
        )
    }

    private fun sqlitePathToJdbcUrl(path: String): String {
        if (path == ":memory:") {
            return "jdbc:sqlite::memory:"
        }

        return "jdbc:sqlite:$path"
    }
}
