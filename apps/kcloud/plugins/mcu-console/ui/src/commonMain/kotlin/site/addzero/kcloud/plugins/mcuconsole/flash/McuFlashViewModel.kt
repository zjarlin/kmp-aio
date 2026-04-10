package site.addzero.kcloud.plugins.mcuconsole.flash

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.plugins.mcuconsole.network.McuFlashRemoteService

/**
 * 管理 MCU 烧录界面的状态与交互逻辑。
 *
 * @property flashRemoteService 烧录远程服务。
 */
@KoinViewModel
class McuFlashViewModel(
    private val flashRemoteService: McuFlashRemoteService,
) : ViewModel() {
    var screenState by mutableStateOf(McuFlashScreenState())
        private set

    private var pollingJob: Job? = null

    init {
        refresh()
    }

    /**
     * 刷新当前界面数据。
     */
    fun refresh() {
        viewModelScope.launch {
            loadSnapshot(
                refreshProbes = true,
                showLoading = true,
            )
        }
    }

    /**
     * 刷新探针列表。
     */
    fun refreshProbes() {
        viewModelScope.launch {
            val previous = screenState
            val result = runCatching {
                flashRemoteService.listProbes()
            }
            val probes = result.getOrElse { previous.probes }
            screenState = reconcileSelections(
                previous = previous,
                candidate = previous.copy(
                    probes = probes,
                    probeMessage = buildProbeMessage(
                        probes = probes,
                        error = result.exceptionOrNull(),
                    ),
                ),
            )
        }
    }

    /**
     * 刷新当前任务状态。
     */
    fun refreshStatus() {
        viewModelScope.launch {
            val previous = screenState
            runCatching {
                flashRemoteService.getStatus()
            }.onSuccess { status ->
                screenState = previous.copy(
                    status = status,
                    noticeMessage = buildNoticeMessage(
                        status = status,
                        profiles = previous.profiles,
                        probes = previous.probes,
                    ),
                    errorMessage = null,
                )
                syncPolling(status)
            }.onFailure { throwable ->
                screenState = previous.copy(
                    errorMessage = "任务状态刷新失败: ${readableMessage(throwable)}",
                )
            }
        }
    }

    /**
     * 选择配置档。
     *
     * @param profileId 配置档 ID。
     */
    fun selectProfile(
        profileId: String,
    ) {
        val previous = screenState
        val previousDefault = previous.selectedProfile?.defaultStartAddress?.toHexAddress()
        val nextProfile = previous.profiles.firstOrNull { it.id == profileId } ?: return
        val nextDefault = nextProfile.defaultStartAddress.toHexAddress()
        val nextStartAddress = when {
            previous.startAddressInput.isBlank() -> nextDefault
            previous.startAddressInput == previousDefault -> nextDefault
            else -> previous.startAddressInput
        }
        screenState = previous.copy(
            selectedProfileId = nextProfile.id,
            startAddressInput = nextStartAddress,
            errorMessage = null,
        )
    }

    /**
     * 选择自动探针。
     */
    fun selectAutoProbe() {
        screenState = screenState.copy(
            useAutoProbeSelection = true,
            selectedProbeSerialNumber = null,
            errorMessage = null,
        )
    }

    /**
     * 选择探针。
     *
     * @param serialNumber 序列号。
     */
    fun selectProbe(
        serialNumber: String,
    ) {
        screenState = screenState.copy(
            useAutoProbeSelection = false,
            selectedProbeSerialNumber = serialNumber,
            errorMessage = null,
        )
    }

    /**
     * 更新固件路径。
     *
     * @param firmwarePath 固件路径。
     */
    fun updateFirmwarePath(
        firmwarePath: String,
    ) {
        screenState = screenState.copy(
            firmwarePath = firmwarePath,
            errorMessage = null,
        )
    }

    /**
     * 更新起始地址输入值。
     *
     * @param startAddressInput 起始地址输入值。
     */
    fun updateStartAddressInput(
        startAddressInput: String,
    ) {
        screenState = screenState.copy(
            startAddressInput = startAddressInput,
            errorMessage = null,
        )
    }

    /**
     * 启动烧录任务。
     */
    fun startFlash() {
        val request = buildFlashRequest() ?: return
        viewModelScope.launch {
            val previous = screenState
            screenState = previous.copy(
                busy = true,
                errorMessage = null,
                noticeMessage = "正在提交 ST-Link 烧录任务...",
            )
            runCatching {
                flashRemoteService.startFlash(request)
            }.onSuccess { status ->
                screenState = screenState.copy(
                    busy = false,
                    status = status,
                    noticeMessage = buildNoticeMessage(
                        status = status,
                        profiles = screenState.profiles,
                        probes = screenState.probes,
                    ),
                    errorMessage = null,
                )
                syncPolling(status)
            }.onFailure { throwable ->
                screenState = screenState.copy(
                    busy = false,
                    errorMessage = "启动烧录失败: ${readableMessage(throwable)}",
                )
            }
        }
    }

    /**
     * 重置目标。
     */
    fun resetTarget() {
        viewModelScope.launch {
            val previous = screenState
            screenState = previous.copy(
                busy = true,
                errorMessage = null,
                noticeMessage = "正在发送 ST-Link 复位脉冲...",
            )
            runCatching {
                flashRemoteService.reset(
                    McuFlashResetRequest(
                        profileId = screenState.selectedProfile?.id,
                        probeSerialNumber = if (screenState.useAutoProbeSelection) {
                            null
                        } else {
                            screenState.selectedProbeSerialNumber
                        },
                    ),
                )
            }.onSuccess { status ->
                screenState = screenState.copy(
                    busy = false,
                    status = status,
                    noticeMessage = buildNoticeMessage(
                        status = status,
                        profiles = screenState.profiles,
                        probes = screenState.probes,
                    ),
                    errorMessage = null,
                )
                syncPolling(status)
            }.onFailure { throwable ->
                screenState = screenState.copy(
                    busy = false,
                    errorMessage = "发送复位失败: ${readableMessage(throwable)}",
                )
            }
        }
    }

    /**
     * 在视图模型销毁时清理后台任务。
     */
    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }

    /**
     * 并发加载界面初始化快照。
     *
     * @param refreshProbes 是否同步刷新探针列表。
     * @param showLoading 是否显示加载状态。
     */
    private suspend fun loadSnapshot(
        refreshProbes: Boolean,
        showLoading: Boolean,
    ) = coroutineScope {
        val previous = screenState
        if (showLoading) {
            screenState = previous.copy(
                loading = true,
                errorMessage = null,
            )
        }
        val profilesDeferred = async {
            runCatching {
                flashRemoteService.listProfiles()
            }
        }
        val statusDeferred = async {
            runCatching {
                flashRemoteService.getStatus()
            }
        }
        val probesDeferred = async {
            if (refreshProbes) {
                runCatching {
                    flashRemoteService.listProbes()
                }
            } else {
                Result.success(previous.probes)
            }
        }

        val profilesResult = profilesDeferred.await()
        val statusResult = statusDeferred.await()
        val probesResult = probesDeferred.await()
        val profiles = profilesResult.getOrElse { previous.profiles }
        val probes = probesResult.getOrElse { previous.probes }
        val status = statusResult.getOrElse { previous.status }
        screenState = reconcileSelections(
            previous = previous,
            candidate = previous.copy(
                loading = false,
                profiles = profiles,
                probes = probes,
                status = status,
                noticeMessage = buildNoticeMessage(
                    status = status,
                    profiles = profiles,
                    probes = probes,
                ),
                errorMessage = buildLoadError(
                    profileError = profilesResult.exceptionOrNull(),
                    statusError = statusResult.exceptionOrNull(),
                ),
                probeMessage = buildProbeMessage(
                    probes = probes,
                    error = probesResult.exceptionOrNull(),
                ),
            ),
        )
        syncPolling(status)
    }

    /**
     * 根据任务状态同步轮询协程。
     *
     * @param status 当前任务状态。
     */
    private fun syncPolling(
        status: McuFlashStatusResponse,
    ) {
        pollingJob?.cancel()
        if (status.state != McuFlashRunState.RUNNING) {
            return
        }
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                runCatching {
                    flashRemoteService.getStatus()
                }.onSuccess { latest ->
                    screenState = screenState.copy(
                        status = latest,
                        noticeMessage = buildNoticeMessage(
                            status = latest,
                            profiles = screenState.profiles,
                            probes = screenState.probes,
                        ),
                        errorMessage = null,
                    )
                    if (latest.state != McuFlashRunState.RUNNING) {
                        cancel()
                    }
                }.onFailure { throwable ->
                    screenState = screenState.copy(
                        errorMessage = "状态轮询失败: ${readableMessage(throwable)}",
                    )
                    cancel()
                }
            }
        }
    }

    /**
     * 构建烧录请求。
     */
    private fun buildFlashRequest(): McuFlashRequest? {
        val current = screenState
        val profile = current.selectedProfile
        if (profile == null) {
            screenState = current.copy(
                errorMessage = "请先选择烧录配置",
            )
            return null
        }
        val firmwarePath = current.firmwarePath.trim()
        if (firmwarePath.isBlank()) {
            screenState = current.copy(
                errorMessage = "请填写固件文件路径",
            )
            return null
        }
        val startAddressText = current.startAddressInput.trim()
        val startAddress = when {
            startAddressText.isBlank() -> null
            else -> parseAddress(startAddressText)
        }
        if (startAddressText.isNotBlank() && startAddress == null) {
            screenState = current.copy(
                errorMessage = "起始地址格式无效，请使用 0x08000000 或十进制地址",
            )
            return null
        }
        return McuFlashRequest(
            profileId = profile.id,
            firmwarePath = firmwarePath,
            probeSerialNumber = if (current.useAutoProbeSelection) {
                null
            } else {
                current.selectedProbeSerialNumber
            },
            startAddress = startAddress,
        )
    }

    /**
     * 对齐当前配置档与探针的选中状态。
     *
     * @param previous 上一次状态。
     * @param candidate 候选状态。
     */
    private fun reconcileSelections(
        previous: McuFlashScreenState,
        candidate: McuFlashScreenState,
    ): McuFlashScreenState {
        val previousDefault = previous.selectedProfile?.defaultStartAddress?.toHexAddress()
        val resolvedProfile = candidate.selectedProfile
            ?: candidate.status.profileId?.let { profileId ->
                candidate.profiles.firstOrNull { it.id == profileId }
            }
            ?: candidate.profiles.firstOrNull()
        val resolvedDefault = resolvedProfile?.defaultStartAddress?.toHexAddress().orEmpty()
        val nextStartAddress = when {
            candidate.startAddressInput.isBlank() -> resolvedDefault
            candidate.startAddressInput == previousDefault -> resolvedDefault
            else -> candidate.startAddressInput
        }
        val useAutoProbeSelection = when {
            candidate.useAutoProbeSelection -> true
            candidate.selectedProbeSerialNumber == null -> true
            else -> candidate.probes.any { it.serialNumber == candidate.selectedProbeSerialNumber }.not()
        }
        return candidate.copy(
            selectedProfileId = resolvedProfile?.id,
            useAutoProbeSelection = useAutoProbeSelection,
            selectedProbeSerialNumber = if (useAutoProbeSelection) {
                null
            } else {
                candidate.selectedProbeSerialNumber
            },
            startAddressInput = nextStartAddress,
        )
    }
}

