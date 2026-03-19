package com.kcloud.features.quicktransfer.server

import com.kcloud.feature.KCloudServerFeature
import com.kcloud.features.quicktransfer.server.generated.springktor.registerGeneratedSpringRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single(binds = [KCloudServerFeature::class])
class QuickTransferServerFeature : KCloudServerFeature {
    override val featureId = "quick-transfer"
    override val order = 10

    override fun installHttp(routing: Routing) {
        routing.registerGeneratedSpringRoutes()
    }
}
