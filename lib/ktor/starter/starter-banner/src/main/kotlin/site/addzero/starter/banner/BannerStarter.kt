package site.addzero.starter.banner

import io.ktor.server.application.*
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import site.addzero.configcenter.ConfigCenter
import site.addzero.ktor.banner.Banner
import site.addzero.starter.AppStarter
import site.addzero.starter.effectiveConfig

@Module
@ComponentScan("site.addzero.starter.banner")
class BannerStarterKoinModule

@Named("bannerStarter")
@Single
class BannerStarter : AppStarter {
    override val order: Int get() = 30

    override fun Application.onInstall() {
        val env = ConfigCenter.getEnv(effectiveConfig()).path("banner")
        val bannerText = env.string("text", "APP") ?: "APP"
        val subtitle = env.string("subtitle").orEmpty()
        install(Banner) {
            this.text = bannerText
            this.subtitle = subtitle
        }
    }
}
