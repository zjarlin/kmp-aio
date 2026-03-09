package site.addzero.notes.data

import kotlin.random.Random
import site.addzero.notes.ai.ReferenceTokenParser
import site.addzero.notes.api.NotesApiClient
import site.addzero.notes.api.StorageSettingsPayload
import site.addzero.notes.api.StorageSettingsUpdateRequest
import site.addzero.notes.model.DataSourceHealth
import site.addzero.notes.model.DataSourceType
import site.addzero.notes.model.Note
import site.addzero.notes.model.StorageSettings
import site.addzero.notes.model.StorageSettingsUpdate
import site.addzero.notes.model.SyncResult

class MultiSourceNoteRepository(
    private val local: NoteDataSource,
    private val remote: NoteDataSource
) : NoteRepository {
    private var activeSourceType: DataSourceType = DataSourceType.SQLITE

    override suspend fun listNotes(): List<Note> {
        val notes = activeDataSource().fetchNotes()
        return sortNotes(notes)
    }

    override suspend fun createNote(): Note {
        val existing = listNotes()
        val note = Note(
            id = generateId(),
            path = makeUniquePath("/notes/new-note.md", existing),
            title = "新建备忘录",
            markdown = "# 新建备忘录\n\n输入 Markdown 内容，或输入 @thisFile / @其他笔记路径 后使用一键整理。",
            pinned = false,
            version = 1L
        )
        activeDataSource().upsertNote(note)
        return note
    }

    override suspend fun saveNote(note: Note): Note {
        val currentNotes = runCatching { activeDataSource().fetchNotes() }
            .getOrDefault(emptyList())

        val previous = currentNotes.firstOrNull { item -> item.id == note.id }
        val nextVersion = maxOf(previous?.version ?: 0L, note.version) + 1L
        val normalizedPath = sanitizePath(note.path, note.title)
        val uniquePath = makeUniquePath(normalizedPath, currentNotes, note.id)
        val normalizedTitle = normalizeTitle(note.title, note.markdown)

        val normalized = note.copy(
            title = normalizedTitle,
            path = uniquePath,
            version = nextVersion
        )
        activeDataSource().upsertNote(normalized)
        return normalized
    }

    override suspend fun togglePinned(noteId: String): Note? {
        val note = listNotes().firstOrNull { item -> item.id == noteId } ?: return null
        return saveNote(note.copy(pinned = !note.pinned))
    }

    override suspend fun deleteNote(noteId: String) {
        activeDataSource().deleteNote(noteId)
    }

    override suspend fun sync(): SyncResult {
        val remoteHealth = remote.health()
        if (!remoteHealth.available) {
            return SyncResult(
                errors = listOf("PostgreSQL 不可用：${remoteHealth.message}")
            )
        }

        val errors = mutableListOf<String>()
        val localNotes = runCatching { local.fetchNotes() }.getOrElse { throwable ->
            return SyncResult(errors = listOf("读取 SQLite 失败：${throwable.message.orEmpty()}"))
        }

        var pushedCount = 0
        localNotes.forEach { note ->
            runCatching { remote.upsertNote(note) }
                .onSuccess { pushedCount += 1 }
                .onFailure { throwable ->
                    errors += "推送 ${note.path} 失败：${throwable.message.orEmpty()}"
                }
        }

        val remoteNotes = runCatching { remote.fetchNotes() }.getOrElse { throwable ->
            return SyncResult(
                pushedCount = pushedCount,
                errors = errors + "拉取 PostgreSQL 失败：${throwable.message.orEmpty()}"
            )
        }

        val merged = mergeNotes(localNotes, remoteNotes)
        var pulledCount = 0
        merged.forEach { note ->
            runCatching { local.upsertNote(note) }
                .onSuccess { pulledCount += 1 }
                .onFailure { throwable ->
                    errors += "写回 ${note.path} 失败：${throwable.message.orEmpty()}"
                }
        }

        return SyncResult(
            pushedCount = pushedCount,
            pulledCount = pulledCount,
            errors = errors
        )
    }

    override suspend fun dataSourceHealth(): List<DataSourceHealth> {
        return listOf(local.health(), remote.health())
    }

    override suspend fun storageSettings(): StorageSettings {
        val payload = NotesApiClient.notesApi().storageSettings()
        val mapped = payload.toStorageSettings()
        activeSourceType = mapped.activeSource
        return mapped
    }

    override suspend fun updateStorageSettings(update: StorageSettingsUpdate): StorageSettings {
        val payload = NotesApiClient.notesApi().updateStorageSettings(
            StorageSettingsUpdateRequest(
                activeSource = update.activeSource.toApiSource(),
                sqlitePath = update.sqlitePath,
                postgresUrl = update.postgresUrl,
                postgresUser = update.postgresUser,
                postgresPassword = update.postgresPassword
            )
        )
        val mapped = payload.toStorageSettings()
        activeSourceType = mapped.activeSource
        return mapped
    }

    private fun activeDataSource(): NoteDataSource {
        return when (activeSourceType) {
            DataSourceType.SQLITE -> local
            DataSourceType.POSTGRES -> remote
        }
    }

    private fun sortNotes(notes: List<Note>): List<Note> {
        return notes.sortedWith(
            compareByDescending<Note> { note -> note.pinned }
                .thenByDescending { note -> note.version }
                .thenBy { note -> note.title.lowercase() }
        )
    }

    private fun mergeNotes(localNotes: List<Note>, remoteNotes: List<Note>): List<Note> {
        val merged = linkedMapOf<String, Note>()
        (localNotes + remoteNotes).forEach { note ->
            val existing = merged[note.id]
            if (existing == null) {
                merged[note.id] = note
                return@forEach
            }
            val winner = when {
                note.version > existing.version -> note
                note.version < existing.version -> existing
                note.pinned && !existing.pinned -> note
                else -> existing
            }
            merged[note.id] = winner
        }

        return sortNotes(merged.values.toList())
    }

    private fun generateId(): String {
        val first = Random.nextInt(100000, 999999)
        val second = Random.nextInt(1000, 9999)
        return "note-$first-$second"
    }

    private fun normalizeTitle(title: String, markdown: String): String {
        if (title.isNotBlank()) {
            return title.trim()
        }
        val firstLine = markdown
            .lines()
            .map { line -> line.trim() }
            .firstOrNull { line -> line.isNotBlank() }
        if (firstLine.isNullOrBlank()) {
            return "未命名笔记"
        }
        return firstLine.removePrefix("#").trim().ifBlank { "未命名笔记" }
    }

    private fun sanitizePath(rawPath: String, title: String): String {
        if (rawPath.isNotBlank()) {
            return ReferenceTokenParser.normalizePath(rawPath)
        }
        val slug = title
            .lowercase()
            .replace(Regex("[^a-z0-9\\u4e00-\\u9fa5]+"), "-")
            .trim('-')
            .ifBlank { "untitled" }
        return "/notes/$slug.md"
    }

    private fun makeUniquePath(path: String, notes: List<Note>, selfId: String? = null): String {
        val occupied = notes
            .filterNot { note -> note.id == selfId }
            .map { note -> ReferenceTokenParser.normalizePath(note.path) }
            .toSet()

        if (!occupied.contains(path)) {
            return path
        }

        val suffixBase = path.removeSuffix(".md")
        var index = 2
        while (true) {
            val candidate = "$suffixBase-$index.md"
            if (!occupied.contains(candidate)) {
                return candidate
            }
            index += 1
        }
    }

    private fun StorageSettingsPayload.toStorageSettings(): StorageSettings {
        return StorageSettings(
            activeSource = activeSource.toSourceType(),
            sqlitePath = sqlitePath,
            sqliteDefaultPath = sqliteDefaultPath,
            postgresUrl = postgresUrl,
            postgresUser = postgresUser,
            postgresConfigured = postgresConfigured,
            postgresAvailable = postgresAvailable
        )
    }

    private fun String.toSourceType(): DataSourceType {
        return when (lowercase()) {
            "postgres" -> DataSourceType.POSTGRES
            else -> DataSourceType.SQLITE
        }
    }

    private fun DataSourceType.toApiSource(): String {
        return when (this) {
            DataSourceType.SQLITE -> "sqlite"
            DataSourceType.POSTGRES -> "postgres"
        }
    }
}
