CREATE TABLE IF NOT EXISTS mcu_device_profile (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_key TEXT NOT NULL,
    manufacturer TEXT,
    remark TEXT,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    UNIQUE(device_key)
);
