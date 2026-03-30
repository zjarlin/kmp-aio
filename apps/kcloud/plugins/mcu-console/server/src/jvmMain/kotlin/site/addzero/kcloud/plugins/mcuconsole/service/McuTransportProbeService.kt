package site.addzero.kcloud.plugins.mcuconsole.service

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import site.addzero.kcloud.plugins.mcuconsole.McuModbusTcpProbeRequest
import site.addzero.kcloud.plugins.mcuconsole.McuMqttProbeRequest
import site.addzero.kcloud.plugins.mcuconsole.McuTransportKind
import site.addzero.kcloud.plugins.mcuconsole.McuTransportProbeResponse
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.Instant

class McuTransportProbeService {
    suspend fun probeModbusTcp(
        request: McuModbusTcpProbeRequest,
    ): McuTransportProbeResponse = withContext(Dispatchers.IO) {
        val host = request.host.trim()
        val endpoint = "$host:${request.port}"
        if (host.isBlank()) {
            return@withContext failure(
                transportKind = McuTransportKind.MODBUS_TCP,
                endpoint = endpoint,
                message = "Modbus TCP 主机不能为空",
            )
        }
        if (request.port !in 1..65535) {
            return@withContext failure(
                transportKind = McuTransportKind.MODBUS_TCP,
                endpoint = endpoint,
                message = "Modbus TCP 端口必须在 1..65535 之间",
            )
        }
        if (request.unitId !in 1..255) {
            return@withContext failure(
                transportKind = McuTransportKind.MODBUS_TCP,
                endpoint = endpoint,
                message = "Modbus TCP UnitId 必须在 1..255 之间",
            )
        }
        if (request.timeoutMs <= 0) {
            return@withContext failure(
                transportKind = McuTransportKind.MODBUS_TCP,
                endpoint = endpoint,
                message = "Modbus TCP 超时必须大于 0",
            )
        }

        val master = ModbusTCPMaster(host, request.port)
        master.setTimeout(request.timeoutMs)
        master.setRetries(0)

        return@withContext try {
            master.connect()
            success(
                transportKind = McuTransportKind.MODBUS_TCP,
                endpoint = endpoint,
                message = "Modbus TCP 已连通，UnitId=${request.unitId}，超时=${request.timeoutMs}ms",
            )
        } catch (throwable: Throwable) {
            failure(
                transportKind = McuTransportKind.MODBUS_TCP,
                endpoint = endpoint,
                message = throwable.transportProbeMessage("Modbus TCP 建连失败"),
            )
        } finally {
            runCatching { master.disconnect() }
        }
    }

    suspend fun probeMqtt(
        request: McuMqttProbeRequest,
    ): McuTransportProbeResponse = withContext(Dispatchers.IO) {
        val broker = parseBrokerEndpoint(request.brokerUrl)
            ?: return@withContext failure(
                transportKind = McuTransportKind.MQTT,
                endpoint = request.brokerUrl.trim(),
                message = "MQTT Broker 地址无效，请使用 tcp://host:port 或 ssl://host:port",
            )
        if (request.clientId.isBlank()) {
            return@withContext failure(
                transportKind = McuTransportKind.MQTT,
                endpoint = broker.endpoint,
                message = "MQTT ClientId 不能为空",
            )
        }
        if (request.keepAliveSeconds <= 0) {
            return@withContext failure(
                transportKind = McuTransportKind.MQTT,
                endpoint = broker.endpoint,
                message = "MQTT KeepAlive 必须大于 0",
            )
        }
        if ((request.username.isNotBlank() || request.password.isNotBlank()) && request.username.isBlank()) {
            return@withContext failure(
                transportKind = McuTransportKind.MQTT,
                endpoint = broker.endpoint,
                message = "MQTT 用户名不能为空",
            )
        }

        val clientBuilder = MqttClient.builder()
            .useMqttVersion5()
            .identifier(request.clientId.trim())
            .serverHost(broker.host)
            .serverPort(broker.port)
        if (broker.enableTls) {
            clientBuilder.sslWithDefaultConfig()
        }
        val client = clientBuilder.buildBlocking()

        return@withContext try {
            val connectBuilder = client.connectWith()
                .keepAlive(request.keepAliveSeconds)
            if (request.username.isNotBlank() || request.password.isNotBlank()) {
                val authBuilder = Mqtt5SimpleAuth.builder()
                    .username(request.username.trim())
                if (request.password.isNotBlank()) {
                    authBuilder.password(request.password.toByteArray(StandardCharsets.UTF_8))
                }
                connectBuilder.simpleAuth(authBuilder.build())
            }
            connectBuilder.send()
            success(
                transportKind = McuTransportKind.MQTT,
                endpoint = broker.endpoint,
                message = "MQTT Broker 已连通，clientId=${request.clientId.trim()}",
            )
        } catch (throwable: Throwable) {
            failure(
                transportKind = McuTransportKind.MQTT,
                endpoint = broker.endpoint,
                message = throwable.transportProbeMessage("MQTT 建连失败"),
            )
        } finally {
            runCatching { client.disconnect() }
        }
    }

