package site.addzero.kcloud.plugins.mcuconsole.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.kcloud.plugins.mcuconsole.McuScriptExecuteRequest
import site.addzero.kcloud.plugins.mcuconsole.McuScriptStatusResponse
import site.addzero.kcloud.plugins.mcuconsole.McuScriptStopRequest
import site.addzero.kcloud.plugins.mcuconsole.service.McuScriptService

/**
 * MCU 脚本执行服务端路由定义，同时作为客户端 API 生成源。
 */
/**
 * 前端按钮: 在线开发页“执行脚本”。
 * 作用: 向设备发送脚本执行请求。
 */
@PostMapping("/api/mcu/script/execute")
fun executeMcuScript(
    @RequestBody request: McuScriptExecuteRequest,
): McuScriptStatusResponse {
    return scriptService().execute(request)
}

/**
 * 前端按钮: 在线开发页“停止脚本”。
 * 作用: 终止当前活动脚本。
 */
@PostMapping("/api/mcu/script/stop")
fun stopMcuScript(
    @RequestBody request: McuScriptStopRequest,
): McuScriptStatusResponse {
    return scriptService().stop(request)
}

/**
 * 前端按钮: 在线开发页“刷新”、调试页“刷新”。
 * 作用: 查询当前脚本状态，并顺带向设备发起一次状态探测。
 */
@GetMapping("/api/mcu/script/status")
fun getMcuScriptStatus(): McuScriptStatusResponse {
    return scriptService().queryStatus()
}

private fun scriptService(): McuScriptService {
    return KoinPlatform.getKoin().get()
}
