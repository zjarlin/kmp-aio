package site.addzero.kcloud.plugins.hostconfig.api.external

import de.jensklingenberg.ktorfit.Ktorfit

/**
 * host-config 生成 API 的稳定工厂入口。
 *
 * 这里显式接收 `Ktorfit`，避免旧版聚合器模板把 Koin service locator 藏进生成代码里。
 */
object HostConfigApiClients {
    fun cloudAccessApi(ktorfit: Ktorfit): CloudAccessApi {
        return ktorfit.createCloudAccessApi()
    }

    fun gatewayConfigApi(ktorfit: Ktorfit): GatewayConfigApi {
        return ktorfit.createGatewayConfigApi()
    }

    fun projectApi(ktorfit: Ktorfit): ProjectApi {
        return ktorfit.createProjectApi()
    }

    fun projectUploadApi(ktorfit: Ktorfit): ProjectUploadApi {
        return ktorfit.createProjectUploadApi()
    }

    fun tagApi(ktorfit: Ktorfit): TagApi {
        return ktorfit.createTagApi()
    }

    fun templateApi(ktorfit: Ktorfit): TemplateApi {
        return ktorfit.createTemplateApi()
    }
}
