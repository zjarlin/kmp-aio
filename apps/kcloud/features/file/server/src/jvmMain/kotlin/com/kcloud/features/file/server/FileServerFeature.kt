package com.kcloud.features.file.server

import com.kcloud.feature.KCloudServerFeature
import com.kcloud.features.file.server.generated.springktor.registerGeneratedSpringRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single(binds = [KCloudServerFeature::class])
class FileServerFeature : KCloudServerFeature {
    override val featureId = "file"
    override val order = 30

    override fun installHttp(routing: Routing) {
        routing.registerGeneratedSpringRoutes()
    }
}
