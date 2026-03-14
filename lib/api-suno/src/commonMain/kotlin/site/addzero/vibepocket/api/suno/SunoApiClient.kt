package site.addzero.vibepocket.api.suno

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.util.date.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.roundToInt
import site.addzero.core.network.apiClient

/**
 * Suno API 客户端（全量接口）
https://sunoapi.org/zh-CN/api-key
 * 对接 https://api.sunoapi.org/api/v1
 */
class SunoApiClient(
    private val apiToken: String = "",
    baseUrl: String = DEFAULT_BASE_URL,
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://api.sunoapi.org/api/v1"
        const val TOKEN_DASHBOARD_URL = "https://sunoapi.org/zh-CN/dashboard"
        const val MISSING_API_TOKEN_MESSAGE =
            "还没配置 Suno API Token。没申请过的话请先去 $TOKEN_DASHBOARD_URL 申请 Token，再回设置页填写。"

        private val creditsJson = Json {
            ignoreUnknownKeys = true
        }
    }

    private val normalizedBaseUrl = baseUrl.ifBlank { DEFAULT_BASE_URL }

    val sunoKtorfit = Ktorfit.Builder()
        .baseUrl(normalizedBaseUrl.trimEnd('/') + "/")
        .httpClient(apiClient)
        .build()
    private val api = sunoKtorfit.createSunoApi()

    private val auth get() = "Bearer $apiToken"

    private suspend inline fun <T> withAuth(
        crossinline block: suspend (String) -> T,
    ): T {
        if (apiToken.isBlank()) {
            throw IllegalStateException(MISSING_API_TOKEN_MESSAGE)
        }
        return block(auth)
    }

    // ── 音乐生成 ─────────────────────────────────────────────

    suspend fun generateMusic(request: SunoGenerateRequest): String =
        withAuth { auth ->
            api.generateMusic(request, auth).getOrThrow().taskId
        }

    suspend fun extendMusic(request: SunoExtendRequest): String =
        withAuth { auth ->
            api.extendMusic(request, auth).getOrThrow().taskId
        }

    suspend fun uploadCover(request: SunoUploadCoverRequest): String =
        withAuth { auth ->
            api.uploadCover(request, auth).getOrThrow().taskId
        }

    suspend fun uploadExtend(request: SunoUploadExtendRequest): String =
        withAuth { auth ->
            api.uploadExtend(request, auth).getOrThrow().taskId
        }

    suspend fun addVocals(request: SunoAddVocalsRequest): String =
        withAuth { auth ->
            api.addVocals(request, auth).getOrThrow().taskId
        }

    suspend fun addInstrumental(request: SunoAddInstrumentalRequest): String =
        withAuth { auth ->
            api.addInstrumental(request, auth).getOrThrow().taskId
        }

    suspend fun generateMusicCover(request: SunoMusicCoverRequest): String =
        withAuth { auth ->
            api.generateMusicCover(request, auth).getOrThrow().taskId
        }

    suspend fun replaceSection(request: SunoReplaceSectionRequest): String =
        withAuth { auth ->
            api.replaceSection(request, auth).getOrThrow().taskId
        }

    // ── 查询 ─────────────────────────────────────────────────

    suspend fun getTaskDetail(taskId: String): SunoTaskDetail? =
        withAuth { auth ->
            api.getTaskDetail(taskId, auth).getOrNull()
        }

    suspend fun getCoverDetail(taskId: String): SunoTaskDetail? =
        withAuth { auth ->
            api.getCoverDetail(taskId, auth).getOrNull()
        }

    // ── 歌词 ─────────────────────────────────────────────────

    suspend fun generateLyrics(request: SunoLyricsRequest): String =
        withAuth { auth ->
            api.generateLyrics(request, auth).getOrThrow().taskId
        }

    suspend fun getLyricsDetail(taskId: String): SunoLyricsTaskDetail? =
        withAuth { auth ->
            api.getLyricsDetail(taskId, auth).getOrNull()
        }

    suspend fun getTimestampedLyrics(request: SunoTimestampedLyricsRequest): SunoTimestampedLyricsData =
        withAuth { auth ->
            api.getTimestampedLyrics(request, auth).getOrThrow()
        }

    // ── Persona ──────────────────────────────────────────────

    suspend fun generatePersona(request: SunoGeneratePersonaRequest): SunoPersonaData =
        withAuth { auth ->
            api.generatePersona(request, auth).getOrThrow()
        }

    // ── 音频处理 ─────────────────────────────────────────────

    suspend fun vocalRemoval(request: SunoVocalRemovalRequest): String =
        withAuth { auth ->
            api.vocalRemoval(request, auth).getOrThrow().taskId
        }

    suspend fun boostMusicStyle(request: SunoBoostStyleRequest): SunoBoostStyleData =
        withAuth { auth ->
            api.boostMusicStyle(request, auth).getOrThrow()
        }

    suspend fun convertToWav(request: SunoWavRequest): String =
        withAuth { auth ->
            api.convertToWav(request, auth).getOrThrow().taskId
        }

    // ── 账户 ─────────────────────────────────────────────────

    suspend fun getCredits(): Int =
        withAuth { auth ->
            val responseText = apiClient.get(normalizedBaseUrl.trimEnd('/') + "/generate/credit") {
                header(HttpHeaders.Authorization, auth)
            }.bodyAsText()

            val creditsValue = creditsJson.parseToJsonElement(responseText)
                .jsonObject["data"]
                ?.jsonPrimitive
                ?.doubleOrNull
                ?: throw RuntimeException("Suno 积分响应格式不正确")

            creditsValue.roundToInt()
        }

    // ── 轮询 ─────────────────────────────────────────────────

    /** 轮询等待音乐生成任务完成 */
    suspend fun waitForCompletion(
        taskId: String,
        maxWaitMs: Long = 600_000L,
        pollIntervalMs: Long = 30_000L,
        onStatusUpdate: ((SunoTaskDetail?) -> Unit)? = null,
    ): SunoTaskDetail {
        val startTime = getTimeMillis()
        while (getTimeMillis() - startTime < maxWaitMs) {
            val detail = getTaskDetail(taskId)
            onStatusUpdate?.invoke(detail)
            when {
                detail?.isSuccess == true -> return detail
                detail?.isFailed == true -> throw RuntimeException(
                    "任务失败: ${detail.errorMessage ?: detail.errorCode ?: "未知错误"}"
                )

                else -> delay(pollIntervalMs)
            }
        }
        throw RuntimeException("任务超时，已等待 ${maxWaitMs / 1000} 秒")
    }
}
