package com.kcloud.features.ai.server.runtime

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module
@Configuration("kcloud")
@ComponentScan("com.kcloud.features.ai.server.runtime")
class AiServerFeatureKoinModule
