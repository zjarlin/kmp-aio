SET @ddl = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'host_config_protocol_instance'
          AND column_name = 'transport_type'
    ),
    'SELECT 1',
    'ALTER TABLE host_config_protocol_instance ADD COLUMN transport_type VARCHAR(64) NULL'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'host_config_protocol_instance'
          AND column_name = 'host'
    ),
    'SELECT 1',
    'ALTER TABLE host_config_protocol_instance ADD COLUMN host VARCHAR(255) NULL'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'host_config_protocol_instance'
          AND column_name = 'tcp_port'
    ),
    'SELECT 1',
    'ALTER TABLE host_config_protocol_instance ADD COLUMN tcp_port INT NULL'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'host_config_protocol_instance'
          AND column_name = 'port_name'
    ),
    'SELECT 1',
    'ALTER TABLE host_config_protocol_instance ADD COLUMN port_name VARCHAR(255) NULL'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'host_config_protocol_instance'
          AND column_name = 'baud_rate'
    ),
    'SELECT 1',
    'ALTER TABLE host_config_protocol_instance ADD COLUMN baud_rate INT NULL'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'host_config_protocol_instance'
          AND column_name = 'data_bits'
    ),
    'SELECT 1',
    'ALTER TABLE host_config_protocol_instance ADD COLUMN data_bits INT NULL'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'host_config_protocol_instance'
          AND column_name = 'stop_bits'
    ),
    'SELECT 1',
    'ALTER TABLE host_config_protocol_instance ADD COLUMN stop_bits INT NULL'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'host_config_protocol_instance'
          AND column_name = 'parity'
    ),
    'SELECT 1',
    'ALTER TABLE host_config_protocol_instance ADD COLUMN parity VARCHAR(64) NULL'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'host_config_protocol_instance'
          AND column_name = 'response_timeout_ms'
    ),
    'SELECT 1',
    'ALTER TABLE host_config_protocol_instance ADD COLUMN response_timeout_ms INT NULL'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
