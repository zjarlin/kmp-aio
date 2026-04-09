UPDATE host_config_protocol_template
SET
    name = CASE code
        WHEN 'MODBUS_RTU_CLIENT' THEN 'ModbusRTU'
        WHEN 'MODBUS_TCP_CLIENT' THEN 'ModbusTCP'
        WHEN 'MQTT_CLIENT' THEN 'Mqtt'
        ELSE name
    END,
    updated_at = 1744156800000
WHERE code IN ('MODBUS_RTU_CLIENT', 'MODBUS_TCP_CLIENT', 'MQTT_CLIENT');
