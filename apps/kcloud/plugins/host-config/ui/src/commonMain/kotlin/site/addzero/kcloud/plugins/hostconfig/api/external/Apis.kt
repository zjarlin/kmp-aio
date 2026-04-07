package site.addzero.kcloud.plugins.hostconfig.api.external

import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.mp.KoinPlatform

/**
 * 聚合后的 Ktorfit 服务提供者
 *
 * 仅聚合 controller2api 生成的接口，不扫描手写接口。
 */
object Apis {
    private fun ktorfit(): Ktorfit = KoinPlatform.getKoin().get()

    val cloudAccessApi: CloudAccessApi
        get() = ktorfit().createCloudAccessApi()

    val gatewayConfigApi: GatewayConfigApi
        get() = ktorfit().createGatewayConfigApi()

    val projectApi: ProjectApi
        get() = ktorfit().createProjectApi()

    val projectUploadApi: ProjectUploadApi
        get() = ktorfit().createProjectUploadApi()

    val tagApi: TagApi
        get() = ktorfit().createTagApi()

    val templateApi: TemplateApi
        get() = ktorfit().createTemplateApi()
}
