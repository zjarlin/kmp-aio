package site.addzero.starter.banner

import site.addzero.configcenter.ConfigCenterItem
import site.addzero.configcenter.ConfigCenterNamespace

@ConfigCenterNamespace(
    namespace = "kcloud",
    objectName = "BannerConfigKeys",
)
interface BannerConfigCenterSpec {
    @ConfigCenterItem(
        key = "banner.text",
        comment = "启动 Banner 主标题。",
    )
    val text: String

    @ConfigCenterItem(
        key = "banner.subtitle",
        comment = "启动 Banner 副标题。",
    )
    val subtitle: String
}
