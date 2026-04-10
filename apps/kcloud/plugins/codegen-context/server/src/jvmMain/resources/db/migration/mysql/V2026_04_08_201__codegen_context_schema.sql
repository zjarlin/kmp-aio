CREATE TABLE IF NOT EXISTS codegen_context_context (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    consumer_target VARCHAR(64) NOT NULL,
    protocol_template_id BIGINT NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_codegen_context_context_code (code),
    CONSTRAINT fk_codegen_context_context_protocol_template
        FOREIGN KEY (protocol_template_id) REFERENCES host_config_protocol_template(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS codegen_context_schema (
    id BIGINT NOT NULL AUTO_INCREMENT,
    context_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sort_index INT NOT NULL DEFAULT 0,
    direction VARCHAR(32) NOT NULL,
    function_code VARCHAR(64) NOT NULL,
    base_address INT NOT NULL,
    method_name VARCHAR(255) NOT NULL,
    model_name VARCHAR(255),
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_codegen_context_schema_context_method (context_id, method_name),
    CONSTRAINT fk_codegen_context_schema_context
        FOREIGN KEY (context_id) REFERENCES codegen_context_context(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS codegen_context_field (
    id BIGINT NOT NULL AUTO_INCREMENT,
    schema_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sort_index INT NOT NULL DEFAULT 0,
    property_name VARCHAR(255) NOT NULL,
    transport_type VARCHAR(64) NOT NULL,
    register_offset INT NOT NULL,
    bit_offset INT NOT NULL DEFAULT 0,
    length INT NOT NULL DEFAULT 1,
    translation_hint TEXT,
    default_literal TEXT,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_codegen_context_field_schema_property (schema_id, property_name),
    CONSTRAINT fk_codegen_context_field_schema
        FOREIGN KEY (schema_id) REFERENCES codegen_context_schema(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS codegen_context_modbus_contract (
    context_id BIGINT NOT NULL,
    context_code VARCHAR(255) NOT NULL,
    context_name VARCHAR(255) NOT NULL,
    enabled TINYINT(1) NOT NULL,
    consumer_target VARCHAR(64) NOT NULL,
    protocol_template_code VARCHAR(255) NOT NULL,
    transport VARCHAR(32) NOT NULL,
    selected TINYINT(1) NOT NULL DEFAULT 0,
    payload LONGTEXT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (context_id, transport),
    CONSTRAINT fk_codegen_context_modbus_contract_context
        FOREIGN KEY (context_id) REFERENCES codegen_context_context(id)
        ON DELETE CASCADE,
    KEY idx_codegen_context_modbus_contract_selected (consumer_target, transport, selected, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO codegen_context_context (
    code,
    name,
    description,
    enabled,
    consumer_target,
    protocol_template_id,
    created_at,
    updated_at
)
SELECT
    'MCU_DEVICE_DEFAULT',
    'MCU 默认协议桥接',
    '接管 mcu-console 当前默认的设备读写契约。',
    1,
    'MCU_CONSOLE',
    template.id,
    1775606400000,
    1775606400000
FROM host_config_protocol_template template
WHERE template.code = 'MODBUS_RTU_CLIENT'
  AND NOT EXISTS (
      SELECT 1 FROM codegen_context_context existing WHERE existing.code = 'MCU_DEVICE_DEFAULT'
  );

INSERT INTO codegen_context_schema (
    context_id,
    name,
    description,
    sort_index,
    direction,
    function_code,
    base_address,
    method_name,
    model_name,
    created_at,
    updated_at
)
SELECT
    context.id,
    '读取 24 路电源灯',
    '读取 24 路电源灯状态。',
    0,
    'READ',
    'READ_COILS',
    0,
    'get24PowerLights',
    'Device24PowerLights',
    1775606400000,
    1775606400000
FROM codegen_context_context context
WHERE context.code = 'MCU_DEVICE_DEFAULT'
  AND NOT EXISTS (
      SELECT 1 FROM codegen_context_schema existing
      WHERE existing.context_id = context.id AND existing.method_name = 'get24PowerLights'
  );

INSERT INTO codegen_context_schema (
    context_id,
    name,
    description,
    sort_index,
    direction,
    function_code,
    base_address,
    method_name,
    model_name,
    created_at,
    updated_at
)
SELECT
    context.id,
    '读取设备信息',
    '读取板子的固件版本、CPU、晶振、Flash 容量和 MAC 地址。',
    10,
    'READ',
    'READ_INPUT_REGISTERS',
    100,
    'getDeviceInfo',
    'DeviceRuntimeInfo',
    1775606400000,
    1775606400000
FROM codegen_context_context context
WHERE context.code = 'MCU_DEVICE_DEFAULT'
  AND NOT EXISTS (
      SELECT 1 FROM codegen_context_schema existing
      WHERE existing.context_id = context.id AND existing.method_name = 'getDeviceInfo'
  );

INSERT INTO codegen_context_schema (
    context_id,
    name,
    description,
    sort_index,
    direction,
    function_code,
    base_address,
    method_name,
    model_name,
    created_at,
    updated_at
)
SELECT
    context.id,
    '设置指示灯',
    '设置故障灯和运行灯。',
    20,
    'WRITE',
    'WRITE_MULTIPLE_COILS',
    24,
    'writeIndicatorLights',
    NULL,
    1775606400000,
    1775606400000
FROM codegen_context_context context
WHERE context.code = 'MCU_DEVICE_DEFAULT'
  AND NOT EXISTS (
      SELECT 1 FROM codegen_context_schema existing
      WHERE existing.context_id = context.id AND existing.method_name = 'writeIndicatorLights'
  );

INSERT INTO codegen_context_schema (
    context_id,
    name,
    description,
    sort_index,
    direction,
    function_code,
    base_address,
    method_name,
    model_name,
    created_at,
    updated_at
)
SELECT
    context.id,
    '读取 Flash 配置',
    '读取 Flash 持久化配置。',
    30,
    'READ',
    'READ_HOLDING_REGISTERS',
    200,
    'getFlashConfig',
    'FlashConfig',
    1775606400000,
    1775606400000
FROM codegen_context_context context
WHERE context.code = 'MCU_DEVICE_DEFAULT'
  AND NOT EXISTS (
      SELECT 1 FROM codegen_context_schema existing
      WHERE existing.context_id = context.id AND existing.method_name = 'getFlashConfig'
  );

INSERT INTO codegen_context_schema (
    context_id,
    name,
    description,
    sort_index,
    direction,
    function_code,
    base_address,
    method_name,
    model_name,
    created_at,
    updated_at
)
SELECT
    context.id,
    '写入 Flash 配置',
    '写入 Flash 持久化配置。',
    40,
    'WRITE',
    'WRITE_MULTIPLE_REGISTERS',
    200,
    'writeFlashConfig',
    NULL,
    1775606400000,
    1775606400000
FROM codegen_context_context context
WHERE context.code = 'MCU_DEVICE_DEFAULT'
  AND NOT EXISTS (
      SELECT 1 FROM codegen_context_schema existing
      WHERE existing.context_id = context.id AND existing.method_name = 'writeFlashConfig'
  );

INSERT IGNORE INTO codegen_context_field (
    schema_id, name, description, sort_index, property_name, transport_type, register_offset, bit_offset, length, translation_hint, default_literal, created_at, updated_at
) VALUES
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 1', NULL, 0, 'light1', 'BOOL_COIL', 0, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 2', NULL, 1, 'light2', 'BOOL_COIL', 1, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 3', NULL, 2, 'light3', 'BOOL_COIL', 2, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 4', NULL, 3, 'light4', 'BOOL_COIL', 3, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 5', NULL, 4, 'light5', 'BOOL_COIL', 4, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 6', NULL, 5, 'light6', 'BOOL_COIL', 5, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 7', NULL, 6, 'light7', 'BOOL_COIL', 6, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 8', NULL, 7, 'light8', 'BOOL_COIL', 7, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 9', NULL, 8, 'light9', 'BOOL_COIL', 8, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 10', NULL, 9, 'light10', 'BOOL_COIL', 9, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 11', NULL, 10, 'light11', 'BOOL_COIL', 10, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 12', NULL, 11, 'light12', 'BOOL_COIL', 11, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 13', NULL, 12, 'light13', 'BOOL_COIL', 12, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 14', NULL, 13, 'light14', 'BOOL_COIL', 13, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 15', NULL, 14, 'light15', 'BOOL_COIL', 14, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 16', NULL, 15, 'light16', 'BOOL_COIL', 15, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 17', NULL, 16, 'light17', 'BOOL_COIL', 16, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 18', NULL, 17, 'light18', 'BOOL_COIL', 17, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 19', NULL, 18, 'light19', 'BOOL_COIL', 18, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 20', NULL, 19, 'light20', 'BOOL_COIL', 19, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 21', NULL, 20, 'light21', 'BOOL_COIL', 20, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 22', NULL, 21, 'light22', 'BOOL_COIL', 21, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 23', NULL, 22, 'light23', 'BOOL_COIL', 22, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights' LIMIT 1), '电源灯 24', NULL, 23, 'light24', 'BOOL_COIL', 23, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getDeviceInfo' LIMIT 1), '固件版本', '板子固件版本号。', 0, 'firmwareVersion', 'STRING_ASCII', 0, 0, 8, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getDeviceInfo' LIMIT 1), 'CPU 型号', 'CPU 型号。', 1, 'cpuModel', 'STRING_ASCII', 8, 0, 8, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getDeviceInfo' LIMIT 1), '晶振频率', '晶振频率，单位 Hz。', 2, 'xtalFrequencyHz', 'U32_BE', 16, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getDeviceInfo' LIMIT 1), 'Flash 容量', 'Flash 容量，单位字节。', 3, 'flashSizeBytes', 'U32_BE', 18, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getDeviceInfo' LIMIT 1), 'MAC 地址', 'MAC 地址。', 4, 'macAddress', 'STRING_ASCII', 20, 0, 9, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'writeIndicatorLights' LIMIT 1), '故障灯', '故障灯开关状态。', 0, 'faultLightOn', 'BOOL_COIL', 0, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'writeIndicatorLights' LIMIT 1), '运行灯', '运行灯开关状态。', 1, 'runLightOn', 'BOOL_COIL', 1, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getFlashConfig' LIMIT 1), '魔术字', '魔术字：0x5A5A5A5A，校验 Flash 数据是否已初始化。', 0, 'magicWord', 'U32_BE', 0, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getFlashConfig' LIMIT 1), '端口配置', '24 路端口配置。', 1, 'portConfig', 'BYTE_ARRAY', 2, 0, 24, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getFlashConfig' LIMIT 1), '串口参数', '串口参数（波特率、校验位等）。', 2, 'uartParams', 'BYTE_ARRAY', 14, 0, 16, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getFlashConfig' LIMIT 1), '从机地址', 'Modbus 从机地址。', 3, 'slaveAddress', 'U8', 22, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getFlashConfig' LIMIT 1), '防抖参数', '抖动采样参数（阈值，范围 1-255，推荐 5）。', 4, 'debounceParams', 'BYTE_ARRAY', 23, 0, 4, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getFlashConfig' LIMIT 1), '帧间隔', 'Modbus 帧时间间隔，单位 ms。', 5, 'modbusInterval', 'U16', 25, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getFlashConfig' LIMIT 1), '看门狗使能', '看门狗硬件使能，0 表示关闭，1 表示开启。', 6, 'wdtEnable', 'U8', 26, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getFlashConfig' LIMIT 1), '固件升级标志', '固件升级标志，0 表示不升级，1 表示升级。', 7, 'firmwareUpgrade', 'U8', 27, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getFlashConfig' LIMIT 1), 'DI 硬件固件版本', 'DI 模块硬件固件版本号，低 8 位为次版本号，高 8 位为主版本号。', 8, 'diHardwareFirmware', 'BYTE_ARRAY', 28, 0, 2, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getFlashConfig' LIMIT 1), 'DI 状态', '24 路 DI 状态，每个 bit 代表 1 路，bit[0] = CH1。', 9, 'diStatus', 'BYTE_ARRAY', 29, 0, 3, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getFlashConfig' LIMIT 1), '故障状态', '故障状态标志，位掩码。', 10, 'faultStatus', 'U8', 31, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getFlashConfig' LIMIT 1), 'CRC', 'CRC16 校验，从 magicWord 到 diStatus 字段。', 11, 'crc', 'U16', 32, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'writeFlashConfig' LIMIT 1), '魔术字', '魔术字：0x5A5A5A5A，校验 Flash 数据是否已初始化。', 0, 'magicWord', 'U32_BE', 0, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'writeFlashConfig' LIMIT 1), '端口配置', '24 路端口配置。', 1, 'portConfig', 'BYTE_ARRAY', 2, 0, 24, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'writeFlashConfig' LIMIT 1), '串口参数', '串口参数（波特率、校验位等）。', 2, 'uartParams', 'BYTE_ARRAY', 14, 0, 16, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'writeFlashConfig' LIMIT 1), '从机地址', 'Modbus 从机地址。', 3, 'slaveAddress', 'U8', 22, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'writeFlashConfig' LIMIT 1), '防抖参数', '抖动采样参数（阈值，范围 1-255，推荐 5）。', 4, 'debounceParams', 'BYTE_ARRAY', 23, 0, 4, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'writeFlashConfig' LIMIT 1), '帧间隔', 'Modbus 帧时间间隔，单位 ms。', 5, 'modbusInterval', 'U16', 25, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'writeFlashConfig' LIMIT 1), '看门狗使能', '看门狗硬件使能，0 表示关闭，1 表示开启。', 6, 'wdtEnable', 'U8', 26, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'writeFlashConfig' LIMIT 1), '固件升级标志', '固件升级标志，0 表示不升级，1 表示升级。', 7, 'firmwareUpgrade', 'U8', 27, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'writeFlashConfig' LIMIT 1), 'DI 硬件固件版本', 'DI 模块硬件固件版本号，低 8 位为次版本号，高 8 位为主版本号。', 8, 'diHardwareFirmware', 'BYTE_ARRAY', 28, 0, 2, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'writeFlashConfig' LIMIT 1), 'DI 状态', '24 路 DI 状态，每个 bit 代表 1 路，bit[0] = CH1。', 9, 'diStatus', 'BYTE_ARRAY', 29, 0, 3, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'writeFlashConfig' LIMIT 1), '故障状态', '故障状态标志，位掩码。', 10, 'faultStatus', 'U8', 31, 0, 1, NULL, NULL, 1775606400000, 1775606400000),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'writeFlashConfig' LIMIT 1), 'CRC', 'CRC16 校验，从 magicWord 到 diStatus 字段。', 11, 'crc', 'U16', 32, 0, 1, NULL, NULL, 1775606400000, 1775606400000);

