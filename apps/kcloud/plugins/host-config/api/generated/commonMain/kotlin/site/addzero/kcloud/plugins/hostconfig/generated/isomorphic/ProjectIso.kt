package site.addzero.kcloud.plugins.hostconfig.generated.isomorphic

import kotlinx.serialization.Serializable

/**
 * 定义项目实体。
 */
@Serializable
data class ProjectIso(
    val id: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val name: String = "",
    val description: String? = null,
    val remark: String? = null,
    val sortIndex: Int = 0,
    val protocolLinks: List<ProjectProtocolIso> = emptyList(),
    val protocols: List<ProtocolInstanceIso> = emptyList(),
    val mqttConfig: ProjectMqttConfigIso? = null,
    val modbusServerConfigs: List<ProjectModbusServerConfigIso> = emptyList()
)