CREATE TABLE IF NOT EXISTS codegen_context_context (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    enabled INTEGER NOT NULL DEFAULT 1,
    consumer_target TEXT NOT NULL,
    protocol_template_id INTEGER NOT NULL,
    external_c_output_root TEXT,
    server_output_root TEXT,
    shared_output_root TEXT,
    gateway_output_root TEXT,
    api_client_output_root TEXT,
    api_client_package_name TEXT,
    spring_route_output_root TEXT,
    c_output_root TEXT,
    markdown_output_root TEXT,
    kotlin_client_transports TEXT,
    c_expose_transports TEXT,
    artifact_kinds TEXT,
    c_output_project_dir TEXT,
    bridge_impl_path TEXT,
    keil_uvprojx_path TEXT,
    keil_target_name TEXT,
    keil_group_name TEXT,
    mxproject_path TEXT,
    rtu_port_path TEXT NOT NULL DEFAULT '/dev/ttyUSB0',
    rtu_unit_id INTEGER NOT NULL DEFAULT 1,
    rtu_baud_rate INTEGER NOT NULL DEFAULT 9600,
    rtu_data_bits INTEGER NOT NULL DEFAULT 8,
    rtu_stop_bits INTEGER NOT NULL DEFAULT 1,
    rtu_parity TEXT NOT NULL DEFAULT 'none',
    rtu_timeout_ms INTEGER NOT NULL DEFAULT 1000,
    rtu_retries INTEGER NOT NULL DEFAULT 2,
    tcp_host TEXT NOT NULL DEFAULT '127.0.0.1',
    tcp_port INTEGER NOT NULL DEFAULT 502,
    tcp_unit_id INTEGER NOT NULL DEFAULT 1,
    tcp_timeout_ms INTEGER NOT NULL DEFAULT 1000,
    tcp_retries INTEGER NOT NULL DEFAULT 2,
    mqtt_broker_url TEXT NOT NULL DEFAULT 'tcp://127.0.0.1:1883',
    mqtt_client_id TEXT NOT NULL DEFAULT 'modbus-mqtt-client',
    mqtt_request_topic TEXT NOT NULL DEFAULT 'modbus/request',
    mqtt_response_topic TEXT NOT NULL DEFAULT 'modbus/response',
    mqtt_qos INTEGER NOT NULL DEFAULT 1,
    mqtt_timeout_ms INTEGER NOT NULL DEFAULT 1000,
    mqtt_retries INTEGER NOT NULL DEFAULT 2,
    create_time TEXT NOT NULL,
    update_time TEXT,
    UNIQUE(code),
    FOREIGN KEY (protocol_template_id) REFERENCES host_config_protocol_template(id)
);

