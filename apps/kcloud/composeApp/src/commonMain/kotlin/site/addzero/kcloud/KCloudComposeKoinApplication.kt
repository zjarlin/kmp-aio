package site.addzero.kcloud

import org.koin.core.annotation.KoinApplication
import site.addzero.kcloud.app.KCloudWorkbenchKoinModule
import site.addzero.kcloud.plugins.mcuconsole.McuConsoleComposeKoinModule
import site.addzero.kcloud.plugins.rbac.RbacKoinModule
import site.addzero.kcloud.plugins.system.aichat.AiChatKoinModule
import site.addzero.kcloud.plugins.system.configcenter.ConfigCenterComposeKoinModule
import site.addzero.kcloud.plugins.system.knowledgebase.KnowledgeBaseKoinModule
import site.addzero.vibepocket.VibePocketKoinModule

@KoinApplication(
    modules = [
        KCloudWorkbenchKoinModule::class,
        McuConsoleComposeKoinModule::class,
        ConfigCenterComposeKoinModule::class,
        RbacKoinModule::class,
        AiChatKoinModule::class,
        KnowledgeBaseKoinModule::class,
        VibePocketKoinModule::class,
    ],
)
object KCloudComposeKoinApplication
