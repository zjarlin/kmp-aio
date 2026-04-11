package site.addzero.kcloud.plugins.hostconfig.generated.isomorphic

import kotlinx.serialization.Serializable

/**
 * 定义项目协议实体。
 */
@Serializable
data class ProjectProtocolIso(
    val id: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val sortIndex: Int = 0,
    val project: ProjectIso = ProjectIso(),
    val protocol: ProtocolInstanceIso = ProtocolInstanceIso()
)