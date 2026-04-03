package site.addzero.kcloud.shell.routebridge.mcu

import androidx.compose.runtime.Composable
import site.addzero.kcloud.plugins.mcuconsole.control.McuControlScreen as WasmMcuControlScreen
import site.addzero.kcloud.plugins.mcuconsole.debug.McuDebugScreen as WasmMcuDebugScreen
import site.addzero.kcloud.plugins.mcuconsole.flash.McuFlashScreen as WasmMcuFlashScreen
import site.addzero.kcloud.plugins.mcuconsole.modbus.McuModbusScreen as WasmMcuModbusScreen
import site.addzero.kcloud.plugins.mcuconsole.onlinedev.McuOnlineDevScreen as WasmMcuOnlineDevScreen

@Composable
internal actual fun McuControlRouteScreen() {
    WasmMcuControlScreen()
}

@Composable
internal actual fun McuFlashRouteScreen() {
    WasmMcuFlashScreen()
}

@Composable
internal actual fun McuModbusRouteScreen() {
    WasmMcuModbusScreen()
}

@Composable
internal actual fun McuOnlineDevRouteScreen() {
    WasmMcuOnlineDevScreen()
}

@Composable
internal actual fun McuDebugRouteScreen() {
    WasmMcuDebugScreen()
}
