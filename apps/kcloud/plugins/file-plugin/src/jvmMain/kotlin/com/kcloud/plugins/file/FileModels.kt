package com.kcloud.plugins.file

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@Serializable
enum class FileSyncState {
    SYNCED,
    PENDING_UPLOAD,
    PENDING_DOWNLOAD,
    CONFLICT,
    ERROR
}

@Serializable
enum class FileConflictResolution {
    USE_LOCAL,
    USE_REMOTE,
    KEEP_BOTH
}

@Serializable
data class FileWorkspaceRecord(
    val path: String,
    val localSize: Long?,
    val remoteSize: Long?,
    val syncState: FileSyncState,
    val lastSyncTime: Long?
)

@Serializable
data class FileWorkspaceActionResult(
    val success: Boolean,
    val message: String
)

interface FileWorkspaceService {
    val records: StateFlow<List<FileWorkspaceRecord>>

    fun currentRecords(): List<FileWorkspaceRecord>

    suspend fun triggerSync(): FileWorkspaceActionResult

    suspend fun resolveConflict(
        path: String,
        resolution: FileConflictResolution
    ): FileWorkspaceActionResult

    fun removeRecord(path: String): FileWorkspaceActionResult
}
