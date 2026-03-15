package com.kcloud.plugins.quicktransfer

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module(includes = [QuickTransferPluginCommonKoinModule::class])
@Configuration("kcloud")
@ComponentScan("com.kcloud.plugins.quicktransfer.system")
class QuickTransferPluginKoinModule
