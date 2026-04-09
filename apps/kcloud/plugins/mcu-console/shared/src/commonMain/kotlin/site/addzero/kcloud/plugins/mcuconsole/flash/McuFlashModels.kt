package site.addzero.kcloud.plugins.mcuconsole.flash

import kotlinx.serialization.Serializable

@Serializable
/**
 * 定义mcu烧录运行状态枚举。
 */
enum class McuFlashRunState {
    IDLE,
    RUNNING,
    COMPLETED,
    ERROR,
}

@Serializable
/**
 * 表示mcu烧录配置档摘要信息。
 *
 * @property id 主键 ID。
 * @property title title。
 * @property transport 传输。
 * @property defaultStartAddress 默认起始地址。
 * @property connectUnderReset connectunder重置。
 * @property supportedChipIds supportedchip ID 列表。
 * @property artifactLabel artifactlabel。
 * @property artifactHint artifacthint。
 */
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
/**
 * 表示mcu烧录配置档响应结果。
 *
 * @property items 条目列表。
 */
data class McuFlashProfilesResponse(
    val items: List<McuFlashProfileSummary> = emptyList(),
)

@Serializable
/**
 * 表示mcu烧录探针摘要信息。
 *
 * @property vendorId vendor ID。
 * @property productId product ID。
 * @property serialNumber 序列号。
 * @property productName product名称。
 * @property manufacturerName manufacturer名称。
 */
data class McuFlashProbeSummary(
    val vendorId: Int,
    val productId: Int,
    val serialNumber: String? = null,
    val productName: String? = null,
    val manufacturerName: String? = null,
)

@Serializable
/**
 * 表示mcu烧录探针响应结果。
 *
 * @property items 条目列表。
 */
data class McuFlashProbesResponse(
    val items: List<McuFlashProbeSummary> = emptyList(),
)

@Serializable
/**
 * 表示mcu烧录请求参数。
 *
 * @property profileId 配置档 ID。
 * @property firmwarePath 固件路径。
 * @property probeSerialNumber 探针序列号。
 * @property startAddress 起始地址。
 */
data class McuFlashRequest(
    val profileId: String,
    val firmwarePath: String,
    val probeSerialNumber: String? = null,
    val startAddress: Long? = null,
)

@Serializable
/**
 * 表示mcu烧录重置请求参数。
 *
 * @property profileId 配置档 ID。
 * @property probeSerialNumber 探针序列号。
 * @property pulseMs pulse毫秒。
 */
data class McuFlashResetRequest(
    val profileId: String? = null,
    val probeSerialNumber: String? = null,
    val pulseMs: Int = 100,
)

@Serializable
/**
 * 表示mcu烧录状态响应结果。
 *
 * @property state 运行状态。
 * @property profileId 配置档 ID。
 * @property probeSerialNumber 探针序列号。
 * @property probeDescription 探针描述。
 * @property targetChipId 目标芯片 ID。
 * @property targetVoltageMillivolts 目标电压（毫伏）。
 * @property flashStartAddress 烧录起始地址。
 * @property firmwarePath 固件路径。
 * @property bytesSent 已发送字节数。
 * @property totalBytes 总字节数。
 * @property progressPercent 进度百分比。
 * @property currentStage 当前阶段。
 * @property verified 是否校验通过。
 * @property startedApplication 是否已启动应用。
 * @property lastMessage 最近一条消息。
 * @property startedAt 开始时间。
 * @property finishedAt 结束时间。
 * @property updatedAt 更新时间戳。
 */
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
