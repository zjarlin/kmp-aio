package site.addzero.vibepocket.routes

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import site.addzero.network.call.suno.SunoClient
import site.addzero.network.call.suno.model.SunoMusicRequest
import site.addzero.vibepocket.dto.GenerateRequest
import site.addzero.vibepocket.dto.TaskListResult
import site.addzero.vibepocket.dto.TaskResponse
import site.addzero.vibepocket.dto.TaskResult
import site.addzero.vibepocket.model.*
import site.addzero.vibepocket.model.by
import java.time.LocalDateTime

private const val CALLBACK_TASK_ID_CONFIG_PREFIX = "suno.callback.taskId."
private const val CALLBACK_PAYLOAD_CONFIG_PREFIX = "suno.callback.payload."

/**
 * Suno 音乐生成相关路由
 *
 * 依赖通过 Koin 自动注入，不需要外部传参。
 */
@PostMapping("/api/suno/generate")
suspend fun generateMusic(
    @RequestBody request: GenerateRequest,
): TaskResult {
    val sqlClient = sqlClient()
    val token = sqlClient.getConfig("suno.api.token")
        ?: throw IllegalArgumentException("Suno API Token 未配置，请在设置页面填写")

    val client = SunoClient(apiToken = token)
    val sunoRequest = SunoMusicRequest(
        prompt = request.prompt,
        title = request.title,
        tags = request.tags,
        mv = request.mv,
        makeInstrumental = request.makeInstrumental,
    )
    val audioUrl = client.generateMusic(sunoRequest)

    val now = LocalDateTime.now()
    val task = new(MusicTask::class).by {
        taskId = audioUrl
        status = "complete"
        title = request.title
        tags = request.tags
        prompt = request.prompt
        mv = request.mv
        this.audioUrl = audioUrl
        createdAt = now
        updatedAt = now
    }
    val saved = sqlClient.save(task)
    return TaskResult(data = saved.modifiedEntity.toResponse())
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
        where(table.taskId eq taskId)
        select(table)
    }.execute().firstOrNull()

    if (task == null) {
        throw NoSuchElementException("Task not found")
    }

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
        "[SunoCallback] kind=$kind requestId=${normalizedRequestId ?: "-"} taskId=${taskId ?: "-"} callbackType=${callbackType ?: "-"} code=${code ?: "-"} msg=${message ?: "-"} payload=$payload"
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

private suspend fun KSqlClient.getConfig(key: String): String? {
    return compatService().getOrImportLegacyValue(
        namespace = "vibepocket",
        key = key,
    )
}

private suspend fun KSqlClient.setConfig(
    key: String,
    value: String,
    description: String? = null,
) {
    compatService().saveLegacyValue(
        namespace = "vibepocket",
        key = key,
        value = value,
        description = description,
    )
}

private fun compatService(): site.addzero.configcenter.runtime.ConfigCenterCompatService {
    return KoinPlatform.getKoin().get()
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
