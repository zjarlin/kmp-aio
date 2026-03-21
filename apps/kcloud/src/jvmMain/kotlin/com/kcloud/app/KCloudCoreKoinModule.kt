package com.kcloud.app

import com.kcloud.db.Database
import com.kcloud.db.DatabaseImpl
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.Screen
import site.addzero.workbenchshell.ScreenCatalog

@Module
@ComponentScan("com.kcloud.app")
class KCloudCoreKoinModule {
    @Single(createdAtStart = true)
    fun database(): Database {
        return DatabaseImpl().apply { initialize() }
    }

    @Single
    fun screenCatalog(
        screens: List<Screen>,
    ): ScreenCatalog {
        return ScreenCatalog(
            screens = kCloudShellRootScreens() + screens,
        )
    }
}
