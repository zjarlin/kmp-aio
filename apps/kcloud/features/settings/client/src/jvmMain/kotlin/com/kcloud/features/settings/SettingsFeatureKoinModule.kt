package com.kcloud.features.settings

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module(includes = [SettingsFeatureCommonKoinModule::class])
@Configuration("kcloud")
@ComponentScan("com.kcloud.features.settings.storage")
class SettingsFeatureKoinModule
