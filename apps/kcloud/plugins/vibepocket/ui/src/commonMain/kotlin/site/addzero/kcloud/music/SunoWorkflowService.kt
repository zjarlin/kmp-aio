package site.addzero.kcloud.music

import io.ktor.http.*
import io.ktor.util.date.*
import kotlinx.coroutines.delay
import site.addzero.kcloud.api.ServerApiClient
import site.addzero.kcloud.api.suno.*
import kotlin.random.Random

object SunoWorkflowService {
    private const val CALLBACK_REQUEST_ID_QUERY = "requestId"
    private const val CALLBACK_TASK_ID_CONFIG_PREFIX = "suno.callback.taskId."
    private const val CALLBACK_RECOVERY_POLL_INTERVAL_MS = 2_000L

    private data class CallbackReservation(
        val requestId: String,
        val callbackUrl: String,
        val taskIdConfigKey: String,
    )

    internal var configLoader: suspend () -> SunoRuntimeConfig = ::loadSunoRuntimeConfig
    internal var configSaver: suspend (String, String, String) -> Unit = ::persistSunoRuntimeConfig
    internal var clientFactory: (SunoRuntimeConfig) -> SunoApiClient = { config -> config.createClient() }

    private fun workflowDebug(message: String) {
        println("[SunoWorkflowService] $message")
    }

    suspend fun loadConfig(): SunoRuntimeConfig {
        return runCatching { configLoader() }.getOrDefault(SunoRuntimeConfig())
    }

    suspend fun saveConfig(
        apiToken: String,
        baseUrl: String,
        callbackUrl: String = "",
    ) {
        configSaver(apiToken, baseUrl, callbackUrl)
    }

    suspend fun getCreditsOrNull(): Int? {
        return runCatching {
            withClient { client -> client.getCredits() }
        }.onFailure { error ->
            workflowDebug("getCreditsOrNull failed: ${error::class.simpleName}: ${error.message}")
        }.getOrNull()
    }

    suspend fun prepareUploadSourceUrl(rawUrl: String): String {
        val normalizedUrl = rawUrl.trim()
        if (normalizedUrl.isBlank()) {
            throw IllegalArgumentException("请输入音频 URL")
        }
        if (normalizedUrl.isSunoHostedUploadUrl()) {
            return normalizedUrl
        }
        return withClient { client ->
            workflowDebug("prepareUploadSourceUrl sourceUrl=$normalizedUrl")
            client.uploadRemoteFile(
                fileUrl = normalizedUrl,
                uploadPath = "audio",
            ).resolvedUrl
        }
    }

    suspend fun uploadLocalAudioSource(
        bytes: ByteArray,
        fileName: String,
        contentType: String? = null,
    ): SunoUploadedFileData {
        return withClient { client ->
            workflowDebug(
                "uploadLocalAudioSource fileName=$fileName size=${bytes.size} contentType=${contentType ?: "-"}"
            )
            client.uploadLocalFile(
                bytes = bytes,
                fileName = fileName,
                contentType = contentType,
                uploadPath = "audio",
            )
        }
    }

