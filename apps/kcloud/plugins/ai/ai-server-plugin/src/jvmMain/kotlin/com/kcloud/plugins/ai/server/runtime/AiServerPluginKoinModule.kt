package com.kcloud.plugins.ai.server.runtime

import com.kcloud.plugins.ai.server.AiServerPluginCommonKoinModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module(includes = [AiServerPluginCommonKoinModule::class])
@Configuration("kcloud")
@ComponentScan("com.kcloud.plugins.ai.server.runtime")
class AiServerPluginKoinModule
