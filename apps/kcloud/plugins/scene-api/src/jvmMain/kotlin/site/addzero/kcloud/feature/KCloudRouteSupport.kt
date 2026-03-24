package site.addzero.kcloud.feature

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.mp.KoinPlatform

interface KCloudRouteRegistrar {
    val order: Int
        get() = 0

    fun register(route: Route)
}

@Single
class KCloudSceneIndexRouteRegistrar(
    private val scenePlugins: List<KCloudScenePlugin>,
) : KCloudRouteRegistrar {
    override fun register(route: Route) {
        route.get("/api/scenes") {
            val payload = scenePlugins
                .sortedWith(compareBy<KCloudScenePlugin> { it.sort }.thenBy { it.displayName })
                .map { plugin ->
                    KCloudSceneDto(
                        sceneId = plugin.sceneId,
                        displayName = plugin.displayName,
                        sort = plugin.sort,
                    )
                }
            call.respond(ApiEnvelope(data = payload))
        }

        route.get("/api/scenes/{sceneId}") {
            val sceneId = call.parameters["sceneId"].orEmpty()
            val scene = scenePlugins.firstOrNull { plugin -> plugin.sceneId == sceneId }
            if (scene == null) {
                call.respond(
                    io.ktor.http.HttpStatusCode.NotFound,
                    ApiEnvelope(code = 404, data = emptyMap<String, String>(), msg = "Scene '$sceneId' not found"),
                )
                return@get
            }

            call.respond(
                ApiEnvelope(
                    data = KCloudSceneDto(
                        sceneId = scene.sceneId,
                        displayName = scene.displayName,
                        sort = scene.sort,
                    ),
                ),
            )
        }

        route.get("/api/scenes/{sceneId}/summary") {
            val sceneId = call.parameters["sceneId"].orEmpty()
            val scene = scenePlugins.firstOrNull { plugin -> plugin.sceneId == sceneId }
            if (scene == null) {
                call.respond(
                    io.ktor.http.HttpStatusCode.NotFound,
                    ApiEnvelope(code = 404, data = emptyMap<String, String>(), msg = "Scene '$sceneId' not found"),
                )
                return@get
            }

            call.respond(
                ApiEnvelope(
                    data = KCloudSceneSummary(
                        sceneId = scene.sceneId,
                        displayName = scene.displayName,
                        pages = scene.pages,
                    ),
                ),
            )
        }
    }
}

@Module
@Configuration("kcloud")
@ComponentScan("site.addzero.kcloud.feature")
class KCloudSceneApiKoinModule

fun Routing.registerKCloudRouteRegistrars(
    registrars: List<KCloudRouteRegistrar> = KoinPlatform.getKoin().getAll(),
) {
    registrars
        .sortedBy { registrar -> registrar.order }
        .forEach { registrar -> registrar.register(this) }
}
