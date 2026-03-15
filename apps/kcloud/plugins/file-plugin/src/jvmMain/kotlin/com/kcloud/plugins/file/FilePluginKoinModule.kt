package com.kcloud.plugins.file

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module(includes = [FilePluginCommonKoinModule::class])
@Configuration("kcloud")
@ComponentScan("com.kcloud.plugins.file.desktop")
class FilePluginKoinModule
