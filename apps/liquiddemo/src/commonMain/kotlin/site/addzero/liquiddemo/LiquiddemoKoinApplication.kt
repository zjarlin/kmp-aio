package site.addzero.liquiddemo

import org.koin.core.annotation.KoinApplication

@KoinApplication(
    modules = [LiquiddemoAppKoinModule::class],
)
object LiquiddemoKoinApplication
