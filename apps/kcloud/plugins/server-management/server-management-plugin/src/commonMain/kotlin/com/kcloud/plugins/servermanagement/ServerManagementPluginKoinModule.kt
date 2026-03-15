package com.kcloud.plugins.servermanagement

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module
@Configuration("kcloud")
@ComponentScan("com.kcloud.plugins.servermanagement")
class ServerManagementPluginKoinModule
