package site.addzero.kcloud.plugins.mcuconsole

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class McuDeviceProfileIso(
    val id: Long = 0L,
    @Contextual val createTime: Instant = kotlinx.datetime.Clock.System.now(),
    @Contextual val updateTime: Instant? = null,
    val deviceKey: String = "",
    val serialNumber: String? = null,
    val manufacturer: String? = null,
    val vendorId: Int? = null,
    val productId: Int? = null,
    val remark: String? = null,
    val lastPortPath: String? = null,
    val lastPortName: String? = null,
    @Contextual val lastSeenAt: Instant? = null
)