package site.addzero.kcloud.plugins.hostconfig.api.external

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * 为 controller2api 生成的接口补充 Koin 注入绑定。
 */
@Module
class ApisModule {
    @Single
    fun cloudAccessApi(): CloudAccessApi {
        return Apis.cloudAccessApi
    }

    @Single
    fun gatewayConfigApi(): GatewayConfigApi {
        return Apis.gatewayConfigApi
    }

    @Single
    fun projectApi(): ProjectApi {
        return Apis.projectApi
    }

    @Single
    fun projectUploadApi(): ProjectUploadApi {
        return Apis.projectUploadApi
    }

    @Single
    fun tagApi(): TagApi {
        return Apis.tagApi
    }

    @Single
    fun templateApi(): TemplateApi {
        return Apis.templateApi
    }
}
