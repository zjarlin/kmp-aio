#ifndef MODBUS_RTU_DISPATCH_H
#define MODBUS_RTU_DISPATCH_H

#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>

#include "device/device_generated.h"
#include "device_write/device_write_generated.h"

/*
 * Modbus RTU 聚合分发入口。
 * 请勿手动修改此文件。
 *
 * 桥接链路：
 * agile_modbus callback
 *   -> modbus_rtu_dispatch.c
 *   -> *_generated.c
 *   -> *_bridge.h / 板级 bridge 实现
 *
 * 固件侧不需要在这里手写地址判断。
 * 只需要实现每个 service 的 *_bridge_* 业务函数，然后把 RTU adapter 接到 agile_modbus。
 */
typedef struct {
    bool accepted;
    const char *summary;
} modbus_rtu_dispatch_command_result_t;

bool modbus_rtu_dispatch_read_coils(uint16_t start_address, uint16_t quantity, bool *out_coils);
bool modbus_rtu_dispatch_read_discrete_inputs(uint16_t start_address, uint16_t quantity, bool *out_inputs);
bool modbus_rtu_dispatch_read_input_registers(uint16_t start_address, uint16_t quantity, uint16_t *out_registers);
bool modbus_rtu_dispatch_read_holding_registers(uint16_t start_address, uint16_t quantity, uint16_t *out_registers);
bool modbus_rtu_dispatch_write_single_coil(uint16_t address, bool value, modbus_rtu_dispatch_command_result_t *out_result);
bool modbus_rtu_dispatch_write_multiple_coils(uint16_t start_address, uint16_t quantity, const bool *input_coils, modbus_rtu_dispatch_command_result_t *out_result);
bool modbus_rtu_dispatch_write_single_register(uint16_t address, uint16_t value, modbus_rtu_dispatch_command_result_t *out_result);
bool modbus_rtu_dispatch_write_multiple_registers(uint16_t start_address, uint16_t quantity, const uint16_t *input_registers, modbus_rtu_dispatch_command_result_t *out_result);

#endif
