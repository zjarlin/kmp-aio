package site.addzero.kcloud.vibepocket.routes

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import site.addzero.kcloud.plugins.system.configcenter.spi.ConfigValueServiceSpi
import site.addzero.kcloud.api.suno.SunoApiClient
import site.addzero.kcloud.api.suno.SunoGenerateRequest
import site.addzero.kcloud.vibepocket.dto.GenerateRequest
import site.addzero.kcloud.vibepocket.dto.TaskListResult
import site.addzero.kcloud.vibepocket.dto.TaskResponse
import site.addzero.kcloud.vibepocket.dto.TaskResult
import site.addzero.kcloud.vibepocket.model.MusicTask
import site.addzero.kcloud.vibepocket.model.MusicTaskDraft
import java.time.LocalDateTime

private const val CALLBACK_TASK_ID_CONFIG_PREFIX = "suno.callback.taskId."
private const val CALLBACK_PAYLOAD_CONFIG_PREFIX = "suno.callback.payload."

@PostMapping("/api/suno/generate")
suspend fun generateMusic(
    @RequestBody request: GenerateRequest,
): TaskResult {
    val sqlClient = sqlClient()
    val token = sqlClient.getConfig("suno.api.token")
        ?: throw IllegalArgumentException("Suno API Token 未配置，请在设置页面填写")

    val client = SunoApiClient(apiToken = token)
    val taskId = client.generateMusic(
        SunoGenerateRequest(
            prompt = request.prompt,
            title = request.title,
            style = request.tags,
            model = request.mv,
            instrumental = request.makeInstrumental,
        ),
    )

    val now = LocalDateTime.now()
    val task = MusicTaskDraft.`$`.produce {
        this.taskId = taskId
        status = "queued"
        title = request.title
        tags = request.tags
        prompt = request.prompt
        mv = request.mv
        audioUrl = null
        videoUrl = null
        errorMessage = null
        createdAt = now
        updatedAt = now
    }
    return TaskResult(data = sqlClient.save(task).modifiedEntity.toResponse())
}

@GetMapping("/api/suno/tasks")
suspend fun listTasks(): TaskListResult {
    val tasks = sqlClient().createQuery(MusicTask::class) {
        select(table)
    }.execute()
    return TaskListResult(data = tasks.map { it.toResponse() })
}

@GetMapping("/api/suno/tasks/{taskId}")
suspend fun readTask(
    @PathVariable taskId: String,
): TaskResult {
    val task = sqlClient().createQuery(MusicTask::class) {
        select(table)
    }.execute().firstOrNull { it.taskId == taskId }
        ?: throw NoSuchElementException("Task not found: $taskId")

    return TaskResult(data = task.toResponse())
}

@PostMapping("/api/suno/callback/{kind}")
suspend fun handleSunoCallback(
    @PathVariable kind: String,
    @RequestParam("requestId", required = false) requestId: String?,
    @RequestBody payload: JsonElement,
): String {
    val normalizedRequestId = requestId?.trim().orEmpty().ifBlank { null }
    val taskId = payload.stringValue("data", "task_id")
        ?: payload.stringValue("data", "taskId")
        ?: payload.stringValue("task_id")
        ?: payload.stringValue("taskId")
    val callbackType = payload.stringValue("data", "callbackType")
        ?: payload.stringValue("callbackType")
    val code = payload.intValue("code")
    val message = payload.stringValue("msg")
        ?: payload.stringValue("message")

    println(
        "[SunoCallback] kind=$kind requestId=${normalizedRequestId ?: "-"} taskId=${taskId ?: "-"} callbackType=${callbackType ?: "-"} code=${code ?: "-"} msg=${message ?: "-"} payload=$payload",
    )

    if (normalizedRequestId != null) {
        val sqlClient = sqlClient()
        sqlClient.setConfig(
            callbackPayloadConfigKey(normalizedRequestId),
            payload.toString(),
            "Suno callback payload",
        )
        if (!taskId.isNullOrBlank()) {
            sqlClient.setConfig(
                callbackTaskIdConfigKey(normalizedRequestId),
                taskId,
                "Suno callback recovered taskId",
            )
        }
    }
    return "ok"
}

private fun MusicTask.toResponse() = TaskResponse(
    id = id,
    taskId = taskId,
    status = status,
    title = title,
    tags = tags,
    prompt = prompt,
    mv = mv,
    audioUrl = audioUrl,
    videoUrl = videoUrl,
    errorMessage = errorMessage,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)

private fun sqlClient(): KSqlClient {
    return KoinPlatform.getKoin().get()
}

private fun configCenterService(): ConfigValueServiceSpi {
    return KoinPlatform.getKoin().get()
}

private fun KSqlClient.readLegacyConfigValue(key: String): String? {
    return createQuery(site.addzero.kcloud.vibepocket.model.AppConfig::class) {
        select(table)
    }.execute().firstOrNull { config -> config.key == key }?.value
}

private suspend fun KSqlClient.getConfig(key: String): String? {
    configCenterService().readValue(
        namespace = "vibepocket",
        key = key,
    ).value?.let { value ->
        return value
    }
    val legacyValue = readLegacyConfigValue(key) ?: return null
    configCenterService().writeValue(
        namespace = "vibepocket",
        key = key,
        value = legacyValue,
    )
    return legacyValue
}

private suspend fun KSqlClient.setConfig(
    key: String,
    value: String,
    _description: String? = null,
) {
    configCenterService().writeValue(
        namespace = "vibepocket",
        key = key,
        value = value,
    )
}

private fun callbackTaskIdConfigKey(requestId: String): String {
    return CALLBACK_TASK_ID_CONFIG_PREFIX + requestId
}

private fun callbackPayloadConfigKey(requestId: String): String {
    return CALLBACK_PAYLOAD_CONFIG_PREFIX + requestId
}

private fun JsonElement.stringValue(vararg keys: String): String? {
    var current: JsonElement = this
    keys.forEach { key ->
        current = current.jsonObject[key] ?: return null
    }
    return current.jsonPrimitive.contentOrNull
}

private fun JsonElement.intValue(vararg keys: String): Int? {
    var current: JsonElement = this
    keys.forEach { key ->
        current = current.jsonObject[key] ?: return null
    }
    return current.jsonPrimitive.intOrNull
}
