package com.kcloud.server.routes

import com.kcloud.server.model.PauseResumeResponse
import com.kcloud.server.model.SuccessResponse
import com.kcloud.server.model.SyncStatusResponse
import com.kcloud.plugins.quicktransfer.QuickTransferService
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Routing.installQuickTransferRoutes(
    service: QuickTransferService
) {
    route("/api/sync") {
        get("/status") {
            val state = service.currentState()
            call.respond(
                SyncStatusResponse(
                    status = state.syncStatus.name,
                    progress = state.overallProgress,
                    currentOperation = state.currentOperation,
                    pendingUploads = state.pendingUploads,
                    pendingDownloads = state.pendingDownloads,
                    conflictCount = state.conflictCount,
                    isOnline = state.isOnline,
                    lastSyncTime = state.lastSyncTime
                )
            )
        }

        post("/trigger") {
            val result = service.triggerSync()
            if (!result.success) {
                call.respond(
                    HttpStatusCode.ServiceUnavailable,
                    SuccessResponse(success = false, error = result.message)
                )
                return@post
            }

            call.respond(SuccessResponse(success = true))
        }

        post("/pause") {
            val result = service.pause()
            if (!result.success) {
                call.respond(
                    HttpStatusCode.ServiceUnavailable,
                    PauseResumeResponse(success = false, paused = false)
                )
                return@post
            }

            call.respond(PauseResumeResponse(success = true, paused = true))
        }

        post("/resume") {
            val result = service.resume()
            if (!result.success) {
                call.respond(
                    HttpStatusCode.ServiceUnavailable,
                    PauseResumeResponse(success = false, paused = true)
                )
                return@post
            }

            call.respond(PauseResumeResponse(success = true, paused = false))
        }
    }
}