UPDATE codegen_context_field
SET description = '板子固件版本号。'
WHERE schema_id = (SELECT id FROM codegen_context_schema WHERE method_name = 'getDeviceInfo' LIMIT 1)
  AND property_name = 'firmwareVersion'
  AND COALESCE(description, '') = '';

UPDATE codegen_context_field
SET description = 'CPU 型号。'
WHERE schema_id = (SELECT id FROM codegen_context_schema WHERE method_name = 'getDeviceInfo' LIMIT 1)
  AND property_name = 'cpuModel'
  AND COALESCE(description, '') = '';

UPDATE codegen_context_field
SET description = '晶振频率，单位 Hz。'
WHERE schema_id = (SELECT id FROM codegen_context_schema WHERE method_name = 'getDeviceInfo' LIMIT 1)
  AND property_name = 'xtalFrequencyHz'
  AND COALESCE(description, '') = '';

UPDATE codegen_context_field
SET description = 'Flash 容量，单位字节。'
WHERE schema_id = (SELECT id FROM codegen_context_schema WHERE method_name = 'getDeviceInfo' LIMIT 1)
  AND property_name = 'flashSizeBytes'
  AND COALESCE(description, '') = '';

UPDATE codegen_context_field
SET description = 'MAC 地址。'
WHERE schema_id = (SELECT id FROM codegen_context_schema WHERE method_name = 'getDeviceInfo' LIMIT 1)
  AND property_name = 'macAddress'
  AND COALESCE(description, '') = '';

