package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.mcuconsole.McuTransportProbeResponse
import site.addzero.kcloud.plugins.mcuconsole.McuModbusTcpProbeRequest
import site.addzero.kcloud.plugins.mcuconsole.McuMqttProbeRequest

/**
 * 原始文件: site.addzero.kcloud.plugins.mcuconsole.routes.McuTransport.kt
 * 基础路径: 
 */
interface McuTransportApi {

/**
 * probeMcuModbusTcpTransport
 * HTTP方法: POST
 * 路径: /api/mcu/transport/modbus-tcp/probe
 * 参数:
 *   - request: site.addzero.kcloud.plugins.mcuconsole.McuModbusTcpProbeRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuTransportProbeResponse
 */
    @POST("/api/mcu/transport/modbus-tcp/probe")    suspend fun probeMcuModbusTcpTransport(
        @Body request: site.addzero.kcloud.plugins.mcuconsole.McuModbusTcpProbeRequest
    ): site.addzero.kcloud.plugins.mcuconsole.McuTransportProbeResponse

/**
 * probeMcuMqttTransport
 * HTTP方法: POST
 * 路径: /api/mcu/transport/mqtt/probe
 * 参数:
 *   - request: site.addzero.kcloud.plugins.mcuconsole.McuMqttProbeRequest (RequestBody)
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.McuTransportProbeResponse
 */
    @POST("/api/mcu/transport/mqtt/probe")    suspend fun probeMcuMqttTransport(
        @Body request: site.addzero.kcloud.plugins.mcuconsole.McuMqttProbeRequest
    ): site.addzero.kcloud.plugins.mcuconsole.McuTransportProbeResponse

}