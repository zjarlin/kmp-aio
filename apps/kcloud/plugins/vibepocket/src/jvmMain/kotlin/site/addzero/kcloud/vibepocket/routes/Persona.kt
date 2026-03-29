package site.addzero.kcloud.vibepocket.routes

import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.kcloud.vibepocket.model.PersonaRecord
import site.addzero.kcloud.vibepocket.model.PersonaRecordDraft
import java.time.LocalDateTime

@PostMapping("/api/personas")
suspend fun savePersona(
    @RequestBody request: PersonaSaveRequest,
): PersonaResponse {
    val entity = PersonaRecordDraft.`$`.produce {
        personaId = request.personaId
        name = request.name
        description = request.description
        createdAt = LocalDateTime.now()
    }
    return sqlClient().save(entity).modifiedEntity.toPersonaResponse()
}

@GetMapping("/api/personas")
suspend fun getPersonas(): List<PersonaResponse> {
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
