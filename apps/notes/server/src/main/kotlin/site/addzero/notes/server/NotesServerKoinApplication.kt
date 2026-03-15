package site.addzero.notes.server

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module

@Module
@Configuration("kcloud")
@ComponentScan("site.addzero.notes.server.store", "site.addzero.notes.server.routes")
class NotesServerKoinModule

@KoinApplication(modules = [NotesServerKoinModule::class])
object NotesServerKoinApplication
