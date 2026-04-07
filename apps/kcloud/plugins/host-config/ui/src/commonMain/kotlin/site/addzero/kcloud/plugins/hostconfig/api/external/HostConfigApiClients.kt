package site.addzero.kcloud.plugins.hostconfig.api.external

import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.annotation.Single

/**
 * host-config 外部 API 的 Koin 叶子入口。
 *
 * 这里直接把 Ktorfit 代理实例做成 `@Single`，这样根应用只需要广域扫描，
 * 不需要再显式 include 额外的 provider module。
 */
@Single
class CloudAccessApiClient(
    ktorfit: Ktorfit,
) : CloudAccessApi by ktorfit.createCloudAccessApi()

@Single
class GatewayConfigApiClient(
    ktorfit: Ktorfit,
) : GatewayConfigApi by ktorfit.createGatewayConfigApi()

@Single
class ProjectApiClient(
    ktorfit: Ktorfit,
) : ProjectApi by ktorfit.createProjectApi()

@Single
class ProjectUploadApiClient(
    ktorfit: Ktorfit,
) : ProjectUploadApi by ktorfit.createProjectUploadApi()

@Single
class TagApiClient(
    ktorfit: Ktorfit,
) : TagApi by ktorfit.createTagApi()

@Single
class TemplateApiClient(
    ktorfit: Ktorfit,
) : TemplateApi by ktorfit.createTemplateApi()
