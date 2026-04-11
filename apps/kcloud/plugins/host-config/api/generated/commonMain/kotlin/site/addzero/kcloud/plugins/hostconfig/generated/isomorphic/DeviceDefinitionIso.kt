package site.addzero.kcloud.plugins.hostconfig.generated.isomorphic

import kotlinx.serialization.Serializable

/**
 * 设备定义实体。
 *
 * 一个产品下可以声明多个设备型号定义，
 * 用来描述该型号支持的属性、功能和设备类型归属。
 */
@Serializable
data class DeviceDefinitionIso(
    val id: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val code: String = "",
    val name: String = "",
    val description: String? = null,
    val supportsTelemetry: Boolean = false,
    val supportsControl: Boolean = false,
    val sortIndex: Int = 0,
    val product: ProductDefinitionIso = ProductDefinitionIso(),
    val deviceType: DeviceTypeIso? = null,
    val properties: List<PropertyDefinitionIso> = emptyList(),
    val features: List<FeatureDefinitionIso> = emptyList()
)