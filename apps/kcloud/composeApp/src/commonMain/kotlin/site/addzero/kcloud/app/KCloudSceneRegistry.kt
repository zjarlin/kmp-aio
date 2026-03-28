package site.addzero.kcloud.app

import site.addzero.generated.RouteKeys

/**
 * 场景定义
 * @author zjarlin
 * @date 2026/03/28
 * @constructor 创建[KCloudSceneDefinition]
 * @param [id]
 * @param [name]
 * @param [iconName]
 * @param [sort]
 * @param [routePathPrefixes]
 * @param [routePaths]
 * @param [defaultRoutePath]
 */
data class KCloudSceneDefinition(
    val id: String,
    val name: String,
    val iconName: String = "Apps",
    val sort: Int = Int.MAX_VALUE,
    val routePathPrefixes: List<String> = emptyList(),
    val routePaths: Set<String> = emptySet(),
    val defaultRoutePath: String? = null,
) {
    fun matches(
        routePath: String,
    ): Boolean {
        return routePath in routePaths || routePathPrefixes.any { prefix ->
            routePath.startsWith(prefix)
        }
    }
}

object KCloudSceneRegistry {
    val all = listOf(
        KCloudSceneDefinition(
            id = "device",
            name = "设备",
            iconName = "Build",
            sort = 0,
            routePathPrefixes = listOf("mcu/"),
            defaultRoutePath = RouteKeys.MCU_CONTROL_SCREEN,
        ),
        KCloudSceneDefinition(
            id = "system",
            name = "系统",
            iconName = "AdminPanelSettings",
            sort = 100,
            routePathPrefixes = listOf("system/"),
            defaultRoutePath = RouteKeys.RBAC_USER_SCREEN,
        ),
        KCloudSceneDefinition(
            id = "music-creation",
            name = "音乐创作",
            iconName = "PlayArrow",
            sort = 200,
            routePathPrefixes = listOf("vibepocket/"),
            defaultRoutePath = RouteKeys.MUSIC_STUDIO_SCREEN,
        ),
    )
}
