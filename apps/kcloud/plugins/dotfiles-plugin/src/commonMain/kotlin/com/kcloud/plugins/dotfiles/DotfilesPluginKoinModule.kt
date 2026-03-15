package com.kcloud.plugins.dotfiles

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module
@Configuration("kcloud")
@ComponentScan("com.kcloud.plugins.dotfiles")
class DotfilesPluginKoinModule
