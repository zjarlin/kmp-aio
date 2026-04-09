CREATE TABLE IF NOT EXISTS codegen_context_class (
    id BIGINT NOT NULL AUTO_INCREMENT,
    context_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sort_index INT NOT NULL DEFAULT 0,
    class_kind VARCHAR(32) NOT NULL,
    class_name VARCHAR(255) NOT NULL,
    package_name VARCHAR(255),
    create_time DATETIME NOT NULL,
    update_time DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_codegen_context_class_context_name (context_id, class_name),
    CONSTRAINT fk_codegen_context_class_context
        FOREIGN KEY (context_id) REFERENCES codegen_context_context(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS codegen_context_method (
    id BIGINT NOT NULL AUTO_INCREMENT,
    owner_class_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sort_index INT NOT NULL DEFAULT 0,
    method_name VARCHAR(255) NOT NULL,
    request_class_name VARCHAR(255),
    response_class_name VARCHAR(255),
    create_time DATETIME NOT NULL,
    update_time DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_codegen_context_method_owner_name (owner_class_id, method_name),
    CONSTRAINT fk_codegen_context_method_owner_class
        FOREIGN KEY (owner_class_id) REFERENCES codegen_context_class(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS codegen_context_property (
    id BIGINT NOT NULL AUTO_INCREMENT,
    owner_class_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sort_index INT NOT NULL DEFAULT 0,
    property_name VARCHAR(255) NOT NULL,
    type_name VARCHAR(255) NOT NULL,
    nullable TINYINT(1) NOT NULL DEFAULT 0,
    default_literal TEXT,
    create_time DATETIME NOT NULL,
    update_time DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_codegen_context_property_owner_name (owner_class_id, property_name),
    CONSTRAINT fk_codegen_context_property_owner_class
        FOREIGN KEY (owner_class_id) REFERENCES codegen_context_class(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS codegen_context_definition (
    id BIGINT NOT NULL AUTO_INCREMENT,
    protocol_template_id BIGINT NOT NULL,
    code VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sort_index INT NOT NULL DEFAULT 0,
    target_kind VARCHAR(32) NOT NULL,
    binding_target_mode VARCHAR(32) NOT NULL,
    source_kind VARCHAR(32) NOT NULL,
    create_time DATETIME NOT NULL,
    update_time DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_codegen_context_definition_protocol_code (protocol_template_id, code),
    CONSTRAINT fk_codegen_context_definition_protocol_template
        FOREIGN KEY (protocol_template_id) REFERENCES host_config_protocol_template(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS codegen_context_param_definition (
    id BIGINT NOT NULL AUTO_INCREMENT,
    definition_id BIGINT NOT NULL,
    code VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sort_index INT NOT NULL DEFAULT 0,
    value_type VARCHAR(32) NOT NULL,
    required TINYINT(1) NOT NULL DEFAULT 0,
    default_value TEXT,
    enum_options LONGTEXT,
    placeholder TEXT,
    create_time DATETIME NOT NULL,
    update_time DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_codegen_context_param_definition_code (definition_id, code),
    CONSTRAINT fk_codegen_context_param_definition_definition
        FOREIGN KEY (definition_id) REFERENCES codegen_context_definition(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS codegen_context_binding (
    id BIGINT NOT NULL AUTO_INCREMENT,
    definition_id BIGINT NOT NULL,
    owner_class_id BIGINT NULL,
    owner_method_id BIGINT NULL,
    owner_property_id BIGINT NULL,
    sort_index INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL,
    update_time DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_codegen_context_binding_owner_class (owner_class_id),
    KEY idx_codegen_context_binding_owner_method (owner_method_id),
    KEY idx_codegen_context_binding_owner_property (owner_property_id),
    CONSTRAINT fk_codegen_context_binding_definition
        FOREIGN KEY (definition_id) REFERENCES codegen_context_definition(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_codegen_context_binding_owner_class
        FOREIGN KEY (owner_class_id) REFERENCES codegen_context_class(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_codegen_context_binding_owner_method
        FOREIGN KEY (owner_method_id) REFERENCES codegen_context_method(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_codegen_context_binding_owner_property
        FOREIGN KEY (owner_property_id) REFERENCES codegen_context_property(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS codegen_context_binding_value (
    id BIGINT NOT NULL AUTO_INCREMENT,
    binding_id BIGINT NOT NULL,
    param_definition_id BIGINT NOT NULL,
    value TEXT,
    create_time DATETIME NOT NULL,
    update_time DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_codegen_context_binding_value_param (binding_id, param_definition_id),
    CONSTRAINT fk_codegen_context_binding_value_binding
        FOREIGN KEY (binding_id) REFERENCES codegen_context_binding(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_codegen_context_binding_value_param_definition
        FOREIGN KEY (param_definition_id) REFERENCES codegen_context_param_definition(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
