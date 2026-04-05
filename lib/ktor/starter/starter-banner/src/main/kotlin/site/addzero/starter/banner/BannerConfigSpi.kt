package site.addzero.starter.banner

interface BannerConfigSpi {
    val enable: Boolean

    val bannerText: String get() = "App"
    val bannerSubtitle: String get() = "Subtitle"
}