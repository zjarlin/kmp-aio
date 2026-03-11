package com.kcloud.server.routes

import com.kcloud.server.model.*
import com.kcloud.state.AppStateManager
import com.kcloud.sync.SyncEngineManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.launch
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/sync")
class SyncController {

    @GetMapping("/status")
    fun getStatus(): SyncStatusResponse {
        val state = AppStateManager.currentState
        return SyncStatusResponse(
            status = state.syncStatus.name,
            progress = state.overallProgress,
            currentOperation = state.currentOperation,
            pendingUploads = state.pendingUploads,
            pendingDownloads = state.pendingDownloads,
            conflictCount = state.conflictCount,
            isOnline = state.isOnline,
            lastSyncTime = state.lastSyncTime
        )
    }

    @PostMapping("/trigger")
    suspend fun triggerSync(call: ApplicationCall): SuccessResponse {
        return try {
            val syncEngine = SyncEngineManager.get()
            syncEngine.syncNow()
            SuccessResponse(success = true)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError)
            SuccessResponse(success = false, error = e.message)
        }
    }

    @PostMapping("/pause")
    fun pauseSync(): PauseResumeResponse {
        val syncEngine = SyncEngineManager.get()
        syncEngine.pause()
        return PauseResumeResponse(success = true, paused = true)
    }

    @PostMapping("/resume")
    fun resumeSync(): PauseResumeResponse {
        val syncEngine = SyncEngineManager.get()
        syncEngine.resume()
        return PauseResumeResponse(success = true, paused = false)
    }
}
