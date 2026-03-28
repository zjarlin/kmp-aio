package site.addzero.kcpi18ndemo

import org.koin.core.annotation.KoinApplication

@KoinApplication(
    modules = [KcpI18nDemoKoinModule::class],
)
object KcpI18nDemoKoinApplication
