package site.addzero.vibepocket.model

import kotlinx.serialization.Serializable

// ══════════════════════════════════════════════════════════════
//  Persona 数据模型
// ══════════════════════════════════════════════════════════════

/** Persona 保存请求 */
@Serializable
data class PersonaSaveRequest(
    val personaId: String,
    val name: String,
    val description: String,
)

/** Persona 响应 */
@Serializable
data class PersonaItem(
    val id: Long? = null,
    val personaId: String,
    val name: String,
    val description: String,
    val createdAt: String? = null,
)
