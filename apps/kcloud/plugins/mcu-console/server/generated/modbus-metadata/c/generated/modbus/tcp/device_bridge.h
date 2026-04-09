#ifndef DEVICE_BRIDGE_H
#define DEVICE_BRIDGE_H

#include "device/device_generated.h"

/*
 * DeviceApi bridge SPI。
 * 请勿手动修改此文件。
 *
 * 这是固件业务层唯一需要长期维护的 service 接口面。
 *
 * 集成方法：
 * 1. 在你的板级/业务 .c 文件中 #include "device/device_bridge.h"
 * 2. 实现下面声明的 device_bridge_* 函数
 * 3. 这些 bridge 函数负责读取真实 GPIO、寄存器、传感器状态，或处理写请求
 * 4. *_generated.c 会调用这些 bridge 函数完成 DTO <-> Modbus 数据转换
 *
 * 桥接链路：adapter -> dispatch -> generated -> bridge implementation
 *
 * 不要修改 *_generated.c；
 * 若要接板级逻辑，只改你自己的 device_bridge_impl.c。
 */

/*
 * 读取 24 路电源灯状态。
 *
 * 参数：
 * - out_response: device_get24_power_lights_response_t 输出对象。
 */
bool device_bridge_get24_power_lights(device_get24_power_lights_response_t *out_response);

/*
 * 读取板子的固件版本、CPU、晶振、Flash 容量和 MAC 地址。
 *
 * 参数：
 * - out_response: device_get_device_info_response_t 输出对象。
 */
bool device_bridge_get_device_info(device_get_device_info_response_t *out_response);

/*
 * 读取 Flash 持久化配置。
 *
 * 参数：
 * - out_response: device_get_flash_config_response_t 输出对象。
 */
bool device_bridge_get_flash_config(device_get_flash_config_response_t *out_response);

#endif
