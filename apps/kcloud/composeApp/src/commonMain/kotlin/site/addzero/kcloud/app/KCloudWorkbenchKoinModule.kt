package site.addzero.kcloud.app

import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.Screen
import site.addzero.workbenchshell.ScreenTree

@Module
@Configuration("kcloud")
class KCloudWorkbenchKoinModule {
    @Single
    fun provideScreenTree(
        screens: List<Screen>,
    ): ScreenTree {
        return ScreenTree(
            screens = screens,
        )
    }
}
