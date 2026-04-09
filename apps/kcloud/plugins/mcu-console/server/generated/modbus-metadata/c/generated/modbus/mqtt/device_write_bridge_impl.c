#include "device_write/device_write_bridge.h"

/*
 * bridge implementation entry.
 *
 * This file is the board-facing implementation entry generated from the contract.
 *
 * 集成方法：
 * - 在下面这些 device_write_bridge_* 函数体里接入 GPIO / ADC / 状态机 / Flash / 传感器驱动
 * - 保留 #include "device_write/device_write_bridge.h"
 * - 不要修改函数签名
 * - 这个文件建议放在 Core/Src/modbus/<transport>/<service>，例如 Core/Src/modbus/rtu/device_write；也可以放在你通过 KSP 参数指定的业务目录
 *
 * 不要直接修改 generated 的 *_generated.c。
 * Modbus 桥接最终会自动调用这里声明的 SPI 函数。
 *
 * 要改哪里：
 * - 只改下面这些 device_write_bridge_* 函数的函数体
 * - 把真实硬件读写逻辑填进去
 * - 不要改函数签名，不要改 *_generated.c / *_dispatch.c / adapter
 */

/*
 * 设置故障灯和运行灯。
 *
 * 参数：
 * - request: device_write_write_indicator_lights_request_t 输入参数对象。
 * - out_result: 命令处理结果输出。
 */
bool device_write_bridge_write_indicator_lights(const device_write_write_indicator_lights_request_t *request, device_write_command_result_t *out_result) {
    /* 要改哪里：从这里开始补板级业务逻辑。 */
    /* 设置故障灯和运行灯。 */
    /* 输入参数：
     * - request->fault_light_on: 故障灯
     * - request->run_light_on: 运行灯
     */
    if (out_result == NULL) {
        return false;
    }
    out_result->accepted = true;
    out_result->summary = "TODO: bridge implementation";
    return true;
    /* 要改哪里：到这里结束。 */
}

/*
 * 写入 Flash 持久化配置。
 *
 * 参数：
 * - request: device_write_write_flash_config_request_t 输入参数对象。
 * - out_result: 命令处理结果输出。
 */
bool device_write_bridge_write_flash_config(const device_write_write_flash_config_request_t *request, device_write_command_result_t *out_result) {
    /* 要改哪里：从这里开始补板级业务逻辑。 */
    /* 写入 Flash 持久化配置。 */
    /* 输入参数：
     * - request->magic_word: 魔术字：0x5A5A5A5A，校验 Flash 数据是否已初始化。
     * - request->port_config: 24 路端口配置。
     * - request->uart_params: 串口参数（波特率、校验位等）。
     * - request->slave_address: Modbus 从机地址。
     * - request->debounce_params: 抖动采样参数（阈值，范围 1-255，推荐 5）。
     * - request->modbus_interval: Modbus 帧时间间隔，单位 ms。
     * - request->wdt_enable: 看门狗硬件使能，0 表示关闭，1 表示开启。
     * - request->firmware_upgrade: 固件升级标志，0 表示不升级，1 表示升级。
     * - request->di_hardware_firmware: DI 模块硬件固件版本号，低 8 位为次版本号，高 8 位为主版本号。
     * - request->di_status: 24 路 DI 状态，每个 bit 代表 1 路，bit[0] = CH1。
     * - request->fault_status: 故障状态标志，位掩码。
     * - request->crc: CRC16 校验，从 magicWord 到 diStatus 字段。
     */
    if (out_result == NULL) {
        return false;
    }
    out_result->accepted = true;
    out_result->summary = "TODO: bridge implementation";
    return true;
    /* 要改哪里：到这里结束。 */
}

