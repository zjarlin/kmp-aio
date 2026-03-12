package com.kcloud.plugins.quicktransfer

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@Serializable
enum class QuickTransferSyncStatus {
    IDLE,
    SCANNING,
    UPLOADING,
    DOWNLOADING,
    CONFLICT,
    ERROR,
    PAUSED,
    OFFLINE
}

@Serializable
data class QuickTransferState(
    val syncStatus: QuickTransferSyncStatus = QuickTransferSyncStatus.IDLE,
    val overallProgress: Float = 0f,
    val currentOperation: String? = null,
    val pendingUploads: Int = 0,
    val pendingDownloads: Int = 0,
    val conflictCount: Int = 0,
    val isOnline: Boolean = true,
    val lastSyncTime: Long? = null
)

@Serializable
data class QuickTransferActionResult(
    val success: Boolean,
    val message: String
)

interface QuickTransferService {
    val state: StateFlow<QuickTransferState>

    fun currentState(): QuickTransferState

    fun isEngineAvailable(): Boolean

    suspend fun triggerSync(): QuickTransferActionResult

    fun pause(): QuickTransferActionResult

    fun resume(): QuickTransferActionResult
}
