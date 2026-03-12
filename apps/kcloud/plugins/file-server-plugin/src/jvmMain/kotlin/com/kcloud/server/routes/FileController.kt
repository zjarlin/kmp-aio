package com.kcloud.server.routes

import com.kcloud.plugins.file.FileConflictResolution
import com.kcloud.plugins.file.FileSyncState
import com.kcloud.plugins.file.FileWorkspaceService
import com.kcloud.server.model.ConflictResponse
import com.kcloud.server.model.FileInfoResponse
import com.kcloud.server.model.ResolveConflictRequest
import com.kcloud.server.model.SuccessResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Routing.installFileRoutes(
    service: FileWorkspaceService
) {
    route("/api/files") {
        get {
            val response = service.currentRecords().map { record ->
                FileInfoResponse(
                    path = record.path,
                    localSize = record.localSize,
                    remoteSize = record.remoteSize,
                    syncState = record.syncState.name,
                    lastSyncTime = record.lastSyncTime
                )
            }
            call.respond(response)
        }
    }

    route("/api/conflicts") {
        get {
            val response = service.currentRecords()
                .filter { record -> record.syncState == FileSyncState.CONFLICT }
                .map { record ->
                    ConflictResponse(
                        path = record.path,
                        localSize = record.localSize ?: 0,
                        remoteSize = record.remoteSize ?: 0
                    )
                }
            call.respond(response)
        }

        post("/resolve") {
            val request = call.receive<ResolveConflictRequest>()
            val resolution = runCatching {
                FileConflictResolution.valueOf(request.resolution)
            }.getOrElse {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SuccessResponse(success = false, error = "未知冲突处理策略")
                )
                return@post
            }
            val result = service.resolveConflict(request.path, resolution)

            call.respond(
                if (result.success) {
                    SuccessResponse(success = true)
                } else {
                    SuccessResponse(success = false, error = result.message)
                }
            )
        }
    }
}
