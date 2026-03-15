package com.kcloud.plugins.environment.server

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module
@Configuration("kcloud")
@ComponentScan("com.kcloud.plugins.environment.server")
class EnvironmentServerPluginKoinModule
