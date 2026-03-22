package com.kcloud.features.transferhistory.server.routes

import com.kcloud.features.transferhistory.TransferHistoryActionResult
import com.kcloud.features.transferhistory.TransferHistoryService
import com.kcloud.server.model.QueueItemResponse
import com.kcloud.server.model.StatsResponse
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@GetMapping("/api/stats")
fun readTransferStats(): StatsResponse {
    val stats = transferHistoryService().currentSnapshot().stats
    return StatsResponse(
        totalFiles = stats.totalFiles,
        syncedFiles = stats.syncedFiles,
        pendingUploads = stats.pendingUploads,
        pendingDownloads = stats.pendingDownloads,
        conflicts = stats.conflicts,
        queuePending = stats.queuePending,
        queueRunning = stats.queueRunning,
        queueFailed = stats.queueFailed,
    )
}

@GetMapping("/api/queue")
fun listTransferQueue(): List<QueueItemResponse> {
    return transferHistoryService().currentSnapshot().queueItems.map { item ->
        QueueItemResponse(
            id = item.id,
            operation = item.operation.name,
            status = item.status.name,
            progress = if (item.totalBytes > 0) {
                item.progressBytes.toFloat() / item.totalBytes
            } else {
                0f
            },
            retryCount = item.retryCount,
        )
    }
}

@PostMapping("/api/queue/clear-completed")
fun clearCompletedQueue(): TransferHistoryActionResult {
    return transferHistoryService().clearCompletedQueue()
}

private fun transferHistoryService(): TransferHistoryService {
    return KoinPlatform.getKoin().get()
}
