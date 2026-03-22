package com.kcloud.server.model

import kotlinx.serialization.Serializable

@Serializable
data class SyncStatusResponse(
    val status: String,
    val progress: Float,
    val currentOperation: String?,
    val pendingUploads: Int,
    val pendingDownloads: Int,
    val conflictCount: Int,
    val isOnline: Boolean,
    val lastSyncTime: Long?
)

@Serializable
data class FileInfoResponse(
    val path: String,
    val localSize: Long?,
    val remoteSize: Long?,
    val syncState: String,
    val lastSyncTime: Long?
)

@Serializable
data class ConflictResponse(
    val path: String,
    val localSize: Long,
    val remoteSize: Long
)

@Serializable
data class ResolveConflictRequest(
    val path: String,
    val resolution: String
)

@Serializable
data class QueueItemResponse(
    val id: Long,
    val operation: String,
    val status: String,
    val progress: Float,
    val retryCount: Int
)

@Serializable
data class StatsResponse(
    val totalFiles: Int,
    val syncedFiles: Int,
    val pendingUploads: Int,
    val pendingDownloads: Int,
    val conflicts: Int,
    val queuePending: Int,
    val queueRunning: Int,
    val queueFailed: Int
)

@Serializable
data class HealthResponse(
    val status: String,
    val version: String,
    val timestamp: Long
)
