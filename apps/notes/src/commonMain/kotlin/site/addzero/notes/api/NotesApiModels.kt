package site.addzero.notes.api

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

@Serializable
data class StorageSettingsPayload(
    val activeSource: String,
    val sqlitePath: String,
    val sqliteDefaultPath: String,
    val postgresUrl: String,
    val postgresUser: String,
    val postgresConfigured: Boolean,
    val postgresAvailable: Boolean
)

@Serializable
data class StorageSettingsUpdateRequest(
    val activeSource: String,
    val sqlitePath: String,
    val postgresUrl: String,
    val postgresUser: String,
    val postgresPassword: String = ""
)
