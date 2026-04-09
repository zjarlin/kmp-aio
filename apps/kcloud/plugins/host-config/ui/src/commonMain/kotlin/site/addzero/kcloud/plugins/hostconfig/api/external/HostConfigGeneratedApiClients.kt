package site.addzero.kcloud.plugins.hostconfig.api.external

import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.CatalogApi
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.CloudAccessApi
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.GatewayConfigApi
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.ProjectApi
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.ProjectUploadApi
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.TagApi
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.TemplateApi
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.createCatalogApi
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.createCloudAccessApi
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.createGatewayConfigApi
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.createProjectApi
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.createProjectUploadApi
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.createTagApi
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.createTemplateApi

/**
 * host-config 生成 API 的 Koin 叶子入口。
 *
 * controller2api 生成的 `ApisModule` 当前没有稳定进入 UI 根配置自动发现链，
 * 这里用 source-controlled 的 `@Single` bridge 兜底，确保页面级 ViewModel
 * 在运行时总能拿到 generated API 代理。
 */
@Single
class CatalogApiClient(
    ktorfit: Ktorfit,
) : CatalogApi by ktorfit.createCatalogApi()

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
