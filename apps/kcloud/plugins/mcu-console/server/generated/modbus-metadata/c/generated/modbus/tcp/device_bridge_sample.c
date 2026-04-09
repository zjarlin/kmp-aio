/*
 * 请勿手动修改此文件。
 *
 * 参考模板：DeviceApi / Modbus TCP
 * 更新日期：2026-04-09
 *
 * 用途：
 * - 当 device_bridge_impl.c 已经存在时，KSP 不会覆盖那个正式实现文件。
 * - 这份 sample 只用于对照最新 SPI、函数签名、参数说明和注释。
 * - 需要更新时，只把新增函数或注释片段复制到你自己的 device_bridge_impl.c。
 *
 * 编译约定：
 * - 该文件输出到 Docs/generated/modbus/...，不参与固件编译。
 */

#include "device/device_bridge.h"
#include <string.h>

static void device_bridge_copy_text(char *out_text, size_t out_capacity, const char *input) {
    if (out_text == NULL || out_capacity == 0u) {
        return;
    }
    if (input == NULL) {
        out_text[0] = '\0';
        return;
    }
    strncpy(out_text, input, out_capacity - 1u);
    out_text[out_capacity - 1u] = '\0';
}

/*
 * bridge implementation entry.
 *
 * This file is the board-facing implementation entry generated from the contract.
 *
 * 集成方法：
 * - 在下面这些 device_bridge_* 函数体里接入 GPIO / ADC / 状态机 / Flash / 传感器驱动
 * - 保留 #include "device/device_bridge.h"
 * - 不要修改函数签名
 * - 这个文件建议放在 Core/Src/modbus/<transport>/<service>，例如 Core/Src/modbus/rtu/device；也可以放在你通过 KSP 参数指定的业务目录
 *
 * 不要直接修改 generated 的 *_generated.c。
 * Modbus 桥接最终会自动调用这里声明的 SPI 函数。
 *
 * 要改哪里：
 * - 只改下面这些 device_bridge_* 函数的函数体
 * - 把真实硬件读写逻辑填进去
 * - 不要改函数签名，不要改 *_generated.c / *_dispatch.c / adapter
 * - 字符串输出请直接调用 device_bridge_copy_text(...)，不要手写多字符 char 赋值
 */

/*
 * 读取 24 路电源灯状态。
 *
 * 参数：
 * - out_response: device_get24_power_lights_response_t 输出对象。
 */
bool device_bridge_get24_power_lights(device_get24_power_lights_response_t *out_response) {
    /* 要改哪里：从这里开始补板级业务逻辑。 */
    /* 读取 24 路电源灯状态。 */
    if (out_response == NULL) {
        return false;
    }
    out_response->light1 = 0;
    out_response->light2 = 0;
    out_response->light3 = 0;
    out_response->light4 = 0;
    out_response->light5 = 0;
    out_response->light6 = 0;
    out_response->light7 = 0;
    out_response->light8 = 0;
    out_response->light9 = 0;
    out_response->light10 = 0;
    out_response->light11 = 0;
    out_response->light12 = 0;
    out_response->light13 = 0;
    out_response->light14 = 0;
    out_response->light15 = 0;
    out_response->light16 = 0;
    out_response->light17 = 0;
    out_response->light18 = 0;
    out_response->light19 = 0;
    out_response->light20 = 0;
    out_response->light21 = 0;
    out_response->light22 = 0;
    out_response->light23 = 0;
    out_response->light24 = 0;
    return true;
    /* 要改哪里：到这里结束。 */
}

/*
 * 读取板子的固件版本、CPU、晶振、Flash 容量和 MAC 地址。
 *
 * 参数：
 * - out_response: device_get_device_info_response_t 输出对象。
 */
bool device_bridge_get_device_info(device_get_device_info_response_t *out_response) {
    /* 要改哪里：从这里开始补板级业务逻辑。 */
    /* 读取板子的固件版本、CPU、晶振、Flash 容量和 MAC 地址。 */
    if (out_response == NULL) {
        return false;
    }
    /* firmware_version 字符串：codec=STRING_ASCII，寄存器宽度=8，最多 16 个字节，缓冲区容量 17（含 '\0'）。 */
    /* 示例：
     * device_bridge_copy_text(out_response->firmware_version, sizeof(out_response->firmware_version), "XXXXXXXX-XXXXX");
     */
    device_bridge_copy_text(out_response->firmware_version, sizeof(out_response->firmware_version), "");
    /* cpu_model 字符串：codec=STRING_ASCII，寄存器宽度=8，最多 16 个字节，缓冲区容量 17（含 '\0'）。 */
    /* 示例：
     * device_bridge_copy_text(out_response->cpu_model, sizeof(out_response->cpu_model), "XXXXXXXX-XXXXX");
     */
    device_bridge_copy_text(out_response->cpu_model, sizeof(out_response->cpu_model), "");
    out_response->xtal_frequency_hz = 0;
    out_response->flash_size_bytes = 0;
    /* mac_address 字符串：codec=STRING_ASCII，寄存器宽度=9，最多 18 个字节，缓冲区容量 19（含 '\0'）。 */
    /* 示例：
     * device_bridge_copy_text(out_response->mac_address, sizeof(out_response->mac_address), "XXXXXXXX-XXXXX");
     */
    device_bridge_copy_text(out_response->mac_address, sizeof(out_response->mac_address), "");
    return true;
    /* 要改哪里：到这里结束。 */
}

/*
 * 读取 Flash 持久化配置。
 *
 * 参数：
 * - out_response: device_get_flash_config_response_t 输出对象。
 */
bool device_bridge_get_flash_config(device_get_flash_config_response_t *out_response) {
    /* 要改哪里：从这里开始补板级业务逻辑。 */
    /* 读取 Flash 持久化配置。 */
    if (out_response == NULL) {
        return false;
    }
    out_response->magic_word = 0;
    /* port_config 字节数组：codec=BYTE_ARRAY，寄存器宽度=12，固定 24 字节。 */
    for (size_t index = 0; index < sizeof(out_response->port_config); ++index) {
        out_response->port_config[index] = 0u;
    }
    /* uart_params 字节数组：codec=BYTE_ARRAY，寄存器宽度=8，固定 16 字节。 */
    for (size_t index = 0; index < sizeof(out_response->uart_params); ++index) {
        out_response->uart_params[index] = 0u;
    }
    out_response->slave_address = 0;
    /* debounce_params 字节数组：codec=BYTE_ARRAY，寄存器宽度=2，固定 4 字节。 */
    for (size_t index = 0; index < sizeof(out_response->debounce_params); ++index) {
        out_response->debounce_params[index] = 0u;
    }
    out_response->modbus_interval = 0;
    out_response->wdt_enable = 0;
    out_response->firmware_upgrade = 0;
    /* di_hardware_firmware 字节数组：codec=BYTE_ARRAY，寄存器宽度=1，固定 2 字节。 */
    for (size_t index = 0; index < sizeof(out_response->di_hardware_firmware); ++index) {
        out_response->di_hardware_firmware[index] = 0u;
    }
    /* di_status 字节数组：codec=BYTE_ARRAY，寄存器宽度=2，固定 3 字节。 */
    for (size_t index = 0; index < sizeof(out_response->di_status); ++index) {
        out_response->di_status[index] = 0u;
    }
    out_response->fault_status = 0;
    out_response->crc = 0;
    return true;
    /* 要改哪里：到这里结束。 */
}

