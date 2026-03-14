package com.kcloud.plugins.transferhistory.server

import com.kcloud.db.*
import com.kcloud.plugins.transferhistory.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
class TransferHistoryServiceImpl : TransferHistoryService {
    private val database = ensureDatabase()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _snapshot = MutableStateFlow(
        buildSnapshot(
            fileRecords = database.getAllFileRecords(),
            queueItems = database.getQueueItems()
        )
    )

    override val snapshot: StateFlow<TransferHistorySnapshot> = _snapshot.asStateFlow()

    init {
        scope.launch {
            database.observeFileRecords().collect { fileRecords ->
                _snapshot.value = buildSnapshot(
                    fileRecords = fileRecords,
                    queueItems = database.getQueueItems()
                )
            }
        }

        scope.launch {
            database.observeQueue().collect { queueItems ->
                _snapshot.value = buildSnapshot(
                    fileRecords = database.getAllFileRecords(),
                    queueItems = queueItems
                )
            }
        }
    }

    override fun currentSnapshot(): TransferHistorySnapshot {
        return snapshot.value
    }

    override fun clearCompletedQueue(): TransferHistoryActionResult {
        val cleared = database.clearCompletedQueue()
        _snapshot.value = buildSnapshot(
            fileRecords = database.getAllFileRecords(),
            queueItems = database.getQueueItems()
        )
        return TransferHistoryActionResult(
            success = true,
            message = "已清理 $cleared 条已完成/失败队列记录"
        )
    }
}

private fun ensureDatabase(): Database {
    return runCatching { DatabaseManager.get() }
        .getOrElse { DatabaseManager.initialize() }
}

private fun buildSnapshot(
    fileRecords: List<FileRecord>,
    queueItems: List<SyncQueueItem>
): TransferHistorySnapshot {
    return TransferHistorySnapshot(
        stats = TransferHistoryStats(
            totalFiles = fileRecords.size,
            syncedFiles = fileRecords.count { it.syncState == SyncState.SYNCED },
            pendingUploads = fileRecords.count { it.syncState == SyncState.PENDING_UPLOAD },
            pendingDownloads = fileRecords.count { it.syncState == SyncState.PENDING_DOWNLOAD },
            conflicts = fileRecords.count { it.syncState == SyncState.CONFLICT },
            queuePending = queueItems.count { it.status == QueueStatus.PENDING },
            queueRunning = queueItems.count { it.status == QueueStatus.RUNNING },
            queueFailed = queueItems.count { it.status == QueueStatus.FAILED }
        ),
        queueItems = queueItems.map { item -> item.toTransferHistoryQueueItem() }
    )
}

private fun SyncQueueItem.toTransferHistoryQueueItem(): TransferHistoryQueueItem {
    return TransferHistoryQueueItem(
        id = id,
        fileId = fileId,
        operation = operation.toTransferHistoryQueueOperation(),
        status = status.toTransferHistoryQueueStatus(),
        progressBytes = progressBytes,
        totalBytes = totalBytes,
        retryCount = retryCount,
        errorMessage = errorMessage
    )
}

private fun SyncOperation.toTransferHistoryQueueOperation(): TransferHistoryQueueOperation {
    return when (this) {
        SyncOperation.UPLOAD -> TransferHistoryQueueOperation.UPLOAD
        SyncOperation.DOWNLOAD -> TransferHistoryQueueOperation.DOWNLOAD
        SyncOperation.DELETE_LOCAL -> TransferHistoryQueueOperation.DELETE_LOCAL
        SyncOperation.DELETE_REMOTE -> TransferHistoryQueueOperation.DELETE_REMOTE
    }
}

private fun QueueStatus.toTransferHistoryQueueStatus(): TransferHistoryQueueStatus {
    return when (this) {
        QueueStatus.PENDING -> TransferHistoryQueueStatus.PENDING
        QueueStatus.RUNNING -> TransferHistoryQueueStatus.RUNNING
        QueueStatus.PAUSED -> TransferHistoryQueueStatus.PAUSED
        QueueStatus.COMPLETED -> TransferHistoryQueueStatus.COMPLETED
        QueueStatus.FAILED -> TransferHistoryQueueStatus.FAILED
        QueueStatus.CANCELLED -> TransferHistoryQueueStatus.CANCELLED
    }
}
