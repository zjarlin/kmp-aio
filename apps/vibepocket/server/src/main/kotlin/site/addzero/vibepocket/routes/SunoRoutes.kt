package site.addzero.vibepocket.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.koin.ktor.ext.inject
import site.addzero.ioc.annotation.Bean
import site.addzero.network.call.suno.SunoClient
import site.addzero.network.call.suno.model.SunoMusicRequest
import site.addzero.starter.statuspages.ErrorResponse
import site.addzero.vibepocket.dto.*
import site.addzero.vibepocket.jimmer.model.entity.MusicTask
import site.addzero.vibepocket.jimmer.model.entity.by
import site.addzero.vibepocket.jimmer.model.entity.taskId
import java.time.LocalDateTime

/**
 * Suno 音乐生成相关路由
 *
 * 依赖通过 Koin 自动注入，不需要外部传参。
 */
@Bean
fun Route.sunoRoutes() {
    val sqlClient by inject<KSqlClient>()

    route("/api/suno") {

        post("/generate") {
            val token = sqlClient.getConfig("suno.api.token")
                ?: throw IllegalArgumentException("Suno API Token 未配置，请在设置页面填写")
            val baseUrl = sqlClient.getConfig("suno.api.base_url")
                ?: "https://vector.addzero.site"

            val client = SunoClient(apiToken = token)

            val req = call.receive<GenerateRequest>()
            val sunoReq = SunoMusicRequest(
                prompt = req.prompt,
                title = req.title,
                tags = req.tags,
                mv = req.mv,
                makeInstrumental = req.makeInstrumental,
            )
            val audioUrl = client.generateMusic(sunoReq)

            val tid = audioUrl ?: "task-${System.currentTimeMillis()}"
            val now = LocalDateTime.now()
            val task = org.babyfish.jimmer.kt.new(MusicTask::class).by {
                taskId = tid
                status = if (audioUrl != null) "complete" else "queued"
                title = req.title
                tags = req.tags
                prompt = req.prompt
                mv = req.mv
                this.audioUrl = audioUrl
                createdAt = now
                updatedAt = now
            }
            val saved = sqlClient.save(task)
            call.respond(TaskResult(data = saved.modifiedEntity.toResponse()))
        }

        get("/tasks") {
            val tasks = sqlClient.createQuery(MusicTask::class) {
                select(table)
            }.execute()
            call.respond(TaskListResult(data = tasks.map { it.toResponse() }))
        }

        get("/tasks/{taskId}") {
            val tid = call.parameters["taskId"]
                ?: throw IllegalArgumentException("taskId is required")
            val task = sqlClient.createQuery(MusicTask::class) {
                where(table.taskId eq tid)
                select(table)
            }.execute().firstOrNull()

            if (task == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(404, "Task not found"))
            } else {
                call.respond(TaskResult(data = task.toResponse()))
            }
        }
    }
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
