package com.kcloud.features.compose.server

import com.kcloud.feature.KCloudServerFeature
import com.kcloud.features.compose.server.generated.springktor.registerGeneratedSpringRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class ComposeServerFeature : KCloudServerFeature {
    override val featureId: String = "compose"
    override val order: Int = 25

    override fun installHttp(routing: Routing) {
        routing.registerGeneratedSpringRoutes()
    }
}
