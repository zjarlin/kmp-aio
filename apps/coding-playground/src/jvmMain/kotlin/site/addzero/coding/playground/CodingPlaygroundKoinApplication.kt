package site.addzero.coding.playground

import org.koin.core.annotation.KoinApplication
import site.addzero.coding.playground.server.config.CodingPlaygroundServerKoinModule

@KoinApplication(
    configurations = ["coding-playground"],
    modules = [CodingPlaygroundAppKoinModule::class, CodingPlaygroundServerKoinModule::class],
)
object CodingPlaygroundKoinApplication
