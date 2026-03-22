package com.kcloud.features.quicktransfer

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module
@Configuration("kcloud")
@ComponentScan("com.kcloud.features.quicktransfer.system")
class QuickTransferFeatureKoinModule
