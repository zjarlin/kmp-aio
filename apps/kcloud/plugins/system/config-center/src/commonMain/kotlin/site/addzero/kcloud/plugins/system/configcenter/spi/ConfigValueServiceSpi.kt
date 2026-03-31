package site.addzero.kcloud.plugins.system.configcenter.spi

import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterValueDto

/**
 * 配置中心对外暴露的最小值读写契约。
 *
 * 其它插件只能依赖这个共享 SPI，不直接依赖 `config-center:server` 里的实现类。
 */
interface ConfigValueServiceSpi {
    fun readValue(
        namespace: String,
        key: String,
        active: String = "dev",
    ): ConfigCenterValueDto

    fun writeValue(
        namespace: String,
        key: String,
        value: String,
        active: String = "dev",
    ): ConfigCenterValueDto
}
