package com.kcloud.features.servermanagement.server

import com.kcloud.feature.KCloudServerFeature
import com.kcloud.features.servermanagement.server.generated.springktor.registerGeneratedSpringRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class ServerManagementServerFeature : KCloudServerFeature {
    override val featureId = "server-management"
    override val order = 20

    override fun installHttp(routing: Routing) {
        routing.registerGeneratedSpringRoutes()
    }
}
