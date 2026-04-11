package site.addzero.kcloud.plugins.hostconfig.generated.isomorphic

import java.math.BigDecimal
import kotlinx.serialization.Serializable
import site.addzero.kcloud.plugins.hostconfig.model.enums.PointType

/**
 * 定义标签实体。
 */
@Serializable
data class TagIso(
    val id: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val name: String = "",
    val description: String? = null,
    val registerAddress: Int = 0,
    val enabled: Boolean = false,
    val defaultValue: String? = null,
    val exceptionValue: String? = null,
    val pointType: PointType? = null,
    val debounceMs: Int? = null,
    val sortIndex: Int = 0,
    val scalingEnabled: Boolean = false,
    val scalingOffset: BigDecimal? = null,
    val rawMin: BigDecimal? = null,
    val rawMax: BigDecimal? = null,
    val engMin: BigDecimal? = null,
    val engMax: BigDecimal? = null,
    val forwardEnabled: Boolean = false,
    val forwardRegisterAddress: Int? = null,
    val device: DeviceIso = DeviceIso(),
    val dataType: DataTypeIso = DataTypeIso(),
    val registerType: RegisterTypeIso = RegisterTypeIso(),
    val forwardRegisterType: RegisterTypeIso? = null,
    val valueTexts: List<TagValueTextIso> = emptyList()
)