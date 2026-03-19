package com.kcloud.features.ssh.server

import com.kcloud.feature.KCloudServerFeature
import com.kcloud.features.ssh.server.generated.springktor.registerGeneratedSpringRoutes
import io.ktor.server.routing.Routing
import org.koin.core.annotation.Single

@Single(binds = [KCloudServerFeature::class])
class SshServerFeature : KCloudServerFeature {
    override val featureId = "ssh"
    override val order = 60

    override fun installHttp(routing: Routing) {
        routing.registerGeneratedSpringRoutes()
    }
}
