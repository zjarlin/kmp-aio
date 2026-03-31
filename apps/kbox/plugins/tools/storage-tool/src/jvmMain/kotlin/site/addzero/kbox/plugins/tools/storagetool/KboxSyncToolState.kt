package site.addzero.kbox.plugins.tools.storagetool

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxComparePreview
import site.addzero.kbox.core.model.KboxSettings
import site.addzero.kbox.core.model.KboxSyncAction
import site.addzero.kbox.core.model.KboxSyncEntry
import site.addzero.kbox.core.model.KboxSyncMappingConfig
import site.addzero.kbox.core.model.KboxSyncRunState
import site.addzero.kbox.core.model.KboxSyncStatus
import site.addzero.kbox.core.model.KboxSyncTransferQueueState
import site.addzero.kbox.core.service.KboxSettingsRepository
import site.addzero.kbox.core.service.KboxSyncCoordinator

@Single
class KboxSyncToolState(
    private val settingsRepository: KboxSettingsRepository,
    private val syncCoordinator: KboxSyncCoordinator,
) {
    private val stateScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val mappings = mutableStateListOf<KboxSyncMappingConfig>()
    val syncEntries = mutableStateListOf<KboxSyncEntry>()

    var runState by mutableStateOf(KboxSyncRunState())
        private set
    var selectedMappingId by mutableStateOf("")
        private set
    var selectedEntryId by mutableStateOf("")
        private set
    var comparePreview by mutableStateOf<KboxComparePreview?>(null)
        private set
    var syncEnabled by mutableStateOf(false)
        private set
    var sshEnabled by mutableStateOf(false)
        private set
    var syncStartOnLaunch by mutableStateOf(true)
        private set
    var remotePollSeconds by mutableStateOf(30)
        private set
    var transferQueue by mutableStateOf(KboxSyncTransferQueueState())
        private set

    val hasEnabledMappings: Boolean
        get() = mappings.any { mapping -> mapping.enabled }

    val canStartSync: Boolean
        get() = syncEnabled && sshEnabled && hasEnabledMappings

    val releasableEntries: List<KboxSyncEntry>
        get() = syncEntries.filter { entry ->
            entry.decision == site.addzero.kbox.core.model.KboxSyncDecision.RELEASE_LOCAL &&
                entry.localFile != null &&
                entry.remoteFile != null
        }

    val releasableEntryCount: Int
        get() = releasableEntries.size

    val releasableBytes: Long
        get() = releasableEntries.sumOf { entry -> entry.localFile?.sizeBytes ?: 0L }

    val visibleEntries: List<KboxSyncEntry>
        get() = if (selectedMappingId.isBlank()) {
            syncEntries.toList()
        } else {
            syncEntries.filter { entry -> entry.mappingId == selectedMappingId }
        }

    val selectedEntry: KboxSyncEntry?
        get() = syncEntries.firstOrNull { entry -> entry.entryId == selectedEntryId }

    init {
        stateScope.launch {
            syncCoordinator.runState.collectLatest { latest ->
                runState = latest
            }
        }
        stateScope.launch {
            syncCoordinator.entries.collectLatest { latest ->
                syncEntries.replaceAll(latest)
                if (selectedMappingId.isBlank() || mappings.none { mapping -> mapping.mappingId == selectedMappingId }) {
                    selectedMappingId = mappings.firstOrNull()?.mappingId.orEmpty()
                }
                if (selectedEntryId.isNotBlank() && syncEntries.none { entry -> entry.entryId == selectedEntryId }) {
                    selectedEntryId = visibleEntries.firstOrNull()?.entryId.orEmpty()
                    comparePreview = null
                }
            }
        }
        stateScope.launch {
            syncCoordinator.transferQueue.collectLatest { latest ->
                transferQueue = latest
            }
        }
    }

    suspend fun load() {
        reloadSettings()
    }

    suspend fun reloadSettings() {
        val settings = withContext(Dispatchers.IO) {
            settingsRepository.load()
        }
        syncEnabled = settings.syncEnabled
        sshEnabled = settings.ssh.enabled
        syncStartOnLaunch = settings.syncStartOnLaunch
        remotePollSeconds = settings.syncRemotePollSeconds
        mappings.replaceAll(settings.syncMappings)
        if (selectedMappingId.isBlank() || mappings.none { mapping -> mapping.mappingId == selectedMappingId }) {
            selectedMappingId = mappings.firstOrNull()?.mappingId.orEmpty()
        }
    }

    suspend fun start() {
        val settings = withContext(Dispatchers.IO) {
            settingsRepository.load()
        }
        reloadSettings()
        if (!canRunSync(settings)) {
            syncCoordinator.stop()
            return
        }
        syncCoordinator.start(settings)
    }

    suspend fun pause() {
        syncCoordinator.pause()
    }

    suspend fun refresh() {
        val settings = withContext(Dispatchers.IO) {
            settingsRepository.load()
        }
        reloadSettings()
        if (!canRunSync(settings)) {
            syncCoordinator.stop()
            return
        }
        if (runState.status == KboxSyncStatus.STOPPED || runState.status == KboxSyncStatus.ERROR) {
            syncCoordinator.start(settings)
            return
        }
        syncCoordinator.refreshNow()
    }

    suspend fun compare(
        entryId: String = selectedEntryId,
    ) {
        val entry = syncEntries.firstOrNull { candidate -> candidate.entryId == entryId }
            ?: error("Select a sync entry first")
        comparePreview = syncCoordinator.buildComparePreview(
            mappingId = entry.mappingId,
            relativePath = entry.relativePath,
        )
        selectedEntryId = entry.entryId
    }

    suspend fun applyAction(
        entryId: String,
        action: KboxSyncAction,
    ) {
        val entry = syncEntries.firstOrNull { candidate -> candidate.entryId == entryId }
            ?: error("Select a sync entry first")
        syncCoordinator.applyAction(
            mappingId = entry.mappingId,
            relativePath = entry.relativePath,
            action = action,
        )
        if (action != KboxSyncAction.COMPARE_CONTENT) {
            comparePreview = null
        }
    }

    suspend fun releaseReclaimableLocalCopies(): Int {
        val settings = withContext(Dispatchers.IO) {
            settingsRepository.load()
        }
        reloadSettings()
        if (!canRunSync(settings)) {
            syncCoordinator.stop()
            return 0
        }
        if (runState.status == KboxSyncStatus.STOPPED || runState.status == KboxSyncStatus.ERROR) {
            syncCoordinator.start(settings)
        } else if (runState.status != KboxSyncStatus.PAUSED) {
            syncCoordinator.refreshNow()
        }
        return syncCoordinator.releaseSuggestedLocalCopies()
    }

    fun selectMapping(
        mappingId: String,
    ) {
        selectedMappingId = mappingId
        if (selectedEntryId.isNotBlank() && visibleEntries.none { entry -> entry.entryId == selectedEntryId }) {
            selectedEntryId = visibleEntries.firstOrNull()?.entryId.orEmpty()
            comparePreview = null
        }
    }

    fun selectEntry(
        entryId: String,
    ) {
        selectedEntryId = entryId
    }

    fun clearComparePreview() {
        comparePreview = null
    }

    private fun <T> SnapshotStateList<T>.replaceAll(
        values: List<T>,
    ) {
        clear()
        addAll(values)
    }

    private fun canRunSync(
        settings: KboxSettings,
    ): Boolean {
        return settings.syncEnabled &&
            settings.ssh.enabled &&
            settings.syncMappings.any { mapping -> mapping.enabled }
    }
}
