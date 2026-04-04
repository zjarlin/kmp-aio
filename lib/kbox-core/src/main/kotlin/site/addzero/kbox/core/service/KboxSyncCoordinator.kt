package site.addzero.kbox.core.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxComparePreview
import site.addzero.kbox.core.model.KboxComparePreviewMode
import site.addzero.kbox.core.model.KboxCompareSidePreview
import site.addzero.kbox.core.model.KboxRemoteFileInfo
import site.addzero.kbox.core.model.KboxRemoteStorageGateway
import site.addzero.kbox.core.model.KboxSettings
import site.addzero.kbox.core.model.KboxSyncAction
import site.addzero.kbox.core.model.KboxSyncDecision
import site.addzero.kbox.core.model.KboxSyncEntry
import site.addzero.kbox.core.model.KboxSyncFileSnapshot
import site.addzero.kbox.core.model.KboxSyncIndexRecord
import site.addzero.kbox.core.model.KboxSyncMappingConfig
import site.addzero.kbox.core.model.KboxSyncRunState
import site.addzero.kbox.core.model.KboxSyncStatus
import site.addzero.kbox.core.model.KboxSyncTransferQueueState
import site.addzero.kbox.core.model.KboxSyncTransferStatus
import site.addzero.kbox.core.model.KboxSyncTransferTask
import site.addzero.kbox.core.support.KboxDefaults
import site.addzero.kbox.core.support.normalizeRelativePath
import site.addzero.kbox.core.support.stableShortHash
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_DELETE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.nio.file.StandardWatchEventKinds.OVERFLOW
import java.nio.file.WatchKey
import java.nio.file.WatchService

