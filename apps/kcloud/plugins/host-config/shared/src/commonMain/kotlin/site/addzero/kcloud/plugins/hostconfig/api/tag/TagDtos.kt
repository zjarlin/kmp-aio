package site.addzero.kcloud.plugins.hostconfig.api.tag

import java.math.BigDecimal
import kotlinx.serialization.Serializable
import site.addzero.kcloud.plugins.hostconfig.model.enums.PointType

@Serializable
data class TagCreateRequest(
    val name: String,
    val description: String? = null,
    val dataTypeId: Long,
    val registerTypeId: Long,
    val registerAddress: Int,
    val enabled: Boolean = true,
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
    val forwardRegisterTypeId: Long? = null,
    val forwardRegisterAddress: Int? = null,
)

@Serializable
data class TagUpdateRequest(
    val name: String,
    val description: String? = null,
    val dataTypeId: Long,
    val registerTypeId: Long,
    val registerAddress: Int,
    val enabled: Boolean = true,
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
    val forwardRegisterTypeId: Long? = null,
    val forwardRegisterAddress: Int? = null,
)

@Serializable
data class ReplaceTagValueTextsRequest(
    val items: List<TagValueTextInput>,
)

@Serializable
data class TagValueTextInput(
    val rawValue: String,
    val displayText: String,
    val sortIndex: Int = 0,
)

@Serializable
data class TagResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val dataTypeId: Long,
    val dataTypeCode: String,
    val dataTypeName: String,
    val registerTypeId: Long,
    val registerTypeCode: String,
    val registerTypeName: String,
    val registerAddress: Int,
    val enabled: Boolean,
    val defaultValue: String?,
    val exceptionValue: String?,
    val pointType: PointType?,
    val debounceMs: Int?,
    val sortIndex: Int,
    val scalingEnabled: Boolean,
    val scalingOffset: BigDecimal?,
    val rawMin: BigDecimal?,
    val rawMax: BigDecimal?,
    val engMin: BigDecimal?,
    val engMax: BigDecimal?,
    val forwardEnabled: Boolean,
    val forwardRegisterTypeId: Long?,
    val forwardRegisterTypeCode: String?,
    val forwardRegisterTypeName: String?,
    val forwardRegisterAddress: Int?,
    val valueTexts: List<TagValueTextResponse>,
)

@Serializable
data class TagValueTextResponse(
    val id: Long,
    val rawValue: String,
    val displayText: String,
    val sortIndex: Int,
)

@Serializable
data class TagPositionUpdateRequest(
    val deviceId: Long,
    val sortIndex: Int = 0,
)
