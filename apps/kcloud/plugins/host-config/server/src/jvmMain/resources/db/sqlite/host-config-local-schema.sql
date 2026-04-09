CREATE TABLE IF NOT EXISTS host_config_project (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT,
    remark TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(name)
);

CREATE TABLE IF NOT EXISTS host_config_protocol_template (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    metadata_json TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(code)
);

CREATE TABLE IF NOT EXISTS host_config_module_template (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    protocol_template_id INTEGER NOT NULL,
    code TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    channel_count INTEGER,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(code),
    FOREIGN KEY (protocol_template_id) REFERENCES host_config_protocol_template(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS host_config_device_type (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(code)
);

CREATE TABLE IF NOT EXISTS host_config_register_type (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(code)
);

CREATE TABLE IF NOT EXISTS host_config_data_type (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(code)
);

CREATE TABLE IF NOT EXISTS host_config_label_definition (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    color_hex TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(code)
);

CREATE TABLE IF NOT EXISTS host_config_product_definition (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    vendor TEXT,
    category TEXT,
    enabled INTEGER NOT NULL DEFAULT 1,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(code)
);

CREATE TABLE IF NOT EXISTS host_config_device_definition (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL,
    device_type_id INTEGER,
    code TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    supports_telemetry INTEGER NOT NULL DEFAULT 1,
    supports_control INTEGER NOT NULL DEFAULT 0,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(product_id, code),
    FOREIGN KEY (product_id) REFERENCES host_config_product_definition(id) ON DELETE CASCADE,
    FOREIGN KEY (device_type_id) REFERENCES host_config_device_type(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS host_config_property_definition (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_definition_id INTEGER NOT NULL,
    data_type_id INTEGER NOT NULL,
    identifier TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    unit TEXT,
    required INTEGER NOT NULL DEFAULT 0,
    writable INTEGER NOT NULL DEFAULT 0,
    telemetry INTEGER NOT NULL DEFAULT 1,
    nullable INTEGER NOT NULL DEFAULT 1,
    length INTEGER,
    attributes_json TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(device_definition_id, identifier),
    FOREIGN KEY (device_definition_id) REFERENCES host_config_device_definition(id) ON DELETE CASCADE,
    FOREIGN KEY (data_type_id) REFERENCES host_config_data_type(id)
);

CREATE TABLE IF NOT EXISTS host_config_feature_definition (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_definition_id INTEGER NOT NULL,
    identifier TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    input_schema TEXT,
    output_schema TEXT,
    asynchronous INTEGER NOT NULL DEFAULT 0,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(device_definition_id, identifier),
    FOREIGN KEY (device_definition_id) REFERENCES host_config_device_definition(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS host_config_product_definition_label (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL,
    label_id INTEGER NOT NULL,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(product_id, label_id),
    FOREIGN KEY (product_id) REFERENCES host_config_product_definition(id) ON DELETE CASCADE,
    FOREIGN KEY (label_id) REFERENCES host_config_label_definition(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS host_config_protocol_instance (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    protocol_template_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    polling_interval_ms INTEGER NOT NULL,
    transport_type TEXT,
    host TEXT,
    tcp_port INTEGER,
    port_name TEXT,
    baud_rate INTEGER,
    data_bits INTEGER,
    stop_bits INTEGER,
    parity TEXT,
    response_timeout_ms INTEGER,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(name),
    FOREIGN KEY (protocol_template_id) REFERENCES host_config_protocol_template(id)
);

CREATE TABLE IF NOT EXISTS host_config_project_protocol (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER NOT NULL,
    protocol_id INTEGER NOT NULL,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(project_id, protocol_id),
    FOREIGN KEY (project_id) REFERENCES host_config_project(id) ON DELETE CASCADE,
    FOREIGN KEY (protocol_id) REFERENCES host_config_protocol_instance(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS host_config_module_instance (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    protocol_id INTEGER NOT NULL,
    module_template_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    port_name TEXT,
    baud_rate INTEGER,
    data_bits INTEGER,
    stop_bits INTEGER,
    parity TEXT,
    response_timeout_ms INTEGER,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(protocol_id, name),
    FOREIGN KEY (protocol_id) REFERENCES host_config_protocol_instance(id) ON DELETE CASCADE,
    FOREIGN KEY (module_template_id) REFERENCES host_config_module_template(id)
);

CREATE TABLE IF NOT EXISTS host_config_device (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    module_id INTEGER NOT NULL,
    device_type_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    station_no INTEGER NOT NULL,
    request_interval_ms INTEGER,
    write_interval_ms INTEGER,
    byte_order2 TEXT,
    byte_order4 TEXT,
    float_order TEXT,
    batch_analog_start INTEGER,
    batch_analog_length INTEGER,
    batch_digital_start INTEGER,
    batch_digital_length INTEGER,
    disabled INTEGER NOT NULL DEFAULT 0,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(module_id, name),
    FOREIGN KEY (module_id) REFERENCES host_config_module_instance(id) ON DELETE CASCADE,
    FOREIGN KEY (device_type_id) REFERENCES host_config_device_type(id)
);

CREATE TABLE IF NOT EXISTS host_config_tag (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_id INTEGER NOT NULL,
    data_type_id INTEGER NOT NULL,
    register_type_id INTEGER NOT NULL,
    forward_register_type_id INTEGER,
    name TEXT NOT NULL,
    description TEXT,
    register_address INTEGER NOT NULL,
    enabled INTEGER NOT NULL DEFAULT 1,
    default_value TEXT,
    exception_value TEXT,
    point_type TEXT,
    debounce_ms INTEGER,
    sort_index INTEGER NOT NULL DEFAULT 0,
    scaling_enabled INTEGER NOT NULL DEFAULT 0,
    scaling_offset REAL,
    raw_min REAL,
    raw_max REAL,
    eng_min REAL,
    eng_max REAL,
    forward_enabled INTEGER NOT NULL DEFAULT 0,
    forward_register_address INTEGER,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(device_id, name),
    UNIQUE(device_id, register_type_id, register_address),
    FOREIGN KEY (device_id) REFERENCES host_config_device(id) ON DELETE CASCADE,
    FOREIGN KEY (data_type_id) REFERENCES host_config_data_type(id),
    FOREIGN KEY (register_type_id) REFERENCES host_config_register_type(id),
    FOREIGN KEY (forward_register_type_id) REFERENCES host_config_register_type(id)
);

CREATE TABLE IF NOT EXISTS host_config_tag_value_text (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tag_id INTEGER NOT NULL,
    raw_value TEXT NOT NULL,
    display_text TEXT NOT NULL,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY (tag_id) REFERENCES host_config_tag(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS host_config_project_mqtt_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER NOT NULL,
    enabled INTEGER NOT NULL DEFAULT 0,
    breakpoint_resume INTEGER NOT NULL DEFAULT 0,
    gateway_name TEXT,
    vendor TEXT,
    host TEXT,
    port INTEGER,
    topic TEXT,
    gateway_id TEXT,
    auth_enabled INTEGER NOT NULL DEFAULT 0,
    username TEXT,
    password_encrypted TEXT,
    tls_enabled INTEGER NOT NULL DEFAULT 0,
    cert_file_ref TEXT,
    client_id TEXT,
    keep_alive_sec INTEGER,
    qos INTEGER,
    report_period_sec INTEGER,
    precision_value REAL,
    value_change_ratio_enabled INTEGER NOT NULL DEFAULT 0,
    cloud_control_disabled INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(project_id),
    FOREIGN KEY (project_id) REFERENCES host_config_project(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS host_config_project_modbus_server_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER NOT NULL,
    transport_type TEXT NOT NULL,
    enabled INTEGER NOT NULL DEFAULT 0,
    tcp_port INTEGER,
    port_name TEXT,
    baud_rate INTEGER,
    data_bits INTEGER,
    stop_bits INTEGER,
    parity TEXT,
    station_no INTEGER,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(project_id, transport_type),
    FOREIGN KEY (project_id) REFERENCES host_config_project(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS host_config_project_gateway_pin_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER NOT NULL,
    fault_indicator_pin TEXT NOT NULL DEFAULT 'PA8',
    running_indicator_pin TEXT NOT NULL DEFAULT 'PA2',
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(project_id),
    FOREIGN KEY (project_id) REFERENCES host_config_project(id) ON DELETE CASCADE
);

INSERT OR IGNORE INTO host_config_protocol_template (
    id, code, name, description, metadata_json, sort_index, created_at, updated_at
) VALUES
    (1, 'MODBUS_RTU_CLIENT', 'ModbusRTU', 'Modbus RTU 客户端协议', '{"transportType":"RTU","transportForm":{"title":"通信配置","subtitle":"协议模板元数据决定字段集合，新增 RTU 类协议时不再改界面分支。","summaryKeys":["PORT_NAME","BAUD_RATE","PARITY"],"fields":[{"key":"PORT_NAME","label":"串口","widget":"TEXT","required":true,"placeholder":"例如 COM4"},{"key":"BAUD_RATE","label":"波特率","widget":"SELECT","required":true,"helperText":"300-115200bps 可选","defaultValue":"9600","options":[{"value":"300","label":"300"},{"value":"600","label":"600"},{"value":"1200","label":"1200"},{"value":"2400","label":"2400"},{"value":"4800","label":"4800"},{"value":"9600","label":"9600"},{"value":"19200","label":"19200"},{"value":"38400","label":"38400"},{"value":"57600","label":"57600"},{"value":"115200","label":"115200"}]},{"key":"DATA_BITS","label":"数据位","widget":"SELECT","required":true,"helperText":"7 位、8 位可选","defaultValue":"8","options":[{"value":"7","label":"7"},{"value":"8","label":"8"}]},{"key":"STOP_BITS","label":"停止位","widget":"SELECT","required":true,"helperText":"1 位、2 位可选","defaultValue":"1","options":[{"value":"1","label":"1"},{"value":"2","label":"2"}]},{"key":"PARITY","label":"校验位","widget":"SELECT","required":true,"defaultValue":"NONE","options":[{"value":"NONE","label":"NONE"},{"value":"ODD","label":"ODD"},{"value":"EVEN","label":"EVEN"}]},{"key":"RESPONSE_TIMEOUT_MS","label":"响应超时(ms)","widget":"NUMBER","helperText":"按协议模板元数据控制默认值和提示文案。","defaultValue":"1000"}]}}', 1, 1743465600000, 1744156800000),
    (2, 'MODBUS_TCP_CLIENT', 'ModbusTCP', 'Modbus TCP 客户端协议', '{"transportType":"TCP","transportForm":{"title":"通信配置","subtitle":"协议模板元数据决定字段集合，新增 TCP 类协议时只补模板元数据。","summaryKeys":["HOST","TCP_PORT"],"fields":[{"key":"HOST","label":"主机地址","widget":"TEXT","required":true,"placeholder":"例如 192.168.1.10"},{"key":"TCP_PORT","label":"TCP 端口","widget":"NUMBER","required":true,"defaultValue":"502","placeholder":"默认 502"},{"key":"RESPONSE_TIMEOUT_MS","label":"响应超时(ms)","widget":"NUMBER","defaultValue":"1000"}]}}', 2, 1743465600000, 1744156800000),
    (3, 'MQTT_CLIENT', 'Mqtt', 'MQTT 客户端协议', '{"transportForm":{"title":"通信配置","subtitle":"当前 MQTT 模板暂未定义额外通信字段。","summaryKeys":[],"fields":[]}}', 3, 1743465600000, 1744156800000);

INSERT OR IGNORE INTO host_config_module_template (
    id, protocol_template_id, code, name, description, sort_index, channel_count, created_at, updated_at
) VALUES
    (1, 1, 'OKMY_RIO_DI_24', '24路DI模块', '24路数字量输入模块', 1, 24, 1743465600000, 1743465600000),
    (2, 1, 'OKMY_RIO_DO_24', '24路DO模块', '24路数字量输出模块', 2, 24, 1743465600000, 1743465600000),
    (3, 1, 'OKMY_RIO_AI_12', '12路AI模块', '12路模拟量输入模块', 3, 12, 1743465600000, 1743465600000),
    (4, 1, 'OKMY_RIO_AO_8', '8路AO模块', '8路模拟量输出模块', 4, 8, 1743465600000, 1743465600000);

INSERT OR IGNORE INTO host_config_device_type (
    id, code, name, description, sort_index, created_at, updated_at
) VALUES
    (1, 'OKM_MODULE', 'OKM模块', 'OKMY 标准模块设备', 1, 1743465600000, 1743465600000),
    (2, 'METER_ELECTRIC', '电表', '电表设备类型', 2, 1743465600000, 1743465600000),
    (3, 'METER_WATER', '水表', '水表设备类型', 3, 1743465600000, 1743465600000),
    (4, 'METER_HEAT', '热量表', '热量表设备类型', 4, 1743465600000, 1743465600000),
    (5, 'METER_COOL', '冷量表', '冷量表设备类型', 5, 1743465600000, 1743465600000);

INSERT OR IGNORE INTO host_config_register_type (
    id, code, name, description, sort_index, created_at, updated_at
) VALUES
    (1, 'COIL_STATUS_F15', '0X(Coil Status)-F15', '线圈状态寄存器', 1, 1743465600000, 1743465600000),
    (2, 'INPUT_STATUS', '1X(Input Status)', '输入状态寄存器', 2, 1743465600000, 1743465600000),
    (3, 'INPUT_REGISTER_F6', '3X(Input Register)-F6', '输入寄存器', 3, 1743465600000, 1743465600000),
    (4, 'HOLDING_REGISTER_F16', '4X(Holding Register)-F16', '保持寄存器', 4, 1743465600000, 1743465600000);

INSERT OR IGNORE INTO host_config_data_type (
    id, code, name, description, sort_index, created_at, updated_at
) VALUES
    (1, 'BOOLEAN', 'Boolean', '布尔值', 1, 1743465600000, 1743465600000),
    (2, 'WORD', 'Word', '16 位无符号整数', 2, 1743465600000, 1743465600000),
    (3, 'DWORD', 'DWord', '32 位无符号整数', 3, 1743465600000, 1743465600000),
    (4, 'LONG', 'Long', '32 位有符号整数', 4, 1743465600000, 1743465600000),
    (5, 'FLOAT', 'Float', '32 位浮点数', 5, 1743465600000, 1743465600000),
    (6, 'DOUBLE', 'Double', '64 位浮点数', 6, 1743465600000, 1743465600000),
    (7, 'STRING', 'String', '字符串', 7, 1743465600000, 1743465600000);
