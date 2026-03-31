package site.addzero.kcloud.vibepocket.model

import kotlinx.serialization.Serializable

@Serializable
data class PersonaSaveRequest(
    val personaId: String,
    val name: String,
    val description: String,
)

@Serializable
data class PersonaItem(
    val id: Long? = null,
    val personaId: String,
    val name: String,
    val description: String,
    val createdAt: String? = null,
)
