package site.addzero.vibepocket.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module

@Module
@ComponentScan("site.addzero.vibepocket.service")
class VibepocketServerKoinModule

@KoinApplication(
    configurations = ["vibepocket"],
    modules = [VibepocketServerKoinModule::class],
)
object AppKoinApplication
