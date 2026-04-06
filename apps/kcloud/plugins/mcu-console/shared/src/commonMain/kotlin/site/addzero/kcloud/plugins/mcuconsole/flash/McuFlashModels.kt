package site.addzero.kcloud.plugins.mcuconsole.flash

import kotlinx.serialization.Serializable

@Serializable
enum class McuFlashRunState {
    IDLE,
    RUNNING,
    COMPLETED,
    ERROR,
}

@Serializable
data class McuFlashProfileSummary(
    val id: String,
    val title: String,
    val transport: String = "st-link-swd",
    val defaultStartAddress: Long = 0x0800_0000,
    val connectUnderReset: Boolean = true,
    val supportedChipIds: List<Int> = emptyList(),
    val artifactLabel: String = "firmware.bin",
    val artifactHint: String? = null,
)

@Serializable
data class McuFlashProfilesResponse(
    val items: List<McuFlashProfileSummary> = emptyList(),
)

@Serializable
data class McuFlashProbeSummary(
    val vendorId: Int,
    val productId: Int,
    val serialNumber: String? = null,
    val productName: String? = null,
    val manufacturerName: String? = null,
)

@Serializable
data class McuFlashProbesResponse(
    val items: List<McuFlashProbeSummary> = emptyList(),
)

@Serializable
data class McuFlashRequest(
    val profileId: String,
    val firmwarePath: String,
    val probeSerialNumber: String? = null,
    val startAddress: Long? = null,
)

@Serializable
data class McuFlashResetRequest(
    val profileId: String? = null,
    val probeSerialNumber: String? = null,
    val pulseMs: Int = 100,
)

@Serializable
data class McuFlashStatusResponse(
    val state: McuFlashRunState = McuFlashRunState.IDLE,
    val profileId: String? = null,
    val probeSerialNumber: String? = null,
    val probeDescription: String? = null,
    val targetChipId: Int? = null,
    val targetVoltageMillivolts: Int? = null,
    val flashStartAddress: Long? = null,
    val firmwarePath: String? = null,
    val bytesSent: Long = 0,
    val totalBytes: Long = 0,
    val progressPercent: Double = 0.0,
    val currentStage: String? = null,
    val verified: Boolean = false,
    val startedApplication: Boolean = false,
    val lastMessage: String? = null,
    val startedAt: String? = null,
    val finishedAt: String? = null,
    val updatedAt: String? = null,
)
