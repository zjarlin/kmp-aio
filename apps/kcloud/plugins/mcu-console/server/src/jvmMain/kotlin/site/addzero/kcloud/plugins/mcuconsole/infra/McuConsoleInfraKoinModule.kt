package site.addzero.kcloud.plugins.mcuconsole.infra

import kotlinx.serialization.json.Json
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * 收口 mcu-console 与业务无关的基础设施装配。
 */
@Module
class McuConsoleInfraKoinModule {
    @Single
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
}
