package site.addzero.kcloud.plugins.hostconfig.cloud

import site.addzero.kcloud.plugins.hostconfig.api.config.ProjectMqttConfigResponse
import site.addzero.kcloud.plugins.hostconfig.api.project.ProjectResponse

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
