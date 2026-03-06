package site.addzero.notes.graph

import site.addzero.notes.ai.ReferenceTokenParser
import site.addzero.notes.model.KnowledgeGraph
import site.addzero.notes.model.KnowledgeGraphEdge
import site.addzero.notes.model.KnowledgeGraphNode
import site.addzero.notes.model.Note

class KnowledgeGraphBuilder {
    fun build(notes: List<Note>): KnowledgeGraph {
        val nodes = notes.map { note ->
            KnowledgeGraphNode(
                noteId = note.id,
                title = note.title,
                path = note.path
            )
        }

        val edges = mutableListOf<KnowledgeGraphEdge>()
        notes.forEach { note ->
            val references = ReferenceTokenParser
                .extractReferences(note.markdown)
                .filterNot { reference -> reference == "thisFile" }

            references.forEach { reference ->
                val target = ReferenceTokenParser.resolveReference(reference, notes)
                if (target != null && target.id != note.id) {
                    edges += KnowledgeGraphEdge(
                        fromNoteId = note.id,
                        toNoteId = target.id,
                        token = "@$reference"
                    )
                }
            }
        }

        return KnowledgeGraph(
            nodes = nodes,
            edges = edges.distinctBy { edge ->
                "${edge.fromNoteId}->${edge.toNoteId}:${edge.token}"
            }
        )
    }
}