UPDATE codegen_context_field
SET description = '故障灯开关状态。'
WHERE schema_id = (SELECT id FROM codegen_context_schema WHERE method_name = 'writeIndicatorLights' LIMIT 1)
  AND property_name = 'faultLightOn'
  AND COALESCE(description, '') = '';

UPDATE codegen_context_field
SET description = '运行灯开关状态。'
WHERE schema_id = (SELECT id FROM codegen_context_schema WHERE method_name = 'writeIndicatorLights' LIMIT 1)
  AND property_name = 'runLightOn'
  AND COALESCE(description, '') = '';

UPDATE codegen_context_field
SET description = '魔术字：0x5A5A5A5A，校验 Flash 数据是否已初始化。'
WHERE schema_id IN (
    SELECT id FROM codegen_context_schema WHERE method_name IN ('getFlashConfig', 'writeFlashConfig')
)
  AND property_name = 'magicWord'
  AND COALESCE(description, '') = '';

UPDATE codegen_context_field
SET description = '24 路端口配置。'
WHERE schema_id IN (
    SELECT id FROM codegen_context_schema WHERE method_name IN ('getFlashConfig', 'writeFlashConfig')
)
  AND property_name = 'portConfig'
  AND COALESCE(description, '') = '';

UPDATE codegen_context_field
SET description = '串口参数（波特率、校验位等）。'
WHERE schema_id IN (
    SELECT id FROM codegen_context_schema WHERE method_name IN ('getFlashConfig', 'writeFlashConfig')
)
  AND property_name = 'uartParams'
  AND COALESCE(description, '') = '';

