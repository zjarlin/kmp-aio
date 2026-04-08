package site.addzero.kcloud.plugins.mcuconsole.api.external

import de.jensklingenberg.ktorfit.http.*
import site.addzero.kcloud.plugins.mcuconsole.modbus.device.DeviceRuntimeInfo
import site.addzero.kcloud.plugins.mcuconsole.modbus.device.Device24PowerLights

/**
 * 原始Controller: site.addzero.kcloud.plugins.mcuconsole.routes.DeviceInfoController
 * 基础路径: /device
 */
interface DeviceInfoApi {

/**
 * getDeviceInfo
 * HTTP方法: GET
 * 路径: /device/getDeviceInfo
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.modbus.device.DeviceRuntimeInfo
 */
    @GET("/device/getDeviceInfo")
    suspend fun getDeviceInfo(): site.addzero.kcloud.plugins.mcuconsole.modbus.device.DeviceRuntimeInfo

/**
 * get24PowerLights
 * HTTP方法: GET
 * 路径: /device/get24PowerLights
 * 返回类型: site.addzero.kcloud.plugins.mcuconsole.modbus.device.Device24PowerLights
 */
    @GET("/device/get24PowerLights")
    suspend fun get24PowerLights(): site.addzero.kcloud.plugins.mcuconsole.modbus.device.Device24PowerLights

}