    suspend fun generateLyricsCandidates(
        prompt: String,
        onStatusUpdate: ((String) -> Unit)? = null,
        maxWaitMs: Long = 300_000L,
        pollIntervalMs: Long = 5_000L,
    ): List<SunoLyricItem> {
        val config = loadConfig()
        val callbackUrl = config.callbackUrlOrNull()
        return withClient { client ->
            onStatusUpdate?.invoke("正在提交...")
            val taskId = client.generateLyrics(
                SunoLyricsRequest(
                    prompt = prompt,
                    callBackUrl = callbackUrl,
                )
            )
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
        actionLabel: String,
        submit: suspend (SunoApiClient, String?) -> String,
        onStatusUpdate: ((String, SunoTaskDetail?) -> Unit)? = null,
        onTaskAccepted: ((String) -> Unit)? = null,
        maxWaitMs: Long = 600_000L,
        pollIntervalMs: Long = 30_000L,
    ): SunoTaskDetail {
        val config = loadConfig()
        config.requireToken()
        val callbackReservation = config.callbackUrlOrNull()?.let(::createCallbackReservation)
        workflowDebug("withClient baseUrl=${config.baseUrl} hasToken=${config.hasToken}")
        val client = clientFactory(config)
        workflowDebug(
            "submitTask start action=$actionLabel requestId=${callbackReservation?.requestId ?: "-"} maxWaitMs=$maxWaitMs pollIntervalMs=$pollIntervalMs"
        )
        onStatusUpdate?.invoke("正在提交...", null)
        val taskId = try {
            submit(client, callbackReservation?.callbackUrl)
        } catch (error: Throwable) {
            recoverTaskIdFromCallback(
                error = error,
                callbackReservation = callbackReservation,
                maxWaitMs = maxWaitMs,
                onStatusUpdate = onStatusUpdate,
            )
        }
        workflowDebug("submitTask accepted taskId=$taskId")
        onTaskAccepted?.invoke(taskId)
        onStatusUpdate?.invoke("已提交，轮询中...", null)
        val detail = client.waitForCompletion(
            taskId = taskId,
            maxWaitMs = maxWaitMs,
            pollIntervalMs = pollIntervalMs,
            onStatusUpdate = { detail: SunoTaskDetail? ->
                workflowDebug(
                    "poll taskId=$taskId status=${detail?.status} error=${detail?.errorMessage ?: detail?.errorCode}"
                )
                onStatusUpdate?.invoke(detail?.displayStatus ?: "轮询中...", detail)
            },
        )
        workflowDebug(
            "submitTask completed taskId=$taskId finalStatus=${detail.status} error=${detail.errorMessage ?: detail.errorCode}"
        )
        return detail
    }

    suspend fun boostStyle(request: SunoBoostStyleRequest): SunoBoostStyleData {
        workflowDebug("boostStyle request=$request")
        return withClient { client -> client.boostMusicStyle(request) }
    }

    suspend fun getTaskDetailOrNull(taskId: String): SunoTaskDetail? {
        return withClient { client ->
            workflowDebug("getTaskDetailOrNull taskId=$taskId")
            client.getTaskDetail(taskId)
        }
    }

    suspend fun generatePersona(request: SunoGeneratePersonaRequest): SunoPersonaData {
        workflowDebug("generatePersona request=$request")
        return withClient { client -> client.generatePersona(request) }
    }

    suspend fun <T> withClient(action: suspend (SunoApiClient) -> T): T {
        val config = loadConfig()
        config.requireToken()
        workflowDebug("withClient baseUrl=${config.baseUrl} hasToken=${config.hasToken}")
        return action(clientFactory(config))
    }

    fun errorMessage(error: Throwable): String {
        val message = error.message?.trim().orEmpty()
        val isTransientNetworkError = error::class.simpleName == "EOFException" ||
            message.contains("Not enough data available", ignoreCase = true) ||
            message.contains("unexpected end", ignoreCase = true) ||
            message.contains("connection reset", ignoreCase = true)
        if (isTransientNetworkError) {
            return "Suno 服务连接被中断。如果你配置了 Callback URL，我会尽量自动恢复任务；如果没配置，只能重新提交一次。"
        }
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

    private suspend fun recoverTaskIdFromCallback(
        error: Throwable,
        callbackReservation: CallbackReservation?,
        maxWaitMs: Long,
        onStatusUpdate: ((String, SunoTaskDetail?) -> Unit)?,
    ): String {
        if (!error.isTransientSubmitError()) {
            throw error
        }
        if (callbackReservation == null) {
            throw RuntimeException(
                "Suno 提交响应被中断，而且当前没有配置 Callback URL，客户端无法自动找回 taskId。请直接再点一次提交。"
            )
        }

        workflowDebug(
            "submit response interrupted, waiting callback recovery requestId=${callbackReservation.requestId}: ${error::class.simpleName}: ${error.message}"
        )
        onStatusUpdate?.invoke("提交响应被中断，正在等待 Suno callback 确认任务...", null)

        val recoveryDeadline = getTimeMillis() + maxWaitMs
        while (getTimeMillis() < recoveryDeadline) {
            val recoveredTaskId = ServerApiClient.getConfig(callbackReservation.taskIdConfigKey)
                ?.trim()
                .orEmpty()
            if (recoveredTaskId.isNotBlank()) {
                workflowDebug(
                    "callback recovery hit requestId=${callbackReservation.requestId} taskId=$recoveredTaskId"
                )
                onStatusUpdate?.invoke("已从 callback 恢复任务，继续轮询...", null)
                return recoveredTaskId
            }
            delay(CALLBACK_RECOVERY_POLL_INTERVAL_MS)
        }

        throw RuntimeException(
            "Suno 提交响应被中断，且在 ${maxWaitMs / 1000} 秒内没有等到 callback 回传任务号。请确认设置页里的 Callback URL 能被公网访问，或者直接重提一次。"
        )
    }

    private fun createCallbackReservation(baseCallbackUrl: String): CallbackReservation {
        val requestId = buildCallbackRequestId()
        return CallbackReservation(
            requestId = requestId,
            callbackUrl = appendQueryParameter(
                url = baseCallbackUrl.trim(),
                name = CALLBACK_REQUEST_ID_QUERY,
                value = requestId,
            ),
            taskIdConfigKey = callbackTaskIdConfigKey(requestId),
        )
    }

    private fun buildCallbackRequestId(): String {
        val randomSuffix = Random.nextInt(100_000, 999_999)
        return "vp-${getTimeMillis()}-$randomSuffix"
    }

    private fun callbackTaskIdConfigKey(requestId: String): String {
        return CALLBACK_TASK_ID_CONFIG_PREFIX + requestId
    }

    private fun appendQueryParameter(
        url: String,
        name: String,
        value: String,
    ): String {
        val anchor = url.substringAfter('#', "")
        val baseUrl = url.substringBefore('#')
        val separator = if (baseUrl.contains('?')) '&' else '?'
        val parameter = "${name.encodeURLQueryComponent()}=${value.encodeURLQueryComponent()}"
        return buildString {
            append(baseUrl)
            append(separator)
            append(parameter)
            if (anchor.isNotBlank()) {
                append('#')
                append(anchor)
            }
        }
    }

    private fun Throwable.isTransientSubmitError(): Boolean {
        val typeName = this::class.simpleName.orEmpty()
        val text = message.orEmpty()
        return typeName == "EOFException" ||
            typeName.contains("Timeout", ignoreCase = true) ||
            text.contains("Not enough data available", ignoreCase = true) ||
            text.contains("unexpected end", ignoreCase = true) ||
            text.contains("connection reset", ignoreCase = true) ||
            text.contains("broken pipe", ignoreCase = true)
    }

    private fun String.isSunoHostedUploadUrl(): Boolean {
        val host = substringAfter("://", "")
            .substringBefore('/')
            .substringBefore('?')
            .substringBefore('#')
            .trim()
            .lowercase()
        return host.endsWith("redpandaai.co")
    }
}
