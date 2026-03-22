package site.addzero.coding.playground

import org.koin.core.annotation.KoinApplication

@KoinApplication(
    configurations = ["coding-playground"],
    modules = [CodingPlaygroundAppKoinModule::class],
)
object CodingPlaygroundKoinApplication
