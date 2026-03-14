package com.kcloud.plugins.ai.spi

import com.kcloud.model.AiSettings
import kotlinx.serialization.Serializable

enum class AiProviderType {
    LOCAL,
    CLOUD
}

@Serializable
data class AiProviderDescriptor(
    val providerId: String,
    val displayName: String,
    val providerType: AiProviderType,
    val supportsModelDiscovery: Boolean = false
)

@Serializable
data class AiConnectionTestResult(
    val success: Boolean,
    val message: String,
    val details: String? = null
) {
    companion object {
        fun success(
            message: String,
            details: String? = null
        ): AiConnectionTestResult {
            return AiConnectionTestResult(
                success = true,
                message = message,
                details = details
            )
        }

        fun failure(
            message: String,
            details: String? = null
        ): AiConnectionTestResult {
            return AiConnectionTestResult(
                success = false,
                message = message,
                details = details
            )
        }
    }
}

@Serializable
data class AiModelOption(
    val id: String,
    val displayName: String
)

interface AiProvider {
    val descriptor: AiProviderDescriptor

    suspend fun testConnection(settings: AiSettings): AiConnectionTestResult

    suspend fun discoverModels(settings: AiSettings): List<AiModelOption> {
        return emptyList()
    }
}

interface AiDiagnosticsService {
    fun availableProviders(): List<AiProviderDescriptor>

    suspend fun testConnection(
        providerId: String,
        settings: AiSettings
    ): AiConnectionTestResult

    suspend fun discoverModels(
        providerId: String,
        settings: AiSettings
    ): List<AiModelOption>
}
