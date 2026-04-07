package site.addzero.kcloud.plugins.hostconfig.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class ByteOrder4 {
    ORDER_4321,
    ORDER_1234,
    ORDER_2143,
    ORDER_3412,
}
