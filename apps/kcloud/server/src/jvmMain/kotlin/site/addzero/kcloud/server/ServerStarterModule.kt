package site.addzero.kcloud.server

import io.ktor.server.application.Application
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.starter.AppStarter
import site.addzero.starter.banner.BannerStarter

@Module
class ServerStarterModule {
    @Single
    fun provideBannerStarter(): AppStarter<Application> {
        return BannerStarter()
    }
}
