package site.addzero.kbox

import org.koin.core.annotation.KoinApplication
import site.addzero.kbox.app.KboxAppKoinModule
import site.addzero.kbox.core.KboxCoreKoinModule
import site.addzero.kbox.ssh.KboxSshKoinModule

@KoinApplication(
    modules = [
        KboxAppKoinModule::class,
        KboxCoreKoinModule::class,
        KboxSshKoinModule::class,
    ],
)
object KboxKoinApplication
