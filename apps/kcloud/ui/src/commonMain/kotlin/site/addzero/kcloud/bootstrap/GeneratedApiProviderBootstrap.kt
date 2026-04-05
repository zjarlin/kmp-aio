package site.addzero.kcloud.bootstrap

import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.mp.KoinPlatform
import site.addzero.kcloud.api.ApiProvider as VibePocketApiProvider
import site.addzero.kcloud.plugins.system.knowledgebase.api.ApiProvider as KnowledgeBaseApiProvider
import site.addzero.kcloud.plugins.system.rbac.api.ApiProvider as RbacApiProvider

private var generatedApiProvidersConfigured = false

internal fun configureGeneratedApiProviders() {
    if (generatedApiProvidersConfigured) {
        return
    }
    val ktorfit = KoinPlatform.getKoin().get<Ktorfit>()
    VibePocketApiProvider.configure(ktorfit)
    KnowledgeBaseApiProvider.configure(ktorfit)
    RbacApiProvider.configure(ktorfit)
    generatedApiProvidersConfigured = true
}
