package com.kcloud

import com.kcloud.app.KCloudCoreKoinModule
import org.koin.core.annotation.KoinApplication

@KoinApplication(
    configurations = ["kcloud"],
    modules = [KCloudCoreKoinModule::class]
)
object KCloudKoinApplication