UPDATE codegen_context_field
SET description = 'Modbus 从机地址。'
WHERE schema_id IN (
    SELECT id FROM codegen_context_schema WHERE method_name IN ('getFlashConfig', 'writeFlashConfig')
)
  AND property_name = 'slaveAddress'
  AND COALESCE(description, '') = '';

UPDATE codegen_context_field
SET description = '抖动采样参数（阈值，范围 1-255，推荐 5）。'
WHERE schema_id IN (
    SELECT id FROM codegen_context_schema WHERE method_name IN ('getFlashConfig', 'writeFlashConfig')
)
  AND property_name = 'debounceParams'
  AND COALESCE(description, '') = '';

UPDATE codegen_context_field
SET description = 'Modbus 帧时间间隔，单位 ms。'
WHERE schema_id IN (
    SELECT id FROM codegen_context_schema WHERE method_name IN ('getFlashConfig', 'writeFlashConfig')
)
  AND property_name = 'modbusInterval'
  AND COALESCE(description, '') = '';

UPDATE codegen_context_field
SET description = '看门狗硬件使能，0 表示关闭，1 表示开启。'
WHERE schema_id IN (
    SELECT id FROM codegen_context_schema WHERE method_name IN ('getFlashConfig', 'writeFlashConfig')
)
  AND property_name = 'wdtEnable'
  AND COALESCE(description, '') = '';

