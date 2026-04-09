CREATE TABLE IF NOT EXISTS mcu_device_profile (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_key TEXT NOT NULL,
    manufacturer TEXT,
    remark TEXT,
    create_time TEXT NOT NULL,
    update_time TEXT,
    UNIQUE(device_key)
);
