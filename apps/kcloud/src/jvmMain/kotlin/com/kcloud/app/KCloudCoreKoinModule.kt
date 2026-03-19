package com.kcloud.app

import com.kcloud.db.Database
import com.kcloud.db.DatabaseImpl
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.kcloud.app")
class KCloudCoreKoinModule {
    @Single(createdAtStart = true)
    fun database(): Database {
        return DatabaseImpl().apply { initialize() }
    }
}
