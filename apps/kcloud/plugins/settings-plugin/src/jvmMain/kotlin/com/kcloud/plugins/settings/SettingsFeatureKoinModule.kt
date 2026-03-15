package com.kcloud.plugins.settings

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module(includes = [SettingsFeatureCommonKoinModule::class])
@Configuration("kcloud")
@ComponentScan("com.kcloud.plugins.settings.storage")
class SettingsFeatureKoinModule
