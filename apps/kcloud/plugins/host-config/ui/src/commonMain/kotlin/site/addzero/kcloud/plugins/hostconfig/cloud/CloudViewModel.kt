package site.addzero.kcloud.plugins.hostconfig.cloud

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectMqttConfigRequest
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.CloudAccessApi
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.ProjectApi

@KoinViewModel
class CloudViewModel(
    private val projectApi: ProjectApi,
    private val cloudAccessApi: CloudAccessApi,
) : ViewModel() {
    var screenState by mutableStateOf(CloudScreenState())
        private set

    init {
        refresh()
    }

    fun clearNotice() {
        screenState = screenState.copy(
            noticeMessage = null,
            errorMessage = null,
        )
    }

    fun refresh() {
        viewModelScope.launch {
            loadPage()
        }
    }

    fun selectProject(
        projectId: Long,
    ) {
        viewModelScope.launch {
            loadPage(preferredProjectId = projectId)
        }
    }

    fun saveConfig(
        request: ProjectMqttConfigRequest,
    ) {
        val projectId = screenState.selectedProjectId ?: return
        viewModelScope.launch {
            screenState = screenState.copy(
                busy = true,
                errorMessage = null,
            )
            runCatching {
                val saved = cloudAccessApi.updateMqttConfig(projectId, request)
                screenState = screenState.copy(
                    busy = false,
                    mqttConfig = saved,
                    noticeMessage = "MQTT 配置已保存",
                )
            }.onFailure { throwable ->
                screenState = screenState.copy(
                    busy = false,
                    errorMessage = throwable.message ?: "保存 MQTT 配置失败",
                )
            }
        }
    }

    private suspend fun loadPage(
        preferredProjectId: Long? = screenState.selectedProjectId,
    ) {
        screenState = screenState.copy(
            loading = true,
            errorMessage = null,
        )
        runCatching {
            val projects = projectApi.listProjects()
            if (projects.isEmpty()) {
                screenState = CloudScreenState(
                    loading = false,
                    projects = emptyList(),
                )
                return
            }
            val selectedProjectId = preferredProjectId
                ?.takeIf { candidate -> projects.any { project -> project.id == candidate } }
                ?: projects.first().id
            val config = cloudAccessApi.getMqttConfig(selectedProjectId)
            screenState = CloudScreenState(
                loading = false,
                projects = projects,
                selectedProjectId = selectedProjectId,
                mqttConfig = config,
                noticeMessage = screenState.noticeMessage,
            )
        }.onFailure { throwable ->
            screenState = screenState.copy(
                loading = false,
                busy = false,
                errorMessage = throwable.message ?: "加载 MQTT 配置失败",
            )
        }
    }
}
