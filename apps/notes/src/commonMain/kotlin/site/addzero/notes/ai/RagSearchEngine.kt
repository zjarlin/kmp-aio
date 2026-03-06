package site.addzero.notes.ai

import site.addzero.notes.model.KnowledgeGraph
import site.addzero.notes.model.Note

data class RagSearchHit(
    val note: Note,
    val score: Int,
    val reason: String
)

class RagSearchEngine {
    fun search(query: String, notes: List<Note>, graph: KnowledgeGraph): List<RagSearchHit> {
        if (notes.isEmpty()) {
            return emptyList()
        }

        val normalizedTerms = query
            .lowercase()
            .split(Regex("\\s+"))
            .map { term -> term.trim() }
            .filter { term -> term.isNotBlank() }

        val nodeDegrees = graph.edges
            .flatMap { edge -> listOf(edge.fromNoteId, edge.toNoteId) }
            .groupingBy { nodeId -> nodeId }
            .eachCount()

        if (normalizedTerms.isEmpty()) {
            return notes
                .sortedWith(
                    compareByDescending<Note> { note -> note.pinned }
                        .thenByDescending { note -> note.version }
                )
                .take(30)
                .map { note ->
                    val degree = nodeDegrees[note.id] ?: 0
                    RagSearchHit(
                        note = note,
                        score = degree,
                        reason = "默认排序，关联边=$degree"
                    )
                }
        }

        return notes
            .map { note ->
                val titleLower = note.title.lowercase()
                val pathLower = note.path.lowercase()
                val markdownLower = note.markdown.lowercase()
                val degree = nodeDegrees[note.id] ?: 0

                var score = 0
                normalizedTerms.forEach { term ->
                    if (titleLower.contains(term)) {
                        score += 40
                    }
                    if (pathLower.contains(term)) {
                        score += 25
                    }
                    val bodyHits = Regex(Regex.escape(term)).findAll(markdownLower).count()
                    score += bodyHits * 5
                }
                score += degree * 2

                val reason = buildString {
                    append("term=")
                    append(normalizedTerms.joinToString("+"))
                    append("，graphDegree=")
                    append(degree)
                }

                RagSearchHit(note = note, score = score, reason = reason)
            }
            .filter { hit -> hit.score > 0 }
            .sortedByDescending { hit -> hit.score }
            .take(100)
    }
}
