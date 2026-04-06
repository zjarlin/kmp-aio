package site.addzero.kcloud.server.context

import org.koin.core.annotation.Single
import site.addzero.configcenter.ConfigCenterEnv
import site.addzero.starter.banner.BannerConfigSpi

@Single
class BannerConfig(
    private val env: ConfigCenterEnv,
) : BannerConfigSpi {
    override val enable: Boolean
        get() = env.boolean("banner.enabled", true) != false

    override val bannerText: String
        get() = env.string("banner.text", "KCLOUD").orEmpty()

    override val bannerSubtitle: String
        get() = env.string("banner.subtitle", "Workbench").orEmpty()
}
