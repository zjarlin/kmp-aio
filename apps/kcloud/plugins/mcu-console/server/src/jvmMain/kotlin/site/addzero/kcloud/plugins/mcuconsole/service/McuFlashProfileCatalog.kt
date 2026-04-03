package site.addzero.kcloud.plugins.mcuconsole.service

import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.mcuconsole.McuFlashProfileSummary
import site.addzero.kcloud.plugins.mcuconsole.McuFlashProfilesResponse
import site.addzero.kcloud.plugins.mcuconsole.McuFlashRuntimeKind
import site.addzero.kcloud.plugins.mcuconsole.McuFlashStrategyKind

@Single
class McuFlashProfileCatalog {
    private val profiles = listOf(
        McuFlashProfileSummary(
            id = "stm32-stlink-swd-f1-hd",
            title = "STM32 / ST-Link SWD",
            runtimeKind = McuFlashRuntimeKind.STM32,
            strategyKind = McuFlashStrategyKind.ST_LINK_SWD,
            mcuFamily = "stm32f1",
            description = "直接通过 ST-Link + SWD 烧录与复位。当前已实测支持 chipId=0x414。",
            artifactLabel = "STM32 固件路径",
            artifactHint = "/abs/path/firmware.bin",
            defaultStartAddress = 0x08000000,
            connectUnderReset = true,
            supportedChipIds = listOf(0x414),
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
