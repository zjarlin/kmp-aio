package site.addzero.kcloud.plugins.mcuconsole.config

import site.addzero.configcenter.ConfigCenterItem
import site.addzero.configcenter.ConfigCenterNamespace

@ConfigCenterNamespace(
    namespace = "kcloud",
    objectName = "McuConsoleConfigKeys",
)
interface McuConsoleConfigCenterSpec {
    @ConfigCenterItem(
        key = "mcu.runtime.bundle-root-dir",
        comment = "MCU Console 运行时固件根目录。",
    )
    val runtimeBundleRootDir: String
}
