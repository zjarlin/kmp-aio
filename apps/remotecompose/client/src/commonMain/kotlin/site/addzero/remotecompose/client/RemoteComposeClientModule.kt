package site.addzero.remotecompose.client

import org.koin.core.module.Module
import org.koin.dsl.module

data class RemoteComposeClientConfig(
    val baseUrl: String,
)

interface RemoteComposeLocalePreferences {
    fun read(): site.addzero.remotecompose.shared.RemoteComposeLocale
    fun write(locale: site.addzero.remotecompose.shared.RemoteComposeLocale)
}

fun remoteComposeClientModule(
    baseUrl: String,
    localePreferences: RemoteComposeLocalePreferences,
): Module {
    return module {
        single {
            RemoteComposeClientConfig(
                baseUrl = baseUrl,
            )
        }
        single<RemoteComposeDemoService> {
            RemoteComposeHttpService(
                config = get(),
            )
        }
        single<RemoteComposeLocalePreferences> {
            localePreferences
        }
        single {
            RemoteComposeDemoState(
                config = get(),
                service = get(),
                localePreferences = get(),
            )
        }
    }
}
