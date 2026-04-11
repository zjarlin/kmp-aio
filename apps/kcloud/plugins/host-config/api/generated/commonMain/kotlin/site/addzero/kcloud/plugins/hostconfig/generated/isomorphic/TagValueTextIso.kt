package site.addzero.kcloud.plugins.hostconfig.generated.isomorphic

import kotlinx.serialization.Serializable

/**
 * 定义标签值text实体。
 */
@Serializable
data class TagValueTextIso(
    val id: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val rawValue: String = "",
    val displayText: String = "",
    val sortIndex: Int = 0,
    val tag: TagIso = TagIso()
)