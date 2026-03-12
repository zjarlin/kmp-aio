package com.kcloud.plugins.file.server

import com.kcloud.db.ConflictResolution
import com.kcloud.db.Database
import com.kcloud.db.DatabaseManager
import com.kcloud.db.FileRecord
import com.kcloud.db.SyncState
import com.kcloud.plugins.file.FileConflictResolution
import com.kcloud.plugins.file.FileSyncState
import com.kcloud.plugins.file.FileWorkspaceActionResult
import com.kcloud.plugins.file.FileWorkspaceRecord
import com.kcloud.plugins.file.FileWorkspaceService
import com.kcloud.sync.SyncEngineManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class FileWorkspaceServiceImpl : FileWorkspaceService {
    private val database = ensureDatabase()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val records: StateFlow<List<FileWorkspaceRecord>> = database.observeFileRecords()
        .map { items -> items.map { record -> record.toWorkspaceRecord() } }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = database.getAllFileRecords().map { record -> record.toWorkspaceRecord() }
        )

    override fun currentRecords(): List<FileWorkspaceRecord> {
        return records.value
    }

    override suspend fun triggerSync(): FileWorkspaceActionResult {
        return runCatching { SyncEngineManager.get().syncNow() }
            .fold(
                onSuccess = { syncResult ->
                    FileWorkspaceActionResult(
                        success = syncResult.success,
                        message = if (syncResult.success) {
                            "已触发一次全量同步检查"
                        } else {
                            "同步执行失败：${syncResult.errors.joinToString().ifBlank { "未知错误" }}"
                        }
                    )
                },
                onFailure = { throwable ->
                    FileWorkspaceActionResult(
                        success = false,
                        message = "同步引擎不可用：${throwable.message ?: "未初始化"}"
                    )
                }
            )
    }

    override suspend fun resolveConflict(
        path: String,
        resolution: FileConflictResolution
    ): FileWorkspaceActionResult {
        val engineResult = runCatching {
            SyncEngineManager.get().resolveConflict(path, resolution.toCoreResolution())
        }

        if (engineResult.isSuccess) {
            return engineResult.getOrThrow().fold(
                onSuccess = {
                    FileWorkspaceActionResult(
                        success = true,
                        message = "已处理冲突：$path -> ${resolution.name}"
                    )
                },
                onFailure = { throwable ->
                    FileWorkspaceActionResult(
                        success = false,
                        message = "冲突处理失败：${throwable.message ?: path}"
                    )
                }
            )
        }

        val fallback = database.resolveConflict(path, resolution.toCoreResolution())
        return if (fallback) {
            FileWorkspaceActionResult(
                success = true,
                message = "同步引擎未初始化，已先更新数据库记录：$path -> ${resolution.name}"
            )
        } else {
            FileWorkspaceActionResult(
                success = false,
                message = "冲突处理失败：${engineResult.exceptionOrNull()?.message ?: path}"
            )
        }
    }

    override fun removeRecord(path: String): FileWorkspaceActionResult {
        val deleted = database.deleteFileRecord(path)
        return if (deleted) {
            FileWorkspaceActionResult(success = true, message = "已移除记录：$path")
        } else {
            FileWorkspaceActionResult(success = false, message = "移除记录失败：$path")
        }
    }
}

private fun ensureDatabase(): Database {
    return runCatching { DatabaseManager.get() }
        .getOrElse { DatabaseManager.initialize() }
}

private fun FileRecord.toWorkspaceRecord(): FileWorkspaceRecord {
    return FileWorkspaceRecord(
        path = path,
        localSize = localSize,
        remoteSize = remoteSize,
        syncState = syncState.toFileSyncState(),
        lastSyncTime = lastSyncTime
    )
}

private fun SyncState.toFileSyncState(): FileSyncState {
    return when (this) {
        SyncState.SYNCED -> FileSyncState.SYNCED
        SyncState.PENDING_UPLOAD -> FileSyncState.PENDING_UPLOAD
        SyncState.PENDING_DOWNLOAD -> FileSyncState.PENDING_DOWNLOAD
        SyncState.CONFLICT -> FileSyncState.CONFLICT
        SyncState.ERROR -> FileSyncState.ERROR
    }
}

private fun FileConflictResolution.toCoreResolution(): ConflictResolution {
    return when (this) {
        FileConflictResolution.USE_LOCAL -> ConflictResolution.USE_LOCAL
        FileConflictResolution.USE_REMOTE -> ConflictResolution.USE_REMOTE
        FileConflictResolution.KEEP_BOTH -> ConflictResolution.KEEP_BOTH
    }
}
