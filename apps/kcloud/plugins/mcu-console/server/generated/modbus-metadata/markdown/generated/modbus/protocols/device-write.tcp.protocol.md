# DeviceWriteApi 协议说明（Modbus TCP）

## 服务概览

| 项目 | 内容 |
| --- | --- |
| 服务标识 | `device-write` |
| 传输方式 | `Modbus TCP` |
| 基础路径 | `/api/modbus` |
| 接口 | `site.addzero.kcloud.plugins.mcuconsole.modbus.device.DeviceWriteApi` |
| 说明 | MCU 默认协议桥接 |

## 联调说明

- 这份文档给上位机、固件和联调同事共用。
- 联调时以上表 `address`、`quantity`、`function code`、标准码值为准。
- 固件侧需要实现的入口是 `device_write_bridge_impl.c` 里的 `device_write_bridge_*` 函数。
- 如果 `device_write_bridge_impl.c` 已经存在，重新生成时不会覆盖；请对照 `device_write_bridge_sample.c` 查看最新模板。
- 固件侧不要修改 `device_write_generated.c`、`modbus_tcp_dispatch.c` 和 adapter。
- `STRING_UTF8` 字段的 `Width` 表示寄存器个数，实际可写入字节数 = `Width * 2`。

| 生成文件 | 用途 |
| --- | --- |
| `device_write_generated.h/.c` | DTO 结构体、地址常量、寄存器/线圈编解码。 |
| `device_write_bridge.h` | 给固件同事看的 SPI 头文件；声明需要实现的 bridge 函数。 |
| `device_write_bridge_impl.c` | 可编辑的板级业务实现模板；只改这里。 |
| `device_write_bridge_sample.c` | 只读桥接模板参考；当已有 impl 不覆盖时，用它对照最新 SPI 和注释；该文件不参与固件编译。 |
| `modbus_tcp_dispatch.h/.c` | 聚合 dispatch，负责按 address 路由到各个 service。 |

## 仿真软件怎么填

| 项目 | 填写方式 |
| --- | --- |
| 连接模式 | 选择 `TCP`。 |
| 主机地址 | 默认 `127.0.0.1`，联调时改成设备 IP。 |
| 端口 | 默认 `502`。 |
| 从站地址 | 默认 `1`；若现场网关改过 `Unit ID`，这里同步改成现场值。 |
| 超时 | 建议先填 `1000 ms`。 |

- 常见主站软件可用 `Modbus Poll`、`QModMaster`、`ModbusClientX`。
- 常见从站仿真软件可用 `Modbus Slave`、`diagslave`。
- 主站发请求时，功能选文档里的“标准功能码 / 标准码值”，起始地址填“地址”，点位数或寄存器数填“数量”。
- 从站仿真时，先在对应数据区预置文档要求的地址范围，再用主站按同样参数发起读写。
- 读操作校验返回数据是否与字段表一致；写操作校验是否返回成功，并按需要补一次读回校验。

## 传输默认值

| 字段 | 默认值 |
| --- | --- |
| Host | `127.0.0.1` |
| Port | `502` |
| Unit ID | `1` |
| Timeout Ms | `1000` |
| Retries | `2` |

## 操作总览

| 操作标识 | 方法 | 标准功能码 | 标准码值 | 标准含义 | 地址 | 数量 | 返回类型 | 说明 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `write-indicator-lights` | `writeIndicatorLights` | `WRITE_MULTIPLE_COILS` | `0x0F` | 写多个线圈 | `24` | `2` | `ModbusCommandResult` | 设置故障灯和运行灯。 |
| `write-flash-config` | `writeFlashConfig` | `WRITE_MULTIPLE_REGISTERS` | `0x10` | 写多个寄存器 | `200` | `33` | `ModbusCommandResult` | 写入 Flash 持久化配置。 |

## `write-indicator-lights`

