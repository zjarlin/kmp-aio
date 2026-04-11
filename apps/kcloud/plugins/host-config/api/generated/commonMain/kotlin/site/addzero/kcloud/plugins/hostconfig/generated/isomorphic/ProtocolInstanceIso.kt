package site.addzero.kcloud.plugins.hostconfig.generated.isomorphic

import kotlinx.serialization.Serializable
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity
import site.addzero.kcloud.plugins.hostconfig.model.enums.TransportType

/**
 * 定义协议instance实体。
 */
@Serializable
data class ProtocolInstanceIso(
    val id: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val name: String = "",
    val pollingIntervalMs: Int = 0,
    val transportType: TransportType? = null,
    val host: String? = null,
    val tcpPort: Int? = null,
    val portName: String? = null,
    val baudRate: Int? = null,
    val dataBits: Int? = null,
    val stopBits: Int? = null,
    val parity: Parity? = null,
    val responseTimeoutMs: Int? = null,
    val protocolTemplate: ProtocolTemplateIso = ProtocolTemplateIso(),
    val projectLinks: List<ProjectProtocolIso> = emptyList(),
    val projects: List<ProjectIso> = emptyList(),
    val devices: List<DeviceIso> = emptyList()
)