@Single
@OptIn(FlowPreview::class)
class KboxSyncCoordinator(
    private val gateway: KboxRemoteStorageGateway,
    private val localChecksumService: KboxLocalChecksumService,
    private val decisionEngine: KboxSyncDecisionEngine,
    private val indexStore: KboxSyncIndexStore,
    private val remotePathService: KboxRemotePathService,
    private val trashService: KboxTrashService,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val refreshSignals = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    private val refreshMutex = Mutex()
    private val transferMutex = Mutex()
    private val transferQueueLock = Any()
    private val activeTransfers = linkedMapOf<String, KboxSyncTransferTask>()
    private val recentTransfers = ArrayDeque<KboxSyncTransferTask>()

    private val _entries = MutableStateFlow<List<KboxSyncEntry>>(emptyList())
    val entries = _entries.asStateFlow()

    private val _runState = MutableStateFlow(KboxSyncRunState())
    val runState = _runState.asStateFlow()

    private val _transferQueue = MutableStateFlow(KboxSyncTransferQueueState())
    val transferQueue = _transferQueue.asStateFlow()

    private var currentSettings: KboxSettings? = null
    private var paused = false
    private var refreshCollectorJob: Job? = null
    private var remotePollJob: Job? = null
    private var localWatchJob: Job? = null
    private var localWatchService: WatchService? = null

    suspend fun start(
        settings: KboxSettings,
    ) {
        val normalized = KboxDefaults.normalize(settings)
        require(normalized.syncEnabled) {
            "Sync is disabled"
        }
        require(normalized.ssh.enabled) {
            "SSH must be enabled before sync can start"
        }
        require(normalized.syncMappings.any { mapping -> mapping.enabled }) {
            "Add at least one enabled sync mapping"
        }
        stopBackground()
        paused = false
        currentSettings = normalized
        indexStore.pruneMappings(normalized.syncMappings.map { mapping -> mapping.mappingId }.toSet())
        _runState.value = KboxSyncRunState(
            status = KboxSyncStatus.STARTING,
            activeMappingIds = normalized.syncMappings
                .filter { mapping -> mapping.enabled }
                .map { mapping -> mapping.mappingId }
                .toSet(),
            startedAtMillis = System.currentTimeMillis(),
        )
        startRefreshCollector()
        startRemotePolling(normalized)
        startLocalWatch(normalized)
        refreshSignals.tryEmit(Unit)
    }

    suspend fun pause() {
        paused = true
        stopBackground()
        _runState.value = _runState.value.copy(
            status = KboxSyncStatus.PAUSED,
        )
    }

    suspend fun stop() {
        paused = false
        currentSettings = null
        stopBackground()
        _runState.value = KboxSyncRunState(status = KboxSyncStatus.STOPPED)
    }

    suspend fun refreshNow() {
        if (paused) {
            currentSettings?.let { settings ->
                refreshSafely(
                    settings = settings,
                    autoApply = false,
                )
            }
            return
        }
        refreshSignals.emit(Unit)
    }

    suspend fun applyAction(
        mappingId: String,
        relativePath: String,
        action: KboxSyncAction,
    ) {
        val settings = currentSettings ?: error("Sync is not configured")
        val mapping = settings.syncMappings.firstOrNull { candidate -> candidate.mappingId == mappingId }
            ?: error("Unknown mapping: $mappingId")
        val entry = _entries.value.firstOrNull { candidate ->
            candidate.mappingId == mappingId && candidate.relativePath == relativePath
        } ?: refreshOneEntry(settings, mapping, relativePath)
            ?: error("Unable to find sync entry for $relativePath")
        runTrackedAction(
            settings = settings,
            mapping = mapping,
            entry = entry,
            action = action,
        )
        refreshSafely(
            settings = settings,
            autoApply = false,
        )
    }

    suspend fun releaseSuggestedLocalCopies(): Int {
        val settings = currentSettings ?: error("Sync is not configured")
        refreshSafely(
            settings = settings,
            autoApply = false,
        )
        val releasableEntries = _entries.value.filter { entry ->
            entry.decision == KboxSyncDecision.RELEASE_LOCAL &&
                entry.localFile != null &&
                entry.remoteFile != null
        }
        releasableEntries.forEach { entry ->
            val mapping = settings.syncMappings.first { candidate -> candidate.mappingId == entry.mappingId }
            runTrackedAction(
                settings = settings,
                mapping = mapping,
                entry = entry,
                action = KboxSyncAction.RELEASE_LOCAL,
            )
        }
        refreshSafely(
            settings = settings,
            autoApply = false,
        )
        return releasableEntries.size
    }

    suspend fun buildComparePreview(
        mappingId: String,
        relativePath: String,
        maxBytes: Int = 16 * 1024,
    ): KboxComparePreview {
        val settings = currentSettings ?: error("Sync is not configured")
        val mapping = settings.syncMappings.firstOrNull { candidate -> candidate.mappingId == mappingId }
            ?: error("Unknown mapping: $mappingId")
        val entry = _entries.value.firstOrNull { candidate ->
            candidate.mappingId == mappingId && candidate.relativePath == relativePath
        } ?: refreshOneEntry(settings, mapping, relativePath)
            ?: error("Unable to find sync entry for $relativePath")

        val localPreviewBytes = entry.localFile?.let { local ->
            localChecksumService.readPreview(File(local.absolutePath), maxBytes)
        } ?: ByteArray(0)
        val remotePreviewBytes = entry.remoteFile?.let { remote ->
            gateway.readPreview(remote.absolutePath, maxBytes, settings.ssh)
        } ?: ByteArray(0)
        val mode = resolvePreviewMode(entry.localFile, entry.remoteFile, localPreviewBytes, remotePreviewBytes)

        return KboxComparePreview(
            mappingId = mappingId,
            relativePath = relativePath,
            mode = mode,
            local = entry.localFile.toCompareSide(
                content = if (mode == KboxComparePreviewMode.TEXT) {
                    localPreviewBytes.toString(Charsets.UTF_8)
                } else {
                    ""
                },
            ),
            remote = entry.remoteFile.toCompareSide(
                content = if (mode == KboxComparePreviewMode.TEXT) {
                    remotePreviewBytes.toString(Charsets.UTF_8)
                } else {
                    ""
                },
            ),
            truncated = localPreviewBytes.size >= maxBytes || remotePreviewBytes.size >= maxBytes,
        )
    }

    private fun startRefreshCollector() {
        refreshCollectorJob = scope.launch {
            refreshSignals
                .debounce(750)
                .collectLatest {
                    currentSettings?.let { settings ->
                        refreshSafely(
                            settings = settings,
                            autoApply = true,
                        )
                    }
                }
        }
    }

    private fun startRemotePolling(
        settings: KboxSettings,
    ) {
        remotePollJob = scope.launch {
            while (true) {
                delay(settings.syncRemotePollSeconds * 1000L)
                refreshSignals.tryEmit(Unit)
            }
        }
    }

    private fun startLocalWatch(
        settings: KboxSettings,
    ) {
        val watchService = FileSystems.getDefault().newWatchService()
        localWatchService = watchService
        localWatchJob = scope.launch {
            val watchRoots = mutableMapOf<WatchKey, Path>()
            settings.syncMappings
                .filter { mapping -> mapping.enabled }
                .forEach { mapping ->
                    val root = File(mapping.localRoot).absoluteFile
                    root.mkdirs()
                    registerAllDirectories(root.toPath(), watchService, watchRoots)
                }
            while (true) {
                val key = watchService.take()
                val directory = watchRoots[key]
                if (directory == null) {
                    key.reset()
                    continue
                }
                key.pollEvents().forEach { event ->
                    if (event.kind() == OVERFLOW) {
                        return@forEach
                    }
                    val relative = event.context() as? Path
                    if (event.kind() == ENTRY_CREATE && relative != null) {
                        val created = directory.resolve(relative)
                        if (Files.isDirectory(created)) {
                            registerAllDirectories(created, watchService, watchRoots)
                        }
                    }
                    if (event.kind() == ENTRY_CREATE || event.kind() == ENTRY_DELETE || event.kind() == ENTRY_MODIFY) {
                        refreshSignals.tryEmit(Unit)
                    }
                }
                if (!key.reset()) {
                    watchRoots.remove(key)
                }
            }
        }
    }

    private suspend fun refreshInternal(
        settings: KboxSettings,
        autoApply: Boolean,
    ) {
        refreshMutex.withLock {
            _runState.value = _runState.value.copy(
                status = KboxSyncStatus.SCANNING,
                activeMappingIds = settings.syncMappings
                    .filter { mapping -> mapping.enabled }
                    .map { mapping -> mapping.mappingId }
                    .toSet(),
                lastError = "",
            )
            var scannedEntries = scanEntries(settings)
            val autoEntries = if (autoApply) {
                scannedEntries.filter { entry ->
                    entry.autoExecutable &&
                        (entry.decision == KboxSyncDecision.UPLOAD_TO_REMOTE ||
                            entry.decision == KboxSyncDecision.DOWNLOAD_TO_LOCAL)
                }
            } else {
                emptyList()
            }
            autoEntries.forEach { entry ->
                val mapping = settings.syncMappings.first { candidate -> candidate.mappingId == entry.mappingId }
                runTrackedAction(
                    settings = settings,
                    mapping = mapping,
                    entry = entry,
                    action = entry.recommendedAction ?: return@forEach,
                )
            }
            if (autoEntries.isNotEmpty()) {
                scannedEntries = scanEntries(settings)
            }
            _entries.value = scannedEntries
            _runState.value = _runState.value.copy(
                status = if (paused) KboxSyncStatus.PAUSED else KboxSyncStatus.RUNNING,
                lastRefreshAtMillis = System.currentTimeMillis(),
                lastError = "",
            )
        }
    }

    private suspend fun refreshSafely(
        settings: KboxSettings,
        autoApply: Boolean,
    ) {
        runCatching {
            refreshInternal(
                settings = settings,
                autoApply = autoApply,
            )
        }.onFailure { error ->
            _runState.value = _runState.value.copy(
                status = if (paused) KboxSyncStatus.PAUSED else KboxSyncStatus.ERROR,
                lastError = error.message ?: error::class.simpleName.orEmpty(),
            )
        }
    }

    private fun scanEntries(
        settings: KboxSettings,
    ): List<KboxSyncEntry> {
        return settings.syncMappings
            .filter { mapping -> mapping.enabled }
            .flatMap { mapping -> scanMappingEntries(settings, mapping) }
            .sortedWith(
                compareBy<KboxSyncEntry> { entry -> entry.mappingName }
                    .thenBy { entry -> decisionPriority(entry.decision) }
                    .thenBy { entry -> entry.relativePath },
            )
    }

    private fun scanMappingEntries(
        settings: KboxSettings,
        mapping: KboxSyncMappingConfig,
    ): List<KboxSyncEntry> {
        gateway.ensureDirectory(mapping.remoteRoot, settings.ssh)
        val previousRecords = indexStore.readMappingRecords(mapping.mappingId)
        val localFiles = localChecksumService.scanLocalFiles(mapping)
        val remoteFiles = gateway.listFiles(
            remoteRootAbsolutePath = remotePathService.normalizeRemotePath(mapping.remoteRoot),
            config = settings.ssh,
        ).associateBy { remote -> normalizeRelativePath(remote.relativePath) }
        val paths = linkedSetOf<String>()
        paths.addAll(previousRecords.keys)
        paths.addAll(localFiles.keys)
        paths.addAll(remoteFiles.keys)
        return paths.mapNotNull { relativePath ->
            val normalizedPath = normalizeRelativePath(relativePath)
            val local = localFiles[normalizedPath]
            val remote = remoteFiles[normalizedPath]
            val previous = previousRecords[normalizedPath]
            val outcome = decisionEngine.decide(local, remote, previous) ?: return@mapNotNull null
            KboxSyncEntry(
                mappingId = mapping.mappingId,
                mappingName = mapping.displayName,
                relativePath = normalizedPath,
                localFile = local,
                remoteFile = remote,
                decision = outcome.decision,
                recommendedAction = outcome.recommendedAction,
                reason = outcome.reason,
                lastSyncedAtMillis = previous?.syncedAtMillis ?: 0,
                autoExecutable = outcome.autoExecutable,
            )
        }
    }

    private fun refreshOneEntry(
        settings: KboxSettings,
        mapping: KboxSyncMappingConfig,
        relativePath: String,
    ): KboxSyncEntry? {
        return scanMappingEntries(settings, mapping)
            .firstOrNull { entry -> entry.relativePath == normalizeRelativePath(relativePath) }
    }

    private suspend fun runTrackedAction(
        settings: KboxSettings,
        mapping: KboxSyncMappingConfig,
        entry: KboxSyncEntry,
        action: KboxSyncAction,
    ) {
        val taskId = createTransferTaskId(mapping.mappingId, entry.relativePath, action)
        enqueueTransfer(
            KboxSyncTransferTask(
                taskId = taskId,
                mappingId = mapping.mappingId,
                mappingName = mapping.displayName,
                relativePath = entry.relativePath,
                action = action,
                totalBytes = resolveTransferBytes(entry, action),
                createdAtMillis = System.currentTimeMillis(),
                detail = transferDetail(action),
            ),
        )
        try {
            transferMutex.withLock {
                markTransferRunning(taskId)
                executeAction(
                    settings = settings,
                    mapping = mapping,
                    entry = entry,
                    action = action,
                    taskId = taskId,
                )
            }
            finishTransfer(
                taskId = taskId,
                status = KboxSyncTransferStatus.COMPLETED,
            )
        } catch (error: Throwable) {
            finishTransfer(
                taskId = taskId,
                status = KboxSyncTransferStatus.FAILED,
                error = error.message ?: error::class.simpleName.orEmpty(),
            )
            throw error
        }
    }

    private fun executeAction(
        settings: KboxSettings,
        mapping: KboxSyncMappingConfig,
        entry: KboxSyncEntry,
        action: KboxSyncAction,
        taskId: String,
    ) {
        val remoteAbsolutePath = remotePathService.remoteAbsolutePath(mapping, entry.relativePath)
        when (action) {
            KboxSyncAction.UPLOAD,
            KboxSyncAction.KEEP_LOCAL -> {
                val localFile = entry.localFile?.let { snapshot -> File(snapshot.absolutePath) }
                    ?: error("Local file is missing")
                gateway.uploadFile(
                    localFile = localFile,
                    remoteAbsolutePath = remoteAbsolutePath,
                    config = settings.ssh,
                    onProgress = { transferred, total ->
                        updateTransferProgress(taskId, transferred, total)
                    },
                )
                val localSnapshot = localChecksumService.snapshotFile(localFile, entry.relativePath)
                indexStore.upsert(localSnapshot.toIndexRecord(mapping.mappingId))
            }

            KboxSyncAction.DOWNLOAD,
            KboxSyncAction.KEEP_REMOTE -> {
                val remote = entry.remoteFile ?: error("Remote file is missing")
                val localFile = File(mapping.localRoot, entry.relativePath.replace('/', File.separatorChar))
                localFile.parentFile?.mkdirs()
                gateway.downloadFile(
                    remoteAbsolutePath = remote.absolutePath,
                    localFile = localFile,
                    config = settings.ssh,
                    onProgress = { transferred, total ->
                        updateTransferProgress(taskId, transferred, total)
                    },
                )
                val localSnapshot = localChecksumService.snapshotFile(localFile, entry.relativePath)
                indexStore.upsert(
                    KboxSyncIndexRecord(
                        mappingId = mapping.mappingId,
                        relativePath = entry.relativePath,
                        localMd5 = localSnapshot.md5,
                        remoteMd5 = remote.md5,
                        localSizeBytes = localSnapshot.sizeBytes,
                        remoteSizeBytes = remote.sizeBytes,
                        syncedAtMillis = System.currentTimeMillis(),
                        localReleased = false,
                    ),
                )
            }

            KboxSyncAction.RELEASE_LOCAL -> {
                val localFile = entry.localFile?.let { snapshot -> File(snapshot.absolutePath) }
                    ?: error("Local file is missing")
                check(entry.remoteFile != null) {
                    "Remote file must exist before local space can be released"
                }
                val totalBytes = entry.localFile.sizeBytes
                updateTransferProgress(taskId, 0, totalBytes)
                trashService.moveToTrash(localFile)
                updateTransferProgress(taskId, totalBytes, totalBytes)
                indexStore.upsert(
                    KboxSyncIndexRecord(
                        mappingId = mapping.mappingId,
                        relativePath = entry.relativePath,
                        localMd5 = entry.localFile.md5,
                        remoteMd5 = entry.remoteFile.md5,
                        localSizeBytes = entry.localFile.sizeBytes,
                        remoteSizeBytes = entry.remoteFile.sizeBytes,
                        syncedAtMillis = System.currentTimeMillis(),
                        localReleased = true,
                    ),
                )
            }

            KboxSyncAction.COMPARE_CONTENT -> Unit
        }
    }

    private suspend fun stopBackground() {
        refreshCollectorJob?.cancelAndJoin()
        refreshCollectorJob = null
        remotePollJob?.cancelAndJoin()
        remotePollJob = null
        localWatchService?.close()
        localWatchService = null
        localWatchJob?.cancelAndJoin()
        localWatchJob = null
    }

    private fun registerAllDirectories(
        startPath: Path,
        watchService: WatchService,
        watchRoots: MutableMap<WatchKey, Path>,
    ) {
        if (!Files.exists(startPath)) {
            Files.createDirectories(startPath)
        }
        Files.walk(startPath).use { paths ->
            paths.filter { candidate -> Files.isDirectory(candidate) }
                .forEach { directory ->
                    val key = directory.register(
                        watchService,
                        ENTRY_CREATE,
                        ENTRY_DELETE,
                        ENTRY_MODIFY,
                    )
                    watchRoots[key] = directory
                }
        }
    }

    private fun resolvePreviewMode(
        local: KboxSyncFileSnapshot?,
        remote: KboxRemoteFileInfo?,
        localPreviewBytes: ByteArray,
        remotePreviewBytes: ByteArray,
    ): KboxComparePreviewMode {
        return when {
            local == null && remote == null -> localChecksumService.previewMode(ByteArray(0))
            local == null -> localChecksumService.previewMode(remotePreviewBytes)
            remote == null -> localChecksumService.previewMode(localPreviewBytes)
            else -> {
                val localMode = localChecksumService.previewMode(localPreviewBytes)
                val remoteMode = localChecksumService.previewMode(remotePreviewBytes)
                if (localMode == remoteMode) localMode else KboxComparePreviewMode.BINARY
            }
        }
    }

    private fun decisionPriority(
        decision: KboxSyncDecision,
    ): Int {
        return when (decision) {
            KboxSyncDecision.COMPARE_CONTENT -> 0
            KboxSyncDecision.KEEP_LOCAL -> 1
            KboxSyncDecision.KEEP_REMOTE -> 2
            KboxSyncDecision.RELEASE_LOCAL -> 3
            KboxSyncDecision.UPLOAD_TO_REMOTE -> 4
            KboxSyncDecision.DOWNLOAD_TO_LOCAL -> 5
            KboxSyncDecision.MANUAL_REVIEW -> 6
        }
    }

    private fun enqueueTransfer(
        task: KboxSyncTransferTask,
    ) {
        synchronized(transferQueueLock) {
            activeTransfers[task.taskId] = task
            publishTransferQueueLocked()
        }
    }

    private fun markTransferRunning(
        taskId: String,
    ) {
        synchronized(transferQueueLock) {
            val current = activeTransfers[taskId] ?: return
            activeTransfers[taskId] = current.copy(
                status = KboxSyncTransferStatus.RUNNING,
                startedAtMillis = System.currentTimeMillis(),
            )
            publishTransferQueueLocked()
        }
    }

    private fun updateTransferProgress(
        taskId: String,
        transferred: Long,
        total: Long,
    ) {
        synchronized(transferQueueLock) {
            val current = activeTransfers[taskId] ?: return
            activeTransfers[taskId] = current.copy(
                bytesTransferred = transferred.coerceAtLeast(0),
                totalBytes = total.coerceAtLeast(current.totalBytes),
            )
            publishTransferQueueLocked()
        }
    }

    private fun finishTransfer(
        taskId: String,
        status: KboxSyncTransferStatus,
        error: String = "",
    ) {
        synchronized(transferQueueLock) {
            val current = activeTransfers.remove(taskId) ?: return
            val finishedTask = current.copy(
                status = status,
                bytesTransferred = when {
                    status == KboxSyncTransferStatus.COMPLETED && current.totalBytes > 0 -> current.totalBytes
                    else -> current.bytesTransferred
                },
                finishedAtMillis = System.currentTimeMillis(),
                error = error,
            )
            recentTransfers.addFirst(finishedTask)
            while (recentTransfers.size > RECENT_TRANSFER_LIMIT) {
                recentTransfers.removeLast()
            }
            publishTransferQueueLocked()
        }
    }

    private fun publishTransferQueueLocked() {
        _transferQueue.value = KboxSyncTransferQueueState(
            activeTasks = activeTransfers.values.toList(),
            recentTasks = recentTransfers.toList(),
            lastUpdatedAtMillis = System.currentTimeMillis(),
        )
    }

    private fun resolveTransferBytes(
        entry: KboxSyncEntry,
        action: KboxSyncAction,
    ): Long {
        return when (action) {
            KboxSyncAction.UPLOAD,
            KboxSyncAction.KEEP_LOCAL,
            KboxSyncAction.RELEASE_LOCAL -> entry.localFile?.sizeBytes ?: 0

            KboxSyncAction.DOWNLOAD,
            KboxSyncAction.KEEP_REMOTE -> entry.remoteFile?.sizeBytes ?: 0

            KboxSyncAction.COMPARE_CONTENT -> 0
        }
    }

    private fun transferDetail(
        action: KboxSyncAction,
    ): String {
        return when (action) {
            KboxSyncAction.UPLOAD -> "Uploading to remote"
            KboxSyncAction.DOWNLOAD -> "Downloading to local"
            KboxSyncAction.RELEASE_LOCAL -> "Releasing local space"
            KboxSyncAction.KEEP_LOCAL -> "Applying keep-local decision"
            KboxSyncAction.KEEP_REMOTE -> "Applying keep-remote decision"
            KboxSyncAction.COMPARE_CONTENT -> "Preparing compare content"
        }
    }

    private fun createTransferTaskId(
        mappingId: String,
        relativePath: String,
        action: KboxSyncAction,
    ): String {
        return stableShortHash("${System.nanoTime()}|$mappingId|$relativePath|${action.name}")
    }

    private fun KboxSyncFileSnapshot.toIndexRecord(
        mappingId: String,
    ): KboxSyncIndexRecord {
        return KboxSyncIndexRecord(
            mappingId = mappingId,
            relativePath = relativePath,
            localMd5 = md5,
            remoteMd5 = md5,
            localSizeBytes = sizeBytes,
            remoteSizeBytes = sizeBytes,
            syncedAtMillis = System.currentTimeMillis(),
            localReleased = false,
        )
    }

    private fun KboxSyncFileSnapshot?.toCompareSide(
        content: String,
    ): KboxCompareSidePreview {
        if (this == null) {
            return KboxCompareSidePreview()
        }
        return KboxCompareSidePreview(
            path = absolutePath,
            sizeBytes = sizeBytes,
            lastModifiedMillis = lastModifiedMillis,
            md5 = md5,
            present = true,
            content = content,
        )
    }

    private fun KboxRemoteFileInfo?.toCompareSide(
        content: String,
    ): KboxCompareSidePreview {
        if (this == null) {
            return KboxCompareSidePreview()
        }
        return KboxCompareSidePreview(
            path = absolutePath,
            sizeBytes = sizeBytes,
            lastModifiedMillis = lastModifiedMillis,
            md5 = md5,
            present = true,
            content = content,
        )
    }

    private companion object {
        const val RECENT_TRANSFER_LIMIT = 12
    }
}
