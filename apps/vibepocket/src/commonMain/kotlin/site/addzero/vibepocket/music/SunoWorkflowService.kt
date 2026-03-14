package site.addzero.vibepocket.music

import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.delay
import site.addzero.vibepocket.api.suno.SunoApiClient
import site.addzero.vibepocket.api.suno.SunoBoostStyleData
import site.addzero.vibepocket.api.suno.SunoBoostStyleRequest
import site.addzero.vibepocket.api.suno.SunoGeneratePersonaRequest
import site.addzero.vibepocket.api.suno.SunoLyricsRequest
import site.addzero.vibepocket.api.suno.SunoLyricItem
import site.addzero.vibepocket.api.suno.SunoPersonaData
import site.addzero.vibepocket.api.suno.SunoTaskDetail

object SunoWorkflowService {
    internal var configLoader: suspend () -> SunoRuntimeConfig = ::loadSunoRuntimeConfig
    internal var configSaver: suspend (String, String) -> Unit = ::persistSunoRuntimeConfig
    internal var clientFactory: (SunoRuntimeConfig) -> SunoApiClient = { config -> config.createClient() }

    suspend fun loadConfig(): SunoRuntimeConfig {
        return runCatching { configLoader() }.getOrDefault(SunoRuntimeConfig())
    }

    suspend fun saveConfig(
        apiToken: String,
        baseUrl: String,
    ) {
        configSaver(apiToken, baseUrl)
    }

    suspend fun getCreditsOrNull(): Int? {
        return runCatching {
            withClient { client -> client.getCredits() }
        }.getOrNull()
    }

    suspend fun generateLyricsCandidates(
        prompt: String,
        onStatusUpdate: ((String) -> Unit)? = null,
        maxWaitMs: Long = 300_000L,
        pollIntervalMs: Long = 5_000L,
    ): List<SunoLyricItem> {
        return withClient { client ->
            onStatusUpdate?.invoke("正在提交...")
            val taskId = client.generateLyrics(SunoLyricsRequest(prompt = prompt))
            onStatusUpdate?.invoke("已提交，轮询中...")

            val startTime = getTimeMillis()
            var lyrics: List<SunoLyricItem>? = null
            while (lyrics == null) {
                val elapsed = getTimeMillis() - startTime
                if (elapsed > maxWaitMs) {
                    throw RuntimeException("歌词生成超时，已等待 ${maxWaitMs / 1000} 秒")
                }

                val detail = client.getLyricsDetail(taskId)
                when {
                    detail?.isSuccess == true -> {
                        lyrics = detail.response?.data?.filter { !it.text.isNullOrBlank() }.orEmpty()
                    }

                    detail?.isFailed == true -> {
                        throw RuntimeException(detail.errorMessage ?: detail.errorCode ?: "歌词生成失败")
                    }

                    else -> {
                        onStatusUpdate?.invoke(lyricStatusText(detail?.status))
                        delay(pollIntervalMs)
                    }
                }
            }
            lyrics
        }
    }

    suspend fun submitTask(
        submit: suspend (SunoApiClient) -> String,
        onStatusUpdate: ((String, SunoTaskDetail?) -> Unit)? = null,
        maxWaitMs: Long = 600_000L,
        pollIntervalMs: Long = 30_000L,
    ): SunoTaskDetail {
        return withClient { client ->
            onStatusUpdate?.invoke("正在提交...", null)
            val taskId = submit(client)
            onStatusUpdate?.invoke("已提交，轮询中...", null)
            client.waitForCompletion(
                taskId = taskId,
                maxWaitMs = maxWaitMs,
                pollIntervalMs = pollIntervalMs,
                onStatusUpdate = { detail: SunoTaskDetail? ->
                    onStatusUpdate?.invoke(detail?.displayStatus ?: "轮询中...", detail)
                },
            )
        }
    }

    suspend fun boostStyle(request: SunoBoostStyleRequest): SunoBoostStyleData {
        return withClient { client -> client.boostMusicStyle(request) }
    }

    suspend fun generatePersona(request: SunoGeneratePersonaRequest): SunoPersonaData {
        return withClient { client -> client.generatePersona(request) }
    }

    suspend fun <T> withClient(action: suspend (SunoApiClient) -> T): T {
        val config = loadConfig()
        config.requireToken()
        return action(clientFactory(config))
    }

    fun errorMessage(error: Throwable): String {
        val message = error.message?.trim().orEmpty()
        return if (message.isBlank()) {
            "请求 Suno 服务失败"
        } else {
            message
        }
    }

    private fun lyricStatusText(status: String?): String {
        return when (status) {
            "SUCCESS" -> "已完成 ✓"
            "FAILED" -> "失败 ✗"
            "QUEUED" -> "排队中..."
            "PROCESSING" -> "生成中..."
            null -> "生成中..."
            else -> status
        }
    }

    internal fun resetForTests() {
        configLoader = ::loadSunoRuntimeConfig
        configSaver = ::persistSunoRuntimeConfig
        clientFactory = { config -> config.createClient() }
    }
}
