package site.addzero.repro.feature.provider

import org.koin.core.annotation.Configuration
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

data class ProvidedPalette(
    val name: String,
)

@Module
class PlainProviderModule {
    @Single
    fun providedPalette(): ProvidedPalette {
        return ProvidedPalette(name = "ocean")
    }
}

@Module(includes = [PlainProviderModule::class])
@ComponentScan
class ProviderGraphModule

@Module(includes = [PlainProviderModule::class, ProviderGraphModule::class])
@Configuration
class DefaultProviderStarterModule

@Module(includes = [PlainProviderModule::class, ProviderGraphModule::class])
@Configuration("wide-scan")
class WideScanProviderStarterModule

@Single
class ProviderBackedScreenState(
    private val providedPalette: ProvidedPalette,
) {
    fun label(): String = "${providedPalette.name}/dashboard"
}
