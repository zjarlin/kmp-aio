ALTER TABLE host_config_device
    ADD COLUMN protocol_id BIGINT NULL AFTER module_id;

ALTER TABLE host_config_module_instance
    ADD COLUMN device_id BIGINT NULL AFTER protocol_id;

UPDATE host_config_device d
INNER JOIN host_config_module_instance mi ON mi.id = d.module_id
SET d.protocol_id = mi.protocol_id
WHERE d.protocol_id IS NULL;

ALTER TABLE host_config_module_instance
    DROP INDEX uk_host_config_module_instance_protocol_name;

CREATE TEMPORARY TABLE tmp_host_config_module_primary_device AS
SELECT
    d.module_id,
    MIN(d.id) AS primary_device_id
FROM host_config_device d
GROUP BY d.module_id;

UPDATE host_config_module_instance mi
INNER JOIN tmp_host_config_module_primary_device pd ON pd.module_id = mi.id
SET mi.device_id = pd.primary_device_id
WHERE mi.device_id IS NULL;

INSERT INTO host_config_module_instance (
    protocol_id,
    device_id,
    module_template_id,
    name,
    port_name,
    baud_rate,
    data_bits,
    stop_bits,
    parity,
    response_timeout_ms,
    sort_index,
    created_at,
    updated_at
)
SELECT
    mi.protocol_id,
    d.id,
    mi.module_template_id,
    mi.name,
    mi.port_name,
    mi.baud_rate,
    mi.data_bits,
    mi.stop_bits,
    mi.parity,
    mi.response_timeout_ms,
    mi.sort_index,
    mi.created_at,
    mi.updated_at
FROM host_config_device d
INNER JOIN host_config_module_instance mi ON mi.id = d.module_id
INNER JOIN tmp_host_config_module_primary_device pd ON pd.module_id = mi.id
WHERE d.id <> pd.primary_device_id;

SET @host_config_default_device_type_id = (
    SELECT MIN(id)
    FROM host_config_device_type
);

INSERT INTO host_config_device (
    module_id,
    protocol_id,
    device_type_id,
    name,
    station_no,
    request_interval_ms,
    write_interval_ms,
    byte_order2,
    byte_order4,
    float_order,
    batch_analog_start,
    batch_analog_length,
    batch_digital_start,
    batch_digital_length,
    disabled,
    sort_index,
    created_at,
    updated_at
)
SELECT
    mi.id,
    mi.protocol_id,
    COALESCE(@host_config_default_device_type_id, 1),
    CONCAT(mi.name, '-auto-', mi.id),
    1,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    0,
    mi.sort_index,
    mi.created_at,
    mi.updated_at
FROM host_config_module_instance mi
LEFT JOIN tmp_host_config_module_primary_device pd ON pd.module_id = mi.id
WHERE pd.module_id IS NULL;

UPDATE host_config_module_instance mi
INNER JOIN host_config_device d ON d.module_id = mi.id
LEFT JOIN tmp_host_config_module_primary_device pd ON pd.module_id = mi.id
SET mi.device_id = d.id
WHERE pd.module_id IS NULL
  AND mi.device_id IS NULL;

CREATE TEMPORARY TABLE tmp_host_config_device_name_fix AS
SELECT
    x.id,
    CASE
        WHEN x.name_rank = 1 THEN x.name
        ELSE CONCAT(x.name, '-', x.id)
    END AS normalized_name
FROM (
    SELECT
        d.id,
        d.name,
        ROW_NUMBER() OVER (PARTITION BY d.protocol_id, d.name ORDER BY d.id) AS name_rank
    FROM host_config_device d
) x;

UPDATE host_config_device d
INNER JOIN tmp_host_config_device_name_fix f ON f.id = d.id
SET d.name = f.normalized_name
WHERE d.name <> f.normalized_name;

DROP TEMPORARY TABLE tmp_host_config_device_name_fix;
DROP TEMPORARY TABLE tmp_host_config_module_primary_device;

ALTER TABLE host_config_device
    MODIFY COLUMN protocol_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_host_config_device_protocol
        FOREIGN KEY (protocol_id) REFERENCES host_config_protocol_instance(id)
        ON DELETE CASCADE,
    ADD UNIQUE KEY uk_host_config_device_protocol_name (protocol_id, name);

ALTER TABLE host_config_module_instance
    MODIFY COLUMN device_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_host_config_module_instance_device
        FOREIGN KEY (device_id) REFERENCES host_config_device(id)
        ON DELETE CASCADE,
    ADD UNIQUE KEY uk_host_config_module_instance_device_name (device_id, name);

ALTER TABLE host_config_device
    DROP FOREIGN KEY fk_host_config_device_module;

ALTER TABLE host_config_device
    DROP INDEX uk_host_config_device_module_name;

ALTER TABLE host_config_device
    DROP COLUMN module_id;
