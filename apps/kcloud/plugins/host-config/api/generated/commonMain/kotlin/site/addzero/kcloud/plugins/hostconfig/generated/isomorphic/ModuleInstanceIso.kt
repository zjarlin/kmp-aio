package site.addzero.kcloud.plugins.hostconfig.generated.isomorphic

import kotlinx.serialization.Serializable

/**
 * 定义模块instance实体。
 */
@Serializable
data class ModuleInstanceIso(
    val id: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val name: String = "",
    val sortIndex: Int = 0,
    val moduleTemplate: ModuleTemplateIso = ModuleTemplateIso(),
    val device: DeviceIso = DeviceIso(),
    val protocol: ProtocolInstanceIso = ProtocolInstanceIso()
)