package site.addzero.starter.statuspages

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.SerializationException

fun Application.installDefaultStatusPages() {
    install(StatusPages) {
        exception<HttpStatusException> { call, cause ->
            call.respond(cause.status, ErrorResponse(cause.status.value, cause.message))
            call.application.environment.log.warn("HTTP ${cause.status.value}: ${cause.message}", cause)
        }
        exception<NoSuchElementException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, ErrorResponse(404, cause.message ?: "Not Found"))
            call.application.environment.log.warn("Not Found: ${cause.message}", cause)
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(400, cause.message ?: "Bad Request"))
            call.application.environment.log.warn("Bad Request: ${cause.message}", cause)
        }
        exception<SerializationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(400, "Malformed JSON: ${cause.message}"))
            call.application.environment.log.error("Serialization error: ${cause.message}", cause)
        }
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(500, cause.message ?: "Internal Server Error"),
            )
            call.application.environment.log.error("Unhandled exception: ${cause.message}", cause)
        }
    }
}
