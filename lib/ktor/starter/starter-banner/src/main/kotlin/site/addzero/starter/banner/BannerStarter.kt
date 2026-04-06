package site.addzero.starter.banner

import io.ktor.server.application.*
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.ktor.banner.Banner
import site.addzero.starter.AppStarter

@Module
@ComponentScan("site.addzero.starter.banner")
class BannerStarterKoinModule

@Single
class BannerStarter(val config: BannerConfigSpi) : AppStarter<Application> {
    override val order get() = 30

    override val enable: Boolean
        get() = config.enable


    override fun Application.onInstall() {
        install(Banner) {
            this.text = config.bannerText
            this.subtitle = config.bannerSubtitle
        }
    }
}
