package com.kcloud.plugins.transferhistory.server.routes

import com.kcloud.plugins.transferhistory.TransferHistoryService
import com.kcloud.server.model.QueueItemResponse
import com.kcloud.server.model.StatsResponse
import com.kcloud.server.model.SuccessResponse
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Routing.installTransferHistoryRoutes(
    service: TransferHistoryService
) {
    route("/api") {
        get("/stats") {
            val stats = service.currentSnapshot().stats
            call.respond(
                StatsResponse(
                    totalFiles = stats.totalFiles,
                    syncedFiles = stats.syncedFiles,
                    pendingUploads = stats.pendingUploads,
                    pendingDownloads = stats.pendingDownloads,
                    conflicts = stats.conflicts,
                    queuePending = stats.queuePending,
                    queueRunning = stats.queueRunning,
                    queueFailed = stats.queueFailed
                )
            )
        }
    }

    route("/api/queue") {
        get {
            val response = service.currentSnapshot().queueItems.map { item ->
                QueueItemResponse(
                    id = item.id,
                    operation = item.operation.name,
                    status = item.status.name,
                    progress = if (item.totalBytes > 0) {
                        item.progressBytes.toFloat() / item.totalBytes
                    } else {
                        0f
                    },
                    retryCount = item.retryCount
                )
            }
            call.respond(response)
        }

        post("/clear-completed") {
            val result = service.clearCompletedQueue()
            call.respond(SuccessResponse(success = result.success, error = if (result.success) null else result.message))
        }
    }
}
