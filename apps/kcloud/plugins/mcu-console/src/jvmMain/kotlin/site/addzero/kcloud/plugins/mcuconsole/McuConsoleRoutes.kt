package site.addzero.kcloud.plugins.mcuconsole

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.io.File
import java.time.Instant

fun Route.mcuConsoleRoutes() {
    route("/api/mcu") {
        get("/actions") {
            call.respond(
                McuConsoleActionsResponse(
                    items = mcuConsoleActionCatalog(),
                ),
            )
        }
        get("/ports") {
            call.respond(
                McuConsolePortsResponse(
                    items = scanLocalPorts(),
                ),
            )
        }
        post("/actions/{action}") {
            val action = call.parameters["action"].orEmpty()
            val accepted = mcuConsoleActionCatalog().any { item -> item.id == action }
            val target = call.request.queryParameters["target"]
                ?: call.request.uri
            val status = if (accepted) HttpStatusCode.OK else HttpStatusCode.NotFound
            call.respond(
                status = status,
                message = McuConsoleActionResponse(
                    action = action,
                    accepted = accepted,
                    target = target,
                    timestamp = Instant.now().toString(),
                ),
            )
        }
    }
}

private fun mcuConsoleActionCatalog(): List<McuConsoleActionMeta> {
    return listOf(
        McuConsoleActionMeta(id = "scan", group = "control"),
        McuConsoleActionMeta(id = "sync", group = "control"),
        McuConsoleActionMeta(id = "start", group = "control"),
        McuConsoleActionMeta(id = "stop", group = "control"),
        McuConsoleActionMeta(id = "power", group = "control"),
        McuConsoleActionMeta(id = "reset", group = "control"),
        McuConsoleActionMeta(id = "flash", group = "flash"),
        McuConsoleActionMeta(id = "upload", group = "flash"),
        McuConsoleActionMeta(id = "download", group = "flash"),
        McuConsoleActionMeta(id = "debug", group = "debug"),
        McuConsoleActionMeta(id = "config", group = "debug"),
        McuConsoleActionMeta(id = "tune", group = "debug"),
    )
}

private fun scanLocalPorts(): List<McuConsolePort> {
    val devDir = File("/dev")
    if (!devDir.exists()) {
        return emptyList()
    }
    return devDir.listFiles()
        .orEmpty()
        .asSequence()
        .filter { file ->
            file.name.startsWith("cu.")
                || file.name.startsWith("tty.")
                || file.name.startsWith("ttyUSB")
                || file.name.startsWith("ttyACM")
        }
        .sortedBy { file -> file.name }
        .map { file ->
            val kind = when {
                file.name.startsWith("cu.") -> "cu"
                file.name.startsWith("ttyUSB") -> "ttyUSB"
                file.name.startsWith("ttyACM") -> "ttyACM"
                else -> "tty"
            }
            McuConsolePort(
                name = file.name,
                path = file.absolutePath,
                kind = kind,
            )
        }
        .toList()
}
