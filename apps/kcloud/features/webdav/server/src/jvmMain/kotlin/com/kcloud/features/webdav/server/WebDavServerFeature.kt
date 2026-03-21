package com.kcloud.features.webdav.server

import com.kcloud.feature.KCloudServerFeature
import com.kcloud.features.webdav.server.generated.springktor.registerGeneratedSpringRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class WebDavServerFeature : KCloudServerFeature {
    override val featureId = "webdav"
    override val order = 70

    override fun installHttp(routing: Routing) {
        routing.registerGeneratedSpringRoutes()
    }
}
