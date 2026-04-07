package site.addzero.kcloud.plugins.hostconfig.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class TransportType {
    TCP,
    RTU,
}
