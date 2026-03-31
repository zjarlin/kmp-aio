package site.addzero.kbox.plugins.tools.storagetool.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import site.addzero.kbox.core.model.KboxComparePreviewMode
import site.addzero.kbox.core.model.KboxSyncAction
import site.addzero.kbox.core.model.KboxSyncDecision
import site.addzero.kbox.core.model.KboxSyncEntry
import site.addzero.kbox.core.model.KboxSyncStatus
import site.addzero.kbox.core.model.KboxSyncTransferQueueState
import site.addzero.kbox.core.model.KboxSyncTransferStatus
import site.addzero.kbox.core.model.KboxSyncTransferTask
import site.addzero.kbox.plugins.tools.storagetool.KboxSyncToolState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SyncTab(
    state: KboxSyncToolState,
    modifier: Modifier,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onRefresh: () -> Unit,
    onCompare: (String) -> Unit,
    onApplyAction: (String, KboxSyncAction) -> Unit,
    onDismissCompare: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SyncMappingsPanel(
            state = state,
            modifier = Modifier
                .width(328.dp)
                .fillMaxHeight(),
            onStart = onStart,
            onPause = onPause,
            onRefresh = onRefresh,
        )
        SyncEntriesPanel(
            state = state,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onCompare = onCompare,
            onApplyAction = onApplyAction,
        )
        SyncDetailsPanel(
            state = state,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onCompare = onCompare,
            onApplyAction = onApplyAction,
            onDismissCompare = onDismissCompare,
        )
    }
}

