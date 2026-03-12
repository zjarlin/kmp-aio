package com.kcloud.plugins.quicktransfer.server

import com.kcloud.plugins.quicktransfer.QuickTransferActionResult
import com.kcloud.plugins.quicktransfer.QuickTransferService
import com.kcloud.plugins.quicktransfer.QuickTransferState
import com.kcloud.plugins.quicktransfer.QuickTransferSyncStatus
import com.kcloud.state.AppState
import com.kcloud.state.AppStateManager
import com.kcloud.state.SyncStatus
import com.kcloud.sync.SyncEngineManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class QuickTransferServiceImpl : QuickTransferService {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val state: StateFlow<QuickTransferState> = AppStateManager.state
        .map { appState -> appState.toQuickTransferState() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = AppStateManager.currentState.toQuickTransferState()
        )

    override fun currentState(): QuickTransferState {
        return state.value
    }

    override fun isEngineAvailable(): Boolean {
        return runCatching { SyncEngineManager.get() }.isSuccess
    }

    override suspend fun triggerSync(): QuickTransferActionResult {
        return runCatching { SyncEngineManager.get().syncNow() }
            .fold(
                onSuccess = { syncResult ->
                    QuickTransferActionResult(
                        success = syncResult.success,
                        message = buildString {
                            append(if (syncResult.success) "同步已触发" else "同步执行失败")
                            append("，上传 ${syncResult.uploaded}")
                            append("，下载 ${syncResult.downloaded}")
                            append("，冲突 ${syncResult.conflicts}")
                            if (syncResult.errors.isNotEmpty()) {
                                append("，错误 ${syncResult.errors.size}")
                            }
                        }
                    )
                },
                onFailure = { throwable ->
                    QuickTransferActionResult(
                        success = false,
                        message = "同步引擎不可用：${throwable.message ?: "未初始化"}"
                    )
                }
            )
    }

    override fun pause(): QuickTransferActionResult {
        return runCatching {
            SyncEngineManager.get().pause()
            QuickTransferActionResult(success = true, message = "已请求暂停同步队列")
        }.getOrElse { throwable ->
            QuickTransferActionResult(
                success = false,
                message = "无法暂停：${throwable.message ?: "同步引擎未初始化"}"
            )
        }
    }

    override fun resume(): QuickTransferActionResult {
        return runCatching {
            SyncEngineManager.get().resume()
            QuickTransferActionResult(success = true, message = "已恢复同步队列")
        }.getOrElse { throwable ->
            QuickTransferActionResult(
                success = false,
                message = "无法恢复：${throwable.message ?: "同步引擎未初始化"}"
            )
        }
    }
}

private fun AppState.toQuickTransferState(): QuickTransferState {
    return QuickTransferState(
        syncStatus = syncStatus.toQuickTransferSyncStatus(),
        overallProgress = overallProgress,
        currentOperation = currentOperation,
        pendingUploads = pendingUploads,
        pendingDownloads = pendingDownloads,
        conflictCount = conflictCount,
        isOnline = isOnline,
        lastSyncTime = lastSyncTime
    )
}

private fun SyncStatus.toQuickTransferSyncStatus(): QuickTransferSyncStatus {
    return when (this) {
        SyncStatus.IDLE -> QuickTransferSyncStatus.IDLE
        SyncStatus.SCANNING -> QuickTransferSyncStatus.SCANNING
        SyncStatus.UPLOADING -> QuickTransferSyncStatus.UPLOADING
        SyncStatus.DOWNLOADING -> QuickTransferSyncStatus.DOWNLOADING
        SyncStatus.CONFLICT -> QuickTransferSyncStatus.CONFLICT
        SyncStatus.ERROR -> QuickTransferSyncStatus.ERROR
        SyncStatus.PAUSED -> QuickTransferSyncStatus.PAUSED
        SyncStatus.OFFLINE -> QuickTransferSyncStatus.OFFLINE
    }
}
