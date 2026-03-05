package site.addzero.vibepocket.routes

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.ktor.ext.inject
import site.addzero.ioc.annotation.Bean
import site.addzero.vibepocket.jimmer.model.entity.PersonaRecord
import site.addzero.vibepocket.jimmer.model.entity.by
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
@Bean
fun Route.personaRoutes() {
    val sqlClient by inject<KSqlClient>()

    route("/api/personas") {

        post {
            val req = call.receive<PersonaSaveRequest>()
            val entity = new(PersonaRecord::class).by {
                personaId = req.personaId
                name = req.name
                description = req.description
                createdAt = LocalDateTime.now()
            }
            val saved = sqlClient.save(entity)
            call.respond(saved.modifiedEntity.toPersonaResponse())
        }

        get {
            val list = sqlClient.createQuery(PersonaRecord::class) {
                select(table)
            }.execute()
            call.respond(list.map { it.toPersonaResponse() })
        }
    }
}

private fun PersonaRecord.toPersonaResponse() = PersonaResponse(
    id = id,
    personaId = personaId,
    name = name,
    description = description,
    createdAt = createdAt.toString(),
)
