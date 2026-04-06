package site.addzero.kcloud.server.context

import org.koin.core.annotation.Single
import site.addzero.starter.banner.BannerConfigSpi

@Single
class BannerConfig(
    private val config: ServerContextConfig,
) : BannerConfigSpi {
    override val enable: Boolean
        get() = config.banner.enabled

    override val bannerText: String
        get() = config.banner.text

    override val bannerSubtitle: String
        get() = config.banner.subtitle
}
