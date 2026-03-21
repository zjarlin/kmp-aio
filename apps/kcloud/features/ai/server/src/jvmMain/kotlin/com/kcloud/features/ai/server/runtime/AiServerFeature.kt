package com.kcloud.features.ai.server.runtime

import com.kcloud.feature.KCloudServerFeature
import org.koin.core.annotation.Single

@Single
class AiServerFeature : KCloudServerFeature {
    override val featureId = "ai"
    override val order = 105
}
