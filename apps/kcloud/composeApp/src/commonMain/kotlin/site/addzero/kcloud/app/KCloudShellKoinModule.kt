package site.addzero.kcloud.app

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.generated.RouteKeys

@Module
@ComponentScan("site.addzero.kcloud.app")
class KCloudShellKoinModule {
    @Single
    fun provideRouteCatalog(): KCloudRouteCatalog {
        // Koin compiler does not honor constructor default arguments for scanned singles here.
        return KCloudRouteCatalog(RouteKeys.allMeta)
    }
}
