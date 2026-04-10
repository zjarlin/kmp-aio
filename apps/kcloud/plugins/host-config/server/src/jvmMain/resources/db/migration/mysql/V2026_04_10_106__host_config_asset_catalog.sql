CREATE TABLE IF NOT EXISTS host_config_asset_node (
    id BIGINT NOT NULL AUTO_INCREMENT,
    parent_id BIGINT NULL,
    node_type VARCHAR(64) NOT NULL,
    code VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    enabled BIT NOT NULL DEFAULT b'1',
    sort_index INT NOT NULL DEFAULT 0,
    vendor VARCHAR(255) NULL,
    category VARCHAR(255) NULL,
    supports_telemetry BIT NOT NULL DEFAULT b'1',
    supports_control BIT NOT NULL DEFAULT b'0',
    protocol_template_id BIGINT NULL,
    device_type_id BIGINT NULL,
    module_template_id BIGINT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_host_config_asset_node_parent_sort (parent_id, sort_index, id),
    KEY idx_host_config_asset_node_type_parent (node_type, parent_id, sort_index, id),
    KEY idx_host_config_asset_node_protocol_template (protocol_template_id),
    KEY idx_host_config_asset_node_device_type (device_type_id),
    KEY idx_host_config_asset_node_module_template (module_template_id),
    CONSTRAINT fk_host_config_asset_node_parent
        FOREIGN KEY (parent_id) REFERENCES host_config_asset_node(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_host_config_asset_node_protocol_template
        FOREIGN KEY (protocol_template_id) REFERENCES host_config_protocol_template(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_host_config_asset_node_device_type
        FOREIGN KEY (device_type_id) REFERENCES host_config_device_type(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_host_config_asset_node_module_template
        FOREIGN KEY (module_template_id) REFERENCES host_config_module_template(id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO host_config_asset_node (
    parent_id,
    node_type,
    code,
    name,
    description,
    enabled,
    sort_index,
    vendor,
    category,
    supports_telemetry,
    supports_control,
    protocol_template_id,
    device_type_id,
    module_template_id,
    created_at,
    updated_at
)
SELECT
    NULL,
    'PRODUCT',
    pd.code,
    pd.name,
    pd.description,
    pd.enabled,
    pd.sort_index,
    pd.vendor,
    pd.category,
    b'1',
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
    ON an.node_type = 'PRODUCT'
   AND an.parent_id IS NULL
   AND an.code = pd.code;

INSERT INTO host_config_asset_node (
    parent_id,
    node_type,
    code,
    name,
    description,
    enabled,
    sort_index,
    vendor,
    category,
    supports_telemetry,
    supports_control,
    protocol_template_id,
    device_type_id,
    module_template_id,
    created_at,
    updated_at
)
SELECT
    pm.node_id,
    'DEVICE',
    dd.code,
    dd.name,
    dd.description,
    b'1',
    dd.sort_index,
    NULL,
    NULL,
    dd.supports_telemetry,
    dd.supports_control,
    NULL,
    dd.device_type_id,
    NULL,
    dd.created_at,
    dd.updated_at
FROM host_config_device_definition dd
INNER JOIN tmp_host_config_asset_product_map pm ON pm.legacy_id = dd.product_id;

CREATE TEMPORARY TABLE tmp_host_config_asset_device_map AS
SELECT
    dd.id AS legacy_id,
    an.id AS node_id
FROM host_config_device_definition dd
INNER JOIN tmp_host_config_asset_product_map pm ON pm.legacy_id = dd.product_id
INNER JOIN host_config_asset_node an
    ON an.node_type = 'DEVICE'
   AND an.parent_id = pm.node_id
   AND an.code = dd.code;

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
        FOREIGN KEY (label_id) REFERENCES host_config_label_definition(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO host_config_asset_node_label (
    asset_id,
    label_id,
    sort_index,
    created_at,
    updated_at
)
SELECT
    pm.node_id,
    pl.label_id,
    pl.sort_index,
    pl.created_at,
    pl.updated_at
FROM host_config_product_definition_label pl
INNER JOIN tmp_host_config_asset_product_map pm ON pm.legacy_id = pl.product_id;

ALTER TABLE host_config_property_definition
    MODIFY COLUMN device_definition_id BIGINT NULL,
    ADD COLUMN node_id BIGINT NULL AFTER device_definition_id;

UPDATE host_config_property_definition pd
INNER JOIN tmp_host_config_asset_device_map dm ON dm.legacy_id = pd.device_definition_id
SET pd.node_id = dm.node_id
WHERE pd.node_id IS NULL;

ALTER TABLE host_config_property_definition
    MODIFY COLUMN node_id BIGINT NOT NULL,
    ADD KEY idx_host_config_property_definition_node_sort (node_id, sort_index, id),
    ADD UNIQUE KEY uk_host_config_property_definition_node_identifier (node_id, identifier),
    ADD CONSTRAINT fk_host_config_property_definition_node
        FOREIGN KEY (node_id) REFERENCES host_config_asset_node(id)
        ON DELETE CASCADE;

ALTER TABLE host_config_feature_definition
    MODIFY COLUMN device_definition_id BIGINT NULL,
    ADD COLUMN node_id BIGINT NULL AFTER device_definition_id;

UPDATE host_config_feature_definition fd
INNER JOIN tmp_host_config_asset_device_map dm ON dm.legacy_id = fd.device_definition_id
SET fd.node_id = dm.node_id
WHERE fd.node_id IS NULL;

ALTER TABLE host_config_feature_definition
    MODIFY COLUMN node_id BIGINT NOT NULL,
    ADD KEY idx_host_config_feature_definition_node_sort (node_id, sort_index, id),
    ADD UNIQUE KEY uk_host_config_feature_definition_node_identifier (node_id, identifier),
    ADD CONSTRAINT fk_host_config_feature_definition_node
        FOREIGN KEY (node_id) REFERENCES host_config_asset_node(id)
        ON DELETE CASCADE;

DROP TEMPORARY TABLE tmp_host_config_asset_device_map;
DROP TEMPORARY TABLE tmp_host_config_asset_product_map;
