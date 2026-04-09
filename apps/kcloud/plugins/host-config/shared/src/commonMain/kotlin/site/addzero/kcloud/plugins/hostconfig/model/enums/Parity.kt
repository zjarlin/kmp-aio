package site.addzero.kcloud.plugins.hostconfig.model.enums

import kotlinx.serialization.Serializable

@Serializable
/**
 * 定义校验位枚举。
 */
enum class Parity {
    NONE,
    ODD,
    EVEN,
}
