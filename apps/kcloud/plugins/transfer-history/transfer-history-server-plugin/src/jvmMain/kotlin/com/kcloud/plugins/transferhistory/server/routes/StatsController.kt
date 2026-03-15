package com.kcloud.plugins.transferhistory.server.routes

import com.kcloud.plugins.transferhistory.TransferHistoryService
import com.kcloud.server.model.QueueItemResponse
import com.kcloud.server.model.StatsResponse
import com.kcloud.server.model.SuccessResponse
import io.ktor.server.application.ApplicationCall
import org.koin.ktor.ext.getKoin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@GetMapping("/api/stats")
fun readTransferStats(call: ApplicationCall): StatsResponse {
    val stats = call.transferHistoryService().currentSnapshot().stats
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
fun listTransferQueue(call: ApplicationCall): List<QueueItemResponse> {
    return call.transferHistoryService().currentSnapshot().queueItems.map { item ->
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
fun clearCompletedQueue(call: ApplicationCall): SuccessResponse {
    val result = call.transferHistoryService().clearCompletedQueue()
    return SuccessResponse(success = result.success, error = if (result.success) null else result.message)
}

private fun ApplicationCall.transferHistoryService(): TransferHistoryService {
    return application.getKoin().get()
}
