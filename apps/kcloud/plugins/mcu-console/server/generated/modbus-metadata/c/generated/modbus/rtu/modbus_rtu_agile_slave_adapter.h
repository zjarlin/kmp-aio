#ifndef MODBUS_RTU_AGILE_SLAVE_ADAPTER_H
#define MODBUS_RTU_AGILE_SLAVE_ADAPTER_H

#include "agile_modbus.h"

/*
 * RTU + agile_modbus 适配入口。
 * 请勿手动修改此文件。
 *
 * 在 freertos.c 或你的串口任务里，把 agile_modbus_slave_handle(...) 的 callback
 * 替换成 generated_modbus_rtu_agile_slave_callback。
 *
 * 业务桥接函数不需要在这个文件里实现；
 * 它们会通过 modbus_rtu_dispatch.c -> *_generated.c -> *_bridge.h 自动被调用。
 */

int generated_modbus_rtu_agile_slave_callback(
    agile_modbus_t *ctx,
    struct agile_modbus_slave_info *slave_info,
    const void *data
);

#endif
