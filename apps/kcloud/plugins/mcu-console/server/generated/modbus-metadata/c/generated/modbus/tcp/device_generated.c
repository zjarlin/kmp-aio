#include "device/device_generated.h"
#include "device/device_bridge.h"

static void device_generated_encode_byte_array_registers(
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

static void device_generated_decode_byte_array_registers(
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

static void device_generated_encode_string_registers(
    const char *input,
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
    const size_t byte_capacity = register_width * 2u;
    for (size_t byte_index = 0; byte_index < byte_capacity; ++byte_index) {
        const unsigned char value = (unsigned char)input[byte_index];
        if (value == 0u) {
            break;
        }
        const size_t target_register = register_offset + (byte_index / 2u);
        if ((byte_index % 2u) == 0u) {
            out_registers[target_register] = (uint16_t)(value << 8);
        } else {
            out_registers[target_register] |= (uint16_t)value;
        }
    }
}

static void device_generated_decode_string_registers(
    const uint16_t *input_registers,
    size_t register_offset,
    size_t register_width,
    char *out_text,
    size_t out_capacity
) {
    if (out_text == NULL || out_capacity == 0u) {
        return;
    }
    out_text[0] = '\0';
    if (input_registers == NULL) {
        return;
    }
    const size_t byte_capacity = register_width * 2u;
    const size_t text_capacity = out_capacity - 1u;
    size_t written = 0u;
    for (size_t byte_index = 0; byte_index < byte_capacity && written < text_capacity; ++byte_index) {
        const uint16_t raw = input_registers[register_offset + (byte_index / 2u)];
        const unsigned char value =
            ((byte_index % 2u) == 0u) ? (unsigned char)((raw >> 8) & 0xFFu) : (unsigned char)(raw & 0xFFu);
        if (value == 0u) {
            break;
        }
        out_text[written++] = (char)value;
    }
    out_text[written] = '\0';
}

/*
 * generated dispatch 实现。
 * 请勿手动修改此文件。
 *
 * 它负责：
 * - 检查 Modbus quantity / buffer 边界
 * - 在 request/response struct 与 Modbus bit/register buffer 之间编解码
 * - 调用 device_bridge_* SPI 获取或提交业务数据
 *
 * 固件业务代码不要改这里。
 */

/*
 * 读取 24 路电源灯状态。
 *
 * 参数：
 * - out_coils: 输出 Modbus 线圈缓冲区。
 * - coil_count: out_coils 可写入的线圈数量。
 */
bool device_generated_get24_power_lights(bool *out_coils, size_t coil_count) {
    if (out_coils == NULL || coil_count < 24) {
        return false;
    }
    device_get24_power_lights_response_t response = {0};
    if (!device_bridge_get24_power_lights(&response)) {
        return false;
    }
    out_coils[0] = response.light1;
    out_coils[1] = response.light2;
    out_coils[2] = response.light3;
    out_coils[3] = response.light4;
    out_coils[4] = response.light5;
    out_coils[5] = response.light6;
    out_coils[6] = response.light7;
    out_coils[7] = response.light8;
    out_coils[8] = response.light9;
    out_coils[9] = response.light10;
    out_coils[10] = response.light11;
    out_coils[11] = response.light12;
    out_coils[12] = response.light13;
    out_coils[13] = response.light14;
    out_coils[14] = response.light15;
    out_coils[15] = response.light16;
    out_coils[16] = response.light17;
    out_coils[17] = response.light18;
    out_coils[18] = response.light19;
    out_coils[19] = response.light20;
    out_coils[20] = response.light21;
    out_coils[21] = response.light22;
    out_coils[22] = response.light23;
    out_coils[23] = response.light24;
    return true;
}

/*
 * 读取板子的固件版本、CPU、晶振、Flash 容量和 MAC 地址。
 *
 * 参数：
 * - out_registers: 输出 Modbus 寄存器缓冲区。
 * - register_count: out_registers 可写入的寄存器数量。
 */
bool device_generated_get_device_info(uint16_t *out_registers, size_t register_count) {
    if (out_registers == NULL || register_count < 29) {
        return false;
    }
    device_get_device_info_response_t response = {0};
    if (!device_bridge_get_device_info(&response)) {
        return false;
    }
    out_registers[0] = 0u;
    device_generated_encode_string_registers(response.firmware_version, out_registers, 0, 8);
    out_registers[8] = 0u;
    device_generated_encode_string_registers(response.cpu_model, out_registers, 8, 8);
    out_registers[16] = 0u;
    out_registers[16] = (uint16_t)((response.xtal_frequency_hz >> 16) & 0xFFFFu);
    out_registers[17] = (uint16_t)(response.xtal_frequency_hz & 0xFFFFu);
    out_registers[18] = 0u;
    out_registers[18] = (uint16_t)((response.flash_size_bytes >> 16) & 0xFFFFu);
    out_registers[19] = (uint16_t)(response.flash_size_bytes & 0xFFFFu);
    out_registers[20] = 0u;
    device_generated_encode_string_registers(response.mac_address, out_registers, 20, 9);
    return true;
}

/*
 * 读取 Flash 持久化配置。
 *
 * 参数：
 * - out_registers: 输出 Modbus 寄存器缓冲区。
 * - register_count: out_registers 可写入的寄存器数量。
 */
bool device_generated_get_flash_config(uint16_t *out_registers, size_t register_count) {
    if (out_registers == NULL || register_count < 33) {
        return false;
    }
    device_get_flash_config_response_t response = {0};
    if (!device_bridge_get_flash_config(&response)) {
        return false;
    }
    out_registers[0] = 0u;
    out_registers[0] = (uint16_t)((response.magic_word >> 16) & 0xFFFFu);
    out_registers[1] = (uint16_t)(response.magic_word & 0xFFFFu);
    out_registers[2] = 0u;
    device_generated_encode_byte_array_registers(response.port_config, 24, out_registers, 2, 12);
    out_registers[14] = 0u;
    device_generated_encode_byte_array_registers(response.uart_params, 16, out_registers, 14, 8);
    out_registers[22] = 0u;
    out_registers[22] = (uint16_t)(response.slave_address);
    out_registers[23] = 0u;
    device_generated_encode_byte_array_registers(response.debounce_params, 4, out_registers, 23, 2);
    out_registers[25] = 0u;
    out_registers[25] = (uint16_t)(response.modbus_interval);
    out_registers[26] = 0u;
    out_registers[26] = (uint16_t)(response.wdt_enable);
    out_registers[27] = 0u;
    out_registers[27] = (uint16_t)(response.firmware_upgrade);
    out_registers[28] = 0u;
    device_generated_encode_byte_array_registers(response.di_hardware_firmware, 2, out_registers, 28, 1);
    out_registers[29] = 0u;
    device_generated_encode_byte_array_registers(response.di_status, 3, out_registers, 29, 2);
    out_registers[31] = 0u;
    out_registers[31] = (uint16_t)(response.fault_status);
    out_registers[32] = 0u;
    out_registers[32] = (uint16_t)(response.crc);
    return true;
}

