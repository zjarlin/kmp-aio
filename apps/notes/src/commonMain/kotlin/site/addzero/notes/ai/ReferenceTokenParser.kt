package site.addzero.notes.ai

import site.addzero.notes.model.Note

private val TOKEN_REGEX = Regex("""@([a-zA-Z0-9_./-]+)""")

object ReferenceTokenParser {
    fun extractReferences(markdown: String): List<String> {
        return TOKEN_REGEX
            .findAll(markdown)
            .map { match -> match.groupValues[1] }
            .distinct()
            .toList()
    }

    fun normalizePath(path: String): String {
        if (path.isBlank()) {
            return "/"
        }
        val trimmed = path.trim().replace("\\", "/")
        return if (trimmed.startsWith("/")) {
            trimmed
        } else {
            "/$trimmed"
        }
    }

    fun resolveReference(reference: String, notes: List<Note>): Note? {
        val normalized = normalizePath(reference)
        val exact = notes.firstOrNull { normalizePath(it.path) == normalized }
        if (exact != null) {
            return exact
        }
        return notes.firstOrNull { normalizePath(it.path).endsWith(normalized) }
    }
}
