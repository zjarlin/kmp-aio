package com.kcloud.server.routes

import com.kcloud.db.DatabaseManager
import com.kcloud.db.SyncState
import com.kcloud.server.model.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/files")
class FileController {

    @GetMapping
    fun listFiles(): List<FileInfoResponse> {
        val db = DatabaseManager.get()
        return db.getAllFileRecords().map { record ->
            FileInfoResponse(
                path = record.path,
                localSize = record.localSize,
                remoteSize = record.remoteSize,
                syncState = record.syncState.name,
                lastSyncTime = record.lastSyncTime
            )
        }
    }
}

@RestController
@RequestMapping("/api/conflicts")
class ConflictController {

    @GetMapping
    fun listConflicts(): List<ConflictResponse> {
        val db = DatabaseManager.get()
        return db.getFileRecordsByState(SyncState.CONFLICT)
            .map { record ->
                ConflictResponse(
                    path = record.path,
                    localSize = record.localSize ?: 0,
                    remoteSize = record.remoteSize ?: 0
                )
            }
    }

    @PostMapping("/resolve")
    fun resolveConflict(@RequestBody request: ResolveConflictRequest): SuccessResponse {
        val db = DatabaseManager.get()
        val success = db.resolveConflict(
            request.path,
            com.kcloud.db.ConflictResolution.valueOf(request.resolution)
        )
        return SuccessResponse(success = success)
    }
}
