package site.addzero.kcloud.screens.creativeassets

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.api.suno.SunoTaskDetail
import site.addzero.kcloud.music.CreativeAssetsService
import site.addzero.kcloud.music.isFailedStatus
import site.addzero.kcloud.music.isRunningStatus
import site.addzero.kcloud.music.isSuccessStatus
import site.addzero.kcloud.music.matchesKeyword
import site.addzero.kcloud.music.replaceTaskResource
import site.addzero.kcloud.vibepocket.model.SunoTaskResourceItem

data class CreativeAssetsScreenState(
    val keyword: String = "",
    val items: List<SunoTaskResourceItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedTaskId: String? = null,
    val liveDetail: SunoTaskDetail? = null,
    val liveSyncMessage: String? = null,
    val isRefreshingLiveDetail: Boolean = false,
)

@KoinViewModel
class CreativeAssetsViewModel(
    private val creativeAssetsService: CreativeAssetsService,
) : ViewModel() {
    private var taskRefreshJob: Job? = null

    var state by mutableStateOf(CreativeAssetsScreenState())
        private set

    val filteredItems: List<SunoTaskResourceItem>
        get() {
            val normalizedKeyword = state.keyword.trim()
            if (normalizedKeyword.isBlank()) {
                return state.items
            }
            return state.items.filter { item -> item.matchesKeyword(normalizedKeyword) }
        }

    val selectedItem: SunoTaskResourceItem?
        get() = filteredItems.firstOrNull { it.taskId == state.selectedTaskId }
            ?: filteredItems.firstOrNull()

    val successCount: Int
        get() = state.items.count { it.isSuccessStatus() }

    val failedCount: Int
        get() = state.items.count { it.isFailedStatus() }

    val runningCount: Int
        get() = state.items.count { it.isRunningStatus() }

    init {
        refreshTaskResources()
    }

    fun updateKeyword(
        value: String,
    ) {
        state = state.copy(keyword = value)
        syncSelectionWithFilter()
    }

    fun selectTask(
        taskId: String,
    ) {
        updateSelectedTask(taskId)
    }

    fun refreshTaskResources() {
        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                errorMessage = null,
            )
            try {
                val mergedItems = creativeAssetsService.loadTaskResources { partial ->
                    state = state.copy(items = partial)
                }
                state = state.copy(items = mergedItems)
                syncSelectionWithFilter(forceRefreshCurrent = true)
            } catch (error: Exception) {
                state = state.copy(
                    errorMessage = creativeAssetsService.errorMessage(error),
                )
            } finally {
                state = state.copy(isLoading = false)
            }
        }
    }

    fun refreshSelectedTaskFromSuno() {
        val currentItem = selectedItem ?: return
        taskRefreshJob?.cancel()
        taskRefreshJob = viewModelScope.launch {
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
            .firstOrNull { item -> item.taskId == state.selectedTaskId }
            ?.taskId
        val desiredTaskId = visibleSelectedTaskId ?: filteredItems.firstOrNull()?.taskId

        if (forceRefreshCurrent && desiredTaskId != null && desiredTaskId == state.selectedTaskId) {
            refreshSelectedTaskFromSunoSilently()
            return
        }
        updateSelectedTask(desiredTaskId)
    }

    private fun updateSelectedTask(
        taskId: String?,
    ) {
        if (state.selectedTaskId == taskId) {
            return
        }

        state = state.copy(
            selectedTaskId = taskId,
            liveDetail = null,
            liveSyncMessage = null,
            isRefreshingLiveDetail = false,
        )

        taskRefreshJob?.cancel()
        val currentItem = selectedItem ?: return
        taskRefreshJob = viewModelScope.launch {
            refreshTaskFromSuno(
                currentItem = currentItem,
                manual = false,
            )
        }
    }

    private fun refreshSelectedTaskFromSunoSilently() {
        val currentItem = selectedItem ?: return
        taskRefreshJob?.cancel()
        taskRefreshJob = viewModelScope.launch {
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
        if (state.selectedTaskId == requestedTaskId) {
            state = state.copy(
                isRefreshingLiveDetail = true,
                liveSyncMessage = if (manual) {
                    "正在按 taskId 查询 Suno..."
                } else {
                    "正在自动按 taskId 回查 Suno..."
                },
            )
        }

        try {
            val refreshedSnapshot = creativeAssetsService.refreshTaskSnapshotById(
                taskId = requestedTaskId,
                fallbackType = currentItem.type,
                requestJson = currentItem.requestJson,
            )
            refreshedSnapshot.archivedItem?.let { archivedItem ->
                state = state.copy(
                    items = state.items.replaceTaskResource(archivedItem),
                )
            }
            if (state.selectedTaskId == requestedTaskId) {
                state = state.copy(
                    liveDetail = refreshedSnapshot.detail,
                    liveSyncMessage = buildString {
                        append(if (manual) "已按 taskId 从 Suno 刷新：" else "已自动按 taskId 从 Suno 刷新：")
                        append(refreshedSnapshot.detail.displayStatus)
                        append("。")
                        append(refreshedSnapshot.archiveStatus)
                    },
                )
            }
        } catch (error: Exception) {
            if (state.selectedTaskId == requestedTaskId) {
                state = state.copy(
                    liveSyncMessage = buildString {
                        append(if (manual) "按 taskId 查询失败：" else "自动按 taskId 回查失败：")
                        append(creativeAssetsService.errorMessage(error))
                    },
                )
            }
        } finally {
            if (state.selectedTaskId == requestedTaskId) {
                state = state.copy(isRefreshingLiveDetail = false)
            }
        }
    }
}
