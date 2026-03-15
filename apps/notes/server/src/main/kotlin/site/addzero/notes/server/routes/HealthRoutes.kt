package site.addzero.notes.server.routes

import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondText
import org.springframework.web.bind.annotation.GetMapping

@GetMapping("/health")
suspend fun readHealth(call: ApplicationCall) {
    call.respondText("ok")
}