UPDATE codegen_context_field
SET description = '固件升级标志，0 表示不升级，1 表示升级。'
WHERE schema_id IN (
    SELECT id FROM codegen_context_schema WHERE method_name IN ('getFlashConfig', 'writeFlashConfig')
)
  AND property_name = 'firmwareUpgrade'
  AND COALESCE(description, '') = '';

UPDATE codegen_context_field
SET description = 'DI 模块硬件固件版本号，低 8 位为次版本号，高 8 位为主版本号。'
WHERE schema_id IN (
    SELECT id FROM codegen_context_schema WHERE method_name IN ('getFlashConfig', 'writeFlashConfig')
)
  AND property_name = 'diHardwareFirmware'
  AND COALESCE(description, '') = '';

UPDATE codegen_context_field
SET description = '24 路 DI 状态，每个 bit 代表 1 路，bit[0] = CH1。'
WHERE schema_id IN (
    SELECT id FROM codegen_context_schema WHERE method_name IN ('getFlashConfig', 'writeFlashConfig')
)
  AND property_name = 'diStatus'
  AND COALESCE(description, '') = '';

UPDATE codegen_context_field
SET description = '故障状态标志，位掩码。'
WHERE schema_id IN (
    SELECT id FROM codegen_context_schema WHERE method_name IN ('getFlashConfig', 'writeFlashConfig')
)
  AND property_name = 'faultStatus'
  AND COALESCE(description, '') = '';

UPDATE codegen_context_field
SET description = 'CRC16 校验，从 magicWord 到 diStatus 字段。'
WHERE schema_id IN (
    SELECT id FROM codegen_context_schema WHERE method_name IN ('getFlashConfig', 'writeFlashConfig')
)
  AND property_name = 'crc'
  AND COALESCE(description, '') = '';
