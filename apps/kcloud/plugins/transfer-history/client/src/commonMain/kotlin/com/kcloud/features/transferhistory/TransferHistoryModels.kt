package com.kcloud.features.transferhistory

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@Serializable
enum class TransferHistoryQueueStatus {
    PENDING,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

@Serializable
enum class TransferHistoryQueueOperation {
    UPLOAD,
    DOWNLOAD,
    DELETE_LOCAL,
    DELETE_REMOTE
}

@Serializable
data class TransferHistoryQueueItem(
    val id: Long,
    val fileId: Long,
    val operation: TransferHistoryQueueOperation,
    val status: TransferHistoryQueueStatus,
    val progressBytes: Long,
    val totalBytes: Long,
    val retryCount: Int,
    val errorMessage: String?
)

@Serializable
data class TransferHistoryStats(
    val totalFiles: Int = 0,
    val syncedFiles: Int = 0,
    val pendingUploads: Int = 0,
    val pendingDownloads: Int = 0,
    val conflicts: Int = 0,
    val queuePending: Int = 0,
    val queueRunning: Int = 0,
    val queueFailed: Int = 0
)

@Serializable
data class TransferHistorySnapshot(
    val stats: TransferHistoryStats = TransferHistoryStats(),
    val queueItems: List<TransferHistoryQueueItem> = emptyList()
)

@Serializable
data class TransferHistoryActionResult(
    val success: Boolean,
    val message: String
)

interface TransferHistoryService {
    val snapshot: StateFlow<TransferHistorySnapshot>

    fun currentSnapshot(): TransferHistorySnapshot

    fun clearCompletedQueue(): TransferHistoryActionResult
}
