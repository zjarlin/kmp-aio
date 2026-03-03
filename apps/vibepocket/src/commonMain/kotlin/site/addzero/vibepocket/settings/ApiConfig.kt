package site.addzero.vibepocket.settings

import kotlinx.serialization.Serializable

/**
 * 单个 API 服务的连接配置信息。
 *
 * @property key API Key / Token
 * @property baseUrl API 端点 Base URL
 * @property label 人类可读标签，用于在设置页面中标识该配置项
 */
@Serializable
data class ApiConfig(
    val key: String = "",
    val baseUrl: String = "",
    val label: String = ""
)

/**
 * 按模块聚合的 API 配置集合。
 *
 * 每个模块持有一个 [ApiConfig] 列表，对应该模块所需的各项 API 服务配置。
 * 音乐模块预置了 Suno Token 和 Suno Base URL 两项配置；图片和视频模块暂为空列表，预留未来扩展。
 *
 * @property music 音乐模块配置（Suno Token, Suno URL）
 * @property image 图片模块配置（预留）
 * @property video 视频模块配置（预留）
 */
@Serializable
data class ModuleConfigs(
    val music: List<ApiConfig> = listOf(
        ApiConfig(label = "Suno API Token"),
        ApiConfig(label = "Suno API Base URL", baseUrl = "https://api.sunoapi.org/api/v1"),
    ),
    val image: List<ApiConfig> = emptyList(),
    val video: List<ApiConfig> = emptyList()
)
