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
