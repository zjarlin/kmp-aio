CREATE TABLE IF NOT EXISTS mcu_device_profile (
    id BIGINT NOT NULL AUTO_INCREMENT,
    device_key VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NULL,
    remark TEXT NULL,
    create_time BIGINT NOT NULL,
    update_time BIGINT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_mcu_device_profile_device_key (device_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
