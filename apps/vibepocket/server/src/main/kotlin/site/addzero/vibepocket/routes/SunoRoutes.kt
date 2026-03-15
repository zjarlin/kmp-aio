package site.addzero.vibepocket.routes

import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.network.call.suno.SunoClient
import site.addzero.network.call.suno.model.SunoMusicRequest
import site.addzero.springktor.runtime.SpringRouteResult
import site.addzero.springktor.runtime.springNotFound
import site.addzero.springktor.runtime.springOk
import site.addzero.starter.statuspages.ErrorResponse
import site.addzero.vibepocket.dto.GenerateRequest
import site.addzero.vibepocket.dto.TaskListResult
import site.addzero.vibepocket.dto.TaskResponse
import site.addzero.vibepocket.dto.TaskResult
import site.addzero.vibepocket.model.*
import site.addzero.vibepocket.model.by
import java.time.LocalDateTime

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
): SpringRouteResult<Any> {
    val task = sqlClient().createQuery(MusicTask::class) {
        where(table.taskId eq taskId)
        select(table)
    }.execute().firstOrNull()

    if (task == null) {
        return springNotFound(ErrorResponse(404, "Task not found"))
    }

    return springOk(TaskResult(data = task.toResponse()))
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