    private fun parseBrokerEndpoint(
        rawValue: String,
    ): ParsedMqttBrokerEndpoint? {
        val trimmed = rawValue.trim()
        if (trimmed.isBlank()) {
            return null
        }
        val normalized = if ("://" in trimmed) {
            trimmed
        } else {
            "tcp://$trimmed"
        }
        val uri = runCatching { URI(normalized) }.getOrNull() ?: return null
        val scheme = uri.scheme?.lowercase() ?: return null
        val host = uri.host?.trim()?.ifBlank { null } ?: return null
        val port = when {
            uri.port > 0 -> uri.port
            scheme in tlsSchemes -> 8883
            scheme in plainSchemes -> 1883
            else -> return null
        }
        if (port !in 1..65535) {
            return null
        }
        if (uri.rawPath?.takeIf { it.isNotBlank() && it != "/" } != null) {
            return null
        }
        if (uri.rawQuery?.isNotBlank() == true) {
            return null
        }
        if (uri.rawFragment?.isNotBlank() == true) {
            return null
        }
        return when (scheme) {
            in plainSchemes -> ParsedMqttBrokerEndpoint(
                host = host,
                port = port,
                endpoint = "tcp://$host:$port",
                enableTls = false,
            )

            in tlsSchemes -> ParsedMqttBrokerEndpoint(
                host = host,
                port = port,
                endpoint = "ssl://$host:$port",
                enableTls = true,
            )

            else -> null
        }
    }

    private fun success(
        transportKind: McuTransportKind,
        endpoint: String,
        message: String,
    ): McuTransportProbeResponse {
        return McuTransportProbeResponse(
            success = true,
            transportKind = transportKind,
            endpoint = endpoint,
            lastMessage = message,
            verifiedAt = Instant.now().toString(),
        )
    }

    private fun failure(
        transportKind: McuTransportKind,
        endpoint: String,
        message: String,
    ): McuTransportProbeResponse {
        return McuTransportProbeResponse(
            success = false,
            transportKind = transportKind,
            endpoint = endpoint,
            lastMessage = message,
            verifiedAt = Instant.now().toString(),
        )
    }

    private data class ParsedMqttBrokerEndpoint(
        val host: String,
        val port: Int,
        val endpoint: String,
        val enableTls: Boolean,
    )

    companion object {
        private val plainSchemes = setOf("tcp", "mqtt")
        private val tlsSchemes = setOf("ssl", "tls", "mqtts")
    }
}

private fun Throwable.transportProbeMessage(
    fallback: String,
): String {
    return generateSequence(this) { it.cause }
        .mapNotNull { cause -> cause.message?.trim()?.takeIf { it.isNotEmpty() } }
        .firstOrNull()
        ?: fallback
}
