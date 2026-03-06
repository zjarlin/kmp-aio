package site.addzero.notes.data

import kotlin.random.Random
import site.addzero.notes.ai.ReferenceTokenParser
import site.addzero.notes.model.DataSourceHealth
import site.addzero.notes.model.Note
import site.addzero.notes.model.SyncResult

class MultiSourceNoteRepository(
    private val local: NoteDataSource,
    private val remote: NoteDataSource
) : NoteRepository {

    override suspend fun listNotes(): List<Note> {
        val localNotes = runCatching { local.fetchNotes() }.getOrDefault(emptyList())
        val remoteNotes = if (remoteAvailable()) {
            runCatching { remote.fetchNotes() }.getOrDefault(emptyList())
        } else {
            emptyList()
        }

        val merged = mergeNotes(localNotes, remoteNotes)
        merged.forEach { note ->
            runCatching { local.upsertNote(note) }
        }
        return merged
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
        local.upsertNote(note)
        if (remoteAvailable()) {
            runCatching { remote.upsertNote(note) }
        }
        return note
    }

    override suspend fun saveNote(note: Note): Note {
        val localNotes = runCatching { local.fetchNotes() }.getOrDefault(emptyList())
        val remoteNotes = if (remoteAvailable()) {
            runCatching { remote.fetchNotes() }.getOrDefault(emptyList())
        } else {
            emptyList()
        }
        val allNotes = mergeNotes(localNotes, remoteNotes)

        val previous = allNotes.firstOrNull { item -> item.id == note.id }
        val nextVersion = maxOf(previous?.version ?: 0L, note.version) + 1L
        val normalizedPath = sanitizePath(note.path, note.title)
        val uniquePath = makeUniquePath(normalizedPath, allNotes, note.id)
        val normalizedTitle = normalizeTitle(note.title, note.markdown)

        val normalized = note.copy(
            title = normalizedTitle,
            path = uniquePath,
            version = nextVersion
        )
        local.upsertNote(normalized)
        if (remoteAvailable()) {
            runCatching { remote.upsertNote(normalized) }
        }
        return normalized
    }

    override suspend fun togglePinned(noteId: String): Note? {
        val note = listNotes().firstOrNull { item -> item.id == noteId } ?: return null
        return saveNote(note.copy(pinned = !note.pinned))
    }

    override suspend fun deleteNote(noteId: String) {
        local.deleteNote(noteId)
        if (remoteAvailable()) {
            runCatching { remote.deleteNote(noteId) }
        }
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

    private suspend fun remoteAvailable(): Boolean {
        return remote.health().available
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

        return merged
            .values
            .sortedWith(
                compareByDescending<Note> { note -> note.pinned }
                    .thenByDescending { note -> note.version }
                    .thenBy { note -> note.title.lowercase() }
            )
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
}
