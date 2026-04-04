package site.addzero.fluentdemo

import org.koin.core.annotation.KoinApplication

@KoinApplication(
    modules = [FluentDemoKoinModule::class],
)
object FluentDemoKoinApplication