| 项目 | 内容 |
| --- | --- |
| 方法 | `writeIndicatorLights` |
| 标准功能码 | `WRITE_MULTIPLE_COILS` |
| 标准码值 | `0x0F` |
| 标准含义 | 写多个线圈 |
| 地址 | `24` |
| 数量 | `2` |
| 返回类型 | `ModbusCommandResult` |
| 说明 | 设置故障灯和运行灯。 |

### 仿真软件填写

| 项目 | 填写值 |
| --- | --- |
| 主站功能选择 | `15 Write Multiple Coils` |
| 标准码值 | `0x0F` |
| 数据区 | `Coils` |
| 起始地址 | `24` |
| 数量 | `2` |
| 绝对地址区间 | `24..25` |
| 测试重点 | 主站应收到成功响应；若要确认业务效果，再补一次读操作核对状态。 |

### 参数

| 名称 | 类型 | 编码 | 寄存器偏移 | 位偏移 | 宽度 | 说明 |
| --- | --- | --- | --- | --- | --- | --- |
| `faultLightOn` | `Boolean` | `BOOL_COIL` | `0` | `0` | `1` | 故障灯 |
| `runLightOn` | `Boolean` | `BOOL_COIL` | `1` | `0` | `1` | 运行灯 |

## `write-flash-config`

| 项目 | 内容 |
| --- | --- |
| 方法 | `writeFlashConfig` |
| 标准功能码 | `WRITE_MULTIPLE_REGISTERS` |
| 标准码值 | `0x10` |
| 标准含义 | 写多个寄存器 |
| 地址 | `200` |
| 数量 | `33` |
| 返回类型 | `ModbusCommandResult` |
| 说明 | 写入 Flash 持久化配置。 |

### 仿真软件填写

| 项目 | 填写值 |
| --- | --- |
| 主站功能选择 | `16 Write Multiple Registers` |
| 标准码值 | `0x10` |
| 数据区 | `Holding Registers` |
| 起始地址 | `200` |
| 数量 | `33` |
| 绝对地址区间 | `200..232` |
| 测试重点 | 主站应收到成功响应；若要确认业务效果，再补一次读操作核对状态。 |

### 参数

| 名称 | 类型 | 编码 | 寄存器偏移 | 位偏移 | 宽度 | 说明 |
| --- | --- | --- | --- | --- | --- | --- |
| `magicWord` | `Int` | `U32_BE` | `0` | `0` | `2` | 魔术字：0x5A5A5A5A，校验 Flash 数据是否已初始化。 |
| `portConfig` | `ByteArray` | `BYTE_ARRAY` | `2` | `0` | `12` | 24 路端口配置。 |
| `uartParams` | `ByteArray` | `BYTE_ARRAY` | `14` | `0` | `8` | 串口参数（波特率、校验位等）。 |
| `slaveAddress` | `Int` | `U8` | `22` | `0` | `1` | Modbus 从机地址。 |
| `debounceParams` | `ByteArray` | `BYTE_ARRAY` | `23` | `0` | `2` | 抖动采样参数（阈值，范围 1-255，推荐 5）。 |
| `modbusInterval` | `Int` | `U16` | `25` | `0` | `1` | Modbus 帧时间间隔，单位 ms。 |
| `wdtEnable` | `Int` | `U8` | `26` | `0` | `1` | 看门狗硬件使能，0 表示关闭，1 表示开启。 |
| `firmwareUpgrade` | `Int` | `U8` | `27` | `0` | `1` | 固件升级标志，0 表示不升级，1 表示升级。 |
| `diHardwareFirmware` | `ByteArray` | `BYTE_ARRAY` | `28` | `0` | `1` | DI 模块硬件固件版本号，低 8 位为次版本号，高 8 位为主版本号。 |
| `diStatus` | `ByteArray` | `BYTE_ARRAY` | `29` | `0` | `2` | 24 路 DI 状态，每个 bit 代表 1 路，bit[0] = CH1。 |
| `faultStatus` | `Int` | `U8` | `31` | `0` | `1` | 故障状态标志，位掩码。 |
| `crc` | `Int` | `U16` | `32` | `0` | `1` | CRC16 校验，从 magicWord 到 diStatus 字段。 |
