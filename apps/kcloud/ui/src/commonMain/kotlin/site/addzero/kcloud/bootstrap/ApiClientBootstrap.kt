package site.addzero.kcloud.bootstrap

import site.addzero.kcloud.api.ServerApiClient
import site.addzero.kcloud.plugins.system.aichat.api.AiChatApiClient
import site.addzero.kcloud.plugins.system.configcenter.api.ConfigCenterApiClient
import site.addzero.kcloud.plugins.system.knowledgebase.api.KnowledgeBaseApiClient
import site.addzero.kcloud.plugins.system.rbac.api.RbacApiClient

internal fun configureApiClients(
    baseUrl: String,
) {
    val normalizedBaseUrl = baseUrl.trim()
        .ifBlank { DEFAULT_KCLOUD_API_BASE_URL }
        .trimEnd('/') + "/"
    ServerApiClient.configureBaseUrl(normalizedBaseUrl)
    AiChatApiClient.configureBaseUrl(normalizedBaseUrl)
    ConfigCenterApiClient.configureBaseUrl(normalizedBaseUrl)
    KnowledgeBaseApiClient.configureBaseUrl(normalizedBaseUrl)
    RbacApiClient.configureBaseUrl(normalizedBaseUrl)
}

private const val DEFAULT_KCLOUD_API_BASE_URL = "http://localhost:18080/"
