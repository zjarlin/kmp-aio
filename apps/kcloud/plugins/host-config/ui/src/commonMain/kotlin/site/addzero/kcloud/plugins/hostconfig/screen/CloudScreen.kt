@file:OptIn(ExperimentalCupertinoApi::class)

package site.addzero.kcloud.plugins.hostconfig.screen.cloud

import androidx.compose.runtime.Composable
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.kcloud.plugins.hostconfig.screen.FeatureUnavailableScreen

@Route(
    title = "云接入",
    routePath = "host-config/cloud",
    icon = "Apps",
    order = 20.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "元数据配置",
            icon = "SettingsApplications",
            order = -10,
        ),
    ),
)
@Composable
fun CloudScreen() {
    FeatureUnavailableScreen(
        title = "云接入",
        subtitle = "云端对接能力后续会放到这里，当前版本先关闭入口内容。",
    )
}
