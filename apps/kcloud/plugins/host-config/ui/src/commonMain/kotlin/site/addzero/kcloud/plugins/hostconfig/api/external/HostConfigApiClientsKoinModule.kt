package site.addzero.kcloud.plugins.hostconfig.api.external

import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * 把 controller2api 生成的接口桥接成稳定的 Koin 注入入口。
 *
 * 当前仓库消费的 `controller2api-processor:2026.04.07` 仍会生成旧式
 * `KoinPlatform.getKoin()` 聚合器，所以这里先用显式注入 `Ktorfit` 的方式兜住。
 */
@Module
@Configuration
class HostConfigApiClientsKoinModule {
    @Single
    fun cloudAccessApi(ktorfit: Ktorfit): CloudAccessApi {
        return HostConfigApiClients.cloudAccessApi(ktorfit)
    }

    @Single
    fun gatewayConfigApi(ktorfit: Ktorfit): GatewayConfigApi {
        return HostConfigApiClients.gatewayConfigApi(ktorfit)
    }

    @Single
    fun projectApi(ktorfit: Ktorfit): ProjectApi {
        return HostConfigApiClients.projectApi(ktorfit)
    }

    @Single
    fun projectUploadApi(ktorfit: Ktorfit): ProjectUploadApi {
        return HostConfigApiClients.projectUploadApi(ktorfit)
    }

    @Single
    fun tagApi(ktorfit: Ktorfit): TagApi {
        return HostConfigApiClients.tagApi(ktorfit)
    }

    @Single
    fun templateApi(ktorfit: Ktorfit): TemplateApi {
        return HostConfigApiClients.templateApi(ktorfit)
    }
}
