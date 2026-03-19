package com.kcloud.features.ai.server.runtime

import com.kcloud.features.ai.server.AiServerFeatureCommonKoinModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module(includes = [AiServerFeatureCommonKoinModule::class])
@Configuration("kcloud")
@ComponentScan("com.kcloud.features.ai.server.runtime")
class AiServerFeatureKoinModule
