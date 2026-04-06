package site.addzero.kcloud.plugins.mcuconsole.flash

import androidx.compose.runtime.Composable
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.kcloud.plugins.mcuconsole.common.McuUnavailableScreen

@Route(
    value = "开发工具",
    title = "烧录",
    routePath = "mcu/flash",
    icon = "Upload",
    order = 10.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "物联网上位机",
            icon = "Build",
            order = 0,
        ),
    ),
)
@Composable
fun McuFlashScreen() {
    McuUnavailableScreen(
        featureName = "MCU 烧录",
        reason = "后台烧录与运行时接口当前未开放",
    )
}
