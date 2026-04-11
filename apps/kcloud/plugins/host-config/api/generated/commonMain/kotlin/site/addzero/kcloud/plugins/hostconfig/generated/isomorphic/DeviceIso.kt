package site.addzero.kcloud.plugins.hostconfig.generated.isomorphic

import kotlinx.serialization.Serializable
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder2
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder4
import site.addzero.kcloud.plugins.hostconfig.model.enums.FloatOrder

/**
 * 定义设备实体。
 */
@Serializable
data class DeviceIso(
    val id: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val name: String = "",
    val stationNo: Int = 0,
    val requestIntervalMs: Int? = null,
    val writeIntervalMs: Int? = null,
    val byteOrder2: ByteOrder2? = null,
    val byteOrder4: ByteOrder4? = null,
    val floatOrder: FloatOrder? = null,
    val batchAnalogStart: Int? = null,
    val batchAnalogLength: Int? = null,
    val batchDigitalStart: Int? = null,
    val batchDigitalLength: Int? = null,
    val disabled: Boolean = false,
    val sortIndex: Int = 0,
    val deviceType: DeviceTypeIso = DeviceTypeIso(),
    val protocol: ProtocolInstanceIso = ProtocolInstanceIso(),
    val modules: List<ModuleInstanceIso> = emptyList(),
    val tags: List<TagIso> = emptyList()
)