package com.kcloud.features.dotfiles.server

import com.kcloud.feature.KCloudServerFeature
import com.kcloud.features.dotfiles.server.generated.springktor.registerGeneratedSpringRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single
class DotfilesServerFeature : KCloudServerFeature {
    override val featureId = "dotfiles"
    override val order = 80

    override fun installHttp(routing: Routing) {
        routing.registerGeneratedSpringRoutes()
    }
}
