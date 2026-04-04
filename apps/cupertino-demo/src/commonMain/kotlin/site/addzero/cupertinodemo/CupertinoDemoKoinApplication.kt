package site.addzero.cupertinodemo

import org.koin.core.annotation.KoinApplication

@KoinApplication(
    modules = [CupertinoDemoKoinModule::class],
)
object CupertinoDemoKoinApplication
