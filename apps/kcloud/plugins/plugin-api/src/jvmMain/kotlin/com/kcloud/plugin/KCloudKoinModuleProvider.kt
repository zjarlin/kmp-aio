package com.kcloud.plugin

import org.koin.core.module.Module

interface KCloudKoinModuleProvider {
    fun modules(): List<Module>
}
