package site.addzero.kcloud

import org.koin.core.annotation.KoinApplication
import site.addzero.configcenter.ktor.ConfigCenterKtorKoinModule
import site.addzero.kcloud.plugins.mcuconsole.McuConsoleServerKoinModule
import site.addzero.kcloud.plugins.rbac.RbacKoinModule
import site.addzero.kcloud.plugins.system.aichat.AiChatKoinModule
import site.addzero.kcloud.plugins.system.knowledgebase.KnowledgeBaseKoinModule
import site.addzero.kcloud.plugins.system.pluginmarket.PluginMarketServerKoinModule
import site.addzero.vibepocket.VibePocketKoinModule

@KoinApplication(
    configurations = ["vibepocket"],
    modules = [
        // <managed:plugin-market-server-koin:start>
        site.addzero.vibepocket.VibePocketKoinModule::class,
        site.addzero.kcloud.plugins.mcuconsole.McuConsoleServerKoinModule::class,
        site.addzero.kcloud.plugins.rbac.RbacKoinModule::class,
        site.addzero.kcloud.plugins.system.aichat.AiChatKoinModule::class,
        site.addzero.kcloud.plugins.system.knowledgebase.KnowledgeBaseKoinModule::class,
        site.addzero.configcenter.ktor.ConfigCenterKtorKoinModule::class,
        site.addzero.kcloud.plugins.system.pluginmarket.PluginMarketServerKoinModule::class,
        // <managed:plugin-market-server-koin:end>
    ],
)
object KCloudServerStarterKoinApplication
