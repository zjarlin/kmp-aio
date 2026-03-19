package com.kcloud.features.quicktransfer.server.routes

import com.kcloud.features.quicktransfer.QuickTransferActionResult
import com.kcloud.features.quicktransfer.QuickTransferService
import com.kcloud.server.model.SyncStatusResponse
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@GetMapping("/api/sync/status")
fun readSyncStatus(): SyncStatusResponse {
    val state = quickTransferService().currentState()
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
suspend fun triggerSync(): QuickTransferActionResult {
    return quickTransferService().triggerSync()
}

@PostMapping("/api/sync/pause")
fun pauseSync(): QuickTransferActionResult {
    return quickTransferService().pause()
}

@PostMapping("/api/sync/resume")
fun resumeSync(): QuickTransferActionResult {
    return quickTransferService().resume()
}

private fun quickTransferService(): QuickTransferService {
    return KoinPlatform.getKoin().get()
}
