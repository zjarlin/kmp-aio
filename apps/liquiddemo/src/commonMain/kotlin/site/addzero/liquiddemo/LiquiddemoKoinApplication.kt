package site.addzero.liquiddemo

import org.koin.core.annotation.KoinApplication

@KoinApplication(
    configurations = ["liquiddemo"],
    modules = [LiquiddemoAppKoinModule::class],
)
object LiquiddemoKoinApplication
