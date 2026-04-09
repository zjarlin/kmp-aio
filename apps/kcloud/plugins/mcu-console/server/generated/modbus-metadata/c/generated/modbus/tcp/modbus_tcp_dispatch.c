#include "transport/modbus_tcp_dispatch.h"

static void modbus_tcp_dispatch_set_result(modbus_tcp_dispatch_command_result_t *out_result, bool accepted, const char *summary) {
    if (out_result == NULL) {
        return;
    }
    out_result->accepted = accepted;
    out_result->summary = summary;
}

bool modbus_tcp_dispatch_read_coils(uint16_t start_address, uint16_t quantity, bool *out_coils) {
    if (out_coils == NULL) {
        return false;
    }

    switch (start_address) {
        case DEVICE_GET24_POWER_LIGHTS_ADDRESS:
            if (quantity != DEVICE_GET24_POWER_LIGHTS_QUANTITY) {
                return false;
            }
            return device_generated_get24_power_lights(out_coils, quantity);
        default:
            return false;
    }
}

bool modbus_tcp_dispatch_read_discrete_inputs(uint16_t start_address, uint16_t quantity, bool *out_inputs) {
    if (out_inputs == NULL) {
        return false;
    }

    switch (start_address) {
        default:
            return false;
    }
}

bool modbus_tcp_dispatch_read_input_registers(uint16_t start_address, uint16_t quantity, uint16_t *out_registers) {
    if (out_registers == NULL) {
        return false;
    }

    switch (start_address) {
        case DEVICE_GET_DEVICE_INFO_ADDRESS:
            if (quantity != DEVICE_GET_DEVICE_INFO_QUANTITY) {
                return false;
            }
            return device_generated_get_device_info(out_registers, quantity);
        default:
            return false;
    }
}

bool modbus_tcp_dispatch_read_holding_registers(uint16_t start_address, uint16_t quantity, uint16_t *out_registers) {
    if (out_registers == NULL) {
        return false;
    }

    switch (start_address) {
        case DEVICE_GET_FLASH_CONFIG_ADDRESS:
            if (quantity != DEVICE_GET_FLASH_CONFIG_QUANTITY) {
                return false;
            }
            return device_generated_get_flash_config(out_registers, quantity);
        default:
            return false;
    }
}

bool modbus_tcp_dispatch_write_single_coil(uint16_t address, bool value, modbus_tcp_dispatch_command_result_t *out_result) {
    if (out_result == NULL) {
        return false;
    }

    switch (address) {
        default:
            modbus_tcp_dispatch_set_result(out_result, false, "unsupported write single coil address");
            return false;
    }
}

bool modbus_tcp_dispatch_write_multiple_coils(uint16_t start_address, uint16_t quantity, const bool *input_coils, modbus_tcp_dispatch_command_result_t *out_result) {
    if (out_result == NULL || input_coils == NULL) {
        return false;
    }

    switch (start_address) {
        case DEVICE_WRITE_WRITE_INDICATOR_LIGHTS_ADDRESS: {
            if (quantity != DEVICE_WRITE_WRITE_INDICATOR_LIGHTS_QUANTITY) {
                modbus_tcp_dispatch_set_result(out_result, false, "write-indicator-lights coil quantity mismatch");
                return false;
            }
            device_write_command_result_t service_result = {0};
            const bool handled = device_write_generated_write_indicator_lights(input_coils, quantity, &service_result);
            modbus_tcp_dispatch_set_result(out_result, service_result.accepted, service_result.summary);
            return handled;
        }
        default:
            modbus_tcp_dispatch_set_result(out_result, false, "unsupported write multiple coils address");
            return false;
    }
}

bool modbus_tcp_dispatch_write_single_register(uint16_t address, uint16_t value, modbus_tcp_dispatch_command_result_t *out_result) {
    if (out_result == NULL) {
        return false;
    }

    switch (address) {
        default:
            modbus_tcp_dispatch_set_result(out_result, false, "unsupported write single register address");
            return false;
    }
}

bool modbus_tcp_dispatch_write_multiple_registers(uint16_t start_address, uint16_t quantity, const uint16_t *input_registers, modbus_tcp_dispatch_command_result_t *out_result) {
    if (out_result == NULL || input_registers == NULL) {
        return false;
    }

    switch (start_address) {
        case DEVICE_WRITE_WRITE_FLASH_CONFIG_ADDRESS: {
            if (quantity != DEVICE_WRITE_WRITE_FLASH_CONFIG_QUANTITY) {
                modbus_tcp_dispatch_set_result(out_result, false, "write-flash-config register quantity mismatch");
                return false;
            }
            device_write_command_result_t service_result = {0};
            const bool handled = device_write_generated_write_flash_config(input_registers, quantity, &service_result);
            modbus_tcp_dispatch_set_result(out_result, service_result.accepted, service_result.summary);
            return handled;
        }
        default:
            modbus_tcp_dispatch_set_result(out_result, false, "unsupported write registers address");
            return false;
    }
}
