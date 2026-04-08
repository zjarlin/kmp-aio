package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.serial.SerialPortConfig

/**
 * 原始Controller: site.addzero.kcloud.plugins.mcuconsole.routes.SerialPortController
 * 基础路径: /mcu-console/router/ports
 */
interface SerialPortApi {

/**
 * getSerialPorts
 * HTTP方法: GET
 * 路径: /mcu-console/router/ports/list
 * 返回类型: kotlin.collections.List<site.addzero.serial.SerialPortDescriptor>
 */
    @GET("/mcu-console/router/ports/list")
    suspend fun getSerialPorts(): kotlin.collections.List<site.addzero.serial.SerialPortDescriptor>

/**
 * open
 * HTTP方法: POST
 * 路径: /mcu-console/router/ports/open
 * 参数:
 *   - serialPortConfig: site.addzero.serial.SerialPortConfig (RequestBody)
 * 返回类型: kotlin.collections.List<site.addzero.serial.SerialPortDescriptor>
 */
    @POST("/mcu-console/router/ports/open")
    @Headers("Content-Type: application/json")
    suspend fun open(
        @Body serialPortConfig: site.addzero.serial.SerialPortConfig
    ): kotlin.collections.List<site.addzero.serial.SerialPortDescriptor>

}