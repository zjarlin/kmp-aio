package site.addzero.kcloud.plugins.mcuconsole.flash

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
