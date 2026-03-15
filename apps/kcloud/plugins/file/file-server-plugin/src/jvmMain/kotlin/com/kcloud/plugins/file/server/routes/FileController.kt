package com.kcloud.plugins.file.server.routes

import com.kcloud.plugins.file.FileConflictResolution
import com.kcloud.plugins.file.FileSyncState
import com.kcloud.plugins.file.FileWorkspaceService
import com.kcloud.server.model.ConflictResponse
import com.kcloud.server.model.FileInfoResponse
import com.kcloud.server.model.ResolveConflictRequest
import com.kcloud.server.model.SuccessResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import org.koin.ktor.ext.getKoin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@GetMapping("/api/files")
fun listFileRecords(call: ApplicationCall): List<FileInfoResponse> {
    return call.fileWorkspaceService().currentRecords().map { record ->
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
fun listConflicts(call: ApplicationCall): List<ConflictResponse> {
    return call.fileWorkspaceService().currentRecords()
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
    call: ApplicationCall,
    @RequestBody request: ResolveConflictRequest,
): SuccessResponse {
    val resolution = runCatching {
        FileConflictResolution.valueOf(request.resolution)
    }.getOrElse {
        call.response.status(HttpStatusCode.BadRequest)
        return SuccessResponse(success = false, error = "未知冲突处理策略")
    }

    val result = call.fileWorkspaceService().resolveConflict(request.path, resolution)
    return if (result.success) {
        SuccessResponse(success = true)
    } else {
        SuccessResponse(success = false, error = result.message)
    }
}

private fun ApplicationCall.fileWorkspaceService(): FileWorkspaceService {
    return application.getKoin().get()
}
