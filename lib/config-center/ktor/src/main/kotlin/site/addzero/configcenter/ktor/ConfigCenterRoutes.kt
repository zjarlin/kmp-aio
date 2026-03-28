package site.addzero.configcenter.ktor

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import site.addzero.configcenter.ktor.routes.createConfigCenterEntry
import site.addzero.configcenter.ktor.routes.deleteConfigCenterEntry
import site.addzero.configcenter.ktor.routes.deleteConfigCenterTarget
import site.addzero.configcenter.ktor.routes.exportConfigCenterTarget
import site.addzero.configcenter.ktor.routes.getBootstrapValue
import site.addzero.configcenter.ktor.routes.getConfigCenterEntry
import site.addzero.configcenter.ktor.routes.getConfigCenterEnv
import site.addzero.configcenter.ktor.routes.getConfigCenterSnapshot
import site.addzero.configcenter.ktor.routes.getConfigCenterTarget
import site.addzero.configcenter.ktor.routes.listConfigCenterEntries
import site.addzero.configcenter.ktor.routes.listConfigCenterTargets
import site.addzero.configcenter.ktor.routes.previewConfigCenterTarget
import site.addzero.configcenter.ktor.routes.saveConfigCenterTarget
import site.addzero.configcenter.ktor.routes.updateConfigCenterEntry
import site.addzero.springktor.runtime.optionalRequestParam
import site.addzero.springktor.runtime.requirePathVariable
import site.addzero.springktor.runtime.requireRequestBody

/**
 * 统一挂载配置中心路由。
 */
fun Route.configCenterRoutes() {
    get("/api/config-center/env") {
        call.respond(
            getConfigCenterEnv(
                key = call.optionalRequestParam("key").orEmpty(),
                namespace = call.optionalRequestParam("namespace"),
                profile = call.optionalRequestParam("profile"),
                domain = call.optionalRequestParam("domain"),
            ),
        )
    }
    get("/api/config-center/snapshot") {
        call.respond(
            getConfigCenterSnapshot(
                namespace = call.optionalRequestParam("namespace"),
                profile = call.optionalRequestParam("profile"),
            ),
        )
    }
    get("/api/config-center/entries") {
        call.respond(
            listConfigCenterEntries(
                namespace = call.optionalRequestParam("namespace"),
                domain = call.optionalRequestParam("domain"),
                profile = call.optionalRequestParam("profile"),
                keyword = call.optionalRequestParam("keyword"),
                includeDisabled = call.optionalRequestParam("includeDisabled"),
            ),
        )
    }
    get("/api/config-center/entries/{id}") {
        call.respond(getConfigCenterEntry(call.requirePathVariable("id")))
    }
    post("/api/config-center/entries") {
        call.respond(createConfigCenterEntry(call.requireRequestBody()))
    }
    put("/api/config-center/entries/{id}") {
        call.respond(
            updateConfigCenterEntry(
                id = call.requirePathVariable("id"),
                request = call.requireRequestBody(),
            ),
        )
    }
    delete("/api/config-center/entries/{id}") {
        call.respond(deleteConfigCenterEntry(call.requirePathVariable("id")))
    }
    get("/api/config-center/targets") {
        call.respond(listConfigCenterTargets())
    }
    get("/api/config-center/targets/{id}") {
        call.respond(getConfigCenterTarget(call.requirePathVariable("id")))
    }
    post("/api/config-center/targets") {
        call.respond(saveConfigCenterTarget(call.requireRequestBody()))
    }
    delete("/api/config-center/targets/{id}") {
        call.respond(deleteConfigCenterTarget(call.requirePathVariable("id")))
    }
    post("/api/config-center/render/{targetId}/preview") {
        call.respond(previewConfigCenterTarget(call.requirePathVariable("targetId")))
    }
    post("/api/config-center/render/{targetId}/export") {
        call.respond(exportConfigCenterTarget(call.requirePathVariable("targetId")))
    }
    get("/api/config-center/bootstrap/{key}") {
        call.respond(getBootstrapValue(call.requirePathVariable("key")))
    }
}

