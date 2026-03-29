package site.addzero.kcloud.vibepocket.routes

import kotlinx.serialization.Serializable

@Serializable
data class PersonaSaveRequest(
    val personaId: String,
    val name: String,
    val description: String,
)

@Serializable
data class PersonaResponse(
    val id: Long,
    val personaId: String,
    val name: String,
    val description: String,
    val createdAt: String? = null,
)
