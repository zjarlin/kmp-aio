package site.addzero.notes.model

data class Note(
    val id: String,
    val path: String,
    val title: String,
    val markdown: String,
    val pinned: Boolean = false,
    val version: Long = 1L
)

enum class DataSourceType {
    SQLITE,
    POSTGRES
}

data class DataSourceHealth(
    val type: DataSourceType,
    val available: Boolean,
    val message: String
)

data class SyncResult(
    val pushedCount: Int = 0,
    val pulledCount: Int = 0,
    val errors: List<String> = emptyList()
) {
    val isSuccess: Boolean
        get() = errors.isEmpty()
}

data class OrganizeResult(
    val organizedMarkdown: String,
    val usedReferences: List<String>,
    val missingReferences: List<String>
)

data class StorageSettings(
    val activeSource: DataSourceType = DataSourceType.SQLITE,
    val sqlitePath: String = "",
    val sqliteDefaultPath: String = "",
    val postgresUrl: String = "",
    val postgresUser: String = "",
    val postgresConfigured: Boolean = false,
    val postgresAvailable: Boolean = false
)

data class StorageSettingsUpdate(
    val activeSource: DataSourceType,
    val sqlitePath: String,
    val postgresUrl: String,
    val postgresUser: String,
    val postgresPassword: String
)
