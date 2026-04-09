@file:OptIn(ExperimentalCupertinoApi::class)

package site.addzero.kcloud.plugins.hostconfig.screen

import androidx.compose.runtime.Composable
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene

@Route(
    title = "网关配置",
    routePath = "host-config/gateway",
    icon = "SettingsInputComponent",
    order = 30.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "元数据配置",
            icon = "SettingsApplications",
            order = -10,
        ),
    ),
)
@Composable
fun GatewayScreen() {
//    TodoText("")
    FeatureUnavailableScreen(
        title = "网关配置",
        subtitle = "网关接入配置后续会放到这里，当前版本先关闭入口内容。",
    )
}