/**
 * 构建加载失败提示。
 *
 * @param profileError 配置档加载异常。
 * @param statusError 状态加载异常。
 */
private fun buildLoadError(
    profileError: Throwable?,
    statusError: Throwable?,
): String? {
    val parts = buildList {
        profileError?.let {
            add("烧录配置加载失败: ${readableMessage(it)}")
        }
        statusError?.let {
            add("任务状态加载失败: ${readableMessage(it)}")
        }
    }
    return parts.takeIf { it.isNotEmpty() }?.joinToString("；")
}

/**
 * 构建探针提示消息。
 *
 * @param probes 探针列表。
 * @param error 异常对象。
 */
private fun buildProbeMessage(
    probes: List<McuFlashProbeSummary>,
    error: Throwable?,
): String {
    error?.let {
        return "ST-Link 探针刷新失败: ${readableMessage(it)}"
    }
    if (probes.isEmpty()) {
        return "暂未发现 ST-Link 探针，请检查 USB 连接、驱动和目标板供电。"
    }
    return "已发现 ${probes.size} 个 ST-Link 探针。"
}

/**
 * 构建界面提示消息。
 *
 * @param status 当前任务状态。
 * @param profiles 配置档列表。
 * @param probes 探针列表。
 */
private fun buildNoticeMessage(
    status: McuFlashStatusResponse,
    profiles: List<McuFlashProfileSummary>,
    probes: List<McuFlashProbeSummary>,
): String {
    val explicitMessage = status.lastMessage?.trim().orEmpty()
    if (explicitMessage.isNotEmpty()) {
        return explicitMessage
    }
    return when (status.state) {
        McuFlashRunState.RUNNING -> "烧录任务正在执行。"
        McuFlashRunState.COMPLETED -> "上次 ST-Link 烧录已完成。"
        McuFlashRunState.ERROR -> "上次烧录任务执行失败。"
        McuFlashRunState.IDLE -> {
            when {
                profiles.isEmpty() -> "后台暂未提供可用烧录配置。"
                probes.isEmpty() -> "页面已就绪，连接 ST-Link 后即可开始烧录。"
                else -> "页面已就绪，可开始 ST-Link SWD 烧录。"
            }
        }
    }
}

/**
 * 解析地址输入值。
 *
 * @param value 待解析的值。
 */
private fun parseAddress(
    value: String,
): Long? {
    val normalized = value.trim()
    if (normalized.isBlank()) {
        return null
    }
    return when {
        normalized.startsWith("0x", ignoreCase = true) -> {
            normalized.removePrefix("0x").removePrefix("0X").toLongOrNull(16)
        }

        normalized.all { it.isDigit() } -> normalized.toLongOrNull()
        else -> normalized.toLongOrNull(16)
    }
}

/**
 * 处理long。
 */
private fun Long.toHexAddress(): String {
    return "0x${toString(16).uppercase().padStart(8, '0')}"
}

/**
 * 提取可读错误消息。
 *
 * @param throwable 异常对象。
 */
private fun readableMessage(
    throwable: Throwable,
): String {
    return throwable.message?.trim().takeIf { !it.isNullOrBlank() }
        ?: throwable.toString()
}
