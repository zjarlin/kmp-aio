#ifndef DEVICE_WRITE_GENERATED_H
#define DEVICE_WRITE_GENERATED_H

#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>

/*
 * 接管 mcu-console 当前默认的设备读写契约。
 * 请勿手动修改此文件。
 *
 * 该文件由 Modbus RTU KSP 自动生成。
 *
 * 职责：
 * - 定义该 service 的 Modbus address / quantity 常量
 * - 定义 request/response DTO 对应的 C struct
 * - 声明 generated dispatch 入口
 *
 * 固件同事真正要实现的板级逻辑不在这里，
 * 而是在 device_write_bridge_impl.c 这类 bridge 实现文件里。
 */

#define DEVICE_WRITE_SERVICE_ID "device-write"
#define DEVICE_WRITE_WRITE_INDICATOR_LIGHTS_ADDRESS 24
#define DEVICE_WRITE_WRITE_INDICATOR_LIGHTS_QUANTITY 2
#define DEVICE_WRITE_WRITE_FLASH_CONFIG_ADDRESS 200
#define DEVICE_WRITE_WRITE_FLASH_CONFIG_QUANTITY 33

typedef struct {
    /* 命令是否被业务层受理。 */
    bool accepted;
    /* 业务层回传的中文说明。 */
    const char *summary;
} device_write_command_result_t;

/* 设置故障灯和运行灯。 请求参数。 */
typedef struct {
    /* 故障灯 */
    bool fault_light_on;
    /* 运行灯 */
    bool run_light_on;
} device_write_write_indicator_lights_request_t;

/* 写入 Flash 持久化配置。 请求参数。 */
typedef struct {
    /* 魔术字：0x5A5A5A5A，校验 Flash 数据是否已初始化。 */
    int32_t magic_word;
    /* 24 路端口配置。 codec=BYTE_ARRAY registers=12 byteLength=24。 */
    uint8_t port_config[24];
    /* 串口参数（波特率、校验位等）。 codec=BYTE_ARRAY registers=8 byteLength=16。 */
    uint8_t uart_params[16];
    /* Modbus 从机地址。 */
    int32_t slave_address;
    /* 抖动采样参数（阈值，范围 1-255，推荐 5）。 codec=BYTE_ARRAY registers=2 byteLength=4。 */
    uint8_t debounce_params[4];
    /* Modbus 帧时间间隔，单位 ms。 */
    int32_t modbus_interval;
    /* 看门狗硬件使能，0 表示关闭，1 表示开启。 */
    int32_t wdt_enable;
    /* 固件升级标志，0 表示不升级，1 表示升级。 */
    int32_t firmware_upgrade;
    /* DI 模块硬件固件版本号，低 8 位为次版本号，高 8 位为主版本号。 codec=BYTE_ARRAY registers=1 byteLength=2。 */
    uint8_t di_hardware_firmware[2];
    /* 24 路 DI 状态，每个 bit 代表 1 路，bit[0] = CH1。 codec=BYTE_ARRAY registers=2 byteLength=3。 */
    uint8_t di_status[3];
    /* 故障状态标志，位掩码。 */
    int32_t fault_status;
    /* CRC16 校验，从 magicWord 到 diStatus 字段。 */
    int32_t crc;
} device_write_write_flash_config_request_t;

/*
 * 设置故障灯和运行灯。
 *
 * 参数：
 * - input_coils: 输入 Modbus 线圈缓冲区。
 * - coil_count: input_coils 中有效的线圈数量。
 * - out_result: 命令处理结果输出。
 */
bool device_write_generated_write_indicator_lights(const bool *input_coils, size_t coil_count, device_write_command_result_t *out_result);

/*
 * 写入 Flash 持久化配置。
 *
 * 参数：
 * - input_registers: 输入 Modbus 寄存器缓冲区。
 * - register_count: input_registers 中有效的寄存器数量。
 * - out_result: 命令处理结果输出。
 */
bool device_write_generated_write_flash_config(const uint16_t *input_registers, size_t register_count, device_write_command_result_t *out_result);

#endif
