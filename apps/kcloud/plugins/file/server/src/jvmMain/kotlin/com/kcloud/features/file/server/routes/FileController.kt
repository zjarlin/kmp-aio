package com.kcloud.features.file.server.routes

import com.kcloud.features.file.FileConflictResolution
import com.kcloud.features.file.FileSyncState
import com.kcloud.features.file.FileWorkspaceActionResult
import com.kcloud.features.file.FileWorkspaceService
import com.kcloud.server.model.ConflictResponse
import com.kcloud.server.model.FileInfoResponse
import com.kcloud.server.model.ResolveConflictRequest
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@GetMapping("/api/files")
fun listFileRecords(): List<FileInfoResponse> {
    return fileWorkspaceService().currentRecords().map { record ->
        FileInfoResponse(
            path = record.path,
            localSize = record.localSize,
            remoteSize = record.remoteSize,
            syncState = record.syncState.name,
            lastSyncTime = record.lastSyncTime,
        )
    }
}

@GetMapping("/api/conflicts")
fun listConflicts(): List<ConflictResponse> {
    return fileWorkspaceService().currentRecords()
        .filter { record -> record.syncState == FileSyncState.CONFLICT }
        .map { record ->
            ConflictResponse(
                path = record.path,
                localSize = record.localSize ?: 0,
                remoteSize = record.remoteSize ?: 0,
            )
        }
}

@PostMapping("/api/conflicts/resolve")
suspend fun resolveConflict(
    @RequestBody request: ResolveConflictRequest,
): FileWorkspaceActionResult {
    val resolution = runCatching {
        FileConflictResolution.valueOf(request.resolution)
    }.getOrElse {
        throw IllegalArgumentException("未知冲突处理策略")
    }

    return fileWorkspaceService().resolveConflict(request.path, resolution)
}

private fun fileWorkspaceService(): FileWorkspaceService {
    return KoinPlatform.getKoin().get()
}
