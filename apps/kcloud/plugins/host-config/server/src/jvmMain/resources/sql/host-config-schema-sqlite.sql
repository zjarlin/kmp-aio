CREATE TABLE IF NOT EXISTS host_config_project (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    remark TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS host_config_protocol_template (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS host_config_module_template (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    protocol_template_id INTEGER NOT NULL,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    channel_count INTEGER,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY(protocol_template_id) REFERENCES host_config_protocol_template(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS host_config_device_type (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS host_config_register_type (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS host_config_data_type (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS host_config_protocol_instance (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    protocol_template_id INTEGER NOT NULL,
    name TEXT NOT NULL UNIQUE,
    polling_interval_ms INTEGER NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY(protocol_template_id) REFERENCES host_config_protocol_template(id)
);

CREATE TABLE IF NOT EXISTS host_config_project_protocol (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER NOT NULL,
    protocol_id INTEGER NOT NULL,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(project_id, protocol_id),
    FOREIGN KEY(project_id) REFERENCES host_config_project(id) ON DELETE CASCADE,
    FOREIGN KEY(protocol_id) REFERENCES host_config_protocol_instance(id) ON DELETE CASCADE
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
    FOREIGN KEY(protocol_id) REFERENCES host_config_protocol_instance(id) ON DELETE CASCADE,
    FOREIGN KEY(module_template_id) REFERENCES host_config_module_template(id)
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
    FOREIGN KEY(module_id) REFERENCES host_config_module_instance(id) ON DELETE CASCADE,
    FOREIGN KEY(device_type_id) REFERENCES host_config_device_type(id)
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
    scaling_offset NUMERIC,
    raw_min NUMERIC,
    raw_max NUMERIC,
    eng_min NUMERIC,
    eng_max NUMERIC,
    forward_enabled INTEGER NOT NULL DEFAULT 0,
    forward_register_address INTEGER,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(device_id, name),
    UNIQUE(device_id, register_type_id, register_address),
    FOREIGN KEY(device_id) REFERENCES host_config_device(id) ON DELETE CASCADE,
    FOREIGN KEY(data_type_id) REFERENCES host_config_data_type(id),
    FOREIGN KEY(register_type_id) REFERENCES host_config_register_type(id),
    FOREIGN KEY(forward_register_type_id) REFERENCES host_config_register_type(id)
);

CREATE TABLE IF NOT EXISTS host_config_tag_value_text (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tag_id INTEGER NOT NULL,
    raw_value TEXT NOT NULL,
    display_text TEXT NOT NULL,
    sort_index INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY(tag_id) REFERENCES host_config_tag(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS host_config_project_mqtt_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER NOT NULL UNIQUE,
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
    precision_value NUMERIC,
    value_change_ratio_enabled INTEGER NOT NULL DEFAULT 0,
    cloud_control_disabled INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY(project_id) REFERENCES host_config_project(id) ON DELETE CASCADE
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
    FOREIGN KEY(project_id) REFERENCES host_config_project(id) ON DELETE CASCADE
);

INSERT OR IGNORE INTO host_config_protocol_template (
    id, code, name, description, sort_index, created_at, updated_at
) VALUES
    (1, 'MODBUS_RTU_CLIENT', 'ModbusRTUClient', 'Modbus RTU 客户端协议', 1, 1743465600000, 1743465600000),
    (2, 'MODBUS_TCP_CLIENT', 'ModbusTCPClient', 'Modbus TCP 客户端协议', 2, 1743465600000, 1743465600000),
    (3, 'MQTT_CLIENT', 'MqttClient', 'MQTT 客户端协议', 3, 1743465600000, 1743465600000);

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

INSERT OR IGNORE INTO host_config_protocol_instance (
    id, protocol_template_id, name, polling_interval_ms, created_at, updated_at
) VALUES
    (1, 1, 'ModbusRTU', 100, 1743465600000, 1743465600000),
    (2, 2, 'ModbusTCP', 100, 1743465600000, 1743465600000),
    (3, 3, 'MQTT', 100, 1743465600000, 1743465600000);
