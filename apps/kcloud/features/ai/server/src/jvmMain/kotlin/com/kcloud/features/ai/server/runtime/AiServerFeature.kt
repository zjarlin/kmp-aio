package com.kcloud.features.ai.server.runtime

import com.kcloud.feature.KCloudServerFeature
import org.koin.core.annotation.Single

@Single(binds = [KCloudServerFeature::class])
class AiServerFeature : KCloudServerFeature {
    override val featureId = "ai"
    override val order = 105
}
