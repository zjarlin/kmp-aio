package com.kcloud.app.routes

import com.kcloud.server.model.HealthResponse
import org.springframework.web.bind.annotation.GetMapping

@GetMapping("/api/health")
fun readHealth(): HealthResponse {
    return HealthResponse(
        status = "ok",
        version = "dev",
        timestamp = System.currentTimeMillis(),
    )
}
