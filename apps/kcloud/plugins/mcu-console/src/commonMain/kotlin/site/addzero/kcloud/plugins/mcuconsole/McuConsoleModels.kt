package site.addzero.kcloud.plugins.mcuconsole

import kotlinx.serialization.Serializable

@Serializable
data class McuConsoleActionMeta(
    val id: String = "",
    val group: String = "",
)

@Serializable
data class McuConsoleActionsResponse(
    val items: List<McuConsoleActionMeta> = emptyList(),
)

@Serializable
data class McuConsolePort(
    val name: String = "",
    val path: String = "",
    val kind: String = "",
)

@Serializable
data class McuConsolePortsResponse(
    val items: List<McuConsolePort> = emptyList(),
)

@Serializable
data class McuConsoleActionResponse(
    val action: String = "",
    val accepted: Boolean = false,
    val target: String = "",
    val timestamp: String = "",
)
