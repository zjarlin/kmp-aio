package com.kcloud.plugins.quicktransfer.server

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module
@Configuration("kcloud")
@ComponentScan("com.kcloud.plugins.quicktransfer.server")
class QuickTransferServerPluginKoinModule
