package com.kcloud.features.environment.server

import com.kcloud.feature.KCloudServerFeature
import com.kcloud.features.environment.server.generated.springktor.registerGeneratedSpringRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class EnvironmentServerFeature : KCloudServerFeature {
    override val featureId = "environment"
    override val order = 90

    override fun installHttp(routing: Routing) {
        routing.registerGeneratedSpringRoutes()
    }
}
