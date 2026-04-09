package site.addzero.kcloud.plugins.hostconfig.gateway

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectGatewayPinConfigRequest
import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectModbusServerConfigRequest
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.GatewayConfigApi
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.ProjectApi
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

@KoinViewModel
/**
 * 管理网关界面的状态与交互逻辑。
 *
 * @property projectApi 项目API。
 * @property gatewayConfigApi 网关配置API。
 */
class GatewayViewModel(
    private val projectApi: ProjectApi,
    private val gatewayConfigApi: GatewayConfigApi,
) : ViewModel() {
    var screenState by mutableStateOf(GatewayScreenState())
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
     * 选择传输。
     *
     * @param transportType 传输类型。
     */
    fun selectTransport(
        transportType: TransportType,
    ) {
        screenState = screenState.copy(
            selectedTransport = transportType,
        )
    }

    /**
     * 保存配置。
     *
     * @param transportType 传输类型。
     * @param request 请求参数。
     */
    fun saveConfig(
        transportType: TransportType,
        request: ProjectModbusServerConfigRequest,
    ) {
        val projectId = screenState.selectedProjectId ?: return
        viewModelScope.launch {
            screenState = screenState.copy(
                busy = true,
                errorMessage = null,
            )
            runCatching {
                val saved = gatewayConfigApi.updateModbusServerConfig(projectId, transportType, request)
                screenState = if (transportType == TransportType.TCP) {
                    screenState.copy(
                        busy = false,
                        tcpConfig = saved,
                        noticeMessage = "TCP 配置已保存",
                    )
                } else {
                    screenState.copy(
                        busy = false,
                        rtuConfig = saved,
                        noticeMessage = "RTU 配置已保存",
                    )
                }
            }.onFailure { throwable ->
                screenState = screenState.copy(
                    busy = false,
                    errorMessage = throwable.message ?: "保存网关配置失败",
                )
            }
        }
    }

    /**
     * 保存pin配置。
     *
     * @param request 请求参数。
     */
    fun savePinConfig(
        request: ProjectGatewayPinConfigRequest,
    ) {
        val projectId = screenState.selectedProjectId ?: return
        viewModelScope.launch {
            screenState = screenState.copy(
                busy = true,
                errorMessage = null,
            )
            runCatching {
                val saved = gatewayConfigApi.updateGatewayPinConfig(projectId, request)
                screenState = screenState.copy(
                    busy = false,
                    pinConfig = saved,
                    noticeMessage = "下位机引脚配置已保存",
                )
            }.onFailure { throwable ->
                screenState = screenState.copy(
                    busy = false,
                    errorMessage = throwable.message ?: "保存下位机引脚配置失败",
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
                screenState = GatewayScreenState(
                    loading = false,
                )
                return
            }
            val selectedProjectId = preferredProjectId
                ?.takeIf { candidate -> projects.any { project -> project.id == candidate } }
                ?: projects.first().id
            val pinConfig = gatewayConfigApi.getGatewayPinConfig(selectedProjectId)
            val tcpConfig = gatewayConfigApi.getModbusServerConfig(selectedProjectId, TransportType.TCP)
            val rtuConfig = gatewayConfigApi.getModbusServerConfig(selectedProjectId, TransportType.RTU)
            screenState = GatewayScreenState(
                loading = false,
                projects = projects,
                selectedProjectId = selectedProjectId,
                selectedTransport = screenState.selectedTransport,
                pinConfig = pinConfig,
                tcpConfig = tcpConfig,
                rtuConfig = rtuConfig,
                noticeMessage = screenState.noticeMessage,
            )
        }.onFailure { throwable ->
            screenState = screenState.copy(
                loading = false,
                busy = false,
                errorMessage = throwable.message ?: "加载网关配置失败",
            )
        }
    }
}
