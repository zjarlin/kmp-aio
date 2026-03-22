package site.addzero.vibepocket.service

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.koin.core.annotation.Single
import site.addzero.starter.statuspages.BadGatewayHttpException
import site.addzero.vibepocket.api.suno.SunoApiClient
import site.addzero.vibepocket.api.music.MusicPlaybackRateUtil
import site.addzero.vibepocket.model.AppConfig
import site.addzero.vibepocket.model.key
import site.addzero.vibepocket.model.value
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
) : UploadCoverSourcePreparationService {
    companion object {
        private const val DEFAULT_PLAYBACK_RATE = 1.06
        private const val DEFAULT_OUTPUT_CONTENT_TYPE = "audio/wav"
        private const val SUNO_API_TOKEN_KEY = "suno_api_token"
        private const val SUNO_API_BASE_URL_KEY = "suno_api_base_url"
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
        val apiToken = readConfigValue(SUNO_API_TOKEN_KEY)
            ?.trim()
            .orEmpty()
        if (apiToken.isBlank()) {
            throw IllegalStateException(SunoApiClient.MISSING_API_TOKEN_MESSAGE)
        }

        val baseUrl = readConfigValue(SUNO_API_BASE_URL_KEY)
            ?.trim()
            ?.ifBlank { SunoApiClient.DEFAULT_BASE_URL }
            ?: SunoApiClient.DEFAULT_BASE_URL

        return SunoApiClient(
            apiToken = apiToken,
            baseUrl = baseUrl,
        )
    }

    private fun readConfigValue(key: String): String? {
        return sqlClient.createQuery(AppConfig::class) {
            where(table.key eq key)
            select(table.value)
        }.execute().firstOrNull()
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
