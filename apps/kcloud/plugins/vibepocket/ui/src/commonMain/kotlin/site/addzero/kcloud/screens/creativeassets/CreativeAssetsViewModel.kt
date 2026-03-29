package site.addzero.kcloud.screens.creativeassets

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import org.koin.core.annotation.Factory
import site.addzero.kcloud.api.ServerApiClient
import site.addzero.kcloud.api.suno.SunoTaskDetail
import site.addzero.kcloud.model.SunoTaskResourceItem
import site.addzero.kcloud.music.SunoRuntimeConfig
import site.addzero.kcloud.music.SunoWorkflowService
import site.addzero.kcloud.music.isFailedStatus
import site.addzero.kcloud.music.isRunningStatus
import site.addzero.kcloud.music.isSuccessStatus
import site.addzero.kcloud.music.matchesKeyword
import site.addzero.kcloud.music.reconcileTaskResourcesWithSuno
import site.addzero.kcloud.music.refreshSunoTaskSnapshotById
import site.addzero.kcloud.music.replaceTaskResource
import site.addzero.kcloud.music.toTaskResourceItem

@Factory
class CreativeAssetsViewModel {
    private val screenScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var taskRefreshJob: Job? = null

    var keyword by mutableStateOf("")
        private set

    var items by mutableStateOf<List<SunoTaskResourceItem>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var selectedTaskId by mutableStateOf<String?>(null)
        private set

    var liveDetail by mutableStateOf<SunoTaskDetail?>(null)
        private set

    var liveSyncMessage by mutableStateOf<String?>(null)
        private set

    var isRefreshingLiveDetail by mutableStateOf(false)
        private set

    val filteredItems: List<SunoTaskResourceItem>
        get() {
            val normalizedKeyword = keyword.trim()
            if (normalizedKeyword.isBlank()) {
                return items
            }
            return items.filter { item -> item.matchesKeyword(normalizedKeyword) }
        }

    val selectedItem: SunoTaskResourceItem?
        get() = filteredItems.firstOrNull { it.taskId == selectedTaskId }
            ?: filteredItems.firstOrNull()

    val successCount: Int
        get() = items.count { it.isSuccessStatus() }

    val failedCount: Int
        get() = items.count { it.isFailedStatus() }

    val runningCount: Int
        get() = items.count { it.isRunningStatus() }

    init {
        refreshTaskResources()
    }

    fun updateKeyword(
        value: String,
    ) {
        keyword = value
        syncSelectionWithFilter()
    }

    fun selectTask(
        taskId: String,
    ) {
        updateSelectedTask(taskId)
    }

    fun refreshTaskResources() {
        screenScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val loaded = ServerApiClient.sunoTaskResourceApi.list()
                    .map { response -> response.toTaskResourceItem() }
                val runtimeConfig = runCatching { SunoWorkflowService.loadConfig() }
                    .getOrDefault(SunoRuntimeConfig())
                items = if (runtimeConfig.hasToken) {
                    loaded.reconcileTaskResourcesWithSuno { partial ->
                        items = partial
                    }
                } else {
                    loaded
                }
                syncSelectionWithFilter(forceRefreshCurrent = true)
            } catch (error: Exception) {
                errorMessage = SunoWorkflowService.errorMessage(error)
            } finally {
                isLoading = false
            }
        }
    }

    fun refreshSelectedTaskFromSuno() {
        val currentItem = selectedItem ?: return
        taskRefreshJob?.cancel()
        taskRefreshJob = screenScope.launch {
            refreshTaskFromSuno(
                currentItem = currentItem,
                manual = true,
            )
        }
    }

    private fun syncSelectionWithFilter(
        forceRefreshCurrent: Boolean = false,
    ) {
        val visibleSelectedTaskId = filteredItems
            .firstOrNull { item -> item.taskId == selectedTaskId }
            ?.taskId
        val desiredTaskId = visibleSelectedTaskId ?: filteredItems.firstOrNull()?.taskId

        if (forceRefreshCurrent && desiredTaskId != null && desiredTaskId == selectedTaskId) {
            refreshSelectedTaskFromSunoSilently()
            return
        }
        updateSelectedTask(desiredTaskId)
    }

    private fun updateSelectedTask(
        taskId: String?,
    ) {
        if (selectedTaskId == taskId) {
            return
        }

        selectedTaskId = taskId
        liveDetail = null
        liveSyncMessage = null
        isRefreshingLiveDetail = false

        taskRefreshJob?.cancel()
        val currentItem = selectedItem ?: return
        taskRefreshJob = screenScope.launch {
            refreshTaskFromSuno(
                currentItem = currentItem,
                manual = false,
            )
        }
    }

    private fun refreshSelectedTaskFromSunoSilently() {
        val currentItem = selectedItem ?: return
        taskRefreshJob?.cancel()
        taskRefreshJob = screenScope.launch {
            refreshTaskFromSuno(
                currentItem = currentItem,
                manual = false,
            )
        }
    }

    private suspend fun refreshTaskFromSuno(
        currentItem: SunoTaskResourceItem,
        manual: Boolean,
    ) {
        val requestedTaskId = currentItem.taskId
        if (selectedTaskId == requestedTaskId) {
            isRefreshingLiveDetail = true
            liveSyncMessage = if (manual) {
                "正在按 taskId 查询 Suno..."
            } else {
                "正在自动按 taskId 回查 Suno..."
            }
        }

        try {
            val refreshedSnapshot = refreshSunoTaskSnapshotById(
                taskId = requestedTaskId,
                fallbackType = currentItem.type,
                requestJson = currentItem.requestJson,
            )
            refreshedSnapshot.archivedItem?.let { archivedItem ->
                items = items.replaceTaskResource(archivedItem)
            }
            if (selectedTaskId == requestedTaskId) {
                liveDetail = refreshedSnapshot.detail
                liveSyncMessage = buildString {
                    append(if (manual) "已按 taskId 从 Suno 刷新：" else "已自动按 taskId 从 Suno 刷新：")
                    append(refreshedSnapshot.detail.displayStatus)
                    append("。")
                    append(refreshedSnapshot.archiveStatus)
                }
            }
        } catch (error: Exception) {
            if (selectedTaskId == requestedTaskId) {
                liveSyncMessage = buildString {
                    append(if (manual) "按 taskId 查询失败：" else "自动按 taskId 回查失败：")
                    append(SunoWorkflowService.errorMessage(error))
                }
            }
        } finally {
            if (selectedTaskId == requestedTaskId) {
                isRefreshingLiveDetail = false
            }
        }
    }

    fun dispose() {
        taskRefreshJob?.cancel()
        screenScope.cancel()
    }
}
