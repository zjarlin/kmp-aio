package site.addzero.kcloud.vibepocket.service

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.Single
import site.addzero.configcenter.ConfigCenterBeanFactory
import site.addzero.configcenter.ConfigCenterEnv
import site.addzero.configcenter.ConfigCenterKeyDefinition
import site.addzero.kcloud.api.suno.SunoApiClient
import site.addzero.kcloud.plugins.system.configcenter.spi.ConfigValueServiceSpi
import site.addzero.kcloud.plugins.system.configcenter.spi.RuntimeConfigCenterActive
import site.addzero.kcloud.vibepocket.config.VibepocketConfigKeys
import site.addzero.kcloud.vibepocket.model.AppConfig
import site.addzero.starter.statuspages.BadGatewayHttpException
import site.addzero.vibepocket.api.music.MusicPlaybackRateUtil
import site.addzero.vibepocket.music.UploadCoverSourcePrepareResponse

interface UploadCoverSourcePreparationService {
    suspend fun prepare(
        sourceUrl: String,
        playbackRate: Double? = null,
    ): UploadCoverSourcePrepareResponse
}

@Single
class DefaultUploadCoverSourcePreparationService(
    private val sqlClient: KSqlClient,
    private val configValueService: ConfigValueServiceSpi,
    private val configCenterBeanFactory: ConfigCenterBeanFactory,
    private val runtimeConfigCenterActive: RuntimeConfigCenterActive,
) : UploadCoverSourcePreparationService {
    companion object {
        private const val DEFAULT_PLAYBACK_RATE = 1.06
        private const val DEFAULT_OUTPUT_CONTENT_TYPE = "audio/wav"
        private const val LEGACY_SUNO_API_TOKEN_KEY = "suno_api_token"
        private const val LEGACY_SUNO_API_BASE_URL_KEY = "suno_api_base_url"
    }

    override suspend fun prepare(
        sourceUrl: String,
        playbackRate: Double?,
    ): UploadCoverSourcePrepareResponse {
        val normalizedSourceUrl = sourceUrl.trim()
        require(normalizedSourceUrl.isNotBlank()) { "sourceUrl is required" }

        val effectivePlaybackRate = normalizePlaybackRate(playbackRate)
        val sunoClient = createSunoClient()

        return runCatching {
            val processedBytes = withContext(Dispatchers.IO) {
                MusicPlaybackRateUtil.changePlaybackRate(
                    inputPathOrUrl = normalizedSourceUrl,
                    playbackRate = effectivePlaybackRate,
                )
            }
            val fileName = buildOutputFileName(
                sourceUrl = normalizedSourceUrl,
                playbackRate = effectivePlaybackRate,
            )
            val uploaded = sunoClient.uploadLocalFile(
                bytes = processedBytes,
                fileName = fileName,
                contentType = DEFAULT_OUTPUT_CONTENT_TYPE,
                uploadPath = "audio",
            )
            UploadCoverSourcePrepareResponse(
                originalUrl = normalizedSourceUrl,
                preparedUrl = uploaded.resolvedUrl,
                playbackRate = effectivePlaybackRate,
                fileName = fileName,
                contentType = DEFAULT_OUTPUT_CONTENT_TYPE,
            )
        }.getOrElse { error ->
            when (error) {
                is IllegalArgumentException -> throw error
                is IllegalStateException -> throw error
                is BadGatewayHttpException -> throw error
                is CancellationException -> throw error
                else -> throw BadGatewayHttpException(
                    error.message?.ifBlank { null }
                        ?: "翻唱音源预处理失败"
                )
            }
        }
    }

    private fun normalizePlaybackRate(playbackRate: Double?): Double {
        val resolved = playbackRate ?: DEFAULT_PLAYBACK_RATE
        require(resolved.isFinite()) { "playbackRate must be finite" }
        require(resolved > 0.0) { "playbackRate must be greater than 0" }
        return resolved
    }

    private fun createSunoClient(): SunoApiClient {
        val apiToken = readRuntimeConfigValue(
            definition = VibepocketConfigKeys.sunoApiToken,
            legacyKeys = arrayOf(LEGACY_SUNO_API_TOKEN_KEY),
        ).trim()
        if (apiToken.isBlank()) {
            throw IllegalStateException(SunoApiClient.MISSING_API_TOKEN_MESSAGE)
        }

        val baseUrl = readRuntimeConfigValue(
            definition = VibepocketConfigKeys.sunoApiBaseUrl,
            legacyKeys = arrayOf(LEGACY_SUNO_API_BASE_URL_KEY),
        ).trim()

        return SunoApiClient(
            apiToken = apiToken,
            baseUrl = baseUrl,
        )
    }

    private fun readRuntimeConfigValue(
        definition: ConfigCenterKeyDefinition,
        legacyKeys: Array<String> = emptyArray(),
    ): String {
        val active = runtimeConfigCenterActive.value
        val configuredValue = runtimeEnv()
            .string(definition.key)
            ?.trim()
            .orEmpty()
        if (configuredValue.isNotBlank()) {
            return configuredValue
        }

        val legacyValue = legacyKeys.firstNotNullOfOrNull { legacyKey ->
            readLegacyConfigValue(legacyKey)
                ?.trim()
                ?.takeIf(String::isNotBlank)
        }
        if (legacyValue != null) {
            configValueService.writeValue(
                namespace = VibepocketConfigKeys.NAMESPACE,
                key = definition.key,
                value = legacyValue,
                active = active,
            )
            return legacyValue
        }

        return definition.defaultValue.orEmpty()
    }

    private fun runtimeEnv(): ConfigCenterEnv {
        return configCenterBeanFactory.env(
            namespace = VibepocketConfigKeys.NAMESPACE,
            active = runtimeConfigCenterActive.value,
        )
    }

    private fun readLegacyConfigValue(
        key: String,
    ): String? {
        return sqlClient.createQuery(AppConfig::class) {
            select(table)
        }.execute().firstOrNull { it.key == key }?.value
    }

    private fun buildOutputFileName(
        sourceUrl: String,
        playbackRate: Double,
    ): String {
        val rawName = sourceUrl.substringAfterLast('/')
            .substringBefore('?')
            .substringBefore('#')
            .ifBlank { "upload-cover-source" }
        val baseName = rawName.substringBeforeLast('.')
            .ifBlank { "upload-cover-source" }
        val safeBaseName = baseName.replace(Regex("[^A-Za-z0-9._-]"), "-")
            .trim('-')
            .ifBlank { "upload-cover-source" }
        val rateSuffix = playbackRate.toString().replace('.', '_')
        return "${safeBaseName}_${rateSuffix}x.wav"
    }
}
