package site.addzero.kcloud.plugins.mcuconsole.service

import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import site.addzero.kcloud.plugins.mcuconsole.McuDeviceProfileIso
import site.addzero.kcloud.plugins.mcuconsole.McuPortSummary
import site.addzero.kcloud.plugins.mcuconsole.McuTransportKind
import site.addzero.kcloud.plugins.mcuconsole.McuTransportProfileIso
import site.addzero.kcloud.plugins.mcuconsole.model.McuDeviceProfile
import site.addzero.kcloud.plugins.mcuconsole.model.McuTransportProfile
import site.addzero.kcloud.plugins.mcuconsole.model.by
import java.time.Instant
import java.util.UUID

class McuConsoleSettingsService(
    private val sqlClient: KSqlClient,
) {
    fun mergeLivePorts(
        livePorts: List<McuPortSummary>,
    ): List<McuPortSummary> {
        val profilesByDeviceKey = allDeviceProfiles().associateBy { profile -> profile.deviceKey }.toMutableMap()
        val now = Instant.now()
        livePorts.forEach { port ->
            val deviceKey = port.deviceKey.trim()
            if (deviceKey.isBlank()) {
                return@forEach
            }
            val existing = profilesByDeviceKey[deviceKey]
            val saved = sqlClient.save(
                new(McuDeviceProfile::class).by {
                    if (existing != null) {
                        id = existing.id
                        createTime = existing.createTime
                    }
                    this.deviceKey = deviceKey
                    serialNumber = port.serialNumber.normalizeNullable() ?: existing?.serialNumber
                    manufacturer = port.manufacturer.normalizeNullable() ?: existing?.manufacturer
                    vendorId = port.vendorId ?: existing?.vendorId
                    productId = port.productId ?: existing?.productId
                    remark = existing?.remark.normalizeNullable()
                    lastPortPath = port.portPath.normalizeNullable()
                    lastPortName = port.portName.normalizeNullable()
                    lastSeenAt = now
                },
            ).modifiedEntity
            profilesByDeviceKey[deviceKey] = saved
        }
        return livePorts.map { port ->
            val persisted = profilesByDeviceKey[port.deviceKey.trim()]
            port.copy(
                remark = persisted?.remark.orEmpty(),
            )
        }
    }

    fun getDeviceProfile(
        deviceKey: String?,
    ): McuDeviceProfileIso {
        val normalizedKey = deviceKey.normalizeNullable().orEmpty()
        if (normalizedKey.isBlank()) {
            return McuDeviceProfileIso()
        }
        return allDeviceProfiles()
            .firstOrNull { profile -> profile.deviceKey == normalizedKey }
            ?.toIso()
            ?: McuDeviceProfileIso(deviceKey = normalizedKey)
    }

    fun saveDeviceProfile(
        request: McuDeviceProfileIso,
    ): McuDeviceProfileIso {
        val deviceKey = request.deviceKey.trim()
        require(deviceKey.isNotBlank()) { "deviceKey 不能为空" }
        val existing = allDeviceProfiles().firstOrNull { profile -> profile.deviceKey == deviceKey }
        return sqlClient.save(
            new(McuDeviceProfile::class).by {
                if (existing != null) {
                    id = existing.id
                    createTime = existing.createTime
                }
                this.deviceKey = deviceKey
                serialNumber = request.serialNumber.normalizeNullable() ?: existing?.serialNumber
                manufacturer = request.manufacturer.normalizeNullable() ?: existing?.manufacturer
                vendorId = request.vendorId ?: existing?.vendorId
                productId = request.productId ?: existing?.productId
                remark = request.remark.normalizeNullable()
                lastPortPath = request.lastPortPath.normalizeNullable() ?: existing?.lastPortPath
                lastPortName = request.lastPortName.normalizeNullable() ?: existing?.lastPortName
                lastSeenAt = request.lastSeenAt?.toJavaInstantCompat() ?: existing?.lastSeenAt
            },
        ).modifiedEntity.toIso()
    }

    fun listTransportProfiles(): List<McuTransportProfileIso> {
        return allTransportProfiles()
            .sortedWith(
                compareByDescending<McuTransportProfile> { profile -> profile.lastUsedAt ?: Instant.EPOCH }
                    .thenByDescending { profile -> profile.updateTime ?: Instant.EPOCH }
                    .thenBy { profile -> profile.name.lowercase() },
            )
            .map(McuTransportProfile::toIso)
    }

    fun saveTransportProfile(
        request: McuTransportProfileIso,
    ): McuTransportProfileIso {
        val normalizedProfileKey = request.profileKey.normalizeNullable() ?: UUID.randomUUID().toString()
        val existing = allTransportProfiles().firstOrNull { profile -> profile.profileKey == normalizedProfileKey }
        val normalizedName = request.name.normalizeNullable()
            ?: existing?.name
            ?: request.transportKind.defaultProfileName()
        return sqlClient.save(
            new(McuTransportProfile::class).by {
                if (existing != null) {
                    id = existing.id
                    createTime = existing.createTime
                }
                profileKey = normalizedProfileKey
                name = normalizedName
                transportKind = request.transportKind
                deviceKey = request.deviceKey.normalizeNullable()
                portPathHint = request.portPathHint.normalizeNullable()
                baudRate = request.baudRate
                unitId = request.unitId
                dataBits = request.dataBits
                stopBits = request.stopBits
                parity = request.parity
                timeoutMs = request.timeoutMs
                retries = request.retries
                host = request.host.normalizeNullable()
                port = request.port
                clientId = request.clientId.normalizeNullable()
                username = request.username.normalizeNullable()
                password = request.password.normalizeNullable()
                publishTopic = request.publishTopic.normalizeNullable()
                subscribeTopic = request.subscribeTopic.normalizeNullable()
                qos = request.qos
                keepAliveSeconds = request.keepAliveSeconds
                lastUsedAt = request.lastUsedAt?.toJavaInstantCompat() ?: existing?.lastUsedAt
            },
        ).modifiedEntity.toIso()
    }

    fun deleteTransportProfile(
        profileKey: String,
    ): List<McuTransportProfileIso> {
        val normalizedProfileKey = profileKey.trim()
        require(normalizedProfileKey.isNotBlank()) { "profileKey 不能为空" }
        val existing = allTransportProfiles().firstOrNull { profile -> profile.profileKey == normalizedProfileKey }
            ?: return listTransportProfiles()
        sqlClient.deleteById(McuTransportProfile::class, existing.id)
        return listTransportProfiles()
    }

    fun markTransportProfileUsed(
        profileKey: String?,
    ) {
        val normalizedProfileKey = profileKey.normalizeNullable() ?: return
        val existing = allTransportProfiles().firstOrNull { profile -> profile.profileKey == normalizedProfileKey } ?: return
        sqlClient.save(
            new(McuTransportProfile::class).by {
                id = existing.id
                this.profileKey = existing.profileKey
                name = existing.name
                transportKind = existing.transportKind
                deviceKey = existing.deviceKey
                portPathHint = existing.portPathHint
                baudRate = existing.baudRate
                unitId = existing.unitId
                dataBits = existing.dataBits
                stopBits = existing.stopBits
                parity = existing.parity
                timeoutMs = existing.timeoutMs
                retries = existing.retries
                host = existing.host
                port = existing.port
                clientId = existing.clientId
                username = existing.username
                password = existing.password
                publishTopic = existing.publishTopic
                subscribeTopic = existing.subscribeTopic
                qos = existing.qos
                keepAliveSeconds = existing.keepAliveSeconds
                lastUsedAt = Instant.now()
                createTime = existing.createTime
            },
        )
    }

    private fun allDeviceProfiles(): List<McuDeviceProfile> {
        return sqlClient.createQuery(McuDeviceProfile::class) {
            select(table)
        }.execute()
    }

    private fun allTransportProfiles(): List<McuTransportProfile> {
        return sqlClient.createQuery(McuTransportProfile::class) {
            select(table)
        }.execute()
    }
}

