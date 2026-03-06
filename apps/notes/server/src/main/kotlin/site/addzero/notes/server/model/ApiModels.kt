package site.addzero.notes.server.model

import kotlinx.serialization.Serializable

@Serializable
data class NotePayload(
    val id: String,
    val path: String,
    val title: String,
    val markdown: String,
    val pinned: Boolean = false,
    val version: Long = 1L
)

@Serializable
data class NoteUpsertRequest(
    val id: String,
    val path: String,
    val title: String,
    val markdown: String,
    val pinned: Boolean = false,
    val version: Long = 1L
)

@Serializable
data class DataSourceHealthPayload(
    val source: String,
    val available: Boolean,
    val message: String
)
