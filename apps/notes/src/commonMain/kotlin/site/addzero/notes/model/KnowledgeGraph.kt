package site.addzero.notes.model

data class KnowledgeGraphNode(
    val noteId: String,
    val title: String,
    val path: String
)

data class KnowledgeGraphEdge(
    val fromNoteId: String,
    val toNoteId: String,
    val token: String
)

data class KnowledgeGraph(
    val nodes: List<KnowledgeGraphNode>,
    val edges: List<KnowledgeGraphEdge>
)
