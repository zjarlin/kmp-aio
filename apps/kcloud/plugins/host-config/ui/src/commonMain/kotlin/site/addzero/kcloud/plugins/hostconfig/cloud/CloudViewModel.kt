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
/**
 * 管理云接入界面的状态与交互逻辑。
 *
 * @property projectApi 项目API。
 * @property cloudAccessApi 云接入访问API。
 */
class CloudViewModel(
    private val projectApi: ProjectApi,
    private val cloudAccessApi: CloudAccessApi,
) : ViewModel() {
    var screenState by mutableStateOf(CloudScreenState())
        private set

    init {
        refresh()
    }

    /**
     * 处理clear提示。
     */
    fun clearNotice() {
        screenState = screenState.copy(
            noticeMessage = null,
            errorMessage = null,
        )
    }

    /**
     * 刷新当前界面数据。
     */
    fun refresh() {
        viewModelScope.launch {
            loadPage()
        }
    }

    /**
     * 选择项目。
     *
     * @param projectId 项目 ID。
     */
    fun selectProject(
        projectId: Long,
    ) {
        viewModelScope.launch {
            loadPage(preferredProjectId = projectId)
        }
    }

    /**
     * 保存配置。
     *
     * @param request 请求参数。
     */
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

    /**
     * 加载分页。
     *
     * @param preferredProjectId preferred项目 ID。
     */
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
