package site.addzero.kcloud.plugins.mcuconsole.debug

import androidx.compose.runtime.Composable
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.component.text.TodoText

@Route(
    value = "开发工具",
    title = "调试",
    routePath = "mcu/debug",
    icon = "BugReport",
    order = 20.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "上位机",
            icon = "Build",
            order = 0,
        ),
    ),
)
@Composable
fun McuDebugScreen() {
    TodoText(
        title = "MCU 调试",
        description = "后台调试日志与运行时接口当前未开放",
    )
}
