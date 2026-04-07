package site.addzero.starter.banner

import io.ktor.server.application.*
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.ktor.banner.Banner
import site.addzero.starter.AppStarter
import site.addzero.starter.AppStarterTest

//@Module
//@ComponentScan("site.addzero.starter.banner")
//class BannerStarterKoinModule

@Module
@ComponentScan
@Configuration
class BannerModule


@Single
class BannerStarter(val config: BannerConfigSpi) : AppStarter {
    override val order get() = 30

    override val enable: Boolean
        get() = config.enable


    override fun onInstall(application: Application) {
        application.install(Banner) {
            this.text = config.bannerText
            this.subtitle = config.bannerSubtitle
        }
    }
}

@Single
class BannerStarterTest(val config: BannerConfigSpi) : AppStarterTest {
    override fun onstart() {
        println(config)
    }


}
