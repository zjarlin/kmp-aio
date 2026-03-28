package site.addzero.kcloud.plugins.mcuconsole

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.mcuconsole.client.McuConsoleRemoteService
import site.addzero.kcloud.plugins.mcuconsole.client.McuConsoleWorkbenchState

@Module
class McuConsoleComposeKoinModule {
    @Single
    fun provideRemoteService(): McuConsoleRemoteService {
        return McuConsoleRemoteService()
    }

    @Single
    fun provideWorkbenchState(
        remoteService: McuConsoleRemoteService,
    ): McuConsoleWorkbenchState {
        return McuConsoleWorkbenchState(
            remoteService = remoteService,
        )
    }
}
