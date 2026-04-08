CREATE TABLE IF NOT EXISTS host_config_project (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    remark TEXT,
    sort_index INT NOT NULL DEFAULT 0,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_host_config_project_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS host_config_protocol_template (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sort_index INT NOT NULL DEFAULT 0,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_host_config_protocol_template_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS host_config_module_template (
    id BIGINT NOT NULL AUTO_INCREMENT,
    protocol_template_id BIGINT NOT NULL,
    code VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sort_index INT NOT NULL DEFAULT 0,
    channel_count INT,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_host_config_module_template_code (code),
    CONSTRAINT fk_host_config_module_template_protocol_template
        FOREIGN KEY (protocol_template_id) REFERENCES host_config_protocol_template(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS host_config_device_type (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sort_index INT NOT NULL DEFAULT 0,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_host_config_device_type_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS host_config_register_type (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sort_index INT NOT NULL DEFAULT 0,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_host_config_register_type_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS host_config_data_type (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sort_index INT NOT NULL DEFAULT 0,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_host_config_data_type_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS host_config_protocol_instance (
    id BIGINT NOT NULL AUTO_INCREMENT,
    protocol_template_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    polling_interval_ms INT NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_host_config_protocol_instance_name (name),
    CONSTRAINT fk_host_config_protocol_instance_template
        FOREIGN KEY (protocol_template_id) REFERENCES host_config_protocol_template(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS host_config_project_protocol (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    protocol_id BIGINT NOT NULL,
    sort_index INT NOT NULL DEFAULT 0,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_host_config_project_protocol_project_protocol (project_id, protocol_id),
    CONSTRAINT fk_host_config_project_protocol_project
        FOREIGN KEY (project_id) REFERENCES host_config_project(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_host_config_project_protocol_protocol
        FOREIGN KEY (protocol_id) REFERENCES host_config_protocol_instance(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS host_config_module_instance (
    id BIGINT NOT NULL AUTO_INCREMENT,
    protocol_id BIGINT NOT NULL,
    module_template_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    port_name VARCHAR(255),
    baud_rate INT,
    data_bits INT,
    stop_bits INT,
    parity VARCHAR(64),
    response_timeout_ms INT,
    sort_index INT NOT NULL DEFAULT 0,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_host_config_module_instance_protocol_name (protocol_id, name),
    CONSTRAINT fk_host_config_module_instance_protocol
        FOREIGN KEY (protocol_id) REFERENCES host_config_protocol_instance(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_host_config_module_instance_template
        FOREIGN KEY (module_template_id) REFERENCES host_config_module_template(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS host_config_device (
    id BIGINT NOT NULL AUTO_INCREMENT,
    module_id BIGINT NOT NULL,
    device_type_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    station_no INT NOT NULL,
    request_interval_ms INT,
    write_interval_ms INT,
    byte_order2 VARCHAR(64),
    byte_order4 VARCHAR(64),
    float_order VARCHAR(64),
    batch_analog_start INT,
    batch_analog_length INT,
    batch_digital_start INT,
    batch_digital_length INT,
    disabled TINYINT(1) NOT NULL DEFAULT 0,
    sort_index INT NOT NULL DEFAULT 0,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_host_config_device_module_name (module_id, name),
    CONSTRAINT fk_host_config_device_module
        FOREIGN KEY (module_id) REFERENCES host_config_module_instance(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_host_config_device_type
        FOREIGN KEY (device_type_id) REFERENCES host_config_device_type(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS host_config_tag (
    id BIGINT NOT NULL AUTO_INCREMENT,
    device_id BIGINT NOT NULL,
    data_type_id BIGINT NOT NULL,
    register_type_id BIGINT NOT NULL,
    forward_register_type_id BIGINT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    register_address INT NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    default_value TEXT,
    exception_value TEXT,
    point_type VARCHAR(128),
    debounce_ms INT,
    sort_index INT NOT NULL DEFAULT 0,
    scaling_enabled TINYINT(1) NOT NULL DEFAULT 0,
    scaling_offset DECIMAL(19, 6),
    raw_min DECIMAL(19, 6),
    raw_max DECIMAL(19, 6),
    eng_min DECIMAL(19, 6),
    eng_max DECIMAL(19, 6),
    forward_enabled TINYINT(1) NOT NULL DEFAULT 0,
    forward_register_address INT,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_host_config_tag_device_name (device_id, name),
    UNIQUE KEY uk_host_config_tag_device_register (device_id, register_type_id, register_address),
    CONSTRAINT fk_host_config_tag_device
        FOREIGN KEY (device_id) REFERENCES host_config_device(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_host_config_tag_data_type
        FOREIGN KEY (data_type_id) REFERENCES host_config_data_type(id),
    CONSTRAINT fk_host_config_tag_register_type
        FOREIGN KEY (register_type_id) REFERENCES host_config_register_type(id),
    CONSTRAINT fk_host_config_tag_forward_register_type
        FOREIGN KEY (forward_register_type_id) REFERENCES host_config_register_type(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS host_config_tag_value_text (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tag_id BIGINT NOT NULL,
    raw_value TEXT NOT NULL,
    display_text TEXT NOT NULL,
    sort_index INT NOT NULL DEFAULT 0,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_host_config_tag_value_text_tag
        FOREIGN KEY (tag_id) REFERENCES host_config_tag(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS host_config_project_mqtt_config (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 0,
    breakpoint_resume TINYINT(1) NOT NULL DEFAULT 0,
    gateway_name VARCHAR(255),
    vendor VARCHAR(255),
    host VARCHAR(255),
    port INT,
    topic VARCHAR(255),
    gateway_id VARCHAR(255),
    auth_enabled TINYINT(1) NOT NULL DEFAULT 0,
    username VARCHAR(255),
    password_encrypted TEXT,
    tls_enabled TINYINT(1) NOT NULL DEFAULT 0,
    cert_file_ref VARCHAR(255),
    client_id VARCHAR(255),
    keep_alive_sec INT,
    qos INT,
    report_period_sec INT,
    precision_value DECIMAL(19, 6),
    value_change_ratio_enabled TINYINT(1) NOT NULL DEFAULT 0,
    cloud_control_disabled TINYINT(1) NOT NULL DEFAULT 0,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_host_config_project_mqtt_config_project (project_id),
    CONSTRAINT fk_host_config_project_mqtt_config_project
        FOREIGN KEY (project_id) REFERENCES host_config_project(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS host_config_project_modbus_server_config (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    transport_type VARCHAR(64) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 0,
    tcp_port INT,
    port_name VARCHAR(255),
    baud_rate INT,
    data_bits INT,
    stop_bits INT,
    parity VARCHAR(64),
    station_no INT,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_host_config_project_modbus_server_config_project_transport (project_id, transport_type),
    CONSTRAINT fk_host_config_project_modbus_server_config_project
        FOREIGN KEY (project_id) REFERENCES host_config_project(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS host_config_project_gateway_pin_config (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    fault_indicator_pin VARCHAR(64) NOT NULL DEFAULT 'PA8',
    running_indicator_pin VARCHAR(64) NOT NULL DEFAULT 'PA2',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_host_config_project_gateway_pin_config_project (project_id),
    CONSTRAINT fk_host_config_project_gateway_pin_config_project
        FOREIGN KEY (project_id) REFERENCES host_config_project(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO host_config_protocol_template (
    id, code, name, description, sort_index, created_at, updated_at
) VALUES
    (1, 'MODBUS_RTU_CLIENT', 'ModbusRTUClient', 'Modbus RTU 客户端协议', 1, 1743465600000, 1743465600000),
    (2, 'MODBUS_TCP_CLIENT', 'ModbusTCPClient', 'Modbus TCP 客户端协议', 2, 1743465600000, 1743465600000),
    (3, 'MQTT_CLIENT', 'MqttClient', 'MQTT 客户端协议', 3, 1743465600000, 1743465600000);

INSERT IGNORE INTO host_config_module_template (
    id, protocol_template_id, code, name, description, sort_index, channel_count, created_at, updated_at
) VALUES
    (1, 1, 'OKMY_RIO_DI_24', '24路DI模块', '24路数字量输入模块', 1, 24, 1743465600000, 1743465600000),
    (2, 1, 'OKMY_RIO_DO_24', '24路DO模块', '24路数字量输出模块', 2, 24, 1743465600000, 1743465600000),
    (3, 1, 'OKMY_RIO_AI_12', '12路AI模块', '12路模拟量输入模块', 3, 12, 1743465600000, 1743465600000),
    (4, 1, 'OKMY_RIO_AO_8', '8路AO模块', '8路模拟量输出模块', 4, 8, 1743465600000, 1743465600000);

INSERT IGNORE INTO host_config_device_type (
    id, code, name, description, sort_index, created_at, updated_at
) VALUES
    (1, 'OKM_MODULE', 'OKM模块', 'OKMY 标准模块设备', 1, 1743465600000, 1743465600000),
    (2, 'METER_ELECTRIC', '电表', '电表设备类型', 2, 1743465600000, 1743465600000),
    (3, 'METER_WATER', '水表', '水表设备类型', 3, 1743465600000, 1743465600000),
    (4, 'METER_HEAT', '热量表', '热量表设备类型', 4, 1743465600000, 1743465600000),
    (5, 'METER_COOL', '冷量表', '冷量表设备类型', 5, 1743465600000, 1743465600000);

INSERT IGNORE INTO host_config_register_type (
    id, code, name, description, sort_index, created_at, updated_at
) VALUES
    (1, 'COIL_STATUS_F15', '0X(Coil Status)-F15', '线圈状态寄存器', 1, 1743465600000, 1743465600000),
    (2, 'INPUT_STATUS', '1X(Input Status)', '输入状态寄存器', 2, 1743465600000, 1743465600000),
    (3, 'INPUT_REGISTER_F6', '3X(Input Register)-F6', '输入寄存器', 3, 1743465600000, 1743465600000),
    (4, 'HOLDING_REGISTER_F16', '4X(Holding Register)-F16', '保持寄存器', 4, 1743465600000, 1743465600000);

INSERT IGNORE INTO host_config_data_type (
    id, code, name, description, sort_index, created_at, updated_at
) VALUES
    (1, 'BOOLEAN', 'Boolean', '布尔值', 1, 1743465600000, 1743465600000),
    (2, 'WORD', 'Word', '16 位无符号整数', 2, 1743465600000, 1743465600000),
    (3, 'DWORD', 'DWord', '32 位无符号整数', 3, 1743465600000, 1743465600000),
    (4, 'LONG', 'Long', '32 位有符号整数', 4, 1743465600000, 1743465600000),
    (5, 'FLOAT', 'Float', '32 位浮点数', 5, 1743465600000, 1743465600000),
    (6, 'DOUBLE', 'Double', '64 位浮点数', 6, 1743465600000, 1743465600000),
    (7, 'STRING', 'String', '字符串', 7, 1743465600000, 1743465600000);
