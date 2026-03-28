package site.addzero.kcloud.plugins.system.configcenter

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class ConfigCenterComposeKoinModule {
    @Single
    fun provideRemoteService(): ConfigCenterRemoteService {
        return ConfigCenterRemoteService()
    }

    @Single
    fun provideWorkbenchState(
        remoteService: ConfigCenterRemoteService,
    ): ConfigCenterWorkbenchState {
        return ConfigCenterWorkbenchState(remoteService)
    }
}

