package site.addzero.kcloud.plugins.system.pluginmarket.jvm.route.config

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.kcloud.plugins.system.pluginmarket.model.PluginMarketConfigDto
import site.addzero.kcloud.plugins.system.pluginmarket.model.UpdatePluginMarketConfigRequest
import site.addzero.kcloud.plugins.system.pluginmarket.service.PluginMarketConfigService

/**
 * 读取插件市场运行配置。
 */
@GetMapping("/api/kcloud/plugin-market/config")
suspend fun readPluginMarketConfig(): PluginMarketConfigDto {
    return configService().read()
}

/**
 * 更新插件市场运行配置。
 */
@PutMapping("/api/kcloud/plugin-market/config")
suspend fun updatePluginMarketConfig(
    @RequestBody request: UpdatePluginMarketConfigRequest,
): PluginMarketConfigDto {
    return configService().update(request)
}

private fun configService(): PluginMarketConfigService = KoinPlatform.getKoin().get()
