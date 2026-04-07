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

    fun refresh() {
        viewModelScope.launch {
            loadSnapshot(
                refreshProbes = true,
                showLoading = true,
            )
        }
    }

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

    fun selectAutoProbe() {
        screenState = screenState.copy(
            useAutoProbeSelection = true,
            selectedProbeSerialNumber = null,
            errorMessage = null,
        )
    }

    fun selectProbe(
        serialNumber: String,
    ) {
        screenState = screenState.copy(
            useAutoProbeSelection = false,
            selectedProbeSerialNumber = serialNumber,
            errorMessage = null,
        )
    }

    fun updateFirmwarePath(
        firmwarePath: String,
    ) {
        screenState = screenState.copy(
            firmwarePath = firmwarePath,
            errorMessage = null,
        )
    }

    fun updateStartAddressInput(
        startAddressInput: String,
    ) {
        screenState = screenState.copy(
            startAddressInput = startAddressInput,
            errorMessage = null,
        )
    }

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

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }

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

private fun Long.toHexAddress(): String {
    return "0x${toString(16).uppercase().padStart(8, '0')}"
}

private fun readableMessage(
    throwable: Throwable,
): String {
    return throwable.message?.trim().takeIf { !it.isNullOrBlank() }
        ?: throwable.toString()
}
