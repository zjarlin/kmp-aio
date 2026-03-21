package com.kcloud.features.packages.server

import com.kcloud.feature.KCloudServerFeature
import com.kcloud.features.packages.server.generated.springktor.registerGeneratedSpringRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class PackageOrganizerServerFeature : KCloudServerFeature {
    override val featureId = "package-organizer"
    override val order = 45

    override fun installHttp(routing: Routing) {
        routing.registerGeneratedSpringRoutes()
    }
}
