# DeviceApi 协议说明（Modbus TCP）

## 服务概览

| 项目 | 内容 |
| --- | --- |
| 服务标识 | `device` |
| 传输方式 | `Modbus TCP` |
| 基础路径 | `/api/modbus` |
| 接口 | `site.addzero.kcloud.plugins.mcuconsole.modbus.device.DeviceApi` |
| 说明 | MCU 默认协议桥接 |

## 联调说明

- 这份文档给上位机、固件和联调同事共用。
- 联调时以上表 `address`、`quantity`、`function code`、标准码值为准。
- 固件侧需要实现的入口是 `device_bridge_impl.c` 里的 `device_bridge_*` 函数。
- 如果 `device_bridge_impl.c` 已经存在，重新生成时不会覆盖；请对照 `device_bridge_sample.c` 查看最新模板。
- 固件侧不要修改 `device_generated.c`、`modbus_tcp_dispatch.c` 和 adapter。
- `STRING_UTF8` 字段的 `Width` 表示寄存器个数，实际可写入字节数 = `Width * 2`。

| 生成文件 | 用途 |
| --- | --- |
| `device_generated.h/.c` | DTO 结构体、地址常量、寄存器/线圈编解码。 |
| `device_bridge.h` | 给固件同事看的 SPI 头文件；声明需要实现的 bridge 函数。 |
| `device_bridge_impl.c` | 可编辑的板级业务实现模板；只改这里。 |
| `device_bridge_sample.c` | 只读桥接模板参考；当已有 impl 不覆盖时，用它对照最新 SPI 和注释；该文件不参与固件编译。 |
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
| `get24-power-lights` | `get24PowerLights` | `READ_COILS` | `0x01` | 读取线圈 | `0` | `24` | `Device24PowerLightsRegisters` | 读取 24 路电源灯状态。 |
| `get-device-info` | `getDeviceInfo` | `READ_INPUT_REGISTERS` | `0x04` | 读取输入寄存器 | `100` | `29` | `DeviceRuntimeInfoRegisters` | 读取板子的固件版本、CPU、晶振、Flash 容量和 MAC 地址。 |
| `get-flash-config` | `getFlashConfig` | `READ_HOLDING_REGISTERS` | `0x03` | 读取保持寄存器 | `200` | `33` | `FlashConfigRegisters` | 读取 Flash 持久化配置。 |

## `get24-power-lights`

| 项目 | 内容 |
| --- | --- |
| 方法 | `get24PowerLights` |
| 标准功能码 | `READ_COILS` |
| 标准码值 | `0x01` |
| 标准含义 | 读取线圈 |
| 地址 | `0` |
| 数量 | `24` |
| 返回类型 | `Device24PowerLightsRegisters` |
| 说明 | 读取 24 路电源灯状态。 |

### 仿真软件填写

| 项目 | 填写值 |
| --- | --- |
| 主站功能选择 | `01 Read Coils` |
| 标准码值 | `0x01` |
| 数据区 | `Coils` |
| 起始地址 | `0` |
| 数量 | `24` |
| 绝对地址区间 | `0..23` |
| 测试重点 | 返回 `quantity=24` 个线圈位；按下面字段表逐个核对布尔状态。 |

### 返回字段

