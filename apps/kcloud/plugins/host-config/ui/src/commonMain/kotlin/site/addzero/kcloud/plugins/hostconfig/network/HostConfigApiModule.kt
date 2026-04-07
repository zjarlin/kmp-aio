package site.addzero.kcloud.plugins.hostconfig.network

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.hostconfig.api.external.Apis
import site.addzero.kcloud.plugins.hostconfig.api.external.CloudAccessApi
import site.addzero.kcloud.plugins.hostconfig.api.external.GatewayConfigApi
import site.addzero.kcloud.plugins.hostconfig.api.external.ProjectApi
import site.addzero.kcloud.plugins.hostconfig.api.external.ProjectUploadApi
import site.addzero.kcloud.plugins.hostconfig.api.external.TagApi
import site.addzero.kcloud.plugins.hostconfig.api.external.TemplateApi

@Module
class HostConfigApiModule {
    @Single
    fun projectApi(): ProjectApi {
        return Apis.projectApi
    }

    @Single
    fun tagApi(): TagApi {
        return Apis.tagApi
    }

    @Single
    fun templateApi(): TemplateApi {
        return Apis.templateApi
    }

    @Single
    fun cloudAccessApi(): CloudAccessApi {
        return Apis.cloudAccessApi
    }

    @Single
    fun gatewayConfigApi(): GatewayConfigApi {
        return Apis.gatewayConfigApi
    }

    @Single
    fun projectUploadApi(): ProjectUploadApi {
        return Apis.projectUploadApi
    }
}
