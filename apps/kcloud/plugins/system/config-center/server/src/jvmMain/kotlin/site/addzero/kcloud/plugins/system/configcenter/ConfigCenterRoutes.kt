package site.addzero.kcloud.plugins.system.configcenter

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import org.koin.mp.KoinPlatform
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterConfigMutationRequest
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterEnvironmentMutationRequest
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterProjectMutationRequest
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterSecretMutationRequest
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterServiceTokenIssueRequest
import site.addzero.springktor.runtime.optionalRequestParam
import site.addzero.springktor.runtime.requirePathVariable
import site.addzero.springktor.runtime.requireRequestParam
import site.addzero.springktor.runtime.requireRequestBody

fun Route.configCenterRoutes() {
    get("/api/system/config-center/projects") {
        call.respond(configCenterService().listProjects())
    }
    post("/api/system/config-center/projects") {
        call.respond(configCenterService().createProject(call.requireRequestBody<ConfigCenterProjectMutationRequest>()))
    }
    put("/api/system/config-center/projects/{projectId}") {
        call.respond(
            configCenterService().updateProject(
                projectId = call.requirePathVariable("projectId"),
                request = call.requireRequestBody<ConfigCenterProjectMutationRequest>(),
            ),
        )
    }
    get("/api/system/config-center/projects/{projectId}/environments") {
        call.respond(
            configCenterService().listEnvironments(
                projectId = call.requirePathVariable("projectId"),
            ),
        )
    }
    post("/api/system/config-center/projects/{projectId}/environments") {
        call.respond(
            configCenterService().createEnvironment(
                projectId = call.requirePathVariable("projectId"),
                request = call.requireRequestBody<ConfigCenterEnvironmentMutationRequest>(),
            ),
        )
    }
    put("/api/system/config-center/projects/{projectId}/environments/{environmentId}") {
        call.respond(
            configCenterService().updateEnvironment(
                projectId = call.requirePathVariable("projectId"),
                environmentId = call.requirePathVariable("environmentId"),
                request = call.requireRequestBody<ConfigCenterEnvironmentMutationRequest>(),
            ),
        )
    }
    get("/api/system/config-center/projects/{projectId}/configs") {
        call.respond(
            configCenterService().listConfigs(
                projectId = call.requirePathVariable("projectId"),
            ),
        )
    }
    post("/api/system/config-center/projects/{projectId}/configs") {
        call.respond(
            configCenterService().createConfig(
                projectId = call.requirePathVariable("projectId"),
                request = call.requireRequestBody<ConfigCenterConfigMutationRequest>(),
            ),
        )
    }
    put("/api/system/config-center/configs/{configId}") {
        call.respond(
            configCenterService().updateConfig(
                configId = call.requirePathVariable("configId"),
                request = call.requireRequestBody<ConfigCenterConfigMutationRequest>(),
            ),
        )
    }
    get("/api/system/config-center/configs/{configId}/secrets") {
        call.respond(
            configCenterService().listSecrets(
                configId = call.requirePathVariable("configId"),
                includeInherited = call.optionalRequestParam<Boolean>("includeInherited") ?: true,
            ),
        )
    }
    post("/api/system/config-center/secrets") {
        call.respond(
            configCenterService().saveSecret(
                request = call.requireRequestBody<ConfigCenterSecretMutationRequest>(),
            ),
        )
    }
    put("/api/system/config-center/secrets/{secretId}") {
        call.respond(
            configCenterService().saveSecret(
                request = call.requireRequestBody<ConfigCenterSecretMutationRequest>(),
                secretId = call.requirePathVariable("secretId"),
            ),
        )
    }
    delete("/api/system/config-center/secrets/{secretId}") {
        configCenterService().deleteSecret(call.requirePathVariable("secretId"))
        call.respond(HttpStatusCode.OK)
    }
    get("/api/system/config-center/secrets/{secretId}/versions") {
        call.respond(
            configCenterService().listSecretVersions(
                secretId = call.requirePathVariable("secretId"),
            ),
        )
    }
    get("/api/system/config-center/configs/{configId}/tokens") {
        call.respond(
            configCenterService().listServiceTokens(
                configId = call.requirePathVariable("configId"),
            ),
        )
    }
    post("/api/system/config-center/tokens") {
        call.respond(
            configCenterService().issueServiceToken(
                request = call.requireRequestBody<ConfigCenterServiceTokenIssueRequest>(),
            ),
        )
    }
    post("/api/system/config-center/tokens/{tokenId}/revoke") {
        call.respond(
            configCenterService().revokeServiceToken(
                tokenId = call.requirePathVariable("tokenId"),
            ),
        )
    }
    get("/api/system/config-center/projects/{projectId}/activities") {
        call.respond(
            configCenterService().listActivityLogs(
                projectId = call.requirePathVariable("projectId"),
                limit = call.optionalRequestParam<Int>("limit") ?: 50,
            ),
        )
    }
    get("/api/system/config-center/compat/value") {
        call.respond(
            configCenterService().readCompatValue(
                namespace = call.requireRequestParam("namespace"),
                key = call.requireRequestParam("key"),
                profile = call.optionalRequestParam<String>("profile") ?: "default",
            ),
        )
    }
}

private fun configCenterService(): ConfigCenterService {
    return KoinPlatform.getKoin().get()
}
