package site.addzero.kcloud.plugins.hostconfig.cloud

import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectMqttConfigResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse

/**
 * 表示云接入界面状态。
 *
 * @property loading 加载状态。
 * @property busy 繁忙状态。
 * @property errorMessage 错误消息。
 * @property noticeMessage 提示消息。
 * @property projects 项目列表。
 * @property selectedProjectId 选中项目 ID。
 * @property mqttConfig MQTT配置。
 */
data class CloudScreenState(
    val loading: Boolean = true,
    val busy: Boolean = false,
    val errorMessage: String? = null,
    val noticeMessage: String? = null,
    val projects: List<ProjectResponse> = emptyList(),
    val selectedProjectId: Long? = null,
    val mqttConfig: ProjectMqttConfigResponse = defaultProjectMqttConfig(),
) {
    val selectedProject: ProjectResponse?
        get() = projects.firstOrNull { item -> item.id == selectedProjectId }
}

/**
 * 处理默认项目MQTT配置。
 */
fun defaultProjectMqttConfig(): ProjectMqttConfigResponse {
    return ProjectMqttConfigResponse(
        id = null,
        enabled = false,
        breakpointResume = false,
        gatewayName = null,
        vendor = null,
        host = null,
        port = null,
        topic = null,
        gatewayId = null,
        authEnabled = false,
        username = null,
        passwordEncrypted = null,
        tlsEnabled = false,
        certFileRef = null,
        clientId = null,
        keepAliveSec = null,
        qos = null,
        reportPeriodSec = null,
        precision = null,
        valueChangeRatioEnabled = false,
        cloudControlDisabled = false,
    )
}
