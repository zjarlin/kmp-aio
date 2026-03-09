package site.addzero.notes.server.store

import site.addzero.notes.server.model.StorageSettingsPayload
import site.addzero.notes.server.model.StorageSettingsUpdateRequest
import java.io.File

class NoteStoreRegistry {
    private val lock = Any()

    private val sqliteDefaultPath = resolveDefaultSqlitePath()

    @Volatile
    private var sqlitePath = resolveInitialSqlitePath()

    @Volatile
    private var postgresUrl = resolveInitialPostgresUrl()

    @Volatile
    private var postgresUser = resolveInitialPostgresUser()

    @Volatile
    private var postgresPassword = resolveInitialPostgresPassword()

    @Volatile
    private var sqliteStore = createSqliteStore(sqlitePath)

    @Volatile
    private var postgresStore = createPostgresStore(postgresUrl, postgresUser, postgresPassword)

    @Volatile
    private var activeSource = resolveInitialActiveSource()

    init {
        if (activeSource == SOURCE_POSTGRES && !postgresStore.isReady()) {
            activeSource = SOURCE_SQLITE
        }
    }

    fun resolve(source: String): JdbcNoteStore? {
        return when (source.lowercase()) {
            "sqlite" -> sqliteStore
            "postgres" -> postgresStore
            else -> null
        }
    }

    fun readSettings(): StorageSettingsPayload {
        val pgStore = postgresStore
        return StorageSettingsPayload(
            activeSource = activeSource,
            sqlitePath = sqlitePath,
            sqliteDefaultPath = sqliteDefaultPath,
            postgresUrl = postgresUrl,
            postgresUser = postgresUser,
            postgresConfigured = postgresUrl.isNotBlank(),
            postgresAvailable = pgStore.isReady()
        )
    }

    fun updateSettings(request: StorageSettingsUpdateRequest): StorageSettingsPayload {
        synchronized(lock) {
            val oldSqliteStore = sqliteStore
            val oldPostgresStore = postgresStore

            val nextSqlitePath = normalizeSqlitePathValue(request.sqlitePath)
            val nextPostgresUrl = request.postgresUrl.trim()
            val nextPostgresUser = request.postgresUser.trim().ifBlank { "postgres" }
            val nextPostgresPassword = when {
                request.postgresPassword.isNotBlank() -> request.postgresPassword
                nextPostgresUrl == postgresUrl -> postgresPassword
                else -> "postgres"
            }

            val nextSqliteStore = createSqliteStore(nextSqlitePath)
            val nextPostgresStore = createPostgresStore(
                url = nextPostgresUrl,
                username = nextPostgresUser,
                password = nextPostgresPassword
            )

            sqlitePath = nextSqlitePath
            postgresUrl = nextPostgresUrl
            postgresUser = nextPostgresUser
            postgresPassword = nextPostgresPassword
            sqliteStore = nextSqliteStore
            postgresStore = nextPostgresStore

            val requested = request.activeSource.trim().lowercase().ifBlank { SOURCE_SQLITE }
            activeSource = when (requested) {
                SOURCE_POSTGRES -> if (nextPostgresStore.isReady()) SOURCE_POSTGRES else SOURCE_SQLITE
                else -> SOURCE_SQLITE
            }

            if (oldSqliteStore !== nextSqliteStore) {
                oldSqliteStore.close()
            }
            if (oldPostgresStore !== nextPostgresStore) {
                oldPostgresStore.close()
            }

            return readSettings()
        }
    }

    fun close() {
        synchronized(lock) {
            sqliteStore.close()
            postgresStore.close()
        }
    }

    private fun createSqliteStore(path: String): JdbcNoteStore {
        val sqliteUrl = sqlitePathToJdbcUrl(path)
        return JdbcNoteStore(
            source = SOURCE_SQLITE,
            driverClassName = "org.sqlite.JDBC",
            jdbcUrl = sqliteUrl,
            username = null,
            password = null,
            enabled = true
        )
    }

    private fun createPostgresStore(
        url: String,
        username: String,
        password: String
    ): JdbcNoteStore {
        if (url.isBlank()) {
            return JdbcNoteStore(
                source = SOURCE_POSTGRES,
                driverClassName = "org.postgresql.Driver",
                jdbcUrl = "jdbc:postgresql://127.0.0.1:5432/vibenotes",
                username = "postgres",
                password = "postgres",
                enabled = false
            )
        }

        return JdbcNoteStore(
            source = SOURCE_POSTGRES,
            driverClassName = "org.postgresql.Driver",
            jdbcUrl = url,
            username = username,
            password = password,
            enabled = true
        )
    }

    private fun resolveInitialSqlitePath(): String {
        val configuredPath = System.getenv("NOTES_SERVER_SQLITE_PATH")?.trim().orEmpty()
        if (configuredPath.isNotBlank()) {
            return normalizeSqlitePathValue(configuredPath)
        }

        val configured = System.getenv("NOTES_SERVER_SQLITE_URL")?.trim().orEmpty()
        if (configured.isNotBlank()) {
            return normalizeSqlitePathValue(configured)
        }
        return sqliteDefaultPath
    }

    private fun resolveDefaultSqlitePath(): String {
        val defaultDirectory = File(System.getProperty("user.home"), ".vibepocket/notes")
        if (!defaultDirectory.exists()) {
            defaultDirectory.mkdirs()
        }
        val dbFile = File(defaultDirectory, "vibenotes-server.db")
        return dbFile.absolutePath
    }

    private fun normalizeSqlitePathValue(value: String): String {
        val trimmed = value.trim()
        if (trimmed.isBlank()) {
            return sqliteDefaultPath
        }
        if (trimmed == ":memory:" || trimmed == "jdbc:sqlite::memory:") {
            return ":memory:"
        }

        val rawPath = if (trimmed.startsWith("jdbc:sqlite:")) {
            trimmed.removePrefix("jdbc:sqlite:")
        } else {
            trimmed
        }

        val file = File(rawPath).absoluteFile
        file.parentFile?.let { parent ->
            if (!parent.exists()) {
                parent.mkdirs()
            }
        }
        return file.absolutePath
    }

    private fun sqlitePathToJdbcUrl(path: String): String {
        if (path == ":memory:") {
            return "jdbc:sqlite::memory:"
        }
        val file = File(path).absoluteFile
        file.parentFile?.let { parent ->
            if (!parent.exists()) {
                parent.mkdirs()
            }
        }
        return "jdbc:sqlite:${file.absolutePath}"
    }

    private fun resolveInitialPostgresUrl(): String {
        return System.getenv("NOTES_SERVER_POSTGRES_URL")?.trim().orEmpty()
    }

    private fun resolveInitialPostgresUser(): String {
        return System.getenv("NOTES_SERVER_POSTGRES_USER")?.trim().orEmpty().ifBlank { "postgres" }
    }

    private fun resolveInitialPostgresPassword(): String {
        return System.getenv("NOTES_SERVER_POSTGRES_PASSWORD")?.trim().orEmpty().ifBlank { "postgres" }
    }

    private fun resolveInitialActiveSource(): String {
        val configured = System.getenv("NOTES_SERVER_ACTIVE_SOURCE")?.trim().orEmpty().lowercase()
        return when (configured) {
            SOURCE_POSTGRES -> SOURCE_POSTGRES
            else -> SOURCE_SQLITE
        }
    }

    private companion object {
        private const val SOURCE_SQLITE = "sqlite"
        private const val SOURCE_POSTGRES = "postgres"
    }
}
