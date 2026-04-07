package site.addzero.repro.feature.onestarter

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Single
class BannerLikeStarterService {
    fun label(): String = "banner-like"
}

@Module
@ComponentScan
@Configuration("banner-like")
class BannerLikeStarterModule

@Single
class NoScanStarterService {
    fun label(): String = "no-scan"
}

@Module
@Configuration("no-scan")
class NoScanStarterModule
