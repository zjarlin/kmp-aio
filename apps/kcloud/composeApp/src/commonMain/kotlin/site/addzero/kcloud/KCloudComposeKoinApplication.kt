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
        // <managed:plugin-market-compose-koin:start>
        site.addzero.kcloud.app.KCloudWorkbenchKoinModule::class,
        site.addzero.kcloud.plugins.mcuconsole.McuConsoleComposeKoinModule::class,
        site.addzero.kcloud.plugins.system.configcenter.ConfigCenterComposeKoinModule::class,
        site.addzero.kcloud.plugins.rbac.RbacKoinModule::class,
        site.addzero.kcloud.plugins.system.aichat.AiChatKoinModule::class,
        site.addzero.kcloud.plugins.system.knowledgebase.KnowledgeBaseKoinModule::class,
        site.addzero.vibepocket.VibePocketKoinModule::class,
        // <managed:plugin-market-compose-koin:end>
    ],
)
object KCloudComposeKoinApplication