| 名称 | 类型 | 编码 | 寄存器偏移 | 位偏移 | 宽度 | 说明 |
| --- | --- | --- | --- | --- | --- | --- |
| `light1` | `Boolean` | `BOOL_COIL` | `0` | `0` | `1` | 电源灯 1 |
| `light2` | `Boolean` | `BOOL_COIL` | `1` | `0` | `1` | 电源灯 2 |
| `light3` | `Boolean` | `BOOL_COIL` | `2` | `0` | `1` | 电源灯 3 |
| `light4` | `Boolean` | `BOOL_COIL` | `3` | `0` | `1` | 电源灯 4 |
| `light5` | `Boolean` | `BOOL_COIL` | `4` | `0` | `1` | 电源灯 5 |
| `light6` | `Boolean` | `BOOL_COIL` | `5` | `0` | `1` | 电源灯 6 |
| `light7` | `Boolean` | `BOOL_COIL` | `6` | `0` | `1` | 电源灯 7 |
| `light8` | `Boolean` | `BOOL_COIL` | `7` | `0` | `1` | 电源灯 8 |
| `light9` | `Boolean` | `BOOL_COIL` | `8` | `0` | `1` | 电源灯 9 |
| `light10` | `Boolean` | `BOOL_COIL` | `9` | `0` | `1` | 电源灯 10 |
| `light11` | `Boolean` | `BOOL_COIL` | `10` | `0` | `1` | 电源灯 11 |
| `light12` | `Boolean` | `BOOL_COIL` | `11` | `0` | `1` | 电源灯 12 |
| `light13` | `Boolean` | `BOOL_COIL` | `12` | `0` | `1` | 电源灯 13 |
| `light14` | `Boolean` | `BOOL_COIL` | `13` | `0` | `1` | 电源灯 14 |
| `light15` | `Boolean` | `BOOL_COIL` | `14` | `0` | `1` | 电源灯 15 |
| `light16` | `Boolean` | `BOOL_COIL` | `15` | `0` | `1` | 电源灯 16 |
| `light17` | `Boolean` | `BOOL_COIL` | `16` | `0` | `1` | 电源灯 17 |
| `light18` | `Boolean` | `BOOL_COIL` | `17` | `0` | `1` | 电源灯 18 |
| `light19` | `Boolean` | `BOOL_COIL` | `18` | `0` | `1` | 电源灯 19 |
| `light20` | `Boolean` | `BOOL_COIL` | `19` | `0` | `1` | 电源灯 20 |
| `light21` | `Boolean` | `BOOL_COIL` | `20` | `0` | `1` | 电源灯 21 |
| `light22` | `Boolean` | `BOOL_COIL` | `21` | `0` | `1` | 电源灯 22 |
| `light23` | `Boolean` | `BOOL_COIL` | `22` | `0` | `1` | 电源灯 23 |
| `light24` | `Boolean` | `BOOL_COIL` | `23` | `0` | `1` | 电源灯 24 |

## `get-device-info`

| 项目 | 内容 |
| --- | --- |
| 方法 | `getDeviceInfo` |
| 标准功能码 | `READ_INPUT_REGISTERS` |
| 标准码值 | `0x04` |
| 标准含义 | 读取输入寄存器 |
| 地址 | `100` |
| 数量 | `29` |
| 返回类型 | `DeviceRuntimeInfoRegisters` |
| 说明 | 读取板子的固件版本、CPU、晶振、Flash 容量和 MAC 地址。 |

### 仿真软件填写

| 项目 | 填写值 |
| --- | --- |
| 主站功能选择 | `04 Read Input Registers` |
| 标准码值 | `0x04` |
| 数据区 | `Input Registers` |
| 起始地址 | `100` |
| 数量 | `29` |
| 绝对地址区间 | `100..128` |
| 测试重点 | 返回 `quantity=29` 个寄存器；按下面字段表核对数值和字符串内容。 |

### 返回字段

| 名称 | 类型 | 编码 | 寄存器偏移 | 位偏移 | 宽度 | 说明 |
| --- | --- | --- | --- | --- | --- | --- |
| `firmwareVersion` | `String` | `STRING_ASCII` | `0` | `0` | `8` | 固件版本 |
| `cpuModel` | `String` | `STRING_ASCII` | `8` | `0` | `8` | CPU 型号 |
| `xtalFrequencyHz` | `Int` | `U32_BE` | `16` | `0` | `2` | 晶振频率 |
| `flashSizeBytes` | `Int` | `U32_BE` | `18` | `0` | `2` | Flash 容量 |
| `macAddress` | `String` | `STRING_ASCII` | `20` | `0` | `9` | MAC 地址 |

### 返回里的字符串字段填写

- `firmwareVersion` 使用 `STRING_ASCII`，绝对寄存器区间 `100..107`，每个寄存器承载 2 个字节；字符串不足部分补 `0x00`。
- `cpuModel` 使用 `STRING_ASCII`，绝对寄存器区间 `108..115`，每个寄存器承载 2 个字节；字符串不足部分补 `0x00`。
- `macAddress` 使用 `STRING_ASCII`，绝对寄存器区间 `120..128`，每个寄存器承载 2 个字节；字符串不足部分补 `0x00`。

## `get-flash-config`

| 项目 | 内容 |
| --- | --- |
| 方法 | `getFlashConfig` |
| 标准功能码 | `READ_HOLDING_REGISTERS` |
| 标准码值 | `0x03` |
| 标准含义 | 读取保持寄存器 |
| 地址 | `200` |
| 数量 | `33` |
| 返回类型 | `FlashConfigRegisters` |
| 说明 | 读取 Flash 持久化配置。 |

### 仿真软件填写

| 项目 | 填写值 |
| --- | --- |
| 主站功能选择 | `03 Read Holding Registers` |
| 标准码值 | `0x03` |
| 数据区 | `Holding Registers` |
| 起始地址 | `200` |
| 数量 | `33` |
| 绝对地址区间 | `200..232` |
| 测试重点 | 返回 `quantity=33` 个寄存器；按下面字段表核对数值和字符串内容。 |

### 返回字段

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
