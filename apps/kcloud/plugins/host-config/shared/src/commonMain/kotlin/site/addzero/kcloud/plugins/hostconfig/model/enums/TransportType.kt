package site.addzero.kcloud.plugins.hostconfig.model.enums

import kotlinx.serialization.Serializable

@Serializable
/**
 * 定义传输类型枚举。
 */
enum class TransportType {
    TCP,
    RTU,
}
