package site.addzero.vibepocket.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 音乐生成请求 */
@Serializable
data class GenerateRequest(
    val prompt: String,
    val title: String? = null,
    val tags: String? = null,
    val mv: String = "chirp-v5",
    @SerialName("make_instrumental")
    val makeInstrumental: Boolean = false,
)

/** 任务状态响应 */
@Serializable
data class TaskResponse(
    val id: Long,
    @SerialName("task_id")
    val taskId: String,
    val status: String,
    val title: String? = null,
    val tags: String? = null,
    val prompt: String? = null,
    val mv: String? = null,
    @SerialName("audio_url")
    val audioUrl: String? = null,
    @SerialName("video_url")
    val videoUrl: String? = null,
    @SerialName("error_message")
    val errorMessage: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
)

/** 音乐搜索请求 */
@Serializable
data class SearchRequest(
    val query: String,
    val limit: Int = 20,
)

/** 成功确认响应 */
@Serializable
data class OkResponse(
    val ok: Boolean = true,
    val message: String = "success"
)

/** 搜索结果包装（通用） */
@Serializable
data class SearchResult<T>(
    val code: Int = 200,
    val message: String = "ok",
    val data: List<T> = emptyList()
)

/** 音乐搜索结果 DTO */
@Serializable
data class MusicSearchResponse(
    val code: Int = 200,
    val message: String = "ok",
    // 兼容可能存在的多种返回格式，使用 JsonObject 或 Any 处理暂未确定的结构
    val data: kotlinx.serialization.json.JsonElement? = null
)

/** S3 上传响应 */
@Serializable
data class S3UploadResponse(
    val key: String,
    val url: String
)

/** S3 URL 响应 */
@Serializable
data class S3UrlResponse(
    val url: String
)

/** S3 对象信息 */
@Serializable
data class S3ObjectDto(
    val key: String,
    val size: Long,
    val lastModified: String
)

/** 单任务响应 */
@Serializable
data class TaskResult(
    val code: Int = 200,
    val message: String = "ok",
    val data: TaskResponse? = null,
)

/** 任务列表响应 */
@Serializable
data class TaskListResult(
    val code: Int = 200,
    val message: String = "ok",
    val data: List<TaskResponse> = emptyList(),
)
