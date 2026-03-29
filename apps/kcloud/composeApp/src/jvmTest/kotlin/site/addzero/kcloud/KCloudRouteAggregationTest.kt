package site.addzero.kcloud

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.generated.RouteKeys
import site.addzero.generated.RouteTable
import site.addzero.kcloud.app.KCloudRouteCatalog

class KCloudRouteAggregationTest {
    @Test
    fun routeKeysExposeNewSystemScreens() {
        assertEquals("system/user-center/profile", RouteKeys.USER_CENTER_PROFILE_SCREEN)
        assertEquals("system/ai-chat/sessions", RouteKeys.AI_CHAT_SESSIONS_SCREEN)
        assertEquals("system/knowledge-base/spaces", RouteKeys.KNOWLEDGE_BASE_SPACES_SCREEN)
        assertEquals("system/config-center/entries", RouteKeys.CONFIG_CENTER_ENTRIES_SCREEN)
        assertEquals("system/config-center/targets", RouteKeys.CONFIG_CENTER_TARGETS_SCREEN)

        assertNotNull(RouteTable.allRoutes[RouteKeys.USER_CENTER_PROFILE_SCREEN])
        assertNotNull(RouteTable.allRoutes[RouteKeys.AI_CHAT_SESSIONS_SCREEN])
        assertNotNull(RouteTable.allRoutes[RouteKeys.KNOWLEDGE_BASE_SPACES_SCREEN])
        assertNotNull(RouteTable.allRoutes[RouteKeys.CONFIG_CENTER_ENTRIES_SCREEN])
        assertNotNull(RouteTable.allRoutes[RouteKeys.CONFIG_CENTER_TARGETS_SCREEN])
    }

    @Test
    fun systemSceneSidebarKeepsRequestedOrder() {
        val catalog = KCloudRouteCatalog()
        val systemScene = catalog.findScene("system")

        assertNotNull(systemScene)
        assertEquals(RouteKeys.RBAC_USER_SCREEN, systemScene.defaultRoutePath)
        assertEquals(
            listOf("用户中心", "插件市场", "权限中心", "AI对话", "知识库", "配置中心"),
            systemScene.menuNodes.map { it.name },
        )
        assertEquals(
            listOf("配置项", "渲染目标", "预览发布"),
            systemScene.menuNodes.last().children.map { it.name },
        )
        assertTrue(systemScene.routes.any { it.routePath == RouteKeys.USER_CENTER_PROFILE_SCREEN })
        assertTrue(systemScene.routes.any { it.routePath == RouteKeys.PLUGIN_MARKET_PACKAGES_SCREEN })
        assertTrue(systemScene.routes.any { it.routePath == RouteKeys.AI_CHAT_SESSIONS_SCREEN })
        assertTrue(systemScene.routes.any { it.routePath == RouteKeys.KNOWLEDGE_BASE_SPACES_SCREEN })
        assertEquals(
            listOf("系统", "配置中心", "渲染目标"),
            catalog.breadcrumbNamesFor(RouteKeys.CONFIG_CENTER_TARGETS_SCREEN),
        )
        assertEquals("system", catalog.sceneIdFor(RouteKeys.CONFIG_CENTER_TARGETS_SCREEN))
    }

    @Test
    fun blankSceneRoutesFallBackToUnassignedScene() {
        val catalog = KCloudRouteCatalog(
            routeMeta = listOf(
                Route(
                    title = "Loose Page",
                    routePath = "loose/page",
                    icon = "Apps",
                    order = 3.0,
                ),
            ),
        )

        val unassignedScene = catalog.findScene("unassigned")

        assertNotNull(unassignedScene)
        assertEquals("未分配场景", unassignedScene.name)
        assertEquals("loose/page", unassignedScene.defaultRoutePath)
        assertEquals(listOf("Loose Page"), unassignedScene.menuNodes.map { it.name })
    }

    @Test
    fun defaultRouteComesFromPlacementFlag() {
        val catalog = KCloudRouteCatalog(
            routeMeta = listOf(
                Route(
                    title = "Overview",
                    routePath = "custom/overview",
                    icon = "Apps",
                    order = 10.0,
                    placement = RoutePlacement(
                        scene = RouteScene(
                            id = "custom",
                            name = "Custom",
                            icon = "Apps",
                            order = 10,
                        ),
                        menuPath = arrayOf("Root"),
                    ),
                ),
                Route(
                    title = "Start Here",
                    routePath = "custom/start",
                    icon = "Apps",
                    order = 99.0,
                    placement = RoutePlacement(
                        scene = RouteScene(
                            id = "custom",
                            name = "Custom",
                            icon = "Apps",
                            order = 10,
                        ),
                        menuPath = arrayOf("Root"),
                        defaultInScene = true,
                    ),
                ),
            ),
        )

        val customScene = catalog.findScene("custom")

        assertNotNull(customScene)
        assertEquals("custom/start", customScene.defaultRoutePath)
    }
}
