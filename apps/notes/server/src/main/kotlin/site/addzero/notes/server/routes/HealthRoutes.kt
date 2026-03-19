package site.addzero.notes.server.routes

import org.springframework.web.bind.annotation.GetMapping

@GetMapping("/health")
fun readHealth(): String {
    return "ok"
}
