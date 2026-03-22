package site.addzero.kcloud

import site.addzero.kcloud.app.KCloudCoreKoinModule
import org.koin.core.annotation.KoinApplication

@KoinApplication(
    configurations = ["kcloud"],
    modules = [KCloudCoreKoinModule::class]
)
object KCloudKoinApplication
