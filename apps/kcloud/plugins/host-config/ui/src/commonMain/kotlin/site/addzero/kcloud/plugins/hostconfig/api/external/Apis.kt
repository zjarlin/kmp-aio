package site.addzero.kcloud.plugins.hostconfig.api.external

import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.mp.KoinPlatform

/**
 * Aggregates controller2api-generated Ktorfit interfaces for host-config.
 *
 * The upstream processor is expected to generate this object, but we keep a
 * repo-local fallback here so the plugin can compile even when only the
 * interface files are emitted.
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
