package site.addzero.kcloud.plugins.mcuconsole.control

import androidx.compose.runtime.Composable
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.kcloud.plugins.mcuconsole.common.McuUnavailableScreen

@Route(
    value = "设备会话",
    title = "控制台",
    routePath = "mcu/control",
    icon = "PowerSettingsNew",
    order = 0.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "物联网上位机",
            icon = "Build",
            order = 0,
        ),
        defaultInScene = true,
    ),
)
@Composable
fun McuControlScreen() {
    McuUnavailableScreen(
        featureName = "MCU 控制台",
        reason = "后台会话能力当前不完整，暂未开放",
    )
}
