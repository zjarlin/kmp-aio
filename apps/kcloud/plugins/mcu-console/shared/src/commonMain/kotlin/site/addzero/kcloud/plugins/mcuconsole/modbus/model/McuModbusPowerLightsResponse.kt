package site.addzero.kcloud.plugins.mcuconsole.modbus.model

import kotlinx.serialization.Serializable

@Serializable
data class McuModbusPowerLightsResponse(
    val success: Boolean = false,
    val portPath: String? = null,
    val lights: List<Boolean> = emptyList(),
    val onCount: Int = 0,
    val lastMessage: String? = null,
    val updatedAt: String? = null,
)
