package site.addzero.kcloud

import org.koin.core.annotation.KoinApplication
import site.addzero.configcenter.ktor.ConfigCenterKtorKoinModule
import site.addzero.kcloud.plugins.mcuconsole.McuConsoleServerKoinModule
import site.addzero.kcloud.plugins.rbac.RbacKoinModule
import site.addzero.kcloud.plugins.system.aichat.AiChatKoinModule
import site.addzero.kcloud.plugins.system.knowledgebase.KnowledgeBaseKoinModule
import site.addzero.vibepocket.VibePocketKoinModule

@KoinApplication(
    configurations = ["vibepocket"],
    modules = [
        VibePocketKoinModule::class,
        McuConsoleServerKoinModule::class,
        RbacKoinModule::class,
        AiChatKoinModule::class,
        KnowledgeBaseKoinModule::class,
        ConfigCenterKtorKoinModule::class,
    ],
)
object KCloudServerStarterKoinApplication
