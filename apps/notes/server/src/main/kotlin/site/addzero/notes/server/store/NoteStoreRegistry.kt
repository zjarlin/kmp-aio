package site.addzero.notes.server.store

import java.io.File

class NoteStoreRegistry {
    private val sqliteStore = createSqliteStore()
    private val postgresStore = createPostgresStore()

    fun resolve(source: String): JdbcNoteStore? {
        return when (source.lowercase()) {
            "sqlite" -> sqliteStore
            "postgres" -> postgresStore
            else -> null
        }
    }

    private fun createSqliteStore(): JdbcNoteStore {
        val sqliteUrl = resolveSqliteUrl()
        return JdbcNoteStore(
            source = "sqlite",
            driverClassName = "org.sqlite.JDBC",
            jdbcUrl = sqliteUrl,
            username = null,
            password = null,
            enabled = true
        )
    }

    private fun createPostgresStore(): JdbcNoteStore {
        val url = System.getenv("NOTES_SERVER_POSTGRES_URL")?.trim().orEmpty()
        if (url.isBlank()) {
            return JdbcNoteStore(
                source = "postgres",
                driverClassName = "org.postgresql.Driver",
                jdbcUrl = "jdbc:postgresql://127.0.0.1:5432/vibenotes",
                username = "postgres",
                password = "postgres",
                enabled = false
            )
        }

        val username = System.getenv("NOTES_SERVER_POSTGRES_USER")?.trim().orEmpty()
            .ifBlank { "postgres" }
        val password = System.getenv("NOTES_SERVER_POSTGRES_PASSWORD")?.trim().orEmpty()
            .ifBlank { "postgres" }

        return JdbcNoteStore(
            source = "postgres",
            driverClassName = "org.postgresql.Driver",
            jdbcUrl = url,
            username = username,
            password = password,
            enabled = true
        )
    }

    private fun resolveSqliteUrl(): String {
        val configured = System.getenv("NOTES_SERVER_SQLITE_URL")?.trim().orEmpty()
        val asUrl = if (configured.isBlank()) {
            "jdbc:sqlite:${File("apps/notes/server/build/vibenotes-server.db").absolutePath}"
        } else {
            configured
        }
        if (asUrl.startsWith("jdbc:")) {
            return asUrl
        }
        return "jdbc:sqlite:$asUrl"
    }
}
