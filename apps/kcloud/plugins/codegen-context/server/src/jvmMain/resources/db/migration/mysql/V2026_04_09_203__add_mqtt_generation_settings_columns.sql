ALTER TABLE codegen_context_context
    ADD COLUMN mqtt_broker_url VARCHAR(255) NOT NULL DEFAULT 'tcp://127.0.0.1:1883' AFTER tcp_retries,
    ADD COLUMN mqtt_client_id VARCHAR(255) NOT NULL DEFAULT 'modbus-mqtt-client' AFTER mqtt_broker_url,
    ADD COLUMN mqtt_request_topic VARCHAR(255) NOT NULL DEFAULT 'modbus/request' AFTER mqtt_client_id,
    ADD COLUMN mqtt_response_topic VARCHAR(255) NOT NULL DEFAULT 'modbus/response' AFTER mqtt_request_topic,
    ADD COLUMN mqtt_qos INT NOT NULL DEFAULT 1 AFTER mqtt_response_topic,
    ADD COLUMN mqtt_timeout_ms BIGINT NOT NULL DEFAULT 1000 AFTER mqtt_qos,
    ADD COLUMN mqtt_retries INT NOT NULL DEFAULT 2 AFTER mqtt_timeout_ms;
