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
 * host-config generated API 的源码级叶子桥接。
 *
 * 当前这个模块里 `@Configuration` 没有被 Koin 稳定识别，
 * 所以这里回退到 root scan 可见的 `@Single` 叶子定义。
 * generated 接口本体仍然留在 build/generated 目录，不进入源码树。
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