CREATE TABLE IF NOT EXISTS codegen_context_modbus_contract (
    context_id INTEGER NOT NULL,
    context_code TEXT NOT NULL,
    context_name TEXT NOT NULL,
    enabled INTEGER NOT NULL,
    consumer_target TEXT NOT NULL,
    protocol_template_code TEXT NOT NULL,
    transport TEXT NOT NULL,
    selected INTEGER NOT NULL DEFAULT 0,
    payload TEXT NOT NULL,
    updated_at INTEGER NOT NULL,
    PRIMARY KEY (context_id, transport),
    FOREIGN KEY (context_id) REFERENCES codegen_context_context(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS codegen_context_class (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    context_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    class_kind TEXT NOT NULL,
    class_name TEXT NOT NULL,
    package_name TEXT,
    create_time TEXT NOT NULL,
    update_time TEXT,
    UNIQUE(context_id, class_name),
    FOREIGN KEY (context_id) REFERENCES codegen_context_context(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS codegen_context_method (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    owner_class_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    method_name TEXT NOT NULL,
    request_class_name TEXT,
    response_class_name TEXT,
    create_time TEXT NOT NULL,
    update_time TEXT,
    UNIQUE(owner_class_id, method_name),
    FOREIGN KEY (owner_class_id) REFERENCES codegen_context_class(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS codegen_context_property (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    owner_class_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    property_name TEXT NOT NULL,
    type_name TEXT NOT NULL,
    nullable INTEGER NOT NULL DEFAULT 0,
    default_literal TEXT,
    create_time TEXT NOT NULL,
    update_time TEXT,
    UNIQUE(owner_class_id, property_name),
    FOREIGN KEY (owner_class_id) REFERENCES codegen_context_class(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS codegen_context_definition (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    protocol_template_id INTEGER NOT NULL,
    code TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    target_kind TEXT NOT NULL,
    binding_target_mode TEXT NOT NULL,
    source_kind TEXT NOT NULL,
    create_time TEXT NOT NULL,
    update_time TEXT,
    UNIQUE(protocol_template_id, code),
    FOREIGN KEY (protocol_template_id) REFERENCES host_config_protocol_template(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS codegen_context_param_definition (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    definition_id INTEGER NOT NULL,
    code TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    value_type TEXT NOT NULL,
    required INTEGER NOT NULL DEFAULT 0,
    default_value TEXT,
    enum_options TEXT,
    placeholder TEXT,
    create_time TEXT NOT NULL,
    update_time TEXT,
    UNIQUE(definition_id, code),
    FOREIGN KEY (definition_id) REFERENCES codegen_context_definition(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS codegen_context_binding (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    definition_id INTEGER NOT NULL,
    owner_class_id INTEGER,
    owner_method_id INTEGER,
    owner_property_id INTEGER,
    sort_index INTEGER NOT NULL DEFAULT 0,
    create_time TEXT NOT NULL,
    update_time TEXT,
    FOREIGN KEY (definition_id) REFERENCES codegen_context_definition(id) ON DELETE CASCADE,
    FOREIGN KEY (owner_class_id) REFERENCES codegen_context_class(id) ON DELETE CASCADE,
    FOREIGN KEY (owner_method_id) REFERENCES codegen_context_method(id) ON DELETE CASCADE,
    FOREIGN KEY (owner_property_id) REFERENCES codegen_context_property(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS codegen_context_binding_value (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    binding_id INTEGER NOT NULL,
    param_definition_id INTEGER NOT NULL,
    value TEXT,
    create_time TEXT NOT NULL,
    update_time TEXT,
    UNIQUE(binding_id, param_definition_id),
    FOREIGN KEY (binding_id) REFERENCES codegen_context_binding(id) ON DELETE CASCADE,
    FOREIGN KEY (param_definition_id) REFERENCES codegen_context_param_definition(id) ON DELETE CASCADE
);

INSERT INTO codegen_context_definition (
    protocol_template_id,
    code,
    name,
    description,
    sort_index,
    target_kind,
    binding_target_mode,
    source_kind,
    create_time,
    update_time
)
SELECT
    template.id,
    'MODBUS_OPERATION',
    'Modbus 方法上下文',
    '描述一个方法对应的 Modbus 功能码、读写方向和基地址。',
    0,
    'METHOD',
    'SINGLE',
    'BUILTIN',
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM host_config_protocol_template template
WHERE template.code IN ('MODBUS_RTU_CLIENT', 'MODBUS_TCP_CLIENT')
  AND NOT EXISTS (
      SELECT 1
      FROM codegen_context_definition existing
      WHERE existing.protocol_template_id = template.id
        AND existing.code = 'MODBUS_OPERATION'
  );

INSERT INTO codegen_context_definition (
    protocol_template_id,
    code,
    name,
    description,
    sort_index,
    target_kind,
    binding_target_mode,
    source_kind,
    create_time,
    update_time
)
SELECT
    template.id,
    'MODBUS_FIELD',
    'Modbus 字段上下文',
    '描述一个属性对应的 Modbus 编码方式和寄存器布局。',
    10,
    'FIELD',
    'SINGLE',
    'BUILTIN',
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM host_config_protocol_template template
WHERE template.code IN ('MODBUS_RTU_CLIENT', 'MODBUS_TCP_CLIENT')
  AND NOT EXISTS (
      SELECT 1
      FROM codegen_context_definition existing
      WHERE existing.protocol_template_id = template.id
        AND existing.code = 'MODBUS_FIELD'
  );

INSERT INTO codegen_context_param_definition (
    definition_id,
    code,
    name,
    description,
    sort_index,
    value_type,
    required,
    default_value,
    enum_options,
    placeholder,
    create_time,
    update_time
)
SELECT
    definition.id,
    'direction',
    '读写方向',
    '决定该方法生成到读接口还是写接口。',
    0,
    'ENUM',
    1,
    'READ',
    '["READ","WRITE"]',
    NULL,
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM codegen_context_definition definition
WHERE definition.code = 'MODBUS_OPERATION'
  AND NOT EXISTS (
      SELECT 1
      FROM codegen_context_param_definition existing
      WHERE existing.definition_id = definition.id
        AND existing.code = 'direction'
  );

INSERT INTO codegen_context_param_definition (
    definition_id,
    code,
    name,
    description,
    sort_index,
    value_type,
    required,
    default_value,
    enum_options,
    placeholder,
    create_time,
    update_time
)
SELECT
    definition.id,
    'functionCode',
    '功能码',
    '决定 Modbus 读写功能码。',
    10,
    'ENUM',
    1,
    NULL,
    '["READ_COILS","READ_DISCRETE_INPUTS","READ_INPUT_REGISTERS","READ_HOLDING_REGISTERS","WRITE_SINGLE_COIL","WRITE_MULTIPLE_COILS","WRITE_SINGLE_REGISTER","WRITE_MULTIPLE_REGISTERS"]',
    NULL,
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM codegen_context_definition definition
WHERE definition.code = 'MODBUS_OPERATION'
  AND NOT EXISTS (
      SELECT 1
      FROM codegen_context_param_definition existing
      WHERE existing.definition_id = definition.id
        AND existing.code = 'functionCode'
  );

INSERT INTO codegen_context_param_definition (
    definition_id,
    code,
    name,
    description,
    sort_index,
    value_type,
    required,
    default_value,
    enum_options,
    placeholder,
    create_time,
    update_time
)
SELECT
    definition.id,
    'baseAddress',
    '基地址',
    '当前方法对应的起始地址。',
    20,
    'INT',
    1,
    '0',
    NULL,
    '例如 200',
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM codegen_context_definition definition
WHERE definition.code = 'MODBUS_OPERATION'
  AND NOT EXISTS (
      SELECT 1
      FROM codegen_context_param_definition existing
      WHERE existing.definition_id = definition.id
        AND existing.code = 'baseAddress'
  );

INSERT INTO codegen_context_param_definition (
    definition_id,
    code,
    name,
    description,
    sort_index,
    value_type,
    required,
    default_value,
    enum_options,
    placeholder,
    create_time,
    update_time
)
SELECT
    definition.id,
    'transportType',
    '传输类型',
    '属性在 Modbus 中的编码方式。',
    0,
    'ENUM',
    1,
    NULL,
    '["BOOL_COIL","U16","U32_BE","STRING_ASCII","STRING_UTF8"]',
    NULL,
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM codegen_context_definition definition
WHERE definition.code = 'MODBUS_FIELD'
  AND NOT EXISTS (
      SELECT 1
      FROM codegen_context_param_definition existing
      WHERE existing.definition_id = definition.id
        AND existing.code = 'transportType'
  );

INSERT INTO codegen_context_param_definition (
    definition_id,
    code,
    name,
    description,
    sort_index,
    value_type,
    required,
    default_value,
    enum_options,
    placeholder,
    create_time,
    update_time
)
SELECT
    definition.id,
    'registerOffset',
    '寄存器偏移',
    '相对方法基地址的偏移。',
    10,
    'INT',
    1,
    '0',
    NULL,
    '例如 0',
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM codegen_context_definition definition
WHERE definition.code = 'MODBUS_FIELD'
  AND NOT EXISTS (
      SELECT 1
      FROM codegen_context_param_definition existing
      WHERE existing.definition_id = definition.id
        AND existing.code = 'registerOffset'
  );

INSERT INTO codegen_context_param_definition (
    definition_id,
    code,
    name,
    description,
    sort_index,
    value_type,
    required,
    default_value,
    enum_options,
    placeholder,
    create_time,
    update_time
)
SELECT
    definition.id,
    'bitOffset',
    '位偏移',
    '仅线圈或位域场景使用，当前默认 0。',
    20,
    'INT',
    0,
    '0',
    NULL,
    '例如 0',
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM codegen_context_definition definition
WHERE definition.code = 'MODBUS_FIELD'
  AND NOT EXISTS (
      SELECT 1
      FROM codegen_context_param_definition existing
      WHERE existing.definition_id = definition.id
        AND existing.code = 'bitOffset'
  );

INSERT INTO codegen_context_param_definition (
    definition_id,
    code,
    name,
    description,
    sort_index,
    value_type,
    required,
    default_value,
    enum_options,
    placeholder,
    create_time,
    update_time
)
SELECT
    definition.id,
    'length',
    '长度',
    '字符串等需要多个寄存器时使用。',
    30,
    'INT',
    0,
    '1',
    NULL,
    '例如 4',
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM codegen_context_definition definition
WHERE definition.code = 'MODBUS_FIELD'
  AND NOT EXISTS (
      SELECT 1
      FROM codegen_context_param_definition existing
      WHERE existing.definition_id = definition.id
        AND existing.code = 'length'
  );

INSERT INTO codegen_context_param_definition (
    definition_id,
    code,
    name,
    description,
    sort_index,
    value_type,
    required,
    default_value,
    enum_options,
    placeholder,
    create_time,
    update_time
)
SELECT
    definition.id,
    'translationHint',
    '转换提示',
    '上层手工翻译提示，例如 C 结构体字段说明。',
    40,
    'STRING',
    0,
    NULL,
    NULL,
    '例如 FlashConfig.magic_word',
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM codegen_context_definition definition
WHERE definition.code = 'MODBUS_FIELD'
  AND NOT EXISTS (
      SELECT 1
      FROM codegen_context_param_definition existing
      WHERE existing.definition_id = definition.id
        AND existing.code = 'translationHint'
  );

INSERT INTO codegen_context_param_definition (
    definition_id,
    code,
    name,
    description,
    sort_index,
    value_type,
    required,
    default_value,
    enum_options,
    placeholder,
    create_time,
    update_time
)
SELECT
    definition.id,
    'defaultLiteral',
    '默认值',
    '生成请求实体或说明文档时可展示的默认字面量。',
    50,
    'STRING',
    0,
    NULL,
    NULL,
    '例如 0x5A5A5A5A',
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM codegen_context_definition definition
WHERE definition.code = 'MODBUS_FIELD'
  AND NOT EXISTS (
      SELECT 1
      FROM codegen_context_param_definition existing
      WHERE existing.definition_id = definition.id
        AND existing.code = 'defaultLiteral'
  );

INSERT OR IGNORE INTO codegen_context_definition (
    protocol_template_id,
    code,
    name,
    description,
    sort_index,
    target_kind,
    binding_target_mode,
    source_kind,
    create_time,
    update_time
)
SELECT
    template.id,
    'MODBUS_OPERATION',
    'Modbus 方法上下文',
    '描述一个方法对应的 Modbus 功能码、读写方向和基地址。',
    0,
    'METHOD',
    'SINGLE',
    'BUILTIN',
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM host_config_protocol_template template
WHERE template.code IN ('MODBUS_RTU_CLIENT', 'MODBUS_TCP_CLIENT');

INSERT OR IGNORE INTO codegen_context_definition (
    protocol_template_id,
    code,
    name,
    description,
    sort_index,
    target_kind,
    binding_target_mode,
    source_kind,
    create_time,
    update_time
)
SELECT
    template.id,
    'MODBUS_FIELD',
    'Modbus 字段上下文',
    '描述一个属性对应的 Modbus 编码方式和寄存器布局。',
    10,
    'FIELD',
    'SINGLE',
    'BUILTIN',
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM host_config_protocol_template template
WHERE template.code IN ('MODBUS_RTU_CLIENT', 'MODBUS_TCP_CLIENT');

INSERT OR IGNORE INTO codegen_context_param_definition (
    definition_id,
    code,
    name,
    description,
    sort_index,
    value_type,
    required,
    default_value,
    enum_options,
    placeholder,
    create_time,
    update_time
)
SELECT
    definition.id,
    'direction',
    '读写方向',
    '决定该方法生成到读接口还是写接口。',
    0,
    'ENUM',
    1,
    'READ',
    '["READ","WRITE"]',
    NULL,
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM codegen_context_definition definition
WHERE definition.code = 'MODBUS_OPERATION';

INSERT OR IGNORE INTO codegen_context_param_definition (
    definition_id,
    code,
    name,
    description,
    sort_index,
    value_type,
    required,
    default_value,
    enum_options,
    placeholder,
    create_time,
    update_time
)
SELECT
    definition.id,
    'functionCode',
    '功能码',
    '决定 Modbus 读写功能码。',
    10,
    'ENUM',
    1,
    NULL,
    '["READ_COILS","READ_DISCRETE_INPUTS","READ_INPUT_REGISTERS","READ_HOLDING_REGISTERS","WRITE_SINGLE_COIL","WRITE_MULTIPLE_COILS","WRITE_SINGLE_REGISTER","WRITE_MULTIPLE_REGISTERS"]',
    NULL,
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM codegen_context_definition definition
WHERE definition.code = 'MODBUS_OPERATION';

INSERT OR IGNORE INTO codegen_context_param_definition (
    definition_id,
    code,
    name,
    description,
    sort_index,
    value_type,
    required,
    default_value,
    enum_options,
    placeholder,
    create_time,
    update_time
)
SELECT
    definition.id,
    'baseAddress',
    '基地址',
    '当前方法对应的起始地址。',
    20,
    'INT',
    1,
    '0',
    NULL,
    '例如 200',
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM codegen_context_definition definition
WHERE definition.code = 'MODBUS_OPERATION';

INSERT OR IGNORE INTO codegen_context_param_definition (
    definition_id,
    code,
    name,
    description,
    sort_index,
    value_type,
    required,
    default_value,
    enum_options,
    placeholder,
    create_time,
    update_time
)
SELECT
    definition.id,
    'transportType',
    '传输类型',
    '属性在 Modbus 中的编码方式。',
    0,
    'ENUM',
    1,
    NULL,
    '["BOOL_COIL","U16","U32_BE","STRING_ASCII","STRING_UTF8"]',
    NULL,
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM codegen_context_definition definition
WHERE definition.code = 'MODBUS_FIELD';

INSERT OR IGNORE INTO codegen_context_param_definition (
    definition_id,
    code,
    name,
    description,
    sort_index,
    value_type,
    required,
    default_value,
    enum_options,
    placeholder,
    create_time,
    update_time
)
SELECT
    definition.id,
    'registerOffset',
    '寄存器偏移',
    '相对方法基地址的偏移。',
    10,
    'INT',
    1,
    '0',
    NULL,
    '例如 0',
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM codegen_context_definition definition
WHERE definition.code = 'MODBUS_FIELD';

INSERT OR IGNORE INTO codegen_context_param_definition (
    definition_id,
    code,
    name,
    description,
    sort_index,
    value_type,
    required,
    default_value,
    enum_options,
    placeholder,
    create_time,
    update_time
)
SELECT
    definition.id,
    'bitOffset',
    '位偏移',
    '仅线圈或位域场景使用，当前默认 0。',
    20,
    'INT',
    0,
    '0',
    NULL,
    '例如 0',
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM codegen_context_definition definition
WHERE definition.code = 'MODBUS_FIELD';

INSERT OR IGNORE INTO codegen_context_param_definition (
    definition_id,
    code,
    name,
    description,
    sort_index,
    value_type,
    required,
    default_value,
    enum_options,
    placeholder,
    create_time,
    update_time
)
SELECT
    definition.id,
    'length',
    '长度',
    '字符串等需要多个寄存器时使用。',
    30,
    'INT',
    0,
    '1',
    NULL,
    '例如 4',
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM codegen_context_definition definition
WHERE definition.code = 'MODBUS_FIELD';

INSERT OR IGNORE INTO codegen_context_param_definition (
    definition_id,
    code,
    name,
    description,
    sort_index,
    value_type,
    required,
    default_value,
    enum_options,
    placeholder,
    create_time,
    update_time
)
SELECT
    definition.id,
    'translationHint',
    '转换提示',
    '上层手工翻译提示，例如 C 结构体字段说明。',
    40,
    'STRING',
    0,
    NULL,
    NULL,
    '例如 FlashConfig.magic_word',
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM codegen_context_definition definition
WHERE definition.code = 'MODBUS_FIELD';

INSERT OR IGNORE INTO codegen_context_param_definition (
    definition_id,
    code,
    name,
    description,
    sort_index,
    value_type,
    required,
    default_value,
    enum_options,
    placeholder,
    create_time,
    update_time
)
SELECT
    definition.id,
    'defaultLiteral',
    '默认值',
    '生成请求实体或说明文档时可展示的默认字面量。',
    50,
    'STRING',
    0,
    NULL,
    NULL,
    '例如 0x5A5A5A5A',
    '2026-04-09 00:00:00',
    '2026-04-09 00:00:00'
FROM codegen_context_definition definition
WHERE definition.code = 'MODBUS_FIELD';
