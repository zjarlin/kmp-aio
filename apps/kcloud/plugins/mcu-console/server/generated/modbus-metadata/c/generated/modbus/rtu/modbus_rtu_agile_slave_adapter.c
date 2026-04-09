#include "transport/modbus_rtu_agile_slave_adapter.h"

#include "transport/modbus_rtu_dispatch.h"

/*
 * 这个文件只做 transport/runtime 适配：
 * 请勿手动修改此文件。
 * 1. 从 agile_modbus 解析请求
 * 2. 调用 modbus_rtu_dispatch_*
 * 3. 把 dispatch 返回值重新打包回 agile_modbus send_buf
 *
 * 它不承载任何板级业务逻辑。
 */

static int generated_pack_read_coils(agile_modbus_t *ctx, struct agile_modbus_slave_info *slave_info, bool (*dispatch_fn)(uint16_t, uint16_t, bool *)) {
    if (ctx == NULL || slave_info == NULL || dispatch_fn == NULL) {
        return -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_VALUE;
    }
    bool coil_values[256] = {0};
    if (slave_info->nb > 256) {
        return -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_VALUE;
    }
    if (!dispatch_fn((uint16_t)slave_info->address, (uint16_t)slave_info->nb, coil_values)) {
        return -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_ADDRESS;
    }
    for (int i = 0; i < slave_info->nb; ++i) {
        agile_modbus_slave_io_set(ctx->send_buf + slave_info->send_index, i, coil_values[i] ? 1 : 0);
    }
    return 0;
}

static int generated_pack_read_registers(agile_modbus_t *ctx, struct agile_modbus_slave_info *slave_info, bool (*dispatch_fn)(uint16_t, uint16_t, uint16_t *)) {
    if (ctx == NULL || slave_info == NULL || dispatch_fn == NULL) {
        return -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_VALUE;
    }
    uint16_t register_values[128] = {0};
    if (slave_info->nb > 128) {
        return -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_VALUE;
    }
    if (!dispatch_fn((uint16_t)slave_info->address, (uint16_t)slave_info->nb, register_values)) {
        return -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_ADDRESS;
    }
    for (int i = 0; i < slave_info->nb; ++i) {
        agile_modbus_slave_register_set(ctx->send_buf + slave_info->send_index, i, register_values[i]);
    }
    return 0;
}

int generated_modbus_rtu_agile_slave_callback(
    agile_modbus_t *ctx,
    struct agile_modbus_slave_info *slave_info,
    const void *data
) {
    (void)data;
    if (ctx == NULL || slave_info == NULL || slave_info->sft == NULL) {
        return -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_VALUE;
    }

    switch (slave_info->sft->function) {
        case AGILE_MODBUS_FC_READ_COILS:
            return generated_pack_read_coils(ctx, slave_info, modbus_rtu_dispatch_read_coils);
        case AGILE_MODBUS_FC_READ_DISCRETE_INPUTS:
            return generated_pack_read_coils(ctx, slave_info, modbus_rtu_dispatch_read_discrete_inputs);
        case AGILE_MODBUS_FC_READ_INPUT_REGISTERS:
            return generated_pack_read_registers(ctx, slave_info, modbus_rtu_dispatch_read_input_registers);
        case AGILE_MODBUS_FC_READ_HOLDING_REGISTERS:
            return generated_pack_read_registers(ctx, slave_info, modbus_rtu_dispatch_read_holding_registers);
        case AGILE_MODBUS_FC_WRITE_SINGLE_COIL: {
            modbus_rtu_dispatch_command_result_t result = {0};
            const int accepted = modbus_rtu_dispatch_write_single_coil((uint16_t)slave_info->address, *((int *)slave_info->buf) != 0, &result);
            return accepted ? 0 : -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_ADDRESS;
        }
        case AGILE_MODBUS_FC_WRITE_MULTIPLE_COILS: {
            bool coil_values[256] = {0};
            if (slave_info->nb > 256) {
                return -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_VALUE;
            }
            for (int i = 0; i < slave_info->nb; ++i) {
                coil_values[i] = agile_modbus_slave_io_get(slave_info->buf, i) != 0;
            }
            modbus_rtu_dispatch_command_result_t result = {0};
            const int accepted = modbus_rtu_dispatch_write_multiple_coils((uint16_t)slave_info->address, (uint16_t)slave_info->nb, coil_values, &result);
            return accepted ? 0 : -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_ADDRESS;
        }
        case AGILE_MODBUS_FC_WRITE_SINGLE_REGISTER: {
            modbus_rtu_dispatch_command_result_t result = {0};
            const int accepted = modbus_rtu_dispatch_write_single_register((uint16_t)slave_info->address, (uint16_t)(*((int *)slave_info->buf)), &result);
            return accepted ? 0 : -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_ADDRESS;
        }
        case AGILE_MODBUS_FC_WRITE_MULTIPLE_REGISTERS: {
            uint16_t register_values[128] = {0};
            if (slave_info->nb > 128) {
                return -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_VALUE;
            }
            for (int i = 0; i < slave_info->nb; ++i) {
                register_values[i] = agile_modbus_slave_register_get(slave_info->buf, i);
            }
            modbus_rtu_dispatch_command_result_t result = {0};
            const int accepted = modbus_rtu_dispatch_write_multiple_registers((uint16_t)slave_info->address, (uint16_t)slave_info->nb, register_values, &result);
            return accepted ? 0 : -AGILE_MODBUS_EXCEPTION_ILLEGAL_DATA_ADDRESS;
        }
        default:
            return -AGILE_MODBUS_EXCEPTION_ILLEGAL_FUNCTION;
    }
}
