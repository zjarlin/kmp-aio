package site.addzero.kbox.core.model

import kotlinx.serialization.Serializable

@Serializable
data class KboxSyncMappingConfig(
    val mappingId: String = "",
    val displayName: String = "",
    val localRoot: String = "",
    val remoteRoot: String = "",
    val enabled: Boolean = true,
)

data class KboxSyncFileSnapshot(
    val absolutePath: String,
    val relativePath: String,
    val sizeBytes: Long,
    val lastModifiedMillis: Long,
    val md5: String,
)

data class KboxRemoteFileInfo(
    val absolutePath: String,
    val relativePath: String,
    val sizeBytes: Long,
    val lastModifiedMillis: Long,
    val md5: String,
)

@Serializable
data class KboxSyncIndexRecord(
    val mappingId: String = "",
    val relativePath: String = "",
    val localMd5: String = "",
    val remoteMd5: String = "",
    val localSizeBytes: Long = 0,
    val remoteSizeBytes: Long = 0,
    val syncedAtMillis: Long = 0,
    val localReleased: Boolean = false,
)

enum class KboxSyncDecision {
    UPLOAD_TO_REMOTE,
    DOWNLOAD_TO_LOCAL,
    RELEASE_LOCAL,
    KEEP_LOCAL,
    KEEP_REMOTE,
    COMPARE_CONTENT,
    MANUAL_REVIEW,
}

enum class KboxSyncAction {
    UPLOAD,
    DOWNLOAD,
    RELEASE_LOCAL,
    KEEP_LOCAL,
    KEEP_REMOTE,
    COMPARE_CONTENT,
}

data class KboxSyncEntry(
    val mappingId: String,
    val mappingName: String,
    val relativePath: String,
    val localFile: KboxSyncFileSnapshot? = null,
    val remoteFile: KboxRemoteFileInfo? = null,
    val decision: KboxSyncDecision,
    val recommendedAction: KboxSyncAction? = null,
    val reason: String = "",
    val lastSyncedAtMillis: Long = 0,
    val autoExecutable: Boolean = false,
) {
    val entryId: String = "$mappingId::$relativePath"
}

enum class KboxSyncStatus {
    STOPPED,
    STARTING,
    SCANNING,
    RUNNING,
    PAUSED,
    ERROR,
}

data class KboxSyncRunState(
    val status: KboxSyncStatus = KboxSyncStatus.STOPPED,
    val activeMappingIds: Set<String> = emptySet(),
    val startedAtMillis: Long = 0,
    val lastRefreshAtMillis: Long = 0,
    val lastError: String = "",
)

enum class KboxSyncTransferStatus {
    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED,
}

data class KboxSyncTransferTask(
    val taskId: String = "",
    val mappingId: String = "",
    val mappingName: String = "",
    val relativePath: String = "",
    val action: KboxSyncAction = KboxSyncAction.UPLOAD,
    val status: KboxSyncTransferStatus = KboxSyncTransferStatus.QUEUED,
    val bytesTransferred: Long = 0,
    val totalBytes: Long = 0,
    val createdAtMillis: Long = 0,
    val startedAtMillis: Long = 0,
    val finishedAtMillis: Long = 0,
    val detail: String = "",
    val error: String = "",
) {
    val progressFraction: Float
        get() = when {
            totalBytes > 0 -> (bytesTransferred.toDouble() / totalBytes.toDouble())
                .coerceIn(0.0, 1.0)
                .toFloat()
            status == KboxSyncTransferStatus.COMPLETED -> 1f
            else -> 0f
        }
}

data class KboxSyncTransferQueueState(
    val activeTasks: List<KboxSyncTransferTask> = emptyList(),
    val recentTasks: List<KboxSyncTransferTask> = emptyList(),
    val lastUpdatedAtMillis: Long = 0,
) {
    val queuedCount: Int
        get() = activeTasks.count { task -> task.status == KboxSyncTransferStatus.QUEUED }

    val runningCount: Int
        get() = activeTasks.count { task -> task.status == KboxSyncTransferStatus.RUNNING }

    val completedCount: Int
        get() = recentTasks.count { task -> task.status == KboxSyncTransferStatus.COMPLETED }

    val failedCount: Int
        get() = recentTasks.count { task -> task.status == KboxSyncTransferStatus.FAILED }

    val currentTask: KboxSyncTransferTask?
        get() = activeTasks.firstOrNull { task -> task.status == KboxSyncTransferStatus.RUNNING }
            ?: activeTasks.firstOrNull()

    val overallProgressFraction: Float
        get() {
            val measurable = activeTasks.filter { task -> task.totalBytes > 0 }
            if (measurable.isEmpty()) {
                return if (activeTasks.isEmpty()) 0f else currentTask?.progressFraction ?: 0f
            }
            val transferred = measurable.sumOf { task -> task.bytesTransferred }
            val total = measurable.sumOf { task -> task.totalBytes }.coerceAtLeast(1L)
            return (transferred.toDouble() / total.toDouble())
                .coerceIn(0.0, 1.0)
                .toFloat()
        }
}

enum class KboxComparePreviewMode {
    TEXT,
    BINARY,
}

data class KboxCompareSidePreview(
    val path: String = "",
    val sizeBytes: Long = 0,
    val lastModifiedMillis: Long = 0,
    val md5: String = "",
    val present: Boolean = false,
    val content: String = "",
)

data class KboxComparePreview(
    val mappingId: String = "",
    val relativePath: String = "",
    val mode: KboxComparePreviewMode = KboxComparePreviewMode.TEXT,
    val local: KboxCompareSidePreview = KboxCompareSidePreview(),
    val remote: KboxCompareSidePreview = KboxCompareSidePreview(),
    val truncated: Boolean = false,
)

@Serializable
data class KboxTrashRecord(
    val sourcePath: String = "",
    val trashPath: String = "",
    val deletedAtMillis: Long = 0,
)
