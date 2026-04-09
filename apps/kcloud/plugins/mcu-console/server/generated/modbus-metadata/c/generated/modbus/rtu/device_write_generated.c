#include "device_write/device_write_generated.h"
#include "device_write/device_write_bridge.h"

static void device_write_generated_encode_byte_array_registers(
    const uint8_t *input,
    size_t byte_length,
    uint16_t *out_registers,
    size_t register_offset,
    size_t register_width
) {
    if (out_registers == NULL) {
        return;
    }
    for (size_t register_index = 0; register_index < register_width; ++register_index) {
        out_registers[register_offset + register_index] = 0u;
    }
    if (input == NULL) {
        return;
    }
    for (size_t byte_index = 0; byte_index < byte_length; ++byte_index) {
        const size_t target_register = register_offset + (byte_index / 2u);
        const uint16_t value = (uint16_t)input[byte_index];
        if ((byte_index % 2u) == 0u) {
            out_registers[target_register] = (uint16_t)(value << 8);
        } else {
            out_registers[target_register] |= value;
        }
    }
}

static void device_write_generated_decode_byte_array_registers(
    const uint16_t *input_registers,
    size_t register_offset,
    size_t byte_length,
    uint8_t *out_bytes,
    size_t out_capacity
) {
    if (out_bytes == NULL || out_capacity < byte_length) {
        return;
    }
    if (input_registers == NULL) {
        for (size_t index = 0; index < byte_length; ++index) {
            out_bytes[index] = 0u;
        }
        return;
    }
    for (size_t byte_index = 0; byte_index < byte_length; ++byte_index) {
        const uint16_t raw = input_registers[register_offset + (byte_index / 2u)];
        out_bytes[byte_index] =
            ((byte_index % 2u) == 0u) ? (uint8_t)((raw >> 8) & 0xFFu) : (uint8_t)(raw & 0xFFu);
    }
}

/*
 * generated dispatch 实现。
 * 请勿手动修改此文件。
 *
 * 它负责：
 * - 检查 Modbus quantity / buffer 边界
 * - 在 request/response struct 与 Modbus bit/register buffer 之间编解码
 * - 调用 device_write_bridge_* SPI 获取或提交业务数据
 *
 * 固件业务代码不要改这里。
 */

/*
 * 设置故障灯和运行灯。
 *
 * 参数：
 * - input_coils: 输入 Modbus 线圈缓冲区。
 * - coil_count: input_coils 中有效的线圈数量。
 * - out_result: 命令处理结果输出。
 */
bool device_write_generated_write_indicator_lights(const bool *input_coils, size_t coil_count, device_write_command_result_t *out_result) {
    if (out_result == NULL || input_coils == NULL || coil_count < 2) {
        return false;
    }
    device_write_write_indicator_lights_request_t request = {0};
    request.fault_light_on = input_coils[0];
    request.run_light_on = input_coils[1];
    return device_write_bridge_write_indicator_lights(&request, out_result);
}

/*
 * 写入 Flash 持久化配置。
 *
 * 参数：
 * - input_registers: 输入 Modbus 寄存器缓冲区。
 * - register_count: input_registers 中有效的寄存器数量。
 * - out_result: 命令处理结果输出。
 */
bool device_write_generated_write_flash_config(const uint16_t *input_registers, size_t register_count, device_write_command_result_t *out_result) {
    if (out_result == NULL || input_registers == NULL || register_count < 33) {
        return false;
    }
    device_write_write_flash_config_request_t request = {0};
    request.magic_word = (int)(((uint32_t)input_registers[0] << 16) | input_registers[1]);
    device_write_generated_decode_byte_array_registers(input_registers, 2, 24, request.port_config, sizeof(request.port_config));
    device_write_generated_decode_byte_array_registers(input_registers, 14, 16, request.uart_params, sizeof(request.uart_params));
    request.slave_address = (int32_t)(input_registers[22] & 0x00FFu);
    device_write_generated_decode_byte_array_registers(input_registers, 23, 4, request.debounce_params, sizeof(request.debounce_params));
    request.modbus_interval = (int32_t)input_registers[25];
    request.wdt_enable = (int32_t)(input_registers[26] & 0x00FFu);
    request.firmware_upgrade = (int32_t)(input_registers[27] & 0x00FFu);
    device_write_generated_decode_byte_array_registers(input_registers, 28, 2, request.di_hardware_firmware, sizeof(request.di_hardware_firmware));
    device_write_generated_decode_byte_array_registers(input_registers, 29, 3, request.di_status, sizeof(request.di_status));
    request.fault_status = (int32_t)(input_registers[31] & 0x00FFu);
    request.crc = (int32_t)input_registers[32];
    return device_write_bridge_write_flash_config(&request, out_result);
}

