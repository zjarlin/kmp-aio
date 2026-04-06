package site.addzero.kcloud.server.context

import org.koin.core.annotation.Single
import site.addzero.starter.banner.BannerConfigSpi

@Single
class BannerConfig : BannerConfigSpi {
    override val enable: Boolean
        get() = true

    override val bannerText: String
        get() = "KCLOUD"

    override val bannerSubtitle: String
        get() = "Workbench"
}
