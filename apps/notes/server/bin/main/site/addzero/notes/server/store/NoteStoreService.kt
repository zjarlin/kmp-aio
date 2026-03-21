package site.addzero.notes.server.store

import org.koin.core.annotation.Single
import site.addzero.notes.server.model.StorageSettingsPayload
import site.addzero.notes.server.model.StorageSettingsUpdateRequest

@Single
class NoteStoreService(
    private val settingsState: NoteStorageSettingsState,
    private val storeFactory: JdbcNoteStoreFactory,
) : AutoCloseable {
    private val lock = Any()

    @Volatile
    private var sqliteStore = createSqliteStore(settingsState.current())

    @Volatile
    private var postgresStore = createPostgresStore(settingsState.current())

    @Volatile
    private var activeSource = resolveInitialActiveSource()

    init {
        if (activeSource == NoteStorageSettingsState.SOURCE_POSTGRES && !postgresStore.isReady()) {
            activeSource = NoteStorageSettingsState.SOURCE_SQLITE
        }
    }

    fun resolve(source: String): JdbcNoteStore? {
        return when (source.lowercase()) {
            NoteStorageSettingsState.SOURCE_SQLITE -> sqliteStore
            NoteStorageSettingsState.SOURCE_POSTGRES -> postgresStore
            else -> null
        }
    }

    fun readSettings(): StorageSettingsPayload {
        val configuration = settingsState.current()
        val currentPostgresStore = postgresStore
        return StorageSettingsPayload(
            activeSource = activeSource,
            sqlitePath = configuration.sqlitePath,
            sqliteDefaultPath = configuration.sqliteDefaultPath,
            postgresUrl = configuration.postgresUrl,
            postgresUser = configuration.postgresUser,
            postgresConfigured = configuration.postgresUrl.isNotBlank(),
            postgresAvailable = currentPostgresStore.isReady(),
        )
    }

    fun updateSettings(request: StorageSettingsUpdateRequest): StorageSettingsPayload {
        synchronized(lock) {
            val nextConfiguration = settingsState.update(request)
            val nextSqliteStore = createSqliteStore(nextConfiguration)
            val nextPostgresStore = createPostgresStore(nextConfiguration)
            val nextActiveSource = resolveRequestedActiveSource(
                requestedSource = request.activeSource,
                postgresCandidate = nextPostgresStore,
            )

            val oldSqliteStore = sqliteStore
            val oldPostgresStore = postgresStore

            sqliteStore = nextSqliteStore
            postgresStore = nextPostgresStore
            activeSource = nextActiveSource

            oldSqliteStore.close()
            oldPostgresStore.close()

            return readSettings()
        }
    }

    override fun close() {
        synchronized(lock) {
            sqliteStore.close()
            postgresStore.close()
        }
    }

    private fun resolveInitialActiveSource(): String {
        return resolveRequestedActiveSource(
            requestedSource = settingsState.initialActiveSource(),
            postgresCandidate = postgresStore,
        )
    }

    private fun resolveRequestedActiveSource(
        requestedSource: String,
        postgresCandidate: JdbcNoteStore,
    ): String {
        val normalized = settingsState.normalizeSource(requestedSource)
        if (normalized == NoteStorageSettingsState.SOURCE_POSTGRES && postgresCandidate.isReady()) {
            return NoteStorageSettingsState.SOURCE_POSTGRES
        }
        return NoteStorageSettingsState.SOURCE_SQLITE
    }

    private fun createSqliteStore(configuration: NoteStorageConfiguration): JdbcNoteStore {
        return storeFactory.createSqlite(configuration.sqlitePath)
    }

    private fun createPostgresStore(configuration: NoteStorageConfiguration): JdbcNoteStore {
        return storeFactory.createPostgres(
            url = configuration.postgresUrl,
            username = configuration.postgresUser,
            password = configuration.postgresPassword,
        )
    }
}
