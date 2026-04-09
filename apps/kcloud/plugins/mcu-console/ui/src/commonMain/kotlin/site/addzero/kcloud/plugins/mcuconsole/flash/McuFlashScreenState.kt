package site.addzero.kcloud.plugins.mcuconsole.flash

/**
 * 表示mcu烧录界面状态。
 *
 * @property loading 加载状态。
 * @property busy 繁忙状态。
 * @property profiles 配置档。
 * @property probes 探针。
 * @property selectedProfileId 选中配置档 ID。
 * @property useAutoProbeSelection 使用自动探针selection。
 * @property selectedProbeSerialNumber 选中探针序列号。
 * @property firmwarePath 固件路径。
 * @property startAddressInput 起始地址输入值。
 * @property status 状态。
 * @property noticeMessage 提示消息。
 * @property errorMessage 错误消息。
 * @property probeMessage 探针提示消息。
 */
data class McuFlashScreenState(
    val loading: Boolean = true,
    val busy: Boolean = false,
    val profiles: List<McuFlashProfileSummary> = emptyList(),
    val probes: List<McuFlashProbeSummary> = emptyList(),
    val selectedProfileId: String? = null,
    val useAutoProbeSelection: Boolean = true,
    val selectedProbeSerialNumber: String? = null,
    val firmwarePath: String = "",
    val startAddressInput: String = "",
    val status: McuFlashStatusResponse = McuFlashStatusResponse(),
    val noticeMessage: String? = null,
    val errorMessage: String? = null,
    val probeMessage: String? = null,
) {
    val selectedProfile: McuFlashProfileSummary?
        get() = profiles.firstOrNull { it.id == selectedProfileId }

    val selectedProbe: McuFlashProbeSummary?
        get() {
            if (useAutoProbeSelection) {
                return null
            }
            return probes.firstOrNull { it.serialNumber == selectedProbeSerialNumber }
        }

    val running: Boolean
        get() = status.state == McuFlashRunState.RUNNING
}
