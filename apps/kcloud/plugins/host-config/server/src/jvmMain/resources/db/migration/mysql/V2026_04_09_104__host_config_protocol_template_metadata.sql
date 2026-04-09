SET @ddl_add_protocol_template_metadata_column = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'host_config_protocol_template'
              AND column_name = 'metadata_json'
        ),
        'SELECT 1',
        'ALTER TABLE host_config_protocol_template ADD COLUMN metadata_json LONGTEXT NULL'
    )
);
PREPARE stmt_add_protocol_template_metadata_column FROM @ddl_add_protocol_template_metadata_column;
EXECUTE stmt_add_protocol_template_metadata_column;
DEALLOCATE PREPARE stmt_add_protocol_template_metadata_column;

UPDATE host_config_protocol_template
SET metadata_json = CASE code
    WHEN 'MODBUS_RTU_CLIENT' THEN '{"transportType":"RTU","transportForm":{"title":"通信配置","subtitle":"协议模板元数据决定字段集合，新增 RTU 类协议时不再改界面分支。","summaryKeys":["PORT_NAME","BAUD_RATE","PARITY"],"fields":[{"key":"PORT_NAME","label":"串口","widget":"TEXT","required":true,"placeholder":"例如 COM4"},{"key":"BAUD_RATE","label":"波特率","widget":"SELECT","required":true,"helperText":"300-115200bps 可选","defaultValue":"9600","options":[{"value":"300","label":"300"},{"value":"600","label":"600"},{"value":"1200","label":"1200"},{"value":"2400","label":"2400"},{"value":"4800","label":"4800"},{"value":"9600","label":"9600"},{"value":"19200","label":"19200"},{"value":"38400","label":"38400"},{"value":"57600","label":"57600"},{"value":"115200","label":"115200"}]},{"key":"DATA_BITS","label":"数据位","widget":"SELECT","required":true,"helperText":"7 位、8 位可选","defaultValue":"8","options":[{"value":"7","label":"7"},{"value":"8","label":"8"}]},{"key":"STOP_BITS","label":"停止位","widget":"SELECT","required":true,"helperText":"1 位、2 位可选","defaultValue":"1","options":[{"value":"1","label":"1"},{"value":"2","label":"2"}]},{"key":"PARITY","label":"校验位","widget":"SELECT","required":true,"defaultValue":"NONE","options":[{"value":"NONE","label":"NONE"},{"value":"ODD","label":"ODD"},{"value":"EVEN","label":"EVEN"}]},{"key":"RESPONSE_TIMEOUT_MS","label":"响应超时(ms)","widget":"NUMBER","helperText":"按协议模板元数据控制默认值和提示文案。","defaultValue":"1000"}]}}'
    WHEN 'MODBUS_TCP_CLIENT' THEN '{"transportType":"TCP","transportForm":{"title":"通信配置","subtitle":"协议模板元数据决定字段集合，新增 TCP 类协议时只补模板元数据。","summaryKeys":["HOST","TCP_PORT"],"fields":[{"key":"HOST","label":"主机地址","widget":"TEXT","required":true,"placeholder":"例如 192.168.1.10"},{"key":"TCP_PORT","label":"TCP 端口","widget":"NUMBER","required":true,"defaultValue":"502","placeholder":"默认 502"},{"key":"RESPONSE_TIMEOUT_MS","label":"响应超时(ms)","widget":"NUMBER","defaultValue":"1000"}]}}'
    WHEN 'MQTT_CLIENT' THEN '{"transportForm":{"title":"通信配置","subtitle":"当前 MQTT 模板暂未定义额外通信字段。","summaryKeys":[],"fields":[]}}'
    ELSE metadata_json
END
WHERE code IN ('MODBUS_RTU_CLIENT', 'MODBUS_TCP_CLIENT', 'MQTT_CLIENT');
