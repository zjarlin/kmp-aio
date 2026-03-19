package com.kcloud.features.transferhistory.server

import com.kcloud.feature.KCloudServerFeature
import com.kcloud.features.transferhistory.server.generated.springktor.registerGeneratedSpringRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single(binds = [KCloudServerFeature::class])
class TransferHistoryServerFeature : KCloudServerFeature {
    override val featureId = "transfer-history"
    override val order = 40

    override fun installHttp(routing: Routing) {
        routing.registerGeneratedSpringRoutes()
    }
}
