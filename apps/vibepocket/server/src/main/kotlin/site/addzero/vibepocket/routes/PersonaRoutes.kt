package site.addzero.vibepocket.routes

import kotlinx.serialization.Serializable
import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.vibepocket.model.*
import site.addzero.vibepocket.model.by
import java.time.LocalDateTime

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

/**
 * Persona 管理相关路由
 */
@PostMapping("/api/personas")
suspend fun savePersona(
    @RequestBody request: PersonaSaveRequest,
): PersonaResponse {
    val entity = new(PersonaRecord::class).by {
        personaId = request.personaId
        name = request.name
        description = request.description
        createdAt = LocalDateTime.now()
    }
    val saved = sqlClient().save(entity)
    return saved.modifiedEntity.toPersonaResponse()
}

@GetMapping("/api/personas")
suspend fun listPersonas(): List<PersonaResponse> {
    return sqlClient()
        .createQuery(PersonaRecord::class) {
            select(table)
        }
        .execute()
        .sortedByDescending { it.createdAt }
        .map { it.toPersonaResponse() }
}

private fun PersonaRecord.toPersonaResponse() = PersonaResponse(
    id = id,
    personaId = personaId,
    name = name,
    description = description,
    createdAt = createdAt.toString(),
)

private fun sqlClient(): KSqlClient {
    return KoinPlatform.getKoin().get()
}
