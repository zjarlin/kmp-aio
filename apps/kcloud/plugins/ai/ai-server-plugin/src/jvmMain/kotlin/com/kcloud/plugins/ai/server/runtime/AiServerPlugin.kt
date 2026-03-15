package com.kcloud.plugins.ai.server.runtime

import com.kcloud.plugin.KCloudServerPlugin
import org.koin.core.annotation.Single

@Single
class AiServerPlugin : KCloudServerPlugin {
    override val pluginId = "ai-server-plugin"
    override val order = 105
}
