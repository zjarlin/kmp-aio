package com.kcloud.features.servermanagement.server

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module
@Configuration("kcloud")
@ComponentScan("com.kcloud.features.servermanagement.server")
class ServerManagementServerFeatureKoinModule
