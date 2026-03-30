package site.addzero.kcloud.plugins.mcuconsole.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.kcloud.plugins.mcuconsole.McuModbusTcpProbeRequest
import site.addzero.kcloud.plugins.mcuconsole.McuMqttProbeRequest
import site.addzero.kcloud.plugins.mcuconsole.McuTransportProbeResponse
import site.addzero.kcloud.plugins.mcuconsole.service.McuTransportProbeService

/**
 * 非串口传输链路探测路由，同时作为客户端 API 生成源。
 */
/**
 * 前端按钮: 控制台“验证 Modbus TCP”。
 * 作用: 按给定主机、端口和 UnitId 验证 TCP 链路是否可达。
 */
@PostMapping("/api/mcu/transport/modbus-tcp/probe")
suspend fun probeMcuModbusTcpTransport(
    @RequestBody request: McuModbusTcpProbeRequest,
): McuTransportProbeResponse {
    return transportProbeService().probeModbusTcp(request)
}

/**
 * 前端按钮: 控制台“验证 MQTT”。
 * 作用: 按给定 Broker 和认证参数发起一次 MQTT 建连探测。
 */
@PostMapping("/api/mcu/transport/mqtt/probe")
suspend fun probeMcuMqttTransport(
    @RequestBody request: McuMqttProbeRequest,
): McuTransportProbeResponse {
    return transportProbeService().probeMqtt(request)
}

private fun transportProbeService(): McuTransportProbeService {
    return KoinPlatform.getKoin().get()
}