@Composable
private fun SyncMappingsPanel(
    state: KboxSyncToolState,
    modifier: Modifier,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onRefresh: () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Sync control",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SyncBadge("State ${state.runState.status.name}")
                SyncBadge("${state.mappings.size} mappings")
            }
            SyncStatCard(
                title = "Run behavior",
                lines = listOf(
                    "Start on launch: ${state.syncStartOnLaunch}",
                    "Remote poll: ${state.remotePollSeconds}s",
                    "Reclaimable local: ${state.releasableEntryCount} files / ${formatBytes(state.releasableBytes)}",
                ),
            )
            startBlockedReason(state)?.let { reason ->
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            if (state.runState.lastRefreshAtMillis > 0) {
                Text("Last refresh: ${formatTime(state.runState.lastRefreshAtMillis)}")
            }
            if (state.runState.lastError.isNotBlank()) {
                Text(
                    text = state.runState.lastError,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            val currentTask = state.transferQueue.currentTask
            if (currentTask != null) {
                TransferQueueSummary(
                    queue = state.transferQueue,
                    currentTask = currentTask,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onStart, enabled = state.canStartSync) {
                    Text("Start")
                }
                Button(
                    onClick = onPause,
                    enabled = state.runState.status != KboxSyncStatus.STOPPED &&
                        state.runState.status != KboxSyncStatus.PAUSED,
                ) {
                    Text("Pause")
                }
                Button(onClick = onRefresh, enabled = state.canStartSync) {
                    Text("Refresh")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.selectedMappingId.isBlank(),
                    onClick = { state.selectMapping("") },
                    label = { Text("All") },
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.mappings, key = { mapping -> mapping.mappingId }) { mapping ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (state.selectedMappingId == mapping.mappingId) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                                } else {
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                },
                                RoundedCornerShape(14.dp),
                            )
                            .clickable { state.selectMapping(mapping.mappingId) }
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(mapping.displayName, fontWeight = FontWeight.Medium)
                        Text(
                            "${mapping.localRoot}\n${mapping.remoteRoot}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncEntriesPanel(
    state: KboxSyncToolState,
    modifier: Modifier,
    onCompare: (String) -> Unit,
    onApplyAction: (String, KboxSyncAction) -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Suggestions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            if (state.visibleEntries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No sync suggestion right now.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.visibleEntries, key = { entry -> entry.entryId }) { entry ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (state.selectedEntryId == entry.entryId) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                                    } else {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                                    },
                                    RoundedCornerShape(14.dp),
                                )
                                .clickable { state.selectEntry(entry.entryId) }
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(entry.relativePath, fontWeight = FontWeight.Medium)
                            Text(
                                "${entry.mappingName} / ${decisionLabel(entry.decision)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                            Text(
                                entry.reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                entry.recommendedAction?.let { action ->
                                    Button(onClick = { onApplyAction(entry.entryId, action) }) {
                                        Text(actionLabel(action))
                                    }
                                }
                                Button(onClick = { onCompare(entry.entryId) }) {
                                    Text("Compare")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncDetailsPanel(
    state: KboxSyncToolState,
    modifier: Modifier,
    onCompare: (String) -> Unit,
    onApplyAction: (String, KboxSyncAction) -> Unit,
    onDismissCompare: () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            val entry = state.selectedEntry
            if (entry == null) {
                Text(
                    text = "Select a sync entry.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
            } else {
                Text(entry.relativePath, fontWeight = FontWeight.Medium)
                SyncStatCard(
                    title = decisionLabel(entry.decision),
                    lines = listOf("Reason: ${entry.reason}"),
                )
                if (entry.lastSyncedAtMillis > 0) {
                    Text("Last synced: ${formatTime(entry.lastSyncedAtMillis)}")
                }
                Text(
                    text = buildMetadataSummary(entry),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    entryActions(entry).forEach { action ->
                        Button(onClick = { onApplyAction(entry.entryId, action) }) {
                            Text(actionLabel(action))
                        }
                    }
                    Button(onClick = { onCompare(entry.entryId) }) {
                        Text("Compare")
                    }
                }

                val preview = state.comparePreview
                if (preview != null && preview.mappingId == entry.mappingId && preview.relativePath == entry.relativePath) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Compare preview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Button(onClick = onDismissCompare) {
                            Text("Close")
                        }
                    }
                    if (preview.truncated) {
                        Text(
                            text = "Preview was truncated to keep the UI responsive.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    if (preview.mode == KboxComparePreviewMode.TEXT) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = preview.local.content,
                                onValueChange = {},
                                modifier = Modifier
                                    .weight(1f)
                                    .height(220.dp),
                                label = { Text("Local") },
                                readOnly = true,
                            )
                            OutlinedTextField(
                                value = preview.remote.content,
                                onValueChange = {},
                                modifier = Modifier
                                    .weight(1f)
                                    .height(220.dp),
                                label = { Text("Remote") },
                                readOnly = true,
                            )
                        }
                    } else {
                        Text(
                            text = buildBinarySummary(
                                localPath = preview.local.path,
                                localSize = preview.local.sizeBytes,
                                localMd5 = preview.local.md5,
                                remotePath = preview.remote.path,
                                remoteSize = preview.remote.sizeBytes,
                                remoteMd5 = preview.remote.md5,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }
            TransferQueueSection(state.transferQueue)
        }
    }
}

@Composable
private fun TransferQueueSummary(
    queue: KboxSyncTransferQueueState,
    currentTask: KboxSyncTransferTask,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                RoundedCornerShape(14.dp),
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "Transfer queue",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = "${actionLabel(currentTask.action)} / ${currentTask.relativePath}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        )
        LinearProgressIndicator(
            progress = { queue.overallProgressFraction },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "${queue.runningCount} running / ${queue.queuedCount} queued / ${queue.completedCount} recent",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun TransferQueueSection(
    queue: KboxSyncTransferQueueState,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Transfer queue",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        if (queue.activeTasks.isEmpty() && queue.recentTasks.isEmpty()) {
            Text(
                text = "No transfer activity yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
            return
        }
        if (queue.activeTasks.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                queue.activeTasks.forEach { task ->
                    TransferQueueRow(task)
                }
            }
        }
        if (queue.recentTasks.isNotEmpty()) {
            Text(
                text = "Recent",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                queue.recentTasks.take(6).forEach { task ->
                    TransferQueueRow(task)
                }
            }
        }
    }
}

@Composable
private fun TransferQueueRow(
    task: KboxSyncTransferTask,
) {
    val progressColor = when (task.status) {
        KboxSyncTransferStatus.FAILED -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.secondary
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                RoundedCornerShape(14.dp),
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "${actionLabel(task.action)} / ${task.relativePath}",
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = "${transferStatusLabel(task.status)} / ${task.mappingName}",
            style = MaterialTheme.typography.bodySmall,
            color = progressColor,
        )
        if (task.status == KboxSyncTransferStatus.RUNNING || task.status == KboxSyncTransferStatus.COMPLETED) {
            LinearProgressIndicator(
                progress = { task.progressFraction },
                modifier = Modifier.fillMaxWidth(),
                color = progressColor,
            )
        }
        Text(
            text = "${formatBytes(task.bytesTransferred)} / ${formatBytes(task.totalBytes)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
        )
        if (task.error.isNotBlank()) {
            Text(
                text = task.error,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun SyncBadge(
    text: String,
) {
    Card(
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SyncStatCard(
    title: String,
    lines: List<String>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                RoundedCornerShape(14.dp),
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
        lines.forEach { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun startBlockedReason(
    state: KboxSyncToolState,
): String? {
    return when {
        !state.syncEnabled -> "Enable SSH sync in Settings to start continuous sync."
        !state.sshEnabled -> "SSH is not enabled yet. Configure SSH first."
        !state.hasEnabledMappings -> "Add at least one enabled mapping before starting sync."
        else -> null
    }
}

private fun entryActions(
    entry: KboxSyncEntry,
): List<KboxSyncAction> {
    return when (entry.decision) {
        KboxSyncDecision.UPLOAD_TO_REMOTE -> listOf(KboxSyncAction.UPLOAD)
        KboxSyncDecision.DOWNLOAD_TO_LOCAL -> listOf(KboxSyncAction.DOWNLOAD)
        KboxSyncDecision.RELEASE_LOCAL -> listOf(KboxSyncAction.RELEASE_LOCAL)
        KboxSyncDecision.KEEP_LOCAL -> listOf(KboxSyncAction.KEEP_LOCAL, KboxSyncAction.KEEP_REMOTE)
        KboxSyncDecision.KEEP_REMOTE -> listOf(KboxSyncAction.KEEP_LOCAL, KboxSyncAction.KEEP_REMOTE)
        KboxSyncDecision.COMPARE_CONTENT -> listOf(KboxSyncAction.KEEP_LOCAL, KboxSyncAction.KEEP_REMOTE)
        KboxSyncDecision.MANUAL_REVIEW -> emptyList()
    }
}

private fun decisionLabel(
    decision: KboxSyncDecision,
): String {
    return when (decision) {
        KboxSyncDecision.UPLOAD_TO_REMOTE -> "Upload to remote"
        KboxSyncDecision.DOWNLOAD_TO_LOCAL -> "Download to local"
        KboxSyncDecision.RELEASE_LOCAL -> "Release local"
        KboxSyncDecision.KEEP_LOCAL -> "Keep local"
        KboxSyncDecision.KEEP_REMOTE -> "Keep remote"
        KboxSyncDecision.COMPARE_CONTENT -> "Compare content"
        KboxSyncDecision.MANUAL_REVIEW -> "Manual review"
    }
}

private fun actionLabel(
    action: KboxSyncAction,
): String {
    return when (action) {
        KboxSyncAction.UPLOAD -> "Upload"
        KboxSyncAction.DOWNLOAD -> "Download"
        KboxSyncAction.RELEASE_LOCAL -> "Release local"
        KboxSyncAction.KEEP_LOCAL -> "Keep local"
        KboxSyncAction.KEEP_REMOTE -> "Keep remote"
        KboxSyncAction.COMPARE_CONTENT -> "Compare content"
    }
}

private fun transferStatusLabel(
    status: KboxSyncTransferStatus,
): String {
    return when (status) {
        KboxSyncTransferStatus.QUEUED -> "Queued"
        KboxSyncTransferStatus.RUNNING -> "Running"
        KboxSyncTransferStatus.COMPLETED -> "Completed"
        KboxSyncTransferStatus.FAILED -> "Failed"
    }
}

private fun buildMetadataSummary(
    entry: KboxSyncEntry,
): String {
    return buildString {
        appendLine("Local: ${entry.localFile?.absolutePath ?: "-"}")
        appendLine("Local md5: ${entry.localFile?.md5 ?: "-"}")
        appendLine("Local size: ${entry.localFile?.sizeBytes ?: 0}")
        appendLine("Remote: ${entry.remoteFile?.absolutePath ?: "-"}")
        appendLine("Remote md5: ${entry.remoteFile?.md5 ?: "-"}")
        append("Remote size: ${entry.remoteFile?.sizeBytes ?: 0}")
    }
}

private fun buildBinarySummary(
    localPath: String,
    localSize: Long,
    localMd5: String,
    remotePath: String,
    remoteSize: Long,
    remoteMd5: String,
): String {
    return buildString {
        appendLine("Binary preview is summarized only.")
        appendLine("Local path: ${localPath.ifBlank { "-" }}")
        appendLine("Local size: $localSize")
        appendLine("Local md5: ${localMd5.ifBlank { "-" }}")
        appendLine("Remote path: ${remotePath.ifBlank { "-" }}")
        appendLine("Remote size: $remoteSize")
        append("Remote md5: ${remoteMd5.ifBlank { "-" }}")
    }
}

private fun formatTime(
    millis: Long,
): String {
    return if (millis <= 0) {
        "-"
    } else {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(millis))
    }
}

private fun formatBytes(
    bytes: Long,
): String {
    if (bytes <= 0) {
        return "0 B"
    }
    if (bytes < 1024) {
        return "$bytes B"
    }
    val units = listOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var index = -1
    while (value >= 1024 && index < units.lastIndex) {
        value /= 1024
        index += 1
    }
    return String.format(Locale.US, "%.1f %s", value, units[index])
}
