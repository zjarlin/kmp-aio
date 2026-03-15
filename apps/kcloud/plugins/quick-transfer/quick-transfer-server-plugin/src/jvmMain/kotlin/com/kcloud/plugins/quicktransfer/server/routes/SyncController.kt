package com.kcloud.plugins.quicktransfer.server.routes

import com.kcloud.plugins.quicktransfer.QuickTransferService
import com.kcloud.server.model.PauseResumeResponse
import com.kcloud.server.model.SuccessResponse
import com.kcloud.server.model.SyncStatusResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import org.koin.ktor.ext.getKoin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@GetMapping("/api/sync/status")
fun readSyncStatus(call: ApplicationCall): SyncStatusResponse {
    val state = call.quickTransferService().currentState()
    return SyncStatusResponse(
        status = state.syncStatus.name,
        progress = state.overallProgress,
        currentOperation = state.currentOperation,
        pendingUploads = state.pendingUploads,
        pendingDownloads = state.pendingDownloads,
        conflictCount = state.conflictCount,
        isOnline = state.isOnline,
        lastSyncTime = state.lastSyncTime,
    )
}

@PostMapping("/api/sync/trigger")
suspend fun triggerSync(call: ApplicationCall): SuccessResponse {
    val result = call.quickTransferService().triggerSync()
    if (!result.success) {
        call.response.status(HttpStatusCode.ServiceUnavailable)
        return SuccessResponse(success = false, error = result.message)
    }

    return SuccessResponse(success = true)
}

@PostMapping("/api/sync/pause")
fun pauseSync(call: ApplicationCall): PauseResumeResponse {
    val result = call.quickTransferService().pause()
    if (!result.success) {
        call.response.status(HttpStatusCode.ServiceUnavailable)
        return PauseResumeResponse(success = false, paused = false)
    }

    return PauseResumeResponse(success = true, paused = true)
}

@PostMapping("/api/sync/resume")
fun resumeSync(call: ApplicationCall): PauseResumeResponse {
    val result = call.quickTransferService().resume()
    if (!result.success) {
        call.response.status(HttpStatusCode.ServiceUnavailable)
        return PauseResumeResponse(success = false, paused = true)
    }

    return PauseResumeResponse(success = true, paused = false)
}

private fun ApplicationCall.quickTransferService(): QuickTransferService {
    return application.getKoin().get()
}
