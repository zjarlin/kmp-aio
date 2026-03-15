package site.addzero.vibepocket.music.suno

import org.koin.core.annotation.Factory
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Property
import site.addzero.vibepocket.api.suno.SunoApiClient

@Module
@Configuration("vibepocket")
class SunoApiBindings {
    @Factory
    fun provideSunoApiClient(
        @Property("suno.apiToken") token: String,
    ): SunoApiClient {
        return SunoApiClient(apiToken = token)
    }
}
