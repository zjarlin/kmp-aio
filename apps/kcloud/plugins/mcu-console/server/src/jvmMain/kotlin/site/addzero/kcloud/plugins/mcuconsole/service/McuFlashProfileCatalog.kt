package site.addzero.kcloud.plugins.mcuconsole.service

import site.addzero.kcloud.plugins.mcuconsole.McuFlashProfileSummary
import site.addzero.kcloud.plugins.mcuconsole.McuFlashProfilesResponse
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRuntimeKind
import site.addzero.kcloud.plugins.mcuconsole.McuFlashStrategyKind

class McuFlashProfileCatalog {
    private val profiles = listOf(
        McuFlashProfileSummary(
            id = "rhai-generic-serial",
            title = "Rhai VM / 板级固件",
            runtimeKind = McuFlashRuntimeKind.RHAI_VM,
            strategyKind = McuFlashStrategyKind.SERIAL_ACK_STREAM,
            mcuFamily = "custom",
            description = "刷写你自己编译的 Rhai 板级固件，设备侧需实现 START_FLASH / ACK / DONE 串口引导协议",
            artifactLabel = "Rhai 固件路径",
            artifactHint = "/abs/path/<board>-rhai.bin",
            defaultBaudRate = 115200,
            supportsOnlineDownload = true,
            downloadUrlHint = "https://firmware.example.com/<board>/rhai.bin",
        ),
        McuFlashProfileSummary(
            id = "micropython-generic-command",
            title = "MicroPython / ESP32 官方固件",
            runtimeKind = McuFlashRuntimeKind.MICROPYTHON,
            strategyKind = McuFlashStrategyKind.COMMAND_TEMPLATE,
            mcuFamily = "esp32",
            description = "按官方 ESP32 流程，用 esptool 擦除 Flash 并写入 MicroPython 固件镜像",
            artifactLabel = "MicroPython 固件路径",
            artifactHint = "/abs/path/ESP32_GENERIC-*.bin",
            defaultBaudRate = 115200,
            commandTemplate = "{esptoolCommand} --chip esp32 --port \"{portPath}\" erase_flash && {esptoolCommand} --chip esp32 --port \"{portPath}\" --baud {baudRate} write_flash -z 0x1000 \"{firmwarePath}\"",
            supportsCommandOverride = true,
            supportsOnlineDownload = true,
            defaultDownloadUrl = "https://micropython.org/download/ESP32_GENERIC/",
            downloadUrlHint = "留空则自动抓取 MicroPython ESP32 Generic 最新稳定版，或填写直链/页面地址",
        ),
    )

    fun listProfiles(): McuFlashProfilesResponse {
        return McuFlashProfilesResponse(
            items = profiles,
            defaultProfileId = profiles.firstOrNull()?.id,
        )
    }

    fun resolve(
        profileId: String?,
    ): McuFlashProfileSummary {
        val normalized = profileId?.trim().orEmpty()
        if (normalized.isBlank()) {
            return profiles.first()
        }
        return profiles.firstOrNull { it.id == normalized }
            ?: throw IllegalArgumentException("未知烧录能力包: $normalized")
    }
}
