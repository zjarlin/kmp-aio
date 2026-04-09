package site.addzero.kcloud.plugins.hostconfig.model.enums

import kotlinx.serialization.Serializable

@Serializable
/**
 * 定义双字节字节序枚举。
 */
enum class ByteOrder2 {
    ORDER_21,
    ORDER_12,
}
