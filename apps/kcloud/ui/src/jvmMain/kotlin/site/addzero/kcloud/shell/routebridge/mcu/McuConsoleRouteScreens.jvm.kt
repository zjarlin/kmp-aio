package site.addzero.kcloud.shell.routebridge.mcu

import androidx.compose.runtime.Composable
import site.addzero.kcloud.plugins.mcuconsole.control.McuControlScreen as RealMcuControlScreen
import site.addzero.kcloud.plugins.mcuconsole.debug.McuDebugScreen as RealMcuDebugScreen
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashScreen as RealMcuFlashScreen
import site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusScreen as RealMcuModbusScreen
import site.addzero.kcloud.plugins.mcuconsole.onlinedev.McuOnlineDevScreen as RealMcuOnlineDevScreen

@Composable
internal actual fun McuControlRouteScreen() {
    RealMcuControlScreen()
}

@Composable
internal actual fun McuFlashRouteScreen() {
    RealMcuFlashScreen()
}

@Composable
internal actual fun McuModbusRouteScreen() {
    RealMcuModbusScreen()
}

@Composable
internal actual fun McuOnlineDevRouteScreen() {
    RealMcuOnlineDevScreen()
}

@Composable
internal actual fun McuDebugRouteScreen() {
    RealMcuDebugScreen()
}
