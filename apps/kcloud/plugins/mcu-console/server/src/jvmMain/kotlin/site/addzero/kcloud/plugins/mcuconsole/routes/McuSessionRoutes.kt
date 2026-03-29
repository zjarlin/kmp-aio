package site.addzero.kcloud.plugins.mcuconsole.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import site.addzero.kcloud.plugins.mcuconsole.*
import site.addzero.kcloud.plugins.mcuconsole.service.McuConsoleSessionService

/**
 * MCU 串口会话相关路由。
 */
@GetMapping("/api/mcu/ports")
fun listMcuPorts(): McuPortsResponse {
    return sessionService().listPorts()
}

/**
 * 读取当前活动串口会话快照。
 */
@GetMapping("/api/mcu/session")
fun getMcuSession(): McuSessionSnapshot {
    return sessionService().getSessionSnapshot()
}

/**
 * 打开或切换当前唯一活动串口会话。
 */
@PostMapping("/api/mcu/session/open")
fun openMcuSession(
    @RequestBody request: McuSessionOpenRequest,
): McuSessionSnapshot {
    return sessionService().openSession(request)
}

/**
 * 关闭当前活动串口会话。
 */
@PostMapping("/api/mcu/session/close")
fun closeMcuSession(): McuSessionSnapshot {
    return sessionService().closeSession()
}

/**
 * 通过 DTR 脉冲执行一次设备复位。
 */
@PostMapping("/api/mcu/session/reset")
fun resetMcuSession(
    @RequestBody request: McuResetRequest,
): McuSessionSnapshot {
    return sessionService().resetSession(request)
}

/**
 * 更新 DTR 和 RTS 的显式线路状态。
 */
@PostMapping("/api/mcu/session/signals")
fun updateMcuSignals(
    @RequestBody request: McuSignalRequest,
): McuSessionSnapshot {
    return sessionService().updateSignals(request)
}

/**
 * 拉取最近一批串口事件日志。
 */
@PostMapping("/api/mcu/session/lines")
fun readMcuRecentLines(
    @RequestBody request: McuSessionLinesRequest,
): McuEventBatchResponse {
    return sessionService().readRecentEvents(request)
}

/**
 * 读取指定序号之后的增量事件。
 */
@GetMapping("/api/mcu/events")
fun readMcuEvents(
    @RequestParam("afterSeq") afterSeq: Long?,
): McuEventBatchResponse {
    return sessionService().readEvents(afterSeq ?: 0L)
}

private fun sessionService(): McuConsoleSessionService {
    return KoinPlatform.getKoin().get()
}
