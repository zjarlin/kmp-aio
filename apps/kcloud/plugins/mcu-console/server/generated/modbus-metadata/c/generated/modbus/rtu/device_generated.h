#ifndef DEVICE_GENERATED_H
#define DEVICE_GENERATED_H

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
 * 而是在 device_bridge_impl.c 这类 bridge 实现文件里。
 */

#define DEVICE_SERVICE_ID "device"
#define DEVICE_GET24_POWER_LIGHTS_ADDRESS 0
#define DEVICE_GET24_POWER_LIGHTS_QUANTITY 24
#define DEVICE_GET_DEVICE_INFO_ADDRESS 100
#define DEVICE_GET_DEVICE_INFO_QUANTITY 29
#define DEVICE_GET_FLASH_CONFIG_ADDRESS 200
#define DEVICE_GET_FLASH_CONFIG_QUANTITY 33

typedef struct {
    /* 命令是否被业务层受理。 */
    bool accepted;
    /* 业务层回传的中文说明。 */
    const char *summary;
} device_command_result_t;

/* 读取 24 路电源灯状态。 */
typedef struct {
    /* 电源灯 1 */
    bool light1;
    /* 电源灯 2 */
    bool light2;
    /* 电源灯 3 */
    bool light3;
    /* 电源灯 4 */
    bool light4;
    /* 电源灯 5 */
    bool light5;
    /* 电源灯 6 */
    bool light6;
    /* 电源灯 7 */
    bool light7;
    /* 电源灯 8 */
    bool light8;
    /* 电源灯 9 */
    bool light9;
    /* 电源灯 10 */
    bool light10;
    /* 电源灯 11 */
    bool light11;
    /* 电源灯 12 */
    bool light12;
    /* 电源灯 13 */
    bool light13;
    /* 电源灯 14 */
    bool light14;
    /* 电源灯 15 */
    bool light15;
    /* 电源灯 16 */
    bool light16;
    /* 电源灯 17 */
    bool light17;
    /* 电源灯 18 */
    bool light18;
    /* 电源灯 19 */
    bool light19;
    /* 电源灯 20 */
    bool light20;
    /* 电源灯 21 */
    bool light21;
    /* 电源灯 22 */
    bool light22;
    /* 电源灯 23 */
    bool light23;
    /* 电源灯 24 */
    bool light24;
} device_get24_power_lights_response_t;

/* 读取板子的固件版本、CPU、晶振、Flash 容量和 MAC 地址。 */
typedef struct {
    /* 固件版本 codec=STRING_ASCII registers=8 charCapacity=17。 */
    char firmware_version[17];
    /* CPU 型号 codec=STRING_ASCII registers=8 charCapacity=17。 */
    char cpu_model[17];
    /* 晶振频率 */
    int32_t xtal_frequency_hz;
    /* Flash 容量 */
    int32_t flash_size_bytes;
    /* MAC 地址 codec=STRING_ASCII registers=9 charCapacity=19。 */
    char mac_address[19];
} device_get_device_info_response_t;

/* 读取 Flash 持久化配置。 */
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
} device_get_flash_config_response_t;

/*
 * 读取 24 路电源灯状态。
 *
 * 参数：
 * - out_coils: 输出 Modbus 线圈缓冲区。
 * - coil_count: out_coils 可写入的线圈数量。
 */
bool device_generated_get24_power_lights(bool *out_coils, size_t coil_count);

/*
 * 读取板子的固件版本、CPU、晶振、Flash 容量和 MAC 地址。
 *
 * 参数：
 * - out_registers: 输出 Modbus 寄存器缓冲区。
 * - register_count: out_registers 可写入的寄存器数量。
 */
bool device_generated_get_device_info(uint16_t *out_registers, size_t register_count);

/*
 * 读取 Flash 持久化配置。
 *
 * 参数：
 * - out_registers: 输出 Modbus 寄存器缓冲区。
 * - register_count: out_registers 可写入的寄存器数量。
 */
bool device_generated_get_flash_config(uint16_t *out_registers, size_t register_count);

#endif
