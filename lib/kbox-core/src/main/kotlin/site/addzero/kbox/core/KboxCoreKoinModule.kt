package site.addzero.kbox.core

import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.core.network.json.prettyJson

@Module
@ComponentScan("site.addzero.kbox.core")
class KboxCoreKoinModule {
    @Single
    fun provideJson(): Json {
        return prettyJson
    }
}
