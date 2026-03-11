package com.kcloud.server.routes

import com.kcloud.db.DatabaseManager
import com.kcloud.server.model.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class StatsController {

    @GetMapping("/stats")
    fun getStats(): StatsResponse {
        val db = DatabaseManager.get()
        val stats = db.getStats()
        return StatsResponse(
            totalFiles = stats.totalFiles,
            syncedFiles = stats.syncedFiles,
            pendingUploads = stats.pendingUploads,
            pendingDownloads = stats.pendingDownloads,
            conflicts = stats.conflicts,
            queuePending = stats.queuePending,
            queueRunning = stats.queueRunning,
            queueFailed = stats.queueFailed
        )
    }
}

@RestController
@RequestMapping("/api/queue")
class QueueController {

    @GetMapping
    fun listQueueItems(): List<QueueItemResponse> {
        val db = DatabaseManager.get()
        return db.getQueueItems().map { item ->
            QueueItemResponse(
                id = item.id,
                operation = item.operation.name,
                status = item.status.name,
                progress = if (item.totalBytes > 0) {
                    item.progressBytes.toFloat() / item.totalBytes
                } else 0f,
                retryCount = item.retryCount
            )
        }
    }
}
