package site.addzero.notes.server.store

import org.koin.core.annotation.Single
import site.addzero.notes.server.NotesEnv
import site.addzero.notes.server.model.StorageSettingsUpdateRequest
import java.io.File

internal data class NoteStorageConfiguration(
    val sqliteDefaultPath: String,
    val sqlitePath: String,
    val postgresUrl: String,
    val postgresUser: String,
    val postgresPassword: String,
)

@Single
class NoteStorageSettingsState {
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

    internal fun current(): NoteStorageConfiguration {
        return synchronized(lock) {
            currentUnsafe()
        }
    }

    internal fun initialActiveSource(): String {
        val configured = NotesEnv.read("NOTES_SERVER_ACTIVE_SOURCE")?.trim().orEmpty()
        return normalizeSource(configured)
    }

    internal fun normalizeSource(value: String): String {
        return when (value.trim().lowercase()) {
            SOURCE_POSTGRES -> SOURCE_POSTGRES
            else -> SOURCE_SQLITE
        }
    }

    internal fun update(request: StorageSettingsUpdateRequest): NoteStorageConfiguration {
        synchronized(lock) {
            val currentPostgresUrl = postgresUrl
            sqlitePath = normalizeSqlitePathValue(request.sqlitePath)
            postgresUrl = request.postgresUrl.trim()
            postgresUser = request.postgresUser.trim().ifBlank { "postgres" }
            postgresPassword = when {
                request.postgresPassword.isNotBlank() -> request.postgresPassword
                postgresUrl == currentPostgresUrl -> postgresPassword
                else -> "postgres"
            }

            return currentUnsafe()
        }
    }

    private fun currentUnsafe(): NoteStorageConfiguration {
        return NoteStorageConfiguration(
            sqliteDefaultPath = sqliteDefaultPath,
            sqlitePath = sqlitePath,
            postgresUrl = postgresUrl,
            postgresUser = postgresUser,
            postgresPassword = postgresPassword,
        )
    }

    private fun resolveInitialSqlitePath(): String {
        val configuredPath = NotesEnv.read("NOTES_SERVER_SQLITE_PATH")?.trim().orEmpty()
        if (configuredPath.isNotBlank()) {
            return normalizeSqlitePathValue(configuredPath)
        }

        val configuredUrl = NotesEnv.read("NOTES_SERVER_SQLITE_URL")?.trim().orEmpty()
        if (configuredUrl.isNotBlank()) {
            return normalizeSqlitePathValue(configuredUrl)
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
        val parent = file.parentFile
        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }
        return file.absolutePath
    }

    private fun resolveInitialPostgresUrl(): String {
        return NotesEnv.read("NOTES_SERVER_POSTGRES_URL")?.trim().orEmpty()
    }

    private fun resolveInitialPostgresUser(): String {
        return NotesEnv.read("NOTES_SERVER_POSTGRES_USER")?.trim().orEmpty().ifBlank { "postgres" }
    }

    private fun resolveInitialPostgresPassword(): String {
        return NotesEnv.read("NOTES_SERVER_POSTGRES_PASSWORD")?.trim().orEmpty().ifBlank { "postgres" }
    }

    internal companion object {
        const val SOURCE_SQLITE = "sqlite"
        const val SOURCE_POSTGRES = "postgres"
    }
}
