package site.addzero.kcloud.plugins.hostconfig.model.enums

import kotlinx.serialization.Serializable

@Serializable
/**
 * 定义point类型枚举。
 */
enum class PointType {
    NORMAL,
    PULSE,
}
