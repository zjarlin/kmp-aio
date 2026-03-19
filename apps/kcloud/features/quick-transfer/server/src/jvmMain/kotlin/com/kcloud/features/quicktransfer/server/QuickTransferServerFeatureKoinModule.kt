package com.kcloud.features.quicktransfer.server

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module
@Configuration("kcloud")
@ComponentScan("com.kcloud.features.quicktransfer.server")
class QuickTransferServerFeatureKoinModule
