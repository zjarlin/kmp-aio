package site.addzero.kcloud.plugins.mcuconsole

import io.ktor.server.routing.*
import site.addzero.esp32_host_computer.generated.modbus.rtu.registerGeneratedModbusRtuRoutes
import site.addzero.kcloud.plugins.mcuconsole.modbus.device.generated.springktor.registerGeneratedSpringRoutes

/**
 * 统一挂载 mcu-console 插件的后端路由。
 */
fun Route.mcuConsoleRoutes() {
    registerGeneratedSpringRoutes()
    registerGeneratedModbusRtuRoutes()
}