private fun McuDeviceProfile.toIso(): McuDeviceProfileIso {
    return McuDeviceProfileIso(
        id = id,
        deviceKey = deviceKey,
        serialNumber = serialNumber,
        manufacturer = manufacturer,
        vendorId = vendorId,
        productId = productId,
        remark = remark,
        lastPortPath = lastPortPath,
        lastPortName = lastPortName,
        lastSeenAt = lastSeenAt?.toKotlinInstantCompat(),
        createTime = createTime.toKotlinInstantCompat(),
        updateTime = updateTime?.toKotlinInstantCompat(),
    )
}

private fun McuTransportProfile.toIso(): McuTransportProfileIso {
    return McuTransportProfileIso(
        id = id,
        profileKey = profileKey,
        name = name,
        transportKind = transportKind,
        deviceKey = deviceKey,
        portPathHint = portPathHint,
        baudRate = baudRate,
        unitId = unitId,
        dataBits = dataBits,
        stopBits = stopBits,
        parity = parity,
        timeoutMs = timeoutMs,
        retries = retries,
        host = host,
        port = port,
        clientId = clientId,
        username = username,
        password = password,
        publishTopic = publishTopic,
        subscribeTopic = subscribeTopic,
        qos = qos,
        keepAliveSeconds = keepAliveSeconds,
        lastUsedAt = lastUsedAt?.toKotlinInstantCompat(),
        createTime = createTime.toKotlinInstantCompat(),
        updateTime = updateTime?.toKotlinInstantCompat(),
    )
}

private fun String?.normalizeNullable(): String? {
    return this?.trim()?.ifBlank { null }
}

private fun kotlinx.datetime.Instant.toJavaInstantCompat(): Instant {
    return Instant.ofEpochMilli(toEpochMilliseconds())
}

private fun Instant.toKotlinInstantCompat(): kotlinx.datetime.Instant {
    return kotlinx.datetime.Instant.fromEpochMilliseconds(toEpochMilli())
}

private fun McuTransportKind.defaultProfileName(): String {
    return when (this) {
        McuTransportKind.SERIAL -> "串口连接"
        McuTransportKind.MODBUS_RTU -> "Modbus RTU"
        McuTransportKind.MODBUS_TCP -> "Modbus TCP"
        McuTransportKind.BLUETOOTH -> "蓝牙连接"
        McuTransportKind.MQTT -> "MQTT 连接"
    }
}
