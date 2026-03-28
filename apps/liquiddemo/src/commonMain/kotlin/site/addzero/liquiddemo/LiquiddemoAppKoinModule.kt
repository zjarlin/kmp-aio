package site.addzero.liquiddemo

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.Screen
import site.addzero.workbenchshell.ScreenTree

@Module
@ComponentScan("site.addzero.liquiddemo")
class LiquiddemoAppKoinModule {
    @Single
    fun provideScreenTree(
        screens: List<Screen>,
    ): ScreenTree {
        return ScreenTree(
            screens = screens,
        )
    }
}
