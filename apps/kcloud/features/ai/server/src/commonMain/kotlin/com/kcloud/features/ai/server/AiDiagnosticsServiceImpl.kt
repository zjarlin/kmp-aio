package com.kcloud.features.ai.server

import com.kcloud.model.AiSettings
import com.kcloud.features.ai.spi.AiConnectionTestResult
import com.kcloud.features.ai.spi.AiDiagnosticsService
import com.kcloud.features.ai.spi.AiModelOption
import com.kcloud.features.ai.spi.AiProvider
import com.kcloud.features.ai.spi.AiProviderDescriptor
import org.koin.core.annotation.Single

@Single(binds = [AiDiagnosticsService::class])
class AiDiagnosticsServiceImpl(
    providers: List<AiProvider>
) : AiDiagnosticsService {
    private val providers: List<AiProvider> = providers.sortedBy { it.descriptor.displayName.lowercase() }
    private val providersById: Map<String, AiProvider> = this.providers.associateBy { it.descriptor.providerId }

    override fun availableProviders(): List<AiProviderDescriptor> {
        return providers.map { it.descriptor }
    }

    override suspend fun testConnection(
        providerId: String,
        settings: AiSettings
    ): AiConnectionTestResult {
        val provider = providersById[providerId]
            ?: return AiConnectionTestResult.failure(
                message = "未找到 AI Provider: $providerId",
                details = "当前已注册: ${providersById.keys.sorted().joinToString()}"
            )

        return provider.testConnection(settings)
    }

    override suspend fun discoverModels(
        providerId: String,
        settings: AiSettings
    ): List<AiModelOption> {
        val provider = providersById[providerId] ?: return emptyList()
        return provider.discoverModels(settings)
    }
}
