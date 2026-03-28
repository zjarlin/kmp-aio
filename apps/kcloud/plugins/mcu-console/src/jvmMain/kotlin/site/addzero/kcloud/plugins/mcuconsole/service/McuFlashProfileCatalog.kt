package site.addzero.kcloud.plugins.mcuconsole.service

import site.addzero.kcloud.plugins.mcuconsole.McuFlashProfileSummary
import site.addzero.kcloud.plugins.mcuconsole.McuFlashProfilesResponse
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRuntimeKind
import site.addzero.kcloud.plugins.mcuconsole.McuFlashStrategyKind

class McuFlashProfileCatalog {
    private val profiles = listOf(
        McuFlashProfileSummary(
            id = "rhai-generic-serial",
            title = "Rhai VM / 串口 Bootloader",
            runtimeKind = McuFlashRuntimeKind.RHAI_VM,
            strategyKind = McuFlashStrategyKind.SERIAL_ACK_STREAM,
            mcuFamily = "generic",
            description = "适配 START_FLASH / ACK / DONE 串口引导协议",
            artifactLabel = "Rhai 固件路径",
            artifactHint = "/abs/path/rhai-vm.bin",
            defaultBaudRate = 115200,
        ),
        McuFlashProfileSummary(
            id = "rhai-generic-command",
            title = "Rhai VM / 任意 MCU 命令模板",
            runtimeKind = McuFlashRuntimeKind.RHAI_VM,
            strategyKind = McuFlashStrategyKind.COMMAND_TEMPLATE,
            mcuFamily = "generic",
            description = "用 openocd / st-flash / pyocd / esptool / bossac 等外部工具刷写 Rhai VM",
            artifactLabel = "Rhai 固件路径",
            artifactHint = "/abs/path/rhai-vm.bin",
            defaultBaudRate = 115200,
            commandTemplate = "vendor-flasher flash \"{firmwarePath}\"",
            supportsCommandOverride = true,
            requiresPort = false,
        ),
        McuFlashProfileSummary(
            id = "rhai-esp-esptool",
            title = "Rhai VM / ESP 系列示例",
            runtimeKind = McuFlashRuntimeKind.RHAI_VM,
            strategyKind = McuFlashStrategyKind.COMMAND_TEMPLATE,
            mcuFamily = "esp32",
            description = "ESP32 系列串口刷写示例模板，可在命令框继续改写地址与参数",
            artifactLabel = "Rhai 固件路径",
            artifactHint = "/abs/path/rhai-vm.bin",
            defaultBaudRate = 921600,
            commandTemplate = "python -m esptool --chip auto --port \"{portPath}\" --baud {baudRate} write_flash 0x1000 \"{firmwarePath}\"",
            supportsCommandOverride = true,
        ),
        McuFlashProfileSummary(
            id = "micropython-generic-command",
            title = "MicroPython / 任意 MCU 固件模板",
            runtimeKind = McuFlashRuntimeKind.MICROPYTHON,
            strategyKind = McuFlashStrategyKind.COMMAND_TEMPLATE,
            mcuFamily = "generic",
            description = "用厂商烧录工具刷写 MicroPython 固件镜像",
            artifactLabel = "MicroPython 固件路径",
            artifactHint = "/abs/path/firmware.bin",
            defaultBaudRate = 115200,
            commandTemplate = "vendor-flasher flash \"{firmwarePath}\"",
            supportsCommandOverride = true,
            requiresPort = false,
        ),
        McuFlashProfileSummary(
            id = "micropython-script-deploy",
            title = "MicroPython / 脚本部署",
            runtimeKind = McuFlashRuntimeKind.MICROPYTHON,
            strategyKind = McuFlashStrategyKind.COMMAND_TEMPLATE,
            mcuFamily = "generic",
            description = "向已刷好 MicroPython 固件的设备上传 main.py / boot.py",
            artifactLabel = "MicroPython 脚本路径",
            artifactHint = "/abs/path/main.py",
            defaultBaudRate = 115200,
            commandTemplate = "mpremote connect \"{portPath}\" fs cp \"{firmwarePath}\" :main.py",
            supportsCommandOverride = true,
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
