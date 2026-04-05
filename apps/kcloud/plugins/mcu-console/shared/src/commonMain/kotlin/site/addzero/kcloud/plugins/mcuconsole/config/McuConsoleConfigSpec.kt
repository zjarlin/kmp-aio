package site.addzero.kcloud.plugins.mcuconsole.config

import site.addzero.configcenter.ConfigCenterItem
import site.addzero.configcenter.ConfigCenterNamespace

@ConfigCenterNamespace(
    namespace = "kcloud.mcu",
    objectName = "McuConsoleConfigKeys",
)
interface McuConsoleConfigSpec {
    @ConfigCenterItem(
        key = "runtime.bundleRootDir",
        comment = "MCU 运行时固件 bundle 的根目录。",
        required = true,
    )
    val runtimeBundleRootDir: String
}
