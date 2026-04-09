#ifndef DEVICE_WRITE_BRIDGE_H
#define DEVICE_WRITE_BRIDGE_H

#include "device_write/device_write_generated.h"

/*
 * DeviceWriteApi bridge SPI。
 * 请勿手动修改此文件。
 *
 * 这是固件业务层唯一需要长期维护的 service 接口面。
 *
 * 集成方法：
 * 1. 在你的板级/业务 .c 文件中 #include "device_write/device_write_bridge.h"
 * 2. 实现下面声明的 device_write_bridge_* 函数
 * 3. 这些 bridge 函数负责读取真实 GPIO、寄存器、传感器状态，或处理写请求
 * 4. *_generated.c 会调用这些 bridge 函数完成 DTO <-> Modbus 数据转换
 *
 * 桥接链路：adapter -> dispatch -> generated -> bridge implementation
 *
 * 不要修改 *_generated.c；
 * 若要接板级逻辑，只改你自己的 device_write_bridge_impl.c。
 */

/*
 * 设置故障灯和运行灯。
 *
 * 参数：
 * - request: device_write_write_indicator_lights_request_t 输入参数对象。
 * - out_result: 命令处理结果输出。
 */
bool device_write_bridge_write_indicator_lights(const device_write_write_indicator_lights_request_t *request, device_write_command_result_t *out_result);

/*
 * 写入 Flash 持久化配置。
 *
 * 参数：
 * - request: device_write_write_flash_config_request_t 输入参数对象。
 * - out_result: 命令处理结果输出。
 */
bool device_write_bridge_write_flash_config(const device_write_write_flash_config_request_t *request, device_write_command_result_t *out_result);

#endif
