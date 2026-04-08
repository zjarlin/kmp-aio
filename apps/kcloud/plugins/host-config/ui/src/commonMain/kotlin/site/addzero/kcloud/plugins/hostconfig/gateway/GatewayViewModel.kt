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
import site.addzero.kcloud.plugins.hostconfig.api.external.GatewayConfigApi
import site.addzero.kcloud.plugins.hostconfig.api.external.ProjectApi
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

@KoinViewModel
class GatewayViewModel(
    private val projectApi: ProjectApi,
    private val gatewayConfigApi: GatewayConfigApi,
) : ViewModel() {
    var screenState by mutableStateOf(GatewayScreenState())
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

    fun selectTransport(
        transportType: TransportType,
    ) {
        screenState = screenState.copy(
            selectedTransport = transportType,
        )
    }

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
