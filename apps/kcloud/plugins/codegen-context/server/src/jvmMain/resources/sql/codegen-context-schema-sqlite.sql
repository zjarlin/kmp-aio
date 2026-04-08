CREATE TABLE IF NOT EXISTS codegen_context_context (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    enabled INTEGER NOT NULL DEFAULT 1,
    consumer_target TEXT NOT NULL,
    protocol_template_id INTEGER NOT NULL,
    create_time TEXT NOT NULL,
    update_time TEXT,
    FOREIGN KEY(protocol_template_id) REFERENCES host_config_protocol_template(id)
);

CREATE TABLE IF NOT EXISTS codegen_context_schema (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    context_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    direction TEXT NOT NULL,
    function_code TEXT NOT NULL,
    base_address INTEGER NOT NULL,
    method_name TEXT NOT NULL,
    model_name TEXT,
    create_time TEXT NOT NULL,
    update_time TEXT,
    UNIQUE(context_id, method_name),
    FOREIGN KEY(context_id) REFERENCES codegen_context_context(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS codegen_context_field (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    schema_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    property_name TEXT NOT NULL,
    transport_type TEXT NOT NULL,
    register_offset INTEGER NOT NULL,
    bit_offset INTEGER NOT NULL DEFAULT 0,
    length INTEGER NOT NULL DEFAULT 1,
    translation_hint TEXT,
    default_literal TEXT,
    create_time TEXT NOT NULL,
    update_time TEXT,
    UNIQUE(schema_id, property_name),
    FOREIGN KEY(schema_id) REFERENCES codegen_context_schema(id) ON DELETE CASCADE
);

INSERT INTO codegen_context_context (
    code,
    name,
    description,
    enabled,
    consumer_target,
    protocol_template_id,
    create_time,
    update_time
)
SELECT
    'MCU_DEVICE_DEFAULT',
    'MCU 默认协议桥接',
    '接管 mcu-console 当前默认的设备读写契约。',
    1,
    'MCU_CONSOLE',
    template.id,
    '2026-04-08 00:00:00',
    '2026-04-08 00:00:00'
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
    create_time,
    update_time
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
    '2026-04-08 00:00:00',
    '2026-04-08 00:00:00'
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
    create_time,
    update_time
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
    '2026-04-08 00:00:00',
    '2026-04-08 00:00:00'
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
    create_time,
    update_time
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
    '2026-04-08 00:00:00',
    '2026-04-08 00:00:00'
FROM codegen_context_context context
WHERE context.code = 'MCU_DEVICE_DEFAULT'
  AND NOT EXISTS (
      SELECT 1 FROM codegen_context_schema existing
      WHERE existing.context_id = context.id AND existing.method_name = 'writeIndicatorLights'
  );

INSERT OR IGNORE INTO codegen_context_field (
    schema_id, name, description, sort_index, property_name, transport_type, register_offset, bit_offset, length, translation_hint, default_literal, create_time, update_time
) VALUES
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 1', NULL, 0, 'light1', 'BOOL_COIL', 0, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 2', NULL, 1, 'light2', 'BOOL_COIL', 1, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 3', NULL, 2, 'light3', 'BOOL_COIL', 2, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 4', NULL, 3, 'light4', 'BOOL_COIL', 3, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 5', NULL, 4, 'light5', 'BOOL_COIL', 4, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 6', NULL, 5, 'light6', 'BOOL_COIL', 5, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 7', NULL, 6, 'light7', 'BOOL_COIL', 6, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 8', NULL, 7, 'light8', 'BOOL_COIL', 7, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 9', NULL, 8, 'light9', 'BOOL_COIL', 8, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 10', NULL, 9, 'light10', 'BOOL_COIL', 9, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 11', NULL, 10, 'light11', 'BOOL_COIL', 10, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 12', NULL, 11, 'light12', 'BOOL_COIL', 11, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 13', NULL, 12, 'light13', 'BOOL_COIL', 12, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 14', NULL, 13, 'light14', 'BOOL_COIL', 13, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 15', NULL, 14, 'light15', 'BOOL_COIL', 14, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 16', NULL, 15, 'light16', 'BOOL_COIL', 15, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 17', NULL, 16, 'light17', 'BOOL_COIL', 16, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 18', NULL, 17, 'light18', 'BOOL_COIL', 17, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 19', NULL, 18, 'light19', 'BOOL_COIL', 18, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 20', NULL, 19, 'light20', 'BOOL_COIL', 19, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 21', NULL, 20, 'light21', 'BOOL_COIL', 20, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 22', NULL, 21, 'light22', 'BOOL_COIL', 21, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 23', NULL, 22, 'light23', 'BOOL_COIL', 22, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'get24PowerLights'), '电源灯 24', NULL, 23, 'light24', 'BOOL_COIL', 23, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getDeviceInfo'), '固件版本', NULL, 0, 'firmwareVersion', 'STRING_ASCII', 0, 0, 8, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getDeviceInfo'), 'CPU 型号', NULL, 1, 'cpuModel', 'STRING_ASCII', 8, 0, 8, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getDeviceInfo'), '晶振频率', NULL, 2, 'xtalFrequencyHz', 'U32_BE', 16, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getDeviceInfo'), 'Flash 容量', NULL, 3, 'flashSizeBytes', 'U32_BE', 18, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'getDeviceInfo'), 'MAC 地址', NULL, 4, 'macAddress', 'STRING_ASCII', 20, 0, 9, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'writeIndicatorLights'), '故障灯', NULL, 0, 'faultLightOn', 'BOOL_COIL', 0, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00'),
    ((SELECT id FROM codegen_context_schema WHERE method_name = 'writeIndicatorLights'), '运行灯', NULL, 1, 'runLightOn', 'BOOL_COIL', 1, 0, 1, NULL, NULL, '2026-04-08 00:00:00', '2026-04-08 00:00:00');
