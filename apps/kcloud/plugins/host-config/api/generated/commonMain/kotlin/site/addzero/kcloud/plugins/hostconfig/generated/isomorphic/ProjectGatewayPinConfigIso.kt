package site.addzero.kcloud.plugins.hostconfig.generated.isomorphic

import kotlinx.serialization.Serializable

/**
 * 表示项目网关pin配置。
 */
@Serializable
data class ProjectGatewayPinConfigIso(
    val id: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val faultIndicatorPin: String = "",
    val runningIndicatorPin: String = "",
    val project: ProjectIso = ProjectIso()
)