CREATE TABLE IF NOT EXISTS host_config_asset_node (
    id BIGINT NOT NULL AUTO_INCREMENT,
    parent_id BIGINT NULL,
    inherit_from_id BIGINT NULL,
    node_type VARCHAR(64) NOT NULL,
    code VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    enabled BIT NOT NULL DEFAULT b'1',
    sort_index INT NOT NULL DEFAULT 0,
    vendor VARCHAR(255) NULL,
    category VARCHAR(255) NULL,
    color_hex VARCHAR(32) NULL,
    identifier VARCHAR(255) NULL,
    unit VARCHAR(255) NULL,
    required BIT NOT NULL DEFAULT b'0',
    writable BIT NOT NULL DEFAULT b'0',
    telemetry BIT NOT NULL DEFAULT b'1',
    nullable BIT NOT NULL DEFAULT b'1',
    length INT NULL,
    supports_telemetry BIT NOT NULL DEFAULT b'1',
    supports_control BIT NOT NULL DEFAULT b'0',
    attributes_json TEXT NULL,
    input_schema TEXT NULL,
    output_schema TEXT NULL,
    asynchronous BIT NOT NULL DEFAULT b'0',
    protocol_template_id BIGINT NULL,
    device_type_id BIGINT NULL,
    data_type_id BIGINT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_host_config_asset_node_parent_sort (parent_id, sort_index, id),
    KEY idx_host_config_asset_node_type_parent (node_type, parent_id, sort_index, id),
    KEY idx_host_config_asset_node_protocol_template (protocol_template_id),
    KEY idx_host_config_asset_node_device_type (device_type_id),
    KEY idx_host_config_asset_node_data_type (data_type_id),
    CONSTRAINT fk_host_config_asset_node_parent
        FOREIGN KEY (parent_id) REFERENCES host_config_asset_node(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_host_config_asset_node_inherit_from
        FOREIGN KEY (inherit_from_id) REFERENCES host_config_asset_node(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_host_config_asset_node_protocol_template
        FOREIGN KEY (protocol_template_id) REFERENCES host_config_protocol_template(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_host_config_asset_node_device_type
        FOREIGN KEY (device_type_id) REFERENCES host_config_device_type(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_host_config_asset_node_data_type
        FOREIGN KEY (data_type_id) REFERENCES host_config_data_type(id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS host_config_asset_node_label (
    id BIGINT NOT NULL AUTO_INCREMENT,
    asset_id BIGINT NOT NULL,
    label_id BIGINT NOT NULL,
    sort_index INT NOT NULL DEFAULT 0,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_host_config_asset_node_label_asset_label (asset_id, label_id),
    KEY idx_host_config_asset_node_label_label (label_id, sort_index, id),
    CONSTRAINT fk_host_config_asset_node_label_asset
        FOREIGN KEY (asset_id) REFERENCES host_config_asset_node(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_host_config_asset_node_label_label
        FOREIGN KEY (label_id) REFERENCES host_config_asset_node(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO host_config_asset_node (
    parent_id,
    inherit_from_id,
    node_type,
    code,
    name,
    description,
    enabled,
    sort_index,
    vendor,
    category,
    color_hex,
    identifier,
    unit,
    required,
    writable,
    telemetry,
    nullable,
    length,
    supports_telemetry,
    supports_control,
    attributes_json,
    input_schema,
    output_schema,
    asynchronous,
    protocol_template_id,
    device_type_id,
    data_type_id,
    created_at,
    updated_at
)
SELECT
    NULL,
    NULL,
    'LABEL',
    ld.code,
    ld.name,
    ld.description,
    b'1',
    ld.sort_index,
    NULL,
    NULL,
    ld.color_hex,
    NULL,
    NULL,
    b'0',
    b'0',
    b'0',
    b'1',
    NULL,
    b'1',
    b'0',
    NULL,
    NULL,
    NULL,
    b'0',
    NULL,
    NULL,
    NULL,
    ld.created_at,
    ld.updated_at
FROM host_config_label_definition ld;

CREATE TEMPORARY TABLE tmp_host_config_asset_label_map AS
SELECT
    ld.id AS legacy_id,
    an.id AS node_id
FROM host_config_label_definition ld
INNER JOIN host_config_asset_node an
    ON an.node_type = 'LABEL'
   AND an.code = ld.code
   AND an.parent_id IS NULL;

INSERT INTO host_config_asset_node (
    parent_id,
    inherit_from_id,
    node_type,
    code,
    name,
    description,
    enabled,
    sort_index,
    vendor,
    category,
    color_hex,
    identifier,
    unit,
    required,
    writable,
    telemetry,
    nullable,
    length,
    supports_telemetry,
    supports_control,
    attributes_json,
    input_schema,
    output_schema,
    asynchronous,
    protocol_template_id,
    device_type_id,
    data_type_id,
    created_at,
    updated_at
)
SELECT
    NULL,
    NULL,
    'ASSET',
    pd.code,
    pd.name,
    pd.description,
    pd.enabled,
    pd.sort_index,
    pd.vendor,
    pd.category,
    NULL,
    NULL,
    NULL,
    b'0',
    b'0',
    b'0',
    b'1',
    NULL,
    b'1',
    b'0',
    NULL,
    NULL,
    NULL,
    b'0',
    NULL,
    NULL,
    NULL,
    pd.created_at,
    pd.updated_at
FROM host_config_product_definition pd;

CREATE TEMPORARY TABLE tmp_host_config_asset_product_map AS
SELECT
    pd.id AS legacy_id,
    an.id AS node_id
FROM host_config_product_definition pd
INNER JOIN host_config_asset_node an
    ON an.node_type = 'ASSET'
   AND an.code = pd.code
   AND an.parent_id IS NULL;

INSERT INTO host_config_asset_node (
    parent_id,
    inherit_from_id,
    node_type,
    code,
    name,
    description,
    enabled,
    sort_index,
    vendor,
    category,
    color_hex,
    identifier,
    unit,
    required,
    writable,
    telemetry,
    nullable,
    length,
    supports_telemetry,
    supports_control,
    attributes_json,
    input_schema,
    output_schema,
    asynchronous,
    protocol_template_id,
    device_type_id,
    data_type_id,
    created_at,
    updated_at
)
SELECT
    pm.node_id,
    NULL,
    'ASSET',
    dd.code,
    dd.name,
    dd.description,
    b'1',
    dd.sort_index,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    b'0',
    b'0',
    b'0',
    b'1',
    NULL,
    dd.supports_telemetry,
    dd.supports_control,
    NULL,
    NULL,
    NULL,
    b'0',
    NULL,
    dd.device_type_id,
    NULL,
    dd.created_at,
    dd.updated_at
FROM host_config_device_definition dd
INNER JOIN tmp_host_config_asset_product_map pm
    ON pm.legacy_id = dd.product_id;

CREATE TEMPORARY TABLE tmp_host_config_asset_device_map AS
SELECT
    dd.id AS legacy_id,
    an.id AS node_id
FROM host_config_device_definition dd
INNER JOIN tmp_host_config_asset_product_map pm
    ON pm.legacy_id = dd.product_id
INNER JOIN host_config_asset_node an
    ON an.node_type = 'ASSET'
   AND an.parent_id = pm.node_id
   AND an.code = dd.code;

INSERT INTO host_config_asset_node (
    parent_id,
    inherit_from_id,
    node_type,
    code,
    name,
    description,
    enabled,
    sort_index,
    vendor,
    category,
    color_hex,
    identifier,
    unit,
    required,
    writable,
    telemetry,
    nullable,
    length,
    supports_telemetry,
    supports_control,
    attributes_json,
    input_schema,
    output_schema,
    asynchronous,
    protocol_template_id,
    device_type_id,
    data_type_id,
    created_at,
    updated_at
)
SELECT
    dm.node_id,
    NULL,
    'PROPERTY',
    pd.identifier,
    pd.name,
    pd.description,
    b'1',
    pd.sort_index,
    NULL,
    NULL,
    NULL,
    pd.identifier,
    pd.unit,
    pd.required,
    pd.writable,
    pd.telemetry,
    pd.nullable,
    pd.length,
    b'1',
    b'0',
    pd.attributes_json,
    NULL,
    NULL,
    b'0',
    NULL,
    NULL,
    pd.data_type_id,
    pd.created_at,
    pd.updated_at
FROM host_config_property_definition pd
INNER JOIN tmp_host_config_asset_device_map dm
    ON dm.legacy_id = pd.device_definition_id;

INSERT INTO host_config_asset_node (
    parent_id,
    inherit_from_id,
    node_type,
    code,
    name,
    description,
    enabled,
    sort_index,
    vendor,
    category,
    color_hex,
    identifier,
    unit,
    required,
    writable,
    telemetry,
    nullable,
    length,
    supports_telemetry,
    supports_control,
    attributes_json,
    input_schema,
    output_schema,
    asynchronous,
    protocol_template_id,
    device_type_id,
    data_type_id,
    created_at,
    updated_at
)
SELECT
    dm.node_id,
    NULL,
    'SERVICE',
    fd.identifier,
    fd.name,
    fd.description,
    b'1',
    fd.sort_index,
    NULL,
    NULL,
    NULL,
    fd.identifier,
    NULL,
    b'0',
    b'0',
    b'0',
    b'1',
    NULL,
    b'1',
    b'0',
    NULL,
    fd.input_schema,
    fd.output_schema,
    fd.asynchronous,
    NULL,
    NULL,
    NULL,
    fd.created_at,
    fd.updated_at
FROM host_config_feature_definition fd
INNER JOIN tmp_host_config_asset_device_map dm
    ON dm.legacy_id = fd.device_definition_id;

INSERT INTO host_config_asset_node_label (
    asset_id,
    label_id,
    sort_index,
    created_at,
    updated_at
)
SELECT
    pm.node_id,
    lm.node_id,
    pl.sort_index,
    pl.created_at,
    pl.updated_at
FROM host_config_product_definition_label pl
INNER JOIN tmp_host_config_asset_product_map pm
    ON pm.legacy_id = pl.product_id
INNER JOIN tmp_host_config_asset_label_map lm
    ON lm.legacy_id = pl.label_id;

DROP TEMPORARY TABLE tmp_host_config_asset_label_map;
DROP TEMPORARY TABLE tmp_host_config_asset_product_map;
DROP TEMPORARY TABLE tmp_host_config_asset_device_map;
