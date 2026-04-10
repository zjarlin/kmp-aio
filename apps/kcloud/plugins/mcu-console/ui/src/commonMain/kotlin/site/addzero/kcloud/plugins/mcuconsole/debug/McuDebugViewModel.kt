package site.addzero.kcloud.plugins.mcuconsole.debug

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
/**
 * 管理 MCU 调试串口日志页面状态。
 *
 * @property remoteService 串口日志远程服务。
 */
class McuDebugViewModel(
    private val remoteService: McuDebugRemoteService,
) : ViewModel() {
    var screenState by mutableStateOf(McuDebugScreenState())
        private set

    private var streamingJob: Job? = null

    init {
        refreshPorts()
    }

    /**
     * 刷新串口列表。
     */
    fun refreshPorts() {
        viewModelScope.launch {
            runCatching {
                remoteService.listPorts()
            }.onSuccess { ports ->
                val nextPortName =
                    when {
                        ports.any { it.systemPortPath == screenState.portName || it.systemPortName == screenState.portName } -> screenState.portName
                        ports.any { it.systemPortPath == "/dev/cu.usbserial-2140" } -> "/dev/cu.usbserial-2140"
                        ports.isNotEmpty() -> ports.first().systemPortPath.ifBlank { ports.first().systemPortName }
                        else -> screenState.portName
                    }
                screenState = screenState.copy(
                    ports = ports,
                    portName = nextPortName,
                    errorMessage = null,
                    noticeMessage = "已刷新串口列表，共 ${ports.size} 个端口。",
                )
            }.onFailure { throwable ->
                screenState = screenState.copy(
                    errorMessage = "读取串口列表失败：${throwable.readableMcuDebugMessage()}",
                )
            }
        }
    }

    /**
     * 更新端口名。
     */
    fun updatePortName(portName: String) {
        screenState = screenState.copy(
            portName = portName,
            errorMessage = null,
        )
    }

    /**
     * 更新波特率输入。
     */
    fun updateBaudRateInput(baudRateInput: String) {
        screenState = screenState.copy(
            baudRateInput = baudRateInput,
            errorMessage = null,
        )
    }

    /**
     * 选择已探测端口。
     */
    fun selectPort(descriptor: McuSerialPortDescriptor) {
        updatePortName(descriptor.systemPortPath.ifBlank { descriptor.systemPortName })
    }

    /**
     * 开始日志流。
     */
    fun startStreaming() {
        val config = screenState.toSerialPortConfigOrNull()
        if (config == null) {
            screenState = screenState.copy(
                errorMessage = "请先填写有效串口名和波特率",
            )
            return
        }

        streamingJob?.cancel()
        streamingJob =
            viewModelScope.launch {
                screenState = screenState.copy(
                    connecting = true,
                    streaming = true,
                    errorMessage = null,
                    noticeMessage = "正在连接串口日志流：${config.portName}",
                )
                runCatching {
                    remoteService.streamSerialLogs(config).collect { line ->
                        val mergedLogs = (screenState.logs + line).takeLast(MAX_LOG_LINES)
                        screenState = screenState.copy(
                            connecting = false,
                            streaming = true,
                            logs = mergedLogs,
                            noticeMessage = "正在接收 ${config.portName} 的实时日志",
                        )
                    }
                }.onFailure { throwable ->
                    screenState = screenState.copy(
                        connecting = false,
                        streaming = false,
                        errorMessage = "串口日志连接失败：${throwable.readableMcuDebugMessage()}",
                    )
                }
            }
    }

    /**
     * 停止日志流。
     */
    fun stopStreaming() {
        viewModelScope.launch {
            streamingJob?.cancelAndJoin()
            streamingJob = null
            screenState = screenState.copy(
                connecting = false,
                streaming = false,
                noticeMessage = "已停止串口日志流。",
            )
        }
    }

    /**
     * 清空日志。
     */
    fun clearLogs() {
        screenState = screenState.copy(
            logs = emptyList(),
            errorMessage = null,
            noticeMessage = "日志已清空。",
        )
    }

    override fun onCleared() {
        streamingJob?.cancel()
        super.onCleared()
    }
}

private const val MAX_LOG_LINES = 400
