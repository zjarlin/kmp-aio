package site.addzero.kcloud.plugins.hostconfig.generated.isomorphic

import kotlinx.serialization.Serializable

/**
 * 定义register类型实体。
 */
@Serializable
data class RegisterTypeIso(
    val id: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val code: String = "",
    val name: String = "",
    val description: String? = null,
    val sortIndex: Int = 0
)